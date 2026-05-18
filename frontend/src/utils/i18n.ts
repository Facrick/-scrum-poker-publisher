import type { DeckType, ParticipantRole, RoomStatus } from '../types/room'

export function translateRole(role: ParticipantRole): string {
  switch (role) {
    case 'MODERATOR':
      return 'Модератор'
    case 'PARTICIPANT':
      return 'Участник'
    case 'OBSERVER':
      return 'Наблюдатель'
    default:
      return role
  }
}

export function translateRoomStatus(status: RoomStatus): string {
  switch (status) {
    case 'VOTING':
      return 'Голосование'
    case 'REVEALED':
      return 'Голоса открыты'
    default:
      return status
  }
}

export function translateDeckType(deckType: DeckType): string {
  switch (deckType) {
    case 'FIBONACCI':
      return 'Fibonacci'
    case 'T_SHIRT':
      return 'Футболки'
    case 'CUSTOM':
      return 'Своя колода'
    default:
      return deckType
  }
}
