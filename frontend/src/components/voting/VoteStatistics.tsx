import type { VoteStats } from '../../types/room'

interface Props {
  stats: VoteStats
  revealed: boolean
}

export function VoteStatistics({ stats, revealed }: Props) {
  return (
    <div className="stats-grid">
      <div className="stat-card">
        <span>Проголосовали</span>
        <strong>{stats.votedCount}/{stats.totalVoters}</strong>
      </div>

      <div className="stat-card">
        <span>Среднее</span>
        <strong>{revealed && stats.average !== null ? stats.average.toFixed(1) : '—'}</strong>
      </div>

      <div className="stat-card">
        <span>Минимум</span>
        <strong>{revealed ? stats.min ?? '—' : '—'}</strong>
      </div>

      <div className="stat-card">
        <span>Максимум</span>
        <strong>{revealed ? stats.max ?? '—' : '—'}</strong>
      </div>

      <div className="stat-card">
        <span>Консенсус</span>
        <strong>{revealed ? (stats.consensus ? 'Да' : 'Нет') : '—'}</strong>
      </div>
    </div>
  )
}
