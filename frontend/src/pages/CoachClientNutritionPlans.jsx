import { useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { CaretRight, FloppyDisk, PencilSimple, Plus, X } from 'phosphor-react'
import {
  addCoachClientPlan,
  getCoachClient,
  getCoachClientPlans,
  updateCoachClientPlan,
} from '../api.js'
import { useLoadData } from '../hooks/useLoadData.js'
import { useFormState } from '../hooks/useFormState.js'
import FormField from '../components/FormField.jsx'
import StatusMessage from '../components/StatusMessage.jsx'
import Button from '../components/Button.jsx'

const EMPTY_PLAN = {
  carbs: '',
  proteins: '',
  fats: '',
  water: '',
  salt: '',
  fiber: '',
}
const INTEGER_PLAN_FIELDS = ['carbs', 'proteins', 'fats']
const WATER_GRAMS_PER_LITER = 1000

function CoachClientNutritionPlans() {
  const { clientId } = useParams()
  const [client, setClient] = useState(null)
  const [plans, setPlans] = useState([])
  const [planForm, setPlanForm] = useState(EMPTY_PLAN)
  const [historyEditingPlanId, setHistoryEditingPlanId] = useState(null)
  const [historyEditForm, setHistoryEditForm] = useState(EMPTY_PLAN)
  const [historySaving, setHistorySaving] = useState(false)

  const form = useFormState()

  const { loading, error: loadError } = useLoadData(
    async () => {
      const [clientData, list] = await Promise.all([
        getCoachClient(clientId),
        getCoachClientPlans(clientId),
      ])
      setClient(clientData)
      setPlans(list || [])
    },
    [clientId],
  )

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

  async function handlePlanSubmit(event) {
    event.preventDefault()
    form.startSaving()

    const hasInvalidIntegerMacro = INTEGER_PLAN_FIELDS.some(
      (key) => planForm[key] === '' || !/^\d+$/.test(planForm[key]),
    )
    if (hasInvalidIntegerMacro) {
      form.setErrorManual('Carbs, proteins, and fats must be natural numbers.')
      return
    }

    const payload = {
      carbs: Number.parseInt(planForm.carbs, 10),
      proteins: Number.parseInt(planForm.proteins, 10),
      fats: Number.parseInt(planForm.fats, 10),
    }
    if (planForm.water !== '') payload.water = Math.round(Number(planForm.water) * WATER_GRAMS_PER_LITER)
    if (planForm.salt !== '') payload.salt = Number(planForm.salt)
    if (planForm.fiber !== '') payload.fiber = Number(planForm.fiber)

    try {
      await addCoachClientPlan(clientId, payload)
      form.setSuccess('Plan added.')
      const list = await getCoachClientPlans(clientId)
      setPlans(list || [])
      setPlanForm(EMPTY_PLAN)
    } catch (err) {
      form.setFailure(err)
    }
  }

  function handleEditPlan(plan) {
    const planId = plan.id
    if (!planId) {
      form.setErrorManual('This plan cannot be edited because its identifier is missing.')
      return
    }
    setHistoryEditingPlanId(planId)
    setHistoryEditForm({
      carbs: plan.carbs ?? '',
      proteins: plan.proteins ?? '',
      fats: plan.fats ?? '',
      water: plan.water != null ? String(Number(plan.water) / WATER_GRAMS_PER_LITER) : '',
      salt: plan.salt ?? '',
      fiber: plan.fiber ?? '',
    })
    form.reset()
  }

  function handleHistoryEditFieldChange(fieldKey, nextValue) {
    if (INTEGER_PLAN_FIELDS.includes(fieldKey) && nextValue !== '' && !/^\d+$/.test(nextValue)) {
      return
    }
    setHistoryEditForm((prev) => ({
      ...prev,
      [fieldKey]: nextValue,
    }))
  }

  function handleCancelHistoryEdit() {
    setHistoryEditingPlanId(null)
    setHistoryEditForm(EMPTY_PLAN)
  }

  async function handleSaveHistoryEdit(planId) {
    form.reset()

    const hasInvalidIntegerMacro = INTEGER_PLAN_FIELDS.some(
      (key) => historyEditForm[key] === '' || !/^\d+$/.test(historyEditForm[key]),
    )
    if (hasInvalidIntegerMacro) {
      form.setErrorManual('Carbs, proteins, and fats must be natural numbers.')
      return
    }

    const payload = {
      carbs: Number.parseInt(historyEditForm.carbs, 10),
      proteins: Number.parseInt(historyEditForm.proteins, 10),
      fats: Number.parseInt(historyEditForm.fats, 10),
    }
    if (historyEditForm.water !== '') {
      payload.water = Math.round(Number(historyEditForm.water) * WATER_GRAMS_PER_LITER)
    }
    if (historyEditForm.salt !== '') payload.salt = Number(historyEditForm.salt)
    if (historyEditForm.fiber !== '') payload.fiber = Number(historyEditForm.fiber)

    setHistorySaving(true)
    try {
      await updateCoachClientPlan(clientId, planId, payload)
      const list = await getCoachClientPlans(clientId)
      setPlans(list || [])
      setHistoryEditingPlanId(null)
      setHistoryEditForm(EMPTY_PLAN)
      form.setSuccess('Plan updated.')
    } catch (err) {
      form.setFailure(err)
    } finally {
      setHistorySaving(false)
    }
  }

  const clientName = `${client?.firstName || ''} ${client?.lastName || ''}`.trim() || 'Client'

  return (
    <section className="panel">
      <div className="panel-header client-detail-header">
        <div>
          <h1>{clientName}</h1>
          <p className="muted">Manage nutrition plans</p>
        </div>
        <Link className="back-button" to={`/coach/clients/${clientId}`}>
          <span className="button-label">Back</span>
          <CaretRight size={22} weight="bold" />
        </Link>
      </div>
      {loading ? <p className="muted">Loading nutrition plans...</p> : null}
      {loadError ? <p className="error">{loadError}</p> : null}
      {!loading && client ? (
        <div className="split-grid client-detail-plan-stack">
          <div className="card">
            <div className="card-title">Nutrition plan creator</div>
            <form className="form nutrition-plan-form" onSubmit={handlePlanSubmit} autoComplete="off">
              {nutritionFields.map((field) => (
                <FormField
                  key={field.key}
                  label={field.label}
                  error={form.fieldErrors?.[field.key]}
                  className="nutrition-plan-field"
                >
                  <input
                    type="number"
                    name={field.key}
                    id={`plan-${field.key}`}
                    autoComplete="off"
                    inputMode={INTEGER_PLAN_FIELDS.includes(field.key) ? 'numeric' : 'decimal'}
                    min="0"
                    step={INTEGER_PLAN_FIELDS.includes(field.key) ? '1' : '0.1'}
                    value={planForm[field.key]}
                    onChange={(event) => {
                      const nextValue = event.target.value
                      if (
                        INTEGER_PLAN_FIELDS.includes(field.key) &&
                        nextValue !== '' &&
                        !/^\d+$/.test(nextValue)
                      ) {
                        return
                      }
                      setPlanForm((prev) => ({
                        ...prev,
                        [field.key]: nextValue,
                      }))
                    }}
                    required={INTEGER_PLAN_FIELDS.includes(field.key)}
                  />
                </FormField>
              ))}
              <Button
                type="submit"
                className="nutrition-plan-submit"
                loading={form.saving}
                loadingText="Saving..."
              >
                <Plus size={22} weight="bold" />
                Create
              </Button>
              <StatusMessage status={form.status} error={form.error} />
            </form>
          </div>
          <div className="card">
            <div className="card-title">Nutrition plan history</div>
            {plans.length === 0 ? (
              <p className="muted">No plans yet.</p>
            ) : (
              <ul className="card-list plan-history-list">
                {plans.map((plan) => (
                  <li
                    key={plan.id ?? plan.createdAt}
                    className={`list-item plan-history-item${historyEditingPlanId === plan.id ? ' is-editing' : ''}`}
                  >
                    <div>
                      <div className="plan-history-header">
                        <div className="card-title">
                          Calories: {plan.calories?.toFixed?.(0) ?? plan.calories ?? '—'} kcal
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
                      {historyEditingPlanId === plan.id ? (
                        <div className="plan-history-values plan-history-values-editing">
                          {nutritionFields.map((field) => (
                            <FormField
                              key={field.key}
                              label={field.label}
                              error={form.fieldErrors?.[field.key]}
                              className="nutrition-plan-field plan-history-inline-field"
                            >
                              <input
                                type="number"
                                inputMode={INTEGER_PLAN_FIELDS.includes(field.key) ? 'numeric' : 'decimal'}
                                min="0"
                                step={INTEGER_PLAN_FIELDS.includes(field.key) ? '1' : '0.1'}
                                value={historyEditForm[field.key]}
                                onChange={(event) => handleHistoryEditFieldChange(field.key, event.target.value)}
                                required={INTEGER_PLAN_FIELDS.includes(field.key)}
                              />
                            </FormField>
                          ))}
                        </div>
                      ) : (
                        <div className="plan-history-values">
                          <div className="card-value">Carbs: {plan.carbs ?? '—'} g</div>
                          <div className="card-value">Proteins: {plan.proteins ?? '—'} g</div>
                          <div className="card-value">Fats: {plan.fats ?? '—'} g</div>
                          <div className="card-value">
                            Water:{' '}
                            {plan.water != null
                              ? `${(Number(plan.water) / WATER_GRAMS_PER_LITER).toFixed(2)} L`
                              : '—'}
                          </div>
                          <div className="card-value">Salt: {plan.salt ?? '—'} g</div>
                          <div className="card-value">Fiber: {plan.fiber ?? '—'} g</div>
                        </div>
                      )}
                    </div>
                    {historyEditingPlanId === plan.id ? (
                      <div className="plan-history-actions">
                        <Button
                          variant="primary"
                          className="plan-history-edit-button plan-history-action-button plan-history-save-button"
                          loading={historySaving}
                          loadingText="Saving..."
                          onClick={() => handleSaveHistoryEdit(plan.id)}
                        >
                          <FloppyDisk size={22} weight="bold" />
                          Save
                        </Button>
                        <Button
                          variant="ghost"
                          className="plan-history-edit-button plan-history-action-button"
                          disabled={historySaving}
                          onClick={handleCancelHistoryEdit}
                        >
                          <X size={22} weight="bold" />
                          Cancel
                        </Button>
                      </div>
                    ) : (
                      <Button
                        variant="ghost"
                        className="plan-history-edit-button"
                        onClick={() => handleEditPlan(plan)}
                      >
                        <PencilSimple size={22} weight="bold" />
                        Edit
                      </Button>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      ) : null}
    </section>
  )
}

export default CoachClientNutritionPlans
