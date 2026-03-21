import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ForkKnife, PaperPlaneTilt, Plus } from 'phosphor-react'
import { getCoachClients, inviteClient } from '../api.js'
import { openChatDock } from '../chatDockEvents.js'
import { getFieldErrors } from '../errorUtils.js'
import { useLoadData } from '../hooks/useLoadData.js'
import { useFormState } from '../hooks/useFormState.js'
import FormField from '../components/FormField.jsx'
import StatusMessage from '../components/StatusMessage.jsx'
import Button from '../components/Button.jsx'

function CoachClients() {
  const navigate = useNavigate()
  const inviteRef = useRef(null)
  const statusTimerRef = useRef(null)
  const [clients, setClients] = useState([])
  const [inviteEmail, setInviteEmail] = useState('')
  const [showInvite, setShowInvite] = useState(false)

  const { loading, error } = useLoadData(async () => {
    const clientList = await getCoachClients()
    setClients(clientList || [])
  }, [])

  const inviteForm = useFormState()

  async function handleInvite(event) {
    event.preventDefault()
    inviteForm.startSaving()
    try {
      await inviteClient(inviteEmail)
      inviteForm.setSuccess('Invitation sent.')
      if (statusTimerRef.current) {
        clearTimeout(statusTimerRef.current)
      }
      statusTimerRef.current = setTimeout(() => {
        inviteForm.reset()
      }, 1000)
      setInviteEmail('')
    } catch (err) {
      inviteForm.setFailure(err, getFieldErrors(err))
    }
  }

  function handleStartChat(client) {
    const name = `${client.firstName || ''} ${client.lastName || ''}`.trim()
    openChatDock({ targetId: client.id, targetName: name })
  }

  function handleOpenNutritionPlans(clientId, event) {
    event.preventDefault()
    event.stopPropagation()
    navigate(`/coach/clients/${clientId}/nutrition-plans`)
  }

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

  return (
    <section className="panel">
      <div className="panel-header clients-header">
        <div>
          <h1>Your clients</h1>
          <p className="muted">Invite new clients and manage active coaching.</p>
        </div>
        <div className="chat-actions invite-actions" ref={inviteRef}>
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
            <div className="chat-start-menu invite-menu" id="invite-form">
              <div className="chat-start-title">Invite a client</div>
              <form className="form invite-form" onSubmit={handleInvite} autoComplete="off">
                <FormField label="Client email" error={inviteForm.fieldErrors?.email}>
                  <input
                    type="email"
                    name="inviteEmail"
                    id="invite-client-email"
                    autoComplete="off"
                    value={inviteEmail}
                    onChange={(event) => setInviteEmail(event.target.value)}
                    placeholder="client@example.com"
                    required
                  />
                </FormField>
                <Button type="submit" loading={inviteForm.saving} loadingText="Sending...">
                  Send invite
                </Button>
                <StatusMessage status={inviteForm.status} error={inviteForm.error} />
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
                    onClick={(event) => handleOpenNutritionPlans(client.id, event)}
                  >
                    <ForkKnife size={22} weight="bold" />
                    Nutrition
                  </button>
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
