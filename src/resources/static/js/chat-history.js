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
    const mobileChatName = document.getElementById('mobileChatName');
    const mobileChatStatus = document.getElementById('mobileChatStatus');
    const mobileChatAvatar = document.getElementById('mobileChatAvatar');

    if (chatHeaderName) {
        chatHeaderName.textContent = userName;
    }

    if (chatHeaderAvatar) {
        chatHeaderAvatar.src = avatarUrl;
        chatHeaderAvatar.alt = userName;
    }

    if (mobileChatAvatar) {
        mobileChatAvatar.src = avatarUrl;
        mobileChatAvatar.alt = userName;
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

    if (mobileChatName) {
        mobileChatName.textContent = userName;
    }

    if (mobileChatStatus) {
        mobileChatStatus.textContent = status === 'online' ? 'Online' : 'Offline';
    }
}

function isMobileChatView() {
    return window.matchMedia('(max-width: 768px)').matches;
}

function syncMobileNavbarOffset() {
    const headerSection = document.querySelector('.header-section');
    const headerHeight = headerSection ? Math.ceil(headerSection.getBoundingClientRect().height) : 96;
    document.documentElement.style.setProperty('--navbar-mobile-height', `${headerHeight}px`);
}

function openChat(conversationId) {
    const item = document.querySelector(`#conversationsList .conversation-item[data-conversation-id="${conversationId}"]`);
    if (!item) {
        return;
    }

    updateChatHeaderFromConversation(item);
    loadChatRoom(conversationId);

    if (!isMobileChatView()) {
        return;
    }

    const chatSidebar = document.querySelector('.chat-sidebar');
    const chatWindow = document.getElementById('chatWindow');
    const mobileChatHeader = document.getElementById('mobileChatHeader');

    if (chatSidebar) {
        chatSidebar.classList.add('hidden');
    }

    if (chatWindow) {
        chatWindow.classList.remove('hidden');
        chatWindow.classList.add('active');
    }

    if (mobileChatHeader) {
        mobileChatHeader.classList.add('active');
    }

    window.dispatchEvent(new Event('chat:window-opened'));
}

function goBackToList() {
    if (!isMobileChatView()) {
        return;
    }

    const chatSidebar = document.querySelector('.chat-sidebar');
    const chatWindow = document.getElementById('chatWindow');
    const mobileChatHeader = document.getElementById('mobileChatHeader');

    if (chatSidebar) {
        chatSidebar.classList.remove('hidden');
    }

    if (chatWindow) {
        chatWindow.classList.add('hidden');
        chatWindow.classList.remove('active');
    }

    if (mobileChatHeader) {
        mobileChatHeader.classList.remove('active');
    }
}

function syncChatViewForBreakpoint() {
    const chatSidebar = document.querySelector('.chat-sidebar');
    const chatWindow = document.getElementById('chatWindow');
    const mobileChatHeader = document.getElementById('mobileChatHeader');

    if (!chatSidebar || !chatWindow || !mobileChatHeader) {
        return;
    }

    if (isMobileChatView()) {
        if (!currentChatRoomId) {
            goBackToList();
        }
    } else {
        chatSidebar.classList.remove('hidden');
        chatWindow.classList.remove('hidden');
        mobileChatHeader.classList.remove('active');
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
        openChat(chatRoomId);
    });
}

function attachConversationSearch() {
    const searchInput = document.getElementById('inputBox');
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

function closeAllChatSettingsDropdowns(exceptWrapper = null) {
    document.querySelectorAll('.chat-settings-wrapper.is-open').forEach(wrapper => {
        if (exceptWrapper && wrapper === exceptWrapper) {
            return;
        }

        wrapper.classList.remove('is-open');
        const trigger = wrapper.querySelector('.settings-action-btn');
        if (trigger) {
            trigger.setAttribute('aria-expanded', 'false');
        }
    });
}

function handleChatSettingsAction(action) {
    const chatPartnerName = document.getElementById('chatHeaderName')?.textContent?.trim() || 'utilizator';

    if (action === 'mute') {
        window.alert(`Conversația cu ${chatPartnerName} a fost pusă pe mute.`);
        return;
    }

    if (action === 'block') {
        const confirmed = window.confirm(`Sigur vrei să blochezi utilizatorul ${chatPartnerName}?`);
        if (confirmed) {
            window.alert(`${chatPartnerName} a fost blocat.`);
        }
        return;
    }

    if (action === 'report') {
        window.alert(`Conversația cu ${chatPartnerName} a fost raportată.`);
    }
}

function attachChatSettingsDropdowns() {
    const settingsWrappers = document.querySelectorAll('.chat-settings-wrapper');
    if (!settingsWrappers.length) {
        return;
    }

    settingsWrappers.forEach(wrapper => {
        const trigger = wrapper.querySelector('.settings-action-btn');
        const dropdown = wrapper.querySelector('.chat-settings-dropdown');
        if (!trigger || !dropdown) {
            return;
        }

        trigger.addEventListener('click', function (event) {
            event.preventDefault();
            event.stopPropagation();

            const willOpen = !wrapper.classList.contains('is-open');
            closeAllChatSettingsDropdowns(wrapper);

            wrapper.classList.toggle('is-open', willOpen);
            trigger.setAttribute('aria-expanded', willOpen ? 'true' : 'false');
        });

        dropdown.addEventListener('click', function (event) {
            const actionButton = event.target.closest('.chat-settings-item[data-chat-action]');
            if (!actionButton) {
                return;
            }

            event.preventDefault();
            event.stopPropagation();

            handleChatSettingsAction(actionButton.dataset.chatAction);
            closeAllChatSettingsDropdowns();
        });
    });

    document.addEventListener('click', function () {
        closeAllChatSettingsDropdowns();
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            closeAllChatSettingsDropdowns();
        }
    });
}

let videoCallInProgress = false;

function setVideoCallButtonsBusy(isBusy) {
    document.querySelectorAll('[data-video-call-trigger="true"]').forEach(button => {
        button.disabled = isBusy;
        button.classList.toggle('is-busy', isBusy);
        button.setAttribute('aria-busy', isBusy ? 'true' : 'false');
    });
}

async function startConversationVideoCall() {
    if (videoCallInProgress) {
        return;
    }

    if (!currentChatRoomId) {
        window.alert('Selecteaza mai intai o conversatie.');
        return;
    }

    videoCallInProgress = true;
    setVideoCallButtonsBusy(true);

    try {
        const response = await fetch(`/api/conversations/${currentChatRoomId}/video-room`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json'
            }
        });

        let payload = null;
        try {
            payload = await response.json();
        } catch (_) {
            payload = null;
        }

        if (!response.ok) {
            if (response.status === 401) {
                window.location.href = '/login';
                return;
            }

            const message = payload?.message || 'Nu am putut porni apelul video acum. Incearca din nou.';
            window.alert(message);
            return;
        }

        const meetingUrl = payload?.meetingUrl;
        if (!meetingUrl) {
            window.alert('Serverul nu a returnat un link valid pentru apel.');
            return;
        }

        const newWindow = window.open(meetingUrl, '_blank', 'noopener,noreferrer');
        if (!newWindow) {
            window.alert('Browserul a blocat pop-up-ul. Permite pop-up-uri pentru acest site si incearca din nou.');
        }
    } catch (error) {
        console.error('Error starting video call:', error);
        window.alert('A aparut o eroare la pornirea apelului video.');
    } finally {
        videoCallInProgress = false;
        setVideoCallButtonsBusy(false);
    }
}

function attachVideoCallButtons() {
    const callButtons = document.querySelectorAll('[data-video-call-trigger="true"]');
    if (!callButtons.length) {
        return;
    }

    callButtons.forEach(button => {
        button.addEventListener('click', function (event) {
            event.preventDefault();
            startConversationVideoCall();
        });
    });
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

            // Subscribe to video call topics for ALL conversations so notifications
            // are received regardless of which conversation is currently open.
            if (typeof subscribeVideoTopicsFromDOM === 'function') {
                subscribeVideoTopicsFromDOM();
            }
            const urlParams = new URLSearchParams(window.location.search);
            const roomIdFromUrl = urlParams.get('roomId');
            const initialRoomId = roomIdFromUrl || String(conversations[0].chatRoomId);
            const initialItem = conversationsList.querySelector(`[data-conversation-id="${initialRoomId}"]`);

            if (initialItem) {
                if (isMobileChatView()) {
                    if (roomIdFromUrl) {
                        openChat(initialRoomId);
                    } else {
                        updateChatHeaderFromConversation(initialItem);
                        goBackToList();
                    }
                } else {
                    openChat(initialRoomId);
                }
            }
        })
        .catch(error => {
            console.error('Error loading conversations:', error);
            renderEmptyConversationsState();
        });
}

document.addEventListener('DOMContentLoaded', function () {
    window.openChat = openChat;
    window.goBackToList = goBackToList;

    syncMobileNavbarOffset();
    clearDefaultMessages();
    attachConversationHandlers();
    attachConversationSearch();
    attachChatSettingsDropdowns();
    attachVideoCallButtons();

    window.addEventListener('resize', function () {
        syncMobileNavbarOffset();
        syncChatViewForBreakpoint();
    });

    const readyPromise = window.currentUserReadyPromise || Promise.resolve(true);
    readyPromise.finally(() => {
        loadConversations();
        syncChatViewForBreakpoint();
    });
});
