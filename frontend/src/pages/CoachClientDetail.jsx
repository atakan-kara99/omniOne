import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { CaretRight } from 'phosphor-react'
import { endCoaching, getCoachClient, getCoachClientActivePlan, getCoachClientAnswers } from '../api.js'
import { formatErrorMessage } from '../errorUtils.js'

function CoachClientDetail() {
  const { clientId } = useParams()
  const navigate = useNavigate()
  const [client, setClient] = useState(null)
  const [activePlan, setActivePlan] = useState(null)
  const [answers, setAnswers] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let mounted = true

    async function load() {
      setLoading(true)
      setError('')
      try {
        const [clientData, active, questionnaire] = await Promise.all([
          getCoachClient(clientId),
          getCoachClientActivePlan(clientId).catch((err) => (err.status === 404 ? null : Promise.reject(err))),
          getCoachClientAnswers(clientId),
        ])
        if (mounted) {
          setClient(clientData)
          setActivePlan(active)
          setAnswers(questionnaire || [])
        }
      } catch (err) {
        if (mounted) {
          setError(err || 'Failed to load client.')
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
  }, [clientId])

  async function handleEndCoaching() {
    const ok = window.confirm('End coaching for this client?')
    if (!ok) return
    try {
      await endCoaching(clientId)
      navigate('/coach/clients')
    } catch (err) {
      setError(err || 'Failed to end coaching.')
    }
  }

  const clientName = `${client?.firstName || ''} ${client?.lastName || ''}`.trim() || 'Client'

  return (
    <section className="panel">
      <div className="panel-header client-detail-header">
        <div>
          <h1>{clientName}</h1>
          <p className="muted">Manage client details</p>
        </div>
        <Link className="back-button" to="/coach/clients">
          <span className="button-label">Back</span>
          <CaretRight size={22} weight="bold" />
        </Link>
      </div>
      {loading ? <p className="muted">Loading client...</p> : null}
      {error ? <p className="error">{formatErrorMessage(error)}</p> : null}
      {!loading && client ? (
        <>
          <div className="client-detail-grid">
            <Link to={`/coach/clients/${clientId}/nutrition-plans`} className="card client-card-link client-detail-nutrition-link">
              <div className="card-title">Nutrition plans</div>
              <div className="plan-grid">
                <div>
                  <div className="label">Calories</div>
                  <div className="value">{activePlan?.calories ?? '—'} kcal</div>
                </div>
                <div>
                  <div className="label">Carbs</div>
                  <div className="value">{activePlan?.carbs ?? '—'} g</div>
                </div>
                <div>
                  <div className="label">Proteins</div>
                  <div className="value">{activePlan?.proteins ?? '—'} g</div>
                </div>
                <div>
                  <div className="label">Fats</div>
                  <div className="value">{activePlan?.fats ?? '—'} g</div>
                </div>
                <div>
                  <div className="label">Water</div>
                  <div className="value">{activePlan?.water ?? '—'} L</div>
                </div>
                <div>
                  <div className="label">Fiber</div>
                  <div className="value">{activePlan?.fiber ?? '—'} g</div>
                </div>
              </div>
            </Link>
            <Link
              to={`/coach/clients/${clientId}/questionnaire-responses`}
              className="card client-card-link client-detail-questionnaire-link"
            >
              <div className="card-title">Questionnaire answers</div>
              {answers.length === 0 ? (
                <p className="muted">No answers yet.</p>
              ) : (
                <ul className="qa-list">
                  {answers.slice(0, 1).map((answer) => (
                    <li key={answer.questionId}>
                      <div className="qa-question">{answer.questionText}</div>
                      <div className="qa-answer">{answer.answerText || '—'}</div>
                    </li>
                  ))}
                </ul>
              )}
              {answers.length > 1 ? (
                <p className="muted client-detail-more-hint">{`+${answers.length - 1} more responses`}</p>
              ) : null}
            </Link>
            <div className="danger-zone client-detail-danger-zone">
              <div>
                <div className="card-title">End coaching</div>
                <p className="muted">This will remove the client from your roster.</p>
              </div>
              <button type="button" className="danger-button" onClick={handleEndCoaching}>
                End coaching
              </button>
            </div>
          </div>
        </>
      ) : null}
    </section>
  )
}

export default CoachClientDetail
