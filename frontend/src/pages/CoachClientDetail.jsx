import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  addCoachClientPlan,
  endCoaching,
  getCoachClient,
  getCoachClientActivePlan,
  getCoachClientAnswers,
  getCoachClientPlans,
  updateCoachClientPlan,
} from '../api.js'

const EMPTY_PLAN = {
  carbs: '',
  proteins: '',
  fats: '',
  water: '',
  salt: '',
  fiber: '',
}

function CoachClientDetail() {
  const { clientId } = useParams()
  const navigate = useNavigate()
  const [client, setClient] = useState(null)
  const [plans, setPlans] = useState([])
  const [activePlan, setActivePlan] = useState(null)
  const [answers, setAnswers] = useState([])
  const [planForm, setPlanForm] = useState(EMPTY_PLAN)
  const [editingPlanId, setEditingPlanId] = useState(null)
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  const nutritionFields = useMemo(
    () => [
      { key: 'carbs', label: 'Carbs (g)' },
      { key: 'proteins', label: 'Proteins (g)' },
      { key: 'fats', label: 'Fats (g)' },
      { key: 'water', label: 'Water (L)' },
      { key: 'salt', label: 'Salt (g)' },
      { key: 'fiber', label: 'Fiber (g)' },
    ],
    [],
  )

  useEffect(() => {
    let mounted = true

    async function load() {
      setLoading(true)
      setError('')
      try {
        const [clientData, active, list, questionnaire] = await Promise.all([
          getCoachClient(clientId),
          getCoachClientActivePlan(clientId).catch((err) => (err.status === 404 ? null : Promise.reject(err))),
          getCoachClientPlans(clientId),
          getCoachClientAnswers(clientId),
        ])
        if (mounted) {
          setClient(clientData)
          setActivePlan(active)
          setPlans(list || [])
          setAnswers(questionnaire || [])
        }
      } catch (err) {
        if (mounted) {
          setError(err.message || 'Failed to load client.')
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

  async function handlePlanSubmit(event) {
    event.preventDefault()
    setStatus('')
    setError('')
    setSaving(true)

    const payload = {
      carbs: Number(planForm.carbs),
      proteins: Number(planForm.proteins),
      fats: Number(planForm.fats),
    }
    if (planForm.water !== '') payload.water = Number(planForm.water)
    if (planForm.salt !== '') payload.salt = Number(planForm.salt)
    if (planForm.fiber !== '') payload.fiber = Number(planForm.fiber)

    try {
      if (editingPlanId) {
        await updateCoachClientPlan(clientId, editingPlanId, payload)
        setStatus('Plan updated.')
      } else {
        await addCoachClientPlan(clientId, payload)
        setStatus('Plan added.')
      }
      const [active, list] = await Promise.all([
        getCoachClientActivePlan(clientId),
        getCoachClientPlans(clientId),
      ])
      setActivePlan(active)
      setPlans(list || [])
      setPlanForm(EMPTY_PLAN)
      setEditingPlanId(null)
    } catch (err) {
      setError(err.message || 'Failed to save plan.')
    } finally {
      setSaving(false)
    }
  }

  async function handleEndCoaching() {
    const ok = window.confirm('End coaching for this client?')
    if (!ok) return
    try {
      await endCoaching(clientId)
      navigate('/coach/clients')
    } catch (err) {
      setError(err.message || 'Failed to end coaching.')
    }
  }

  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <h1>Client detail</h1>
          <p className="muted">Manage plans and review questionnaire answers.</p>
        </div>
        <Link className="text-link" to="/coach/clients">Back to clients</Link>
      </div>
      {loading ? <p className="muted">Loading client...</p> : null}
      {error ? <p className="error">{error}</p> : null}
      {!loading && client ? (
        <>
          <div className="stat-grid">
            <div className="stat">
              <div className="label">Client</div>
              <div className="value">{client.firstName || 'Client'} {client.lastName || ''}</div>
              <div className="label">Client ID</div>
              <div className="value">{client.id}</div>
            </div>
            <div className="stat">
              <div className="label">Active calories</div>
              <div className="value">{activePlan?.calories ?? '—'}</div>
            </div>
            <div className="stat">
              <div className="label">Plans total</div>
              <div className="value">{plans.length}</div>
            </div>
          </div>
          <div className="split-grid">
            <div className="card">
              <div className="card-title">Nutrition plan editor</div>
              <form className="form" onSubmit={handlePlanSubmit}>
                {nutritionFields.map((field) => (
                  <label key={field.key} className="field">
                    <span>{field.label}</span>
                    <input
                      type="number"
                      min="0"
                      step="0.1"
                      value={planForm[field.key]}
                      onChange={(event) =>
                        setPlanForm((prev) => ({
                          ...prev,
                          [field.key]: event.target.value,
                        }))
                      }
                      required={['carbs', 'proteins', 'fats'].includes(field.key)}
                    />
                  </label>
                ))}
                <button type="submit" disabled={saving}>
                  {saving ? 'Saving...' : editingPlanId ? 'Update plan' : 'Create plan'}
                </button>
                {status ? <p className="success">{status}</p> : null}
              </form>
            </div>
            <div className="card">
              <div className="card-title">Plan history</div>
              {plans.length === 0 ? (
                <p className="muted">No plans yet.</p>
              ) : (
                <ul className="card-list">
                  {plans.map((plan) => (
                    <li key={plan.createdAt} className="list-item">
                      <div>
                        <div className="card-title">{new Date(plan.createdAt).toLocaleString()}</div>
                        <div className="card-value">{plan.calories?.toFixed?.(0) ?? plan.calories} kcal</div>
                      </div>
                      {plan.id ? (
                        <button
                          type="button"
                          className="ghost-button"
                          onClick={() => {
                            setEditingPlanId(plan.id)
                            setPlanForm({
                              carbs: plan.carbs ?? '',
                              proteins: plan.proteins ?? '',
                              fats: plan.fats ?? '',
                              water: plan.water ?? '',
                              salt: plan.salt ?? '',
                              fiber: plan.fiber ?? '',
                            })
                          }}
                        >
                          Edit
                        </button>
                      ) : null}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
          <div className="card">
            <div className="card-title">Questionnaire responses</div>
            {answers.length === 0 ? (
              <p className="muted">No answers yet.</p>
            ) : (
              <ul className="qa-list">
                {answers.map((answer) => (
                  <li key={answer.questionId}>
                    <div className="qa-question">{answer.questionText}</div>
                    <div className="qa-answer">{answer.answerText || '—'}</div>
                  </li>
                ))}
              </ul>
            )}
          </div>
          <div className="danger-zone">
            <div>
              <div className="card-title">End coaching</div>
              <p className="muted">This will remove the client from your roster.</p>
            </div>
            <button type="button" className="danger-button" onClick={handleEndCoaching}>
              End coaching
            </button>
          </div>
        </>
      ) : null}
    </section>
  )
}

export default CoachClientDetail
