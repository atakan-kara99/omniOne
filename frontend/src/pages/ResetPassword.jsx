import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { resetPassword } from '../api.js'
import { useFormState } from '../hooks/useFormState.js'
import { formatErrorMessage, getFieldErrors } from '../errorUtils.js'
import { isValidPassword, PASSWORD_PATTERN_STRING, PASSWORD_REQUIREMENTS } from '../passwordUtils.js'
import FormField from '../components/FormField.jsx'
import StatusMessage from '../components/StatusMessage.jsx'
import Button from '../components/Button.jsx'

function ResetPassword() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')
  const [password, setPassword] = useState('')
  const form = useFormState()

  async function handleSubmit(event) {
    event.preventDefault()
    form.reset()

    if (!token) {
      form.setErrorManual('Reset token missing.')
      return
    }

    if (!isValidPassword(password)) {
      form.setError(PASSWORD_REQUIREMENTS)
      return
    }

    form.startSaving()
    try {
      await resetPassword(token, { password })
      form.setSuccess('Password updated. You can now sign in.')
      setTimeout(() => navigate('/login'), 1000)
    } catch (err) {
      form.setFieldErrors(getFieldErrors(err))
      form.setFailure(formatErrorMessage(err) || 'Failed to reset password.')
    }
  }

  return (
    <section className="panel panel-narrow">
      <h1>Create A New Password</h1>
      <p className="muted">Choose a password to secure your account.</p>
      <form className="form" onSubmit={handleSubmit} autoComplete="off">
        <FormField label="Password" error={form.fieldErrors?.password}>
          <input
            type="password"
            name="password"
            id="reset-password"
            autoComplete="off"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="New password"
            minLength={8}
            maxLength={32}
            pattern={PASSWORD_PATTERN_STRING}
            title={PASSWORD_REQUIREMENTS}
            onInvalid={(event) => event.target.setCustomValidity(PASSWORD_REQUIREMENTS)}
            onInput={(event) => event.target.setCustomValidity('')}
            required
          />
        </FormField>
        <Button type="submit" loading={form.saving} loadingText="Updating...">
          Reset password
        </Button>
        <StatusMessage status={form.status} error={form.error} />
      </form>
      <p className="hint">
        <Link to="/login">Back to sign in</Link>
      </p>
    </section>
  )
}

export default ResetPassword
