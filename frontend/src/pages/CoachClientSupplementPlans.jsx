import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { CaretRight, FloppyDisk, PencilSimple, Plus, Trash, X } from 'phosphor-react'
import {
  addCoachClientSupplementPlan,
  deleteCoachClientSupplementPlan,
  getCoachClient,
  getCoachClientSupplementPlans,
  updateCoachClientSupplementPlan,
} from '../api.js'
import { useLoadData } from '../hooks/useLoadData.js'
import { useFormState } from '../hooks/useFormState.js'
import FormField from '../components/FormField.jsx'
import StatusMessage from '../components/StatusMessage.jsx'
import Button from '../components/Button.jsx'

const EMPTY_ENTRY = { supplementName: '', brand: '', dosage: '', timing: '', notes: '' }

function newEntry() {
  return { ...EMPTY_ENTRY }
}

function CoachClientSupplementPlans() {
  const { clientId } = useParams()
  const [client, setClient] = useState(null)
  const [plans, setPlans] = useState([])
  const [entries, setEntries] = useState([newEntry()])
  const [historyEditingPlanId, setHistoryEditingPlanId] = useState(null)
  const [historyEditEntries, setHistoryEditEntries] = useState([])
  const [historySaving, setHistorySaving] = useState(false)
  const [historyDeleting, setHistoryDeleting] = useState(null)

  const form = useFormState()

  const { loading, error: loadError } = useLoadData(
    async () => {
      const clientData = await getCoachClient(clientId)
      setClient(clientData)

      try {
        const list = await getCoachClientSupplementPlans(clientId)
        setPlans(list || [])
      } catch {
        // plan history unavailable — form still works
      }
    },
    [clientId],
  )

  function handleEntryChange(index, field, value) {
    setEntries((prev) => prev.map((e, i) => (i === index ? { ...e, [field]: value } : e)))
  }

  function handleAddEntry() {
    setEntries((prev) => [...prev, newEntry()])
  }

  function handleRemoveEntry(index) {
    setEntries((prev) => (prev.length > 1 ? prev.filter((_, i) => i !== index) : prev))
  }

  async function handlePlanSubmit(event) {
    event.preventDefault()
    form.startSaving()

    const hasInvalidEntry = entries.some(
      (e) => !e.supplementName.trim() || !e.dosage.trim(),
    )
    if (hasInvalidEntry) {
      form.setErrorManual('Each supplement entry requires a name and dosage.')
      return
    }

    const payload = {
      entries: entries.map((e) => ({
        supplementName: e.supplementName.trim(),
        brand: e.brand.trim() || null,
        dosage: e.dosage.trim(),
        timing: e.timing.trim() || null,
        notes: e.notes.trim() || null,
      })),
    }

    try {
      await addCoachClientSupplementPlan(clientId, payload)
      form.setSuccess('Plan created.')
      const list = await getCoachClientSupplementPlans(clientId)
      setPlans(list || [])
      setEntries([newEntry()])
    } catch (err) {
      form.setFailure(err)
    }
  }

  function handleEditPlan(plan) {
    if (!plan.id) {
      form.setErrorManual('This plan cannot be edited because its identifier is missing.')
      return
    }
    setHistoryEditingPlanId(plan.id)
    setHistoryEditEntries(
      (plan.entries || []).map((e) => ({
        supplementName: e.supplementName ?? '',
        brand: e.brand ?? '',
        dosage: e.dosage ?? '',
        timing: e.timing ?? '',
        notes: e.notes ?? '',
      })),
    )
    form.reset()
  }

  function handleHistoryEntryChange(index, field, value) {
    setHistoryEditEntries((prev) =>
      prev.map((e, i) => (i === index ? { ...e, [field]: value } : e)),
    )
  }

  function handleHistoryAddEntry() {
    setHistoryEditEntries((prev) => [...prev, newEntry()])
  }

  function handleHistoryRemoveEntry(index) {
    setHistoryEditEntries((prev) =>
      prev.length > 1 ? prev.filter((_, i) => i !== index) : prev,
    )
  }

  function handleCancelHistoryEdit() {
    setHistoryEditingPlanId(null)
    setHistoryEditEntries([])
  }

  async function handleSaveHistoryEdit(planId) {
    form.reset()

    const hasInvalidEntry = historyEditEntries.some(
      (e) => !e.supplementName.trim() || !e.dosage.trim(),
    )
    if (hasInvalidEntry) {
      form.setErrorManual('Each supplement entry requires a name and dosage.')
      return
    }

    const payload = {
      entries: historyEditEntries.map((e) => ({
        supplementName: e.supplementName.trim(),
        brand: e.brand.trim() || null,
        dosage: e.dosage.trim(),
        timing: e.timing.trim() || null,
        notes: e.notes.trim() || null,
      })),
    }

    setHistorySaving(true)
    try {
      await updateCoachClientSupplementPlan(clientId, planId, payload)
      const list = await getCoachClientSupplementPlans(clientId)
      setPlans(list || [])
      setHistoryEditingPlanId(null)
      setHistoryEditEntries([])
      form.setSuccess('Plan updated.')
    } catch (err) {
      form.setFailure(err)
    } finally {
      setHistorySaving(false)
    }
  }

  async function handleDeletePlan(planId) {
    form.reset()
    setHistoryDeleting(planId)
    try {
      await deleteCoachClientSupplementPlan(clientId, planId)
      const list = await getCoachClientSupplementPlans(clientId)
      setPlans(list || [])
      form.setSuccess('Plan deleted.')
    } catch (err) {
      form.setFailure(err)
    } finally {
      setHistoryDeleting(null)
    }
  }

  const clientName =
    `${client?.firstName || ''} ${client?.lastName || ''}`.trim() || 'Client'

  return (
    <section className="panel">
      <div className="panel-header client-detail-header">
        <div>
          <h1>{clientName}</h1>
          <p className="muted">Submitting creates a new plan and makes it active.</p>
        </div>
        <Link className="back-button" to={`/coach/clients/${clientId}`}>
          <span className="button-label">Back</span>
          <CaretRight size={22} weight="bold" />
        </Link>
      </div>
      {loading ? <p className="muted">Loading supplement plans...</p> : null}
      {loadError ? <p className="error">{loadError}</p> : null}
      {!loading && client ? (
        <div className="split-grid client-detail-plan-stack">

          {/* ── Create form ── */}
          <div className="card">
            <div className="card-title">New supplement plan</div>
            <form className="supplement-plan-form" onSubmit={handlePlanSubmit} autoComplete="off">
              {entries.map((entry, index) => (
                <div key={index} className="supplement-entry-row">
                  <div className="supplement-entry-fields">
                    <FormField label="Name *">
                      <input
                        type="text"
                        value={entry.supplementName}
                        onChange={(e) => handleEntryChange(index, 'supplementName', e.target.value)}
                        placeholder="e.g. Creatine"
                        required
                      />
                    </FormField>
                    <FormField label="Brand">
                      <input
                        type="text"
                        value={entry.brand}
                        onChange={(e) => handleEntryChange(index, 'brand', e.target.value)}
                        placeholder="e.g. Optimum Nutrition"
                      />
                    </FormField>
                    <FormField label="Dosage *">
                      <input
                        type="text"
                        value={entry.dosage}
                        onChange={(e) => handleEntryChange(index, 'dosage', e.target.value)}
                        placeholder="e.g. 5g"
                        required
                      />
                    </FormField>
                    <FormField label="Timing">
                      <input
                        type="text"
                        value={entry.timing}
                        onChange={(e) => handleEntryChange(index, 'timing', e.target.value)}
                        placeholder="e.g. Pre-workout"
                      />
                    </FormField>
                    <FormField label="Notes" className="supplement-entry-notes-field">
                      <textarea
                        value={entry.notes}
                        onChange={(e) => handleEntryChange(index, 'notes', e.target.value)}
                        placeholder="Additional instructions..."
                        rows={2}
                      />
                    </FormField>
                  </div>
                  {entries.length > 1 ? (
                    <Button
                      type="button"
                      variant="ghost"
                      className="supplement-remove-button"
                      onClick={() => handleRemoveEntry(index)}
                      aria-label="Remove entry"
                    >
                      <X size={18} weight="bold" />
                    </Button>
                  ) : null}
                </div>
              ))}
              <div className="supplement-form-actions">
                <Button
                  type="button"
                  variant="ghost"
                  className="supplement-add-entry-button"
                  onClick={handleAddEntry}
                >
                  <Plus size={18} weight="bold" />
                  Add supplement
                </Button>
                <Button
                  type="submit"
                  className="nutrition-plan-submit"
                  loading={form.saving}
                  loadingText="Saving..."
                >
                  <Plus size={22} weight="bold" />
                  Create plan
                </Button>
              </div>
              <StatusMessage status={form.status} error={form.error} />
            </form>
          </div>

          {/* ── History ── */}
          <div className="card">
            <div className="card-title">Supplement plan history</div>
            {plans.length === 0 ? (
              <p className="muted">No plans yet.</p>
            ) : (
              <ul className="card-list plan-history-list">
                {plans.map((plan, planIndex) => {
                  const isEditing = historyEditingPlanId === plan.id
                  const entryCount = plan.entries?.length ?? 0

                  return (
                    <li
                      key={plan.id ?? plan.createdAt}
                      className={`list-item plan-history-item${isEditing ? ' is-editing' : ''}`}
                    >
                      <div>
                        <div className="plan-history-header">
                          <div className="card-title">
                            {entryCount} supplement{entryCount !== 1 ? 's' : ''}
                            {planIndex === 0 ? (
                              <span className="active-badge">Active</span>
                            ) : null}
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

                        {isEditing ? (
                          <div className="supplement-edit-entries">
                            {historyEditEntries.map((entry, index) => (
                              <div key={index} className="supplement-entry-row">
                                <div className="supplement-entry-fields">
                                  <FormField label="Name *">
                                    <input
                                      type="text"
                                      value={entry.supplementName}
                                      onChange={(e) =>
                                        handleHistoryEntryChange(index, 'supplementName', e.target.value)
                                      }
                                      required
                                    />
                                  </FormField>
                                  <FormField label="Brand">
                                    <input
                                      type="text"
                                      value={entry.brand}
                                      onChange={(e) =>
                                        handleHistoryEntryChange(index, 'brand', e.target.value)
                                      }
                                    />
                                  </FormField>
                                  <FormField label="Dosage *">
                                    <input
                                      type="text"
                                      value={entry.dosage}
                                      onChange={(e) =>
                                        handleHistoryEntryChange(index, 'dosage', e.target.value)
                                      }
                                      required
                                    />
                                  </FormField>
                                  <FormField label="Timing">
                                    <input
                                      type="text"
                                      value={entry.timing}
                                      onChange={(e) =>
                                        handleHistoryEntryChange(index, 'timing', e.target.value)
                                      }
                                    />
                                  </FormField>
                                  <FormField label="Notes" className="supplement-entry-notes-field">
                                    <textarea
                                      value={entry.notes}
                                      onChange={(e) =>
                                        handleHistoryEntryChange(index, 'notes', e.target.value)
                                      }
                                      rows={2}
                                    />
                                  </FormField>
                                </div>
                                {historyEditEntries.length > 1 ? (
                                  <Button
                                    type="button"
                                    variant="ghost"
                                    className="supplement-remove-button"
                                    onClick={() => handleHistoryRemoveEntry(index)}
                                    aria-label="Remove entry"
                                  >
                                    <X size={18} weight="bold" />
                                  </Button>
                                ) : null}
                              </div>
                            ))}
                            <div className="supplement-form-actions">
                              <Button
                                type="button"
                                variant="ghost"
                                className="supplement-add-entry-button"
                                onClick={handleHistoryAddEntry}
                              >
                                <Plus size={18} weight="bold" />
                                Add supplement
                              </Button>
                            </div>
                          </div>
                        ) : (
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
                        )}
                      </div>

                      {isEditing ? (
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
                        <div className="plan-history-actions">
                          <Button
                            variant="ghost"
                            className="plan-history-edit-button"
                            onClick={() => handleEditPlan(plan)}
                          >
                            <PencilSimple size={22} weight="bold" />
                            Edit
                          </Button>
                          <Button
                            variant="ghost"
                            className="plan-history-edit-button plan-history-action-button"
                            loading={historyDeleting === plan.id}
                            loadingText="Deleting..."
                            onClick={() => handleDeletePlan(plan.id)}
                          >
                            <Trash size={22} weight="bold" />
                            Delete
                          </Button>
                        </div>
                      )}
                    </li>
                  )
                })}
              </ul>
            )}
          </div>

        </div>
      ) : null}
    </section>
  )
}

export default CoachClientSupplementPlans
