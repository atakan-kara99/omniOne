import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getProfile, getUser, login, refreshAuth, refreshCsrf, setLoggingOut } from '../api.js'
import { getToken, setToken } from '../auth.js'
import { useAuth } from '../authContext.js'
import { isValidPassword, PASSWORD_PATTERN_STRING, PASSWORD_REQUIREMENTS } from '../passwordUtils.js'

function Login() {
  const navigate = useNavigate()
  const { setUser } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [status, setStatus] = useState('')
  const [loading, setLoading] = useState(false)
  const refreshAttemptedRef = useRef(false)

  useEffect(() => {
    const deleted = sessionStorage.getItem('omniOne.accountDeleted')
    if (deleted) {
      setStatus('Your account was deleted successfully.')
      sessionStorage.removeItem('omniOne.accountDeleted')
    }
  }, [])

  useEffect(() => {
    if (refreshAttemptedRef.current) return
    refreshAttemptedRef.current = true
    if (getToken()) return
    if (sessionStorage.getItem('omniOne.loggedOut')) {
      sessionStorage.removeItem('omniOne.loggedOut')
      return
    }
    ;(async () => {
      try {
        const refreshed = await refreshAuth()
        if (refreshed?.jwt) {
          setToken(refreshed.jwt)
          setLoggingOut(false)
          await refreshCsrf()
          const user = await getUser()
          let profile = null
          try {
            profile = await getProfile()
          } catch {
            profile = null
          }
          const mergedUser = profile ? { ...user, ...profile } : user
          setUser(mergedUser)
          if (mergedUser.role === 'COACH') {
            navigate('/coach', { replace: true })
          } else if (mergedUser.role === 'CLIENT') {
            navigate('/client', { replace: true })
          } else {
            setError('This UI only supports Coach and Client roles.')
          }
        }
      } catch {
        // no refresh cookie or refresh failed
      }
    })()
  }, [navigate, setUser])

  async function runLogin(nextEmail, nextPassword) {
    setError('')
    setStatus('')
    setLoading(true)

    try {
      if (!isValidPassword(nextPassword)) {
        setError(PASSWORD_REQUIREMENTS)
        setLoading(false)
        return
      }
      const response = await login({ email: nextEmail, password: nextPassword })
      setLoggingOut(false)
      if (response?.jwt) {
        setToken(response.jwt)
      }
      await refreshCsrf()
      const user = await getUser()
      let profile = null
      try {
        profile = await getProfile()
      } catch {
        profile = null
      }
      const mergedUser = profile ? { ...user, ...profile } : user
      setUser(mergedUser)

      if (mergedUser.role === 'COACH') {
        navigate('/coach')
      } else if (mergedUser.role === 'CLIENT') {
        navigate('/client')
      } else {
        setError('This UI only supports Coach and Client roles.')
      }
    } catch (err) {
      const detail = err?.payload?.detail
      const payloadText = typeof err?.payload === 'string' ? err.payload : ''
      if (
        err?.status === 403 &&
        (detail?.includes('User Account Disabled') || payloadText.includes('User Account Disabled'))
      ) {
        setError('Your account is not activated yet. Check your email for the activation link.')
      } else if (
        err?.status === 403 &&
        (detail?.includes('Der Benutzer') || payloadText.includes('Der Benutzer'))
      ) {
        setError('Your account is disabled. Please contact support.')
      } else {
        setError(err.message || 'Login failed.')
      }
    } finally {
      setLoading(false)
    }
  }

  async function handleSubmit(event) {
    event.preventDefault()
    await runLogin(email, password)
  }

  async function handleQuickLogin(nextUsername, nextPassword) {
    setEmail(nextUsername)
    setPassword(nextPassword)
    await runLogin(nextUsername, nextPassword)
  }

  return (
    <>
      <section className="panel hero">
        <div className="hero-body">
          <h1>Welcome Back!</h1>
          <p className="muted">Sign in to continue your coaching.</p>
          <form className="form" onSubmit={handleSubmit}>
            <label className="field">
              <input
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="Email"
                autoComplete="email"
                required
              />
            </label>
            <label className="field">
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="Password"
                autoComplete="current-password"
                minLength={8}
                maxLength={32}
                pattern={PASSWORD_PATTERN_STRING}
                title={PASSWORD_REQUIREMENTS}
                onInvalid={(event) => event.target.setCustomValidity(PASSWORD_REQUIREMENTS)}
                onInput={(event) => event.target.setCustomValidity('')}
                required
              />
            </label>
            <button type="submit" disabled={loading}>
              {loading ? 'Signing in...' : 'Sign in'}
            </button>
            {error ? <p className="error">{error}</p> : null}
            {status ? <p className="success">{status}</p> : null}
          </form>
          <div className="hint-row">
            <span className="hint">New here? <Link to="/register">Create an account</Link></span>
            <span className="hint right-link"><Link to="/forgot">Forgot password?</Link></span>
          </div>
        </div>
      </section>
      {import.meta.env.DEV ? (
        <div className="dev-login">
          <button
            type="button"
            className="dev-button"
            onClick={() => handleQuickLogin('coach-10@omni.one', 'Testpq12')}
            disabled={loading}
          >
            coach-10@omni.one
          </button>
          <button
            type="button"
            className="dev-button"
            onClick={() => handleQuickLogin('client-100@omni.one', 'Testpq12')}
            disabled={loading}
          >
            client-100@omni.one
          </button>
        </div>
      ) : null}
    </>
  )
}

export default Login
