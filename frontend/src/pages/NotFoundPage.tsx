import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <main className="page page-center">
      <section className="panel home-panel">
        <h1>404</h1>
        <p className="muted">Страница не найдена</p>
        <Link className="link-button" to="/">
          На главную
        </Link>
      </section>
    </main>
  )
}
