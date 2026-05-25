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

// Просто берем готовый URL из переменной окружения.
const brokerURL = import.meta.env.VITE_WS_URL;

export function connectRoomSocket(options: ConnectOptions) {
  disconnectRoomSocket()

  // Проверяем наличие переменной окружения
  if (!brokerURL) {
    console.error("VITE_WS_URL is not defined! Please check your .env file for local development or Vercel environment variables for deployment.");
    options.onError?.("WebSocket URL is not configured.");
    return;
  }

  console.log('Attempting to connect to WebSocket at:', brokerURL);

  const token = useAuthStore.getState().token;

  client = new Client({
    brokerURL,
    connectHeaders: {
      ...(token && { Authorization: `Bearer ${token}` })
    },
    reconnectDelay: 5000,
    onConnect: () => {
      console.log('STOMP: Connected successfully!');
      client?.subscribe(`/topic/rooms/${options.roomId}`, (message) => {
        const event = JSON.parse(message.body) as SocketEvent<Room>
        options.onRoomUpdate(event.payload)
      })
    },
    onStompError: (frame) => {
      console.error('STOMP: Broker reported error:', frame.headers['message']);
    },
    onWebSocketError: (event) => {
      console.error('STOMP: WebSocket error observed:', event);
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
