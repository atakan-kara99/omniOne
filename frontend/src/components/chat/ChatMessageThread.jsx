import { PaperPlaneRight } from 'phosphor-react'
import { useMemo } from 'react'
import { formatMessageDay, formatMessageTime } from './chatUtils.js'

export function ChatMessageThread({
  activeChatId,
  activeTargetId,
  messages,
  messageError,
  loadingOlder,
  input,
  userId,
  threadRef,
  inputRef,
  onScroll,
  onSend,
  onInputChange,
  onKeyDown,
  onWheel,
  onTouchMove,
  nearBottomThreshold,
}) {
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
      items.push({
        type: 'message',
        key: `msg-${message?.clientMessageId || message?.messageId || ''}`,
        message,
      })
    })
    return items
  }, [messages])

  if (!activeChatId) {
    return (
      <div className="chat-dock-thread">
        <p className="muted chat-empty">Select a chat to start messaging.</p>
      </div>
    )
  }

  return (
    <div className="chat-dock-thread">
      <div
        className="chat-thread"
        ref={threadRef}
        onWheel={onWheel}
        onTouchMove={onTouchMove}
        onScroll={onScroll}
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
              const isSelf = item.message.senderId === userId
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
                      className={`chat-meta${isFailed ? ' chat-meta-error' : ''}${
                        isPending || isSendingMeta ? ' chat-meta-pending' : ''
                      }`}
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
      <form className="chat-input" onSubmit={onSend} autoComplete="off">
        <textarea
          name="message"
          id="chat-message"
          autoComplete="off"
          ref={inputRef}
          className="chat-textarea"
          placeholder={activeTargetId ? 'Type your message...' : 'Recipient unavailable'}
          value={input}
          onChange={onInputChange}
          onKeyDown={onKeyDown}
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
    </div>
  )
}
