import { Plus, UserList } from 'phosphor-react'
import { formatChatTimestamp } from './chatUtils.js'

export function ChatConversationList({
  loadingChats,
  sortedChats,
  activeChatId,
  notifiedChatIds,
  isCoach,
  showStart,
  starting,
  startTargets,
  onSelectChat,
  onStartChat,
  onStartWithTarget,
  chatError,
  menuRef,
}) {
  return (
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
                className={`list-link${activeChatId === chat.conversationId ? ' active' : ''}${
                  notifiedChatIds.includes(chat.conversationId) ? ' is-notified' : ''
                }`}
                onClick={() => onSelectChat(chat)}
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
  )
}

export function ChatDockActions({
  isCoach,
  showStart,
  starting,
  startTargets,
  onStartChat,
  onStartWithTarget,
  menuRef,
  notifiedChatIds,
}) {
  return (
    <div className="chat-dock-actions" ref={menuRef}>
      <button
        type="button"
        className={`icon-button chat-dock-start${showStart ? ' is-active' : ''}`}
        onClick={onStartChat}
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
                    onClick={() => onStartWithTarget(client.id)}
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
  )
}

export function ChatDockListToggle({
  showList,
  notifiedChatIds,
  onToggleList,
}) {
  return (
    <button
      type="button"
      className={`icon-button chat-dock-list-toggle${showList ? ' is-active' : ''}${
        notifiedChatIds.length ? ' is-notified' : ''
      }`}
      onClick={onToggleList}
      aria-label={showList ? 'Hide chat list' : 'Show chat list'}
      aria-pressed={showList}
    >
      <UserList size={24} weight="bold" />
      <span className="chat-dock-list-label">Chats</span>
    </button>
  )
}
