import { useEffect, useState } from 'react'
import { sendVote } from '../../websocket/socket'

interface Props {
  roomId: string
  participantId: string
  deck: string[]
  disabled: boolean
  roundId: string
}

export function VotingCards({
  roomId,
  participantId,
  deck,
  disabled,
  roundId
}: Props) {
  const [selected, setSelected] = useState<string | null>(null)

  useEffect(() => {
    setSelected(null)
  }, [roundId])

  function handleVote(value: string) {
    setSelected(value)

    sendVote(roomId, {
      participantId,
      value
    })
  }

  return (
    <div className="cards">
      {deck.map((card) => (
        <button
          key={card}
          className={`vote-card ${selected === card ? 'vote-card-selected' : ''}`}
          disabled={disabled}
          onClick={() => handleVote(card)}
        >
          {card}
        </button>
      ))}
    </div>
  )
}
