import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { ChatsCircle, User } from 'phosphor-react'
import {
  getChatMessages,
  getChats,
  getClientCoach,
  getCoachClient,
  getCoachClients,
  startChat,
} from '../api.js'
import { useAuth } from '../authContext.js'
import { formatErrorMessage } from '../errorUtils.js'
import { Link } from 'react-router-dom'
import { CHAT_DOCK_OPEN_EVENT } from '../chatDockEvents.js'
import { useChatDockLayout } from './chat/useChatDockLayout.js'
import { useChatWebSocket } from './chat/useChatWebSocket.js'
import { ChatConversationList, ChatDockActions, ChatDockListToggle } from './chat/ChatConversationList.jsx'
import { ChatMessageThread } from './chat/ChatMessageThread.jsx'

function ChatDock() {
  const { user } = useAuth()
  const isCoach = user?.role === 'COACH'

  // Session-persisted state
  const [open, setOpen] = useState(() => {
    try {
      const stored = JSON.parse(sessionStorage.getItem('omniOne.chatDock') || '{}')
      return Boolean(stored.open)
    } catch {
      return false
    }
  })
  const [activeChatId, setActiveChatId] = useState(() => {
    try {
      const stored = JSON.parse(sessionStorage.getItem('omniOne.chatDock') || '{}')
      return stored.activeChatId || null
    } catch {
      return null
    }
  })
  const [activeTargetId, setActiveTargetId] = useState(() => {
    try {
      const stored = JSON.parse(sessionStorage.getItem('omniOne.chatDock') || '{}')
      return stored.activeTargetId || ''
    } catch {
      return ''
    }
  })
  const [activeTargetName, setActiveTargetName] = useState(() => {
    try {
      const stored = JSON.parse(sessionStorage.getItem('omniOne.chatDock') || '{}')
      return stored.activeTargetName || ''
    } catch {
      return ''
    }
  })

  // Chat list state
  const [chats, setChats] = useState([])
  const [loadingChats, setLoadingChats] = useState(false)
  const [chatError, setChatError] = useState('')

  // Messages state
  const [messages, setMessages] = useState([])
  const [messagesByConversation, setMessagesByConversation] = useState({})
  const [pagingByConversation, setPagingByConversation] = useState({})
  const [messageError, setMessageError] = useState('')
  const [loadingOlder, setLoadingOlder] = useState(false)

  // UI state
  const [input, setInput] = useState('')
  const [showStart, setShowStart] = useState(false)
  const [starting, setStarting] = useState(false)
  const [startTargets, setStartTargets] = useState([])
  const [showList, setShowList] = useState(true)
  const [hasNewMessage, setHasNewMessage] = useState(false)
  const [notifiedChatIds, setNotifiedChatIds] = useState([])

  // Refs
  const menuRef = useRef(null)
  const toggleRef = useRef(null)
  const threadRef = useRef(null)
  const inputRef = useRef(null)
  const clientRef = useRef(null)
  const activeChatIdRef = useRef(activeChatId)
  const activeTargetIdRef = useRef(activeTargetId)
  const activeTargetNameRef = useRef(activeTargetName)
  const openRef = useRef(open)
  const prevActiveChatIdRef = useRef(null)
  const lastScrollTopRef = useRef(0)
  const lastMessageMapRef = useRef(new Map())
  const isAtBottomRef = useRef(true)
  const pagingByConversationRef = useRef({})
  const loadingOlderRef = useRef(false)
  const pendingAnchorRef = useRef(null)
  const pendingToggleScrollRef = useRef(null)
  const pendingToggleAtBottomRef = useRef(false)
  const lastReadSentRef = useRef(new Map())
  const chatsRef = useRef([])
  const pendingMessagesRef = useRef(new Map())
  const lastSentMessageIdRef = useRef(null)
  const refreshChatsInFlightRef = useRef(false)
  const userScrollIntentRef = useRef(false)
  const userScrollIntentTimerRef = useRef(null)
  const lastNarrowChatRef = useRef(null)
  const lastLoadedChatIdRef = useRef(null)
  const forceScrollOnNextLoadRef = useRef(false)
  const pendingScrollToBottomRef = useRef(false)
  const switchGuardRef = useRef(false)
  const isChatUnreadRef = useRef(null)
  const listToggleTimerRef = useRef(null)
  const panelRef = useRef(null)

  const DEBUG_WS = import.meta.env.DEV
  const logWs = useCallback((...args) => {
    if (DEBUG_WS) {
      console.log('[chat-ws]', ...args)
    }
  }, [DEBUG_WS])

  const nearBottomThreshold = 80
  const pageSize = 25

  // Layout hook
  const {
    dockSize,
    setDockSize,
    dockPos,
    setDockPos,
    listWidth,
    setListWidth,
    panelWidth,
    setPanelWidth,
    handleDragStart,
    handleDividerMouseDown,
    handleResizeStart,
    handleDockReset,
    handleDividerReset,
  } = useChatDockLayout({
    open,
    panelRef,
    threadRef,
  })

  const isDockNarrow = panelWidth !== null && panelWidth < 570
  const isDockTiny = panelWidth !== null && panelWidth < 500

  // Message utilities
  const normalizeMessages = useCallback((list) => {
    const next = [...(list || [])]
    next.sort((a, b) => new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime())
    return next
  }, [])

  const getMessageKey = useCallback((message) => {
    return message?.clientMessageId || message?.messageId || ''
  }, [])

  const mergeMessages = useCallback((older, existing) => {
    const map = new Map()
    ;(existing || []).forEach((item) => {
      const key = getMessageKey(item)
      if (key) map.set(key, item)
    })
    ;(older || []).forEach((item) => {
      const key = getMessageKey(item)
      if (key && !map.has(key)) {
        map.set(key, item)
      }
    })
    return normalizeMessages([...map.values()])
  }, [getMessageKey, normalizeMessages])

  const updateMessageByClientId = useCallback((conversationId, clientMessageId, updater) => {
    if (!clientMessageId) return
    setMessages((prev) => {
      let changed = false
      const next = prev.map((item) => {
        if (item?.clientMessageId === clientMessageId) {
          changed = true
          return updater(item)
        }
        return item
      })
      return changed ? next : prev
    })
    setMessagesByConversation((prev) => {
      const list = prev[conversationId]
      if (!list) return prev
      let changed = false
      const nextList = list.map((item) => {
        if (item?.clientMessageId === clientMessageId) {
          changed = true
          return updater(item)
        }
        return item
      })
      return changed ? { ...prev, [conversationId]: nextList } : prev
    })
  }, [])

  const markPendingMessages = useCallback(() => {
    if (!pendingMessagesRef.current.size) return
    pendingMessagesRef.current.forEach((entry, clientMessageId) => {
      if (!entry?.conversationId) return
      pendingMessagesRef.current.set(clientMessageId, { ...entry, status: 'pending' })
      updateMessageByClientId(entry.conversationId, clientMessageId, (message) => ({
        ...message,
        status: 'pending',
      }))
    })
  }, [updateMessageByClientId])

  const scrollThreadToBottom = useCallback(() => {
    if (!threadRef.current) return
    threadRef.current.scrollTop = threadRef.current.scrollHeight
  }, [])

  const sendReadReceipt = useCallback((chatId) => {
    if (!chatId || !clientRef.current) return
    const threadVisible = openRef.current && (!isDockNarrow || !showList)
    if (!threadVisible) return
    const now = Date.now()
    const lastSent = lastReadSentRef.current.get(chatId) || 0
    if (now - lastSent < 1000) return
    lastReadSentRef.current.set(chatId, now)
    clientRef.current.publish({
      destination: '/app/chat.read',
      body: JSON.stringify(chatId),
    })
    logWs('read', { destination: '/app/chat.read', chatId })
    setChats((prev) =>
      prev.map((chat) =>
        chat.conversationId === chatId
          ? { ...chat, lastReadAt: new Date().toISOString() }
          : chat,
      ),
    )
    setNotifiedChatIds((prev) => prev.filter((id) => id !== chatId))
  }, [isDockNarrow, showList, logWs])

  // Sorted chats memo
  const sortedChats = useMemo(() => {
    return [...(chats || [])].sort((a, b) => {
      const aTime = a?.lastMessageAt ? new Date(a.lastMessageAt).getTime() : 0
      const bTime = b?.lastMessageAt ? new Date(b.lastMessageAt).getTime() : 0
      return bTime - aTime
    })
  }, [chats])

  // Sync refs
  useEffect(() => {
    pagingByConversationRef.current = pagingByConversation
  }, [pagingByConversation])

  useEffect(() => {
    chatsRef.current = chats || []
  }, [chats])

  useEffect(() => {
    activeChatIdRef.current = activeChatId
  }, [activeChatId])

  useEffect(() => {
    activeTargetIdRef.current = activeTargetId
  }, [activeTargetId])

  useEffect(() => {
    activeTargetNameRef.current = activeTargetName
  }, [activeTargetName])

  useEffect(() => {
    openRef.current = open
  }, [open])

  // Load older messages
  const loadOlderMessages = useCallback(async () => {
    const chatId = activeChatIdRef.current
    if (!chatId || loadingOlderRef.current) return
    const paging = pagingByConversationRef.current[chatId]
    if (!paging?.hasMore || !paging?.cursor) return
    if (!threadRef.current) return
    const prevScrollTop = threadRef.current.scrollTop
    const prevScrollHeight = threadRef.current.scrollHeight
    pendingAnchorRef.current = { chatId, prevScrollTop, prevScrollHeight }
    loadingOlderRef.current = true
    setLoadingOlder(true)
    try {
      const data = await getChatMessages(chatId, {
        size: pageSize,
        beforeSentAt: paging.cursor,
      })
      const incoming = normalizeMessages(data?.content || [])
      const nextCursor = incoming[0]?.sentAt || null
      const hasMore = data?.last === false
      setPagingByConversation((prev) => ({
        ...prev,
        [chatId]: { cursor: nextCursor, hasMore },
      }))
      setMessagesByConversation((prev) => {
        const existing = prev[chatId] || []
        const merged = mergeMessages(incoming, existing)
        return { ...prev, [chatId]: merged }
      })
      if (activeChatIdRef.current === chatId) {
        setMessages((prev) => mergeMessages(incoming, prev))
      }
    } catch (err) {
      setMessageError(err || 'Failed to load older messages.')
    } finally {
      loadingOlderRef.current = false
      setLoadingOlder(false)
    }
  }, [mergeMessages, normalizeMessages, pageSize])

  // Anchor adjustment after loading older messages
  useEffect(() => {
    if (!pendingAnchorRef.current || !threadRef.current) return
    const { chatId, prevScrollTop, prevScrollHeight } = pendingAnchorRef.current
    if (activeChatIdRef.current !== chatId) return
    requestAnimationFrame(() => {
      if (!threadRef.current) return
      const nextHeight = threadRef.current.scrollHeight
      const delta = nextHeight - prevScrollHeight
      threadRef.current.scrollTop = prevScrollTop + delta
      pendingAnchorRef.current = null
    })
  }, [messages])

  // Thread scroll handler
  function handleThreadScroll(event) {
    const target = event.currentTarget
    lastScrollTopRef.current = target.scrollTop
  }

  // Reset on logout
  useEffect(() => {
    if (!user?.id) {
      setOpen(false)
      setActiveChatId(null)
      setActiveTargetId('')
      setActiveTargetName('')
      setMessages([])
      setMessagesByConversation({})
      setHasNewMessage(false)
      setNotifiedChatIds([])
      lastMessageMapRef.current = new Map()
      sessionStorage.removeItem('omniOne.chatDock')
      return
    }
    setOpen(false)
    setActiveChatId(null)
    setActiveTargetId('')
    setActiveTargetName('')
    setMessages([])
    setMessagesByConversation({})
    setHasNewMessage(false)
    setNotifiedChatIds([])
    lastMessageMapRef.current = new Map()
    sessionStorage.removeItem('omniOne.chatDock')
  }, [user?.id])

  // Compute unread chats
  const isChatUnread = useCallback((chat) => {
    if (!chat?.lastMessageAt) return false
    if (chat?.lastMessageSenderId && chat.lastMessageSenderId === user?.id) {
      return false
    }
    if (!chat?.lastReadAt) return true
    return new Date(chat.lastMessageAt).getTime() > new Date(chat.lastReadAt).getTime()
  }, [user?.id])

  useEffect(() => {
    isChatUnreadRef.current = isChatUnread
  }, [isChatUnread])

  useEffect(() => {
    if (!user?.id || loadingChats) return
    if (!chats?.length) {
      setNotifiedChatIds([])
      return
    }
    const unreadIds = chats.filter((chat) => isChatUnread(chat)).map((chat) => chat.conversationId)
    setNotifiedChatIds(unreadIds)
  }, [user?.id, chats, isChatUnread, loadingChats])

  // Activity state
  useEffect(() => {
    if (activeChatId) {
      forceScrollOnNextLoadRef.current = true
    }
  }, [activeChatId])

  useEffect(() => {
    loadingOlderRef.current = false
    setLoadingOlder(false)
    pendingAnchorRef.current = null
  }, [activeChatId])

  useEffect(() => {
    if (!activeChatId) return
    switchGuardRef.current = true
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        switchGuardRef.current = false
      })
    })
  }, [activeChatId])

  useEffect(() => {
    prevActiveChatIdRef.current = activeChatId
  }, [activeChatId])

  useEffect(() => {
    if (open) {
      lastLoadedChatIdRef.current = null
      pendingScrollToBottomRef.current = false
    }
  }, [open])

  // Auto-scroll to bottom
  useEffect(() => {
    if (!pendingScrollToBottomRef.current || !threadRef.current) return
    pendingScrollToBottomRef.current = false
    scrollThreadToBottom()
  }, [messages, open, showList, isDockNarrow, activeChatId, scrollThreadToBottom])

  // Dock narrow responsive behavior
  useEffect(() => {
    if (isDockNarrow) {
      setShowList(false)
    }
    if (isDockNarrow && !activeChatId) {
      setShowList(true)
    }
  }, [isDockNarrow, activeChatId])

  // Session persistence
  useEffect(() => {
    sessionStorage.setItem(
      'omniOne.chatDock',
      JSON.stringify({
        open,
        activeChatId,
        activeTargetId,
        activeTargetName,
      }),
    )
  }, [open, activeChatId, activeTargetId, activeTargetName])

  useEffect(() => {
    if (!open) return
    return undefined
  }, [open])

  // Initial loading state
  useEffect(() => {
    if (!user?.id) return
    setLoadingChats(true)
    return () => {
      setLoadingChats(false)
    }
  }, [user?.id])

  // Load conversation messages
  useEffect(() => {
    if (!open || !activeChatId) return
    let mounted = true

    async function loadConversation() {
      setMessageError('')
      try {
        if (messagesByConversation[activeChatId]) {
          setMessages(messagesByConversation[activeChatId])
          if (!pagingByConversationRef.current[activeChatId]) {
            setPagingByConversation((prev) => ({
              ...prev,
              [activeChatId]: { cursor: null, hasMore: false },
            }))
          }
          const shouldAutoScroll =
            forceScrollOnNextLoadRef.current ||
            lastLoadedChatIdRef.current !== activeChatId ||
            isAtBottomRef.current
          if (shouldAutoScroll) {
            pendingScrollToBottomRef.current = true
          }
          forceScrollOnNextLoadRef.current = false
          lastLoadedChatIdRef.current = activeChatId
          return
        }
        const data = await getChatMessages(activeChatId, { size: pageSize })
        if (!mounted) return
        const ordered = normalizeMessages(data?.content || [])
        const nextCursor = ordered[0]?.sentAt || null
        const hasMore = data?.last === false
        setMessagesByConversation((prev) => ({ ...prev, [activeChatId]: ordered }))
        setMessages(ordered)
        setPagingByConversation((prev) => ({
          ...prev,
          [activeChatId]: { cursor: nextCursor, hasMore },
        }))
        pendingScrollToBottomRef.current = true
        forceScrollOnNextLoadRef.current = false
        lastLoadedChatIdRef.current = activeChatId
      } catch (err) {
        if (mounted) {
          setMessageError(err || 'Failed to load messages.')
        }
      }
    }

    loadConversation()
    return () => {
      mounted = false
    }
  }, [open, activeChatId, messagesByConversation, normalizeMessages, pageSize])

  // Toggle list
  const handleToggleList = useCallback(() => {
    if (listToggleTimerRef.current) {
      clearTimeout(listToggleTimerRef.current)
    }
    if (isDockNarrow && threadRef.current) {
      const { scrollTop, scrollHeight, clientHeight } = threadRef.current
      pendingToggleScrollRef.current = scrollTop
      pendingToggleAtBottomRef.current = scrollTop + clientHeight >= scrollHeight - nearBottomThreshold
    }
    setShowList((prev) => {
      const next = !prev
      if (isDockNarrow) {
        if (next) {
          lastNarrowChatRef.current = {
            chatId: activeChatIdRef.current,
            targetId: activeTargetIdRef.current,
            targetName: activeTargetName,
          }
          setActiveChatId(null)
          setActiveTargetId('')
          setActiveTargetName('')
        } else if (lastNarrowChatRef.current?.chatId) {
          const saved = lastNarrowChatRef.current
          setActiveChatId(saved.chatId)
          let nextTargetId = saved.targetId || ''
          let nextTargetName = saved.targetName || ''
          if (!nextTargetId || !nextTargetName) {
            const match = chatsRef.current.find(
              (chat) => chat.conversationId === saved.chatId,
            )
            if (match) {
              nextTargetId = match.otherUserId || nextTargetId
              nextTargetName = `${match.otherFirstName || ''} ${match.otherLastName || ''}`.trim()
            }
          }
          setActiveTargetId(nextTargetId)
          setActiveTargetName(nextTargetName)
        }
      }
      return next
    })
    requestAnimationFrame(() => {
      if (!threadRef.current) return
      if (pendingToggleScrollRef.current != null) {
        threadRef.current.scrollTop = pendingToggleScrollRef.current
      }
      pendingToggleScrollRef.current = null
      pendingToggleAtBottomRef.current = false
    })
  }, [activeTargetName, isDockNarrow, nearBottomThreshold])

  // Initialize WebSocket
  useChatWebSocket({
    userId: user?.id,
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
  })

  // Close start menu on outside click
  useEffect(() => {
    if (!showStart || !isCoach) return
    function handleClickOutside(event) {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setShowStart(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
    }
  }, [showStart, isCoach])

  // Select chat
  const selectChat = useCallback(
    (chat, overrideName) => {
      if (!chat) return
      switchGuardRef.current = true
      setActiveChatId(chat.conversationId)
      setActiveTargetId(chat.otherUserId)
      const fallbackName = `${chat.otherFirstName || ''} ${chat.otherLastName || ''}`.trim()
      setActiveTargetName(overrideName || fallbackName)
      if (isDockNarrow) {
        setShowList(false)
      }
      setNotifiedChatIds((prev) => prev.filter((id) => id !== chat.conversationId))
      sendReadReceipt(chat.conversationId)
      requestAnimationFrame(() => {
        scrollThreadToBottom()
      })
    },
    [isDockNarrow, sendReadReceipt, scrollThreadToBottom],
  )

  // Open chat with target
  const openChatWithTarget = useCallback(
    async (targetId, targetName) => {
      if (!targetId) return
      setOpen(true)
      setChatError('')
      const existing = chatsRef.current.find((chat) => chat.otherUserId === targetId)
      if (existing) {
        selectChat(existing, targetName)
        return
      }
      try {
        const chat = await startChat(targetId)
        const list = await getChats()
        setChats(list || [])
        setActiveChatId(chat.conversationId)
        setActiveTargetId(targetId)
        if (targetName) {
          setActiveTargetName(targetName)
        } else if (isCoach) {
          const client = await getCoachClient(targetId)
          setActiveTargetName(`${client?.firstName || ''} ${client?.lastName || ''}`.trim())
        } else {
          const coach = await getClientCoach()
          setActiveTargetName(`${coach?.firstName || ''} ${coach?.lastName || ''}`.trim())
        }
      } catch (err) {
        setChatError(err || 'Failed to start chat.')
      }
    },
    [isCoach, selectChat],
  )

  // Handle chat dock open event
  useEffect(() => {
    function handleOpen(event) {
      const detail = event?.detail || {}
      if (!detail.targetId) return
      openChatWithTarget(detail.targetId, detail.targetName)
    }
    window.addEventListener(CHAT_DOCK_OPEN_EVENT, handleOpen)
    return () => {
      window.removeEventListener(CHAT_DOCK_OPEN_EVENT, handleOpen)
    }
  }, [openChatWithTarget])

  // Start new chat
  async function handleStartChat() {
    if (starting) return
    setChatError('')
    if (isCoach) {
      if (showStart) {
        setShowStart(false)
        return
      }
      setStarting(true)
      try {
        const list = await getCoachClients()
        const existing = new Set((chats || []).map((chat) => chat.otherUserId))
        const filtered = (list || []).filter((client) => !existing.has(client.id))
        setStartTargets(filtered)
        setShowStart(true)
      } catch (err) {
        setChatError(err || 'Failed to load clients.')
      } finally {
        setStarting(false)
      }
      return
    }

    setStarting(true)
    try {
      const coach = await getClientCoach()
      if (!coach?.id) {
        setChatError('No coach assigned yet.')
        setStarting(false)
        return
      }
      const chat = await startChat(coach.id)
      const list = await getChats()
      setChats(list || [])
      setActiveChatId(chat.conversationId)
      setActiveTargetId(coach.id)
      setActiveTargetName(`${coach.firstName || ''} ${coach.lastName || ''}`.trim())
    } catch (err) {
      setChatError(err || 'Failed to start chat.')
    } finally {
      setStarting(false)
    }
  }

  // Start chat with specific target
  async function handleStartWithTarget(targetId) {
    setChatError('')
    setStarting(true)
    try {
      const chat = await startChat(targetId)
      const list = await getChats()
      setChats(list || [])
      setActiveChatId(chat.conversationId)
      setActiveTargetId(targetId)
      const client = await getCoachClient(targetId)
      setActiveTargetName(`${client?.firstName || ''} ${client?.lastName || ''}`.trim())
      setShowStart(false)
    } catch (err) {
      setChatError(err || 'Failed to start chat.')
    } finally {
      setStarting(false)
    }
  }

  // Handle message send
  function handleSend(event) {
    event.preventDefault()
    if (!input.trim() || !activeTargetId) return
    const isConnected = Boolean(clientRef.current && clientRef.current.connected)

    const content = input.trim()
    const clientMessageId = crypto?.randomUUID
      ? crypto.randomUUID()
      : `${Date.now()}-${Math.random()}`
    const optimisticMessage = {
      clientMessageId,
      messageId: clientMessageId,
      senderId: user?.id,
      sentAt: new Date().toISOString(),
      content,
      status: isConnected ? 'sending' : 'pending',
      errorMessage: '',
    }

    if (isConnected) {
      clientRef.current.publish({
        destination: '/app/chat.send',
        body: JSON.stringify({ clientMessageId, to: activeTargetId, content }),
      })
      logWs('send', {
        destination: '/app/chat.send',
        chatId: activeChatId,
        to: activeTargetId,
        content,
        clientMessageId,
      })
    }

    if (activeChatId) {
      pendingMessagesRef.current.set(clientMessageId, {
        conversationId: activeChatId,
        payload: { clientMessageId, to: activeTargetId, content },
        status: isConnected ? 'sending' : 'pending',
      })
      lastSentMessageIdRef.current = clientMessageId
    }

    setMessages((prev) => [...prev, optimisticMessage])
    requestAnimationFrame(() => {
      scrollThreadToBottom()
    })
    if (activeChatId) {
      const now = optimisticMessage.sentAt
      setChats((prev) => {
        const next = prev.map((chat) =>
          chat.conversationId === activeChatId
            ? {
                ...chat,
                lastMessagePreview: content,
                lastMessageAt: now,
                lastReadAt: now,
                lastMessageSenderId: user?.id || chat.lastMessageSenderId,
              }
            : chat,
        )
        return next
      })
      setMessagesByConversation((prev) => {
        const existing = prev[activeChatId] || []
        const next = [...existing, optimisticMessage].sort(
          (a, b) => new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime(),
        )
        return { ...prev, [activeChatId]: next }
      })
    }
    setInput('')
    if (inputRef.current) {
      inputRef.current.style.height = 'auto'
    }
    if (activeChatId) {
      sendReadReceipt(activeChatId)
      setNotifiedChatIds((prev) => prev.filter((id) => id !== activeChatId))
    }
  }

  // Handle input change
  function handleInputChange(event) {
    setInput(event.target.value)
    event.target.style.height = 'auto'
    event.target.style.height = `${event.target.scrollHeight}px`
  }

  // Handle input key down
  function handleInputKeyDown(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      handleSend(event)
    }
  }

  // Handle thread wheel
  function handleThreadWheel() {
    userScrollIntentRef.current = true
    if (userScrollIntentTimerRef.current) {
      clearTimeout(userScrollIntentTimerRef.current)
    }
    userScrollIntentTimerRef.current = setTimeout(() => {
      userScrollIntentRef.current = false
    }, 200)
  }

  // Handle thread touch move
  function handleThreadTouchMove() {
    userScrollIntentRef.current = true
    if (userScrollIntentTimerRef.current) {
      clearTimeout(userScrollIntentTimerRef.current)
    }
    userScrollIntentTimerRef.current = setTimeout(() => {
      userScrollIntentRef.current = false
    }, 200)
  }

  // Handle thread scroll
  function handleThreadScroll(event) {
    handleThreadScroll(event)
    if (!activeChatId) return
    if (switchGuardRef.current) return
    if (event.currentTarget.scrollTop <= 120) {
      loadOlderMessages()
    }
    const { scrollTop, scrollHeight, clientHeight } = event.currentTarget
    const atBottom = scrollTop + clientHeight >= scrollHeight - nearBottomThreshold
    isAtBottomRef.current = atBottom
    if (atBottom && event.nativeEvent?.isTrusted) {
      setNotifiedChatIds((prev) => prev.filter((id) => id !== activeChatId))
      sendReadReceipt(activeChatId)
    }
  }

  function handleToggleDock() {
    setOpen((prev) => !prev)
  }

  return (
    <div className="chat-dock">
      <button
        type="button"
        className={`chat-dock-toggle${open ? ' is-active' : ''}${hasNewMessage ? ' is-notified' : ''}`}
        onClick={handleToggleDock}
        aria-label="Open chats"
        title="Chat"
        ref={toggleRef}
        aria-pressed={open}
      >
        <ChatsCircle size={28} weight="bold" />
        <span className="chat-toggle-text">Chat</span>
      </button>
      {open ? (
        <div
          className={`chat-dock-panel${isDockNarrow ? ' is-narrow' : ''}${isDockTiny ? ' is-tiny' : ''}`}
          ref={panelRef}
          style={
            dockSize || dockPos
              ? {
                  ...(dockSize
                    ? {
                        width: `${dockSize.width}px`,
                        height: `${dockSize.height}px`,
                      }
                    : {}),
                  ...(dockPos
                    ? {
                        left: `${dockPos.x}px`,
                        top: `${dockPos.y}px`,
                        right: 'auto',
                        bottom: 'auto',
                      }
                    : {}),
                }
              : undefined
          }
        >
          <div className="chat-dock-header" onMouseDown={handleDragStart} onDoubleClick={handleDockReset}>
            <div className="chat-dock-left">
              <ChatDockListToggle
                showList={showList}
                notifiedChatIds={notifiedChatIds}
                onToggleList={handleToggleList}
              />
              <ChatDockActions
                isCoach={isCoach}
                showStart={showStart}
                starting={starting}
                startTargets={startTargets}
                onStartChat={handleStartChat}
                onStartWithTarget={handleStartWithTarget}
                menuRef={menuRef}
                notifiedChatIds={notifiedChatIds}
              />
            </div>
            <div className="chat-dock-title">
              {activeChatId && activeTargetName ? (
                <span aria-hidden="true" />
              ) : null}
            </div>
            <div className="chat-dock-actions">
              {activeChatId && activeTargetName ? (
                <Link
                  className="chat-dock-contact"
                  to={isCoach ? `/coach/clients/${activeTargetId}` : '/client/coach'}
                  onClick={() => setOpen(false)}
                >
                  <User size={22} weight="bold" />
                  {activeTargetName}
                </Link>
              ) : null}
            </div>
          </div>
          {chatError ? <p className="error">{formatErrorMessage(chatError)}</p> : null}
          <div
            className={`chat-dock-body${showList ? '' : ' is-list-hidden'}${
              isDockNarrow ? (showList ? ' is-list-only' : ' is-thread-only') : ''
            }`}
            style={
              !isDockNarrow && showList
                ? {
                    gridTemplateColumns: `${Math.max(200, listWidth)}px 4px minmax(280px, 1fr)`,
                  }
                : undefined
            }
          >
            <ChatConversationList
              loadingChats={loadingChats}
              sortedChats={sortedChats}
              activeChatId={activeChatId}
              notifiedChatIds={notifiedChatIds}
              isCoach={isCoach}
              showStart={showStart}
              starting={starting}
              startTargets={startTargets}
              onSelectChat={selectChat}
              onStartChat={handleStartChat}
              onStartWithTarget={handleStartWithTarget}
              chatError={chatError}
              menuRef={menuRef}
            />
            {!isDockNarrow && showList ? (
              <div
                className="chat-dock-divider"
                onMouseDown={handleDividerMouseDown}
                onDoubleClick={handleDividerReset}
              />
            ) : null}
            <ChatMessageThread
              activeChatId={activeChatId}
              activeTargetId={activeTargetId}
              messages={messages}
              messageError={messageError}
              loadingOlder={loadingOlder}
              input={input}
              userId={user?.id}
              threadRef={threadRef}
              inputRef={inputRef}
              onScroll={handleThreadScroll}
              onSend={handleSend}
              onInputChange={handleInputChange}
              onKeyDown={handleInputKeyDown}
              onWheel={handleThreadWheel}
              onTouchMove={handleThreadTouchMove}
              nearBottomThreshold={nearBottomThreshold}
            />
          </div>
          <div
            className="chat-dock-resize"
            onMouseDown={handleResizeStart}
            onDoubleClick={() => {
              if (panelRef.current) {
                const rect = panelRef.current.getBoundingClientRect()
                const inset = window.innerWidth <= 720 ? 20 : 36
                const minTop = 96
                const minHeight = 320
                const maxWidth = window.innerWidth - inset * 2
                const defaultWidth = Math.min(window.innerWidth * 0.92, 860, maxWidth)
                const defaultHeight = Math.max(minHeight, window.innerHeight - 220)
                const nextX = Math.max(inset, rect.right - defaultWidth)
                const nextY = Math.max(minTop, rect.bottom - defaultHeight)
                setDockPos({ x: nextX, y: nextY })
                setDockSize({ width: defaultWidth, height: defaultHeight })
              }
              localStorage.removeItem('omniOne.chatDockSize')
            }}
            role="presentation"
            aria-hidden="true"
          />
        </div>
      ) : null}
    </div>
  )
}

export default ChatDock
