import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getProfile, getReferenceCities, getReferenceCountries, updateProfile } from '../api.js'
import { useAuth } from '../authContext.js'
import { EMPTY_PROFILE, isProfileComplete } from '../profileUtils.js'
import { useFormState } from '../hooks/useFormState.js'
import { useLoadData } from '../hooks/useLoadData.js'
import FormField from '../components/FormField.jsx'
import SearchableSelect from '../components/SearchableSelect.jsx'
import StatusMessage from '../components/StatusMessage.jsx'
import Button from '../components/Button.jsx'
import PagePanel from '../components/PagePanel.jsx'

function CompleteProfile() {
  const navigate = useNavigate()
  const { user, setUser, profileComplete, setProfileComplete } = useAuth()
  const [profile, setProfile] = useState(EMPTY_PROFILE)
  const [countries, setCountries] = useState([])
  const [cities, setCities] = useState([])
  const [countrySearch, setCountrySearch] = useState('')
  const [citySearch, setCitySearch] = useState('')
  const form = useFormState()
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

  useEffect(() => {
    if (profileComplete) {
      navigate('/', { replace: true })
    }
  }, [profileComplete, navigate])

  const { loading, error: loadError } = useLoadData(async () => {
    try {
      const profileData = await getProfile()
      setProfile({
        ...EMPTY_PROFILE,
        ...profileData,
      })
      setCities([])
      setCitySearch(profileData?.city || '')
    } catch (err) {
      if (err?.status === 404) {
        setProfile(EMPTY_PROFILE)
        setCities([])
      } else {
        throw err
      }
    }
  }, [])

  async function handleProfileSave(event) {
    event.preventDefault()
    form.startSaving()
    try {
      await updateProfile(profile)
      const complete = isProfileComplete(profile)
      setProfileComplete(complete)
      if (user) {
        setUser({ ...user, ...profile })
      }
      form.setSuccess('Profile updated.')
      if (complete) {
        navigate('/', { replace: true })
      }
    } catch (err) {
      form.setFailure(err)
    }
  }

  return (
    <PagePanel
      title="Complete your profile"
      subtitle="We need a few details before you can continue."
      loading={loading}
      error={loadError}
      status={form.status}
      className="panel-wide"
    >
      <div className="card">
        <div className="card-title">Required details</div>
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
              required
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
            Save and continue
          </Button>
        </form>
      </div>
    </PagePanel>
  )
}

export default CompleteProfile
