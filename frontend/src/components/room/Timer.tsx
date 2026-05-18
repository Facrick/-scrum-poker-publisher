import { useEffect, useMemo, useState } from 'react'

interface Props {
  enabled: boolean
  durationSeconds: number
  roundId: string
}

export function Timer({ enabled, durationSeconds, roundId }: Props) {
  const [secondsLeft, setSecondsLeft] = useState(durationSeconds)

  useEffect(() => {
    setSecondsLeft(durationSeconds)

    if (!enabled) {
      return
    }

    const interval = window.setInterval(() => {
      setSecondsLeft((value) => Math.max(0, value - 1))
    }, 1000)

    return () => window.clearInterval(interval)
  }, [enabled, durationSeconds, roundId])

  const formatted = useMemo(() => {
    const minutes = Math.floor(secondsLeft / 60)
    const seconds = secondsLeft % 60
    return `${minutes}:${seconds.toString().padStart(2, '0')}`
  }, [secondsLeft])

  if (!enabled) {
    return null
  }

  return (
    <div className="timer">
      <span>Таймер</span>
      <strong>{formatted}</strong>
    </div>
  )
}
