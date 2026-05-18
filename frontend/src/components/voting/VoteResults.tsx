import type { Room } from '../../types/room'
import { VoteStatistics } from './VoteStatistics'

interface Props {
  room: Room
}

export function VoteResults({ room }: Props) {
  const votes = room.participants.filter((participant) => participant.vote)
  const revealed = room.status === 'REVEALED'

  return (
    <div className="results">
      <h3>Результаты</h3>

      <VoteStatistics stats={room.stats} revealed={revealed} />

      {!revealed ? (
        <p className="muted">
          Голоса скрыты до открытия.
        </p>
      ) : votes.length === 0 ? (
        <p className="muted">Пока никто не проголосовал.</p>
      ) : (
        <div className="result-grid">
          {votes.map((participant) => (
            <div key={participant.id} className="result-item">
              <span>{participant.name}</span>
              <strong>{participant.vote}</strong>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
