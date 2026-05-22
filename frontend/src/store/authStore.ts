import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

interface AuthState {
  token: string | null;
  setToken: (token: string) => void;
  clearToken: () => void;
  isAuthenticated: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  // persist позволяет сохранять состояние в localStorage
  persist(
    (set, get) => ({
      token: null,
      setToken: (token: string) => set({ token }),
      clearToken: () => set({ token: null }),
      isAuthenticated: () => !!get().token,
    }),
    {
      name: 'auth-storage', // имя ключа в localStorage
      storage: createJSONStorage(() => localStorage), // указываем, что хранилище - localStorage
    }
  )
);
