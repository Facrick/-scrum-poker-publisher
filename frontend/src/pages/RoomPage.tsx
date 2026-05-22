import { useEffect, useMemo } from 'react'
import { Link, useParams } from 'react-router-dom'
import { roomApi } from '../api/roomApi'
import { ParticipantsList } from '../components/participants/ParticipantsList'
import { RoomHeader } from '../components/room/RoomHeader'
import { RoomSettingsPanel } from '../components/room/RoomSettingsPanel'
import { Timer } from '../components/room/Timer'
import { VoteResults } from '../components/voting/VoteResults'
import { VotingCards } from '../components/voting/VotingCards'
import { useRoomStore } from '../store/roomStore'
import {
  connectRoomSocket,
  disconnectRoomSocket,
  sendReset,
  sendReveal
} from '../websocket/socket'

export function RoomPage() {
  const { roomId } = useParams()

  const {
    room,
    participantId,
    role,
    loading,
    error,
    setRoom,
    setLoading,
    setError,
    loadSession
  } = useRoomStore()

  const inviteLink = useMemo(() => {
    if (!roomId) return ''
    return `${window.location.origin}/room/${roomId}/join`
  }, [roomId])

  useEffect(() => {
    if (!roomId) return

    loadSession(roomId)
    setLoading(true)

    roomApi.getRoom(roomId)
      .then(setRoom)
      .catch(() => setError('Комната не найдена'))
      .finally(() => setLoading(false))

    connectRoomSocket({
      roomId,
      onRoomUpdate: setRoom,
      onError: setError
    })

    return () => disconnectRoomSocket()
  }, [roomId, loadSession, setError, setLoading, setRoom])

  if (!roomId) {
    return <main className="page">Некорректный URL комнаты</main>
  }

  if (loading) {
    return <main className="page">Загрузка...</main>
  }

  if (error) {
    return <main className="page error">{error}</main>
  }

  if (!room) {
    return <main className="page">Комната не загружена</main>
  }

  const isModerator = role === 'MODERATOR'
  const canVote = participantId && role !== 'OBSERVER'

  return (
    <main className="page">
      <RoomHeader room={room} inviteLink={inviteLink} />

      <Timer
        enabled={room.settings.timerEnabled}
        durationSeconds={room.settings.timerDurationSeconds}
        roundId={room.currentRoundId}
      />

      {!participantId && (
        <section className="panel warning-panel">
          <p>Вы еще не присоединились к комнате.</p>
          <Link className="link-button" to={`/room/${roomId}/join`}>
            Войти в комнату
          </Link>
        </section>
      )}

      <div className="room-grid">
        <section className="panel">
          <h2>Голосование</h2>

          {canVote ? (
            <VotingCards
              roomId={roomId}
              participantId={participantId}
              deck={room.deck}
              disabled={room.status === 'REVEALED'}
              roundId={room.currentRoundId}
            />
          ) : (
            <p className="muted">Наблюдатели не голосуют.</p>
          )}

          <div className="actions">
            {isModerator && participantId && (
              <>
                <button
                  onClick={() => sendReveal(roomId)} // Убрали participantId
                  disabled={room.status === 'REVEALED'}
                >
                  Открыть голоса
                </button>

                <button
                  className="secondary"
                  onClick={() => sendReset(roomId)} // Убрали participantId
                >
                  Новый раунд
                </button>
              </>
            )}
          </div>

          <VoteResults room={room} />
        </section>

        <div className="side-column">
          <ParticipantsList participants={room.participants} />

          {isModerator && ( // participantId больше не нужен для проверки
            <RoomSettingsPanel
              room={room}
              onRoomUpdate={setRoom}
            />
          )}
        </div>
      </div>
    </main>
  )
}
