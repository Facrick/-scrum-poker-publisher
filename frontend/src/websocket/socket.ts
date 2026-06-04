import { Client } from '@stomp/stompjs'
import type { CastVoteRequest, Room } from '../types/room'
import type { SocketEvent } from '../types/socket'
import { useAuthStore } from '../store/authStore'

let client: Client | null = null

interface ConnectOptions {
  roomId: string
  onRoomUpdate: (room: Room) => void
  onError?: (message: string) => void
}

const brokerURL = import.meta.env.VITE_WS_URL

export function connectRoomSocket(options: ConnectOptions) {
  disconnectRoomSocket()

  if (!brokerURL) {
    options.onError?.('WebSocket URL is not configured.')
    return
  }

  const token = useAuthStore.getState().token

  client = new Client({
    brokerURL,
    connectHeaders: {
      ...(token && { Authorization: `Bearer ${token}` })
    },
    reconnectDelay: 5000,
    onConnect: () => {
      client?.subscribe(`/topic/rooms/${options.roomId}`, (message) => {
        const event = JSON.parse(message.body) as SocketEvent<Room>
        options.onRoomUpdate(event.payload)
      })
    },
    onStompError: (frame) => {
      console.error('STOMP error:', frame.headers['message'])
    },
    onWebSocketError: (event) => {
      console.error('WebSocket error:', event)
    }
  })

  client.activate()
}

export function disconnectRoomSocket() {
  if (client) {
    client.deactivate()
    client = null
  }
}

export function sendVote(roomId: string, payload: CastVoteRequest) {
  client?.publish({
    destination: `/app/rooms/${roomId}/vote`,
    body: JSON.stringify(payload)
  })
}

export function sendReveal(roomId: string) {
  client?.publish({
    destination: `/app/rooms/${roomId}/reveal`,
    body: JSON.stringify({})
  })
}

export function sendReset(roomId: string) {
  client?.publish({
    destination: `/app/rooms/${roomId}/reset`,
    body: JSON.stringify({})
  })
}
