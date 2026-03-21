import { useCallback, useEffect } from 'react'
import { Client } from '@stomp/stompjs'
import {
  getChatMessages,
  getChats,
  getClientCoach,
  getCoachClient,
  getCoachClients,
  startChat,
} from '../../api.js'
import { getToken } from '../../auth.js'
import { formatErrorMessage } from '../../errorUtils.js'
import { buildWebSocketUrl } from './chatUtils.js'

export function useChatWebSocket({
  userId,
  isCoach,
  activeChatId,
  activeTargetId,
  activeTargetName,
  open,
  messages,
  chats,
  isDockNarrow,
  showList,
  nearBottomThreshold,
  pageSize,
  setChats,
  setMessages,
  setMessagesByConversation,
  setNotifiedChatIds,
  setChatError,
  setLoadingChats,
  setActiveChatId,
  setActiveTargetId,
  setActiveTargetName,
  setMessageError,
  setHasNewMessage,
  threadRef,
  clientRef,
  activeChatIdRef,
  activeTargetIdRef,
  activeTargetNameRef,
  openRef,
  chatsRef,
  isAtBottomRef,
  pendingMessagesRef,
  lastReadSentRef,
  lastSentMessageIdRef,
  refreshChatsInFlightRef,
  lastMessageMapRef,
  pagingByConversationRef,
  isChatUnreadRef,
  updateMessageByClientId,
  markPendingMessages,
  scrollThreadToBottom,
  sendReadReceipt,
  logWs,
  DEBUG_WS,
}) {
  const logWsRef = { current: logWs }

  useEffect(() => {
    logWsRef.current = logWs
  }, [logWs])

  const isChatUnread = useCallback((chat) => {
    if (!chat?.lastMessageAt) return false
    if (chat?.lastMessageSenderId && chat.lastMessageSenderId === userId) {
      return false
    }
    if (!chat?.lastReadAt) return true
    return new Date(chat.lastMessageAt).getTime() > new Date(chat.lastReadAt).getTime()
  }, [userId])

  useEffect(() => {
    isChatUnreadRef.current = isChatUnread
  }, [isChatUnread])

  const refreshChatsList = useCallback(async () => {
    if (refreshChatsInFlightRef.current) return
    refreshChatsInFlightRef.current = true
    try {
      const list = await getChats()
      setChats(list || [])
      updateLastMessageMap(list || [])
    } catch {
      // ignore refresh errors
    } finally {
      refreshChatsInFlightRef.current = false
    }
  }, [setChats])

  const updateLastMessageMap = useCallback((list) => {
    const nextMap = new Map()
    ;(list || []).forEach((chat) => {
      const time = chat?.lastMessageAt ? new Date(chat.lastMessageAt).getTime() : 0
      nextMap.set(chat.conversationId, time)
    })
    lastMessageMapRef.current = nextMap
  }, [lastMessageMapRef])

  useEffect(() => {
    const token = getToken()
    if (!token) return

    const client = new Client({
      brokerURL: buildWebSocketUrl(),
      connectHeaders: {
        authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
    })

    client.onStompError = (frame) => {
      logWsRef.current?.('stomp-error', {
        message: frame?.headers?.message || '',
        body: frame?.body || '',
      })
    }

    client.onWebSocketError = (event) => {
      logWsRef.current?.('ws-error', { type: event?.type || 'error' })
      markPendingMessages?.()
    }

    client.onWebSocketClose = () => {
      logWsRef.current?.('ws-close', { userId })
      markPendingMessages?.()
    }

    client.onUnhandledMessage = (message) => {
      logWsRef.current?.('unhandled', {
        destination: message?.headers?.destination || '',
        body: message?.body || '',
      })
    }

    client.onConnect = () => {
      logWsRef.current?.('connect', { userId })
      logWsRef.current?.('subscribe', { destination: '/user/queue/reply' })
      client.subscribe('/user/queue/reply', (message) => {
        try {
          logWsRef.current?.('recv', {
            destination: '/user/queue/reply',
            size: message?.body?.length || 0,
            body: message?.body || null,
          })
          const incoming = message?.body ? JSON.parse(message.body) : null
          if (!incoming?.conversationId || !incoming?.messageId || !incoming?.sentAt) return

          const currentChatId = activeChatIdRef.current
          const isActiveChat = currentChatId && incoming.conversationId === currentChatId
          const isSelfMessage = incoming.senderId && incoming.senderId === userId
          const knownChat = chatsRef.current.some(
            (chat) => chat.conversationId === incoming.conversationId,
          )
          if (!knownChat) {
            refreshChatsList()
          }

          if (isActiveChat) {
            let shouldAutoScroll = false
            if (threadRef.current) {
              const { scrollTop, scrollHeight, clientHeight } = threadRef.current
              const atBottom = scrollTop + clientHeight >= scrollHeight - nearBottomThreshold
              isAtBottomRef.current = atBottom
              shouldAutoScroll = atBottom
            }
            setMessages((prev) => {
              if (prev.some((message) => message.messageId === incoming.messageId)) {
                return prev
              }
              const next = [...prev, incoming]
              next.sort((a, b) => new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime())
              return next
            })
            if (openRef.current && shouldAutoScroll) {
              requestAnimationFrame(() => {
                scrollThreadToBottom?.()
              })
            }
          }
          setMessagesByConversation((prev) => {
            const existing = prev[incoming.conversationId]
            if (!existing) return prev
            if (existing.some((message) => message.messageId === incoming.messageId)) {
              return prev
            }
            const next = [...existing, incoming].sort(
              (a, b) => new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime(),
            )
            return { ...prev, [incoming.conversationId]: next }
          })

          setChats((prev) => {
            const next = prev.map((chat) =>
              chat.conversationId === incoming.conversationId
                ? {
                    ...chat,
                    lastMessageAt: incoming.sentAt,
                    lastMessagePreview: incoming.content,
                    lastMessageSenderId: incoming.senderId || chat.lastMessageSenderId,
                    lastReadAt:
                      isActiveChat && openRef.current && isAtBottomRef.current
                        ? incoming.sentAt
                        : chat.lastReadAt,
                  }
                : chat,
            )
            updateLastMessageMap(next)
            return next
          })

          setNotifiedChatIds((prev) => {
            if (isSelfMessage) {
              return prev.filter((id) => id !== incoming.conversationId)
            }
            if (isActiveChat && openRef.current && isAtBottomRef.current) {
              return prev.filter((id) => id !== incoming.conversationId)
            }
            return Array.from(new Set([...prev, incoming.conversationId]))
          })
          if (isSelfMessage && activeChatIdRef.current === incoming.conversationId) {
            setHasNewMessage(false)
          }
          if (isActiveChat && openRef.current && isAtBottomRef.current) {
            sendReadReceipt?.(incoming.conversationId)
          }
        } catch {
          // ignore refresh errors
        }
      })

      logWsRef.current?.('subscribe', { destination: '/user/queue/acks' })
      client.subscribe('/user/queue/acks', (message) => {
        try {
          const payload = message?.body ? JSON.parse(message.body) : null
          const clientMessageId = payload?.clientMessageId
          const conversationId = payload?.conversationId
          if (!clientMessageId || !conversationId) return
          pendingMessagesRef.current.delete(clientMessageId)
          updateMessageByClientId?.(conversationId, clientMessageId, (item) => ({
            ...item,
            messageId: payload?.messageId ?? item.messageId,
            sentAt: payload?.sentAt || item.sentAt,
            status: 'sent',
            errorMessage: '',
          }))
          setChats((prev) => {
            const next = prev.map((chat) =>
              chat.conversationId === conversationId
                ? {
                    ...chat,
                    lastMessageAt: payload?.sentAt || chat.lastMessageAt,
                    lastReadAt: payload?.sentAt || chat.lastReadAt,
                    lastMessageSenderId: userId || chat.lastMessageSenderId,
                  }
                : chat,
            )
            updateLastMessageMap(next)
            return next
          })
        } catch {
          // ignore ack parse errors
        }
      })

      logWsRef.current?.('subscribe', { destination: '/user/queue/errors' })
      client.subscribe('/user/queue/errors', (message) => {
        try {
          const payload = message?.body ? JSON.parse(message.body) : null
          const clientMessageId = payload?.clientMessageId || lastSentMessageIdRef.current
          const pendingEntry = clientMessageId ? pendingMessagesRef.current.get(clientMessageId) : null
          const conversationId = pendingEntry?.conversationId || null
          const errorDetail =
            payload?.message ||
            (payload?.fieldErrors ? Object.values(payload.fieldErrors)[0] : '') ||
            'Message failed to send.'
          const errorMessage = formatErrorMessage({
            detail: errorDetail,
            traceId: payload?.traceId,
          })
          if (clientMessageId && conversationId) {
            pendingMessagesRef.current.delete(clientMessageId)
            updateMessageByClientId?.(conversationId, clientMessageId, (item) => ({
              ...item,
              status: 'failed',
              errorMessage,
            }))
          }
        } catch {
          // ignore error parse failures
        }
      })

      ;(async () => {
        try {
          const list = await getChats()
          setChats(list || [])
          updateLastMessageMap(list || [])
          if (userId) {
            const unreadIds = (list || [])
              .filter((chat) => isChatUnreadRef.current?.(chat))
              .map((chat) => chat.conversationId)
            if (unreadIds.length) {
              setNotifiedChatIds((prev) => Array.from(new Set([...prev, ...unreadIds])))
            }
          }
          if (activeChatIdRef.current && !activeTargetNameRef.current) {
            const match = (list || []).find((item) => item.conversationId === activeChatIdRef.current)
            if (match) {
              const name = `${match.otherFirstName || ''} ${match.otherLastName || ''}`.trim()
              setActiveTargetId(match.otherUserId || '')
              setActiveTargetName(name)
            }
          }
        } catch (err) {
          setChatError(err || 'Failed to load chats.')
        } finally {
          setLoadingChats(false)
        }
      })()

      if (pendingMessagesRef.current.size) {
        pendingMessagesRef.current.forEach((entry, clientMessageId) => {
          if (!entry?.payload || !entry?.conversationId) return
          const { payload, conversationId } = entry
          client.publish({
            destination: '/app/chat.send',
            body: JSON.stringify(payload),
          })
          pendingMessagesRef.current.set(clientMessageId, { ...entry, status: 'sending' })
          updateMessageByClientId?.(conversationId, clientMessageId, (item) => ({
            ...item,
            status: 'sending',
            errorMessage: '',
          }))
          lastSentMessageIdRef.current = clientMessageId
        })
      }
    }

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
    }
  }, [userId])
}
