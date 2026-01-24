import { useState } from 'react'
import { Link } from 'react-router-dom'
import { forgotPassword } from '../api.js'

function ForgotPassword() {
  const [email, setEmail] = useState('')
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setStatus('')
    setError('')
    setLoading(true)
    try {
      await forgotPassword(email)
      setStatus('Reset link sent. Check your email for next steps.')
    } catch (err) {
      setError(err.message || 'Failed to send reset link.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="panel panel-narrow">
      <h1>Reset Your Password</h1>
      <p className="muted">Enter your email to receive a reset link.</p>
      <form className="form" onSubmit={handleSubmit}>
        <label className="field">
          <input
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="Email"
            required
          />
        </label>
        <button type="submit" disabled={loading}>
          {loading ? 'Sending...' : 'Send reset link'}
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

export default ForgotPassword
