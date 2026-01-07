<template>
  <el-dialog
    v-model="visible"
    title="My Profile"
    width="500px"
    @open="fetchData"
  >
    <el-tabs v-model="activeTab">
      <el-tab-pane label="Basic Info" name="basic">
        <el-form :model="form" label-width="100px">
          <el-form-item label="Avatar">
             <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :http-request="handleAvatarUpload"
            >
              <img v-if="form.avatar" :src="form.avatar.startsWith('http') ? form.avatar : `http://localhost:8080${form.avatar}`" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>
          <el-form-item label="Name">
            <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item label="Email">
            <el-input v-model="form.email" />
          </el-form-item>
          <el-form-item label="Phone">
            <el-input v-model="form.phone" />
          </el-form-item>
          <el-form-item label="Intro">
            <el-input v-model="form.intro" type="textarea" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveBasic">Save Changes</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      
      <el-tab-pane label="Security" name="security">
        <el-form :model="securityForm" label-width="120px">
          <el-alert title="Verify identity to change password" type="info" :closable="false" style="margin-bottom: 20px" />
          
          <div v-if="!verified">
             <el-form-item label="Security Question">
                <el-input v-model="securityQuestionDisplay" disabled />
             </el-form-item>
             <el-form-item label="Answer">
                <el-input v-model="verifyAnswer" placeholder="Enter answer to verify" />
             </el-form-item>
             <el-form-item>
                <el-button type="primary" @click="verifyIdentity">Verify</el-button>
             </el-form-item>
          </div>
          
          <div v-else>
             <el-form-item label="New Password">
                <el-input v-model="securityForm.newPassword" type="password" show-password placeholder="Leave empty to keep unchanged" />
             </el-form-item>
             <el-form-item label="New Question">
                <el-input v-model="securityForm.newQuestion" placeholder="Leave empty to keep unchanged" />
             </el-form-item>
             <el-form-item label="New Answer">
                <el-input v-model="securityForm.newAnswer" placeholder="Leave empty to keep unchanged" />
             </el-form-item>
             <el-form-item>
                <el-button type="primary" @click="saveSecurity">Update Security</el-button>
                <el-button @click="verified = false">Cancel</el-button>
             </el-form-item>
          </div>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getUserProfile, updateUserProfile, uploadFile, verifySecurity, updateSecurity } from '../api/user'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  userId: [Number, String]
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const activeTab = ref('basic')
const userStore = useUserStore()

const form = ref({
  id: null,
  name: '',
  email: '',
  phone: '',
  avatar: '',
  intro: ''
})

const securityQuestionDisplay = ref('')
const verifyAnswer = ref('')
const verified = ref(false)

const securityForm = ref({
  newPassword: '',
  newQuestion: '',
  newAnswer: ''
})

const fetchData = async () => {
  if (!props.userId) return
  try {
    const data = await getUserProfile(props.userId)
    form.value = { ...data }
    securityQuestionDisplay.value = data.securityQuestion || 'Not set'
    verified.value = false
    verifyAnswer.value = ''
    securityForm.value = {
      newPassword: '',
      newQuestion: '',
      newAnswer: ''
    }
  } catch (e) {
    console.error(e)
  }
}

const handleAvatarUpload = async (options) => {
  try {
    const res = await uploadFile(options.file)
    form.value.avatar = res // Assuming res is the URL path
    ElMessage.success('Avatar uploaded')
  } catch (e) {
    ElMessage.error('Upload failed')
  }
}

const saveBasic = async () => {
  try {
    await updateUserProfile(form.value)
    ElMessage.success('Profile updated')
    // Update store if it's current user
    if (userStore.user && userStore.user.id === form.value.id) {
        userStore.user = { ...userStore.user, ...form.value }
    }
    visible.value = false
  } catch (e) {
    ElMessage.error(e.message || 'Update failed')
  }
}

const verifyIdentity = async () => {
  try {
    const res = await verifySecurity({
        userId: form.value.id,
        answer: verifyAnswer.value
    })
    if (res) {
        verified.value = true
        ElMessage.success('Verified')
    } else {
        ElMessage.error('Incorrect answer')
    }
  } catch (e) {
    ElMessage.error('Verification failed')
  }
}

const saveSecurity = async () => {
  try {
    await updateSecurity({
        userId: form.value.id,
        newPassword: securityForm.value.newPassword,
        newQuestion: securityForm.value.newQuestion,
        newAnswer: securityForm.value.newAnswer
    })
    ElMessage.success('Security settings updated')
    visible.value = false
    verified.value = false
  } catch (e) {
    ElMessage.error(e.message || 'Update failed')
  }
}
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  display: block;
  border-radius: 50%;
  object-fit: cover;
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
}
</style>
