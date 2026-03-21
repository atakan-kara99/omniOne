import { useState } from 'react'
import { Link } from 'react-router-dom'
import { forgotPassword } from '../api.js'
import { useFormState } from '../hooks/useFormState.js'
import { formatErrorMessage, getFieldErrors } from '../errorUtils.js'
import FormField from '../components/FormField.jsx'
import StatusMessage from '../components/StatusMessage.jsx'
import Button from '../components/Button.jsx'

function ForgotPassword() {
  const [email, setEmail] = useState('')
  const form = useFormState()

  async function handleSubmit(event) {
    event.preventDefault()
    form.reset()

    form.startSaving()
    try {
      await forgotPassword(email)
      form.setSuccess('Reset link sent. Check your email for next steps.')
    } catch (err) {
      form.setFieldErrors(getFieldErrors(err))
      form.setFailure(formatErrorMessage(err) || 'Failed to send reset link.')
    }
  }

  return (
    <section className="panel panel-narrow">
      <h1>Reset Your Password</h1>
      <p className="muted">Enter your email to receive a reset link.</p>
      <form className="form" onSubmit={handleSubmit} autoComplete="off">
        <FormField label="Email" error={form.fieldErrors?.email}>
          <input
            type="email"
            name="email"
            id="forgot-email"
            autoComplete="off"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="Email"
            required
          />
        </FormField>
        <Button loading={form.saving} loadingText="Sending...">
          Send reset link
        </Button>
        <StatusMessage status={form.status} error={form.error} />
      </form>
      <p className="hint">
        <Link to="/login">Back to sign in</Link>
      </p>
    </section>
  )
}

export default ForgotPassword
