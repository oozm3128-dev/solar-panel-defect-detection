<template>
  <div class="detection-container">
    <h1>检测工作台</h1>
    <div class="model-selector">
      <el-select v-model="selectedModel" placeholder="选择模型">
        <el-option label="YOLOv8" value="YOLOv8"></el-option>
        <el-option label="YOLOv10" value="YOLOv10"></el-option>
        <el-option label="YOLOv11" value="YOLOv11"></el-option>
        <el-option label="YOLOv12" value="YOLOv12"></el-option>
      </el-select>
    </div>
    
    <el-tabs v-model="activeTab">
      <el-tab-pane label="图片检测示例" name="image">
        <div class="detection-content">
          <div class="upload-area">
            <el-upload
              class="upload-demo"
              action="#"
              :auto-upload="false"
              :on-change="handleFileChange"
              :limit="1"
              :file-list="fileList"
            >
              <el-button type="primary">点击上传</el-button>
              <template #tip>
                <div class="el-upload__tip">请上传 JPG、PNG 格式的图片</div>
              </template>
            </el-upload>
          </div>
          
          <div v-if="imageUrl" class="image-preview">
            <img :src="imageUrl" alt="预览" class="preview-image">
          </div>
          
          <div v-if="detectionResult" class="result-area">
            <h3>检测结果</h3>
            <div class="result-image">
              <img :src="resultImage" alt="检测结果" class="result-img">
            </div>
            <div class="defects-list">
              <h4>缺陷列表</h4>
              <el-table :data="defects" style="width: 100%">
                <el-table-column prop="type" label="缺陷类型"></el-table-column>
                <el-table-column prop="confidence" label="置信度">
                  <template #default="scope">
                    {{ (scope.row.confidence * 100).toFixed(2) }}%
                  </template>
                </el-table-column>
                <el-table-column prop="bbox" label="位置">
                  <template #default="scope">
                    {{ scope.row.bbox.join(', ') }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <el-button type="primary" @click="analyzeDefects" :loading="analyzing">
              分析缺陷
            </el-button>
            <div v-if="analysisResult" class="analysis-result">
              <h4>AI 分析报告</h4>
              <div class="analysis-content">{{ analysisResult }}</div>
            </div>
          </div>
          
          <el-button 
            type="success" 
            @click="startDetection" 
            :loading="loading"
            :disabled="!imageUrl"
          >
            开始检测
          </el-button>
          <el-button 
            type="warning" 
            @click="markAsUnrecognized"
            :disabled="!imageUrl"
          >
            未能成功识别
          </el-button>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="视频检测" name="video">
        <div class="detection-content">
          <div class="video-area">
            <video ref="video" width="640" height="480" autoplay></video>
            <div class="camera-controls">
              <el-button type="primary" @click="startCamera">开启摄像头</el-button>
              <el-button type="danger" @click="stopCamera">关闭摄像头</el-button>
              <el-button type="success" @click="captureFrame">捕获帧并检测</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="摄像头检测" name="camera">
        <div class="detection-content">
          <div class="camera-area">
            <video ref="camera" width="640" height="480" autoplay></video>
            <div class="camera-controls">
              <el-button type="primary" @click="startCamera">开启摄像头</el-button>
              <el-button type="danger" @click="stopCamera">关闭摄像头</el-button>
              <el-button type="success" @click="startRealTimeDetection">开始实时检测</el-button>
              <el-button type="warning" @click="stopRealTimeDetection">停止实时检测</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { detectionApi } from '../api'

const selectedModel = ref('YOLOv11')
const activeTab = ref('image')
const fileList = ref([])
const imageUrl = ref('')
const loading = ref(false)
const detectionResult = ref(null)
const resultImage = ref('')
const defects = ref([])
const analyzing = ref(false)
const analysisResult = ref('')
const video = ref(null)
const camera = ref(null)
const cameraActive = ref(false)
const realTimeDetection = ref(false)

const handleFileChange = (file) => {
  fileList.value = [file]
  const reader = new FileReader()
  reader.onload = (e) => {
    imageUrl.value = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const startDetection = async () => {
  if (!fileList.value.length) {
    ElMessage.warning('请先上传图片')
    return
  }
  
  loading.value = true
  try {
    const formData = new FormData()
    formData.append('file', fileList.value[0].raw)
    formData.append('modelVersion', selectedModel.value)
    
    const response = await detectionApi.detectImage(formData)
    if (response.code === 200) {
      detectionResult.value = response.data
      resultImage.value = `data:image/jpeg;base64,${response.data.result_image}`
      defects.value = response.data.defects
      ElMessage.success('检测成功')
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('检测失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const startCamera = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ video: true })
    video.value.srcObject = stream
    cameraActive.value = true
  } catch (error) {
    ElMessage.error('无法访问摄像头')
  }
}

const stopCamera = () => {
  if (video.value && video.value.srcObject) {
    video.value.srcObject.getTracks().forEach(track => track.stop())
    cameraActive.value = false
  }
}

const captureFrame = () => {
  if (!cameraActive.value) {
    ElMessage.warning('请先开启摄像头')
    return
  }
  
  const canvas = document.createElement('canvas')
  canvas.width = video.value.videoWidth
  canvas.height = video.value.videoHeight
  const ctx = canvas.getContext('2d')
  ctx.drawImage(video.value, 0, 0, canvas.width, canvas.height)
  
  canvas.toBlob(async (blob) => {
    const formData = new FormData()
    formData.append('file', blob, 'capture.jpg')
    formData.append('modelVersion', selectedModel.value)
    
    loading.value = true
    try {
      const response = await detectionApi.detectImage(formData)
      if (response.code === 200) {
        detectionResult.value = response.data
        resultImage.value = `data:image/jpeg;base64,${response.data.result_image}`
        defects.value = response.data.defects
        ElMessage.success('检测成功')
      } else {
        ElMessage.error(response.message)
      }
    } catch (error) {
      ElMessage.error('检测失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

const startRealTimeDetection = () => {
  if (!cameraActive.value) {
    ElMessage.warning('请先开启摄像头')
    return
  }
  
  realTimeDetection.value = true
  ElMessage.success('开始实时检测')
}

const stopRealTimeDetection = () => {
  realTimeDetection.value = false
  ElMessage.success('停止实时检测')
}

const analyzeDefects = async () => {
  if (!detectionResult.value) {
    ElMessage.warning('请先进行检测')
    return
  }
  
  analyzing.value = true
  try {
    const recordId = detectionResult.value.record_id
    if (!recordId) {
      ElMessage.error('检测结果中没有记录ID')
      return
    }
    const response = await detectionApi.analyzeDefects(recordId)
    if (response.code === 200) {
      analysisResult.value = response.data.analysis_content
      ElMessage.success('分析成功')
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('分析失败，请稍后重试')
  } finally {
    analyzing.value = false
  }
}

const markAsUnrecognized = async () => {
  if (!fileList.value.length) {
    ElMessage.warning('请先上传图片')
    return
  }
  
  try {
    const formData = new FormData()
    formData.append('file', fileList.value[0].raw)
    
    const response = await detectionApi.markUnrecognized(formData)
    
    if (response.code === 200) {
      ElMessage.success('图片已标记为未能成功识别并存储')
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败，请稍后重试')
  }
}
</script>

<style scoped>
.detection-container {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.detection-container h1 {
  text-align: center;
  color: #2c3e50;
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 30px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.model-selector {
  margin-bottom: 30px;
  display: flex;
  justify-content: center;
}

.model-selector .el-select {
  width: 200px;
}

.detection-content {
  margin-top: 30px;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}

.upload-area {
  margin-bottom: 30px;
  text-align: center;
}

.upload-demo {
  border: 2px dashed #409eff;
  border-radius: 8px;
  padding: 40px 20px;
  transition: all 0.3s ease;
}

.upload-demo:hover {
  border-color: #66b1ff;
  background-color: rgba(64, 158, 255, 0.05);
}

.upload-demo .el-button {
  font-size: 16px;
  padding: 12px 24px;
}

.image-preview {
  margin: 30px 0;
  text-align: center;
}

.preview-image {
  max-width: 100%;
  max-height: 500px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  transition: transform 0.3s ease;
}

.preview-image:hover {
  transform: scale(1.02);
}

.result-area {
  margin-top: 30px;
  padding: 30px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  transition: box-shadow 0.3s ease;
}

.result-area:hover {
  box-shadow: 0 6px 24px rgba(0,0,0,0.15);
}

.result-area h3 {
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  border-bottom: 2px solid #409eff;
  padding-bottom: 10px;
}

.result-image {
  margin: 25px 0;
  text-align: center;
}

.result-img {
  max-width: 100%;
  max-height: 500px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  transition: transform 0.3s ease;
}

.result-img:hover {
  transform: scale(1.02);
}

.defects-list {
  margin: 30px 0;
}

.defects-list h4 {
  color: #34495e;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 15px;
}

.defects-list .el-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.defects-list .el-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.defects-list .el-table tr:hover {
  background-color: #f5f7fa;
}

.analysis-result {
  margin-top: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #f0f4f8 0%, #e9ecef 100%);
  border-radius: 8px;
  border-left: 4px solid #409eff;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.analysis-result h4 {
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 15px;
}

.analysis-content {
  line-height: 1.6;
  color: #34495e;
  white-space: pre-wrap;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
}

.analysis-content h1, .analysis-content h2, .analysis-content h3 {
  color: #2c3e50;
  margin-top: 20px;
  margin-bottom: 10px;
}

.analysis-content h1 {
  font-size: 20px;
  border-bottom: 1px solid #e9ecef;
  padding-bottom: 10px;
}

.analysis-content h2 {
  font-size: 18px;
}

.analysis-content h3 {
  font-size: 16px;
}

.analysis-content ul, .analysis-content ol {
  margin-left: 20px;
  margin-bottom: 15px;
}

.analysis-content li {
  margin-bottom: 5px;
}

.analysis-content strong {
  color: #2c3e50;
  font-weight: 600;
}

.video-area, .camera-area {
  margin-top: 30px;
  text-align: center;
}

.video-area video, .camera-area video {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  margin-bottom: 20px;
}

.camera-controls {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.camera-controls .el-button {
  padding: 10px 20px;
  font-size: 14px;
}

.el-button {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.el-tabs {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  overflow: hidden;
}

.el-tabs__header {
  background: #f8f9fa;
  padding-left: 20px;
  border-bottom: 1px solid #e9ecef;
}

.el-tabs__tab {
  font-size: 16px;
  padding: 15px 20px;
  color: #6c757d;
  transition: all 0.3s ease;
}

.el-tabs__tab:hover {
  color: #409eff;
}

.el-tabs__tab.is-active {
  color: #409eff;
  font-weight: 600;
}

.el-tabs__content {
  padding: 20px;
}

@media (max-width: 768px) {
  .detection-container {
    padding: 10px;
  }
  
  .detection-container h1 {
    font-size: 24px;
  }
  
  .result-area {
    padding: 20px;
  }
  
  .upload-demo {
    padding: 30px 10px;
  }
  
  .camera-controls {
    flex-direction: column;
    align-items: center;
  }
  
  .camera-controls .el-button {
    width: 200px;
  }
}
</style>