<template>
  <div class="comment-section">
    <div class="header">
      <h3>Comments</h3>
    </div>
    
    <div class="comment-list">
      <div v-if="comments.length === 0" class="empty-text">No comments yet</div>
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <div class="comment-header">
           <div class="user-info">
             <el-avatar v-if="comment.userAvatar" :size="24" :src="comment.userAvatar.startsWith('http') ? comment.userAvatar : `http://localhost:8080${comment.userAvatar}`">
             </el-avatar>
             <el-avatar v-else :size="24" style="background-color: #409EFF; font-size: 12px;">
                {{ (comment.userName || comment.userAccount || '?').charAt(0).toUpperCase() }}
             </el-avatar>
             <span class="username" @click="$emit('view-profile', { 
               userId: comment.userId, 
               name: comment.userName, 
               avatar: comment.userAvatar, 
               account: comment.userAccount 
           })">{{ comment.userName || comment.userAccount }}</span>
             <el-tag size="small" :type="getPermissionType(comment.permission)" style="margin-left: 5px; zoom: 0.8;">
               {{ comment.permission || 'VIEWER' }}
             </el-tag>
           </div>
           <el-button 
             v-if="canDelete(comment)" 
             type="danger" 
             link 
             size="small" 
             @click="handleDelete(comment.id)"
           >✕</el-button>
        </div>
        <div class="comment-content" v-html="formatContent(comment)" @click="handleContentClick"></div>
        <div class="comment-time">{{ formatTime(comment.createdAt) }}</div>
      </div>
    </div>
    
    <div class="comment-input-area">
       <el-input
         ref="commentInputRef"
         v-model="newComment"
         type="textarea"
         :rows="3"
         placeholder="Type a comment... Use @ to mention"
         @input="handleInput"
         @keydown.enter.exact.prevent="submitComment"
         @keydown.up.prevent="navigateList(-1)"
         @keydown.down.prevent="navigateList(1)"
         @keydown.enter.prevent="selectCurrentItem"
       />
       
       <div v-if="showMentions && filteredCollaborators.length > 0" class="mention-list">
          <div 
            v-for="(user, index) in filteredCollaborators" 
            :key="user.userId" 
            class="mention-item"
            :class="{ active: index === activeIndex }"
            @click="insertMention(user)"
            @mouseenter="activeIndex = index"
          >
            <el-avatar v-if="user.avatar" :size="20" :src="user.avatar.startsWith('http') ? user.avatar : `http://localhost:8080${user.avatar}`">
            </el-avatar>
            <el-avatar v-else :size="20" style="background-color: #409EFF; font-size: 10px;">
                 {{ (user.name || user.account || '?').charAt(0).toUpperCase() }}
            </el-avatar>
            <span style="margin-left: 5px">{{ user.name || user.account }}</span>
          </div>
       </div>
       
       <div style="text-align: right; margin-top: 5px;">
         <el-button type="primary" size="small" @click="submitComment" :disabled="!newComment.trim()">Send</el-button>
       </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { addComment, deleteComment, getComments as fetchCommentsApi } from '../api/comment'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { Refresh } from '@element-plus/icons-vue'

const props = defineProps({
  docId: { type: [String, Number], required: true },
  collaborators: { type: Array, default: () => [] },
  ownerId: { type: [Number, String], required: true },
  ownerProfile: { type: Object, default: null }
})

const emit = defineEmits(['view-profile'])

const userStore = useUserStore()
const comments = ref([])
const newComment = ref('')
const showMentions = ref(false)
const mentionQuery = ref('')
const activeIndex = ref(0)
const commentInputRef = ref(null)

const fetchComments = async () => {
  if (!props.docId) return
  try {
    const res = await fetchCommentsApi(props.docId)
    comments.value = res
  } catch (error) {
    console.error(error)
  }
}

watch(() => props.docId, () => {
  fetchComments()
})

defineExpose({ fetchComments })

onMounted(() => {
  fetchComments()
  const interval = setInterval(fetchComments, 30000)
  onUnmounted(() => clearInterval(interval))
})

const getPermissionType = (perm) => {
    switch(perm) {
        case 'OWNER': return 'danger';
        case 'ADMIN': return 'warning';
        case 'EDITOR': return 'success';
        default: return 'info';
    }
}

const formatTime = (time) => {
    if (!time) return ''
    return new Date(time).toLocaleString()
}

const formatContent = (comment) => {
    // Basic XSS protection could be added here
    const safeContent = comment.content

    // Replace @{uid:123} with span
    return safeContent.replace(/@\{uid:(\d+)\}/g, (match, uid) => {
        // Debug log
        // console.log('Formatting comment:', comment.id, 'uid:', uid, 'mentionedUsers:', comment.mentionedUsers)
        
        // 1. Try mentionedUsers from backend
        if (comment.mentionedUsers && comment.mentionedUsers[uid]) {
             const name = comment.mentionedUsers[uid]
             return `<span class="mention" data-user-id="${uid}" style="color: #409EFF; font-weight: bold; cursor: pointer;">@${name}</span>`
        }
        
        // Fallback...
        // Combine collaborators and owner for lookup
        let allUsers = []
        // Add collaborators (only ACCEPTED)
        const acceptedCollaborators = props.collaborators.filter(c => c.status === 'ACCEPTED')
        allUsers = [...acceptedCollaborators]
        
        if (props.ownerProfile) {
            if (!allUsers.find(u => String(u.userId) === String(props.ownerProfile.userId))) {
                allUsers.unshift(props.ownerProfile)
            }
        }

        const user = allUsers.find(c => String(c.userId) === String(uid))
        const name = user ? (user.name || user.account) : 'Unknown'
        return `<span class="mention" data-user-id="${uid}" style="color: #409EFF; font-weight: bold; cursor: pointer;">@${name}</span>`
    })
}

const handleContentClick = (e) => {
    const target = e.target.closest('.mention')
    if (target) {
        const userId = target.getAttribute('data-user-id')
        if (userId) {
            // Find user details to pass consistent object
            // Combine collaborators and owner for lookup
            let allUsers = []
            const acceptedCollaborators = props.collaborators.filter(c => c.status === 'ACCEPTED')
            allUsers = [...acceptedCollaborators]
            if (props.ownerProfile) {
                if (!allUsers.find(u => String(u.userId) === String(props.ownerProfile.userId))) {
                    allUsers.unshift(props.ownerProfile)
                }
            }
            
            const user = allUsers.find(u => String(u.userId) === String(userId))
            if (user) {
                emit('view-profile', user)
            } else {
                // Fallback if user not found in local list (should be rare as we just rendered it)
                 emit('view-profile', { userId })
            }
        }
    }
}

const canDelete = (comment) => {
    if (!userStore.user) return false
    const currentUserId = userStore.user.id
    
    // 1. Author
    if (String(comment.userId) === String(currentUserId)) return true
    
    // 2. Document Owner
    if (String(props.ownerId) === String(currentUserId)) return true
    
    // 3. Document Admin
    const myCollab = props.collaborators.find(c => String(c.userId) === String(currentUserId))
    if (myCollab && myCollab.permission === 'ADMIN') return true
    
    return false
}

const handleDelete = async (commentId) => {
    try {
        await ElMessageBox.confirm('Are you sure you want to delete this comment?', 'Warning', {
            confirmButtonText: 'Delete',
            cancelButtonText: 'Cancel',
            type: 'warning'
        })
        await deleteComment(commentId)
        ElMessage.success('Deleted')
        fetchComments()
    } catch (e) {
        // cancelled or failed
    }
}

// Mention logic
const filteredCollaborators = computed(() => {
    // Current user ID
    const currentUserId = userStore.user ? userStore.user.id : null
    
    // Combine collaborators and owner
    let allUsers = []
    
    // Add collaborators (only ACCEPTED)
    const acceptedCollaborators = props.collaborators.filter(c => c.status === 'ACCEPTED')
    allUsers = [...acceptedCollaborators]
    
    if (props.ownerProfile) {
        // Avoid duplicate if owner is somehow in collaborators list
        if (!allUsers.find(u => String(u.userId) === String(props.ownerProfile.userId))) {
            allUsers.unshift(props.ownerProfile)
        }
    }
    
    // Filter out current user first
    const others = allUsers.filter(c => String(c.userId) !== String(currentUserId))

    if (!mentionQuery.value) return others
    
    const q = mentionQuery.value.toLowerCase()
    return others.filter(c => {
        const name = (c.name || '').toLowerCase()
        const account = (c.account || '').toLowerCase()
        return name.includes(q) || account.includes(q)
    })
})

const handleInput = (val) => {
    nextTick(() => {
        if (!commentInputRef.value || !commentInputRef.value.textarea) return
        
        const el = commentInputRef.value.textarea
        const cursor = el.selectionStart
        const textBefore = val.slice(0, cursor)
        const lastAt = textBefore.lastIndexOf('@')
        
        if (lastAt !== -1) {
            // Check text between @ and cursor
            const query = textBefore.slice(lastAt + 1)
            // If query contains space, we assume mention is done/invalid
            if (query.includes(' ')) {
                showMentions.value = false
                mentionQuery.value = ''
            } else {
                showMentions.value = true
                mentionQuery.value = query
                activeIndex.value = 0
            }
        } else {
            // No @ found before cursor
            showMentions.value = false
            mentionQuery.value = ''
        }
    })
}

const insertMention = (user) => {
    const name = user.name || user.account
    
    // We need to replace the query part with the name
    // Re-calculate position to be safe
    const el = commentInputRef.value.textarea
    const cursor = el.selectionStart
    const textBefore = newComment.value.slice(0, cursor)
    const lastAt = textBefore.lastIndexOf('@')
    
    if (lastAt !== -1) {
        const before = newComment.value.slice(0, lastAt)
        const after = newComment.value.slice(cursor)
        newComment.value = before + '@' + name + ' ' + after
        
        // Move cursor to end of inserted name
        nextTick(() => {
            el.focus()
            const newCursorPos = lastAt + 1 + name.length + 1
            el.setSelectionRange(newCursorPos, newCursorPos)
        })
    }
    
    showMentions.value = false
    mentionQuery.value = ''
}

const navigateList = (step) => {
    if (!showMentions.value) return
    const len = filteredCollaborators.value.length
    if (len === 0) return
    activeIndex.value = (activeIndex.value + step + len) % len
}

const selectCurrentItem = () => {
    if (showMentions.value && filteredCollaborators.value.length > 0) {
        insertMention(filteredCollaborators.value[activeIndex.value])
    }
}

const submitComment = async () => {
    if (showMentions.value) {
        selectCurrentItem()
        return
    }

    if (!newComment.value.trim()) return

    // Permission check
    if (!userStore.user) {
        ElMessage.error('Please login first')
        return
    }

    try {
        // Process mentions
        let processedContent = newComment.value
        
        // Combine collaborators and owner for lookup
        let allUsers = []
        // Add collaborators (only ACCEPTED)
        const acceptedCollaborators = props.collaborators.filter(c => c.status === 'ACCEPTED')
        allUsers = [...acceptedCollaborators]
        
        if (props.ownerProfile) {
            if (!allUsers.find(u => String(u.userId) === String(props.ownerProfile.userId))) {
                allUsers.unshift(props.ownerProfile)
            }
        }

        // Sort collabs by name length to avoid partial matches being replaced incorrectly
        const sortedCollabs = allUsers.sort((a, b) => {
             const nameA = a.name || a.account
             const nameB = b.name || b.account
             return nameB.length - nameA.length
        })

        // Replace @Name with @{uid:ID}
        // We use a regex that matches @Name followed by boundary
        processedContent = processedContent.replace(/@([^\s]+)/g, (match, name) => {
            const col = sortedCollabs.find(c => (c.name || c.account) === name)
            if (col) {
                return `@{uid:${col.userId}}`
            }
            return match
        })

        await addComment(props.docId, { content: processedContent })
        newComment.value = ''
        fetchComments()
    } catch (error) {
        ElMessage.error('Failed to send comment')
    }
}
</script>

<style scoped>
.comment-section {
    display: flex;
    flex-direction: column;
    height: 100%;
    border-left: 1px solid #e0e0e0;
    background-color: #f9f9f9;
}
.header {
    padding: 10px;
    border-bottom: 1px solid #eee;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fff;
}
.header h3 {
    margin: 0;
    font-size: 16px;
}
.comment-list {
    flex: 1;
    overflow-y: auto;
    padding: 10px;
}
.comment-item {
    background: #fff;
    padding: 10px;
    border-radius: 4px;
    margin-bottom: 10px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
.comment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 5px;
}
.user-info {
    display: flex;
    align-items: center;
}
.username {
    font-weight: bold;
    font-size: 13px;
    margin-left: 5px;
    cursor: pointer;
    color: #333; /* Default color */
}
.username:hover {
    color: #409EFF;
}
.comment-content {
    font-size: 14px;
    margin-bottom: 5px;
    word-break: break-word;
}
.comment-time {
    font-size: 11px;
    color: #999;
}
.comment-input-area {
    padding: 10px;
    background: #fff;
    border-top: 1px solid #eee;
    position: relative;
}
.mention-list {
    position: absolute;
    bottom: 100%;
    left: 10px;
    right: 10px;
    background: #fff;
    border: 1px solid #ccc;
    border-radius: 4px;
    box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
    max-height: 150px;
    overflow-y: auto;
    z-index: 10;
}
.mention-item {
    padding: 8px;
    display: flex;
    align-items: center;
    cursor: pointer;
}
.mention-item:hover, .mention-item.active {
    background-color: #f0f0f0;
}
.empty-text {
    text-align: center;
    color: #999;
    margin-top: 20px;
}
</style>
