import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { roomApi } from '../api/roomApi';
import { useRoomStore } from '../store/roomStore';
import { useAuthStore } from '../store/authStore';
import { useAuthUser } from '../hooks/useAuthUser';

export function HomePage() {
  const navigate = useNavigate();
  const setCurrentParticipant = useRoomStore((state) => state.setCurrentParticipant);
  const { isAuthenticated, clearToken } = useAuthStore();
  const user = useAuthUser();

  const [roomName, setRoomName] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleCreateRoom(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      const response = await roomApi.createRoom({ roomName });

      if (user) {
        setCurrentParticipant(response.participantId, user.username, 'MODERATOR');
      }

      navigate(`/room/${response.roomId}`);
    } catch {
      setError('Не удалось создать комнату. Возможно, вы не авторизованы.');
    } finally {
      setSubmitting(false);
    }
  }

  const renderAuthenticatedView = () => (
    <form onSubmit={handleCreateRoom} className="form">
      <label>
        Название комнаты
        <input
          value={roomName}
          onChange={(event) => setRoomName(event.target.value)}
          placeholder="Название комнаты"
          required
        />
      </label>

      {error && <div className="error">{error}</div>}

      <button disabled={submitting} type="submit">
        {submitting ? 'Создаем...' : 'Создать комнату'}
      </button>
      <button onClick={clearToken} type="button" className="button-secondary">
        Выйти
      </button>
    </form>
  );

  const renderGuestView = () => (
    <div className="guest-actions">
      <Link to="/login" className="button">
        Войти
      </Link>
      <Link to="/register" className="button button-secondary">
        Зарегистрироваться
      </Link>
    </div>
  );

  return (
    <main className="page page-center">
      <section className="panel home-panel">
        <div className="hero-icon">♠</div>
        <h1>Scrum Poker</h1>
        <p className="muted">
          Создай комнату для оценки задач.
        </p>

        {isAuthenticated() ? renderAuthenticatedView() : renderGuestView()}

      </section>
    </main>
  );
}
