import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useViewStore = defineStore('view', () => {
  const activeFolder = ref('all')
  const searchKeyword = ref('')
  const isSearching = ref(false)
  const searchForm = ref({
    title: '',
    keyword: '',
    authorName: '',
    dateRange: [],
    sortField: 'updated_at',
    sortOrder: 'desc'
  })

  function reset() {
    activeFolder.value = 'all'
    searchKeyword.value = ''
    isSearching.value = false
    searchForm.value = {
      title: '',
      keyword: '',
      authorName: '',
      dateRange: [],
      sortField: 'updated_at',
      sortOrder: 'desc'
    }
  }

  return {
    activeFolder,
    searchKeyword,
    isSearching,
    searchForm,
    reset
  }
})
