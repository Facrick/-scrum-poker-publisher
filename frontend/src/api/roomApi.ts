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
    // Теперь baseURL в http.ts уже содержит /api, поэтому здесь только /rooms
    const response = await http.post<CreateRoomResponse>('/rooms', payload)
    return response.data
  },

  async joinRoom(roomId: string, payload: JoinRoomRequest): Promise<JoinRoomResponse> {
    const response = await http.post<JoinRoomResponse>(`/rooms/${roomId}/join`, payload)
    return response.data
  },

  async getRoom(roomId: string): Promise<Room> {
    const response = await http.get<Room>(`/rooms/${roomId}`)
    return response.data
  },

  async castVote(roomId: string, payload: CastVoteRequest): Promise<Room> {
    const response = await http.post<Room>(`/rooms/${roomId}/votes`, payload)
    return response.data
  },

  async reveal(roomId: string): Promise<Room> {
    const response = await http.post<Room>(`/rooms/${roomId}/reveal`)
    return response.data
  },

  async reset(roomId: string): Promise<Room> {
    const response = await http.post<Room>(`/rooms/${roomId}/reset`)
    return response.data
  },

  async updateSettings(roomId: string, payload: UpdateRoomSettingsRequest): Promise<Room> {
    const response = await http.put<Room>(`/rooms/${roomId}/settings`, payload)
    return response.data
  }
}
