import { useEffect, useRef, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { Client } from '@stomp/stompjs'
import { ArrowUUpLeft, PaperPlaneRight } from 'phosphor-react'
import { getChat, getChats, getClientCoach, getCoachClient } from '../api.js'
import { getToken } from '../auth.js'
import { useAuth } from '../authContext.js'

function buildWebSocketUrl() {
  const apiBase = import.meta.env.VITE_API_BASE || 'http://localhost:8080'
  const wsBase = apiBase.replace(/^http/, 'ws')
  return `${wsBase}/ws`
}

function parseMessage(message) {
  if (!message?.body) return null
  try {
    return JSON.parse(message.body)
  } catch {
    return message.body
  }
}

function formatMessageTime(value) {
  if (!value) return ''
  return new Date(value).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function ChatDetail() {
  const { conversationId } = useParams()
  const location = useLocation()
  const { user } = useAuth()
  const clientRef = useRef(null)
  const [chat, setChat] = useState(null)
  const [messages, setMessages] = useState([])
  const threadRef = useRef(null)
  const [targetUserId, setTargetUserId] = useState(location.state?.otherUserId || '')
  const [targetName, setTargetName] = useState(location.state?.otherName || '')
  const [input, setInput] = useState('')
  const inputRef = useRef(null)
  const [status, setStatus] = useState('connecting')
  const [error, setError] = useState('')
  const [connectionNotice, setConnectionNotice] = useState('')
  const wasDisconnectedRef = useRef(false)
  const reconnectTimerRef = useRef(null)
  const [loading, setLoading] = useState(true)

  function normalizeMessages(list) {
    return [...(list || [])].sort(
      (a, b) => new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime(),
    )
  }

  useEffect(() => {
    let mounted = true

    async function load() {
      setLoading(true)
      setError('')
      try {
        const data = await getChat(conversationId)
        if (mounted) {
          setChat(data)
          setMessages(normalizeMessages(data?.messages))
        }
      } catch (err) {
        if (mounted) {
          setError(err.message || 'Failed to load chat.')
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
  }, [conversationId, targetUserId])


  useEffect(() => {
    let mounted = true

    async function loadTarget() {
      if (targetName) return
      try {
        if (!targetUserId) {
          const chatList = await getChats()
          const match = (chatList || []).find((item) => item.conversationId === conversationId)
          if (mounted && match) {
            setTargetUserId(match.otherUserId || '')
            const name = `${match.otherFirstName || ''} ${match.otherLastName || ''}`.trim()
            if (name) {
              setTargetName(name)
              return
            }
          }
        }

        if (!mounted) return

        if (user?.role === 'CLIENT') {
          const coach = await getClientCoach()
          const name = `${coach?.firstName || ''} ${coach?.lastName || ''}`.trim()
          if (mounted && name) setTargetName(name)
        } else if (user?.role === 'COACH' && targetUserId) {
          const client = await getCoachClient(targetUserId)
          const name = `${client?.firstName || ''} ${client?.lastName || ''}`.trim()
          if (mounted && name) setTargetName(name)
        }
      } catch {
        // keep silent; fallback handled in UI
      }
    }

    loadTarget()
    return () => {
      mounted = false
    }
  }, [conversationId, targetUserId, targetName, user?.role])

  useEffect(() => {
    if (threadRef.current) {
      threadRef.current.scrollTop = threadRef.current.scrollHeight
    }
  }, [messages])

  useEffect(() => {
    const token = getToken()
    if (!token) {
      setStatus('disconnected')
      return undefined
    }

    const client = new Client({
      brokerURL: buildWebSocketUrl(),
      connectHeaders: {
        authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
    })

    client.onConnect = () => {
      setStatus('connected')
      if (wasDisconnectedRef.current) {
        setError('')
        setConnectionNotice('Chat connection is back online.')
        wasDisconnectedRef.current = false
        if (reconnectTimerRef.current) {
          clearTimeout(reconnectTimerRef.current)
        }
        reconnectTimerRef.current = setTimeout(() => {
          setConnectionNotice('')
        }, 4000)
      }

      client.subscribe('/user/queue/reply', (message) => {
        const payload = parseMessage(message)
        if (!payload) return
        if (typeof payload === 'string') {
          setMessages((prev) => [
            ...prev,
            {
              messageId: `${Date.now()}-${Math.random()}`,
              senderId: targetUserId || 'unknown',
              sentAt: new Date().toISOString(),
              content: payload,
            },
          ])
          return
        }

        if (Array.isArray(payload.messages)) {
          setMessages(normalizeMessages(payload.messages))
          return
        }

        if (payload.content) {
          setMessages((prev) => [
            ...prev,
            {
              messageId: payload.messageId || `${Date.now()}-${Math.random()}`,
              senderId: payload.senderId || targetUserId || 'unknown',
              sentAt: payload.sentAt || new Date().toISOString(),
              content: payload.content,
            },
          ])
        }
      })
    }

    client.onStompError = (frame) => {
      setStatus('error')
      setError(frame.headers?.message || 'WebSocket error.')
      setConnectionNotice('')
    }

    client.onWebSocketClose = () => {
      setStatus('disconnected')
      setConnectionNotice('')
      wasDisconnectedRef.current = true
      setError('Chat connection is offline. Try again in a moment.')
    }

    client.activate()
    clientRef.current = client

    return () => {
      if (reconnectTimerRef.current) {
        clearTimeout(reconnectTimerRef.current)
      }
      client.deactivate()
    }
  }, [conversationId])

  async function handleSend(event) {
    event.preventDefault()
    if (!input.trim()) return
    if (!targetUserId) {
      setError('Recipient missing for this conversation.')
      return
    }

    const content = input.trim()
    if (!clientRef.current || status !== 'connected') {
      setError('Chat connection is offline. Try again in a moment.')
      return
    }

    clientRef.current.publish({
      destination: '/app/chat',
      body: JSON.stringify({ to: targetUserId, content }),
    })

    setMessages((prev) => [
      ...prev,
      {
        messageId: `${Date.now()}-${Math.random()}`,
        senderId: user?.id,
        sentAt: new Date().toISOString(),
        content,
      },
    ])
    setInput('')
    if (inputRef.current) {
      inputRef.current.style.height = 'auto'
    }
  }

  const backPath = user?.role === 'COACH' ? '/coach' : '/client'

  return (
    <section className="panel">
      <div className="panel-header">
        <div className="header-stack">
          <div className="header-row header-row-chat">
            <div className="header-inline">
              <h1>Chat</h1>
            </div>
            <Link
              className="contact-link contact-link-center"
              to={user?.role === 'CLIENT' ? '/client/coach' : `/coach/clients/${targetUserId}`}
            >
              {targetName || 'Open profile'}
            </Link>
            <Link className="icon-button back-icon" to={backPath} aria-label="Back to chats" title="Back to chats">
              <ArrowUUpLeft size={22} weight="bold" />
            </Link>
          </div>
        </div>
      </div>
      {loading ? <p className="muted">Loading messages...</p> : null}
      {error ? <p className="error">{error}</p> : null}
      {connectionNotice ? <p className="success">{connectionNotice}</p> : null}
      {!loading && chat ? (
        <div className="chat-thread" ref={threadRef}>
          {messages.length === 0 ? (
            <p className="muted">No messages yet. Start the conversation below.</p>
          ) : (
            messages.map((message) => (
              <div
                key={message.messageId}
                className={`chat-bubble ${message.senderId === user?.id ? 'chat-self' : 'chat-peer'}`}
              >
                <div className="chat-meta">{formatMessageTime(message.sentAt)}</div>
                <p>{message.content}</p>
              </div>
            ))
          )}
        </div>
      ) : null}
      <form className="chat-input" onSubmit={handleSend}>
        <textarea
          ref={inputRef}
          className="chat-textarea"
          placeholder={targetUserId ? 'Type your message...' : 'Recipient unavailable'}
          value={input}
          onChange={(event) => {
            setInput(event.target.value)
            event.target.style.height = 'auto'
            event.target.style.height = `${event.target.scrollHeight}px`
          }}
          onKeyDown={(event) => {
            if (event.key === 'Enter' && !event.shiftKey) {
              event.preventDefault()
              handleSend(event)
            }
          }}
          rows={1}
          disabled={!targetUserId}
        />
        <button
          type="submit"
          className="send-button"
          disabled={!targetUserId || status !== 'connected'}
          aria-label="Send message"
        >
          <PaperPlaneRight size={22} weight="bold" />
        </button>
      </form>
    </section>
  )
}

export default ChatDetail
