import type { Participant } from '../../types/room'
import { translateRole } from '../../utils/i18n'

interface Props {
  participants: Participant[]
}

export function ParticipantsList({ participants }: Props) {
  return (
    <aside className="panel">
      <h2>Участники</h2>

      <div className="participant-counter">
        Всего: {participants.length}
      </div>

      <div className="participant-list">
        {participants.map((participant) => (
          <div key={participant.id} className="participant">
            <div>
              <strong>{participant.name}</strong>
              <span>{translateRole(participant.role)}</span>
            </div>

            <div className="participant-status">
              {participant.voted ? '✅' : '⌛'}
            </div>
          </div>
        ))}
      </div>
    </aside>
  )
}
