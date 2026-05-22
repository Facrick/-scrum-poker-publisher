import { useAuthStore } from '../store/authStore';
import { jwtDecode } from 'jwt-decode';

interface DecodedToken {
  sub: string; // 'sub' (subject) - это стандартное поле для имени пользователя в JWT
  iat: number;
  exp: number;
}

export const useAuthUser = () => {
  const token = useAuthStore((state) => state.token);

  if (!token) {
    return null;
  }

  try {
    const decoded = jwtDecode<DecodedToken>(token);
    return { username: decoded.sub };
  } catch (error) {
    console.error("Failed to decode JWT:", error);
    return null;
  }
};
