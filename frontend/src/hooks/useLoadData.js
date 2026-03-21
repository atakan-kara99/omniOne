import { useEffect, useState } from 'react'
import { formatErrorMessage } from '../errorUtils.js'

/**
 * useLoadData — eliminates the mounted-flag + loading + error pattern for initial data fetching.
 *
 * Usage:
 *   const { loading, error } = useLoadData(async () => {
 *     const data = await getClients()
 *     setClients(data || [])
 *   }, [])
 *
 *   // With dependencies (re-fetches when clientId changes):
 *   const { loading, error } = useLoadData(async () => {
 *     const [client, plans] = await Promise.all([getClient(clientId), getPlans(clientId)])
 *     setClient(client)
 *     setPlans(plans || [])
 *   }, [clientId])
 */
export function useLoadData(loader, deps = []) {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let mounted = true

    async function load() {
      setLoading(true)
      setError('')
      try {
        await loader()
      } catch (err) {
        if (mounted) {
          setError(formatErrorMessage(err) || 'Something went wrong.')
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
  }, deps) // eslint-disable-line react-hooks/exhaustive-deps

  return { loading, error }
}
