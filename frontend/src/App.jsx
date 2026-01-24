import {
  BrowserRouter,
  Navigate,
  Link,
  NavLink,
  Route,
  Routes,
  useNavigate,
} from 'react-router-dom'
import { useEffect, useMemo, useState } from 'react'
import './App.css'
import {
  clearStoredUser,
  clearToken,
  getStoredUser,
  getToken,
  setStoredUser,
} from './auth.js'
import { getUser } from './api.js'
import { AuthContext, useAuth } from './authContext.js'
import { House, List, SignOut, User } from 'phosphor-react'
import CoachDashboard from './pages/CoachDashboard.jsx'
import ClientDashboard from './pages/ClientDashboard.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import ActivateAccount from './pages/ActivateAccount.jsx'
import ForgotPassword from './pages/ForgotPassword.jsx'
import ResetPassword from './pages/ResetPassword.jsx'
import AcceptInvitation from './pages/AcceptInvitation.jsx'
import Profile from './pages/Profile.jsx'
import CoachClients from './pages/CoachClients.jsx'
import CoachClientDetail from './pages/CoachClientDetail.jsx'
import CoachQuestionnaire from './pages/CoachQuestionnaire.jsx'
import ClientNutritionPlans from './pages/ClientNutritionPlans.jsx'
import ClientQuestionnaire from './pages/ClientQuestionnaire.jsx'
import ChatDetail from './pages/ChatDetail.jsx'
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

function ProtectedRoute({ children, allowedRoles }) {
  const { user, loading } = useAuth()
  if (loading) {
    return <LoadingScreen />
  }
  if (!user) {
    return <Navigate to="/login" replace />
  }
  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/login" replace />
  }
  return children
}

function AppShell({ children, user, onLogout }) {
  const navigate = useNavigate()
  const [isNavOpen, setIsNavOpen] = useState(false)
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
        {user ? (
          <button
            type="button"
            className="nav-toggle nav-toggle-header"
            aria-label="Toggle navigation"
            aria-expanded={isNavOpen}
            aria-controls="side-nav"
            onClick={() => setIsNavOpen((open) => !open)}
          >
            <List size={22} />
          </button>
        ) : null}
        <div className="user-slot">
          {user ? (
            <div className="user-chip">
              <NavLink
                to={user?.role === 'COACH' ? '/coach' : '/client'}
                className={({ isActive }) => `icon-button user-action${isActive ? ' is-active' : ''}`}
                aria-label="Dashboard"
                title="Dashboard"
              >
                <House size={22} weight="bold" />
                <span className="button-label">Dashboard</span>
              </NavLink>
              <NavLink
                to="/profile"
                className={({ isActive }) => `icon-button user-action profile-action${isActive ? ' is-active' : ''}`}
                aria-label="Profile"
                title="Profile"
              >
                <User size={22} weight="bold" />
                <span className="user-action-email">{user.email}</span>
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
      <div className={`app-body${user ? '' : ' solo'}`}>
        {user ? (
          <button
            type="button"
            className={`nav-overlay${isNavOpen ? ' show' : ''}`}
            aria-label="Close navigation"
            onClick={() => setIsNavOpen(false)}
          />
        ) : null}
        {user ? (
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

  useEffect(() => {
    let mounted = true

    async function loadUser() {
      const token = getToken()
      if (!token) {
        setLoadingUser(false)
        return
      }
      setLoadingUser(true)
      setAuthError('')
      try {
        const userData = await getUser()
        if (mounted) {
          setUser(userData)
          setStoredUser(userData)
        }
      } catch (err) {
        if (mounted) {
          clearToken()
          clearStoredUser()
          setUser(null)
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
      },
      logout: () => {
        if (user?.id) {
          localStorage.setItem(`omniOne.chatLastSeen.${user.id}`, new Date().toISOString())
        }
        clearToken()
        clearStoredUser()
        sessionStorage.removeItem('omniOne.chatDockScroll')
        setUser(null)
      },
      loading: loadingUser,
    }),
    [user, loadingUser],
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
            <Route path="/" element={<Navigate to={user ? (user.role === 'COACH' ? '/coach' : '/client') : '/login'} replace />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/activate" element={<ActivateAccount />} />
            <Route path="/forgot" element={<ForgotPassword />} />
            <Route path="/reset" element={<ResetPassword />} />
            <Route path="/invite" element={<AcceptInvitation />} />
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
              path="/coach/chats/:conversationId"
              element={
                <ProtectedRoute allowedRoles={['COACH']}>
                  <ChatDetail />
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
              path="/client/chats/:conversationId"
              element={
                <ProtectedRoute allowedRoles={['CLIENT']}>
                  <ChatDetail />
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
            <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </AppShell>
      </BrowserRouter>
    </AuthContext.Provider>
  )
}

export default App
