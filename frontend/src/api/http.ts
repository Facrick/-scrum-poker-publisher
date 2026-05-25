import axios from 'axios';
import { useAuthStore } from '../store/authStore';

const apiUrl = import.meta.env.VITE_API_URL;

// Проверяем наличие переменной окружения
if (!apiUrl) {
  console.error("VITE_API_URL is not defined! Please check your .env file for local development or Vercel environment variables for deployment.");
  // В продакшене лучше выбросить ошибку, чтобы приложение не работало некорректно
  // В локальной разработке можно использовать fallback, но для продакшена это критично
  throw new Error("VITE_API_URL is not configured.");
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
