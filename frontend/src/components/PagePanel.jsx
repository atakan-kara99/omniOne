import { formatErrorMessage } from '../errorUtils.js'

/**
 * PagePanel — standard page wrapper with loading/error states.
 *
 * Usage:
 *   <PagePanel
 *     title="Your clients"
 *     subtitle="Invite new clients and manage active coaching."
 *     loading={loading}
 *     error={error}
 *     headerRight={<Link to="/coach/clients">View clients</Link>}
 *     className="panel-narrow"
 *   >
 *     {content when loaded}
 *   </PagePanel>
 */
export default function PagePanel({
  title,
  subtitle,
  loading,
  error,
  status,
  loadingText = 'Loading...',
  headerRight,
  headerClassName = '',
  className = '',
  children,
}) {
  return (
    <section className={`panel${className ? ` ${className}` : ''}`}>
      {title ? (
        <div className={`panel-header${headerClassName ? ` ${headerClassName}` : ''}`}>
          <div>
            <h1>{title}</h1>
            {subtitle ? <p className="muted">{subtitle}</p> : null}
          </div>
          {headerRight || null}
        </div>
      ) : null}
      {loading ? <p className="muted">{loadingText}</p> : null}
      {error ? <p className="error">{typeof error === 'string' ? error : formatErrorMessage(error)}</p> : null}
      {status ? <p className="success">{status}</p> : null}
      {!loading ? children : null}
    </section>
  )
}
