export const EMPTY_PROFILE = {
  firstName: '',
  lastName: '',
  birthDate: '',
  gender: 'OTHER',
  countryCode: '',
  city: '',
}

const REQUIRED_GENDERS = new Set(['MALE', 'FEMALE', 'OTHER'])

export function isProfileComplete(profile) {
  if (!profile) return false
  const firstName = (profile.firstName || '').trim()
  const lastName = (profile.lastName || '').trim()
  const birthDate = (profile.birthDate || '').trim()
  const gender = profile.gender || ''
  const countryCode = (profile.countryCode || '').trim()
  const city = (profile.city || '').trim()
  return Boolean(firstName && lastName && birthDate && REQUIRED_GENDERS.has(gender) && countryCode && city)
}
