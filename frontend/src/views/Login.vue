<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>CoEdit Login</h2>
      </template>
      <el-form :model="form" label-width="80px">
        <el-form-item label="Account">
          <el-input v-model="form.account" />
        </el-form-item>
        <el-form-item label="Password">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <div style="display: flex; justify-content: flex-end; margin-bottom: 10px;">
          <el-button link type="primary" @click="showForgotPassword = true">Forgot Password?</el-button>
        </div>
        <el-form-item>
          <el-button type="primary" @click="handleLogin">Login</el-button>
          <el-button @click="$router.push('/register')">Register</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Forgot Password Dialog -->
    <el-dialog v-model="showForgotPassword" title="Reset Password" width="400px">
      <el-form :model="resetForm" label-width="120px">
        <template v-if="!securityQuestion">
           <el-form-item label="Account">
              <el-input v-model="resetForm.account" placeholder="Enter your account" />
           </el-form-item>
           <el-form-item>
              <el-button type="primary" @click="fetchQuestion">Next</el-button>
           </el-form-item>
        </template>
        
        <template v-else>
           <el-form-item label="Account">
              <el-input v-model="resetForm.account" disabled />
           </el-form-item>
           <el-form-item label="Question">
              <div style="line-height: 1.5; padding-top: 5px;">{{ securityQuestion }}</div>
           </el-form-item>
           <el-form-item label="Answer">
              <el-input v-model="resetForm.securityAnswer" placeholder="Enter security answer" />
           </el-form-item>
           <el-form-item label="New Password">
              <el-input v-model="resetForm.newPassword" type="password" show-password />
           </el-form-item>
           <el-form-item>
              <el-button type="primary" @click="handleResetPassword">Reset Password</el-button>
              <el-button @click="resetFlow">Back</el-button>
           </el-form-item>
        </template>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { getSecurityQuestion, resetPassword } from '../api/user'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  account: '',
  password: ''
})

// Forgot Password State
const showForgotPassword = ref(false)
const resetForm = ref({
  account: '',
  securityAnswer: '',
  newPassword: ''
})
const securityQuestion = ref('')

const fetchQuestion = async () => {
  if (!resetForm.value.account) {
    ElMessage.error('Please enter account')
    return
  }
  try {
    const res = await getSecurityQuestion(resetForm.value.account)
    if (res) {
      securityQuestion.value = res
    } else {
      ElMessage.error('No security question set for this account')
    }
  } catch (error) {
    // Error handled in interceptor or here
    // ElMessage.error('Account not found')
  }
}

const handleResetPassword = async () => {
  if (!resetForm.value.securityAnswer || !resetForm.value.newPassword) {
    ElMessage.error('Please fill in all fields')
    return
  }
  try {
    await resetPassword(resetForm.value)
    ElMessage.success('Password reset successfully')
    showForgotPassword.value = false
    resetFlow()
  } catch (error) {
    // handled
  }
}

const resetFlow = () => {
  securityQuestion.value = ''
  resetForm.value.securityAnswer = ''
  resetForm.value.newPassword = ''
  resetForm.value.account = ''
}

import { useViewStore } from '../stores/view'
const viewStore = useViewStore()

const handleLogin = async () => {
  try {
    await userStore.login(form.value)
    viewStore.reset()
    router.push('/')
  } catch (error) {
    ElMessage.error(error.message || 'Login failed')
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.login-card {
  width: 400px;
}
</style>
