import { Link } from 'react-router-dom'
import { useState } from 'react'
import {
  getChats,
  getClient,
  getClientActivePlan,
  getClientQuestionnaire,
} from '../api.js'
import { useLoadData } from '../hooks/useLoadData.js'
import PagePanel from '../components/PagePanel.jsx'

function ClientDashboard() {
  const [client, setClient] = useState(null)
  const [plan, setPlan] = useState(null)
  const [questions, setQuestions] = useState([])
  const [chats, setChats] = useState([])

  const { loading, error } = useLoadData(async () => {
    const [clientData, planData, questionList, chatList] = await Promise.all([
      getClient(),
      getClientActivePlan().catch((err) => (err.status === 404 ? null : Promise.reject(err))),
      getClientQuestionnaire(),
      getChats(),
    ])
    setClient(clientData)
    setPlan(planData)
    setQuestions(questionList || [])
    setChats(chatList || [])
  }, [])

  return (
    <PagePanel
      title="Client dashboard"
      subtitle="Your coaching plan, questions, and chat updates."
      loading={loading}
      error={error}
      headerAction={
        <Link className="primary-link" to="/client/nutrition-plans">
          View plans
        </Link>
      }
    >
      <div className="stat-grid">
        <div className="stat">
          <div className="label">Client</div>
          <div className="value">
            {client?.firstName || 'Client'} {client?.lastName || ''}
          </div>
        </div>
        <div className="stat">
          <div className="label">Active plan calories</div>
          <div className="value">{plan?.calories ?? '—'}</div>
        </div>
        <div className="stat">
          <div className="label">Open chats</div>
          <div className="value">{chats.length}</div>
        </div>
      </div>
      <div className="list">
        <div className="section-title">Next questionnaire items</div>
        {questions.length === 0 ? (
          <p className="muted">No questions yet.</p>
        ) : (
          <ul className="chip-list">
            {questions.slice(0, 6).map((question) => (
              <li key={question.id} className="pill">
                {question.text}
              </li>
            ))}
          </ul>
        )}
      </div>
    </PagePanel>
  )
}

export default ClientDashboard
