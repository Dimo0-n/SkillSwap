function escapeHtmlForSidebar(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
}

let chatHeaderLocalTimeIntervalId = null;

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

function ensureChatToastContainer() {
    let container = document.getElementById('chatToastContainer');
    if (container) {
        return container;
    }

    container = document.createElement('div');
    container.id = 'chatToastContainer';
    container.className = 'chat-toast-container';
    document.body.appendChild(container);
    return container;
}

function showChatToast(message, tone = 'info') {
    const container = ensureChatToastContainer();
    const toast = document.createElement('div');
    toast.className = `chat-toast chat-toast-${tone}`;
    toast.textContent = message;

    container.appendChild(toast);

    requestAnimationFrame(() => {
        toast.classList.add('is-visible');
    });

    window.setTimeout(() => {
        toast.classList.remove('is-visible');
        window.setTimeout(() => {
            toast.remove();
        }, 220);
    }, 2800);
}

window.showChatToast = showChatToast;

function buildConversationItem(conversation) {
    const unreadBadge = conversation.unreadCount > 0
        ? `<span class="unread-badge">${conversation.unreadCount}</span>`
        : '';
    const avatarUrl = conversation.otherUserAvatarUrl || '/img/default-avatar.png';
    const status = conversation.otherUserOnline ? 'online' : 'offline';
    const muted = conversation.mutedByCurrentUser === true;
    const blocked = conversation.blockedByCurrentUser === true;
    const reported = conversation.reportedByCurrentUser === true;
    const stateBadges = [
        muted ? '<span class="conversation-state-badge">Mute</span>' : '',
        blocked ? '<span class="conversation-state-badge conversation-state-badge-blocked">Blocat</span>' : '',
        reported ? '<span class="conversation-state-badge conversation-state-badge-reported">Raportat</span>' : ''
    ].join('');

    return `
        <div class="conversation-item"
             data-conversation-id="${conversation.chatRoomId}"
             data-user-id="${conversation.otherUserId}"
             data-unread-count="${Number.isFinite(Number(conversation.unreadCount)) && Number(conversation.unreadCount) > 0 ? Math.floor(Number(conversation.unreadCount)) : 0}"
             data-user-name="${escapeHtmlForSidebar(conversation.otherUserName)}"
             data-user-avatar="${escapeHtmlForSidebar(avatarUrl)}"
             data-user-timezone="${escapeHtmlForSidebar(conversation.otherUserTimeZoneId || '')}"
             data-user-status="${status}"
             data-proposal-id="${conversation.activeProposalId || ''}"
             data-proposal-status="${escapeHtmlForSidebar(conversation.activeProposalStatus || '')}"
             data-proposal-status-label="${escapeHtmlForSidebar(conversation.activeProposalStatusLabel || '')}"
             data-proposal-owner="${conversation.currentUserIsProposalOwner === true ? 'true' : 'false'}"
             data-proposal-can-accept="${conversation.canAcceptActiveProposal === true ? 'true' : 'false'}"
             data-muted-by-current-user="${muted ? 'true' : 'false'}"
             data-blocked-by-current-user="${blocked ? 'true' : 'false'}"
             data-reported-by-current-user="${reported ? 'true' : 'false'}">
            <div class="conversation-avatar">
                <img src="${escapeHtmlForSidebar(avatarUrl)}" alt="${escapeHtmlForSidebar(conversation.otherUserName)}">
                <span class="status-indicator ${status === 'online' ? 'status-online' : 'status-offline'}"></span>
            </div>
            <div class="conversation-content">
                <div class="conversation-header">
                    <span class="conversation-name">${escapeHtmlForSidebar(conversation.otherUserName)}</span>
                    <span class="conversation-time">${formatConversationTime(conversation.lastMessageTime)}</span>
                </div>
                <div class="conversation-preview">${escapeHtmlForSidebar(conversation.lastMessage)}</div>
                <div class="conversation-state-badges">${stateBadges}</div>
            </div>
            ${unreadBadge}
        </div>
    `;
}

function getConversationSettingsState(item) {
    return {
        muted: item?.dataset.mutedByCurrentUser === 'true',
        blocked: item?.dataset.blockedByCurrentUser === 'true',
        reported: item?.dataset.reportedByCurrentUser === 'true'
    };
}

function renderConversationStateBadges(item) {
    if (!item) {
        return;
    }

    let badgesContainer = item.querySelector('.conversation-state-badges');
    if (!badgesContainer) {
        badgesContainer = document.createElement('div');
        badgesContainer.className = 'conversation-state-badges';
        const content = item.querySelector('.conversation-content');
        if (content) {
            content.appendChild(badgesContainer);
        }
    }

    const state = getConversationSettingsState(item);
    badgesContainer.innerHTML = [
        state.muted ? '<span class="conversation-state-badge">Mute</span>' : '',
        state.blocked ? '<span class="conversation-state-badge conversation-state-badge-blocked">Blocat</span>' : '',
        state.reported ? '<span class="conversation-state-badge conversation-state-badge-reported">Raportat</span>' : ''
    ].join('');

    item.classList.toggle('is-muted', state.muted);
    item.classList.toggle('is-blocked', state.blocked);
    item.classList.toggle('is-reported', state.reported);
}

function applyConversationSettingsToActiveView(item) {
    const state = getConversationSettingsState(item);
    window.currentConversationSettings = state;

    const messageInput = document.getElementById('messageInput');
    const messageSendButton = document.querySelector('#messageForm .message-send-btn');
    const messageInputArea = document.querySelector('.message-input-area');
    const blockedPlaceholder = 'Conversația este blocată. Deblochează pentru a trimite mesaje.';

    if (messageInput) {
        if (!messageInput.dataset.defaultPlaceholder) {
            messageInput.dataset.defaultPlaceholder = messageInput.getAttribute('placeholder') || '';
        }

        messageInput.disabled = state.blocked;
        messageInput.placeholder = state.blocked ? blockedPlaceholder : messageInput.dataset.defaultPlaceholder;
    }

    if (messageSendButton) {
        messageSendButton.disabled = state.blocked;
    }

    if (messageInputArea) {
        messageInputArea.classList.toggle('is-disabled', state.blocked);
    }

    document.querySelectorAll('[data-video-call-trigger="true"]').forEach(button => {
        button.disabled = state.blocked;
    });

    document.querySelectorAll('.chat-settings-item[data-chat-action="mute"]').forEach(button => {
        button.textContent = state.muted ? 'Activează sunetul' : 'Dezactivează sunetul';
    });

    document.querySelectorAll('.chat-settings-item[data-chat-action="block"]').forEach(button => {
        button.textContent = state.blocked ? 'Deblochează' : 'Blochează';
    });

    document.querySelectorAll('.chat-settings-item[data-chat-action="report"]').forEach(button => {
        button.textContent = state.reported ? 'Raportată' : 'Raportează';
        button.disabled = state.reported;
    });
}

function updateConversationSettingsState(chatRoomId, settings) {
    const item = document.querySelector(`#conversationsList .conversation-item[data-conversation-id="${chatRoomId}"]`);
    if (!item || !settings) {
        return;
    }

    item.dataset.mutedByCurrentUser = settings.muted === true ? 'true' : 'false';
    item.dataset.blockedByCurrentUser = settings.blocked === true ? 'true' : 'false';
    item.dataset.reportedByCurrentUser = settings.reported === true ? 'true' : 'false';

    renderConversationStateBadges(item);

    if (item.classList.contains('active')) {
        applyConversationSettingsToActiveView(item);
    }
}

function setProposalHeaderState(options = {}) {
    const statusBadge = document.getElementById('chatProposalStatusBadge');
    const desktopAcceptButton = document.getElementById('chatProposalAcceptButton');
    const mobileAcceptButton = document.getElementById('mobileProposalAcceptButton');
    const status = options.status || '';
    const statusLabel = options.statusLabel || '';
    const canAccept = options.canAccept === true;
    const proposalId = options.proposalId || '';
    const isBusy = options.isBusy === true;

    if (statusBadge) {
        if (statusLabel) {
            statusBadge.textContent = `Schimb: ${statusLabel}`;
            statusBadge.classList.remove('is-hidden');
            statusBadge.classList.toggle('is-accepted', status === 'ACCEPTED');
        } else {
            statusBadge.textContent = '';
            statusBadge.classList.add('is-hidden');
            statusBadge.classList.remove('is-accepted');
        }
    }

    [desktopAcceptButton, mobileAcceptButton].forEach(button => {
        if (!button) {
            return;
        }

        button.dataset.proposalId = proposalId;
        button.classList.toggle('is-hidden', !canAccept);
        button.disabled = !canAccept || isBusy;
        button.classList.toggle('is-busy', isBusy);
        button.setAttribute('aria-busy', isBusy ? 'true' : 'false');
    });
}

function updateProposalHeaderFromConversation(item) {
    if (!item) {
        setProposalHeaderState();
        return;
    }

    setProposalHeaderState({
        proposalId: item.dataset.proposalId || '',
        status: item.dataset.proposalStatus || '',
        statusLabel: item.dataset.proposalStatusLabel || '',
        canAccept: item.dataset.proposalCanAccept === 'true'
    });
}

function updateConversationProposalState(chatRoomId, state = {}) {
    const item = document.querySelector(`#conversationsList .conversation-item[data-conversation-id="${chatRoomId}"]`);
    if (!item) {
        return;
    }

    item.dataset.proposalId = state.proposalId || '';
    item.dataset.proposalStatus = state.status || '';
    item.dataset.proposalStatusLabel = state.statusLabel || '';
    item.dataset.proposalOwner = state.isOwner === true ? 'true' : 'false';
    item.dataset.proposalCanAccept = state.canAccept === true ? 'true' : 'false';

    if (item.classList.contains('active')) {
        updateProposalHeaderFromConversation(item);
    }
}

function getTotalUnreadFromDOM() {
    return Array.from(document.querySelectorAll('#conversationsList .conversation-item[data-conversation-id]'))
        .reduce((sum, item) => {
            const unread = Number(item.dataset.unreadCount || 0);
            return sum + (Number.isFinite(unread) && unread > 0 ? unread : 0);
        }, 0);
}

function emitUnreadTotalUpdate() {
    window.dispatchEvent(new CustomEvent('chat:conversations-updated', {
        detail: { totalUnread: getTotalUnreadFromDOM() }
    }));
}

function markConversationAsRead(conversationId) {
    const item = document.querySelector(`#conversationsList .conversation-item[data-conversation-id="${conversationId}"]`);
    if (!item) {
        return;
    }

    const unread = Number(item.dataset.unreadCount || 0);
    if (!Number.isFinite(unread) || unread <= 0) {
        return;
    }

    item.dataset.unreadCount = '0';
    const unreadBadge = item.querySelector('.unread-badge');
    if (unreadBadge) {
        unreadBadge.remove();
    }

    emitUnreadTotalUpdate();
}

function updateConversationLastMessage(conversationId, messageContent, timestamp) {
    const conversationsList = document.getElementById('conversationsList');
    if (!conversationsList) {
        return;
    }

    const item = conversationsList.querySelector(`.conversation-item[data-conversation-id="${conversationId}"]`);
    if (!item) {
        return;
    }

    const previewElement = item.querySelector('.conversation-preview');
    if (previewElement) {
        previewElement.textContent = messageContent || '';
    }

    const timeElement = item.querySelector('.conversation-time');
    if (timeElement) {
        timeElement.textContent = formatConversationTime(timestamp || new Date().toISOString());
    }

    // Keep the most recently active conversation at the top of the list.
    if (conversationsList.firstElementChild !== item) {
        conversationsList.prepend(item);
    }
}

function updateChatHeaderFromConversation(item) {
    const userName = item.dataset.userName || 'Conversa?ie';
    const avatarUrl = item.dataset.userAvatar || '/img/default-avatar.png';
    const status = item.dataset.userStatus || 'offline';
    const timeZoneId = item.dataset.userTimezone || '';

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
        chatHeaderStatusText.textContent = buildChatPresenceText(status, timeZoneId);
    }

    if (mobileChatName) {
        mobileChatName.textContent = userName;
    }

    if (mobileChatStatus) {
        mobileChatStatus.textContent = buildChatPresenceText(status, timeZoneId);
    }

    document.querySelectorAll('#conversationsList .conversation-item.active').forEach(activeItem => {
        activeItem.classList.remove('active');
    });
    item.classList.add('active');

    updateProposalHeaderFromConversation(item);
    applyConversationSettingsToActiveView(item);

    startChatHeaderLocalTimeUpdates();
}

function formatUserLocalTime(timeZoneId) {
    if (!timeZoneId) {
        return '';
    }

    try {
        return new Intl.DateTimeFormat('en-GB', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false,
            timeZone: timeZoneId
        }).format(new Date());
    } catch (_) {
        return '';
    }
}

function buildChatPresenceText(status, timeZoneId) {
    const statusLabel = status === 'online' ? 'Online' : 'Offline';
    const localTime = formatUserLocalTime(timeZoneId);

    if (!localTime) {
        return statusLabel;
    }

    return `${statusLabel} \u2022 ${localTime} local time`;
}

function refreshActiveChatHeaderLocalTime() {
    const activeItem = document.querySelector('#conversationsList .conversation-item.active[data-conversation-id]');
    if (!activeItem) {
        return;
    }

    const timeZoneId = activeItem.dataset.userTimezone || '';
    const status = activeItem.dataset.userStatus || 'offline';
    const presenceText = buildChatPresenceText(status, timeZoneId);

    const chatHeaderStatusText = document.getElementById('chatHeaderStatusText');
    const mobileChatStatus = document.getElementById('mobileChatStatus');

    if (chatHeaderStatusText) {
        chatHeaderStatusText.textContent = presenceText;
    }

    if (mobileChatStatus) {
        mobileChatStatus.textContent = presenceText;
    }
}

function startChatHeaderLocalTimeUpdates() {
    if (chatHeaderLocalTimeIntervalId) {
        return;
    }

    chatHeaderLocalTimeIntervalId = window.setInterval(refreshActiveChatHeaderLocalTime, 30000);
}

function updateConversationPresence(userId, isOnline, timeZoneId) {
    if (userId === null || userId === undefined) {
        return;
    }

    const targetUserId = String(userId);
    const status = isOnline ? 'online' : 'offline';

    document.querySelectorAll('#conversationsList .conversation-item[data-user-id]').forEach(item => {
        if ((item.dataset.userId || '') !== targetUserId) {
            return;
        }

        item.dataset.userStatus = status;
        if (typeof timeZoneId === 'string' && timeZoneId.trim()) {
            item.dataset.userTimezone = timeZoneId.trim();
        }

        const indicator = item.querySelector('.conversation-avatar .status-indicator');
        if (indicator) {
            indicator.classList.remove('status-online', 'status-offline');
            indicator.classList.add(isOnline ? 'status-online' : 'status-offline');
        }
    });

    const activeItem = document.querySelector('#conversationsList .conversation-item.active[data-user-id]');
    if (activeItem && (activeItem.dataset.userId || '') === targetUserId) {
        updateChatHeaderFromConversation(activeItem);
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
    markConversationAsRead(conversationId);
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

async function updateConversationSettingsOnServer(chatRoomId, payload) {
    const response = await fetch(`/api/chat/rooms/${chatRoomId}/settings`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        throw new Error('Nu am putut actualiza setările conversației.');
    }

    return response.json();
}

async function handleChatSettingsAction(action) {
    const activeConversation = document.querySelector('#conversationsList .conversation-item.active[data-conversation-id]');
    if (!activeConversation) {
        showChatToast('Selectează mai întâi o conversație.', 'warning');
        return;
    }

    const chatRoomId = activeConversation.dataset.conversationId;
    const chatPartnerName = activeConversation.dataset.userName || 'utilizator';
    const state = getConversationSettingsState(activeConversation);

    try {
        if (action === 'mute') {
            const settings = await updateConversationSettingsOnServer(chatRoomId, { muted: !state.muted });
            updateConversationSettingsState(chatRoomId, settings);
            showChatToast(settings.muted
                ? `Conversația cu ${chatPartnerName} este acum pe mute.`
                : `Sunetul pentru conversația cu ${chatPartnerName} a fost reactivat.`, settings.muted ? 'info' : 'success');
            return;
        }

        if (action === 'block') {
            const willBlock = !state.blocked;
            const settings = await updateConversationSettingsOnServer(chatRoomId, { blocked: willBlock });
            updateConversationSettingsState(chatRoomId, settings);
            showChatToast(settings.blocked
                ? `${chatPartnerName} a fost blocat pentru această conversație.`
                : `Conversația cu ${chatPartnerName} a fost deblocată.`, settings.blocked ? 'warning' : 'success');
            return;
        }

        if (action === 'report') {
            if (state.reported) {
                showChatToast('Conversația a fost deja raportată.', 'warning');
                return;
            }

            const settings = await updateConversationSettingsOnServer(chatRoomId, { reported: true });
            updateConversationSettingsState(chatRoomId, settings);
            showChatToast(`Conversația cu ${chatPartnerName} a fost raportată.`, 'success');
        }
    } catch (error) {
        showChatToast(error.message || 'Nu am putut actualiza setările conversației.', 'error');
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
let proposalAcceptInProgress = false;

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
        showChatToast('Selectează mai întâi o conversație.', 'warning');
        return;
    }

    if (window.currentConversationSettings?.blocked === true) {
        showChatToast('Conversația este blocată. Deblochează pentru a porni un apel video.', 'warning');
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
            showChatToast('Serverul nu a returnat un link valid pentru apel.', 'error');
            return;
        }

        const newWindow = window.open(meetingUrl, '_blank', 'noopener,noreferrer');
        if (!newWindow) {
            showChatToast('Browserul a blocat pop-up-ul. Permite pop-up-uri pentru acest site și încearcă din nou.', 'warning');
        }
    } catch (error) {
        console.error('Error starting video call:', error);
        showChatToast('A apărut o eroare la pornirea apelului video.', 'error');
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

async function acceptNegotiatedProposal() {
    if (proposalAcceptInProgress) {
        return;
    }

    const activeItem = document.querySelector('#conversationsList .conversation-item.active[data-conversation-id]');
    const proposalId = activeItem ? activeItem.dataset.proposalId : '';
    if (!activeItem || !proposalId || activeItem.dataset.proposalCanAccept !== 'true') {
        return;
    }

    proposalAcceptInProgress = true;
    updateProposalHeaderFromConversation(activeItem);
    setProposalHeaderState({
        proposalId,
        status: activeItem.dataset.proposalStatus || '',
        statusLabel: activeItem.dataset.proposalStatusLabel || '',
        canAccept: true,
        isBusy: true
    });

    try {
        const response = await fetch(`/api/skill-swap-proposals/${proposalId}/accept`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json'
            }
        });

        const payload = await response.json().catch(() => ({}));

        if (payload && payload.redirectUrl && response.status === 401) {
            window.location.href = payload.redirectUrl;
            return;
        }

        if (!response.ok || payload.success === false) {
            throw new Error(payload.message || 'Nu am putut accepta schimbul acum.');
        }

        updateConversationProposalState(activeItem.dataset.conversationId, {
            proposalId,
            status: 'ACCEPTED',
            statusLabel: 'Acceptat',
            isOwner: true,
            canAccept: false
        });
    } catch (error) {
        window.alert(error.message || 'Nu am putut accepta schimbul acum.');
    } finally {
        proposalAcceptInProgress = false;
        const refreshedActiveItem = document.querySelector('#conversationsList .conversation-item.active[data-conversation-id]');
        if (refreshedActiveItem) {
            updateProposalHeaderFromConversation(refreshedActiveItem);
        } else {
            setProposalHeaderState();
        }
    }
}

function attachProposalAcceptButtons() {
    const acceptButtons = [
        document.getElementById('chatProposalAcceptButton'),
        document.getElementById('mobileProposalAcceptButton')
    ].filter(Boolean);

    acceptButtons.forEach(button => {
        button.addEventListener('click', function (event) {
            event.preventDefault();
            acceptNegotiatedProposal();
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
                emitUnreadTotalUpdate();
                return;
            }

            conversationsList.innerHTML = conversations.map(buildConversationItem).join('');
            conversationsList.querySelectorAll('.conversation-item[data-conversation-id]').forEach(renderConversationStateBadges);
            emitUnreadTotalUpdate();

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
    window.updateConversationPresence = updateConversationPresence;
    window.updateConversationLastMessage = updateConversationLastMessage;
    window.openChat = openChat;
    window.goBackToList = goBackToList;

    syncMobileNavbarOffset();
    clearDefaultMessages();
    attachConversationHandlers();
    attachConversationSearch();
    attachChatSettingsDropdowns();
    attachVideoCallButtons();
    attachProposalAcceptButtons();

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
