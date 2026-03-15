import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { CaretRight } from 'phosphor-react'
import { getCoachClient, getCoachClientAnswers } from '../api.js'
import { formatErrorMessage } from '../errorUtils.js'

function CoachClientQuestionnaireResponses() {
  const { clientId } = useParams()
  const [client, setClient] = useState(null)
  const [answers, setAnswers] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let mounted = true

    async function load() {
      setLoading(true)
      setError('')
      try {
        const [clientData, questionnaire] = await Promise.all([
          getCoachClient(clientId),
          getCoachClientAnswers(clientId),
        ])
        if (mounted) {
          setClient(clientData)
          setAnswers(questionnaire || [])
        }
      } catch (err) {
        if (mounted) {
          setError(err || 'Failed to load questionnaire responses.')
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

  const clientName = `${client?.firstName || ''} ${client?.lastName || ''}`.trim() || 'Client'

  return (
    <section className="panel">
      <div className="panel-header client-detail-header">
        <div>
          <h1>{clientName}</h1>
          <p className="muted">View questionnaire answers</p>
        </div>
        <Link className="back-button" to={`/coach/clients/${clientId}`}>
          <span className="button-label">Back</span>
          <CaretRight size={22} weight="bold" />
        </Link>
      </div>
      {loading ? <p className="muted">Loading responses...</p> : null}
      {error ? <p className="error">{formatErrorMessage(error)}</p> : null}
      {!loading && !error ? (
        <div className="card">
          <div className="card-title">All responses</div>
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
      ) : null}
    </section>
  )
}

export default CoachClientQuestionnaireResponses
