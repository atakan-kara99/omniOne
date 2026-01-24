import { useEffect, useState } from 'react'
import {
  getClientAnswers,
  getClientQuestionnaire,
  updateClientAnswers,
} from '../api.js'

function ClientQuestionnaire() {
  const [questions, setQuestions] = useState([])
  const [answers, setAnswers] = useState({})
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
        const [questionList, answerList] = await Promise.all([
          getClientQuestionnaire(),
          getClientAnswers().catch((err) => (err.status === 404 ? [] : Promise.reject(err))),
        ])
        if (mounted) {
          setQuestions(questionList || [])
          const answerMap = {}
          ;(answerList || []).forEach((item) => {
            answerMap[item.questionId] = item.answerText || ''
          })
          setAnswers(answerMap)
        }
      } catch (err) {
        if (mounted) {
          setError(err.message || 'Failed to load questionnaire.')
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

  async function handleSubmit(event) {
    event.preventDefault()
    setStatus('')
    setError('')
    setSaving(true)
    try {
      const payload = questions.map((question) => ({
        questionId: question.id,
        answer: answers[question.id] || '',
      }))
      await updateClientAnswers(payload)
      setStatus('Answers saved.')
    } catch (err) {
      setError(err.message || 'Failed to save answers.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <h1>Your questionnaire</h1>
          <p className="muted">Update your intake answers to keep your plan precise.</p>
        </div>
      </div>
      {loading ? <p className="muted">Loading questionnaire...</p> : null}
      {error ? <p className="error">{error}</p> : null}
      {!loading && !error ? (
        questions.length === 0 ? (
          <p className="muted">No questions available yet.</p>
        ) : (
          <form className="form" onSubmit={handleSubmit}>
            {questions.map((question) => (
              <label key={question.id} className="field">
                <span>{question.text}</span>
                <input
                  type="text"
                  value={answers[question.id] || ''}
                  onChange={(event) =>
                    setAnswers((prev) => ({
                      ...prev,
                      [question.id]: event.target.value,
                    }))
                  }
                />
              </label>
            ))}
            <button type="submit" disabled={saving}>
              {saving ? 'Saving...' : 'Save answers'}
            </button>
            {status ? <p className="success">{status}</p> : null}
          </form>
        )
      ) : null}
    </section>
  )
}

export default ClientQuestionnaire
