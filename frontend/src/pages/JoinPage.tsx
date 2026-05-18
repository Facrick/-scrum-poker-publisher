import { FormEvent, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { roomApi } from '../api/roomApi'
import { useRoomStore } from '../store/roomStore'
import type { ParticipantRole } from '../types/room'

export function JoinPage() {
  const { roomId } = useParams()
  const navigate = useNavigate()
  const setCurrentParticipant = useRoomStore((state) => state.setCurrentParticipant)

  const [name, setName] = useState('')
  const [role, setRole] = useState<ParticipantRole>('PARTICIPANT')
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()

    if (!roomId) {
      setError('Комната не найдена')
      return
    }

    try {
      const response = await roomApi.joinRoom(roomId, { name, role })

      setCurrentParticipant(response.participantId, response.participantName, role)
      navigate(`/room/${roomId}`)
    } catch {
      setError('Не удалось подключиться. Возможно, лимит голосующих уже достигнут.')
    }
  }

  return (
    <main className="page page-center">
      <section className="panel home-panel">
        <h1>Войти в комнату</h1>

        <form onSubmit={handleSubmit} className="form">
          <label>
            Ваше имя
            <input
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="Ваше имя"
              required
            />
          </label>

          <label>
            Роль
            <select
              value={role}
              onChange={(event) => setRole(event.target.value as ParticipantRole)}
            >
              <option value="PARTICIPANT">Участник</option>
              <option value="OBSERVER">Наблюдатель</option>
            </select>
          </label>

          {error && <div className="error">{error}</div>}

          <button type="submit">Войти</button>
        </form>
      </section>
    </main>
  )
}
