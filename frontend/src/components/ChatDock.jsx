import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
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
import { Link, useLocation } from 'react-router-dom'

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
  const location = useLocation()
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
  const [isThreadAtTop, setIsThreadAtTop] = useState(true)
  const [stickyDayLabel, setStickyDayLabel] = useState('')
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
  const scrollToBottomRef = useRef(false)
  const forceScrollRef = useRef(false)
  const needsBottomRef = useRef(false)
  const listToggleTimerRef = useRef(null)
  const [showNewMessageBanner, setShowNewMessageBanner] = useState(false)
  const dragStartRef = useRef(null)
  const isAtBottomRef = useRef(true)
  const dividerDragRef = useRef(null)
  const pagingByConversationRef = useRef({})
  const loadingOlderRef = useRef(false)
  const pendingAnchorRef = useRef(null)
  const pendingBottomRef = useRef(null)
  const pendingToggleScrollRef = useRef(null)
  const pendingToggleAtBottomRef = useRef(false)
  const lastReadSentRef = useRef(new Map())
  const chatsRef = useRef([])
  const refreshChatsInFlightRef = useRef(false)
  const userScrollIntentRef = useRef(false)
  const userScrollIntentTimerRef = useRef(null)
  const skipRouteChatRef = useRef(true)
  const lastNarrowChatRef = useRef(null)
  const DEBUG_WS = import.meta.env.DEV
  const logWs = (...args) => {
    if (DEBUG_WS) {
      console.log('[chat-ws]', ...args)
    }
  }
  const nearBottomThreshold = 80
  const switchGuardRef = useRef(false)
  const logDock = () => {}
  const isDockNarrow = panelWidth !== null && panelWidth < 570
  const isDockTiny = panelWidth !== null && panelWidth < 500
  const pageSize = 25
  const prevMetricsRef = useRef(null)
  const itemHeightsRef = useRef(new Map())
  const [measureVersion, setMeasureVersion] = useState(0)
  const [threadScrollTop, setThreadScrollTop] = useState(0)
  const [threadViewportHeight, setThreadViewportHeight] = useState(0)
  const itemGap = 3

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
    if (!chat?.lastReadAt) return true
    return new Date(chat.lastMessageAt).getTime() > new Date(chat.lastReadAt).getTime()
  }, [])

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

  const sendReadReceipt = useCallback((chatId) => {
    if (!chatId || !clientRef.current) return
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
    setShowNewMessageBanner(false)
  }, [])

  const normalizeMessages = useCallback((list) => {
    const next = [...(list || [])]
    next.sort((a, b) => new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime())
    return next
  }, [])

  const mergeMessages = useCallback((older, existing) => {
    const map = new Map()
    ;(existing || []).forEach((item) => {
      if (item?.messageId) map.set(item.messageId, item)
    })
    ;(older || []).forEach((item) => {
      if (item?.messageId && !map.has(item.messageId)) {
        map.set(item.messageId, item)
      }
    })
    return normalizeMessages([...map.values()])
  }, [normalizeMessages])


  const measureItem = useCallback(
    (key) => (node) => {
      if (!node) return
      const height = node.getBoundingClientRect().height
      const prev = itemHeightsRef.current.get(key)
      if (prev !== height) {
        itemHeightsRef.current.set(key, height)
        setMeasureVersion((value) => value + 1)
      }
    },
    [],
  )

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
      items.push({ type: 'message', key: `msg-${message.messageId}`, message })
    })
    return items
  }, [messages])

  const virtualData = useMemo(() => {
    const count = threadItems.length
    const sizes = new Array(count)
    const offsets = new Array(count)
    let total = 0
    for (let index = 0; index < count; index += 1) {
      const item = threadItems[index]
      const key = item.key
      const measured = itemHeightsRef.current.get(key)
      const estimate = item.type === 'day' ? 40 : 76
      const size = (measured ?? estimate) + (index === 0 ? 0 : itemGap)
      sizes[index] = size
      offsets[index] = total
      total += size
    }
    return { sizes, offsets, total }
  }, [threadItems, measureVersion, itemGap])

  const dayMarkers = useMemo(() => {
    return threadItems
      .map((item, index) =>
        item.type === 'day' ? { label: item.label, offset: virtualData.offsets[index] ?? 0 } : null,
      )
      .filter(Boolean)
  }, [threadItems, virtualData.offsets])

  const scrollThreadToBottom = useCallback(() => {
    if (!threadRef.current) return false
    if (threadRef.current.scrollHeight === 0) {
      return false
    }
    threadRef.current.scrollTop = threadRef.current.scrollHeight
    setThreadScrollTop(threadRef.current.scrollTop)
    return true
  }, [])

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
          pendingBottomRef.current = saved.chatId
          scrollToBottomRef.current = true
          needsBottomRef.current = true
        }
      }
      return next
    })
    requestAnimationFrame(() => {
      if (!threadRef.current) return
      if (pendingToggleAtBottomRef.current) {
        scrollThreadToBottom()
      } else if (pendingToggleScrollRef.current != null) {
        threadRef.current.scrollTop = pendingToggleScrollRef.current
        setThreadScrollTop(threadRef.current.scrollTop)
      }
      pendingToggleScrollRef.current = null
      pendingToggleAtBottomRef.current = false
    })
  }, [isDockNarrow, nearBottomThreshold, scrollThreadToBottom])

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

  const virtualRange = useMemo(() => {
    const count = threadItems.length
    if (!count) {
      return { start: 0, end: -1, total: 0 }
    }
    const { offsets, sizes, total } = virtualData
    const scrollTop = threadScrollTop
    const viewport = threadViewportHeight || 0
    const overscan = 6
    const findIndexForOffset = (target) => {
      let low = 0
      let high = offsets.length - 1
      let answer = offsets.length - 1
      while (low <= high) {
        const mid = Math.floor((low + high) / 2)
        const start = offsets[mid]
        const end = start + sizes[mid]
        if (end >= target) {
          answer = mid
          high = mid - 1
        } else {
          low = mid + 1
        }
      }
      return answer
    }
    let start = findIndexForOffset(scrollTop)
    let end = findIndexForOffset(scrollTop + viewport)
    start = Math.max(0, start - overscan)
    end = Math.min(count - 1, end + overscan)
    return { start, end, total }
  }, [threadItems.length, virtualData, threadScrollTop, threadViewportHeight])

  const latestDayLabel = useMemo(() => {
    if (!messages?.length) return ''
    const lastMessage = messages[messages.length - 1]
    return formatMessageDay(lastMessage?.sentAt)
  }, [messages])

  useEffect(() => {
    if (isThreadAtTop) {
      setStickyDayLabel('')
      return
    }
    setStickyDayLabel(latestDayLabel)
  }, [latestDayLabel, isThreadAtTop])

  useEffect(() => {
    if (!pendingAnchorRef.current || !threadRef.current) return
    const { chatId, prevScrollTop, prevScrollHeight } = pendingAnchorRef.current
    if (activeChatIdRef.current !== chatId) return
    requestAnimationFrame(() => {
      if (!threadRef.current) return
      const nextHeight = threadRef.current.scrollHeight
      const delta = nextHeight - prevScrollHeight
      threadRef.current.scrollTop = prevScrollTop + delta
      setThreadScrollTop(threadRef.current.scrollTop)
      pendingAnchorRef.current = null
    })
  }, [messages, measureVersion])

  useEffect(() => {
    if (!open || !isDockNarrow || !threadRef.current) return
    if (!isAtBottomRef.current) return
    requestAnimationFrame(() => {
      if (!threadRef.current) return
      scrollThreadToBottom()
    })
  }, [showList, open, isDockNarrow, scrollThreadToBottom])

  useEffect(() => {
    if (!open || !threadRef.current) return
    const pendingChatId = pendingBottomRef.current
    if (!pendingChatId || pendingChatId !== activeChatIdRef.current) return
    requestAnimationFrame(() => {
      if (!threadRef.current) return
      const didScroll = scrollThreadToBottom()
      const { scrollTop, scrollHeight, clientHeight } = threadRef.current
      const atBottom = scrollTop + clientHeight >= scrollHeight - nearBottomThreshold
      if (!atBottom) {
        requestAnimationFrame(() => {
          if (!threadRef.current) return
          scrollThreadToBottom()
          pendingBottomRef.current = null
        })
        return
      }
      pendingBottomRef.current = null
    })
  }, [messages, measureVersion, open, scrollThreadToBottom, nearBottomThreshold])

  useEffect(() => {
    if (!open) return
    if (messages.length === 0 || !dayMarkers.length) {
      setStickyDayLabel('')
      setIsThreadAtTop(true)
      return
    }
    const scrollTop = threadRef.current?.scrollTop ?? 0
    const firstMarker = dayMarkers[0]
    const atTop = firstMarker ? firstMarker.offset - scrollTop >= 0 : true
    setIsThreadAtTop(atTop)
    if (atTop) {
      setStickyDayLabel('')
    }
  }, [open, messages.length, dayMarkers])

  function handleThreadScroll(event) {
    const target = event.currentTarget
    lastScrollTopRef.current = target.scrollTop
    setThreadScrollTop(target.scrollTop)
    if (!dayMarkers.length) {
      setIsThreadAtTop(true)
      setStickyDayLabel('')
      return
    }
    const firstMarker = dayMarkers[0]
    if (firstMarker && firstMarker.offset - target.scrollTop >= 0) {
      setIsThreadAtTop(true)
      setStickyDayLabel('')
      return
    }
    requestAnimationFrame(() => {
      if (firstMarker && firstMarker.offset - target.scrollTop >= 0) {
        setIsThreadAtTop(true)
        setStickyDayLabel('')
        return
      }
      setIsThreadAtTop(false)
      let current = ''
      for (const marker of dayMarkers) {
        if (marker.offset - target.scrollTop <= 8) {
          current = marker.label || ''
        }
      }
      setStickyDayLabel(current || latestDayLabel)
    })
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
      skipRouteChatRef.current = true
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
    skipRouteChatRef.current = true
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
    if (!activeChatId) {
      setShowNewMessageBanner(false)
      return
    }
    setShowNewMessageBanner(notifiedChatIds.includes(activeChatId))
  }, [activeChatId, notifiedChatIds])

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
    const prevId = prevActiveChatIdRef.current
    prevActiveChatIdRef.current = activeChatId
  }, [activeChatId])

  useEffect(() => {
    activeTargetIdRef.current = activeTargetId
  }, [activeTargetId])

  useEffect(() => {
    openRef.current = open
  }, [open])

  useEffect(() => {
    if (!open || !activeChatId) return
    scrollToBottomRef.current = true
    forceScrollRef.current = true
    needsBottomRef.current = true
    pendingBottomRef.current = activeChatId
  }, [open, activeChatId])

  useEffect(() => {
    if (open) return
    if (!activeChatIdRef.current || !threadRef.current) return
  }, [open])


  useEffect(() => {
    if (!dockSize) return
    localStorage.setItem('omniOne.chatDockSize', JSON.stringify(dockSize))
  }, [dockSize])

  useEffect(() => {
    if (!threadRef.current) return
    const observer = new ResizeObserver((entries) => {
      const entry = entries[0]
      if (entry) {
        setThreadViewportHeight(entry.contentRect.height)
      }
    })
    observer.observe(threadRef.current)
    setThreadViewportHeight(threadRef.current.clientHeight)
    return () => observer.disconnect()
  }, [open, activeChatId])

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

  function getUpdatedChatIds(list) {
    const prevMap = lastMessageMapRef.current
    const updatedIds = []
    const nextMap = new Map()
    ;(list || []).forEach((chat) => {
      const time = chat?.lastMessageAt ? new Date(chat.lastMessageAt).getTime() : 0
      const prevTime = prevMap.get(chat.conversationId) || 0
      if (time && time > prevTime) {
        updatedIds.push(chat.conversationId)
      }
      nextMap.set(chat.conversationId, time)
    })
    lastMessageMapRef.current = nextMap
    return updatedIds
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
  }, [open])

  useEffect(() => {
    if (!threadRef.current) return
    const height = threadRef.current.scrollHeight
    const client = threadRef.current.clientHeight
    const prev = prevMetricsRef.current
    if (!prev || prev.height !== height || prev.client !== client) {
      prevMetricsRef.current = { height, client }
    }
  }, [measureVersion, open, activeChatId, showList, isDockNarrow])

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
    if (!open) return
  }, [location.pathname])

  useEffect(() => {
    const match = location.pathname.match(/\/(?:coach|client)\/chats\/([0-9a-f-]{36})/i)
    if (match?.[1]) {
      if (skipRouteChatRef.current) return
      setActiveChatId(match[1])
    }
  }, [location.pathname])

  useEffect(() => {
    if (!user?.id) return
    const timer = setTimeout(() => {
      skipRouteChatRef.current = false
    }, 0)
    return () => clearTimeout(timer)
  }, [user?.id])

  useEffect(() => {
    if (!open || !activeChatId) return
    let mounted = true

    async function loadConversation() {
      setMessageError('')
      try {
          if (messagesByConversation[activeChatId]) {
            setMessages(messagesByConversation[activeChatId])
            if (scrollToBottomRef.current) {
              forceScrollRef.current = true
              needsBottomRef.current = true
              scrollToBottomRef.current = false
            }
          if (!pagingByConversationRef.current[activeChatId]) {
            setPagingByConversation((prev) => ({
              ...prev,
              [activeChatId]: { cursor: null, hasMore: false },
            }))
          }
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
        if (scrollToBottomRef.current) {
          forceScrollRef.current = true
          needsBottomRef.current = true
          scrollToBottomRef.current = false
        }
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
    if (forceScrollRef.current) {
      forceScrollRef.current = false
      const didScroll = scrollThreadToBottom()
      requestAnimationFrame(() => {
        if (!threadRef.current) return
        const { scrollTop, scrollHeight, clientHeight } = threadRef.current
        const atBottom = scrollTop + clientHeight >= scrollHeight - nearBottomThreshold
        isAtBottomRef.current = atBottom
        if (needsBottomRef.current && !atBottom) {
          requestAnimationFrame(() => {
            if (!threadRef.current) return
            if (!scrollThreadToBottom()) {
              needsBottomRef.current = true
              return
            }
            needsBottomRef.current = false
          })
        } else {
          needsBottomRef.current = false
        }
        if (atBottom && activeChatIdRef.current) {
          setNotifiedChatIds((prev) => prev.filter((id) => id !== activeChatIdRef.current))
          setShowNewMessageBanner(false)
        }
      })
    }
  }, [messages, measureVersion, showList, scrollThreadToBottom, open, activeChatId])

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
      logWs('stomp-error', {
        message: frame?.headers?.message || '',
        body: frame?.body || '',
      })
    }

    client.onWebSocketError = (event) => {
      logWs('ws-error', { type: event?.type || 'error' })
    }

    client.onUnhandledMessage = (message) => {
      logWs('unhandled', {
        destination: message?.headers?.destination || '',
        body: message?.body || '',
      })
    }

    client.onConnect = () => {
      logWs('connect', { userId: user?.id })
      logWs('subscribe', { destination: '/user/queue/reply' })
      client.subscribe('/user/queue/reply', (message) => {
        try {
          logWs('recv', {
            destination: '/user/queue/reply',
            size: message?.body?.length || 0,
            body: message?.body || null,
          })
          const incoming = message?.body ? JSON.parse(message.body) : null
          if (!incoming?.conversationId || !incoming?.messageId || !incoming?.sentAt) return

          const currentChatId = activeChatIdRef.current
          const isActiveChat = currentChatId && incoming.conversationId === currentChatId
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
              if (!atBottom) {
                setShowNewMessageBanner(true)
                forceScrollRef.current = false
                needsBottomRef.current = false
              }
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
              forceScrollRef.current = true
              needsBottomRef.current = true
              setShowNewMessageBanner(false)
              sendReadReceipt(incoming.conversationId)
            } else {
              // keep current auto-scroll flag as-is
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
                ? { ...chat, lastMessageAt: incoming.sentAt, lastMessagePreview: incoming.content }
                : chat,
            )
            updateLastMessageMap(next)
            return next
          })

          setNotifiedChatIds((prev) => {
            if (isActiveChat && openRef.current && isAtBottomRef.current) {
              return prev.filter((id) => id !== incoming.conversationId)
            }
            return Array.from(new Set([...prev, incoming.conversationId]))
          })
          if (isActiveChat && openRef.current && isAtBottomRef.current) {
            setShowNewMessageBanner(false)
            sendReadReceipt(incoming.conversationId)
          }
        } catch {
          // ignore refresh errors
        }
      })

      ;(async () => {
        try {
          const list = await getChats()
          setChats(list || [])
          updateLastMessageMap(list || [])
          if (user?.id) {
            const unreadIds = (list || [])
              .filter((chat) => isChatUnread(chat))
              .map((chat) => chat.conversationId)
            if (unreadIds.length) {
              setNotifiedChatIds((prev) => Array.from(new Set([...prev, ...unreadIds])))
            }
          }
          if (activeChatIdRef.current && !activeTargetName) {
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
    switchGuardRef.current = true
    setActiveChatId(chat.conversationId)
    setActiveTargetId(chat.otherUserId)
    const name = `${chat.otherFirstName || ''} ${chat.otherLastName || ''}`.trim()
    setActiveTargetName(name)
    scrollToBottomRef.current = true
    needsBottomRef.current = true
    if (isDockNarrow) {
      setShowList(false)
    }
    pendingBottomRef.current = chat.conversationId
    if (!isChatUnread(chat)) {
      sendReadReceipt(chat.conversationId)
    }
  }

  function handleToggleDock() {
    setOpen((prev) => !prev)
  }

  function handleSend(event) {
    event.preventDefault()
    if (!input.trim() || !activeTargetId) return
    if (!clientRef.current) {
      setMessageError('Chat connection is offline. Try again in a moment.')
      return
    }

    const content = input.trim()
    const optimisticMessage = {
      messageId: `${Date.now()}-${Math.random()}`,
      senderId: user?.id,
      sentAt: new Date().toISOString(),
      content,
    }
    clientRef.current.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({ to: activeTargetId, content }),
    })
    logWs('send', {
      destination: '/app/chat.send',
      chatId: activeChatId,
      to: activeTargetId,
      content,
    })

    setMessages((prev) => [...prev, optimisticMessage])
    forceScrollRef.current = true
    needsBottomRef.current = true
    if (activeChatId) {
      const now = optimisticMessage.sentAt
      setChats((prev) => {
        const next = prev.map((chat) =>
          chat.conversationId === activeChatId
            ? { ...chat, lastMessagePreview: content, lastMessageAt: now }
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
        <ChatsCircle size={28} />
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
                  <UserList size={24} />
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
                    <Plus size={24} />
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
                    <User size={24} />
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
                          setShowNewMessageBanner(false)
                          if (userScrollIntentRef.current) {
                            sendReadReceipt(activeChatId)
                          }
                        }
                      }}
                    >
                      {loadingOlder ? <div className="muted">Loading older messages...</div> : null}
                      {stickyDayLabel ? (
                        <div className={`chat-day-sticky${isThreadAtTop ? ' is-hidden' : ''}`}>
                          <span>{stickyDayLabel}</span>
                        </div>
                      ) : null}
                      {messages.length === 0 ? (
                        <p className="muted">No messages yet. Start the conversation below.</p>
                      ) : (
                        <div className="chat-thread-virtual" style={{ height: virtualRange.total }}>
                          {threadItems
                            .slice(virtualRange.start, virtualRange.end + 1)
                            .map((item, virtualIndex) => {
                              const index = virtualRange.start + virtualIndex
                              const top = virtualData.offsets[index] ?? 0
                              const paddingTop = index === 0 ? 0 : itemGap
                              if (item.type === 'day') {
                                return (
                                  <div
                                    key={item.key}
                                    ref={measureItem(item.key)}
                                    className="chat-thread-item chat-day-row"
                                    style={{ top, paddingTop }}
                                    data-day={item.label}
                                  >
                                    <div className="chat-day-divider">
                                      <span>{item.label}</span>
                                    </div>
                                  </div>
                                )
                              }
                              const isSelf = item.message.senderId === user?.id
                              return (
                                <div
                                  key={item.key}
                                  ref={measureItem(item.key)}
                                  className={`chat-thread-item ${isSelf ? 'chat-row-self' : 'chat-row-peer'}`}
                                  style={{ top, paddingTop }}
                                >
                                  <div className={`chat-bubble ${isSelf ? 'chat-self' : 'chat-peer'}`}>
                                    <div className="chat-meta">
                                      {formatMessageTime(item.message.sentAt)}
                                    </div>
                                    <p>{item.message.content}</p>
                                  </div>
                                </div>
                              )
                            })}
                        </div>
                      )}
                        {showNewMessageBanner ? (
                          <button
                            type="button"
                            className="chat-new-banner"
                            onClick={() => {
                              needsBottomRef.current = true
                              scrollThreadToBottom()
                              requestAnimationFrame(() => {
                                scrollThreadToBottom()
                                if (needsBottomRef.current) {
                                  requestAnimationFrame(() => {
                                    scrollThreadToBottom()
                                    needsBottomRef.current = false
                                  })
                                }
                              })
                              isAtBottomRef.current = true
                              setShowNewMessageBanner(false)
                              if (activeChatId) {
                                setNotifiedChatIds((prev) => prev.filter((id) => id !== activeChatId))
                                sendReadReceipt(activeChatId)
                              }
                            }}
                          >
                            New messages
                          </button>
                      ) : null}
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
                        <PaperPlaneRight size={20} />
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
