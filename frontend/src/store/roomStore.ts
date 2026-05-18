import { create } from 'zustand'
import type { ParticipantRole, Room } from '../types/room'

interface RoomStore {
  room: Room | null
  participantId: string | null
  participantName: string | null
  role: ParticipantRole | null
  loading: boolean
  error: string | null

  setRoom: (room: Room | null) => void
  setCurrentParticipant: (participantId: string, participantName: string, role: ParticipantRole) => void
  setLoading: (loading: boolean) => void
  setError: (error: string | null) => void
  loadSession: (roomId: string) => void
}

const participantKey = (roomId: string) => `scrum-poker:${roomId}:participant`

export const useRoomStore = create<RoomStore>((set) => ({
  room: null,
  participantId: null,
  participantName: null,
  role: null,
  loading: false,
  error: null,

  setRoom: (room) => set({ room }),

  setCurrentParticipant: (participantId, participantName, role) => {
    set({ participantId, participantName, role })

    const roomId = window.location.pathname.split('/')[2]
    if (roomId) {
      localStorage.setItem(
        participantKey(roomId),
        JSON.stringify({ participantId, participantName, role })
      )
    }
  },

  setLoading: (loading) => set({ loading }),

  setError: (error) => set({ error }),

  loadSession: (roomId) => {
    const raw = localStorage.getItem(participantKey(roomId))

    if (!raw) {
      return
    }

    try {
      const session = JSON.parse(raw)
      set({
        participantId: session.participantId,
        participantName: session.participantName,
        role: session.role
      })
    } catch {
      localStorage.removeItem(participantKey(roomId))
    }
  }
}))
