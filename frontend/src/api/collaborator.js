import request from '../utils/request'

export function getCollaborators(documentId) {
  return request({
    url: '/collaborator/list',
    method: 'get',
    params: { documentId }
  })
}

export function addCollaborator(data) {
  return request({
    url: '/collaborator/add',
    method: 'post',
    data
  })
}

export function removeCollaborator(data) {
  return request({
    url: '/collaborator/remove',
    method: 'post',
    data
  })
}

export function updateCollaboratorPermission(data) {
  return request({
    url: '/collaborator/update-permission',
    method: 'post',
    data
  })
}

export function acceptInvitation(data) {
  return request({
    url: '/collaborator/accept',
    method: 'post',
    data
  })
}
