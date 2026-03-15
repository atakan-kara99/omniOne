export const EMPTY_PROFILE = {
  firstName: '',
  lastName: '',
  birthDate: '',
  gender: 'OTHER',
}

const REQUIRED_GENDERS = new Set(['MALE', 'FEMALE', 'OTHER'])

export function isProfileComplete(profile) {
  if (!profile) return false
  const firstName = (profile.firstName || '').trim()
  const lastName = (profile.lastName || '').trim()
  const birthDate = (profile.birthDate || '').trim()
  const gender = profile.gender || ''
  return Boolean(firstName && lastName && birthDate && REQUIRED_GENDERS.has(gender))
}
