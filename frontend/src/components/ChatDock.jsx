import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react'
import { ChatsCircle, PaperPlaneRight, Plus, UserList, User } from 'phosphor-react'
import {
  getChatMessages,
  getChats,
  getClientCoach,
  getCoachClient,
  getCoachClients,
  startChat,
} from '../api.js'
import { getToken } from '../auth.js'
import { useAuth } from '../authContext.js'
import { Client } from '@stomp/stompjs'
import { Link } from 'react-router-dom'
import { CHAT_DOCK_OPEN_EVENT } from '../chatDockEvents.js'

function buildWebSocketUrl() {
  const apiBase = import.meta.env.VITE_API_BASE || 'http://localhost:8080'
  const wsBase = apiBase.replace(/^http/, 'ws')
  return `${wsBase}/ws`
}

function formatChatTimestamp(value) {
  if (!value) return ''
  const date = new Date(value)
  const now = new Date()
  const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const startOfDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const dayDiff = Math.round((startOfToday - startOfDate) / (1000 * 60 * 60 * 24))
  const time = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })

  if (dayDiff === 0) return `Today ${time}`
  if (dayDiff === 1) return `Yesterday ${time}`

  const startOfWeek = new Date(startOfToday)
  startOfWeek.setDate(startOfWeek.getDate() - ((startOfWeek.getDay() + 6) % 7))

  if (startOfDate >= startOfWeek) {
    const weekday = date.toLocaleDateString([], { weekday: 'long' })
    return `${weekday} ${time}`
  }

  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  return `${day}.${month}.${year} ${time}`
}

function formatMessageTime(value) {
  if (!value) return ''
  return new Date(value).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function formatMessageDay(value) {
  if (!value) return ''
  const date = new Date(value)
  const now = new Date()
  const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const startOfDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const dayDiff = Math.round((startOfToday - startOfDate) / (1000 * 60 * 60 * 24))

  if (dayDiff === 0) return 'Today'
  if (dayDiff === 1) return 'Yesterday'

  const startOfWeek = new Date(startOfToday)
  startOfWeek.setDate(startOfWeek.getDate() - ((startOfWeek.getDay() + 6) % 7))

  if (startOfDate >= startOfWeek) {
    return date.toLocaleDateString('en-US', { weekday: 'long' })
  }

  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  return `${day}.${month}.${year}`
}

function ChatDock() {
  const { user } = useAuth()
  const isCoach = user?.role === 'COACH'
  const [open, setOpen] = useState(() => {
    try {
      const stored = JSON.parse(sessionStorage.getItem('omniOne.chatDock') || '{}')
      return Boolean(stored.open)
    } catch {
      return false
    }
  })
  const [chats, setChats] = useState([])
  const [loadingChats, setLoadingChats] = useState(false)
  const [chatError, setChatError] = useState('')
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
  const [messages, setMessages] = useState([])
  const [messagesByConversation, setMessagesByConversation] = useState({})
  const [pagingByConversation, setPagingByConversation] = useState({})
  const [messageError, setMessageError] = useState('')
  const [input, setInput] = useState('')
  const [startTargets, setStartTargets] = useState([])
  const [showStart, setShowStart] = useState(false)
  const [starting, setStarting] = useState(false)
  const [showList, setShowList] = useState(true)
  const [loadingOlder, setLoadingOlder] = useState(false)
  const [hasNewMessage, setHasNewMessage] = useState(false)
  const [notifiedChatIds, setNotifiedChatIds] = useState([])
  const [dockSize, setDockSize] = useState(() => {
    try {
      const stored = JSON.parse(localStorage.getItem('omniOne.chatDockSize') || 'null')
      return stored && stored.width && stored.height ? stored : null
    } catch {
      return null
    }
  })
  const [listWidth, setListWidth] = useState(() => {
    try {
      const stored = JSON.parse(localStorage.getItem('omniOne.chatDockListWidth') || 'null')
      return typeof stored === 'number' ? stored : 320
    } catch {
      return 320
    }
  })
  const [dockPos, setDockPos] = useState(() => {
    try {
      const stored = JSON.parse(localStorage.getItem('omniOne.chatDockPos') || 'null')
      return stored && typeof stored.x === 'number' && typeof stored.y === 'number' ? stored : null
    } catch {
      return null
    }
  })
  const [isResizing, setIsResizing] = useState(false)
  const [isResizingDivider, setIsResizingDivider] = useState(false)
  const [isDragging, setIsDragging] = useState(false)
  const [panelWidth, setPanelWidth] = useState(null)
  const menuRef = useRef(null)
  const toggleRef = useRef(null)
  const threadRef = useRef(null)
  const panelRef = useRef(null)
  const inputRef = useRef(null)
  const clientRef = useRef(null)
  const activeChatIdRef = useRef(activeChatId)
  const activeTargetIdRef = useRef(activeTargetId)
  const openRef = useRef(open)
  const prevActiveChatIdRef = useRef(null)
  const lastScrollTopRef = useRef(0)
  const lastMessageMapRef = useRef(new Map())
  const resizeStartRef = useRef(null)
  const listToggleTimerRef = useRef(null)
  const dragStartRef = useRef(null)
  const isAtBottomRef = useRef(true)
  const dividerDragRef = useRef(null)
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
  const DEBUG_WS = import.meta.env.DEV
  const logWs = useCallback((...args) => {
    if (DEBUG_WS) {
      console.log('[chat-ws]', ...args)
    }
  }, [DEBUG_WS])
  const nearBottomThreshold = 80
  const switchGuardRef = useRef(false)
  const isDockNarrow = panelWidth !== null && panelWidth < 570
  const isDockTiny = panelWidth !== null && panelWidth < 500
  const pageSize = 25
  const prevMetricsRef = useRef(null)
  const logWsRef = useRef(logWs)
  const refreshChatsListRef = useRef(null)
  const markPendingMessagesRef = useRef(null)
  const updateMessageByClientIdRef = useRef(null)
  const isChatUnreadRef = useRef(null)
  const scrollThreadToBottomRef = useRef(null)
  const sendReadReceiptRef = useRef(null)
  const activeTargetNameRef = useRef(activeTargetName)

  useEffect(() => {
    logWsRef.current = logWs
  }, [logWs])

  const sortedChats = useMemo(() => {
    return [...(chats || [])].sort((a, b) => {
      const aTime = a?.lastMessageAt ? new Date(a.lastMessageAt).getTime() : 0
      const bTime = b?.lastMessageAt ? new Date(b.lastMessageAt).getTime() : 0
      return bTime - aTime
    })
  }, [chats])

  useEffect(() => {
    pagingByConversationRef.current = pagingByConversation
  }, [pagingByConversation])

  useEffect(() => {
    chatsRef.current = chats || []
  }, [chats])

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
  }, [])

  useEffect(() => {
    refreshChatsListRef.current = refreshChatsList
  }, [refreshChatsList])

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

  useEffect(() => {
    sendReadReceiptRef.current = sendReadReceipt
  }, [sendReadReceipt])

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

  useEffect(() => {
    updateMessageByClientIdRef.current = updateMessageByClientId
  }, [updateMessageByClientId])

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

  useEffect(() => {
    markPendingMessagesRef.current = markPendingMessages
  }, [markPendingMessages])


  const threadItems = useMemo(() => {
    const items = []
    let lastDay = ''
    ;(messages || []).forEach((message) => {
      const dayLabel = formatMessageDay(message.sentAt)
      if (dayLabel && dayLabel !== lastDay) {
        items.push({
          type: 'day',
          key: `day-${dayLabel}-${message.messageId}`,
          label: dayLabel,
        })
        lastDay = dayLabel
      }
      items.push({ type: 'message', key: `msg-${getMessageKey(message)}`, message })
    })
    return items
  }, [getMessageKey, messages])

  const scrollThreadToBottom = useCallback(() => {
    if (!threadRef.current) return
    threadRef.current.scrollTop = threadRef.current.scrollHeight
  }, [])

  useEffect(() => {
    scrollThreadToBottomRef.current = scrollThreadToBottom
  }, [scrollThreadToBottom])

  const handleToggleList = useCallback(() => {
    if (listToggleTimerRef.current) {
      clearTimeout(listToggleTimerRef.current)
    }
    if (isDockNarrow && threadRef.current) {
      const { scrollTop, scrollHeight, clientHeight } = threadRef.current
      pendingToggleScrollRef.current = scrollTop
      pendingToggleAtBottomRef.current =
        scrollTop + clientHeight >= scrollHeight - nearBottomThreshold
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
      setMessageError(err.message || 'Failed to load older messages.')
    } finally {
      loadingOlderRef.current = false
      setLoadingOlder(false)
    }
  }, [mergeMessages, normalizeMessages, pageSize])

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


  function handleThreadScroll(event) {
    const target = event.currentTarget
    lastScrollTopRef.current = target.scrollTop
  }

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

  useEffect(() => {
    if (!user?.id || loadingChats) return
    if (!chats?.length) {
      setNotifiedChatIds([])
      return
    }
    const unreadIds = chats.filter((chat) => isChatUnread(chat)).map((chat) => chat.conversationId)
    setNotifiedChatIds(unreadIds)
  }, [user?.id, chats, isChatUnread, loadingChats])

  useEffect(() => {
    activeChatIdRef.current = activeChatId
  }, [activeChatId])

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
    activeTargetIdRef.current = activeTargetId
  }, [activeTargetId])

  useEffect(() => {
    activeTargetNameRef.current = activeTargetName
  }, [activeTargetName])

  useEffect(() => {
    openRef.current = open
  }, [open])

  useEffect(() => {
    if (open) {
      lastLoadedChatIdRef.current = null
      pendingScrollToBottomRef.current = false
    }
  }, [open])

  useLayoutEffect(() => {
    if (!pendingScrollToBottomRef.current || !threadRef.current) return
    pendingScrollToBottomRef.current = false
    scrollThreadToBottom()
  }, [messages, open, showList, isDockNarrow, activeChatId, scrollThreadToBottom])


  useEffect(() => {
    if (!dockSize) return
    localStorage.setItem('omniOne.chatDockSize', JSON.stringify(dockSize))
  }, [dockSize])

  useEffect(() => {
    if (!dockPos) return
    localStorage.setItem('omniOne.chatDockPos', JSON.stringify(dockPos))
  }, [dockPos])

  useEffect(() => {
    if (!listWidth) return
    localStorage.setItem('omniOne.chatDockListWidth', JSON.stringify(listWidth))
  }, [listWidth])

  useEffect(() => {
    if (!isResizing) return
    function handleMove(event) {
      if (!resizeStartRef.current) return
      const { startRight, startBottom } = resizeStartRef.current
      const inset = window.innerWidth <= 720 ? 20 : 36
      const minTop = 96
      const minWidth = 280
      const minHeight = 320
      const maxLeft = (startRight ?? inset + minWidth) - minWidth
      const minLeft = inset
      const nextLeft = Math.min(maxLeft, Math.max(minLeft, event.clientX))
      const nextWidth = Math.max(minWidth, (startRight ?? nextLeft + minWidth) - nextLeft)
      const maxTop = (startBottom ?? minTop + minHeight) - minHeight
      const nextTop = Math.min(maxTop, Math.max(minTop, event.clientY))
      const nextHeight = Math.max(minHeight, (startBottom ?? nextTop + minHeight) - nextTop)
      setDockSize({ width: nextWidth, height: nextHeight })
      setDockPos({ x: nextLeft, y: nextTop })
    }

    function handleUp() {
      setIsResizing(false)
      resizeStartRef.current = null
    }

    window.addEventListener('mousemove', handleMove)
    window.addEventListener('mouseup', handleUp)
    return () => {
      window.removeEventListener('mousemove', handleMove)
      window.removeEventListener('mouseup', handleUp)
    }
  }, [isResizing])

  useEffect(() => {
    if (!isDragging) return
    const previousUserSelect = document.body.style.userSelect
    const previousCursor = document.body.style.cursor
    document.body.style.userSelect = 'none'
    document.body.style.cursor = 'grabbing'
    function handleMove(event) {
      if (!dragStartRef.current) return
      const { offsetX, offsetY } = dragStartRef.current
      const width = panelRef.current?.offsetWidth || 0
      const height = panelRef.current?.offsetHeight || 0
      const inset = window.innerWidth <= 720 ? 20 : 36
      const minX = inset
      const maxX = Math.max(inset, window.innerWidth - width - inset)
      const minY = 96
      const maxY = Math.max(16, window.innerHeight - height - 16)
      const nextX = Math.min(maxX, Math.max(minX, event.clientX - offsetX))
      const nextY = Math.min(maxY, Math.max(minY, event.clientY - offsetY))
      setDockPos({ x: nextX, y: nextY })
    }

    function handleUp() {
      setIsDragging(false)
      if (!panelRef.current) {
        dragStartRef.current = null
        return
      }
      dragStartRef.current = null
    }

    window.addEventListener('mousemove', handleMove)
    window.addEventListener('mouseup', handleUp)
    return () => {
      window.removeEventListener('mousemove', handleMove)
      window.removeEventListener('mouseup', handleUp)
      document.body.style.userSelect = previousUserSelect
      document.body.style.cursor = previousCursor
    }
  }, [isDragging])

  useEffect(() => {
    if (isResizing || isDragging) return
    function clampDockHeight() {
      if (!dockSize && !dockPos) return
      const top = typeof dockPos?.y === 'number' ? dockPos.y : 96
      const minHeight = 320
      const maxHeight = Math.max(160, window.innerHeight - 16 - top)
      if (!dockSize) return
      const nextHeight = Math.min(Math.max(dockSize.height, Math.min(minHeight, maxHeight)), maxHeight)
      if (nextHeight !== dockSize.height) {
        setDockSize({ ...dockSize, height: nextHeight })
      }
    }
    clampDockHeight()
    window.addEventListener('resize', clampDockHeight)
    return () => window.removeEventListener('resize', clampDockHeight)
  }, [dockSize, dockPos, isResizing, isDragging])

  useEffect(() => {
    if (!isResizingDivider) return
    const previousUserSelect = document.body.style.userSelect
    const previousCursor = document.body.style.cursor
    document.body.style.userSelect = 'none'
    document.body.style.cursor = 'col-resize'

    function handleMove(event) {
      if (!panelRef.current || !dividerDragRef.current) return
      const rect = panelRef.current.getBoundingClientRect()
      const dividerWidth = 4
      const minList = dividerDragRef.current.minList || 200
      const minThread = 280
      const maxList = rect.width - minThread - dividerWidth
      const deltaX = event.clientX - dividerDragRef.current.startX
      const next = Math.max(
        minList,
        Math.min(maxList, dividerDragRef.current.startWidth + deltaX),
      )
      setListWidth(next)
    }

    function handleUp() {
      setIsResizingDivider(false)
      dividerDragRef.current = null
      window.removeEventListener('mousemove', handleMove)
      window.removeEventListener('mouseup', handleUp)
    }

    window.addEventListener('mousemove', handleMove)
    window.addEventListener('mouseup', handleUp)
    return () => {
      window.removeEventListener('mousemove', handleMove)
      window.removeEventListener('mouseup', handleUp)
      document.body.style.userSelect = previousUserSelect
      document.body.style.cursor = previousCursor
    }
  }, [isResizingDivider])

  function handleDividerMouseDown(event) {
    if (isDockNarrow) return
    event.preventDefault()
    const listEl = panelRef.current?.querySelector('.chat-dock-list')
    const rawWidth = listEl ? Math.ceil(listEl.scrollWidth) : 200
    const minList = Math.max(200, Math.min(280, rawWidth))
    dividerDragRef.current = {
      startX: event.clientX,
      startWidth: listWidth,
      minList,
    }
    setIsResizingDivider(true)
  }

  function handleDragStart(event) {
    if (isResizing) return
    if (
      event.target.closest('button') ||
      event.target.closest('a') ||
      event.target.closest('input') ||
      event.target.closest('textarea')
    ) {
      return
    }
    if (!panelRef.current) return
    event.preventDefault()
    const rect = panelRef.current.getBoundingClientRect()
    if (!dockPos) {
      setDockPos({ x: rect.left, y: rect.top })
    }
    if (!dockSize) {
      setDockSize({ width: rect.width, height: rect.height })
    }
    dragStartRef.current = {
      offsetX: event.clientX - rect.left,
      offsetY: event.clientY - rect.top,
    }
    setIsDragging(true)
  }

  function handleDockReset() {
    setDockPos(null)
    localStorage.removeItem('omniOne.chatDockPos')
  }

  function handleResizeStart(event) {
    if (!panelRef.current) return
    event.preventDefault()
    const rect = panelRef.current.getBoundingClientRect()
    resizeStartRef.current = {
      startX: event.clientX,
      startY: event.clientY,
      startWidth: rect.width,
      startHeight: rect.height,
      startLeft: rect.left,
      startTop: rect.top,
      startRight: rect.right,
      startBottom: rect.bottom,
    }
    setIsResizing(true)
  }

  useEffect(() => {
    setHasNewMessage(notifiedChatIds.length > 0)
  }, [notifiedChatIds])

  function updateLastMessageMap(list) {
    const nextMap = new Map()
    ;(list || []).forEach((chat) => {
      const time = chat?.lastMessageAt ? new Date(chat.lastMessageAt).getTime() : 0
      nextMap.set(chat.conversationId, time)
    })
    lastMessageMapRef.current = nextMap
  }

  useEffect(() => {
    if (!open || !panelRef.current) return
    if (panelWidth === null) {
      const rect = panelRef.current.getBoundingClientRect()
      if (rect.width) {
        setPanelWidth(rect.width)
      }
    }
    const observer = new ResizeObserver((entries) => {
      const entry = entries[0]
      if (entry?.contentRect?.width) {
        setPanelWidth(entry.contentRect.width)
      }
    })
    observer.observe(panelRef.current)
    return () => observer.disconnect()
  }, [open, panelWidth])

  useEffect(() => {
    if (!threadRef.current) return
    const height = threadRef.current.scrollHeight
    const client = threadRef.current.clientHeight
    const prev = prevMetricsRef.current
    if (!prev || prev.height !== height || prev.client !== client) {
      prevMetricsRef.current = { height, client }
    }
  }, [open, activeChatId, showList, isDockNarrow])

  useEffect(() => {
    if (isDockNarrow) {
      setShowList(false)
    }
    if (isDockNarrow && !activeChatId) {
      setShowList(true)
    }
  }, [isDockNarrow, activeChatId])

  useEffect(() => {
    if (!isDockNarrow || !showList) return
    const chatId = activeChatIdRef.current
    if (!chatId || !threadRef.current) return
    switchGuardRef.current = true
    requestAnimationFrame(() => {
      switchGuardRef.current = false
    })
  }, [isDockNarrow, showList])

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

  useEffect(() => {
    if (!user?.id) return
    setLoadingChats(true)
    return () => {
      setLoadingChats(false)
    }
  }, [user?.id])

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
          setMessageError(err.message || 'Failed to load messages.')
        }
      }
    }

    loadConversation()
    return () => {
      mounted = false
    }
  }, [open, activeChatId, messagesByConversation, normalizeMessages, pageSize])

  useEffect(() => {
    if (!open || !threadRef.current) return
  }, [messages, showList, open, activeChatId])

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
      markPendingMessagesRef.current?.()
    }

    client.onWebSocketClose = () => {
      logWsRef.current?.('ws-close', { userId: user?.id })
      markPendingMessagesRef.current?.()
    }

    client.onUnhandledMessage = (message) => {
      logWsRef.current?.('unhandled', {
        destination: message?.headers?.destination || '',
        body: message?.body || '',
      })
    }

    client.onConnect = () => {
      logWsRef.current?.('connect', { userId: user?.id })
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
          const isSelfMessage = incoming.senderId && incoming.senderId === user?.id
          const knownChat = chatsRef.current.some(
            (chat) => chat.conversationId === incoming.conversationId,
          )
          if (!knownChat) {
            refreshChatsListRef.current?.()
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
                scrollThreadToBottomRef.current?.()
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
            sendReadReceiptRef.current?.(incoming.conversationId)
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
          updateMessageByClientIdRef.current?.(conversationId, clientMessageId, (item) => ({
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
                    lastMessageSenderId: user?.id || chat.lastMessageSenderId,
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
          const errorMessage =
            payload?.message ||
            (payload?.fieldErrors ? Object.values(payload.fieldErrors)[0] : '') ||
            'Message failed to send.'
          if (clientMessageId && conversationId) {
            pendingMessagesRef.current.delete(clientMessageId)
            updateMessageByClientIdRef.current?.(conversationId, clientMessageId, (item) => ({
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
          if (user?.id) {
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
          setChatError(err.message || 'Failed to load chats.')
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
          updateMessageByClientIdRef.current?.(conversationId, clientMessageId, (item) => ({
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
  }, [user?.id])

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
        setChatError(err.message || 'Failed to start chat.')
      }
    },
    [isCoach, selectChat],
  )

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
        setChatError(err.message || 'Failed to load clients.')
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
      setChatError(err.message || 'Failed to start chat.')
    } finally {
      setStarting(false)
    }
  }

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
      setChatError(err.message || 'Failed to start chat.')
    } finally {
      setStarting(false)
    }
  }

  function handleSelectChat(chat) {
    selectChat(chat)
  }

  function handleToggleDock() {
    setOpen((prev) => !prev)
  }

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
        updateLastMessageMap(next)
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
                <button
                  type="button"
                  className={`icon-button chat-dock-list-toggle${showList ? ' is-active' : ''}${
                    notifiedChatIds.length ? ' is-notified' : ''
                  }`}
                  onClick={handleToggleList}
                  aria-label={showList ? 'Hide chat list' : 'Show chat list'}
                  aria-pressed={showList}
                >
                  <UserList size={24} weight="bold" />
                  <span className="chat-dock-list-label">Chats</span>
                </button>
                <div className="chat-dock-actions" ref={menuRef}>
                  <button
                    type="button"
                    className={`icon-button chat-dock-start${showStart ? ' is-active' : ''}`}
                    onClick={handleStartChat}
                    aria-label={isCoach ? 'Start chat' : 'Message coach'}
                    title={isCoach ? 'Start chat' : 'Message coach'}
                    aria-pressed={showStart}
                    aria-busy={starting}
                    aria-disabled={starting}
                  >
                    <Plus size={22} weight="bold" />
                    <span className="chat-dock-start-label">New</span>
                  </button>
                  {showStart && isCoach ? (
                    <div className="chat-start-menu">
                      <div className="chat-start-title">Start new chat</div>
                      {startTargets.length === 0 ? (
                        <p className="muted chat-start-empty">No clients available.</p>
                      ) : (
                        <ul className="chat-start-list">
                          {startTargets.map((client) => (
                            <li key={client.id}>
                              <button
                                type="button"
                                className="chat-start-item"
                                onClick={() => handleStartWithTarget(client.id)}
                              >
                                {client.firstName || 'Client'} {client.lastName || ''}
                              </button>
                            </li>
                          ))}
                        </ul>
                      )}
                    </div>
                  ) : null}
                </div>
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
            {chatError ? <p className="error">{chatError}</p> : null}
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
              <div className="chat-dock-list">
                {loadingChats ? <p className="muted">Loading chats...</p> : null}
                {!loadingChats && sortedChats.length === 0 ? (
                  <p className="muted chat-empty">No chats yet.</p>
                ) : (
                  <ul className="card-list">
                    {sortedChats.map((chat) => (
                      <li key={chat.conversationId} className="list-item">
                        <button
                          type="button"
                          className={`list-link${
                            activeChatId === chat.conversationId ? ' active' : ''
                          }${
                            notifiedChatIds.includes(chat.conversationId) ? ' is-notified' : ''
                          }`}
                          onClick={() => handleSelectChat(chat)}
                        >
                          <div className="chat-item-row">
                            <div className="card-title">
                              {chat.otherFirstName || 'Client'} {chat.otherLastName || ''}
                            </div>
                            {chat.lastMessageAt ? (
                              <div
                                className={`muted chat-time${
                                  notifiedChatIds.includes(chat.conversationId) ? ' is-notified' : ''
                                }`}
                              >
                                {formatChatTimestamp(chat.lastMessageAt)}
                              </div>
                            ) : null}
                          </div>
                          {chat.lastMessagePreview ? (
                            <div className="muted chat-preview">{chat.lastMessagePreview}</div>
                          ) : null}
                        </button>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
              {!isDockNarrow && showList ? (
                <div
                  className="chat-dock-divider"
                  onMouseDown={handleDividerMouseDown}
                  onDoubleClick={() => {
                    setListWidth(320)
                    localStorage.removeItem('omniOne.chatDockListWidth')
                  }}
                />
              ) : null}
              <div className="chat-dock-thread">
                {activeChatId ? (
                  <>
                    <div
                      className="chat-thread"
                      ref={threadRef}
                      onWheel={() => {
                        userScrollIntentRef.current = true
                        if (userScrollIntentTimerRef.current) {
                          clearTimeout(userScrollIntentTimerRef.current)
                        }
                        userScrollIntentTimerRef.current = setTimeout(() => {
                          userScrollIntentRef.current = false
                        }, 200)
                      }}
                      onTouchMove={() => {
                        userScrollIntentRef.current = true
                        if (userScrollIntentTimerRef.current) {
                          clearTimeout(userScrollIntentTimerRef.current)
                        }
                        userScrollIntentTimerRef.current = setTimeout(() => {
                          userScrollIntentRef.current = false
                        }, 200)
                      }}
                      onScroll={(event) => {
                        const isTrusted = event.nativeEvent?.isTrusted
                        handleThreadScroll(event)
                        if (!activeChatId) return
                        if (switchGuardRef.current) return
                        if (event.currentTarget.scrollTop <= 120) {
                          loadOlderMessages()
                        }
                        const { scrollTop, scrollHeight, clientHeight } = event.currentTarget
                        const atBottom = scrollTop + clientHeight >= scrollHeight - nearBottomThreshold
                        isAtBottomRef.current = atBottom
                        if (atBottom && isTrusted) {
                          setNotifiedChatIds((prev) => prev.filter((id) => id !== activeChatId))
                          sendReadReceipt(activeChatId)
                        }
                      }}
                    >
                      {loadingOlder ? <div className="muted">Loading older messages...</div> : null}
                      {messages.length === 0 ? (
                        <p className="muted">No messages yet. Start the conversation below.</p>
                      ) : (
                        <div className="chat-thread-list">
                          {threadItems.map((item) => {
                            if (item.type === 'day') {
                              return (
                                <div key={item.key} className="chat-thread-item chat-day-row" data-day={item.label}>
                                  <div className="chat-day-divider">
                                    <span>{item.label}</span>
                                  </div>
                                </div>
                              )
                            }
                            const isSelf = item.message.senderId === user?.id
                            const isSending = item.message.status === 'sending'
                            const isFailed = item.message.status === 'failed'
                            const isPending = item.message.status === 'pending'
                            const isSendingMeta = isSending
                            const metaLabel = isSendingMeta
                              ? 'Sending...'
                              : isPending
                                ? 'Pending'
                                : isFailed
                                  ? 'Failed'
                                  : formatMessageTime(item.message.sentAt)
                            return (
                              <div
                                key={item.key}
                                className={`chat-thread-item ${isSelf ? 'chat-row-self' : 'chat-row-peer'}`}
                              >
                                <div className={`chat-bubble ${isSelf ? 'chat-self' : 'chat-peer'}`}>
                                  <p>{item.message.content}</p>
                                  <div
                                    className={`chat-meta${
                                      isFailed ? ' chat-meta-error' : ''
                                    }${isPending || isSendingMeta ? ' chat-meta-pending' : ''}`}
                                  >
                                    {metaLabel}
                                  </div>
                                </div>
                              </div>
                            )
                          })}
                        </div>
                      )}
                    </div>
                    {messageError ? <p className="error">{messageError}</p> : null}
                    <form className="chat-input" onSubmit={handleSend}>
                      <textarea
                        ref={inputRef}
                        className="chat-textarea"
                        placeholder={activeTargetId ? 'Type your message...' : 'Recipient unavailable'}
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
                        disabled={!activeTargetId}
                      />
                      <button
                        type="submit"
                        className="send-button"
                        disabled={!activeTargetId}
                        aria-label="Send message"
                      >
                        <PaperPlaneRight size={20} weight="bold" />
                      </button>
                    </form>
                  </>
                ) : (
                  <p className="muted chat-empty">Select a chat to start messaging.</p>
                )}
              </div>
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
