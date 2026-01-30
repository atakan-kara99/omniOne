import { BrowserRouter, Navigate, Link, NavLink, Route, Routes, useLocation } from 'react-router-dom'
import { useEffect, useMemo, useState } from 'react'
import './App.css'
import {
  clearStoredUser,
  clearToken,
  getStoredUser,
  getToken,
  setToken,
  setStoredUser,
} from './auth.js'
import { getProfile, getUser, logout, refreshAuth, setLoggingOut } from './api.js'
import { AuthContext, useAuth } from './authContext.js'
import { House, List, SignOut, User } from 'phosphor-react'
import { isProfileComplete } from './profileUtils.js'
import CoachDashboard from './pages/CoachDashboard.jsx'
import ClientDashboard from './pages/ClientDashboard.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import ActivateAccount from './pages/ActivateAccount.jsx'
import ForgotPassword from './pages/ForgotPassword.jsx'
import ResetPassword from './pages/ResetPassword.jsx'
import AcceptInvitation from './pages/AcceptInvitation.jsx'
import Profile from './pages/Profile.jsx'
import CompleteProfile from './pages/CompleteProfile.jsx'
import CoachClients from './pages/CoachClients.jsx'
import CoachClientDetail from './pages/CoachClientDetail.jsx'
import CoachQuestionnaire from './pages/CoachQuestionnaire.jsx'
import ClientNutritionPlans from './pages/ClientNutritionPlans.jsx'
import ClientQuestionnaire from './pages/ClientQuestionnaire.jsx'
import ClientCoach from './pages/ClientCoach.jsx'
import ChatDock from './components/ChatDock.jsx'

function LoadingScreen({ message }) {
  return (
    <div className="panel hero">
      <div className="hero-body">
        <h1>{message || 'Warming up your dashboard...'}</h1>
        <p className="muted">Fetching session context and secure data.</p>
      </div>
      <div className="hero-orb" aria-hidden="true" />
    </div>
  )
}

function ProtectedRoute({ children, allowedRoles, allowIncomplete = false }) {
  const { user, loading, profileComplete } = useAuth()
  if (loading) {
    return <LoadingScreen />
  }
  if (!user) {
    return <Navigate to="/login" replace />
  }
  if (!allowIncomplete && !profileComplete) {
    return <Navigate to="/profile/complete" replace />
  }
  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/login" replace />
  }
  return children
}

function getHomePath(user) {
  if (!user) return '/login'
  return user.role === 'COACH' ? '/coach' : '/client'
}

function PublicOnlyRoute({ children }) {
  const { user, loading, profileComplete } = useAuth()
  if (loading) {
    return <LoadingScreen />
  }
  if (user) {
    if (!profileComplete) {
      return <Navigate to="/profile/complete" replace />
    }
    return <Navigate to={getHomePath(user)} replace />
  }
  return children
}

function AppShell({ children, user, onLogout }) {
  const location = useLocation()
  const isProfileCompleteRoute = location.pathname === '/profile/complete'
  const isSoloLayout = !user || isProfileCompleteRoute
  const [isNavOpen, setIsNavOpen] = useState(false)
  const profileLabel = useMemo(() => {
    const first = user?.firstName?.trim() || ''
    const last = user?.lastName?.trim() || ''
    const full = `${first} ${last}`.trim()
    return full || user?.email || ''
  }, [user?.firstName, user?.lastName, user?.email])
  const navItems = user?.role === 'COACH'
    ? [
        { label: 'Clients', to: '/coach/clients' },
        { label: 'Questionnaire', to: '/coach/questionnaire' },
      ]
    : user?.role === 'CLIENT'
      ? [
          { label: 'Coach', to: '/client/coach' },
          { label: 'Nutrition plans', to: '/client/nutrition-plans' },
          { label: 'Questionnaire', to: '/client/questionnaire' },
        ]
      : []

  return (
    <div className="app-shell">
      <header className="top-bar">
        {user && !isProfileCompleteRoute ? (
          <button
            type="button"
            className="nav-toggle nav-toggle-header"
            aria-label="Toggle navigation"
            aria-expanded={isNavOpen}
            aria-controls="side-nav"
            onClick={() => setIsNavOpen((open) => !open)}
          >
            <List size={22} weight="bold" />
          </button>
        ) : null}
        <div className="user-slot">
          {user ? (
            <div className="user-chip">
              {!isProfileCompleteRoute ? (
                <NavLink
                  to={user?.role === 'COACH' ? '/coach' : '/client'}
                  end
                  className={({ isActive }) => `icon-button user-action${isActive ? ' is-active' : ''}`}
                  aria-label="Dashboard"
                  title="Dashboard"
                >
                  <House size={22} weight="bold" />
                  <span className="button-label">Dashboard</span>
                </NavLink>
              ) : null}
              <NavLink
                to="/profile"
                className={({ isActive }) => `icon-button user-action profile-action${isActive ? ' is-active' : ''}`}
                aria-label="Profile"
                title="Profile"
              >
                <User size={22} weight="bold" />
                <span className="user-action-email">{profileLabel}</span>
              </NavLink>
              <button type="button" className="icon-button user-action" onClick={onLogout} aria-label="Sign off" title="Sign off">
                <span className="user-action-icon">
                  <SignOut size={22} weight="bold" />
                </span>
                <span className="button-label">Sign off</span>
              </button>
            </div>
          ) : null}
        </div>
        <Link className={`brand-block${user ? '' : ' centered'}`} to="/" aria-label="omniOne home">
          <img className="brand-logo" src="/logo.svg" alt="omniOne" />
          <div>
            <div className="brand">omniOne</div>
            <div className="brand-sub">be One with Omnia</div>
          </div>
        </Link>
        <div className="top-spacer" />
      </header>
      <div className={`app-body${isSoloLayout ? ' solo' : ''}`}>
        {user && !isProfileCompleteRoute ? (
          <button
            type="button"
            className={`nav-overlay${isNavOpen ? ' show' : ''}`}
            aria-label="Close navigation"
            onClick={() => setIsNavOpen(false)}
          />
        ) : null}
        {user && !isProfileCompleteRoute ? (
          <aside id="side-nav" className={`side-nav${isNavOpen ? ' open' : ''}`}>
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/coach' || item.to === '/client'}
                className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
                onClick={() => setIsNavOpen(false)}
              >
                {item.label}
              </NavLink>
            ))}
          </aside>
        ) : null}
        <main className="content">{children}</main>
      </div>
      {user ? <ChatDock /> : null}
    </div>
  )
}

function App() {
  const [user, setUser] = useState(getStoredUser())
  const [loadingUser, setLoadingUser] = useState(Boolean(getToken()))
  const [authError, setAuthError] = useState('')
  const [profileComplete, setProfileComplete] = useState(false)

  useEffect(() => {
    let mounted = true

    async function loadUser() {
      let token = getToken()
      if (token) {
        setLoadingUser(true)
      }
      setAuthError('')
      try {
        if (!token) {
          try {
            const refreshed = await refreshAuth()
            if (refreshed?.jwt) {
              setToken(refreshed.jwt)
              token = refreshed.jwt
            }
          } catch {
            // no refresh cookie or refresh failed
          }
        }
        if (!token) {
          clearToken()
          clearStoredUser()
          setUser(null)
          setProfileComplete(false)
          setLoadingUser(false)
          return
        }
        const userData = await getUser()
        let profileData = null
        try {
          profileData = await getProfile()
        } catch {
          profileData = null
        }
        const mergedUser = profileData ? { ...userData, ...profileData } : userData
        if (mounted) {
          setUser(mergedUser)
          setStoredUser(mergedUser)
          setProfileComplete(isProfileComplete(mergedUser))
        }
      } catch {
        if (mounted) {
          clearToken()
          clearStoredUser()
          setUser(null)
          setProfileComplete(false)
          setAuthError('Session expired. Please sign in again.')
        }
      } finally {
        if (mounted) {
          setLoadingUser(false)
        }
      }
    }

    loadUser()

    return () => {
      mounted = false
    }
  }, [])

  const authValue = useMemo(
    () => ({
      user,
      setUser: (nextUser) => {
        setUser(nextUser)
        setStoredUser(nextUser)
        setAuthError('')
        setProfileComplete(isProfileComplete(nextUser))
      },
      profileComplete,
      setProfileComplete,
      logout: () => {
        setLoggingOut(true)
        logout().catch(() => {})
        if (user?.id) {
          localStorage.setItem(`omniOne.chatLastSeen.${user.id}`, new Date().toISOString())
        }
        clearToken()
        clearStoredUser()
        sessionStorage.setItem('omniOne.loggedOut', '1')
        sessionStorage.removeItem('omniOne.chatDockScroll')
        setUser(null)
        setProfileComplete(false)
      },
      loading: loadingUser,
    }),
    [user, loadingUser, profileComplete],
  )

  useEffect(() => {
    if (!user?.id) return
    const handleUnload = () => {
      localStorage.setItem(`omniOne.chatLastSeen.${user.id}`, new Date().toISOString())
    }
    window.addEventListener('beforeunload', handleUnload)
    return () => {
      window.removeEventListener('beforeunload', handleUnload)
    }
  }, [user?.id])

  if (loadingUser) {
    return (
      <div className="app-shell">
        <div className="content wide">
          <LoadingScreen />
        </div>
      </div>
    )
  }

  return (
    <AuthContext.Provider value={authValue}>
      <BrowserRouter>
        <AppShell user={user} onLogout={authValue.logout}>
          {authError ? <p className="error-banner">{authError}</p> : null}
          <Routes>
            <Route path="/" element={<Navigate to={getHomePath(user)} replace />} />
            <Route
              path="/login"
              element={
                <PublicOnlyRoute>
                  <Login />
                </PublicOnlyRoute>
              }
            />
            <Route
              path="/register"
              element={
                <PublicOnlyRoute>
                  <Register />
                </PublicOnlyRoute>
              }
            />
            <Route
              path="/activate"
              element={
                <PublicOnlyRoute>
                  <ActivateAccount />
                </PublicOnlyRoute>
              }
            />
            <Route
              path="/forgot"
              element={
                <PublicOnlyRoute>
                  <ForgotPassword />
                </PublicOnlyRoute>
              }
            />
            <Route
              path="/reset"
              element={
                <PublicOnlyRoute>
                  <ResetPassword />
                </PublicOnlyRoute>
              }
            />
            <Route
              path="/invite"
              element={
                <PublicOnlyRoute>
                  <AcceptInvitation />
                </PublicOnlyRoute>
              }
            />
            <Route
              path="/coach"
              element={
                <ProtectedRoute allowedRoles={['COACH']}>
                  <CoachDashboard user={user} />
                </ProtectedRoute>
              }
            />
            <Route
              path="/coach/clients"
              element={
                <ProtectedRoute allowedRoles={['COACH']}>
                  <CoachClients />
                </ProtectedRoute>
              }
            />
            <Route
              path="/coach/clients/:clientId"
              element={
                <ProtectedRoute allowedRoles={['COACH']}>
                  <CoachClientDetail />
                </ProtectedRoute>
              }
            />
            <Route
              path="/coach/questionnaire"
              element={
                <ProtectedRoute allowedRoles={['COACH']}>
                  <CoachQuestionnaire />
                </ProtectedRoute>
              }
            />
            <Route
              path="/client"
              element={
                <ProtectedRoute allowedRoles={['CLIENT']}>
                  <ClientDashboard user={user} />
                </ProtectedRoute>
              }
            />
            <Route
              path="/client/nutrition-plans"
              element={
                <ProtectedRoute allowedRoles={['CLIENT']}>
                  <ClientNutritionPlans />
                </ProtectedRoute>
              }
            />
            <Route
              path="/client/questionnaire"
              element={
                <ProtectedRoute allowedRoles={['CLIENT']}>
                  <ClientQuestionnaire />
                </ProtectedRoute>
              }
            />
            <Route
              path="/client/coach"
              element={
                <ProtectedRoute allowedRoles={['CLIENT']}>
                  <ClientCoach />
                </ProtectedRoute>
              }
            />
            <Route
              path="/profile"
              element={
                <ProtectedRoute allowedRoles={['COACH', 'CLIENT']}>
                  <Profile />
                </ProtectedRoute>
              }
            />
            <Route
              path="/profile/complete"
              element={
                <ProtectedRoute allowedRoles={['COACH', 'CLIENT']} allowIncomplete>
                  <CompleteProfile />
                </ProtectedRoute>
              }
            />
            <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </AppShell>
      </BrowserRouter>
    </AuthContext.Provider>
  )
}

export default App
