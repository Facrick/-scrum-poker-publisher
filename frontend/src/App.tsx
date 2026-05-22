import { Navigate, Route, Routes } from 'react-router-dom';
import { HomePage } from './pages/HomePage';
import { JoinPage } from './pages/JoinPage';
import { NotFoundPage } from './pages/NotFoundPage';
import { RoomPage } from './pages/RoomPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { useAuthStore } from './store/authStore';
import { ReactNode } from 'react';

// Компонент для защиты роутов
const ProtectedRoute = ({ children }: { children: ReactNode }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated());
  if (!isAuthenticated) {
    // Если пользователь не аутентифицирован, перенаправляем на страницу входа
    return <Navigate to="/login" replace />;
  }
  return children;
};

export default function App() {
  return (
    <Routes>
      {/* Публичные роуты */}
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/room/:roomId" element={<RoomPage />} />
      <Route path="/room/:roomId/join" element={<JoinPage />} />

      {/* Защищенные роуты (пример) */}
      {/* <Route
        path="/create-room"
        element={
          <ProtectedRoute>
            <CreateRoomPage />
          </ProtectedRoute>
        }
      /> */}

      {/* Системные роуты */}
      <Route path="/404" element={<NotFoundPage />} />
      <Route path="*" element={<Navigate to="/404" replace />} />
    </Routes>
  );
}
