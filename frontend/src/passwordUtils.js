export const PASSWORD_PATTERN_STRING = '^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)\\S{8,32}$'
export const PASSWORD_PATTERN = new RegExp(PASSWORD_PATTERN_STRING)

export const PASSWORD_REQUIREMENTS =
  'Password must be 8–32 characters and include at least one uppercase letter, one lowercase letter, and one number. Special characters are allowed. No spaces.'

export function isValidPassword(value) {
  return PASSWORD_PATTERN.test(value || '')
}
