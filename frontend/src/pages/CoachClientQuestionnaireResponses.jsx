import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { CaretRight } from 'phosphor-react'
import { getCoachClient, getCoachClientAnswers } from '../api.js'
import { useLoadData } from '../hooks/useLoadData.js'
import PagePanel from '../components/PagePanel.jsx'

function CoachClientQuestionnaireResponses() {
  const { clientId } = useParams()
  const [client, setClient] = useState(null)
  const [answers, setAnswers] = useState([])

  const { loading, error } = useLoadData(async () => {
    const [clientData, questionnaire] = await Promise.all([
      getCoachClient(clientId),
      getCoachClientAnswers(clientId),
    ])
    setClient(clientData)
    setAnswers(questionnaire || [])
  }, [clientId])

  const clientName = `${client?.firstName || ''} ${client?.lastName || ''}`.trim() || 'Client'

  return (
    <PagePanel
      title={clientName}
      subtitle="View questionnaire answers"
      loading={loading}
      error={error}
      actions={
        <Link className="back-button" to={`/coach/clients/${clientId}`}>
          <span className="button-label">Back</span>
          <CaretRight size={22} weight="bold" />
        </Link>
      }
    >
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
    </PagePanel>
  )
}

export default CoachClientQuestionnaireResponses
