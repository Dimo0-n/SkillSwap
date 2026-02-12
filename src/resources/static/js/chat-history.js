function escapeHtmlForSidebar(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
}

function formatConversationTime(timestamp) {
    if (!timestamp) {
        return '';
    }

    const date = new Date(timestamp);
    const now = new Date();

    const sameDay = date.toDateString() === now.toDateString();
    if (sameDay) {
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        return `${hours}:${minutes}`;
    }

    return date.toLocaleDateString('ro-RO', { day: '2-digit', month: '2-digit' });
}

function renderEmptyConversationsState() {
    const conversationsList = document.getElementById('conversationsList');
    if (!conversationsList) {
        return;
    }

    conversationsList.innerHTML = `
        <div class="conversation-item">
            <div class="conversation-content">
                <div class="conversation-header">
                    <span class="conversation-name">Nu ai conversatii inca</span>
                </div>
                <div class="conversation-preview">Incepe un chat dintr-un anunt pentru a vedea istoricul aici.</div>
            </div>
        </div>
    `;
}

function buildConversationItem(conversation) {
    const unreadBadge = conversation.unreadCount > 0
        ? `<span class="unread-badge">${conversation.unreadCount}</span>`
        : '';
    const avatarUrl = conversation.otherUserAvatarUrl || '/img/default-avatar.png';

    return `
        <div class="conversation-item"
             data-conversation-id="${conversation.chatRoomId}"
             data-user-name="${escapeHtmlForSidebar(conversation.otherUserName)}"
             data-user-avatar="${escapeHtmlForSidebar(avatarUrl)}"
             data-user-status="offline">
            <div class="conversation-avatar">
                <img src="${escapeHtmlForSidebar(avatarUrl)}" alt="${escapeHtmlForSidebar(conversation.otherUserName)}">
                <span class="status-indicator status-offline"></span>
            </div>
            <div class="conversation-content">
                <div class="conversation-header">
                    <span class="conversation-name">${escapeHtmlForSidebar(conversation.otherUserName)}</span>
                    <span class="conversation-time">${formatConversationTime(conversation.lastMessageTime)}</span>
                </div>
                <div class="conversation-preview">${escapeHtmlForSidebar(conversation.lastMessage)}</div>
            </div>
            ${unreadBadge}
        </div>
    `;
}

function updateChatHeaderFromConversation(item) {
    const userName = item.dataset.userName || 'Conversa?ie';
    const avatarUrl = item.dataset.userAvatar || '/img/default-avatar.png';
    const status = item.dataset.userStatus || 'offline';

    const chatHeaderName = document.getElementById('chatHeaderName');
    const chatHeaderStatus = document.getElementById('chatHeaderStatus');
    const chatHeaderStatusText = document.getElementById('chatHeaderStatusText');
    const chatHeaderAvatar = document.getElementById('chatHeaderAvatar');

    if (chatHeaderName) {
        chatHeaderName.textContent = userName;
    }

    if (chatHeaderAvatar) {
        chatHeaderAvatar.src = avatarUrl;
        chatHeaderAvatar.alt = userName;
    }

    window.currentChatPartnerAvatar = avatarUrl;
    document.querySelectorAll('.message-incoming .message-avatar img').forEach(img => {
        img.src = avatarUrl;
        img.alt = userName;
    });

    if (chatHeaderStatus) {
        chatHeaderStatus.classList.remove('status-online', 'status-offline');
        chatHeaderStatus.classList.add(status === 'online' ? 'status-online' : 'status-offline');
    }

    if (chatHeaderStatusText) {
        chatHeaderStatusText.textContent = status === 'online' ? 'Online' : 'Offline';
    }
}

function attachConversationHandlers() {
    const conversationsList = document.getElementById('conversationsList');
    if (!conversationsList) {
        return;
    }

    conversationsList.addEventListener('click', function (event) {
        const item = event.target.closest('.conversation-item[data-conversation-id]');
        if (!item) {
            return;
        }

        const chatRoomId = item.dataset.conversationId;
        updateChatHeaderFromConversation(item);
        loadChatRoom(chatRoomId);
    });
}

function attachConversationSearch() {
    const searchInput = document.getElementById('conversationSearch');
    if (!searchInput) {
        return;
    }

    searchInput.addEventListener('input', function () {
        const term = this.value.trim().toLowerCase();
        document.querySelectorAll('#conversationsList .conversation-item[data-conversation-id]').forEach(item => {
            const name = (item.dataset.userName || '').toLowerCase();
            const previewElement = item.querySelector('.conversation-preview');
            const preview = previewElement ? previewElement.textContent.toLowerCase() : '';
            const matches = name.includes(term) || preview.includes(term);
            item.style.display = matches ? '' : 'none';
        });
    });
}

function clearDefaultMessages() {
    const messagesContainer = document.getElementById('messagesContainer');
    if (messagesContainer) {
        messagesContainer.innerHTML = '';
    }
}

function loadConversations() {
    const conversationsList = document.getElementById('conversationsList');
    if (!conversationsList) {
        return;
    }

    fetch('/api/chat/rooms')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load conversations');
            }
            return response.json();
        })
        .then(conversations => {
            if (!Array.isArray(conversations) || conversations.length === 0) {
                renderEmptyConversationsState();
                return;
            }

            conversationsList.innerHTML = conversations.map(buildConversationItem).join('');

            const urlParams = new URLSearchParams(window.location.search);
            const roomIdFromUrl = urlParams.get('roomId');
            const initialRoomId = roomIdFromUrl || String(conversations[0].chatRoomId);
            const initialItem = conversationsList.querySelector(`[data-conversation-id="${initialRoomId}"]`);

            if (initialItem) {
                updateChatHeaderFromConversation(initialItem);
                loadChatRoom(initialRoomId);
            }
        })
        .catch(error => {
            console.error('Error loading conversations:', error);
            renderEmptyConversationsState();
        });
}

document.addEventListener('DOMContentLoaded', function () {
    clearDefaultMessages();
    attachConversationHandlers();
    attachConversationSearch();

    const readyPromise = window.currentUserReadyPromise || Promise.resolve(true);
    readyPromise.finally(() => {
        loadConversations();
    });
});
