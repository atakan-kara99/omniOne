import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { endClientCoaching, getClientCoach, startChat } from '../api.js'

function ClientCoach() {
  const navigate = useNavigate()
  const [coach, setCoach] = useState(null)
  const [error, setError] = useState('')
  const [status, setStatus] = useState('')
  const [loading, setLoading] = useState(true)
  const [ending, setEnding] = useState(false)
  const [starting, setStarting] = useState(false)

  useEffect(() => {
    let mounted = true

    async function load() {
      setLoading(true)
      setError('')
      try {
        const data = await getClientCoach()
        if (mounted) {
          setCoach(data)
        }
      } catch (err) {
        if (mounted) {
          if (err?.status === 404) {
            setCoach(null)
          } else {
            setError(err.message || 'Failed to load coach information.')
          }
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

  async function handleEndCoaching() {
    const ok = window.confirm('End coaching with your coach?')
    if (!ok) return
    setEnding(true)
    setError('')
    setStatus('')
    try {
      await endClientCoaching()
      setCoach(null)
      setStatus('Coaching relationship ended.')
    } catch (err) {
      setError(err.message || 'Failed to end coaching.')
    } finally {
      setEnding(false)
    }
  }

  async function handleStartChat() {
    if (!coach?.id) return
    setStarting(true)
    setError('')
    setStatus('')
    try {
      const chat = await startChat(coach.id)
      navigate(`/client/chats/${chat.conversationId}`, {
        state: { otherUserId: coach.id },
      })
    } catch (err) {
      setError(err.message || 'Failed to start chat.')
    } finally {
      setStarting(false)
    }
  }

  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <h1>Your coach</h1>
          <p className="muted">Details about your assigned coach.</p>
        </div>
      </div>
      {loading ? <p className="muted">Loading coach...</p> : null}
      {error ? <p className="error">{error}</p> : null}
      {status ? <p className="success">{status}</p> : null}
      {!loading && !error ? (
        coach ? (
          <div className="card">
            <div className="card-title">Coach</div>
            <div className="value">
              {coach.firstName || 'Coach'} {coach.lastName || ''}
            </div>
            <div className="label">Coach ID</div>
            <div className="value">{coach.id}</div>
            <button type="button" onClick={handleStartChat} disabled={starting}>
              {starting ? 'Starting...' : 'Message coach'}
            </button>
            <div className="danger-zone">
              <div>
                <div className="card-title">End coaching</div>
                <p className="muted">This removes your coach assignment.</p>
              </div>
              <button type="button" className="danger-button" onClick={handleEndCoaching} disabled={ending}>
                {ending ? 'Ending...' : 'End coaching'}
              </button>
            </div>
          </div>
        ) : (
          <div className="card">
            <div className="card-title">No coach assigned</div>
            <p className="muted">You have not been assigned a coach yet.</p>
          </div>
        )
      ) : null}
    </section>
  )
}

export default ClientCoach
