import { FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { roomApi } from '../api/roomApi'
import { useRoomStore } from '../store/roomStore'

export function HomePage() {
  const navigate = useNavigate()
  const setCurrentParticipant = useRoomStore((state) => state.setCurrentParticipant)

  const [roomName, setRoomName] = useState('')
  const [moderatorName, setModeratorName] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)

    try {
      const response = await roomApi.createRoom({
        roomName,
        moderatorName
      })

      setCurrentParticipant(response.participantId, moderatorName, 'MODERATOR')
      navigate(`/room/${response.roomId}`)
    } catch {
      setError('Не удалось создать комнату')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="page page-center">
      <section className="panel home-panel">
        <div className="hero-icon">♠</div>

        <h1>Scrum Poker</h1>
        <p className="muted">
          Создай комнату для оценки задач. До 100 голосующих участников.
        </p>

        <form onSubmit={handleSubmit} className="form">
          <label>
            Название комнаты
            <input
              value={roomName}
              onChange={(event) => setRoomName(event.target.value)}
              placeholder="Название комнаты"
              required
            />
          </label>

          <label>
            Ваше имя
            <input
              value={moderatorName}
              onChange={(event) => setModeratorName(event.target.value)}
              placeholder="Имя пользователя"
              required
            />
          </label>

          {error && <div className="error">{error}</div>}

          <button disabled={submitting} type="submit">
            {submitting ? 'Создаем...' : 'Создать комнату'}
          </button>
        </form>
      </section>
    </main>
  )
}
