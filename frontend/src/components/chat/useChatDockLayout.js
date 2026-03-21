import { useCallback, useEffect, useRef, useState } from 'react'

export function useChatDockLayout({
  open,
  panelRef,
  threadRef,
}) {
  const [dockSize, setDockSize] = useState(() => {
    try {
      const stored = JSON.parse(localStorage.getItem('omniOne.chatDockSize') || 'null')
      return stored && stored.width && stored.height ? stored : null
    } catch {
      return null
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
  const [listWidth, setListWidth] = useState(() => {
    try {
      const stored = JSON.parse(localStorage.getItem('omniOne.chatDockListWidth') || 'null')
      return typeof stored === 'number' ? stored : 320
    } catch {
      return 320
    }
  })
  const [panelWidth, setPanelWidth] = useState(null)
  const [isResizingState, setIsResizing] = useState(false)
  const [isDraggingState, setIsDragging] = useState(false)
  const [isResizingDividerState, setIsResizingDivider] = useState(false)

  const isDockNarrow = panelWidth !== null && panelWidth < 570

  const resizeStartRef = useRef(null)
  const dragStartRef = useRef(null)
  const dividerDragRef = useRef(null)
  const prevMetricsRef = useRef(null)

  // Persist dockSize to localStorage
  useEffect(() => {
    if (!dockSize) return
    localStorage.setItem('omniOne.chatDockSize', JSON.stringify(dockSize))
  }, [dockSize])

  // Persist dockPos to localStorage
  useEffect(() => {
    if (!dockPos) return
    localStorage.setItem('omniOne.chatDockPos', JSON.stringify(dockPos))
  }, [dockPos])

  // Persist listWidth to localStorage
  useEffect(() => {
    if (!listWidth) return
    localStorage.setItem('omniOne.chatDockListWidth', JSON.stringify(listWidth))
  }, [listWidth])

  // Handle resize
  useEffect(() => {
    if (!isResizingState) return
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
  }, [isResizingState])

  // Handle drag
  useEffect(() => {
    if (!isDraggingState) return
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
  }, [isDraggingState])

  // Clamp dock height on window resize
  useEffect(() => {
    if (isResizingState || isDraggingState) return
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
  }, [dockSize, dockPos, isResizingState, isDraggingState])

  // Handle divider resize
  useEffect(() => {
    if (!isResizingDividerState) return
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
  }, [isResizingDividerState])

  // Observe panel width
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

  // Track thread metrics
  useEffect(() => {
    if (!threadRef.current) return
    const height = threadRef.current.scrollHeight
    const client = threadRef.current.clientHeight
    const prev = prevMetricsRef.current
    if (!prev || prev.height !== height || prev.client !== client) {
      prevMetricsRef.current = { height, client }
    }
  }, [open, isDockNarrow])

  const handleDragStart = useCallback((event) => {
    if (isResizingState) return
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
  }, [isResizingState, dockPos, dockSize])

  const handleDividerMouseDown = useCallback((event) => {
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
  }, [isDockNarrow, listWidth])

  const handleResizeStart = useCallback((event) => {
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
  }, [])

  const handleDockReset = useCallback(() => {
    setDockPos(null)
    localStorage.removeItem('omniOne.chatDockPos')
  }, [])

  const handleDividerReset = useCallback(() => {
    setListWidth(320)
    localStorage.removeItem('omniOne.chatDockListWidth')
  }, [])

  return {
    dockSize,
    setDockSize,
    dockPos,
    setDockPos,
    listWidth,
    setListWidth,
    panelWidth,
    setPanelWidth,
    isResizingState,
    setIsResizing,
    isDraggingState,
    setIsDragging,
    isResizingDividerState,
    setIsResizingDivider,
    resizeStartRef,
    dragStartRef,
    dividerDragRef,
    handleDragStart,
    handleDividerMouseDown,
    handleResizeStart,
    handleDockReset,
    handleDividerReset,
  }
}
