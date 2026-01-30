import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { activateAccount } from '../api.js'

function ActivateAccount() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const navigate = useNavigate()
  const [status, setStatus] = useState('Activating your account...')
  const [error, setError] = useState('')

  useEffect(() => {
    let mounted = true
    async function run() {
      if (!token) {
        setError('Activation token missing.')
        setStatus('')
        return
      }
      try {
        await activateAccount(token)
        if (mounted) {
          setStatus('Account activated. You can now sign in.')
          setTimeout(() => navigate('/login'), 1200)
        }
      } catch (err) {
        if (mounted) {
          setError(err.message || 'Activation failed.')
          setStatus('')
        }
      }
    }

    run()
    return () => {
      mounted = false
    }
  }, [navigate, token])

  return (
    <section className="panel panel-narrow">
      <h1>Activate Account</h1>
      <p className="muted">We are verifying your activation link.</p>
      {status ? <p className="success">{status}</p> : null}
      {error ? <p className="error">{error}</p> : null}
      <p className="hint">
        <Link to="/login">Return to sign in</Link>
      </p>
    </section>
  )
}

export default ActivateAccount
