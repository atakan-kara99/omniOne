// REFERENCE PAGE: New pages should follow this structure.
// Pattern: useLoadData for fetching, useFormState for forms, FormField/Button/StatusMessage for UI.
import { useState } from 'react'
import { addCoachQuestion, deleteCoachQuestion, getCoachQuestions } from '../api.js'
import { getFieldErrors } from '../errorUtils.js'
import { useLoadData } from '../hooks/useLoadData.js'
import { useFormState } from '../hooks/useFormState.js'
import FormField from '../components/FormField.jsx'
import StatusMessage from '../components/StatusMessage.jsx'
import Button from '../components/Button.jsx'

function CoachQuestionnaire() {
  const [questions, setQuestions] = useState([])
  const [text, setText] = useState('')

  const { loading, error } = useLoadData(async () => {
    const list = await getCoachQuestions()
    setQuestions(list || [])
  }, [])

  const form = useFormState()

  async function handleAdd(event) {
    event.preventDefault()
    form.startSaving()
    try {
      const created = await addCoachQuestion({ text })
      setQuestions((prev) => [created, ...prev])
      setText('')
      form.setSuccess('Question added.')
    } catch (err) {
      form.setFailure(err, getFieldErrors(err))
    }
  }

  async function handleDelete(questionId) {
    const ok = window.confirm('Delete this question?')
    if (!ok) return
    try {
      await deleteCoachQuestion(questionId)
      setQuestions((prev) => prev.filter((item) => item.id !== questionId))
    } catch (err) {
      form.setError(err || 'Failed to delete question.')
    }
  }

  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <h1>Questionnaire builder</h1>
          <p className="muted">Craft the intake questions your clients see.</p>
        </div>
      </div>
      <div className="split-grid">
        <div className="card">
          <div className="card-title">Add a question</div>
          <form className="form" onSubmit={handleAdd} autoComplete="off">
            <FormField label="Prompt" error={form.fieldErrors?.text}>
              <input
                type="text"
                name="text"
                id="coach-question-text"
                autoComplete="off"
                value={text}
                onChange={(event) => setText(event.target.value)}
                placeholder="Ex: How many meals do you eat daily?"
                required
              />
            </FormField>
            <Button type="submit" loading={form.saving} loadingText="Saving...">
              Add question
            </Button>
            <StatusMessage status={form.status} error={form.error} />
          </form>
        </div>
        <div className="card">
          <div className="card-title">Live questions</div>
          {loading ? <p className="muted">Loading questions...</p> : null}
          {error ? <p className="error">{error}</p> : null}
          {!loading && !error ? (
            questions.length === 0 ? (
              <p className="muted">No questions yet.</p>
            ) : (
              <ul className="card-list">
                {questions.map((question) => (
                  <li key={question.id} className="list-item">
                    <span>{question.text}</span>
                    <button
                      type="button"
                      className="ghost-button"
                      onClick={() => handleDelete(question.id)}
                    >
                      Remove
                    </button>
                  </li>
                ))}
              </ul>
            )
          ) : null}
        </div>
      </div>
    </section>
  )
}

export default CoachQuestionnaire
