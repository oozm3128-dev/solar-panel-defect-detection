<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="card-content">
            <div class="card-title">总检测次数</div>
            <div class="card-value">{{ statistics.totalDetection }}</div>
            <div class="card-desc">本月新增 {{ statistics.monthlyDetection }} 次</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="card-content">
            <div class="card-title">总缺陷数量</div>
            <div class="card-value">{{ statistics.totalDefects }}</div>
            <div class="card-desc">本月新增 {{ statistics.monthlyDefects }} 个</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="card-content">
            <div class="card-title">检测准确率</div>
            <div class="card-value">{{ statistics.accuracy }}%</div>
            <div class="card-desc">较上月提升 2.5%</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="card-content">
            <div class="card-title">模型使用频率</div>
            <div class="card-value">{{ statistics.modelUsage }}</div>
            <div class="card-desc">{{ modelUsageDesc }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>缺陷类型占比</span>
            </div>
          </template>
          <div ref="defectTypeChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>每日检测数量趋势</span>
            </div>
          </template>
          <div ref="dailyTrendChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>模型使用频率</span>
            </div>
          </template>
          <div ref="modelUsageChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>检测模式分布</span>
            </div>
          </template>
          <div ref="detectionModeChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import * as echarts from 'echarts'
import { detectionApi } from '../api'
import { ElMessage } from 'element-plus'

const statistics = ref({
  totalDetection: 0,
  monthlyDetection: 0,
  totalDefects: 0,
  monthlyDefects: 0,
  accuracy: 0,
  modelUsage: '-'
})

const defectTypeChart = ref(null)
const dailyTrendChart = ref(null)
const modelUsageChart = ref(null)
const detectionModeChart = ref(null)

let defectTypeInstance = null
let dailyTrendInstance = null
let modelUsageInstance = null
let detectionModeInstance = null

const modelUsageDesc = computed(() => {
  if (statistics.value.modelStats && statistics.value.modelStats.length > 0) {
    const topModel = statistics.value.modelStats.reduce((max, item) =>
      parseInt(item.COUNT) > parseInt(max.COUNT) ? item : max
    , statistics.value.modelStats[0])
    return `${topModel.MODEL_VERSION || 'N/A'} 使用最多`
  }
  return '暂无数据'
})

const fetchStatistics = async () => {
  try {
    const response = await detectionApi.getStatistics()
    if (response.code === 200) {
      const data = response.data
      statistics.value = {
        totalDetection: data.totalDetection || 0,
        monthlyDetection: data.monthlyDetection || 0,
        totalDefects: data.totalDefects || 0,
        monthlyDefects: data.monthlyDefects || 0,
        accuracy: data.accuracy || 0,
        modelUsage: data.modelUsage || '-',
        defectTypeStats: data.defectTypeStats || [],
        dailyStats: data.dailyStats || [],
        modelStats: data.modelStats || [],
        modeStats: data.modeStats || []
      }
      initCharts()
    } else {
      ElMessage.error(response.message || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败，请稍后重试')
  }
}

const initCharts = () => {
  if (defectTypeChart.value) {
    if (defectTypeInstance) defectTypeInstance.dispose()
    defectTypeInstance = echarts.init(defectTypeChart.value)

    const defectData = statistics.value.defectTypeStats.map(item => ({
      name: item.DEFECT_TYPE || '未知',
      value: parseInt(item.COUNT) || 0
    }))

    if (defectData.length === 0) {
      defectData.push({ name: '暂无数据', value: 0 })
    }

    defectTypeInstance.setOption({
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '缺陷类型',
          type: 'pie',
          radius: '50%',
          data: defectData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
  }

  if (dailyTrendChart.value) {
    if (dailyTrendInstance) dailyTrendInstance.dispose()
    dailyTrendInstance = echarts.init(dailyTrendChart.value)

    const dailyData = statistics.value.dailyStats.map(item => ({
      date: item.DATE || '',
      count: parseInt(item.COUNT) || 0
    }))

    if (dailyData.length === 0) {
      dailyData.push({ date: '暂无数据', count: 0 })
    }

    dailyTrendInstance.setOption({
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: dailyData.map(d => d.date)
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          data: dailyData.map(d => d.count),
          type: 'line',
          smooth: true
        }
      ]
    })
  }

  if (modelUsageChart.value) {
    if (modelUsageInstance) modelUsageInstance.dispose()
    modelUsageInstance = echarts.init(modelUsageChart.value)

    const modelData = statistics.value.modelStats.map(item => ({
      name: item.MODEL_VERSION || '未知',
      value: parseInt(item.COUNT) || 0
    }))

    if (modelData.length === 0) {
      modelData.push({ name: '暂无数据', value: 0 })
    }

    modelUsageInstance.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      xAxis: {
        type: 'category',
        data: modelData.map(d => d.name)
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          data: modelData.map(d => d.value),
          type: 'bar'
        }
      ]
    })
  }

  if (detectionModeChart.value) {
    if (detectionModeInstance) detectionModeInstance.dispose()
    detectionModeInstance = echarts.init(detectionModeChart.value)

    const modeData = statistics.value.modeStats.map(item => ({
      name: item.DETECTION_MODE === 'image' ? '图片' : item.DETECTION_MODE === 'video' ? '视频' : '摄像头',
      value: parseInt(item.COUNT) || 0
    }))

    if (modeData.length === 0) {
      modeData.push({ name: '暂无数据', value: 0 })
    }

    detectionModeInstance.setOption({
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '检测模式',
          type: 'pie',
          radius: '50%',
          data: modeData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
  }
}

const handleResize = () => {
  if (defectTypeInstance) defectTypeInstance.resize()
  if (dailyTrendInstance) dailyTrendInstance.resize()
  if (modelUsageInstance) modelUsageInstance.resize()
  if (detectionModeInstance) detectionModeInstance.resize()
}

onMounted(() => {
  fetchStatistics()
  window.addEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.card-content {
  text-align: center;
  padding: 20px 0;
}

.card-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.card-desc {
  font-size: 12px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart {
  width: 100%;
  height: 300px;
}
</style>
