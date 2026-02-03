import { useState } from 'react'
import { Link } from 'react-router-dom'
import { register } from '../api.js'
import { formatErrorMessage, getFieldErrors } from '../errorUtils.js'
import { isValidPassword, PASSWORD_PATTERN_STRING, PASSWORD_REQUIREMENTS } from '../passwordUtils.js'

function Register() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setStatus('')
    setFieldErrors(null)
    setLoading(true)
    try {
      if (!isValidPassword(password)) {
        setError(PASSWORD_REQUIREMENTS)
        setLoading(false)
        return
      }
      await register({ email, password, role: 'COACH' })
      setStatus('Coach account created. Check your email to activate it.')
    } catch (err) {
      setFieldErrors(getFieldErrors(err))
      setError(formatErrorMessage(err) || 'Registration failed.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="panel panel-narrow">
      <h1>Create Your Account</h1>
      <p className="muted">Create a coach account and set up your login.</p>
      <form className="form" onSubmit={handleSubmit} autoComplete="off">
        <label className="field">
          <input
            type="email"
            name="email"
            id="register-email"
            autoComplete="off"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="Email"
            required
          />
          {fieldErrors?.email ? <p className="field-error">{fieldErrors.email}</p> : null}
        </label>
        <label className="field">
          <input
            type="password"
            name="password"
            id="register-password"
            autoComplete="off"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="Password"
            minLength={8}
            maxLength={32}
            pattern={PASSWORD_PATTERN_STRING}
            title={PASSWORD_REQUIREMENTS}
            onInvalid={(event) => event.target.setCustomValidity(PASSWORD_REQUIREMENTS)}
            onInput={(event) => event.target.setCustomValidity('')}
            required
          />
          {fieldErrors?.password ? <p className="field-error">{fieldErrors.password}</p> : null}
        </label>
        <button type="submit" disabled={loading}>
          {loading ? 'Creating account...' : 'Register'}
        </button>
        {error ? <p className="error">{formatErrorMessage(error)}</p> : null}
        {status ? <p className="success">{status}</p> : null}
      </form>
      <p className="hint">
        Already active? <Link to="/login">Sign in</Link>
      </p>
    </section>
  )
}

export default Register
