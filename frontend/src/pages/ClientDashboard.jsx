import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  getChats,
  getClient,
  getClientActivePlan,
  getClientQuestionnaire,
} from '../api.js'

function ClientDashboard() {
  const [client, setClient] = useState(null)
  const [plan, setPlan] = useState(null)
  const [questions, setQuestions] = useState([])
  const [chats, setChats] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let mounted = true

    async function load() {
      setLoading(true)
      setError('')
      try {
        const [clientData, planData, questionList, chatList] = await Promise.all([
          getClient(),
          getClientActivePlan().catch((err) => (err.status === 404 ? null : Promise.reject(err))),
          getClientQuestionnaire(),
          getChats(),
        ])
        if (mounted) {
          setClient(clientData)
          setPlan(planData)
          setQuestions(questionList || [])
          setChats(chatList || [])
        }
      } catch (err) {
        if (mounted) {
          setError(err.message || 'Failed to load client data.')
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
          <h1>Client dashboard</h1>
          <p className="muted">Your coaching plan, questions, and chat updates.</p>
        </div>
        <Link className="primary-link" to="/client/nutrition-plans">
          View plans
        </Link>
      </div>
      {loading ? <p className="muted">Loading client data...</p> : null}
      {error ? <p className="error">{error}</p> : null}
      {!loading && !error ? (
        <>
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
        </>
      ) : null}
    </section>
  )
}

export default ClientDashboard
