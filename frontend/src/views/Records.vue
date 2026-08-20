<template>
  <div class="records">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>历史记录管理</span>
        </div>
      </template>

      <!-- 过滤条件 -->
      <div class="filter-container">
        <el-form :inline="true" :model="filterForm" class="demo-form-inline">
          <el-form-item label="检测模式">
            <el-select v-model="filterForm.mode" placeholder="选择检测模式">
              <el-option label="全部" value=""></el-option>
              <el-option label="图片" value="image"></el-option>
              <el-option label="视频" value="video"></el-option>
              <el-option label="摄像头" value="camera"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="模型版本">
            <el-select v-model="filterForm.model" placeholder="选择模型版本">
              <el-option label="全部" value=""></el-option>
              <el-option label="YOLOv8" value="YOLOv8"></el-option>
              <el-option label="YOLOv10" value="YOLOv10"></el-option>
              <el-option label="YOLOv11" value="YOLOv11"></el-option>
              <el-option label="YOLOv12" value="YOLOv12"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="开始日期">
            <el-date-picker v-model="filterForm.startDate" type="date" placeholder="选择开始日期"></el-date-picker>
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker v-model="filterForm.endDate" type="date" placeholder="选择结束日期"></el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 记录列表 -->
      <el-table :data="records" style="width: 100%">
        <el-table-column prop="id" label="记录 ID" width="100"></el-table-column>
        <el-table-column prop="detectionMode" label="检测模式">
          <template #default="scope">
            {{ scope.row.detectionMode === 'image' ? '图片' : scope.row.detectionMode === 'video' ? '视频' : '摄像头' }}
          </template>
        </el-table-column>
        <el-table-column prop="modelVersion" label="模型版本"></el-table-column>
        <el-table-column prop="detectionTime" label="检测时间">
          <template #default="scope">
            {{ formatDate(scope.row.detectionTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewRecord(scope.row)">查看</el-button>
            <el-button type="success" size="small" @click="viewAnalysis(scope.row.id)">查看分析</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        ></el-pagination>
      </div>

      <!-- 查看记录对话框 -->
      <el-dialog
        v-model="dialogVisible"
        title="检测记录详情"
        width="80%"
      >
        <div v-if="currentRecord">
          <div class="record-detail">
            <div class="detail-item">
              <span class="label">记录 ID：</span>
              <span>{{ currentRecord.id }}</span>
            </div>
            <div class="detail-item">
              <span class="label">检测模式：</span>
              <span>{{ currentRecord.detectionMode === 'image' ? '图片' : currentRecord.detectionMode === 'video' ? '视频' : '摄像头' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">模型版本：</span>
              <span>{{ currentRecord.modelVersion }}</span>
            </div>
            <div class="detail-item">
              <span class="label">检测时间：</span>
              <span>{{ formatDate(currentRecord.detectionTime) }}</span>
            </div>
          </div>

          <div class="image-comparison" v-if="currentRecord.detectionMode === 'image'">
            <div class="image-item">
              <h3>原始图像</h3>
              <img v-if="currentRecord.originalImagePath" :src="'data:image/jpeg;base64,' + currentRecord.originalImagePath" alt="原始图像" class="result-image">
              <div v-else class="no-image">无原始图像</div>
            </div>
            <div class="image-item">
              <h3>检测结果</h3>
              <img v-if="currentRecord.resultImagePath" :src="'data:image/jpeg;base64,' + currentRecord.resultImagePath" alt="检测结果" class="result-image">
              <div v-else class="no-image">无检测结果</div>
            </div>
          </div>

          <div class="defect-info" v-if="currentDefects.length > 0">
            <h3>缺陷信息</h3>
            <el-table :data="currentDefects" style="width: 100%">
              <el-table-column prop="defectType" label="缺陷类型"></el-table-column>
              <el-table-column prop="confidence" label="置信度">
                <template #default="scope">
                  {{ scope.row.confidence.toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column label="边界框">
                <template #default="scope">
                  {{ scope.row.x }}, {{ scope.row.y }}, {{ scope.row.w }}, {{ scope.row.h }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-dialog>

      <!-- 查看分析对话框 -->
      <el-dialog
        v-model="analysisDialogVisible"
        title="AI 分析报告"
        width="80%"
      >
        <div v-if="analysisReport" class="analysis-content">
          <div v-html="analysisHtml"></div>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { detectionApi } from '../api'
import { ElMessage } from 'element-plus'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'

// 禁止 markdown 中直接渲染原始 HTML，并配合 DOMPurify 净化输出，防御 XSS
const md = new MarkdownIt({ html: false })

// 过滤条件
const filterForm = ref({
  mode: '',
  model: '',
  startDate: '',
  endDate: ''
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 记录列表
const records = ref([])

// 对话框状态
const dialogVisible = ref(false)
const analysisDialogVisible = ref(false)

// 当前记录和缺陷
const currentRecord = ref(null)
const currentDefects = ref([])
const analysisReport = ref(null)
const analysisHtml = computed(() => {
  if (analysisReport.value) {
    return DOMPurify.sanitize(md.render(analysisReport.value.analysisContent))
  }
  return ''
})

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 查询记录
const fetchRecords = async () => {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (filterForm.value.mode) {
      params.mode = filterForm.value.mode
    }
    if (filterForm.value.model) {
      params.modelVersion = filterForm.value.model
    }
    // 这里可以添加日期过滤逻辑

    const response = await detectionApi.getDetectionRecords(params)
    if (response.code === 200) {
      records.value = response.data
      // 模拟总条数
      total.value = 100
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('获取记录失败，请稍后重试')
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchRecords()
}

// 重置过滤条件
const resetFilter = () => {
  filterForm.value = {
    mode: '',
    model: '',
    startDate: '',
    endDate: ''
  }
  currentPage.value = 1
  fetchRecords()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchRecords()
}

// 处理当前页码变化
const handleCurrentChange = (current) => {
  currentPage.value = current
  fetchRecords()
}

// 查看记录
const viewRecord = async (record) => {
  // 获取完整记录详情，包括图片数据
  try {
    const response = await detectionApi.getDetectionRecord(record.id)
    if (response.code === 200) {
      currentRecord.value = response.data
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('获取记录详情失败，请稍后重试')
  }
  
  // 获取缺陷详情
  try {
    const response = await detectionApi.getDefectDetails(record.id)
    if (response.code === 200) {
      currentDefects.value = response.data
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('获取缺陷详情失败，请稍后重试')
  }
  dialogVisible.value = true
}

// 查看分析
const viewAnalysis = async (recordId) => {
  try {
    const response = await detectionApi.getAiAnalysisReport(recordId)
    if (response.code === 200) {
      analysisReport.value = response.data
      analysisDialogVisible.value = true
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('获取分析报告失败，请稍后重试')
  }
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.records {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-container {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.record-detail {
  margin-bottom: 20px;
}

.detail-item {
  margin-bottom: 10px;
}

.label {
  font-weight: bold;
  margin-right: 10px;
}

.image-comparison {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.image-item {
  flex: 1;
  text-align: center;
}

.image-item h3 {
  margin-bottom: 10px;
  font-size: 16px;
  color: #303133;
}

.result-image {
  max-width: 100%;
  max-height: 400px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.defect-info {
  margin-top: 20px;
}

.defect-info h3 {
  margin-bottom: 10px;
  font-size: 16px;
  color: #303133;
}

.analysis-content {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  max-height: 500px;
  overflow-y: auto;
}
</style>
