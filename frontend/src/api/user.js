import request from '../utils/request'

export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

export function getCurrentUser() {
  return request({
    url: '/user/current',
    method: 'get'
  })
}

export function getUserProfile(userId) {
  return request({
    url: '/user/profile',
    method: 'get',
    params: { userId }
  })
}

export function updateUserProfile(data) {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function verifySecurity(data) {
  return request({
    url: '/user/verify-security',
    method: 'post',
    data
  })
}

export function updateSecurity(data) {
  return request({
    url: '/user/update-security',
    method: 'post',
    data
  })
}

export function getSecurityQuestion(account) {
  return request({
    url: '/user/security-question',
    method: 'get',
    params: { account }
  })
}

export function resetPassword(data) {
  return request({
    url: '/user/reset-password',
    method: 'post',
    data
  })
}

