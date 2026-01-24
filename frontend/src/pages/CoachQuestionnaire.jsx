import { useEffect, useState } from 'react'
import { addCoachQuestion, deleteCoachQuestion, getCoachQuestions } from '../api.js'

function CoachQuestionnaire() {
  const [questions, setQuestions] = useState([])
  const [text, setText] = useState('')
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let mounted = true

    async function load() {
      setLoading(true)
      setError('')
      try {
        const list = await getCoachQuestions()
        if (mounted) {
          setQuestions(list || [])
        }
      } catch (err) {
        if (mounted) {
          setError(err.message || 'Failed to load questions.')
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

  async function handleAdd(event) {
    event.preventDefault()
    setStatus('')
    setError('')
    setSaving(true)
    try {
      const created = await addCoachQuestion({ text })
      setQuestions((prev) => [created, ...prev])
      setText('')
      setStatus('Question added.')
    } catch (err) {
      setError(err.message || 'Failed to add question.')
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(questionId) {
    const ok = window.confirm('Delete this question?')
    if (!ok) return
    try {
      await deleteCoachQuestion(questionId)
      setQuestions((prev) => prev.filter((item) => item.id !== questionId))
    } catch (err) {
      setError(err.message || 'Failed to delete question.')
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
          <form className="form" onSubmit={handleAdd}>
            <label className="field">
              <span>Prompt</span>
              <input
                type="text"
                value={text}
                onChange={(event) => setText(event.target.value)}
                placeholder="Ex: How many meals do you eat daily?"
                required
              />
            </label>
            <button type="submit" disabled={saving}>
              {saving ? 'Saving...' : 'Add question'}
            </button>
            {status ? <p className="success">{status}</p> : null}
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
