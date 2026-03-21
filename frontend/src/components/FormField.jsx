/**
 * FormField — wraps a label + input + optional field-level error.
 *
 * Usage:
 *   <FormField label="Email" error={fieldErrors?.email}>
 *     <input type="email" value={email} onChange={...} required />
 *   </FormField>
 *
 *   // Without label (e.g. login page):
 *   <FormField error={fieldErrors?.email}>
 *     <input type="email" placeholder="Email" ... />
 *   </FormField>
 */
export default function FormField({ label, error, children, className = '' }) {
  return (
    <label className={`field${className ? ` ${className}` : ''}`}>
      {label ? <span>{label}</span> : null}
      {children}
      {error ? <p className="field-error">{error}</p> : null}
    </label>
  )
}
