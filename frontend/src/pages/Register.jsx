import { useState } from 'react'
import { Link } from 'react-router-dom'
import { register } from '../api.js'

const ROLE_OPTIONS = [
  { value: 'COACH', label: 'Coach' },
  { value: 'CLIENT', label: 'Client' },
]

function Register() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState('COACH')
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setStatus('')
    setLoading(true)
    try {
      await register({ email, password, role })
      setStatus('Account created. Check your email to activate it.')
    } catch (err) {
      setError(err.message || 'Registration failed.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="panel panel-narrow">
      <h1>Create Your Account</h1>
      <p className="muted">Choose your role and set up your login.</p>
      <form className="form" onSubmit={handleSubmit}>
        <div className="register-row">
          <div className="register-fields">
            <label className="field">
              <input
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="Email"
                required
              />
            </label>
            <label className="field">
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="Password"
                autoComplete="new-password"
                required
              />
            </label>
          </div>
          <div className="role-switch role-switch-vertical" role="group" aria-label="Select role">
            {ROLE_OPTIONS.map((option) => (
              <button
                key={option.value}
                type="button"
                className={`role-option${role === option.value ? ' active' : ''}`}
                onClick={() => setRole(option.value)}
              >
                {option.label}
              </button>
            ))}
          </div>
        </div>
        <button type="submit" disabled={loading}>
          {loading ? 'Creating account...' : 'Register'}
        </button>
        {error ? <p className="error">{error}</p> : null}
        {status ? <p className="success">{status}</p> : null}
      </form>
      <p className="hint">
        Already active? <Link to="/login">Sign in</Link>
      </p>
    </section>
  )
}

export default Register
