import { http } from './http'
import type {
  CastVoteRequest,
  CreateRoomRequest,
  CreateRoomResponse,
  JoinRoomRequest,
  JoinRoomResponse,
  Room,
  UpdateRoomSettingsRequest
} from '../types/room'

export const roomApi = {
  async createRoom(payload: CreateRoomRequest): Promise<CreateRoomResponse> {
    const response = await http.post<CreateRoomResponse>('/api/rooms', payload)
    return response.data
  },

  async joinRoom(roomId: string, payload: JoinRoomRequest): Promise<JoinRoomResponse> {
    const response = await http.post<JoinRoomResponse>(`/api/rooms/${roomId}/join`, payload)
    return response.data
  },

  async getRoom(roomId: string): Promise<Room> {
    const response = await http.get<Room>(`/api/rooms/${roomId}`)
    return response.data
  },

  async castVote(roomId: string, payload: CastVoteRequest): Promise<Room> {
    const response = await http.post<Room>(`/api/rooms/${roomId}/votes`, payload)
    return response.data
  },

  async reveal(roomId: string, moderatorId: string): Promise<Room> {
    const response = await http.post<Room>(`/api/rooms/${roomId}/reveal`, null, {
      params: { moderatorId }
    })
    return response.data
  },

  async reset(roomId: string, moderatorId: string): Promise<Room> {
    const response = await http.post<Room>(`/api/rooms/${roomId}/reset`, null, {
      params: { moderatorId }
    })
    return response.data
  },

  async updateSettings(roomId: string, payload: UpdateRoomSettingsRequest): Promise<Room> {
    const response = await http.put<Room>(`/api/rooms/${roomId}/settings`, payload)
    return response.data
  }
}
