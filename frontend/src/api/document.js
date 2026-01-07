import request from '../utils/request'

export function createDocument(data) {
  return request({
    url: '/document/create',
    method: 'post',
    data
  })
}

export function getDocument(id) {
  return request({
    url: `/document/${id}`,
    method: 'get'
  })
}

export function updateDocument(id, data) {
  return request({
    url: `/document/${id}`,
    method: 'put',
    data
  })
}

export function deleteDocument(id) {
  return request({
    url: `/document/${id}`,
    method: 'delete'
  })
}

export function getDocumentList() {
  return request({
    url: '/document/list',
    method: 'get'
  })
}

export function searchDocuments(keyword) {
  return request({
    url: '/document/search',
    method: 'get',
    params: { keyword }
  })
}

export function moveDocument(data) {
  return request({
    url: '/document/move',
    method: 'post',
    data
  })
}

export function searchDocumentsAdvanced(data) {
  return request({
    url: '/document/search/advanced',
    method: 'post',
    data
  })
}
