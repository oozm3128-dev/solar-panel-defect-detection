<template>
  <div class="app">
    <el-container style="height: 100vh;">
      <!-- 侧边栏 -->
      <el-aside width="200px" style="background-color: #303133;">
        <div class="logo" style="color: white; text-align: center; padding: 20px 0;">
          <h2>光伏电池板缺陷检测系统</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical-demo"
          background-color="#303133"
          text-color="#fff"
          active-text-color="#409EFF"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <span>数据大盘</span>
          </el-menu-item>
          <el-menu-item index="/detection">
            <el-icon><Camera /></el-icon>
            <span>检测工作台</span>
          </el-menu-item>
          <el-menu-item index="/records">
            <el-icon><Document /></el-icon>
            <span>历史记录</span>
          </el-menu-item>
          <el-menu-item index="/users" v-if="user.role === 'admin'">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><Setting /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
          <el-menu-item index="/login" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航栏 -->
        <el-header style="background-color: #fff; border-bottom: 1px solid #e4e7ed;">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <span>{{ currentPage }}</span>
            </div>
            <div style="display: flex; align-items: center;">
              <el-avatar :size="32" :src="user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" style="margin-right: 10px;"></el-avatar>
              <span>{{ user.name }}</span>
            </div>
          </div>
        </el-header>

        <!-- 内容区 -->
        <el-main>
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { HomeFilled, Camera, Document, User, Setting, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from './store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = ref('/dashboard')
const currentPage = ref('数据大盘')
const user = computed(() => userStore.user)

// 处理菜单选择
const handleMenuSelect = (key) => {
  router.push(key)
}

// 处理退出登录
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

// 监听路由变化，更新当前页面标题和活跃菜单
onMounted(() => {
  // 从本地存储加载用户信息
  userStore.loadUserFromStorage()

  // 监听路由变化
  router.beforeEach((to, from, next) => {
    activeMenu.value = to.path
    // 更新当前页面标题
    switch (to.path) {
      case '/dashboard':
        currentPage.value = '数据大盘'
        break
      case '/detection':
        currentPage.value = '检测工作台'
        break
      case '/records':
        currentPage.value = '历史记录'
        break
      case '/users':
        currentPage.value = '用户管理'
        break
      case '/profile':
        currentPage.value = '个人中心'
        break
      default:
        currentPage.value = '数据大盘'
    }
    next()
  })
})
</script>

<style scoped>
.app {
  height: 100vh;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
