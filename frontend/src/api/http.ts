import axios from 'axios'

const apiUrl = import.meta.env.VITE_API_URL || ''

export const http = axios.create({
  baseURL: apiUrl,
  headers: {
    'Content-Type': 'application/json'
  }
})
