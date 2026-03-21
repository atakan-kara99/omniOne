import { useState } from 'react'
import { Link } from 'react-router-dom'
import { getCoach, getCoachClients } from '../api.js'
import { useLoadData } from '../hooks/useLoadData.js'
import PagePanel from '../components/PagePanel.jsx'

function CoachDashboard() {
  const [coach, setCoach] = useState(null)
  const [clients, setClients] = useState([])

  const { loading, error } = useLoadData(async () => {
    const [coachData, clientList] = await Promise.all([getCoach(), getCoachClients()])
    setCoach(coachData)
    setClients(clientList || [])
  }, [])

  return (
    <PagePanel
      title="Coach dashboard"
      subtitle="Keep tabs on client progress and upcoming touchpoints."
      loading={loading}
      error={error}
      actions={
        <Link className="primary-link" to="/coach/clients">
          View clients
        </Link>
      }
    >
      <div className="stat-grid">
        <div className="stat">
          <div className="label">Coach ID</div>
          <div className="value">{coach?.id || '—'}</div>
        </div>
        <div className="stat">
          <div className="label">Active clients</div>
          <div className="value">{clients.length}</div>
        </div>
      </div>
    </PagePanel>
  )
}

export default CoachDashboard
