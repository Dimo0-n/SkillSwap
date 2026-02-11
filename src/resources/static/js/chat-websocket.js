// WebSocket Chat Client with STOMP
let stompClient = null;
let currentChatRoomId = null;
let currentUserId = null;
let typingTimeout = null;
let subscriptions = {}; // Track active subscriptions to prevent duplicates

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

// Send typing indicator
function sendTypingIndicator(isTyping) {
    if (!currentChatRoomId) return;

    const typing = {
        chatRoomId: currentChatRoomId,
        isTyping: isTyping
    };
    stompClient.send("/app/chat.typing", {}, JSON.stringify(typing));
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
                <img src="/img/details/comment/comment-1.jpg" alt="${message.senderName}">
            </div>
        `;
    }

    messageHTML += `
        <div class="message-content">
            <div class="message-bubble" oncontextmenu="showReactionMenu(event, ${message.id})">
                <p>${escapeHtml(message.content)}</p>
                <div class="message-reactions" id="reactions-${message.id}"></div>
            </div>
            <div class="message-time">
                ${formatTime(message.timestamp)}
                ${isOwnMessage ? '<span class="message-status" id="status-' + message.id + '">✓</span>' : ''}
            </div>
        </div>
    `;

    messageDiv.innerHTML = messageHTML;
    messagesContainer.appendChild(messageDiv);

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
            if (reaction.currentUserReacted) {
                emojiSpan.classList.add('user-reacted');
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

    switch (status) {
        case 'SENT':
            statusElement.innerHTML = '✓';
            statusElement.style.color = '#999';
            break;
        case 'DELIVERED':
            statusElement.innerHTML = '✓✓';
            statusElement.style.color = '#999';
            break;
        case 'SEEN':
            statusElement.innerHTML = '✓✓';
            statusElement.style.color = '#4CAF50';
            break;
    }
}

// Update message reactions
function updateMessageReaction(reaction) {
    const reactionsContainer = document.getElementById('reactions-' + reaction.messageId);
    if (!reactionsContainer) return;

    // Find existing emoji or create new one
    const existingEmoji = Array.from(reactionsContainer.children).find(
        span => span.dataset.emoji === reaction.emoji
    );

    if (existingEmoji) {
        // Update count
        const currentCount = parseInt(existingEmoji.dataset.count) || 1;
        existingEmoji.dataset.count = currentCount + 1;
        existingEmoji.textContent = reaction.emoji + ' ' + (currentCount + 1);
        
        // Add username to title
        const currentTitle = existingEmoji.title || '';
        existingEmoji.title = currentTitle ? currentTitle + ', ' + reaction.userName : reaction.userName;
    } else {
        // Create new emoji span with aggregation support
        const emojiSpan = document.createElement('span');
        emojiSpan.className = 'reaction-emoji';
        emojiSpan.dataset.emoji = reaction.emoji;
        emojiSpan.dataset.count = '1';
        emojiSpan.textContent = reaction.emoji + ' 1';
        emojiSpan.title = reaction.userName;
        reactionsContainer.appendChild(emojiSpan);
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
                        <img src="/img/details/comment/comment-1.jpg" alt="User">
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

// Show reaction menu (emoji picker)
function showReactionMenu(event, messageId) {
    event.preventDefault();

    // Remove existing menu
    const existingMenu = document.querySelector('.reaction-menu');
    if (existingMenu) existingMenu.remove();

    const menu = document.createElement('div');
    menu.className = 'reaction-menu';
    menu.style.position = 'fixed';
    menu.style.left = event.clientX + 'px';
    menu.style.top = event.clientY + 'px';

    const emojis = ['❤️', '😂', '😭', '😡', '🔥', '👍'];
    menu.innerHTML = emojis.map(emoji =>
        `<span class="emoji-option" onclick="selectReaction(${messageId}, '${emoji}')">${emoji}</span>`
    ).join('');

    document.body.appendChild(menu);

    // Close menu on click outside
    setTimeout(() => {
        document.addEventListener('click', function closeMenu() {
            menu.remove();
            document.removeEventListener('click', closeMenu);
        });
    }, 100);
}

// Select reaction
function selectReaction(messageId, emoji) {
    addReaction(messageId, emoji);
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
    // Get current user ID from backend API
    fetch('/api/chat/current-user')
        .then(response => response.json())
        .then(data => {
            currentUserId = data.userId;
            console.log('Current user ID:', currentUserId);
        })
        .catch(error => {
            console.error('Error fetching current user:', error);
            currentUserId = null;
        });

    // Connect to WebSocket
    connectWebSocket();

    // Handle message form submission
    const messageForm = document.getElementById('messageForm');
    const messageInput = document.getElementById('messageInput');

    if (messageForm) {
        messageForm.addEventListener('submit', function (e) {
            e.preventDefault();
            const content = messageInput.value.trim();
            if (content) {
                sendMessage(content);
                messageInput.value = '';
                sendTypingIndicator(false);
            }
        });
    }

    // Handle typing indicator
    if (messageInput) {
        let typingTimer;
        messageInput.addEventListener('input', function () {
            clearTimeout(typingTimer);
            sendTypingIndicator(true);

            typingTimer = setTimeout(() => {
                sendTypingIndicator(false);
            }, 2000);
        });
    }

    // Handle conversation selection
    document.querySelectorAll('.conversation-item').forEach(item => {
        item.addEventListener('click', function () {
            const chatRoomId = this.dataset.conversationId;
            loadChatRoom(chatRoomId);
        });
    });

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
    document.querySelector(`[data-conversation-id="${chatRoomId}"]`).classList.add('active');
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
