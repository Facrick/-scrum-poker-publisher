import axios from 'axios';
import { useAuthStore } from '../store/authStore';

// VITE_API_URL должен содержать полный путь к вашему API, например, http://localhost:8080
const apiUrl = import.meta.env.VITE_API_URL || '';

export const http = axios.create({
  baseURL: apiUrl,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Создаем перехватчик запросов
http.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);
