import axios from 'axios';
import { useAuthStore } from '../store/authStore';

const apiUrl = import.meta.env.VITE_API_URL;

if (!apiUrl) {
  console.error("VITE_API_URL is not defined! Please check your .env file or Vercel environment variables.");
  // Можно выбросить ошибку или использовать fallback URL
  // throw new Error("VITE_API_URL is not defined!");
}

export const http = axios.create({
  baseURL: `${apiUrl}/api`, // Добавляем /api здесь, чтобы в VITE_API_URL был только базовый URL бэкенда
  headers: {
    'Content-Type': 'application/json',
  },
});

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
