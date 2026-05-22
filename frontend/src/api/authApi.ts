import { http } from './http';
import { AuthRequest, AuthResponse } from '../types/auth';

export const authApi = {
  register: (data: AuthRequest): Promise<AuthResponse> => {
    // Явно указываем полный путь с /api
    return http.post('/api/auth/register', data).then(res => res.data);
  },

  login: (data: AuthRequest): Promise<AuthResponse> => {
    // Явно указываем полный путь с /api
    return http.post('/api/auth/login', data).then(res => res.data);
  },
};
