// WebSocket Chat Client with STOMP
let stompClient = null;
let currentChatRoomId = null;
let currentUserId = null;
let typingTimeout = null;
let subscriptions = {}; // Track active subscriptions to prevent duplicates
let currentUserReadyResolve;
const REACTION_OPTIONS = ['\uD83D\uDC9C', '\uD83D\uDE02', '\uD83D\uDE2D', '\uD83D\uDE21', '\uD83D\uDD25', '\uD83D\uDC4D'];
let messageUserReactions = {};
window.currentUserReadyPromise = new Promise(resolve => {
    currentUserReadyResolve = resolve;
});

// Initialize WebSocket connection
function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        // Subscribe to chat room messages if we have an active chat
        if (currentChatRoomId) {
            subscribeToChat(currentChatRoomId);
        }
    }, function (error) {
        console.error('WebSocket connection error:', error);
        // Retry connection after 5 seconds
        setTimeout(connectWebSocket, 5000);
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

// Unsubscribe from all active subscriptions
function unsubscribeFromAll() {
    console.log('Unsubscribing from all topics');
    Object.keys(subscriptions).forEach(key => {
        if (subscriptions[key]) {
            subscriptions[key].unsubscribe();
        }
    });
    subscriptions = {};
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

    picker.style.left = isOutgoing ? 'auto' : 'calc(100% + 8px)';
    picker.style.right = isOutgoing ? 'calc(100% + 8px)' : 'auto';
    picker.style.transform = 'translate(0, -50%)';

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

    picker.style.transform = `translate(${offsetX}px, -50%)`;
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

    function autoResizeMessageInput() {
        if (!messageInput) {
            return;
        }

        messageInput.style.height = 'auto';

        const computed = window.getComputedStyle(messageInput);
        const lineHeight = parseFloat(computed.lineHeight) || 22;
        const borderHeight = (parseFloat(computed.borderTopWidth) || 0) + (parseFloat(computed.borderBottomWidth) || 0);
        const paddingHeight = (parseFloat(computed.paddingTop) || 0) + (parseFloat(computed.paddingBottom) || 0);
        const minHeight = lineHeight + borderHeight + paddingHeight;
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
                sendTypingIndicator(false);
            }
        });
    }

    // Handle typing indicator
    if (messageInput) {
        let typingTimer;
        messageInput.addEventListener('input', function () {
            autoResizeMessageInput();

            clearTimeout(typingTimer);
            sendTypingIndicator(true);

            typingTimer = setTimeout(() => {
                sendTypingIndicator(false);
            }, 2000);
        });

        autoResizeMessageInput();
        window.addEventListener('resize', autoResizeMessageInput);
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
