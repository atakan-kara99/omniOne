import { useEffect, useState } from 'react'
import {
  changePassword,
  deleteUser,
  getProfile,
  getReferenceCities,
  getReferenceCountries,
  updateProfile,
} from '../api.js'
import { useAuth } from '../authContext.js'
import { EMPTY_PROFILE, isProfileComplete } from '../profileUtils.js'
import { isValidPassword, PASSWORD_PATTERN_STRING, PASSWORD_REQUIREMENTS } from '../passwordUtils.js'
import { useFormState } from '../hooks/useFormState.js'
import { useLoadData } from '../hooks/useLoadData.js'
import FormField from '../components/FormField.jsx'
import SearchableSelect from '../components/SearchableSelect.jsx'
import StatusMessage from '../components/StatusMessage.jsx'
import Button from '../components/Button.jsx'
import PagePanel from '../components/PagePanel.jsx'

function Profile() {
  const { user, logout, setUser, setProfileComplete } = useAuth()
  const [profile, setProfile] = useState(EMPTY_PROFILE)
  const [passwords, setPasswords] = useState({ oldPassword: '', newPassword: '' })
  const [countries, setCountries] = useState([])
  const [cities, setCities] = useState([])
  const [countrySearch, setCountrySearch] = useState('')
  const [citySearch, setCitySearch] = useState('')
  const form = useFormState()
  const [savingPassword, setSavingPassword] = useState(false)
  const countryOptions = countries
  const cityOptions = cities

  useEffect(() => {
    let cancelled = false

    getReferenceCountries('', 300).then((result) => {
      if (!cancelled) {
        setCountries(result)
      }
    })

    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    let cancelled = false

    if (!profile.countryCode) {
      return undefined
    }

    getReferenceCities(profile.countryCode, '', 10000).then((result) => {
      if (!cancelled) {
        setCities(result)
      }
    })

    return () => {
      cancelled = true
    }
  }, [profile.countryCode])

  const { loading, error: loadError } = useLoadData(async () => {
    const profileData = await getProfile()
    setProfile({
      ...EMPTY_PROFILE,
      ...profileData,
    })
    setCities([])
    setCitySearch(profileData?.city || '')
  }, [])

  async function handleProfileSave(event) {
    event.preventDefault()
    form.startSaving()
    try {
      await updateProfile(profile)
      setProfileComplete(isProfileComplete(profile))
      if (user) {
        setUser({ ...user, ...profile })
      }
      form.setSuccess('Profile updated.')
    } catch (err) {
      form.setFailure(err)
    }
  }

  async function handlePasswordSave(event) {
    event.preventDefault()
    setSavingPassword(true)
    form.reset()
    try {
      if (!isValidPassword(passwords.newPassword)) {
        form.setErrorManual(PASSWORD_REQUIREMENTS)
        setSavingPassword(false)
        return
      }
      await changePassword(passwords)
      form.setSuccess('Password updated.')
      setPasswords({ oldPassword: '', newPassword: '' })
    } catch (err) {
      form.setFailure(err)
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
      form.setFailure(err)
    }
  }

  return (
    <PagePanel
      title="Your profile"
      subtitle="Update your personal info, password, and account settings."
      loading={loading}
      error={loadError}
      status={form.status}
    >
      <div className="split-grid">
        <div className="card">
          <div className="card-title">Personal details</div>
          <StatusMessage status={form.status} error={form.error} />
          <form className="form" onSubmit={handleProfileSave} autoComplete="off">
            <FormField label="Email">
              <input
                type="email"
                name="email"
                id="profile-email"
                autoComplete="off"
                value={user?.email || ''}
                disabled
              />
            </FormField>
            <FormField label="First name" error={form.fieldErrors?.firstName}>
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
            </FormField>
            <FormField label="Last name" error={form.fieldErrors?.lastName}>
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
            </FormField>
            <FormField label="Birth date" error={form.fieldErrors?.birthDate}>
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
            </FormField>
            <FormField label="Gender" error={form.fieldErrors?.gender}>
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
            </FormField>
            <FormField label="Country" error={form.fieldErrors?.countryCode}>
              <SearchableSelect
                value={profile.countryCode}
                searchValue={countrySearch}
                id="profile-countryCode"
                name="countryCode"
                options={countryOptions}
                placeholder="Search country"
                emptyText="No matching countries."
                onSearchChange={setCountrySearch}
                onChange={(countryCode) => {
                  setProfile((prev) => ({
                    ...prev,
                    countryCode,
                    city: '',
                  }))
                  setCities([])
                  setCitySearch('')
                }}
                required
              />
            </FormField>
            <FormField label="City" error={form.fieldErrors?.city}>
              <SearchableSelect
                value={profile.city}
                searchValue={citySearch}
                id="profile-city"
                name="city"
                options={cityOptions}
                placeholder={profile.countryCode ? 'Search city' : 'Select a country first'}
                emptyText="No matching cities."
                onSearchChange={setCitySearch}
                onChange={(city) =>
                  setProfile((prev) => ({
                    ...prev,
                    city,
                  }))
                }
                disabled={!profile.countryCode || cities.length === 0}
                required
              />
            </FormField>
            <Button type="submit" loading={form.saving} loadingText="Saving...">
              Save profile
            </Button>
          </form>
        </div>
        <div className="card">
          <div className="card-title">Security</div>
          <form className="form" onSubmit={handlePasswordSave} autoComplete="off">
            <FormField label="Current password" error={form.fieldErrors?.oldPassword}>
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
            </FormField>
            <FormField label="New password" error={form.fieldErrors?.newPassword}>
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
            </FormField>
            <Button type="submit" loading={savingPassword} loadingText="Updating...">
              Change password
            </Button>
          </form>
          <div className="danger-zone">
            <div>
              <div className="card-title">Delete account</div>
              <p className="muted">This will permanently remove your account.</p>
            </div>
            <Button variant="danger" onClick={handleDeleteAccount}>
              Delete account
            </Button>
          </div>
        </div>
      </div>
    </PagePanel>
  )
}

export default Profile
