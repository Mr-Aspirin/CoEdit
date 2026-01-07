import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import Document from '../views/Document.vue'
import Notifications from '../views/Notifications.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/document/:id',
    name: 'Document',
    component: Document
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: Notifications
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
