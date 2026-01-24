import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getCoachClients, inviteClient, startChat } from '../api.js'

function CoachClients() {
  const navigate = useNavigate()
  const [clients, setClients] = useState([])
  const [inviteEmail, setInviteEmail] = useState('')
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [inviting, setInviting] = useState(false)
  const [startingId, setStartingId] = useState(null)

  useEffect(() => {
    let mounted = true

    async function load() {
      setLoading(true)
      setError('')
      try {
        const clientList = await getCoachClients()
        if (mounted) {
          setClients(clientList || [])
        }
      } catch (err) {
        if (mounted) {
          setError(err.message || 'Failed to load clients.')
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

  async function handleInvite(event) {
    event.preventDefault()
    setStatus('')
    setError('')
    setInviting(true)
    try {
      await inviteClient(inviteEmail)
      setStatus('Invitation sent.')
      setInviteEmail('')
    } catch (err) {
      setError(err.message || 'Failed to send invite.')
    } finally {
      setInviting(false)
    }
  }

  async function handleStartChat(clientId) {
    setError('')
    setStatus('')
    setStartingId(clientId)
    try {
      const chat = await startChat(clientId)
      navigate(`/coach/chats/${chat.conversationId}`, {
        state: { otherUserId: clientId },
      })
    } catch (err) {
      setError(err.message || 'Failed to start chat.')
    } finally {
      setStartingId(null)
    }
  }

  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <h1>Your clients</h1>
          <p className="muted">Invite new clients and manage active coaching.</p>
        </div>
      </div>
      <div className="split-grid">
        <div className="card">
          <div className="card-title">Invite a client</div>
          <form className="form" onSubmit={handleInvite}>
            <label className="field">
              <span>Client email</span>
              <input
                type="email"
                value={inviteEmail}
                onChange={(event) => setInviteEmail(event.target.value)}
                placeholder="client@example.com"
                required
              />
            </label>
            <button type="submit" disabled={inviting}>
              {inviting ? 'Sending...' : 'Send invite'}
            </button>
            {status ? <p className="success">{status}</p> : null}
          </form>
        </div>
        <div className="card">
          <div className="card-title">Active roster</div>
          {loading ? <p className="muted">Loading clients...</p> : null}
          {error ? <p className="error">{error}</p> : null}
          {!loading && !error ? (
            clients.length === 0 ? (
              <p className="muted">No clients yet.</p>
            ) : (
              <ul className="card-list">
                {clients.map((client) => (
                  <li key={client.id} className="list-item">
                    <div>
                      <div className="card-title">
                        {client.firstName || 'Client'} {client.lastName || ''}
                      </div>
                      <div className="muted">{client.id}</div>
                    </div>
                    <div className="inline-actions">
                      <button
                        type="button"
                        className="ghost-button"
                        onClick={() => handleStartChat(client.id)}
                        disabled={startingId === client.id}
                      >
                        {startingId === client.id ? 'Starting...' : 'Message'}
                      </button>
                      <Link className="text-link" to={`/coach/clients/${client.id}`}>
                        Open
                      </Link>
                    </div>
                  </li>
                ))}
              </ul>
            )
          ) : null}
        </div>
      </div>
    </section>
  )
}

export default CoachClients
