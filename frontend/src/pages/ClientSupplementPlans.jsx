import { useState } from 'react'
import { getClientActiveSupplementPlan, getClientSupplementPlans } from '../api.js'
import { useLoadData } from '../hooks/useLoadData.js'
import PagePanel from '../components/PagePanel.jsx'

function SupplementPlanCard({ title, plan, featured }) {
  const cardClassName = `card${featured ? ' active-plan-card active-plan-card--featured' : ''}`

  if (!plan) {
    return (
      <div className={cardClassName}>
        {!featured ? <div className="card-title">{title}</div> : null}
        <p className="muted">No plan yet.</p>
      </div>
    )
  }

  return (
    <div className={cardClassName}>
      {!featured ? <div className="card-title">{title}</div> : null}
      <ul className="supplement-entry-list">
        {(plan.entries || []).map((entry, i) => (
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
            {entry.notes ? (
              <div className="supplement-entry-notes card-value">{entry.notes}</div>
            ) : null}
          </li>
        ))}
      </ul>
    </div>
  )
}

function ClientSupplementPlans() {
  const [activePlan, setActivePlan] = useState(null)
  const [plans, setPlans] = useState([])

  const { loading, error } = useLoadData(async () => {
    const [active, list] = await Promise.all([
      getClientActiveSupplementPlan().catch((err) =>
        err.status === 404 ? null : Promise.reject(err),
      ),
      getClientSupplementPlans(),
    ])
    setActivePlan(active)
    setPlans(list || [])
  }, [])

  return (
    <PagePanel
      title="Your supplement plans"
      subtitle="Review your current supplement protocol and past plans."
      loading={loading}
      error={error}
    >
      <SupplementPlanCard title="Active plan" plan={activePlan} featured />
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
                    <div className="card-title">
                      {plan.entries?.length ?? 0} supplement
                      {plan.entries?.length !== 1 ? 's' : ''}
                    </div>
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
                  <ul className="supplement-entry-list">
                    {(plan.entries || []).map((entry, i) => (
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
                        {entry.notes ? (
                          <div className="supplement-entry-notes card-value">{entry.notes}</div>
                        ) : null}
                      </li>
                    ))}
                  </ul>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </PagePanel>
  )
}

export default ClientSupplementPlans
