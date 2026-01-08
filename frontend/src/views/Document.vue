<template>
  <div class="document-container">
    <div class="header">
      <el-button @click="goBack">Back</el-button>
      <el-input v-model="title" class="title-input" @input="autoSave" :disabled="!canEdit" />
      <div class="collaborators" v-if="canViewCollaborators">
        <el-button size="small" @click="showCollaborators = true">Collaborators</el-button>
      </div>
    </div>
    <div class="main-content">
      <div class="editor-wrapper">
        <div class="remote-cursors-container">
            <div 
                v-for="(cursor, uid) in remoteCursors" 
                :key="uid" 
                class="remote-cursor"
                :style="{ top: cursor.top + 'px', left: cursor.left + 'px', height: cursor.height + 'px' }"
            >
                <div class="cursor-flag" :style="{ backgroundColor: cursor.color }">
                    <span class="cursor-name">{{ cursor.name }}</span>
                </div>
                <div class="cursor-caret" :style="{ backgroundColor: cursor.color }"></div>
            </div>
        </div>
        <QuillEditor 
          v-if="contentLoaded"
          ref="editorRef"
          theme="snow" 
          v-model:content="content" 
          contentType="html"
          :readOnly="!canEdit"
          @textChange="onTextChange"
          @selectionChange="onSelectionChange"
          @ready="onEditorReady"
        />
      </div>
      <div class="sidebar">
        <CommentSection 
          v-if="contentLoaded"
          ref="commentSectionRef"
          :docId="docId"
          :collaborators="collaborators"
          :ownerId="ownerId"
          :ownerProfile="ownerProfile"
          @view-profile="handleViewProfile"
        />
      </div>
    </div>
    <el-dialog v-model="showCollaborators" title="Collaborators">
      <div class="add-collaborator">
        <template v-if="canManage">
            <el-input v-model="newCollaboratorId" placeholder="Account to invite" style="width: 200px" />
            <el-button type="primary" @click="addCollaboratorUser">Add</el-button>
        </template>
        <el-button :icon="Refresh" circle plain @click="fetchCollaborators" title="Refresh list" style="font-size: 18px;" />
      </div>
      <el-table :data="filteredCollaborators" style="width: 100%; margin-top: 20px">
        <el-table-column label="Name">
           <template #default="scope">
              <span 
                style="cursor: pointer; color: #409EFF;" 
                @click="handleViewProfile(scope.row)"
              >
                {{ scope.row.name || scope.row.account }}
              </span>
           </template>
        </el-table-column>
        <el-table-column v-if="canManage" prop="status" label="Status" width="120">
           <template #default="scope">
              <el-tag :type="scope.row.status === 'ACCEPTED' ? 'success' : 'warning'">{{ scope.row.status }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column label="Permission">
          <template #default="scope">
            <el-select v-if="canManage" v-model="scope.row.permission" size="small" @change="updatePermission(scope.row)">
              <el-option label="Viewer" value="VIEWER" />
              <el-option label="Editor" value="EDITOR" />
              <el-option label="Admin" value="ADMIN" />
            </el-select>
            <span v-else>{{ scope.row.permission }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="canManage" label="Action">
          <template #default="scope">
            <el-button size="small" type="danger" @click="removeCollaboratorUser(scope.row.userId)">Remove</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
    <el-dialog v-model="showUserProfile" title="User Profile" width="400px">
        <div class="user-profile-card" v-loading="loadingProfile">
           <div style="display: flex; align-items: center; margin-bottom: 20px;">
               <el-avatar v-if="selectedUser.avatar" :size="60" :src="selectedUser.avatar && selectedUser.avatar.startsWith('http') ? selectedUser.avatar : (selectedUser.avatar ? `http://localhost:8080${selectedUser.avatar}` : '')">
               </el-avatar>
               <el-avatar v-else :size="60" style="background-color: #409EFF; font-size: 24px;">
                  {{ (selectedUser.name || selectedUser.account || '?').charAt(0).toUpperCase() }}
               </el-avatar>
               <div style="margin-left: 15px;">
                   <div style="font-weight: bold; font-size: 18px;">{{ selectedUser.name || selectedUser.account }}</div>
                   <div style="color: #666;">@{{ selectedUser.account }}</div>
               </div>
           </div>
           <p><strong>Email:</strong> {{ selectedUser.email || 'Not set' }}</p>
           <p><strong>Phone:</strong> {{ selectedUser.phone || 'Not set' }}</p>
           <p><strong>Intro:</strong> {{ selectedUser.intro || 'Not set' }}</p>
        </div>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getDocument, updateDocument } from '../api/document'
import { getCollaborators, addCollaborator, removeCollaborator, updateCollaboratorPermission } from '../api/collaborator'
import { getUserProfile } from '../api/user'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import SockJS from 'sockjs-client/dist/sockjs.min.js'
import Stomp from 'stompjs'
import CommentSection from '../components/CommentSection.vue'
import { Quill } from '@vueup/vue-quill'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const docId = route.params.id
const title = ref('')
const content = ref('')
const commentSectionRef = ref(null)
const editorRef = ref(null)
const contentLoaded = ref(false)
const showCollaborators = ref(false)
const collaborators = ref([])
const newCollaboratorId = ref('')
const ownerId = ref(null)
const ownerProfile = ref(null)
const canEdit = ref(false)
const canManage = ref(false)
const canViewCollaborators = ref(false)

const filteredCollaborators = computed(() => {
    if (canManage.value) {
        return collaborators.value
    }
    return collaborators.value.filter(c => c.status === 'ACCEPTED')
})

let version = 1

let stompClient = null
let isRemoteChange = false

const showUserProfile = ref(false)
const selectedUser = ref({})
const loadingProfile = ref(false)

const isInitializing = ref(true)

const fetchDoc = async () => {
  try {
    isInitializing.value = true
    contentLoaded.value = false 
    const doc = await getDocument(docId)
    title.value = doc.title
    content.value = doc.content
    version = doc.version
    ownerId.value = doc.ownerId
    ownerProfile.value = {
        userId: doc.ownerId,
        name: doc.ownerName,
        account: doc.ownerAccount,
        avatar: doc.ownerAvatar,
        permission: 'OWNER'
    }
    await fetchCollaborators()
    checkPermissions()
    contentLoaded.value = true

    setTimeout(() => {
        isInitializing.value = false
    }, 500)
  } catch (error) {
    console.error(error)
    
    router.push('/')
  }
}

const checkPermissions = () => {
    if (!userStore.user) return
    const userId = userStore.user.id
    const docOwnerId = ownerId.value

    canEdit.value = false
    canManage.value = false
    canViewCollaborators.value = false

    if (String(userId) === String(docOwnerId)) {
        canEdit.value = true
        canManage.value = true
        canViewCollaborators.value = true
        return
    }
    
    const myCollab = collaborators.value.find(c => String(c.userId) === String(userId))
    
    if (myCollab && myCollab.status === 'ACCEPTED') {
        canViewCollaborators.value = true
        if (myCollab.permission === 'ADMIN') {
            canEdit.value = true
            canManage.value = true
        } else if (myCollab.permission === 'EDITOR') {
            canEdit.value = true
            canManage.value = false
        } else {
            canEdit.value = false
            canManage.value = false
        }
    }
}

const fetchCollaborators = async () => {
  collaborators.value = await getCollaborators(docId)
}

const addCollaboratorUser = async () => {
  try {
    await addCollaborator({ documentId: docId, userId: newCollaboratorId.value })
    ElMessage.success('Added')
    fetchCollaborators()
    newCollaboratorId.value = ''
  } catch (error) {
  }
}

const removeCollaboratorUser = async (userId) => {
  try {
    await removeCollaborator({ documentId: docId, userId })
    ElMessage.success('Removed')
    fetchCollaborators()
  } catch (error) {
  }
}

const updatePermission = async (collaborator) => {
  try {
    await updateCollaboratorPermission({
      documentId: docId,
      userId: collaborator.userId,
      permission: collaborator.permission
    })
    ElMessage.success('Permission updated')
  } catch (error) {
    ElMessage.error('Failed to update permission')
    fetchCollaborators() 
  }
}

onMounted(async () => {
  await userStore.fetchUser()
  await fetchDoc()
  connectWebSocket()
})

onUnmounted(() => {
  if (stompClient) stompClient.disconnect()
})

const saveDoc = async () => {
  try {
    const res = await updateDocument(docId, {
      title: title.value,
      content: content.value,
      version: version
    })
    version = res.version
  } catch (error) {
    console.error('Auto-save failed', error)
  }
}

const debounce = (fn, delay) => {
  let timeoutId
  return (...args) => {
    clearTimeout(timeoutId)
    timeoutId = setTimeout(() => fn(...args), delay)
  }
}

const autoSave = debounce(async () => {
    if (!canEdit.value) return
    await saveDoc()
}, 1000)

const remoteCursors = ref({})
const cursorColors = ['#ff0000', '#00ff00', '#0000ff', '#ff00ff', '#00ffff', '#ffff00', '#ff8800', '#88ff00', '#0088ff']

const getCollaboratorColor = (id) => {
    if (!id) return '#000'
    const num = String(id).split('').reduce((acc, char) => acc + char.charCodeAt(0), 0)
    return cursorColors[num % cursorColors.length]
}

const onTextChange = ({ delta, oldContents, source }) => {
  if (source === 'user') {
    autoSave()

    if (canEdit.value && stompClient && stompClient.connected) {
      setTimeout(() => {
          const quill = editorRef.value?.getQuill()
          let cursorIndex = null
          let cursorLength = 0
          
          const range = quill?.getSelection()
          if (range) {
              cursorIndex = range.index
              cursorLength = range.length
          }
          
          const message = {
            docId: docId,
            content: null, 
            delta: delta, 
            type: 'EDIT',
            senderId: userStore.user.id,
            senderName: userStore.user.name || userStore.user.account
          }
          
          if (cursorIndex !== null) {
              message.cursorIndex = cursorIndex
              message.cursorLength = cursorLength
              message.senderColor = getCollaboratorColor(userStore.user.id)
          }
          
          stompClient.send(`/app/doc/${docId}/edit`, {}, JSON.stringify(message))
      }, 0)
    }
  }

  if (source === 'user') {
      for (const uid in remoteCursors.value) {
          const cursor = remoteCursors.value[uid]
          if (cursor.index !== undefined && delta && delta.transformPosition) {
              try {
                 const newIndex = delta.transformPosition(cursor.index, false)
                 remoteCursors.value[uid].index = newIndex
              } catch (e) {
                  console.error(e)
              }
          }
      }
      updateCursorPositions()
  }
}

const onSelectionChange = ({ range, oldRange, source }) => {
    if (source === 'user' && range) {
        if (canEdit.value && stompClient && stompClient.connected) {
             stompClient.send(`/app/doc/${docId}/edit`, {}, JSON.stringify({
                docId: docId,
                type: 'CURSOR',
                cursorIndex: range.index,
                cursorLength: range.length,
                senderId: userStore.user.id,
                senderName: userStore.user.name || userStore.user.account,
                senderColor: getCollaboratorColor(userStore.user.id)
            }))
        }
    }
}

const updateCursorPositions = () => {
    const quill = editorRef.value?.getQuill()
    if (!quill) return

    const quillContainer = quill.container

    const cursorContainer = document.querySelector('.remote-cursors-container')
    
    if (!quillContainer || !cursorContainer) return
    
    const containerRect = quillContainer.getBoundingClientRect()
    const wrapperRect = cursorContainer.getBoundingClientRect()

    const offsetTop = containerRect.top - wrapperRect.top
    const offsetLeft = containerRect.left - wrapperRect.left
    
    for (const uid in remoteCursors.value) {
        const cursor = remoteCursors.value[uid]
        if (cursor.index !== undefined) {
             try {
                const length = quill.getLength()
                const index = Math.min(cursor.index, length - 1)
                const bounds = quill.getBounds(index, cursor.length || 0)
                
                if (bounds) {
                    remoteCursors.value[uid] = {
                        ...cursor,
                        top: bounds.top + offsetTop,
                        left: bounds.left + offsetLeft,
                        height: bounds.height
                    }
                }
             } catch (e) {
                 console.error('Error updating cursor pos', e)
             }
        }
    }
}

const connectWebSocket = () => {
  const socket = new SockJS('http://localhost:8080/ws')
  stompClient = Stomp.over(socket)
  stompClient.debug = () => {} 
  stompClient.connect({}, frame => {
    console.log('Connected: ' + frame)
    stompClient.subscribe(`/topic/doc/${docId}`, message => {
      const body = JSON.parse(message.body)
      
      if (body.type === 'COMMENT_UPDATE') {
          commentSectionRef.value?.fetchComments()
          return
      }

      if (body.type === 'COLLABORATOR_REMOVED') {
          if (String(body.targetUserId) === String(userStore.user.id)) {
              ElMessageBox.alert('You have been removed from this document.', 'Access Revoked', {
                  confirmButtonText: 'OK',
                  callback: () => {
                      router.push('/')
                  }
              })
          } else {
              fetchCollaborators()
          }
          return
      }

      if (body.type === 'PERMISSION_UPDATE') {
          if (String(body.targetUserId) === String(userStore.user.id)) {
              ElMessageBox.alert(`Your permission has been updated to ${body.permission}. You will be redirected to the home page.`, 'Permission Changed', {
                  confirmButtonText: 'OK',
                  callback: () => {
                      router.push('/')
                  }
              })
          } else {
              fetchCollaborators()
          }
          return
      }

      if (body.senderId !== userStore.user.id) {
        const quill = editorRef.value?.getQuill()
        
        if (body.type === 'EDIT' && body.delta) {
            if (quill) {
                const Delta = Quill.imports.delta
                const changeDelta = new Delta(body.delta)
                
                for (const uid in remoteCursors.value) {
                    const cursor = remoteCursors.value[uid]
                    if (cursor.index !== undefined) {
                        try {
                             const isSender = (String(uid) === String(body.senderId))
                             
                             if (!isSender) {
                                 const newIndex = changeDelta.transformPosition(cursor.index, false)
                                 remoteCursors.value[uid].index = newIndex
                             }
                        } catch (e) {
                            console.error('Transform cursor error', e)
                        }
                    }
                }

                quill.updateContents(body.delta)

                if (body.cursorIndex !== undefined) {
                    const existingTimer = remoteCursors.value[body.senderId]?.timer
                    
                    remoteCursors.value[body.senderId] = {
                        index: body.cursorIndex,
                        length: body.cursorLength || 0,
                        name: body.senderName,
                        color: body.senderColor || getCollaboratorColor(body.senderId),
                        timestamp: Date.now(),
                        timer: existingTimer
                    }
                    
                     if (remoteCursors.value[body.senderId].timer) clearTimeout(remoteCursors.value[body.senderId].timer)
                     
                     const timerId = setTimeout(() => {
                         if (remoteCursors.value[body.senderId] && remoteCursors.value[body.senderId].timer === timerId) {
                            delete remoteCursors.value[body.senderId]
                         }
                     }, 60000)
                     
                     remoteCursors.value[body.senderId].timer = timerId
                }
                
                updateCursorPositions()
            }
        } else if (body.type === 'CURSOR') {
            remoteCursors.value[body.senderId] = {
                index: body.cursorIndex,
                length: body.cursorLength,
                name: body.senderName,
                color: body.senderColor || getCollaboratorColor(body.senderId),
                timestamp: Date.now(),
                
                timer: remoteCursors.value[body.senderId]?.timer
            }
            updateCursorPositions()

            if (remoteCursors.value[body.senderId].timer) clearTimeout(remoteCursors.value[body.senderId].timer)

             const timerId = setTimeout(() => {
                 if (remoteCursors.value[body.senderId] && remoteCursors.value[body.senderId].timer === timerId) {
                      delete remoteCursors.value[body.senderId]
                 }
             }, 10000) 
            
            remoteCursors.value[body.senderId].timer = timerId
        } else if (!body.type && body.content) {
             if (content.value !== body.content) {
                 content.value = body.content
            }
        }
      }
    })
  })
}

const onEditorReady = (quill) => {
    if (quill.root) {
        quill.root.addEventListener('scroll', updateCursorPositions)
    }
}

const goBack = () => {
  router.back()
}

const handleViewProfile = async (user) => {
    showUserProfile.value = true
    selectedUser.value = user 
    loadingProfile.value = true
    try {
        if (user.userId || user.id) {
            const profile = await getUserProfile(user.userId || user.id)
            selectedUser.value = profile
        }
    } catch (e) {
        console.error(e)
    } finally {
        loadingProfile.value = false
    }
}
</script>
<style scoped>
.document-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.header {
  padding: 10px 20px;
  border-bottom: 1px solid #ccc;
  display: flex;
  align-items: center;
  gap: 20px;
}
.title-input {
  width: 300px;
}
.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.editor-wrapper {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
}
.sidebar {
  width: 300px;
  border-left: 1px solid #ccc;
  background-color: #f9f9f9;
  display: flex;
  flex-direction: column;
}
.collaborators {
  margin-left: auto;
}
.add-collaborator {
    display: flex;
    gap: 10px;
}

.remote-cursors-container {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
    z-index: 99;
    overflow: hidden;  
}

.remote-cursor {
    position: absolute;
    transition: all 0.1s ease;
    pointer-events: none;
}

.cursor-caret {
    position: absolute;
    top: 0;
    left: 0;
    bottom: 0;
    width: 2px;
}

.cursor-flag {
    position: absolute;
    top: -18px;
    left: 0;
    font-size: 10px;
    color: black;
    padding: 1px 4px;
    border-radius: 2px;
    white-space: nowrap;
    opacity: 0;  
    transition: opacity 0.2s;
}

.cursor-flag {
    opacity: 1; 
}

:deep(.el-table .cell) {
  white-space: nowrap;
}
</style>
