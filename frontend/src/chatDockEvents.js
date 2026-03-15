export const CHAT_DOCK_OPEN_EVENT = 'omniOne:openChatDock'

export function openChatDock({ targetId, targetName }) {
  if (!targetId) return
  window.dispatchEvent(
    new CustomEvent(CHAT_DOCK_OPEN_EVENT, {
      detail: { targetId, targetName },
    }),
  )
}
