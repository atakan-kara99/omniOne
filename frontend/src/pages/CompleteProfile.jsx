import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getProfile, updateProfile } from '../api.js'
import { useAuth } from '../authContext.js'
import { formatErrorMessage, getFieldErrors } from '../errorUtils.js'
import { EMPTY_PROFILE, isProfileComplete } from '../profileUtils.js'

function CompleteProfile() {
  const navigate = useNavigate()
  const { user, setUser, profileComplete, setProfileComplete } = useAuth()
  const [profile, setProfile] = useState(EMPTY_PROFILE)
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (profileComplete) {
      navigate('/', { replace: true })
    }
  }, [profileComplete, navigate])

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
          if (err?.status === 404) {
            setProfile(EMPTY_PROFILE)
          } else {
            setError(err || 'Failed to load profile.')
          }
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
      const complete = isProfileComplete(profile)
      setProfileComplete(complete)
      if (user) {
        setUser({ ...user, ...profile })
      }
      setStatus('Profile updated.')
      if (complete) {
        navigate('/', { replace: true })
      }
    } catch (err) {
      setFieldErrors(getFieldErrors(err))
      setError(err || 'Failed to update profile.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="panel panel-wide">
      <div className="panel-header">
        <div>
          <h1>Complete your profile</h1>
          <p className="muted">We need a few details before you can continue.</p>
        </div>
      </div>
      {loading ? <p className="muted">Loading profile...</p> : null}
      {error ? <p className="error">{formatErrorMessage(error)}</p> : null}
      {status ? <p className="success">{status}</p> : null}
      {!loading ? (
        <div className="card">
          <div className="card-title">Required details</div>
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
              {fieldErrors?.firstName ? <p className="field-error">{fieldErrors.firstName}</p> : null}
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
              {fieldErrors?.lastName ? <p className="field-error">{fieldErrors.lastName}</p> : null}
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
              {fieldErrors?.birthDate ? <p className="field-error">{fieldErrors.birthDate}</p> : null}
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
                required
              >
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
              {fieldErrors?.gender ? <p className="field-error">{fieldErrors.gender}</p> : null}
            </label>
            <button type="submit" disabled={saving}>
              {saving ? 'Saving...' : 'Save and continue'}
            </button>
          </form>
        </div>
      ) : null}
    </section>
  )
}

export default CompleteProfile
