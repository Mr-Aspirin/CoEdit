import request from '../utils/request'

export function getComments(docId) {
  return request({
    url: `/documents/${docId}/comments`,
    method: 'get'
  })
}

export function addComment(docId, data) {
  return request({
    url: `/documents/${docId}/comments`,
    method: 'post',
    data
  })
}

export function deleteComment(commentId) {
  return request({
    url: `/comments/${commentId}`,
    method: 'delete'
  })
}
