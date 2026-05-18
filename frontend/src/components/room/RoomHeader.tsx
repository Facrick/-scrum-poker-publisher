import type { Room } from '../../types/room'
import { translateRoomStatus } from '../../utils/i18n'

interface Props {
  room: Room
  inviteLink: string
}

export function RoomHeader({ room, inviteLink }: Props) {
  async function copyInviteLink() {
    await navigator.clipboard.writeText(inviteLink)
    alert('Ссылка скопирована')
  }

  return (
    <header className="room-header">
      <div>
        <h1>{room.name}</h1>
        <p className="muted">
          Статус: {translateRoomStatus(room.status)} · Лимит голосующих: {room.maxVoters}
        </p>
      </div>

      <button className="secondary" onClick={copyInviteLink}>
        Скопировать ссылку
      </button>
    </header>
  )
}
