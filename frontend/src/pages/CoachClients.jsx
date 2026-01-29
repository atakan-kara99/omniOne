import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { PaperPlaneTilt, Plus } from 'phosphor-react'
import { getCoachClients, inviteClient } from '../api.js'
import { openChatDock } from '../chatDockEvents.js'

function CoachClients() {
  const inviteRef = useRef(null)
  const statusTimerRef = useRef(null)
  const [clients, setClients] = useState([])
  const [inviteEmail, setInviteEmail] = useState('')
  const [status, setStatus] = useState('')
  const [inviteError, setInviteError] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [inviting, setInviting] = useState(false)
  const [showInvite, setShowInvite] = useState(false)

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

  useEffect(() => {
    if (!showInvite) return
    function handleClickOutside(event) {
      if (inviteRef.current && !inviteRef.current.contains(event.target)) {
        setShowInvite(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
    }
  }, [showInvite])

  useEffect(() => {
    return () => {
      if (statusTimerRef.current) {
        clearTimeout(statusTimerRef.current)
      }
    }
  }, [])

  async function handleInvite(event) {
    event.preventDefault()
    setStatus('')
    setInviteError('')
    setInviting(true)
    try {
      await inviteClient(inviteEmail)
      setStatus('Invitation sent.')
      if (statusTimerRef.current) {
        clearTimeout(statusTimerRef.current)
      }
      statusTimerRef.current = setTimeout(() => {
        setStatus('')
      }, 1000)
      setInviteEmail('')
    } catch (err) {
      setInviteError(err.message || 'Failed to send invite.')
    } finally {
      setInviting(false)
    }
  }

  function handleStartChat(client) {
    const name = `${client.firstName || ''} ${client.lastName || ''}`.trim()
    openChatDock({ targetId: client.id, targetName: name })
  }

  return (
    <section className="panel">
      <div className="panel-header clients-header">
        <div>
          <h1>Your clients</h1>
          <p className="muted">Invite new clients and manage active coaching.</p>
        </div>
        <div className="chat-actions invite-actions">
          <button
            type="button"
            className={`icon-button user-action invite-button${showInvite ? ' is-active' : ''}`}
            onClick={() => setShowInvite((prev) => !prev)}
            aria-expanded={showInvite}
            aria-controls="invite-form"
          >
            <Plus size={22} weight="bold" />
            <span className="button-label">Invite</span>
          </button>
          {showInvite ? (
            <div className="chat-start-menu invite-menu" id="invite-form" ref={inviteRef}>
              <div className="chat-start-title">Invite a client</div>
              <form className="form invite-form" onSubmit={handleInvite}>
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
                {inviteError ? <p className="error">{inviteError}</p> : null}
              </form>
            </div>
          ) : null}
        </div>
      </div>
      {loading ? <p className="muted">Loading clients...</p> : null}
      {error ? <p className="error">{error}</p> : null}
      {!loading && !error ? (
        clients.length === 0 ? (
          <p className="muted">No clients yet.</p>
        ) : (
          <div className="client-cards">
            {clients.map((client) => (
              <Link key={client.id} className="card client-card client-card-link" to={`/coach/clients/${client.id}`}>
                <div className="card-title">
                  {client.firstName || 'Client'} {client.lastName || ''}
                </div>
                <div className="inline-actions">
                  <button
                    type="button"
                    className="ghost-button message-button"
                    onClick={(event) => {
                      event.preventDefault()
                      event.stopPropagation()
                      handleStartChat(client)
                    }}
                  >
                    <PaperPlaneTilt size={22} weight="bold" />
                    <span className="button-label">Message</span>
                  </button>
                </div>
              </Link>
            ))}
          </div>
        )
      ) : null}
    </section>
  )
}

export default CoachClients
