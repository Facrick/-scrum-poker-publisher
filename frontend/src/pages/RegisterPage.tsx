import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { authApi } from '../api/authApi';

export const RegisterPage: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const setToken = useAuthStore((state) => state.setToken);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      const response = await authApi.register({ username, password });
      setToken(response.token);
      navigate('/'); // Перенаправляем на главную после успешной регистрации
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="page page-center">
      <section className="panel home-panel">
        <div className="hero-icon">♠</div>
        <h1>Регистрация</h1>
        <form onSubmit={handleSubmit} className="form">
          <label>
            Имя пользователя
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Введите имя"
              required
            />
          </label>
          <label>
            Пароль
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Введите пароль"
              required
            />
          </label>
          {error && <div className="error">{error}</div>}
          <button type="submit" disabled={submitting}>
            {submitting ? 'Регистрация...' : 'Зарегистрироваться'}
          </button>
          <div className="form-footer">
            <span>Уже есть аккаунт? </span>
            <Link to="/login">Войти</Link>
          </div>
        </form>
      </section>
    </main>
  );
};
