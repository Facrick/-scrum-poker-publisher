export type ParticipantRole = 'MODERATOR' | 'PARTICIPANT' | 'OBSERVER'

export type RoomStatus = 'VOTING' | 'REVEALED'

export type DeckType = 'FIBONACCI' | 'T_SHIRT' | 'CUSTOM'

export interface RoomSettings {
  deckType: DeckType
  customDeck: string[]
  autoReveal: boolean
  observersAllowed: boolean
  timerEnabled: boolean
  timerDurationSeconds: number
}

export interface VoteStats {
  votedCount: number
  totalVoters: number
  average: number | null
  min: string | null
  max: string | null
  consensus: boolean
}

export interface Participant {
  id: string
  name: string
  role: ParticipantRole
  connected: boolean
  voted: boolean
  vote: string | null
}

export interface Room {
  id: string
  name: string
  currentRoundId: string
  status: RoomStatus
  maxVoters: number
  settings: RoomSettings
  deck: string[]
  stats: VoteStats
  participants: Participant[]
}

export interface CreateRoomRequest {
  roomName: string
  moderatorName: string
}

export interface CreateRoomResponse {
  roomId: string
  participantId: string
  roomName: string
}

export interface JoinRoomRequest {
  name: string
  role: ParticipantRole
}

export interface JoinRoomResponse {
  roomId: string
  participantId: string
  participantName: string
}

export interface CastVoteRequest {
  participantId: string
  value: string
}

export interface UpdateRoomSettingsRequest {
  moderatorId: string
  deckType: DeckType
  customDeck: string[]
  autoReveal: boolean
  observersAllowed: boolean
  timerEnabled: boolean
  timerDurationSeconds: number
}
