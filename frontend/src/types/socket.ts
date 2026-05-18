import type { Room } from './room'

export type SocketEventType =
  | 'ROOM_UPDATED'
  | 'VOTE_CAST'
  | 'VOTES_REVEALED'
  | 'VOTES_RESET'
  | 'PARTICIPANT_JOINED'
  | 'PARTICIPANT_LEFT'
  | 'ERROR'
  | 'PONG'

export interface SocketEvent<T = Room> {
  type: SocketEventType
  payload: T
}
