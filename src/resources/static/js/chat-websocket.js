// WebSocket Chat Client with STOMP
let stompClient = null;
let currentChatRoomId = null;
let currentUserId = null;
let typingTimeout = null;
let subscriptions = {}; // Track active subscriptions to prevent duplicates
let videoSubscriptions = {}; // Per-room video subscriptions, kept across conversation switches
let currentUserReadyResolve;
const REACTION_OPTIONS = ['\uD83D\uDC9C', '\uD83D\uDE02', '\uD83D\uDE2D', '\uD83D\uDE21', '\uD83D\uDD25', '\uD83D\uDC4D'];
let messageUserReactions = {};
let presenceSubscription = null;
window.currentUserReadyPromise = new Promise(resolve => {
    currentUserReadyResolve = resolve;
});

// Initialize WebSocket connection
function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        // Reset video subscriptions on reconnect so they can be re-established
        videoSubscriptions = {};

        // Subscribe to chat room messages if we have an active chat
        if (currentChatRoomId) {
            subscribeToChat(currentChatRoomId);
        }

        // Subscribe to video topics for all conversations already in the DOM
        subscribeVideoTopicsFromDOM();

        subscribeToPresence();
    }, function (error) {
        console.error('WebSocket connection error:', error);
        // Retry connection after 5 seconds
        setTimeout(connectWebSocket, 5000);
    });
}

function subscribeToPresence() {
    if (!stompClient || !stompClient.connected) {
        return;
    }

    if (presenceSubscription) {
        presenceSubscription.unsubscribe();
    }

    presenceSubscription = stompClient.subscribe('/topic/presence', function (presenceEvent) {
        const payload = JSON.parse(presenceEvent.body);
        if (typeof window.updateConversationPresence === 'function') {
            window.updateConversationPresence(payload.userId, payload.online === true);
        }
    });
}

// Subscribe to chat room topics
function subscribeToChat(chatRoomId) {
    // Unsubscribe from previous subscriptions to prevent duplicates
    unsubscribeFromAll();
    
    currentChatRoomId = chatRoomId;

    console.log('Subscribing to chat room:', chatRoomId);

    // Subscribe to chat messages
    subscriptions.messages = stompClient.subscribe('/topic/chat/' + chatRoomId, function (message) {
        const chatMessage = JSON.parse(message.body);
        console.log('Message received:', chatMessage);
        displayMessage(chatMessage);

        if (typeof window.updateConversationLastMessage === 'function') {
            window.updateConversationLastMessage(chatMessage.chatRoomId, chatMessage.content, chatMessage.timestamp);
        }

        // Mark as delivered if it's not our message
        if (chatMessage.senderId !== currentUserId) {
            markAsDelivered(chatMessage.id);
        }
    });

    // Subscribe to message status updates
    subscriptions.status = stompClient.subscribe('/topic/chat/status/*', function (statusUpdate) {
        const status = JSON.parse(statusUpdate.body);
        updateMessageStatus(status.messageId, status.status);
    });

    // Subscribe to reactions
    subscriptions.reactions = stompClient.subscribe('/topic/chat/reactions/*', function (reactionUpdate) {
        const reaction = JSON.parse(reactionUpdate.body);
        updateMessageReaction(reaction);
    });

    // Subscribe to typing indicators
    subscriptions.typing = stompClient.subscribe('/topic/chat/typing/' + chatRoomId, function (typingData) {
        const typing = JSON.parse(typingData.body);
        if (typing.userId !== currentUserId) {
            showTypingIndicator(typing);
        }
    });

    console.log('Active subscriptions:', Object.keys(subscriptions).length);
}

function showJoinCallSection(payload) {
    console.log('[VIDEO] showJoinCallSection called, payload:', payload, 'currentUserId:', currentUserId);
    const messagesContainer = document.getElementById('messagesContainer');
    if (!messagesContainer) {
        console.warn('[VIDEO] messagesContainer not found, cannot show join-call section');
        return;
    }

    if (payload && payload.createdByUserId != null && String(payload.createdByUserId) === String(currentUserId)) {
        console.log('[VIDEO] Suppressing join-call section for call initiator (userId=' + currentUserId + ')');
        return;
    }

    const meetingUrl = payload && payload.meetingUrl ? payload.meetingUrl : null;
    const message = 'Alătură-te apelului';

    const existing = document.getElementById('join-call-section');
    if (existing) {
        existing.remove();
    }

    const section = document.createElement('div');
    section.id = 'join-call-section';
    section.className = 'join-call-section';

    const text = document.createElement('span');
    text.className = 'join-call-section__text';
    text.textContent = message;
    section.appendChild(text);

    if (meetingUrl) {
        const joinButton = document.createElement('button');
        joinButton.type = 'button';
        joinButton.className = 'join-call-section__btn';
        joinButton.textContent = 'Alătură-te';
        joinButton.addEventListener('click', function () {
            const newWindow = window.open(meetingUrl, '_blank', 'noopener,noreferrer');
            if (!newWindow) {
                window.alert('Browserul a blocat pop-up-ul. Permite pop-up-uri pentru acest site si incearca din nou.');
            }
        });
        section.appendChild(joinButton);
    }

    messagesContainer.appendChild(section);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

// Subscribe to video topic for a single conversation room (idempotent)
function subscribeVideoRoom(roomId) {
    const key = String(roomId);
    if (videoSubscriptions[key]) return; // already subscribed
    if (!stompClient || !stompClient.connected) return;

    console.log('[VIDEO] Subscribing to /topic/chat/' + key + '/video');
    videoSubscriptions[key] = stompClient.subscribe('/topic/chat/' + key + '/video', function (videoData) {
        console.log('[VIDEO] Event received on room', key, videoData.body);
        const payload = JSON.parse(videoData.body);
        console.log('[VIDEO] Parsed payload:', payload, 'currentUserId:', currentUserId, 'currentChatRoomId:', currentChatRoomId);
        handleVideoSessionReady(payload, key);
    });
}

// Subscribe to video topics for ALL conversations visible in the sidebar
function subscribeVideoTopicsFromDOM() {
    if (!stompClient || !stompClient.connected) return;
    document.querySelectorAll('[data-conversation-id]').forEach(function (item) {
        const roomId = item.dataset.conversationId;
        if (roomId) subscribeVideoRoom(roomId);
    });
}

// Unsubscribe from all video subscriptions (call on logout / full reset only)
function unsubscribeAllVideoTopics() {
    Object.keys(videoSubscriptions).forEach(function (key) {
        if (videoSubscriptions[key]) videoSubscriptions[key].unsubscribe();
    });
    videoSubscriptions = {};
}

// Handle incoming video session event
function handleVideoSessionReady(payload, roomId) {
    if (payload && payload.createdByUserId != null && String(payload.createdByUserId) === String(currentUserId)) {
        console.log('[VIDEO] Suppressing notification for call initiator');
        return;
    }

    if (String(roomId) === String(currentChatRoomId)) {
        // User is already looking at this conversation – show inline section
        showJoinCallSection(payload);
    } else {
        // Call is in a different conversation – highlight corresponding sidebar item
        const convItem = document.querySelector('[data-conversation-id="' + roomId + '"]');
        if (convItem) {
            convItem.classList.add('has-incoming-call');
            convItem.dataset.pendingVideoUrl = payload.meetingUrl || '';
            console.log('[VIDEO] Highlighted sidebar item for conversation', roomId);
        }
    }
}

// Unsubscribe from all active subscriptions
function unsubscribeFromAll() {
    console.log('Unsubscribing from all topics');
    Object.keys(subscriptions).forEach(key => {
        if (subscriptions[key]) {
            subscriptions[key].unsubscribe();
        }
    });
    subscriptions = {};
    // NOTE: videoSubscriptions intentionally NOT cleared here – they survive conversation switches
}

// Send message
function sendMessage(content) {
    if (!content.trim() || !currentChatRoomId) return;

    const message = {
        chatRoomId: currentChatRoomId,
        content: content,
        timestamp: new Date().toISOString()
    };

    stompClient.send("/app/chat.send", {}, JSON.stringify(message));
}

// Mark message as delivered
function markAsDelivered(messageId) {
    const statusUpdate = {
        messageId: messageId
    };
    stompClient.send("/app/chat.delivered", {}, JSON.stringify(statusUpdate));
}

// Mark message as seen
function markAsSeen(messageId) {
    const statusUpdate = {
        messageId: messageId
    };
    stompClient.send("/app/chat.seen", {}, JSON.stringify(statusUpdate));
}

// Add reaction to message
function addReaction(messageId, emoji) {
    const reaction = {
        messageId: messageId,
        emoji: emoji
    };
    stompClient.send("/app/chat.react", {}, JSON.stringify(reaction));
}

function removeReaction(messageId) {
    const reaction = {
        messageId: messageId
    };
    stompClient.send("/app/chat.unreact", {}, JSON.stringify(reaction));
}

// Send typing indicator
function sendTypingIndicator(isTyping) {
    if (!currentChatRoomId) return;

    const typing = {
        chatRoomId: currentChatRoomId,
        isTyping: isTyping
    };
    stompClient.send("/app/chat.typing", {}, JSON.stringify(typing));
}

function getCurrentChatPartnerAvatar() {
    return window.currentChatPartnerAvatar || '/img/default-avatar.png';
}

function getStatusLabel(status) {
    switch (status) {
        case 'SEEN':
            return 'Vazut';
        case 'DELIVERED':
            return 'livrat';
        case 'SENT':
        default:
            return 'trimis';
    }
}

function refreshOwnMessageStatusVisibility() {
    const ownStatusLines = document.querySelectorAll('.message-outgoing .message-status-line');
    if (!ownStatusLines.length) {
        return;
    }

    ownStatusLines.forEach((line, index) => {
        line.style.display = index === ownStatusLines.length - 1 ? 'block' : 'none';
    });
}

// Display message in UI
function displayMessage(message) {
    const messagesContainer = document.getElementById('messagesContainer');
    const isOwnMessage = message.senderId === currentUserId;

    const messageDiv = document.createElement('div');
    messageDiv.className = 'message ' + (isOwnMessage ? 'message-outgoing' : 'message-incoming');
    messageDiv.dataset.messageId = message.id;

    let messageHTML = '';

    if (!isOwnMessage) {
        messageHTML += `
            <div class="message-avatar">
                <img src="${getCurrentChatPartnerAvatar()}" alt="${message.senderName}">
            </div>
        `;
    }

    messageHTML += `
        <div class="message-content">
            <div class="message-bubble">
                <div class="message-main-row">
                    <p>${escapeHtml(message.content)}</p>
                    <span class="message-time">${formatTime(message.timestamp)}</span>
                </div>
                <div class="reaction-picker" id="reaction-picker-${message.id}">
                    ${REACTION_OPTIONS.map(emoji => `
                        <button type="button" class="emoji-option ${emoji === REACTION_OPTIONS[0] ? 'emoji-option-heart' : ''}" onclick="selectReaction(event, ${message.id}, '${emoji}')">
                            ${emoji === REACTION_OPTIONS[0] ? '<i class="fa fa-heart"></i>' : emoji}
                        </button>
                    `).join('')}
                </div>
                <button type="button" class="reaction-trigger" onclick="toggleReactionPicker(event, ${message.id})" aria-label="React">
                    <i class="fa fa-smile-o"></i>
                </button>
            </div>
            <div class="message-reactions-row">
                <div class="message-reactions" id="reactions-${message.id}"></div>
            </div>
            ${isOwnMessage ? '<div class="message-status-line" id="status-' + message.id + '">' + getStatusLabel(message.status) + '</div>' : ''}
        </div>
    `;

    messageDiv.innerHTML = messageHTML;
    messagesContainer.appendChild(messageDiv);
    refreshOwnMessageStatusVisibility();

    // Display existing reactions if any
    if (message.reactions && message.reactions.length > 0) {
        const reactionsContainer = document.getElementById('reactions-' + message.id);
        message.reactions.forEach(reaction => {
            const emojiSpan = document.createElement('span');
            emojiSpan.className = 'reaction-emoji';
            emojiSpan.dataset.emoji = reaction.emoji;
            emojiSpan.dataset.count = reaction.count;
            emojiSpan.textContent = reaction.emoji + ' ' + reaction.count;
            emojiSpan.title = 'Click to react';
            emojiSpan.addEventListener('click', function () {
                const messageIdKey = String(message.id);
                const currentUserKey = currentUserId != null ? String(currentUserId) : null;
                if (!currentUserKey) {
                    return;
                }
                if (messageUserReactions[messageIdKey] && messageUserReactions[messageIdKey][currentUserKey] === reaction.emoji) {
                    removeReaction(message.id);
                }
            });
            if (reaction.currentUserReacted) {
                emojiSpan.classList.add('user-reacted');
                if (currentUserId) {
                    if (!messageUserReactions[message.id]) {
                        messageUserReactions[message.id] = {};
                    }
                    messageUserReactions[message.id][String(currentUserId)] = reaction.emoji;
                }
            }
            reactionsContainer.appendChild(emojiSpan);
        });
    }

    // Scroll to bottom
    messagesContainer.scrollTop = messagesContainer.scrollHeight;

    // Mark as seen if it's visible and not our message
    if (!isOwnMessage) {
        setTimeout(() => markAsSeen(message.id), 1000);
    }
}

// Update message status indicator
function updateMessageStatus(messageId, status) {
    const statusElement = document.getElementById('status-' + messageId);
    if (!statusElement) return;

    statusElement.textContent = getStatusLabel(status);
    statusElement.style.color = status === 'SEEN'
        ? '#7fd4ff'
        : status === 'DELIVERED'
            ? 'rgba(176, 220, 255, 0.82)'
            : 'rgba(236, 242, 255, 0.65)';

    refreshOwnMessageStatusVisibility();
}

// Update message reactions
function updateMessageReaction(reaction) {
    const reactionsContainer = document.getElementById('reactions-' + reaction.messageId);
    if (!reactionsContainer) return;

    const messageIdKey = String(reaction.messageId);
    const userIdKey = reaction.userId != null ? String(reaction.userId) : null;
    if (!messageUserReactions[messageIdKey]) {
        messageUserReactions[messageIdKey] = {};
    }

    const previousEmoji = userIdKey ? messageUserReactions[messageIdKey][userIdKey] : null;
    const userIsCurrent = currentUserId != null && userIdKey === String(currentUserId);

    if (reaction.removed === true) {
        const removedEmoji = reaction.emoji || previousEmoji;
        if (removedEmoji) {
            const removedEmojiSpan = Array.from(reactionsContainer.children).find(
                span => span.dataset.emoji === removedEmoji
            );
            if (removedEmojiSpan) {
                const currentCount = parseInt(removedEmojiSpan.dataset.count, 10) || 1;
                const nextCount = currentCount - 1;
                if (nextCount <= 0) {
                    removedEmojiSpan.remove();
                } else {
                    removedEmojiSpan.dataset.count = String(nextCount);
                    removedEmojiSpan.textContent = removedEmoji + ' ' + nextCount;
                }
                if (userIsCurrent) {
                    removedEmojiSpan.classList.remove('user-reacted');
                }
            }
        }
        if (userIdKey && messageUserReactions[messageIdKey]) {
            delete messageUserReactions[messageIdKey][userIdKey];
        }
        return;
    }

    if (previousEmoji && previousEmoji !== reaction.emoji) {
        const previousEmojiSpan = Array.from(reactionsContainer.children).find(
            span => span.dataset.emoji === previousEmoji
        );
        if (previousEmojiSpan) {
            const previousCount = parseInt(previousEmojiSpan.dataset.count, 10) || 1;
            const nextCount = previousCount - 1;
            if (nextCount <= 0) {
                previousEmojiSpan.remove();
            } else {
                previousEmojiSpan.dataset.count = String(nextCount);
                previousEmojiSpan.textContent = previousEmoji + ' ' + nextCount;
            }
            if (userIsCurrent) {
                previousEmojiSpan.classList.remove('user-reacted');
            }
        }
    } else if (previousEmoji && previousEmoji === reaction.emoji) {
        const sameEmojiSpan = Array.from(reactionsContainer.children).find(
            span => span.dataset.emoji === reaction.emoji
        );
        if (sameEmojiSpan && userIsCurrent) {
            sameEmojiSpan.classList.add('user-reacted');
        }
        return;
    }

    const existingEmoji = Array.from(reactionsContainer.children).find(
        span => span.dataset.emoji === reaction.emoji
    );

    if (existingEmoji) {
        const currentCount = parseInt(existingEmoji.dataset.count, 10) || 1;
        existingEmoji.dataset.count = String(currentCount + 1);
        existingEmoji.textContent = reaction.emoji + ' ' + (currentCount + 1);
        
        const currentTitle = existingEmoji.title || '';
        existingEmoji.title = currentTitle ? currentTitle + ', ' + reaction.userName : reaction.userName;
        if (userIsCurrent) {
            existingEmoji.classList.add('user-reacted');
        }
    } else {
        const emojiSpan = document.createElement('span');
        emojiSpan.className = 'reaction-emoji';
        emojiSpan.dataset.emoji = reaction.emoji;
        emojiSpan.dataset.count = '1';
        emojiSpan.textContent = reaction.emoji + ' 1';
        emojiSpan.title = reaction.userName;
        emojiSpan.addEventListener('click', function () {
            const currentUserKey = currentUserId != null ? String(currentUserId) : null;
            if (!currentUserKey) {
                return;
            }
            if (messageUserReactions[messageIdKey] && messageUserReactions[messageIdKey][currentUserKey] === reaction.emoji) {
                removeReaction(reaction.messageId);
            }
        });
        if (userIsCurrent) {
            emojiSpan.classList.add('user-reacted');
        }
        reactionsContainer.appendChild(emojiSpan);
    }

    if (userIdKey) {
        messageUserReactions[messageIdKey][userIdKey] = reaction.emoji;
    }
}

// Show typing indicator
function showTypingIndicator(typing) {
    const messagesContainer = document.getElementById('messagesContainer');
    let typingIndicator = document.getElementById('typing-indicator');

    if (typing.isTyping) {
        if (!typingIndicator) {
            typingIndicator = document.createElement('div');
            typingIndicator.id = 'typing-indicator';
            typingIndicator.className = 'typing-indicator';
            typingIndicator.innerHTML = `
                <div class="message message-incoming">
                    <div class="message-avatar">
                        <img src="${getCurrentChatPartnerAvatar()}" alt="User">
                    </div>
                    <div class="message-content">
                        <div class="message-bubble">
                            <div class="typing-dots">
                                <span></span><span></span><span></span>
                            </div>
                        </div>
                    </div>
                </div>
            `;
            messagesContainer.appendChild(typingIndicator);
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }

        // Auto-hide after 3 seconds
        clearTimeout(typingTimeout);
        typingTimeout = setTimeout(() => {
            if (typingIndicator) {
                typingIndicator.remove();
            }
        }, 3000);
    } else {
        if (typingIndicator) {
            typingIndicator.remove();
        }
    }
}

function closeAllReactionPickers(exceptMessageId = null) {
    document.querySelectorAll('.reaction-picker.open').forEach(picker => {
        if (exceptMessageId && picker.id === `reaction-picker-${exceptMessageId}`) {
            return;
        }
        picker.classList.remove('open');
        picker.classList.remove('reaction-picker-below');
        picker.style.left = '';
        picker.style.right = '';
        picker.style.transform = '';
    });
}

function positionReactionPicker(picker) {
    if (!picker) {
        return;
    }

    const messageElement = picker.closest('.message');
    const isOutgoing = messageElement && messageElement.classList.contains('message-outgoing');
    const messagesContainer = picker.closest('.messages-container');

    picker.classList.remove('reaction-picker-below');
    picker.style.left = isOutgoing ? 'auto' : '0';
    picker.style.right = isOutgoing ? '0' : 'auto';
    picker.style.transform = 'translate(0, 0)';

    const viewportPadding = 8;
    const rect = picker.getBoundingClientRect();
    const boundsRect = messagesContainer
        ? messagesContainer.getBoundingClientRect()
        : { left: 0, right: window.innerWidth };
    const minLeft = Math.max(boundsRect.left + viewportPadding, viewportPadding);
    const maxRight = Math.min(boundsRect.right - viewportPadding, window.innerWidth - viewportPadding);
    let offsetX = 0;

    if (rect.right > maxRight) {
        offsetX -= rect.right - maxRight;
    }

    if (rect.left < minLeft) {
        offsetX += minLeft - rect.left;
    }

    picker.style.transform = `translate(${offsetX}px, 0)`;

    const positionedRect = picker.getBoundingClientRect();
    const minTop = messagesContainer
        ? Math.max(boundsRect.top + viewportPadding, viewportPadding)
        : viewportPadding;

    if (positionedRect.top < minTop) {
        picker.classList.add('reaction-picker-below');
    }
}

function toggleReactionPicker(event, messageId) {
    event.preventDefault();
    event.stopPropagation();

    const picker = document.getElementById(`reaction-picker-${messageId}`);
    if (!picker) {
        return;
    }

    const willOpen = !picker.classList.contains('open');
    closeAllReactionPickers(messageId);
    picker.classList.toggle('open', willOpen);

    if (willOpen) {
        requestAnimationFrame(() => positionReactionPicker(picker));
    }
}

function selectReaction(event, messageId, emoji) {
    event.preventDefault();
    event.stopPropagation();
    const messageIdKey = String(messageId);
    const currentUserKey = currentUserId != null ? String(currentUserId) : null;
    const previousEmoji = currentUserKey && messageUserReactions[messageIdKey]
        ? messageUserReactions[messageIdKey][currentUserKey]
        : null;

    if (previousEmoji && previousEmoji === emoji) {
        removeReaction(messageId);
    } else {
        addReaction(messageId, emoji);
    }
    closeAllReactionPickers();
}

// Utility functions
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatTime(timestamp) {
    const date = new Date(timestamp);
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function () {
    document.addEventListener('click', function () {
        closeAllReactionPickers();
    });

    window.addEventListener('resize', function () {
        document.querySelectorAll('.reaction-picker.open').forEach(positionReactionPicker);
    });

    // Get current user ID from backend API
    fetch('/api/chat/current-user')
        .then(response => response.json())
        .then(data => {
            currentUserId = data.userId;
            console.log('Current user ID:', currentUserId);
            currentUserReadyResolve(true);
        })
        .catch(error => {
            console.error('Error fetching current user:', error);
            currentUserId = null;
            currentUserReadyResolve(false);
        });

    // Connect to WebSocket
    connectWebSocket();

    // Handle message form submission
    const messageForm = document.getElementById('messageForm');
    const messageInput = document.getElementById('messageInput');
    const messageInputArea = document.querySelector('.message-input-area');

    function syncMobileComposerOffset() {
        if (!messageInputArea) {
            return;
        }

        const composerHeight = Math.ceil(messageInputArea.getBoundingClientRect().height);
        if (composerHeight <= 0) {
            return;
        }
        document.documentElement.style.setProperty('--chat-mobile-input-height', `${composerHeight}px`);
    }

    window.addEventListener('chat:window-opened', function () {
        requestAnimationFrame(() => {
            autoResizeMessageInput();
            syncMobileComposerOffset();

            const messagesContainer = document.getElementById('messagesContainer');
            if (messagesContainer) {
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
            }
        });
    });

    function autoResizeMessageInput() {
        if (!messageInput) {
            return;
        }

        messageInput.style.height = 'auto';

        const computed = window.getComputedStyle(messageInput);
        const lineHeight = parseFloat(computed.lineHeight) || 22;
        const borderHeight = (parseFloat(computed.borderTopWidth) || 0) + (parseFloat(computed.borderBottomWidth) || 0);
        const paddingHeight = (parseFloat(computed.paddingTop) || 0) + (parseFloat(computed.paddingBottom) || 0);
        const cssMinHeight = parseFloat(computed.minHeight) || 0;
        const minHeight = Math.max(cssMinHeight, lineHeight + borderHeight + paddingHeight);
        const maxHeight = (lineHeight * 5) + borderHeight + paddingHeight;
        const isEmpty = messageInput.value.trim().length === 0;
        const contentHeight = isEmpty ? minHeight : messageInput.scrollHeight;
        const nextHeight = Math.max(minHeight, Math.min(contentHeight, maxHeight));

        messageInput.style.height = `${nextHeight}px`;
        messageInput.style.overflowY = !isEmpty && messageInput.scrollHeight > maxHeight ? 'auto' : 'hidden';
    }

    if (messageForm) {
        messageForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const content = messageInput.value.trim();
            if (content) {
                sendMessage(content);
                messageInput.value = '';
                autoResizeMessageInput();
                syncMobileComposerOffset();
                sendTypingIndicator(false);
            }
        });
    }

    // Handle typing indicator
    if (messageInput) {
        let typingTimer;

        messageInput.addEventListener('keydown', function (event) {
            if (event.key !== 'Enter') {
                return;
            }

            // Enter sends the message, Shift+Enter keeps newline behavior.
            if (event.shiftKey) {
                return;
            }

            event.preventDefault();

            const content = messageInput.value.trim();
            if (!content) {
                return;
            }

            sendMessage(content);
            messageInput.value = '';
            autoResizeMessageInput();
            syncMobileComposerOffset();
            sendTypingIndicator(false);
        });

        messageInput.addEventListener('input', function () {
            autoResizeMessageInput();
            syncMobileComposerOffset();

            clearTimeout(typingTimer);
            sendTypingIndicator(true);

            typingTimer = setTimeout(() => {
                sendTypingIndicator(false);
            }, 2000);
        });

        autoResizeMessageInput();
        syncMobileComposerOffset();
        window.addEventListener('resize', autoResizeMessageInput);
        window.addEventListener('resize', syncMobileComposerOffset);
    }

    // Check if roomId is in URL (coming from announce-details)
    const urlParams = new URLSearchParams(window.location.search);
    const roomIdFromUrl = urlParams.get('roomId');
    if (roomIdFromUrl) {
        // Wait for WebSocket to connect, then load the room
        const checkConnection = setInterval(() => {
            if (stompClient && stompClient.connected && currentUserId) {
                clearInterval(checkConnection);
                loadChatRoom(roomIdFromUrl);
            }
        }, 100);
    }
});

// Load chat room
function loadChatRoom(chatRoomId) {
    currentChatRoomId = chatRoomId;
    messageUserReactions = {};

    // Clear current messages
    const messagesContainer = document.getElementById('messagesContainer');
    messagesContainer.innerHTML = '';

    // Subscribe to new chat room
    if (stompClient && stompClient.connected) {
        subscribeToChat(chatRoomId);
        // Ensure video subscription for this room exists
        subscribeVideoRoom(chatRoomId);
    }

    // Load chat history from API
    fetch(`/api/chat/history/${chatRoomId}`)
        .then(response => response.json())
        .then(messages => {
            messages.reverse().forEach(message => displayMessage(message));
        })
        .catch(error => console.error('Error loading chat history:', error));

    // Update active conversation in sidebar
    document.querySelectorAll('.conversation-item').forEach(item => {
        item.classList.remove('active');
    });
    const activeConversation = document.querySelector(`[data-conversation-id="${chatRoomId}"]`);
    if (activeConversation) {
        activeConversation.classList.add('active');

        // If there was a pending video call notification for this conversation, show it now
        const pendingVideoUrl = activeConversation.dataset.pendingVideoUrl;
        if (pendingVideoUrl) {
            activeConversation.classList.remove('has-incoming-call');
            delete activeConversation.dataset.pendingVideoUrl;
            showJoinCallSection({ meetingUrl: pendingVideoUrl, createdByUserId: null });
        }
    }
}

// Show welcome message for new conversations
function showWelcomeMessage(userName) {
    const messagesContainer = document.getElementById('messagesContainer');
    const welcomeDiv = document.createElement('div');
    welcomeDiv.className = 'welcome-message';
    welcomeDiv.innerHTML = `
        <div class="welcome-content">
            <h3>👋 Bun venit!</h3>
            <p>Începe conversația cu ${userName}</p>
        </div>
    `;
    messagesContainer.appendChild(welcomeDiv);
}
