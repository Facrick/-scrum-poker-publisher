import { FormEvent, useEffect, useState } from 'react'
import { roomApi } from '../../api/roomApi'
import type { DeckType, Room, RoomSettings } from '../../types/room'

interface Props {
  room: Room
  moderatorId: string
  onRoomUpdate: (room: Room) => void
}

export function RoomSettingsPanel({ room, moderatorId, onRoomUpdate }: Props) {
  const [settings, setSettings] = useState<RoomSettings>(room.settings)
  const [customDeckText, setCustomDeckText] = useState(room.settings.customDeck.join(', '))
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setSettings(room.settings)
    setCustomDeckText(room.settings.customDeck.join(', '))
  }, [room.settings])

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError(null)

    try {
      const customDeck = customDeckText
        .split(',')
        .map((item) => item.trim())
        .filter(Boolean)

      const updated = await roomApi.updateSettings(room.id, {
        moderatorId,
        ...settings,
        customDeck
      })

      onRoomUpdate(updated)
    } catch {
      setError('Не удалось сохранить настройки')
    } finally {
      setSaving(false)
    }
  }

  function setDeckType(deckType: DeckType) {
    setSettings((current) => ({
      ...current,
      deckType
    }))
  }

  return (
    <section className="panel settings-panel">
      <h2>Настройки комнаты</h2>

      <form className="settings-form" onSubmit={handleSubmit}>
        <label>
          Колода
          <select
            value={settings.deckType}
            onChange={(event) => setDeckType(event.target.value as DeckType)}
          >
            <option value="FIBONACCI">Fibonacci</option>
            <option value="T_SHIRT">Футболки</option>
            <option value="CUSTOM">Своя колода</option>
          </select>
        </label>

        {settings.deckType === 'CUSTOM' && (
          <label>
            Своя колода через запятую
            <input
              value={customDeckText}
              onChange={(event) => setCustomDeckText(event.target.value)}
              placeholder="1, 2, 3, 5, 8"
            />
          </label>
        )}

        <label className="checkbox-label">
          <input
            type="checkbox"
            checked={settings.autoReveal}
            onChange={(event) =>
              setSettings((current) => ({ ...current, autoReveal: event.target.checked }))
            }
          />
          Автоматически открыть голоса, когда все проголосовали
        </label>

        <label className="checkbox-label">
          <input
            type="checkbox"
            checked={settings.observersAllowed}
            onChange={(event) =>
              setSettings((current) => ({ ...current, observersAllowed: event.target.checked }))
            }
          />
          Разрешить наблюдателей
        </label>

        <label className="checkbox-label">
          <input
            type="checkbox"
            checked={settings.timerEnabled}
            onChange={(event) =>
              setSettings((current) => ({ ...current, timerEnabled: event.target.checked }))
            }
          />
          Включить таймер
        </label>

        {settings.timerEnabled && (
          <label>
            Длительность таймера, секунд
            <input
              type="number"
              min={30}
              max={7200}
              value={settings.timerDurationSeconds}
              onChange={(event) =>
                setSettings((current) => ({
                  ...current,
                  timerDurationSeconds: Number(event.target.value)
                }))
              }
            />
          </label>
        )}

        {error && <div className="error">{error}</div>}

        <button type="submit" disabled={saving}>
          {saving ? 'Сохраняем...' : 'Сохранить настройки'}
        </button>
      </form>
    </section>
  )
}
