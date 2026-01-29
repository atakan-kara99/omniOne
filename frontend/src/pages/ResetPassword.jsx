import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { resetPassword } from '../api.js'

function ResetPassword() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')
  const [password, setPassword] = useState('')
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setStatus('')
    setError('')
    if (!token) {
      setError('Reset token missing.')
      return
    }
    setLoading(true)
    try {
      await resetPassword(token, { password })
      setStatus('Password updated. You can now sign in.')
      setTimeout(() => navigate('/login'), 1000)
    } catch (err) {
      setError(err.message || 'Failed to reset password.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="panel panel-narrow">
      <h1>Create A New Password</h1>
      <p className="muted">Choose a password to secure your account.</p>
      <form className="form" onSubmit={handleSubmit}>
        <label className="field">
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="New password"
            autoComplete="new-password"
            required
          />
        </label>
        <button type="submit" disabled={loading}>
          {loading ? 'Updating...' : 'Reset password'}
        </button>
        {status ? <p className="success">{status}</p> : null}
        {error ? <p className="error">{error}</p> : null}
      </form>
      <p className="hint">
        <Link to="/login">Back to sign in</Link>
      </p>
    </section>
  )
}

export default ResetPassword
