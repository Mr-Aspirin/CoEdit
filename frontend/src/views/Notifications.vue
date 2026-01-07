<template>
  <div class="notifications-container">
    <div class="header">
      <div style="display: flex; align-items: center;">
        <el-button @click="goBack" circle :icon="ArrowLeft" style="margin-right: 15px;"></el-button>
        <h2>Notifications</h2>
      </div>
      <el-button @click="fetchNotifications" circle :icon="Refresh" title="Refresh list" style="margin-left: auto; font-size: 18px;"></el-button>
    </div>
    <div v-if="notifications.length === 0">No notifications</div>
    <el-card v-for="item in notifications" :key="item.id" class="notification-card">
      <div class="content">
        <p>{{ item.content }}</p>
        <span class="time">{{ new Date(item.createdAt).toLocaleString() }}</span>
      </div>
      <div class="actions">
        <el-button 
          size="small" 
          :type="item.accepted ? '' : 'primary'" 
          v-if="item.type === 'INVITE'" 
          @click="acceptInvite(item)"
          :disabled="item.accepted"
        >
          {{ item.accepted ? 'Accepted' : 'Accept' }}
        </el-button>
        <el-button size="small" @click="deleteNotify(item.id)">Delete</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, deleteNotification } from '../api/notification'
import { acceptInvitation } from '../api/collaborator'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'

const notifications = ref([])
const router = useRouter()

const goBack = () => {
  router.push('/')
}

const fetchNotifications = async () => {
  try {
    notifications.value = await getNotifications()
  } catch (error) {
    // handled
  }
}

onMounted(() => {
  fetchNotifications()
})

const deleteNotify = async (id) => {
  try {
    await deleteNotification(id)
    fetchNotifications()
  } catch (error) {
    // handled
  }
}

const acceptInvite = async (item) => {
  if (item.type === 'INVITE' && item.relatedId) {
    try {
      await acceptInvitation({ documentId: item.relatedId })
      ElMessage.success('Invitation accepted')
      item.accepted = true
    } catch (error) {
      ElMessage.error(error.message || 'Failed to accept')
    }
  }
}
</script>

<style scoped>
.notifications-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.header h2 {
  margin: 0;
}
.notification-card {
  margin-bottom: 10px;
}
.content {
  margin-bottom: 10px;
}
.time {
  font-size: 12px;
  color: #999;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
