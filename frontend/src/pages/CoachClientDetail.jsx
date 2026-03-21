import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { CaretRight } from 'phosphor-react'
import { endCoaching, getCoachClient, getCoachClientActivePlan, getCoachClientActiveSupplementPlan, getCoachClientAnswers } from '../api.js'
import { useLoadData } from '../hooks/useLoadData.js'
import { useFormState } from '../hooks/useFormState.js'
import PagePanel from '../components/PagePanel.jsx'
import Button from '../components/Button.jsx'

function CoachClientDetail() {
  const { clientId } = useParams()
  const navigate = useNavigate()
  const [client, setClient] = useState(null)
  const [activePlan, setActivePlan] = useState(null)
  const [activeSupplementPlan, setActiveSupplementPlan] = useState(null)
  const [answers, setAnswers] = useState([])

  const { loading, error } = useLoadData(async () => {
    const [clientData, active, activeSupp, questionnaire] = await Promise.all([
      getCoachClient(clientId),
      getCoachClientActivePlan(clientId).catch((err) => (err.status === 404 ? null : Promise.reject(err))),
      getCoachClientActiveSupplementPlan(clientId).catch((err) => (err.status === 404 ? null : Promise.reject(err))),
      getCoachClientAnswers(clientId),
    ])
    setClient(clientData)
    setActivePlan(active)
    setActiveSupplementPlan(activeSupp)
    setAnswers(questionnaire || [])
  }, [clientId])

  const endCoachingForm = useFormState()

  async function handleEndCoaching() {
    const ok = window.confirm('End coaching for this client?')
    if (!ok) return
    endCoachingForm.startSaving()
    try {
      await endCoaching(clientId)
      navigate('/coach/clients')
    } catch (err) {
      endCoachingForm.setError(err || 'Failed to end coaching.')
    }
  }

  const clientName = `${client?.firstName || ''} ${client?.lastName || ''}`.trim() || 'Client'

  return (
    <PagePanel
      title={clientName}
      subtitle="Manage client details"
      loading={loading}
      error={error}
      actions={
        <Link className="back-button" to="/coach/clients">
          <span className="button-label">Back</span>
          <CaretRight size={22} weight="bold" />
        </Link>
      }
    >
      {client ? (
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
              to={`/coach/clients/${clientId}/supplement-plans`}
              className="card client-card-link"
            >
              <div className="card-title">Supplement plans</div>
              {activeSupplementPlan && activeSupplementPlan.entries?.length > 0 ? (
                <>
                  <ul className="supplement-entry-list">
                    {activeSupplementPlan.entries.slice(0, 3).map((entry, i) => (
                      <li key={entry.id ?? i} className="supplement-entry-item">
                        <div className="supplement-entry-name">
                          {entry.supplementName}
                          {entry.brand ? (
                            <span className="supplement-entry-brand"> — {entry.brand}</span>
                          ) : null}
                        </div>
                        <div className="supplement-entry-meta">
                          <span className="card-value">{entry.dosage}</span>
                          {entry.timing ? (
                            <span className="card-value">{entry.timing}</span>
                          ) : null}
                        </div>
                      </li>
                    ))}
                    {activeSupplementPlan.entries.length > 3 ? (
                      <p className="muted client-detail-more-hint">
                        {`+${activeSupplementPlan.entries.length - 3} more`}
                      </p>
                    ) : null}
                  </ul>
                  <p className="muted client-detail-more-hint">Manage plans →</p>
                </>
              ) : (
                <p className="muted">No plan yet — click to create one →</p>
              )}
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
              <Button
                variant="danger"
                loading={endCoachingForm.saving}
                onClick={handleEndCoaching}
              >
                End coaching
              </Button>
              {endCoachingForm.error ? <p className="error">{endCoachingForm.error}</p> : null}
            </div>
          </div>
        </>
      ) : null}
    </PagePanel>
  )
}

export default CoachClientDetail
