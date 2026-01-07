import request from '../utils/request'

export function getFolders() {
  return request({
    url: '/folder/list',
    method: 'get'
  })
}

export function createFolder(data) {
  return request({
    url: '/folder/create',
    method: 'post',
    data
  })
}

export function updateFolder(id, data) {
  return request({
    url: `/folder/${id}`,
    method: 'put',
    data
  })
}

export function deleteFolder(id) {
  return request({
    url: `/folder/${id}`,
    method: 'delete'
  })
}
