import { useNavigate, useSearchParams } from 'react-router-dom'
import { activateAccount } from '../api.js'
import { useLoadData } from '../hooks/useLoadData.js'
import { formatErrorMessage } from '../errorUtils.js'
import StatusMessage from '../components/StatusMessage.jsx'
import { Link } from 'react-router-dom'

function ActivateAccount() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const navigate = useNavigate()

  const { loading, error } = useLoadData(async () => {
    if (!token) {
      throw new Error('Activation token missing.')
    }
    await activateAccount(token)
  }, [token])

  if (!loading && !error) {
    setTimeout(() => navigate('/login'), 1200)
  }

  const status = loading ? 'Activating your account...' : !error ? 'Account activated. You can now sign in.' : ''

  return (
    <section className="panel panel-narrow">
      <h1>Activate Account</h1>
      <p className="muted">We are verifying your activation link.</p>
      <StatusMessage status={status} error={error} />
      <p className="hint">
        <Link to="/login">Return to sign in</Link>
      </p>
    </section>
  )
}

export default ActivateAccount
