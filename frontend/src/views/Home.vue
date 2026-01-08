<template>
  <div class="home-container">
    <el-container style="height: 100vh;">
      <el-header class="header">
        <div class="logo">CoEdit</div>
        <div class="user-info" v-if="userStore.user">
          <el-dropdown>
            <span class="el-dropdown-link">
              <el-avatar 
                v-if="userStore.user.avatar" 
                :size="30" 
                :src="userStore.user.avatar.startsWith('http') ? userStore.user.avatar : `http://localhost:8080${userStore.user.avatar}`" 
                style="margin-right: 8px;" 
              />
              <el-avatar v-else :size="30" style="margin-right: 8px; background-color: #409EFF;">{{ (userStore.user.name || userStore.user.account || '?').charAt(0).toUpperCase() }}</el-avatar>
              {{ userStore.user.name || userStore.user.account }}
              <el-icon class="el-icon--right">
                <arrow-down />
              </el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="openProfile">Profile</el-dropdown-item>
                <el-dropdown-item @click="handleLogout">Logout</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div v-else>
          <el-button type="primary" @click="$router.push('/login')">Login</el-button>
        </div>
      </el-header>
      <el-container v-if="userStore.user" style="height: calc(100vh - 60px);">
        <el-aside width="250px" class="aside" style="display: flex; flex-direction: column;">
          <div class="folder-header">
            <span>Folders</span>
            <el-button type="text" @click="openCreateFolder">
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
          <el-menu :default-active="activeFolder" @select="handleFolderSelect" class="folder-menu" style="flex: 1; overflow-y: auto;">
            <el-menu-item index="all">
              <el-icon><Document /></el-icon>
              <span>All Documents</span>
            </el-menu-item>
            <el-menu-item index="root">
              <el-icon><Folder /></el-icon>
              <span>Uncategorized</span>
            </el-menu-item>
            <el-menu-item v-for="folder in folders" :key="folder.id" :index="String(folder.id)" class="folder-menu-item">
              <el-icon><Folder /></el-icon>
              <span class="folder-name">{{ folder.name }}</span>
              <el-dropdown trigger="click" style="margin-left: auto; margin-right: 10px" @command="(cmd) => handleFolderCommand(cmd, folder)">
                <span class="el-dropdown-link" @click.stop>
                  <el-icon><More /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="rename">Rename</el-dropdown-item>
                    <el-dropdown-item command="delete" style="color: red">Delete</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </el-menu-item>
          </el-menu>
          <div class="notification-area" @click="handleNotificationsClick">
            <el-icon><Bell /></el-icon>
            <span style="margin-left: 10px;">Notifications</span>
            <div v-if="hasUnread" class="notification-dot"></div>
          </div>
        </el-aside>
        <el-main>
          <div class="actions">
            <el-button type="primary" @click="createNewDoc">New Document</el-button>
            <el-button @click="openAdvancedSearch">Advanced Search</el-button>
            <div style="flex: 1"></div>
            <el-input 
              v-model="searchKeyword" 
              placeholder="Quick search..." 
              style="width: 250px;" 
              @keyup.enter="handleSearch"
            >
              <template #append>
                <el-button :icon="Search" @click="handleSearch" />
              </template>
            </el-input>
          </div>
          <div class="current-path" style="margin: 15px 0; display: flex; justify-content: space-between; align-items: center;">
            <div style="color: #666; font-size: 14px;">
              Current View: <strong>{{ currentFolderName }}</strong>
              <el-button v-if="isSearching" type="primary" link @click="clearSearch" style="margin-left: 10px; padding: 0;">
                Clear Search
                <el-icon class="el-icon--right"><Close /></el-icon>
              </el-button>
            </div>
            <el-button :icon="Refresh" circle @click="fetchDocs" title="Refresh list" />
          </div>
          <el-table :data="filteredDocuments" style="width: 100%">
            <el-table-column prop="title" label="Title" />
            <el-table-column prop="ownerName" label="Owner" width="150">
              <template #default="scope">
                <span 
                  style="cursor: pointer; color: #409EFF;" 
                  @click="handleViewProfile({
                      userId: scope.row.ownerId,
                      name: scope.row.ownerName,
                      account: scope.row.ownerAccount,
                      avatar: scope.row.ownerAvatar,
                      email: scope.row.ownerEmail,
                      phone: scope.row.ownerPhone,
                      intro: scope.row.ownerIntro
                  })"
                >
                  {{ scope.row.ownerName }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="permission" label="Permission" width="120">
              <template #default="scope">
                <el-tag v-if="scope.row.permission" :type="getPermissionType(scope.row.permission)">
                  {{ formatPermission(scope.row.permission) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updatedAt" label="Last Updated" width="180">
              <template #default="scope">
                {{ scope.row.updatedAt ? scope.row.updatedAt.replace('T', ' ') : '' }}
              </template>
            </el-table-column>
            <el-table-column label="Operations" width="250">
              <template #default="scope">
                <el-button size="small" @click="openDoc(scope.row.id)">Open</el-button>
                <el-dropdown trigger="click" style="margin-left: 10px" @command="(cmd) => handleDocCommand(cmd, scope.row)">
                  <el-button size="small" circle>
                    <el-icon><More /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="move">Move</el-dropdown-item>
                      <el-dropdown-item command="delete" style="color: red">Delete</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-table-column>
          </el-table>
        </el-main>
      </el-container>
      <el-main v-else class="welcome-box">
        <h2>Welcome to CoEdit</h2>
        <p>Please login to manage your documents.</p>
      </el-main>
    </el-container>
    <ProfileDialog v-model="showProfileDialog" :userId="profileUserId" />
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
    <el-dialog v-model="showAdvancedSearch" title="Advanced Search" width="500px">
      <el-form :model="searchForm" label-width="100px">
        <el-form-item label="Title">
          <el-input v-model="searchForm.title" placeholder="Search in title"></el-input>
        </el-form-item>
        <el-form-item label="Content">
          <el-input v-model="searchForm.keyword" placeholder="Search in content"></el-input>
        </el-form-item>
        <el-form-item label="Author">
          <el-input v-model="searchForm.authorName" placeholder="Owner name"></el-input>
        </el-form-item>
        <el-form-item label="Date Range">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="To"
            start-placeholder="Start"
            end-placeholder="End"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="Sort By">
          <el-select v-model="searchForm.sortField" style="width: 140px">
            <el-option label="Updated Date" value="updated_at" />
            <el-option label="Created Date" value="created_at" />
          </el-select>
          <el-select v-model="searchForm.sortOrder" style="width: 140px; margin-left: 10px">
            <el-option label="Descending" value="desc" />
            <el-option label="Ascending" value="asc" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAdvancedSearch = false">Cancel</el-button>
          <el-button type="primary" @click="performAdvancedSearch">Search</el-button>
        </span>
      </template>
    </el-dialog>
    <el-dialog v-model="showMoveDialog" title="Move Document" width="400px">
      <span>Select Folder:</span>
      <el-select v-model="moveTargetFolderId" placeholder="Select folder" style="width: 100%; margin-top: 10px;">
        <el-option label="Uncategorized (Root)" :value="0" />
        <el-option v-for="f in folders" :key="f.id" :label="f.name" :value="f.id" />
      </el-select>
      <template #footer>
        <el-button @click="showMoveDialog = false">Cancel</el-button>
        <el-button type="primary" @click="confirmMove">Move</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useViewStore } from '../stores/view'
import { storeToRefs } from 'pinia'
import { getDocumentList, createDocument, deleteDocument, searchDocuments, moveDocument, searchDocumentsAdvanced, getDocument } from '../api/document'
import { getFolders, createFolder, deleteFolder, updateFolder } from '../api/folder'
import { markAllAsRead, getUnreadCount } from '../api/notification'
import { getUserProfile } from '../api/user'
import { ArrowDown, Search, Plus, Folder, Document, More, Close, Bell, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ProfileDialog from '../components/ProfileDialog.vue'

const router = useRouter()
const userStore = useUserStore()
const viewStore = useViewStore()

const { activeFolder, searchKeyword, isSearching, searchForm } = storeToRefs(viewStore)

const documents = ref([])
const folders = ref([])

const showUserProfile = ref(false)
const selectedUser = ref({})
const loadingProfile = ref(false)

const showProfileDialog = ref(false)
const profileUserId = ref(null)

const openProfile = () => {
  profileUserId.value = userStore.user.id
  showProfileDialog.value = true
}

const handleViewProfile = async (user) => {
    showUserProfile.value = true
    selectedUser.value = user 
    loadingProfile.value = true
    try {
        if (user.userId) {
            const profile = await getUserProfile(user.userId)
            selectedUser.value = profile
        }
    } catch (e) {
        console.error(e)
    } finally {
        loadingProfile.value = false
    }
}

const showAdvancedSearch = ref(false)

const showMoveDialog = ref(false)
const moveTargetFolderId = ref(null)
const documentToMove = ref(null)

const hasUnread = ref(false)

const handleNotificationsClick = async () => {
  try {
    if (hasUnread.value) {
      await markAllAsRead()
      hasUnread.value = false
    }
    router.push('/notifications')
  } catch (e) {
    console.error(e)
    router.push('/notifications')
  }
}

const checkUnread = async () => {
  if (userStore.user) {
    try {
      const count = await getUnreadCount()
      hasUnread.value = count > 0
    } catch (e) {
      console.error(e)
    }
  }
}

const fetchDocs = async () => {
  if (userStore.user) {
    if (isSearching.value) {
        if (searchKeyword.value) {
            handleSearch()
        } else {
            if (searchForm.value.title || searchForm.value.keyword || searchForm.value.authorName || searchForm.value.dateRange.length > 0) {
                 performAdvancedSearch()
            } else {
                documents.value = await getDocumentList()
                isSearching.value = false
            }
        }
    } else {
        documents.value = await getDocumentList()
    }
  }
}

const fetchFoldersList = async () => {
  if (userStore.user) {
    folders.value = await getFolders()
  }
}

const init = async () => {
  await userStore.fetchUser()
  if (userStore.user) {
    await Promise.all([fetchDocs(), fetchFoldersList(), checkUnread()])
    
    const interval = setInterval(() => {
      checkUnread()
      fetchDocs()
    }, 30000)
    onUnmounted(() => clearInterval(interval))
  }
}

onMounted(init)

const handleLogout = async () => {
  await userStore.logout()
  viewStore.reset()
  router.push('/login')
}

const createNewDoc = async () => {
  try {
    const newDoc = await createDocument({ title: 'Untitled Document', content: '' })

    const currentFolderId = (activeFolder.value !== 'all' && activeFolder.value !== 'root') ? Number(activeFolder.value) : null
    
    if (currentFolderId) {
      await moveDocument({
        docId: newDoc.id,
        folderId: currentFolderId
      })
    }
    
    router.push(`/document/${newDoc.id}`)
  } catch (error) {
    console.error(error)
  }
}

const handleDocCommand = (command, doc) => {
  if (command === 'move') {
    openMoveDialog(doc)
  } else if (command === 'delete') {
    deleteDoc(doc)
  }
}

const openDoc = async (id) => {
  try {
    await getDocument(id)
    router.push(`/document/${id}`)
  } catch (e) {
    fetchDocs()
  }
}

const deleteDoc = async (doc) => {
  const isOwner = doc.ownerId === userStore.user.id
  const msg = isOwner 
      ? 'Are you sure you want to permanently delete this document for everyone?' 
      : 'Are you sure you want to remove this document from your list?'

  try {
    await ElMessageBox.confirm(msg, 'Warning', {
      confirmButtonText: 'OK',
      cancelButtonText: 'Cancel',
      type: 'warning'
    })
    await deleteDocument(doc.id)
    ElMessage.success('Deleted')
    fetchDocs()
  } catch (e) {}
}

const handleFolderSelect = (index) => {
  activeFolder.value = index
}

const openCreateFolder = async () => {
  try {
    const { value } = await ElMessageBox.prompt('Please input folder name', 'New Folder', {
      confirmButtonText: 'Create',
      cancelButtonText: 'Cancel',
    })
    if (value) {
      await createFolder({ name: value })
      ElMessage.success('Folder created')
      fetchFoldersList()
    }
  } catch (e) {}
}

const handleFolderCommand = async (command, folder) => {
  if (command === 'delete') {
    try {
      await ElMessageBox.confirm('Are you sure to delete this folder? Documents in it will be moved to Uncategorized.', 'Warning', { type: 'warning' })
      await deleteFolder(folder.id)
      ElMessage.success('Deleted')
      fetchFoldersList()
      activeFolder.value = 'all' 
      
      fetchDocs() 
    } catch (e) {}
  } else if (command === 'rename') {
    try {
      const { value } = await ElMessageBox.prompt('New folder name', 'Rename', {
        inputValue: folder.name
      })
      if (value) {
        await updateFolder(folder.id, { name: value })
        ElMessage.success('Renamed')
        fetchFoldersList()
      }
    } catch (e) {}
  }
}

const currentFolderName = computed(() => {
  if (activeFolder.value === 'all') return 'All Documents'
  if (activeFolder.value === 'root') return 'Uncategorized'
  const f = folders.value.find(f => String(f.id) === activeFolder.value)
  return f ? f.name : 'Unknown'
})

const filteredDocuments = computed(() => {
  if (activeFolder.value === 'all') return documents.value
  if (activeFolder.value === 'root') {
    return documents.value.filter(d => !d.folderId)
  }
  return documents.value.filter(d => String(d.folderId) === activeFolder.value)
})

const handleSearch = async () => {
  if (!searchKeyword.value) {
    fetchDocs()
    isSearching.value = false
    return
  }

  const params = {
    keyword: searchKeyword.value,
    folderId: null 
  }
  
  try {
     documents.value = await searchDocumentsAdvanced(params)

     isSearching.value = true
  } catch (e) {
     console.error(e)
  }
}

const openAdvancedSearch = () => {
  showAdvancedSearch.value = true
}

const performAdvancedSearch = async () => {
  const params = {
    title: searchForm.value.title,
    content: searchForm.value.keyword, 
    authorName: searchForm.value.authorName,
    sortField: searchForm.value.sortField,
    sortOrder: searchForm.value.sortOrder,

    folderId: null 
  }
  
  if (searchForm.value.dateRange && searchForm.value.dateRange.length === 2) {
    params.startDate = searchForm.value.dateRange[0]
    params.endDate = searchForm.value.dateRange[1]
  }

  try {
    documents.value = await searchDocumentsAdvanced(params)
    showAdvancedSearch.value = false
    
    isSearching.value = true
    ElMessage.success('Search completed')
  } catch (e) {
    console.error(e)
  }
}

const clearSearch = () => {
  viewStore.reset()
  fetchDocs()
}

const openMoveDialog = (doc) => {
  documentToMove.value = doc
  moveTargetFolderId.value = doc.folderId || 0
  showMoveDialog.value = true
}

const confirmMove = async () => {
  if (!documentToMove.value) return
  try {
    await moveDocument({
      docId: documentToMove.value.id,
      folderId: moveTargetFolderId.value === 0 ? null : moveTargetFolderId.value
    })
    ElMessage.success('Moved successfully')
    showMoveDialog.value = false
    fetchDocs()
  } catch (e) {
    console.error(e)
  }
}

const getPermissionType = (permission) => {
  switch (permission) {
    case 'OWNER': return 'danger'
    case 'ADMIN': return 'warning'
    case 'EDITOR': return 'primary'
    case 'VIEWER': return 'info'
    default: return 'info'
  }
}

const formatPermission = (permission) => {
  if (!permission) return ''
  return permission.charAt(0) + permission.slice(1).toLowerCase()
}
</script>
<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #dcdfe6;
  padding: 0 20px;
  background-color: #fff;
  z-index: 10;
}
.logo {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}
.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}
.aside {
  border-right: 1px solid #dcdfe6;
  background-color: #f5f7fa;
}
.folder-header {
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  border-bottom: 1px solid #e6e6e6;
}
.folder-menu {
  border-right: none;
  background-color: transparent;
}
.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.welcome-box {
  text-align: center;
  margin-top: 50px;
}

.folder-menu-item {
  height: auto !important;
  line-height: 1.5 !important;
  padding-top: 10px;
  padding-bottom: 10px;
}
.folder-name {
  white-space: normal;
  word-break: break-word;
  line-height: 1.4;
  flex: 1;
  margin-right: 5px;
}

.notification-area {
  padding: 15px 20px;
  border-top: 1px solid #dcdfe6;
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #606266;
  transition: background-color 0.3s;
}

.notification-area:hover {
  background-color: #ecf5ff;
  color: #409EFF;
}

.notification-dot {
  width: 8px;
  height: 8px;
  background-color: #409EFF;
  border-radius: 50%;
  margin-left: auto;
  margin-right: 5px;
}

:deep(.el-table .cell) {
  white-space: nowrap;
}
</style>
