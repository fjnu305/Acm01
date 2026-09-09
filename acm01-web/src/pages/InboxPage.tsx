import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { ApiError } from '../api/auth'
import {
  acceptFriendRequest,
  fetchIncomingFriendRequests,
  rejectFriendRequest,
  type FriendRequestItem,
} from '../api/friend'
import {
  acceptTeamInvite,
  approveTeamApplicationByMemberId,
  declineTeamInvite,
  rejectTeamApplicationByMemberId,
} from '../api/team'
import {
  fetchChatThread,
  fetchConversations,
  INBOX_PUSH_EVENT,
  isTeamInboxRef,
  markChatThreadRead,
  sendPersonalMessage,
  type ChatMessage,
  type ConversationSummary,
  type InboxPushDetail,
} from '../api/inbox'
import { fetchPublicProfile, friendRelationLabel, searchUsers, type UserSearchItem } from '../api/user'
import ClickableUserAvatar from '../components/ClickableUserAvatar'
import TeamInboxCardActions, { isTeamActionCard } from '../components/inbox/TeamInboxCardActions'
import { useInboxUnread } from '../context/InboxUnreadContext'

interface ChatPeer {
  userId: number
  name: string
  avatar?: string
  official?: boolean
  friendStatus: string
}

function formatSessionTime(iso: string): string {
  const date = new Date(iso)
  const now = new Date()
  const sameDay =
    date.getFullYear() === now.getFullYear() &&
    date.getMonth() === now.getMonth() &&
    date.getDate() === now.getDate()
  if (sameDay) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
}

function formatMessageTime(iso: string): string {
  return new Date(iso).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function formatDateDivider(iso: string): string {
  const date = new Date(iso)
  const now = new Date()
  const sameDay =
    date.getFullYear() === now.getFullYear() &&
    date.getMonth() === now.getMonth() &&
    date.getDate() === now.getDate()
  if (sameDay) return '今天'
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) return '昨天'
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

function shouldShowDateDivider(current: string, previous?: string): boolean {
  if (!previous) return true
  return new Date(current).toDateString() !== new Date(previous).toDateString()
}

function toPeerFromConversation(conv: ConversationSummary): ChatPeer {
  return {
    userId: conv.peerId,
    name: conv.peerName,
    avatar: conv.peerAvatar,
    official: conv.official,
    friendStatus: 'FRIENDS',
  }
}

function toPeerFromSearch(item: UserSearchItem): ChatPeer {
  return {
    userId: item.userId,
    name: item.nickname || item.username,
    avatar: item.avatar,
    official: item.official,
    friendStatus: item.friendStatus,
  }
}

export default function InboxPage() {
  const { refreshUnread } = useInboxUnread()
  const [searchParams, setSearchParams] = useSearchParams()
  const urlPeerId = useMemo(() => {
    const id = Number(searchParams.get('user'))
    return Number.isFinite(id) && id > 0 ? id : null
  }, [searchParams])
  const [conversations, setConversations] = useState<ConversationSummary[]>([])
  const [threadMessages, setThreadMessages] = useState<ChatMessage[]>([])
  const [selectedPeer, setSelectedPeer] = useState<ChatPeer | null>(null)
  const [incoming, setIncoming] = useState<FriendRequestItem[]>([])
  const [loading, setLoading] = useState(true)
  const [threadLoading, setThreadLoading] = useState(false)
  const [error, setError] = useState('')
  const [sendError, setSendError] = useState('')
  const [searchQuery, setSearchQuery] = useState('')
  const [searchResults, setSearchResults] = useState<UserSearchItem[]>([])
  const [searchLoading, setSearchLoading] = useState(false)
  const [threadBody, setThreadBody] = useState('')
  const [threadSending, setThreadSending] = useState(false)
  const [handledTeamCards, setHandledTeamCards] = useState<Set<number>>(() => new Set())
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const composeRef = useRef<HTMLTextAreaElement>(null)

  const isSearching = searchQuery.trim().length > 0

  const canSendMessage =
    selectedPeer != null &&
    selectedPeer.friendStatus === 'FRIENDS' &&
    !selectedPeer.official

  const loadSidebar = useCallback(async () => {
    try {
      const requests = await fetchIncomingFriendRequests()
      setIncoming(requests)
    } catch {
      setIncoming([])
    }
  }, [])

  const loadConversations = useCallback(async () => {
    try {
      const data = await fetchConversations()
      setConversations(data)
    } catch {
      setConversations([])
    }
  }, [])

  const loadThread = useCallback(async (peerId: number) => {
    setThreadLoading(true)
    setError('')
    try {
      const data = await fetchChatThread(peerId)
      setThreadMessages(data)
      await markChatThreadRead(peerId)
      void refreshUnread()
    } catch (err) {
      if (selectedPeer?.userId === peerId) {
        setThreadMessages([])
      }
      if (err instanceof ApiError && err.message) {
        setError(err.message)
      }
    } finally {
      setThreadLoading(false)
    }
  }, [refreshUnread, selectedPeer?.userId])

  useEffect(() => {
    void (async () => {
      setLoading(true)
      await Promise.all([loadSidebar(), loadConversations()])
      setLoading(false)
    })()
  }, [loadSidebar, loadConversations])

  useEffect(() => {
    if (urlPeerId == null || loading) return

    const fromConv = conversations.find((c) => c.peerId === urlPeerId)
    if (fromConv) {
      setSelectedPeer(toPeerFromConversation(fromConv))
      return
    }

    void fetchPublicProfile(urlPeerId)
      .then((profile) => {
        setSelectedPeer({
          userId: profile.userId,
          name: profile.nickname || profile.username,
          avatar: profile.avatar,
          official: profile.official,
          friendStatus: profile.friendStatus ?? 'NONE',
        })
      })
      .catch(() => setError('无法打开该用户的会话'))
  }, [urlPeerId, loading, conversations])

  useEffect(() => {
    if (isSearching || selectedPeer || urlPeerId != null) return
    if (conversations.length > 0) {
      setSelectedPeer(toPeerFromConversation(conversations[0]))
    }
  }, [conversations, isSearching, selectedPeer, urlPeerId])

  useEffect(() => {
    if (!selectedPeer || selectedPeer.friendStatus !== 'FRIENDS') {
      setThreadMessages([])
      return
    }
    void loadThread(selectedPeer.userId)
  }, [selectedPeer?.userId, selectedPeer?.friendStatus, loadThread])

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [threadMessages])

  useEffect(() => {
    if (canSendMessage && selectedPeer) {
      composeRef.current?.focus()
    }
  }, [canSendMessage, selectedPeer?.userId])

  useEffect(() => {
    const keyword = searchQuery.trim()
    if (!keyword) {
      setSearchResults([])
      setSearchLoading(false)
      return
    }
    setSearchLoading(true)
    const timer = window.setTimeout(() => {
      void searchUsers(keyword)
        .then(setSearchResults)
        .catch(() => setSearchResults([]))
        .finally(() => setSearchLoading(false))
    }, 300)
    return () => window.clearTimeout(timer)
  }, [searchQuery])

  useEffect(() => {
    const handler = (event: Event) => {
      const detail = (event as CustomEvent<InboxPushDetail>).detail
      if (detail.inboxRefType && !isTeamInboxRef(detail.inboxRefType)) return
      void loadConversations()
      if (selectedPeer && detail.inboxSenderId === selectedPeer.userId) {
        void loadThread(selectedPeer.userId)
      }
    }
    window.addEventListener(INBOX_PUSH_EVENT, handler)
    return () => window.removeEventListener(INBOX_PUSH_EVENT, handler)
  }, [loadConversations, loadThread, selectedPeer])

  const clearUrlPeer = () => {
    if (searchParams.has('user')) {
      searchParams.delete('user')
      setSearchParams(searchParams, { replace: true })
    }
  }

  const selectConversation = (conv: ConversationSummary) => {
    setSearchQuery('')
    setSendError('')
    clearUrlPeer()
    setSelectedPeer({
      userId: conv.peerId,
      name: conv.peerName,
      avatar: conv.peerAvatar,
      official: conv.official,
      friendStatus: 'FRIENDS',
    })
  }

  const selectSearchResult = (item: UserSearchItem) => {
    setSearchQuery('')
    setSendError('')
    clearUrlPeer()
    setSelectedPeer(toPeerFromSearch(item))
  }

  const handleAccept = async (requestId: number) => {
    try {
      await acceptFriendRequest(requestId)
      await loadSidebar()
      await loadConversations()
      void refreshUnread()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '操作失败')
    }
  }

  const markTeamCardHandled = (memberId: number) => {
    setHandledTeamCards((prev) => new Set(prev).add(memberId))
  }

  const afterTeamCardAction = async () => {
    if (selectedPeer) {
      await loadThread(selectedPeer.userId)
    }
    await loadConversations()
    void refreshUnread()
  }

  const handleRejectTeamInvite = async (memberId: number) => {
    try {
      await declineTeamInvite(memberId)
      markTeamCardHandled(memberId)
      await afterTeamCardAction()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '操作失败')
    }
  }

  const handleAcceptTeamInvite = async (memberId: number) => {
    try {
      await acceptTeamInvite(memberId)
      markTeamCardHandled(memberId)
      await afterTeamCardAction()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '操作失败')
    }
  }

  const handleApproveTeamApply = async (memberId: number) => {
    try {
      await approveTeamApplicationByMemberId(memberId)
      markTeamCardHandled(memberId)
      await afterTeamCardAction()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '操作失败')
    }
  }

  const handleRejectTeamApply = async (memberId: number) => {
    try {
      await rejectTeamApplicationByMemberId(memberId)
      markTeamCardHandled(memberId)
      await afterTeamCardAction()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '操作失败')
    }
  }

  const handleReject = async (requestId: number) => {
    try {
      await rejectFriendRequest(requestId)
      await loadSidebar()
      await loadConversations()
      void refreshUnread()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '操作失败')
    }
  }

  const handleThreadSend = async () => {
    if (!selectedPeer || !canSendMessage) return
    const body = threadBody.trim()
    if (!body) return
    setThreadSending(true)
    setSendError('')
    try {
      const sent = await sendPersonalMessage(selectedPeer.userId, body)
      setThreadMessages((prev) => [...prev, sent])
      setThreadBody('')
      await loadConversations()
      composeRef.current?.focus()
    } catch (err) {
      setSendError(err instanceof ApiError ? err.message : '发送失败')
    } finally {
      setThreadSending(false)
    }
  }

  const handleComposeKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      void handleThreadSend()
    }
  }

  const sessionUnreadMap = useMemo(() => {
    const map = new Map<number, number>()
    for (const conv of conversations) {
      map.set(conv.peerId, conv.unreadCount ?? 0)
    }
    return map
  }, [conversations])

  const displaySessions = useMemo((): ConversationSummary[] => {
    if (!selectedPeer || selectedPeer.friendStatus !== 'FRIENDS') {
      return conversations
    }
    if (conversations.some((c) => c.peerId === selectedPeer.userId)) {
      return conversations
    }
    const draft: ConversationSummary = {
      peerId: selectedPeer.userId,
      peerName: selectedPeer.name,
      peerAvatar: selectedPeer.avatar,
      official: selectedPeer.official,
      lastMessageBody: '暂无消息',
      lastMessageTime: new Date().toISOString(),
      unreadCount: 0,
    }
    return [draft, ...conversations]
  }, [conversations, selectedPeer])

  return (
    <div className="comm-body inbox-page">
      {error && <div className="alert alert-error">{error}</div>}

      <div className="im-shell">
        <aside className="im-sessions">
          <div className="im-sessions-head">
            <h2>消息</h2>
          </div>

          <div className="im-search-wrap">
            <input
              type="search"
              className="im-search-input"
              placeholder="搜索好友或用户"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>

          {incoming.length > 0 && !isSearching && (
            <div className="im-requests">
              <p className="im-requests-label">好友申请 · {incoming.length}</p>
              {incoming.map((req) => (
                <div key={req.id} className="im-request-row">
                  <ClickableUserAvatar
                    user={{
                      userId: req.requesterId,
                      username: req.requesterName,
                      nickname: req.requesterName,
                      avatar: req.requesterAvatar,
                      roles: [],
                    }}
                    className="im-avatar"
                  />
                  <div className="im-request-meta">
                    <strong>{req.requesterName}</strong>
                    <span>{req.message || '请求添加你为好友'}</span>
                  </div>
                  <div className="im-request-btns">
                    <button type="button" onClick={() => void handleAccept(req.id)}>同意</button>
                    <button type="button" className="muted" onClick={() => void handleReject(req.id)}>拒绝</button>
                  </div>
                </div>
              ))}
            </div>
          )}

          <div className="im-session-list">
            {isSearching ? (
              searchLoading ? (
                <div className="im-loading">
                  <div className="boot-spinner" />
                  <span>搜索中…</span>
                </div>
              ) : searchResults.length === 0 ? (
                <div className="im-empty-sessions">
                  <p>未找到用户</p>
                  <span>试试昵称或用户名</span>
                </div>
              ) : (
                searchResults.map((item) => {
                  const peer = toPeerFromSearch(item)
                  const relation = friendRelationLabel(item.friendStatus, item.official)
                  const isNonFriend = item.friendStatus === 'NONE'
                  return (
                    <button
                      key={item.userId}
                      type="button"
                      className={`im-search-card${selectedPeer?.userId === item.userId ? ' active' : ''}`}
                      onClick={() => selectSearchResult(item)}
                    >
                      <ClickableUserAvatar
                        user={{
                          userId: item.userId,
                          username: item.username,
                          nickname: item.nickname ?? item.username,
                          avatar: item.avatar,
                          roles: [],
                        }}
                        className="im-avatar"
                        stopPropagation
                      />
                      <div className="im-search-card-body">
                        <div className="im-search-card-top">
                          <strong>{peer.name}</strong>
                          <span className={`im-relation-tag${isNonFriend ? ' stranger' : ''}`}>
                            {relation}
                          </span>
                        </div>
                        <p>@{item.username}{item.school ? ` · ${item.school}` : ''}</p>
                      </div>
                    </button>
                  )
                })
              )
            ) : loading ? (
              <div className="im-loading">
                <div className="boot-spinner" />
                <span>加载中…</span>
              </div>
            ) : displaySessions.length === 0 ? (
              <div className="im-empty-sessions">
                <p>暂无会话</p>
                <span>搜索用户开始聊天</span>
              </div>
            ) : (
              displaySessions.map((conv) => {
                const unread = sessionUnreadMap.get(conv.peerId) ?? 0
                const preview = conv.lastMessageBody || conv.lastMessageTitle || ''
                const isDraft = conv.lastMessageBody === '暂无消息' &&
                  !conversations.some((c) => c.peerId === conv.peerId)
                return (
                  <button
                    key={conv.peerId}
                    type="button"
                    className={`im-session${selectedPeer?.userId === conv.peerId ? ' active' : ''}${unread > 0 ? ' unread' : ''}`}
                    onClick={() => selectConversation(conv)}
                  >
                    <span className="im-avatar-wrap">
                      <ClickableUserAvatar
                        user={{
                          userId: conv.peerId,
                          username: conv.peerName,
                          nickname: conv.peerName,
                          avatar: conv.peerAvatar,
                          roles: [],
                        }}
                        className="im-avatar"
                        stopPropagation
                      />
                      {unread > 0 && <span className="im-unread-dot" aria-label="未读消息" />}
                    </span>
                    <div className="im-session-body">
                      <div className="im-session-top">
                        <span className="im-session-name">
                          {conv.peerName}
                          {conv.official && <em className="im-official-tag">官方</em>}
                        </span>
                        {!isDraft && <time>{formatSessionTime(conv.lastMessageTime)}</time>}
                      </div>
                      <p className={`im-session-preview${isDraft ? ' muted' : ''}`}>{preview}</p>
                    </div>
                  </button>
                )
              })
            )}
          </div>
        </aside>

        <section className="im-thread wx-chat">
          {!selectedPeer ? (
            <div className="im-thread-empty wx-chat-idle">
              <div className="wx-chat-idle-icon" aria-hidden />
              <h3>微信电脑版</h3>
              <p>选择一个会话开始聊天</p>
            </div>
          ) : (
            <>
              <header className="wx-chat-head">
                <div className="wx-chat-head-main">
                  <h3>{selectedPeer.name}</h3>
                  {selectedPeer.official && <span className="wx-chat-official">官方</span>}
                </div>
                <Link to={`/users/${selectedPeer.userId}`} className="wx-chat-head-link" title="查看资料">
                  ···
                </Link>
              </header>

              {!canSendMessage && (
                <div className="im-thread-hint wx-chat-hint">
                  {selectedPeer.friendStatus === 'NONE' && (
                    <>
                      对方不是你的好友，需先添加好友才能发消息。
                      <Link to={`/users/${selectedPeer.userId}`}>去添加好友</Link>
                    </>
                  )}
                  {selectedPeer.friendStatus === 'PENDING_SENT' && '好友申请已发送，等待对方同意'}
                  {selectedPeer.friendStatus === 'PENDING_RECEIVED' && (
                    <>
                      对方已向你发送好友申请，
                      <Link to={`/users/${selectedPeer.userId}`}>前往处理</Link>
                    </>
                  )}
                  {selectedPeer.official && '官方消息仅支持查看，无法回复'}
                </div>
              )}

              <div className="wx-chat-body">
                {threadLoading ? (
                  <div className="wx-chat-loading">
                    <div className="boot-spinner" />
                  </div>
                ) : threadMessages.length === 0 ? (
                  <div className="wx-chat-empty-hint">
                    <span>你们还没有聊过天，打个招呼吧</span>
                  </div>
                ) : (
                  threadMessages.map((msg, index) => {
                    const prev = threadMessages[index - 1]
                    const showDate = shouldShowDateDivider(msg.createdTime, prev?.createdTime)
                    const isCard = isTeamActionCard(msg)
                    const cardHandled = msg.refId != null && handledTeamCards.has(msg.refId)

                    return (
                      <div key={msg.id}>
                        {showDate && (
                          <div className="wx-chat-date">
                            <span>{formatDateDivider(msg.createdTime)} {formatMessageTime(msg.createdTime)}</span>
                          </div>
                        )}
                        <div className={`wx-chat-row${msg.mine ? ' mine' : ''}`}>
                          {!msg.mine && (
                            <ClickableUserAvatar
                              user={{
                                userId: msg.senderId,
                                username: msg.senderName ?? '',
                                nickname: msg.senderName ?? '',
                                avatar: msg.senderAvatar,
                                roles: [],
                              }}
                              className="im-avatar wx-chat-avatar"
                            />
                          )}
                          <div className={`wx-chat-bubble${isCard ? ' wx-chat-card' : ''}`}>
                            {msg.title ? <strong className="wx-chat-bubble-title">{msg.title}</strong> : null}
                            <p>{msg.body}</p>
                            <TeamInboxCardActions
                              msg={msg}
                              handled={cardHandled}
                              onAcceptInvite={(id) => void handleAcceptTeamInvite(id)}
                              onDeclineInvite={(id) => void handleRejectTeamInvite(id)}
                              onApproveApply={(id) => void handleApproveTeamApply(id)}
                              onRejectApply={(id) => void handleRejectTeamApply(id)}
                            />
                          </div>
                          {msg.mine && (
                            <ClickableUserAvatar
                              user={{
                                userId: msg.senderId,
                                username: msg.senderName ?? '',
                                nickname: msg.senderName ?? '',
                                avatar: msg.senderAvatar,
                                roles: [],
                              }}
                              className="im-avatar wx-chat-avatar"
                            />
                          )}
                        </div>
                      </div>
                    )
                  })
                )}
                <div ref={messagesEndRef} />
              </div>

              {canSendMessage && (
                <footer className="wx-chat-compose">
                  <textarea
                    ref={composeRef}
                    className="wx-chat-input"
                    placeholder=""
                    value={threadBody}
                    onChange={(e) => setThreadBody(e.target.value)}
                    onKeyDown={handleComposeKeyDown}
                    maxLength={2000}
                    rows={4}
                  />
                  <div className="wx-chat-send-row">
                    <span className="wx-chat-send-tip">Enter 发送 · Shift+Enter 换行</span>
                    {sendError && <span className="wx-chat-send-error">{sendError}</span>}
                    <button
                      type="button"
                      className={`wx-chat-send${threadBody.trim() ? ' active' : ''}`}
                      disabled={threadSending || !threadBody.trim()}
                      onClick={() => void handleThreadSend()}
                    >
                      {threadSending ? '发送中' : '发送(S)'}
                    </button>
                  </div>
                </footer>
              )}
            </>
          )}
        </section>
      </div>
    </div>
  )
}
