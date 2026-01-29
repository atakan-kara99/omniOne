import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getCoach, getCoachClients, getChats } from '../api.js'

function CoachDashboard() {
  const [coach, setCoach] = useState(null)
  const [clients, setClients] = useState([])
  const [chats, setChats] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let mounted = true

    async function load() {
      setLoading(true)
      setError('')
      try {
        const [coachData, clientList, chatList] = await Promise.all([
          getCoach(),
          getCoachClients(),
          getChats(),
        ])
        if (mounted) {
          setCoach(coachData)
          setClients(clientList || [])
          setChats(chatList || [])
        }
      } catch (err) {
        if (mounted) {
          setError(err.message || 'Failed to load coach data.')
        }
      } finally {
        if (mounted) {
          setLoading(false)
        }
      }
    }

    load()
    return () => {
      mounted = false
    }
  }, [])

  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <h1>Coach dashboard</h1>
          <p className="muted">Keep tabs on client progress and upcoming touchpoints.</p>
        </div>
        <Link className="primary-link" to="/coach/clients">
          View clients
        </Link>
      </div>
      {loading ? <p className="muted">Loading coach data...</p> : null}
      {error ? <p className="error">{error}</p> : null}
      {!loading && !error ? (
        <>
          <div className="stat-grid">
            <div className="stat">
              <div className="label">Coach ID</div>
              <div className="value">{coach?.id || '—'}</div>
            </div>
            <div className="stat">
              <div className="label">Active clients</div>
              <div className="value">{clients.length}</div>
            </div>
            <div className="stat">
              <div className="label">Open chats</div>
              <div className="value">{chats.length}</div>
            </div>
          </div>
          <div className="list">
            <div className="section-title">Recent clients</div>
            {clients.length === 0 ? (
              <p className="muted">No clients yet.</p>
            ) : (
              <ul className="card-list">
                {clients.slice(0, 4).map((client) => (
                  <li key={client.id} className="card">
                    <div>
                      <div className="card-title">
                        {client.firstName || 'Client'} {client.lastName || ''}
                      </div>
                      <div className="card-value">{client.id}</div>
                    </div>
                    <Link className="text-link" to={`/coach/clients/${client.id}`}>
                      View details
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </>
      ) : null}
    </section>
  )
}

export default CoachDashboard
