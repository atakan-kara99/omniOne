import { useState } from 'react'
import { Link } from 'react-router-dom'
import { register } from '../api.js'
import { useFormState } from '../hooks/useFormState.js'
import { formatErrorMessage, getFieldErrors } from '../errorUtils.js'
import { isValidPassword, PASSWORD_PATTERN_STRING, PASSWORD_REQUIREMENTS } from '../passwordUtils.js'
import FormField from '../components/FormField.jsx'
import StatusMessage from '../components/StatusMessage.jsx'
import Button from '../components/Button.jsx'

function Register() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const form = useFormState()

  async function handleSubmit(event) {
    event.preventDefault()
    form.reset()

    if (!isValidPassword(password)) {
      form.setError(PASSWORD_REQUIREMENTS)
      return
    }

    form.startSaving()
    try {
      await register({ email, password, role: 'COACH' })
      form.setSuccess('Coach account created. Check your email to activate it.')
    } catch (err) {
      form.setFieldErrors(getFieldErrors(err))
      form.setFailure(formatErrorMessage(err) || 'Registration failed.')
    }
  }

  return (
    <section className="panel panel-narrow">
      <h1>Create Your Account</h1>
      <p className="muted">Create a coach account and set up your login.</p>
      <form className="form" onSubmit={handleSubmit} autoComplete="off">
        <FormField label="Email" error={form.fieldErrors?.email}>
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
        </FormField>
        <FormField label="Password" error={form.fieldErrors?.password}>
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
        </FormField>
        <Button type="submit" loading={form.saving} loadingText="Creating account...">
          Register
        </Button>
        <StatusMessage status={form.status} error={form.error} />
      </form>
      <p className="hint">
        Already active? <Link to="/login">Sign in</Link>
      </p>
    </section>
  )
}

export default Register
