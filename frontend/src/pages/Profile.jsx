import { useEffect, useState } from 'react'
import {
  changePassword,
  deleteUser,
  getProfile,
  updateProfile,
} from '../api.js'
import { useAuth } from '../authContext.js'
import { formatErrorMessage, getFieldErrors } from '../errorUtils.js'
import { EMPTY_PROFILE, isProfileComplete } from '../profileUtils.js'
import { isValidPassword, PASSWORD_PATTERN_STRING, PASSWORD_REQUIREMENTS } from '../passwordUtils.js'

function Profile() {
  const { user, logout, setUser, setProfileComplete } = useAuth()
  const [profile, setProfile] = useState(EMPTY_PROFILE)
  const [passwords, setPasswords] = useState({ oldPassword: '', newPassword: '' })
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [savingPassword, setSavingPassword] = useState(false)

  useEffect(() => {
    let mounted = true
    async function load() {
      setLoading(true)
      setError('')
      setFieldErrors(null)
      try {
        const profileData = await getProfile()
        if (mounted) {
          setProfile({
            ...EMPTY_PROFILE,
            ...profileData,
          })
        }
      } catch (err) {
        if (mounted) {
          setError(err || 'Failed to load profile.')
        }
      } finally {
        if (mounted) {
          setLoading(false)
        }
      }
    }

    load()
    return () => {
      mounted = false
    }
  }, [])

  async function handleProfileSave(event) {
    event.preventDefault()
    setStatus('')
    setError('')
    setFieldErrors(null)
    setSaving(true)
    try {
      await updateProfile(profile)
      setProfileComplete(isProfileComplete(profile))
      if (user) {
        setUser({ ...user, ...profile })
      }
      setStatus('Profile updated.')
    } catch (err) {
      setFieldErrors(getFieldErrors(err))
      setError(err || 'Failed to update profile.')
    } finally {
      setSaving(false)
    }
  }

  async function handlePasswordSave(event) {
    event.preventDefault()
    setStatus('')
    setError('')
    setFieldErrors(null)
    setSavingPassword(true)
    try {
      if (!isValidPassword(passwords.newPassword)) {
        setError(PASSWORD_REQUIREMENTS)
        setSavingPassword(false)
        return
      }
      await changePassword(passwords)
      setStatus('Password updated.')
      setPasswords({ oldPassword: '', newPassword: '' })
    } catch (err) {
      setFieldErrors(getFieldErrors(err))
      setError(err || 'Failed to update password.')
    } finally {
      setSavingPassword(false)
    }
  }

  async function handleDeleteAccount() {
    const ok = window.confirm('Delete your account? This action is permanent.')
    if (!ok) return
    try {
      await deleteUser()
      sessionStorage.setItem('omniOne.accountDeleted', '1')
      logout()
    } catch (err) {
      setError(err || 'Failed to delete account.')
    }
  }

  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <h1>Your profile</h1>
          <p className="muted">Update your personal info, password, and account settings.</p>
        </div>
      </div>
      {loading ? <p className="muted">Loading profile...</p> : null}
      {error ? <p className="error">{formatErrorMessage(error)}</p> : null}
      {status ? <p className="success">{status}</p> : null}
      {!loading ? (
        <div className="split-grid">
          <div className="card">
            <div className="card-title">Personal details</div>
            <form className="form" onSubmit={handleProfileSave} autoComplete="off">
              <label className="field">
                <span>Email</span>
                <input
                  type="email"
                  name="email"
                  id="profile-email"
                  autoComplete="off"
                  value={user?.email || ''}
                  disabled
                />
              </label>
              <label className="field">
                <span>First name</span>
                <input
                  type="text"
                  name="firstName"
                  id="profile-firstName"
                  autoComplete="off"
                  value={profile.firstName}
                  onChange={(event) =>
                    setProfile((prev) => ({
                      ...prev,
                      firstName: event.target.value,
                    }))
                  }
                  required
                />
                {fieldErrors?.firstName ? (
                  <p className="field-error">{fieldErrors.firstName}</p>
                ) : null}
              </label>
              <label className="field">
                <span>Last name</span>
                <input
                  type="text"
                  name="lastName"
                  id="profile-lastName"
                  autoComplete="off"
                  value={profile.lastName}
                  onChange={(event) =>
                    setProfile((prev) => ({
                      ...prev,
                      lastName: event.target.value,
                    }))
                  }
                  required
                />
                {fieldErrors?.lastName ? (
                  <p className="field-error">{fieldErrors.lastName}</p>
                ) : null}
              </label>
              <label className="field">
                <span>Birth date</span>
                <input
                  type="date"
                  name="birthDate"
                  id="profile-birthDate"
                  autoComplete="off"
                  value={profile.birthDate}
                  onChange={(event) =>
                    setProfile((prev) => ({
                      ...prev,
                      birthDate: event.target.value,
                    }))
                  }
                  required
                />
                {fieldErrors?.birthDate ? (
                  <p className="field-error">{fieldErrors.birthDate}</p>
                ) : null}
              </label>
              <label className="field">
                <span>Gender</span>
                <select
                  name="gender"
                  id="profile-gender"
                  autoComplete="off"
                  value={profile.gender}
                  onChange={(event) =>
                    setProfile((prev) => ({
                      ...prev,
                      gender: event.target.value,
                    }))
                  }
                  className="select"
                >
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                  <option value="OTHER">Other</option>
                </select>
                {fieldErrors?.gender ? <p className="field-error">{fieldErrors.gender}</p> : null}
              </label>
              <button type="submit" disabled={saving}>
                {saving ? 'Saving...' : 'Save profile'}
              </button>
            </form>
          </div>
          <div className="card">
            <div className="card-title">Security</div>
            <form className="form" onSubmit={handlePasswordSave} autoComplete="off">
              <label className="field">
                <span>Current password</span>
                <input
                  type="password"
                  name="oldPassword"
                  id="profile-oldPassword"
                  autoComplete="off"
                  value={passwords.oldPassword}
                  onChange={(event) =>
                    setPasswords((prev) => ({
                      ...prev,
                      oldPassword: event.target.value,
                    }))
                  }
                  minLength={8}
                  maxLength={32}
                  pattern={PASSWORD_PATTERN_STRING}
                  title={PASSWORD_REQUIREMENTS}
                  onInvalid={(event) => event.target.setCustomValidity(PASSWORD_REQUIREMENTS)}
                  onInput={(event) => event.target.setCustomValidity('')}
                  required
                />
                {fieldErrors?.oldPassword ? (
                  <p className="field-error">{fieldErrors.oldPassword}</p>
                ) : null}
              </label>
              <label className="field">
                <span>New password</span>
                <input
                  type="password"
                  name="newPassword"
                  id="profile-newPassword"
                  autoComplete="off"
                  value={passwords.newPassword}
                  onChange={(event) =>
                    setPasswords((prev) => ({
                      ...prev,
                      newPassword: event.target.value,
                    }))
                  }
                  minLength={8}
                  maxLength={32}
                  pattern={PASSWORD_PATTERN_STRING}
                  title={PASSWORD_REQUIREMENTS}
                  onInvalid={(event) => event.target.setCustomValidity(PASSWORD_REQUIREMENTS)}
                  onInput={(event) => event.target.setCustomValidity('')}
                  required
                />
                {fieldErrors?.newPassword ? (
                  <p className="field-error">{fieldErrors.newPassword}</p>
                ) : null}
              </label>
              <button type="submit" disabled={savingPassword}>
                {savingPassword ? 'Updating...' : 'Change password'}
              </button>
            </form>
            <div className="danger-zone">
              <div>
                <div className="card-title">Delete account</div>
                <p className="muted">This will permanently remove your account.</p>
              </div>
              <button type="button" className="danger-button" onClick={handleDeleteAccount}>
                Delete account
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </section>
  )
}

export default Profile
