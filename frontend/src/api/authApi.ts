import { http } from './http';
import { AuthRequest, AuthResponse } from '../types/auth';

export const authApi = {
  register: (data: AuthRequest): Promise<AuthResponse> => {
    // Теперь baseURL в http.ts уже содержит /api, поэтому здесь только /auth/register
    return http.post('/auth/register', data).then(res => res.data);
  },

  login: (data: AuthRequest): Promise<AuthResponse> => {
    // Теперь baseURL в http.ts уже содержит /api, поэтому здесь только /auth/login
    return http.post('/auth/login', data).then(res => res.data);
  },
};
