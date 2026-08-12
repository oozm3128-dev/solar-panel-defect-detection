<template>
  <div class="profile">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="userInfo" :rules="basicRules" ref="basicFormRef" label-width="80px">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="userInfo.username" disabled></el-input>
            </el-form-item>
            <el-form-item label="姓名" prop="name">
              <el-input v-model="userInfo.name"></el-input>
            </el-form-item>
            <el-form-item label="角色">
              <el-input v-model="userInfo.role" disabled></el-input>
            </el-form-item>
            <el-form-item label="头像">
              <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :on-change="handleAvatarChange"
                accept="image/*"
              >
                <img v-if="userInfo.avatar" :src="userInfo.avatar" class="avatar">
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateBasic">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input type="password" v-model="passwordForm.oldPassword" placeholder="请输入原密码"></el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码"></el-input>
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input type="password" v-model="passwordForm.confirmPassword" placeholder="请确认新密码"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { userApi, authApi } from '../api'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const activeTab = ref('basic')

// 基本信息表单
const userInfo = ref({
  id: null,
  username: '',
  name: '',
  role: '',
  avatar: ''
})

const basicFormRef = ref(null)
const basicRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ]
}

// 密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordFormRef = ref(null)
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const response = await authApi.getCurrentUser()
    if (response.code === 200) {
      userInfo.value = response.data
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败，请稍后重试')
  }
}

// 处理头像上传
const handleAvatarChange = (file) => {
  // 这里可以添加头像上传逻辑
  // 暂时使用本地预览
  const reader = new FileReader()
  reader.onload = (e) => {
    userInfo.value.avatar = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

// 处理更新基本信息
const handleUpdateBasic = async () => {
  await basicFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await userApi.updateUser(userInfo.value.id, userInfo.value)
        if (response.code === 200) {
          ElMessage.success('修改成功')
          // 更新本地存储的用户信息
          userStore.updateUser(userInfo.value)
        } else {
          ElMessage.error(response.message)
        }
      } catch (error) {
        ElMessage.error('修改失败，请稍后重试')
      }
    }
  })
}

// 处理修改密码
const handleChangePassword = async () => {
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await userApi.changePassword(
          userInfo.value.id,
          passwordForm.value.oldPassword,
          passwordForm.value.newPassword
        )
        if (response.code === 200) {
          ElMessage.success('密码修改成功')
          // 重置表单
          passwordForm.value = {
            oldPassword: '',
            newPassword: '',
            confirmPassword: ''
          }
        } else {
          ElMessage.error(response.message)
        }
      } catch (error) {
        ElMessage.error('密码修改失败，请稍后重试')
      }
    }
  })
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.profile {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>
