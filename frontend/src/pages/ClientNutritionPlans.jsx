import { useEffect, useState } from 'react'
import { getClientActivePlan, getClientPlans } from '../api.js'
import { formatErrorMessage } from '../errorUtils.js'

function PlanCard({ title, plan }) {
  const isActiveCard = title === 'Active plan'
  const cardClassName = `card${isActiveCard ? ' active-plan-card active-plan-card--featured' : ''}`

  if (!plan) {
    return (
      <div className={cardClassName}>
        {!isActiveCard ? <div className="card-title">{title}</div> : null}
        <p className="muted">No plan yet.</p>
      </div>
    )
  }
  return (
    <div className={cardClassName}>
      {!isActiveCard ? <div className="card-title">{title}</div> : null}
      <div className="plan-grid">
        <div>
          <div className="label">Calories</div>
          <div className="value">{plan.calories ?? '—'} kcal</div>
        </div>
        <div>
          <div className="label">Carbs</div>
          <div className="value">{plan.carbs ?? '—'} g</div>
        </div>
        <div>
          <div className="label">Proteins</div>
          <div className="value">{plan.proteins ?? '—'} g</div>
        </div>
        <div>
          <div className="label">Fats</div>
          <div className="value">{plan.fats ?? '—'} g</div>
        </div>
        <div>
          <div className="label">Water</div>
          <div className="value">{plan.water ?? '—'} L</div>
        </div>
        <div>
          <div className="label">Fiber</div>
          <div className="value">{plan.fiber ?? '—'} g</div>
        </div>
      </div>
    </div>
  )
}

function ClientNutritionPlans() {
  const [activePlan, setActivePlan] = useState(null)
  const [plans, setPlans] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let mounted = true

    async function load() {
      setLoading(true)
      setError('')
      try {
        const [active, list] = await Promise.all([
          getClientActivePlan().catch((err) => (err.status === 404 ? null : Promise.reject(err))),
          getClientPlans(),
        ])
        if (mounted) {
          setActivePlan(active)
          setPlans(list || [])
        }
      } catch (err) {
        if (mounted) {
          setError(err || 'Failed to load plans.')
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
          <h1>Your nutrition plans</h1>
          <p className="muted">Review your current macro targets and past plans.</p>
        </div>
      </div>
      {loading ? <p className="muted">Loading plans...</p> : null}
      {error ? <p className="error">{formatErrorMessage(error)}</p> : null}
      {!loading && !error ? (
        <>
          <PlanCard title="Active plan" plan={activePlan} />
          <div className="card client-plan-history-card">
            <div className="card-title">Plan history</div>
            {plans.length === 0 ? (
              <p className="muted">No historical plans yet.</p>
            ) : (
                <ul className="card-list plan-history-list">
                  {plans.map((plan) => (
                    <li key={plan.createdAt} className="list-item plan-history-item">
                      <div>
                        <div className="plan-history-header">
                          <div className="card-title">Calories: {plan.calories ?? '—'} kcal</div>
                          <div className="card-title plan-history-timestamp">
                            {new Date(plan.createdAt).toLocaleTimeString([], {
                              hour: '2-digit',
                              minute: '2-digit',
                            })}
                            ,{' '}
                            {new Date(plan.createdAt).toLocaleDateString([], {
                              year: 'numeric',
                              month: '2-digit',
                              day: '2-digit',
                            })}
                          </div>
                        </div>
                        <div className="plan-history-values">
                          <div className="card-value">Carbs: {plan.carbs ?? '—'} g</div>
                          <div className="card-value">Proteins: {plan.proteins ?? '—'} g</div>
                          <div className="card-value">Fats: {plan.fats ?? '—'} g</div>
                          <div className="card-value">Water: {plan.water ?? '—'} L</div>
                          <div className="card-value">Salt: {plan.salt ?? '—'} g</div>
                          <div className="card-value">Fiber: {plan.fiber ?? '—'} g</div>
                        </div>
                      </div>
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

export default ClientNutritionPlans
