/**
 * Button — wraps the existing CSS button classes with a consistent API.
 *
 * Usage:
 *   <Button loading={saving}>Save</Button>
 *   <Button variant="ghost" onClick={handleCancel}>Cancel</Button>
 *   <Button variant="danger" onClick={handleDelete}>Delete</Button>
 *
 * Props:
 *   variant  — 'primary' (default), 'ghost', 'danger'
 *   loading  — disables button and shows loadingText
 *   loadingText — text while loading (default: 'Saving...')
 */
export default function Button({
  variant = 'primary',
  type = 'button',
  loading = false,
  loadingText = 'Saving...',
  children,
  className = '',
  ...props
}) {
  const baseClass =
    variant === 'ghost'
      ? 'ghost-button'
      : variant === 'danger'
        ? 'danger-button'
        : 'button'

  return (
    <button
      type={type}
      className={`${baseClass}${className ? ` ${className}` : ''}`}
      disabled={loading || props.disabled}
      {...props}
    >
      {loading ? loadingText : children}
    </button>
  )
}
