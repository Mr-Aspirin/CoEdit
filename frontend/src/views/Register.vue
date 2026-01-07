<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <h2>CoEdit Register</h2>
      </template>
      <el-form :model="form" label-width="150px">
        <el-form-item label="Account">
          <el-input v-model="form.account" />
        </el-form-item>
        <el-form-item label="Name">
          <el-input v-model="form.name" placeholder="Display name" />
        </el-form-item>
        <el-form-item label="Password">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="Security Question">
          <el-input v-model="form.securityQuestion" placeholder="e.g. What is your pet's name?" />
        </el-form-item>
        <el-form-item label="Security Answer">
          <el-input v-model="form.securityAnswer" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister">Register</el-button>
          <el-button @click="$router.push('/login')">Back to Login</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()

const form = ref({
  account: '',
  name: '',
  password: '',
  securityQuestion: '',
  securityAnswer: ''
})

const handleRegister = async () => {
  if (!form.value.name || !form.value.account || !form.value.password) {
      ElMessage.error('Please fill in all required fields')
      return
  }
  try {
    await register(form.value)
    ElMessage.success('Registration successful, please login')
    router.push('/login')
  } catch (error) {
    // Error handled in request interceptor
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.register-card {
  width: 500px;
}
</style>
