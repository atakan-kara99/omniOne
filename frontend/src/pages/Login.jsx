import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getProfile, getUser, login, refreshAuth, refreshCsrf, setLoggingOut } from '../api.js'
import { getToken, setToken } from '../auth.js'
import { useAuth } from '../authContext.js'
import { useFormState } from '../hooks/useFormState.js'
import { formatErrorMessage, getFieldErrors } from '../errorUtils.js'
import { isValidPassword, PASSWORD_PATTERN_STRING, PASSWORD_REQUIREMENTS } from '../passwordUtils.js'
import FormField from '../components/FormField.jsx'
import StatusMessage from '../components/StatusMessage.jsx'
import Button from '../components/Button.jsx'

function Login() {
  const navigate = useNavigate()
  const { setUser } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const form = useFormState()
  const refreshAttemptedRef = useRef(false)

  useEffect(() => {
    const deleted = sessionStorage.getItem('omniOne.accountDeleted')
    if (deleted) {
      form.setStatus('Your account was deleted successfully.')
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
            form.setError('This UI only supports Coach and Client roles.')
          }
        }
      } catch {
        // no refresh cookie or refresh failed
      }
    })()
  }, [navigate, setUser, form])

  async function runLogin(nextEmail, nextPassword) {
    form.reset()

    if (!isValidPassword(nextPassword)) {
      form.setError(PASSWORD_REQUIREMENTS)
      return
    }

    form.startSaving()

    try {
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
        form.setError('This UI only supports Coach and Client roles.')
      }
    } catch (err) {
      const errorCode = err?.errorCode
      form.setFieldErrors(getFieldErrors(err))
      if (errorCode === 'AUTH_ACCOUNT_DISABLED') {
        form.setError(
          formatErrorMessage({
            detail: 'Your account is not activated yet. Check your email for the activation link.',
            traceId: err?.traceId,
          }),
        )
      } else if (errorCode === 'AUTH_INVALID_CREDENTIALS') {
        form.setError(
          formatErrorMessage({ detail: 'Invalid email or password.', traceId: err?.traceId }),
        )
      } else {
        form.setError(formatErrorMessage(err) || 'Login failed.')
      }
    } finally {
      // Note: form.saving state is managed by useFormState, but we manually
      // stop it here since we're not using form.startSaving/setSuccess/setFailure pattern
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
          <form className="form" onSubmit={handleSubmit} autoComplete="off">
            <FormField label="Email" error={form.fieldErrors?.email}>
              <input
                type="email"
                name="email"
                id="login-email"
                autoComplete="off"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="Email"
                required
              />
            </FormField>
            <FormField label="Password" error={form.fieldErrors?.password}>
              <input
                type="password"
                name="password"
                id="login-password"
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
            </FormField>
            <Button type="submit" loading={form.saving} loadingText="Signing in...">
              Sign in
            </Button>
            <StatusMessage status={form.status} error={form.error} />
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
            disabled={form.saving}
          >
            coach-10@omni.one
          </button>
          <button
            type="button"
            className="dev-button"
            onClick={() => handleQuickLogin('client-100@omni.one', 'Testpq12')}
            disabled={form.saving}
          >
            client-100@omni.one
          </button>
        </div>
      ) : null}
    </>
  )
}

export default Login
