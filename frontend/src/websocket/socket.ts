import { Client } from '@stomp/stompjs'
import type { CastVoteRequest, Room } from '../types/room'
import type { SocketEvent } from '../types/socket'

let client: Client | null = null

interface ConnectOptions {
  roomId: string
  onRoomUpdate: (room: Room) => void
  onError?: (message: string) => void
}

function resolveWebSocketUrl(): string {
  const envUrl = import.meta.env.VITE_WS_URL

  if (envUrl) {
    return envUrl
  }

  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  return `${protocol}://${window.location.host}/ws-native`
}

export function connectRoomSocket(options: ConnectOptions) {
  disconnectRoomSocket()

  client = new Client({
    brokerURL: resolveWebSocketUrl(),
    reconnectDelay: 3000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    debug: () => {},
    onConnect: () => {
      client?.subscribe(`/topic/rooms/${options.roomId}`, (message) => {
        const event = JSON.parse(message.body) as SocketEvent<Room>
        options.onRoomUpdate(event.payload)
      })
    },
    onStompError: (frame) => {
      options.onError?.(frame.headers.message ?? 'WebSocket STOMP error')
    },
    onWebSocketError: () => {
      options.onError?.('WebSocket connection error')
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

export function sendReveal(roomId: string, moderatorId: string) {
  client?.publish({
    destination: `/app/rooms/${roomId}/reveal`,
    body: JSON.stringify({ moderatorId })
  })
}

export function sendReset(roomId: string, moderatorId: string) {
  client?.publish({
    destination: `/app/rooms/${roomId}/reset`,
    body: JSON.stringify({ moderatorId })
  })
}
