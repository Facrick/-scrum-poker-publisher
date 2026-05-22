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

function resolveWebSocketUrl(): string {
  const envUrl = import.meta.env.VITE_WS_URL;

  if (envUrl) {
    return envUrl;
  }

  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
  const host = window.location.hostname;

  return `${protocol}://${host}:8080/ws-native`;
}

export function connectRoomSocket(options: ConnectOptions) {
  disconnectRoomSocket()

  console.log('Attempting to connect to WebSocket at:', resolveWebSocketUrl());

  const token = useAuthStore.getState().token;

  client = new Client({
    brokerURL: resolveWebSocketUrl(),
    connectHeaders: {
      ...(token && { Authorization: `Bearer ${token}` })
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    debug: (msg) => console.log('STOMP DEBUG:', msg),
    onConnect: () => {
      console.log('STOMP: Connected successfully!');
      client?.subscribe(`/topic/rooms/${options.roomId}`, (message) => {
        const event = JSON.parse(message.body) as SocketEvent<Room>
        options.onRoomUpdate(event.payload)
      })
    },
    onStompError: (frame) => {
      console.error('STOMP: Broker reported error:', frame.headers['message']);
      console.error('STOMP: Additional details:', frame.body);
      options.onError?.(frame.headers['message'] ?? 'WebSocket STOMP error')
    },
    onWebSocketError: (event) => {
      console.error('STOMP: WebSocket error observed:', event);
      options.onError?.('WebSocket connection error')
    },
    onWebSocketClose: (event) => {
      console.log('STOMP: WebSocket connection closed.', event);
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

// Остальные функции без изменений
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
