import { useState } from 'react'
import { LinkBreak, PaperPlaneTilt } from 'phosphor-react'
import { endClientCoaching, getClientCoach } from '../api.js'
import { openChatDock } from '../chatDockEvents.js'
import { useLoadData } from '../hooks/useLoadData.js'
import { useFormState } from '../hooks/useFormState.js'
import PagePanel from '../components/PagePanel.jsx'
import StatusMessage from '../components/StatusMessage.jsx'
import Button from '../components/Button.jsx'

function ClientCoach() {
  const [coach, setCoach] = useState(null)
  const [ending, setEnding] = useState(false)
  const form = useFormState()

  const { loading, error: loadError } = useLoadData(async () => {
    try {
      const data = await getClientCoach()
      setCoach(data)
    } catch (err) {
      if (err?.status === 404) {
        setCoach(null)
      } else {
        throw err
      }
    }
  }, [])

  async function handleEndCoaching() {
    const ok = window.confirm('End coaching with your coach?')
    if (!ok) return
    setEnding(true)
    form.reset()
    try {
      await endClientCoaching()
      setCoach(null)
      form.setSuccess('Coaching relationship ended.')
    } catch (err) {
      form.setFailure(err || 'Failed to end coaching.')
    } finally {
      setEnding(false)
    }
  }

  function handleStartChat() {
    if (!coach?.id) return
    const name = `${coach.firstName || ''} ${coach.lastName || ''}`.trim()
    openChatDock({ targetId: coach.id, targetName: name })
  }

  return (
    <PagePanel
      title="Your coach"
      subtitle="Details about your assigned coach."
      loading={loading}
      error={loadError}
    >
      <StatusMessage status={form.status} error={form.error} />
      {coach ? (
        <div className="card">
          <div className="card-header-row">
            <div className="card-title">
              {coach.firstName || 'Coach'} {coach.lastName || ''}
            </div>
            <div className="inline-actions">
              <button
                type="button"
                className="ghost-button message-button"
                onClick={handleStartChat}
              >
                <PaperPlaneTilt size={22} weight="bold" />
                <span className="button-label">Message</span>
              </button>
              <Button
                variant="danger"
                onClick={handleEndCoaching}
                loading={ending}
                loadingText="Ending..."
              >
                <LinkBreak size={24} />
                End coaching
              </Button>
            </div>
          </div>
        </div>
      ) : (
        <div className="card">
          <div className="card-title">No coach assigned</div>
          <p className="muted">You have not been assigned a coach yet.</p>
        </div>
      )}
    </PagePanel>
  )
}

export default ClientCoach
