<template>
  <div class="users">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAddUser">添加用户</el-button>
        </div>
      </template>

      <!-- 用户列表 -->
      <el-table :data="users" style="width: 100%">
        <el-table-column prop="id" label="用户 ID" width="100"></el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="name" label="姓名"></el-table-column>
        <el-table-column prop="role" label="角色">
          <template #default="scope">
            {{ scope.row.role === 'admin' ? '管理员' : '普通用户' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditUser(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteUser(scope.row.id)">删除</el-button>
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

      <!-- 添加/编辑用户对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="500px"
      >
        <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="80px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="userForm.username" placeholder="请输入用户名"></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password" v-if="!userForm.id">
            <el-input type="password" v-model="userForm.password" placeholder="请输入密码"></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="userForm.name" placeholder="请输入姓名"></el-input>
          </el-form-item>
          <el-form-item label="角色" prop="role">
            <el-select v-model="userForm.role" placeholder="选择角色">
              <el-option label="管理员" value="admin"></el-option>
              <el-option label="普通用户" value="user"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSaveUser">保存</el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 用户列表
const users = ref([])

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('添加用户')

// 用户表单
const userForm = ref({
  id: null,
  username: '',
  password: '',
  name: '',
  role: 'user'
})

const userFormRef = ref(null)

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'blur' }
  ]
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    const response = await userApi.getAllUsers()
    if (response.code === 200) {
      users.value = response.data
      total.value = response.data.length
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败，请稍后重试')
  }
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchUsers()
}

// 处理当前页码变化
const handleCurrentChange = (current) => {
  currentPage.value = current
  fetchUsers()
}

// 处理添加用户
const handleAddUser = () => {
  userForm.value = {
    id: null,
    username: '',
    password: '',
    name: '',
    role: 'user'
  }
  dialogTitle.value = '添加用户'
  dialogVisible.value = true
}

// 处理编辑用户
const handleEditUser = (user) => {
  userForm.value = { ...user }
  // 清空密码字段，编辑时不需要输入密码
  userForm.value.password = ''
  dialogTitle.value = '编辑用户'
  dialogVisible.value = true
}

// 处理保存用户
const handleSaveUser = async () => {
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        if (userForm.value.id) {
          // 编辑用户
          response = await userApi.updateUser(userForm.value.id, userForm.value)
        } else {
          // 添加用户
          response = await userApi.register(userForm.value)
        }
        if (response.code === 200) {
          ElMessage.success(userForm.value.id ? '编辑成功' : '添加成功')
          dialogVisible.value = false
          fetchUsers()
        } else {
          ElMessage.error(response.message)
        }
      } catch (error) {
        ElMessage.error('操作失败，请稍后重试')
      }
    }
  })
}

// 处理删除用户
const handleDeleteUser = (id) => {
  ElMessageBox.confirm('确定要删除该用户吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await userApi.deleteUser(id)
      if (response.code === 200) {
        ElMessage.success('删除成功')
        fetchUsers()
      } else {
        ElMessage.error(response.message)
      }
    } catch (error) {
      ElMessage.error('删除失败，请稍后重试')
    }
  }).catch(() => {
    // 取消删除
  })
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.users {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}
</style>
