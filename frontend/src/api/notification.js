import request from '../utils/request'

export function getNotifications() {
  return request({
    url: '/notification/list',
    method: 'get'
  })
}

export function deleteNotification(id) {
  return request({
    url: `/notification/${id}`,
    method: 'delete'
  })
}

export function markAllAsRead() {
  return request({
    url: '/notification/mark-all-read',
    method: 'post'
  })
}

export function getUnreadCount() {
  return request({
    url: '/notification/unread-count',
    method: 'get'
  })
}
