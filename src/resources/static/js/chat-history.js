function escapeHtmlForSidebar(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
}

let chatHeaderLocalTimeIntervalId = null;

function isUnresolvedI18nValue(value) {
    return typeof value === 'string' && value.startsWith('??') && value.endsWith('??');
}

function pickI18nValue(value, fallback) {
    if (isUnresolvedI18nValue(value) || value === null || value === undefined || value === '') {
        return fallback;
    }
    return value;
}

function formatI18nTemplate(template, values = {}) {
    if (typeof template !== 'string') {
        return '';
    }

    return template.replace(/\{(\w+)\}/g, (_, key) => {
        const value = values[key];
        return value === null || value === undefined ? '' : String(value);
    });
}

function getChatHistoryI18nDefaults() {
    return {
        common: {
            locale: (document && document.documentElement && document.documentElement.lang) || 'en'
        },
        selectConversation: 'Select a conversation',
        unknownUser: 'User',
        empty: {
            title: 'No conversations yet',
            subtitle: 'Start a chat from an announcement to see history here.'
        },
        state: {
            muted: 'Muted',
            blocked: 'Blocked',
            reported: 'Reported'
        },
        presence: {
            online: 'Online',
            offline: 'Offline',
            localTimeSuffix: 'local time'
        },
        settings: {
            blockedPlaceholder: 'Conversation is blocked. Unblock to send messages.',
            muteEnable: 'Unmute',
            muteDisable: 'Mute',
            blockEnable: 'Unblock',
            blockDisable: 'Block',
            reportAction: 'Report',
            reportDone: 'Reported',
            updateError: 'Could not update conversation settings.',
            selectConversation: 'Select a conversation first.'
        },
        toast: {
            mutedOn: 'Conversation with {name} is now muted.',
            mutedOff: 'Sound for conversation with {name} has been restored.',
            blockedOn: '{name} has been blocked for this conversation.',
            blockedOff: 'Conversation with {name} has been unblocked.',
            reportedAlready: 'Conversation has already been reported.',
            reportedDone: 'Conversation with {name} has been reported.'
        },
        video: {
            selectConversation: 'Select a conversation first.',
            blocked: 'Conversation is blocked. Unblock to start a video call.',
            startFailed: 'Could not start the video call now. Try again.',
            missingUrl: 'Server did not return a valid meeting link.',
            popupBlocked: 'Browser blocked the pop-up. Allow pop-ups for this site and try again.',
            errorStart: 'An error occurred while starting the video call.'
        },
        proposal: {
            updateError: 'Could not update swap status.',
            updated: 'Swap status updated.'
        }
    };
}

function getChatHistoryI18n() {
    const defaults = getChatHistoryI18nDefaults();
    const commonRaw = (window.chatI18n && window.chatI18n.common) || {};
    const raw = (window.chatI18n && window.chatI18n.history) || {};

    return {
        common: {
            locale: pickI18nValue(commonRaw.locale, defaults.common.locale)
        },
        selectConversation: pickI18nValue(raw.selectConversation, defaults.selectConversation),
        unknownUser: pickI18nValue(raw.unknownUser, defaults.unknownUser),
        empty: {
            title: pickI18nValue(raw.empty && raw.empty.title, defaults.empty.title),
            subtitle: pickI18nValue(raw.empty && raw.empty.subtitle, defaults.empty.subtitle)
        },
        state: {
            muted: pickI18nValue(raw.state && raw.state.muted, defaults.state.muted),
            blocked: pickI18nValue(raw.state && raw.state.blocked, defaults.state.blocked),
            reported: pickI18nValue(raw.state && raw.state.reported, defaults.state.reported)
        },
        presence: {
            online: pickI18nValue(raw.presence && raw.presence.online, defaults.presence.online),
            offline: pickI18nValue(raw.presence && raw.presence.offline, defaults.presence.offline),
            localTimeSuffix: pickI18nValue(raw.presence && raw.presence.localTimeSuffix, defaults.presence.localTimeSuffix)
        },
        settings: {
            blockedPlaceholder: pickI18nValue(raw.settings && raw.settings.blockedPlaceholder, defaults.settings.blockedPlaceholder),
            muteEnable: pickI18nValue(raw.settings && raw.settings.muteEnable, defaults.settings.muteEnable),
            muteDisable: pickI18nValue(raw.settings && raw.settings.muteDisable, defaults.settings.muteDisable),
            blockEnable: pickI18nValue(raw.settings && raw.settings.blockEnable, defaults.settings.blockEnable),
            blockDisable: pickI18nValue(raw.settings && raw.settings.blockDisable, defaults.settings.blockDisable),
            reportAction: pickI18nValue(raw.settings && raw.settings.reportAction, defaults.settings.reportAction),
            reportDone: pickI18nValue(raw.settings && raw.settings.reportDone, defaults.settings.reportDone),
            updateError: pickI18nValue(raw.settings && raw.settings.updateError, defaults.settings.updateError),
            selectConversation: pickI18nValue(raw.settings && raw.settings.selectConversation, defaults.settings.selectConversation)
        },
        toast: {
            mutedOn: pickI18nValue(raw.toast && raw.toast.mutedOn, defaults.toast.mutedOn),
            mutedOff: pickI18nValue(raw.toast && raw.toast.mutedOff, defaults.toast.mutedOff),
            blockedOn: pickI18nValue(raw.toast && raw.toast.blockedOn, defaults.toast.blockedOn),
            blockedOff: pickI18nValue(raw.toast && raw.toast.blockedOff, defaults.toast.blockedOff),
            reportedAlready: pickI18nValue(raw.toast && raw.toast.reportedAlready, defaults.toast.reportedAlready),
            reportedDone: pickI18nValue(raw.toast && raw.toast.reportedDone, defaults.toast.reportedDone)
        },
        video: {
            selectConversation: pickI18nValue(raw.video && raw.video.selectConversation, defaults.video.selectConversation),
            blocked: pickI18nValue(raw.video && raw.video.blocked, defaults.video.blocked),
            startFailed: pickI18nValue(raw.video && raw.video.startFailed, defaults.video.startFailed),
            missingUrl: pickI18nValue(raw.video && raw.video.missingUrl, defaults.video.missingUrl),
            popupBlocked: pickI18nValue(raw.video && raw.video.popupBlocked, defaults.video.popupBlocked),
            errorStart: pickI18nValue(raw.video && raw.video.errorStart, defaults.video.errorStart)
        },
        proposal: {
            updateError: pickI18nValue(raw.proposal && raw.proposal.updateError, defaults.proposal.updateError),
            updated: pickI18nValue(raw.proposal && raw.proposal.updated, defaults.proposal.updated)
        }
    };
}

function getProposalI18nDefaults() {
    return {
        prefix: 'Schimb',
        defaultLabel: 'Status',
        systemTitle: 'Skill Swap Proposal',
        systemStatusPrefix: 'Status',
        statuses: {
            PENDING: 'In asteptare',
            NEGOTIATING: 'Negociere',
            ACCEPTED: 'Acceptat',
            IN_PROGRESS: 'In progres',
            COMPLETED: 'Finalizat',
            CANCELLED: 'Anulat',
            REJECTED: 'Refuzat'
        },
        actions: {
            accept: 'Accepta (Acceptat)',
            start: 'Porneste (In progres)',
            complete: 'Finalizeaza (Finalizat)',
            cancel: 'Anuleaza (Anulat)'
        }
    };
}

function getProposalI18n() {
    const defaults = getProposalI18nDefaults();
    const raw = (window.chatI18n && window.chatI18n.proposal) || {};

    return {
        prefix: pickI18nValue(raw.prefix, defaults.prefix),
        defaultLabel: pickI18nValue(raw.defaultLabel, defaults.defaultLabel),
        systemTitle: pickI18nValue(raw.systemTitle, defaults.systemTitle),
        systemStatusPrefix: pickI18nValue(raw.systemStatusPrefix, defaults.systemStatusPrefix),
        statuses: {
            PENDING: pickI18nValue(raw.statuses && raw.statuses.PENDING, defaults.statuses.PENDING),
            NEGOTIATING: pickI18nValue(raw.statuses && raw.statuses.NEGOTIATING, defaults.statuses.NEGOTIATING),
            ACCEPTED: pickI18nValue(raw.statuses && raw.statuses.ACCEPTED, defaults.statuses.ACCEPTED),
            IN_PROGRESS: pickI18nValue(raw.statuses && raw.statuses.IN_PROGRESS, defaults.statuses.IN_PROGRESS),
            COMPLETED: pickI18nValue(raw.statuses && raw.statuses.COMPLETED, defaults.statuses.COMPLETED),
            CANCELLED: pickI18nValue(raw.statuses && raw.statuses.CANCELLED, defaults.statuses.CANCELLED),
            REJECTED: pickI18nValue(raw.statuses && raw.statuses.REJECTED, defaults.statuses.REJECTED)
        },
        actions: {
            accept: pickI18nValue(raw.actions && raw.actions.accept, defaults.actions.accept),
            start: pickI18nValue(raw.actions && raw.actions.start, defaults.actions.start),
            complete: pickI18nValue(raw.actions && raw.actions.complete, defaults.actions.complete),
            cancel: pickI18nValue(raw.actions && raw.actions.cancel, defaults.actions.cancel)
        }
    };
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

    const locale = getChatHistoryI18n().common.locale || 'en';
    return date.toLocaleDateString(locale, { day: '2-digit', month: '2-digit' });
}

function renderEmptyConversationsState() {
    const conversationsList = document.getElementById('conversationsList');
    if (!conversationsList) {
        return;
    }

    const i18n = getChatHistoryI18n();
    conversationsList.innerHTML = `
        <div class="conversation-item">
            <div class="conversation-content">
                <div class="conversation-header">
                    <span class="conversation-name">${escapeHtmlForSidebar(i18n.empty.title)}</span>
                </div>
                <div class="conversation-preview">${escapeHtmlForSidebar(i18n.empty.subtitle)}</div>
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
    const i18n = getChatHistoryI18n();
    const unreadBadge = conversation.unreadCount > 0
        ? `<span class="unread-badge">${conversation.unreadCount}</span>`
        : '';
    const avatarUrl = conversation.otherUserAvatarUrl || '/img/default-avatar.png';
    const status = conversation.otherUserOnline ? 'online' : 'offline';
    const muted = conversation.mutedByCurrentUser === true;
    const blocked = conversation.blockedByCurrentUser === true;
    const reported = conversation.reportedByCurrentUser === true;
    const stateBadges = [
        muted ? `<span class="conversation-state-badge">${escapeHtmlForSidebar(i18n.state.muted)}</span>` : '',
        blocked ? `<span class="conversation-state-badge conversation-state-badge-blocked">${escapeHtmlForSidebar(i18n.state.blocked)}</span>` : '',
        reported ? `<span class="conversation-state-badge conversation-state-badge-reported">${escapeHtmlForSidebar(i18n.state.reported)}</span>` : ''
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

    const i18n = getChatHistoryI18n();
    const state = getConversationSettingsState(item);
    badgesContainer.innerHTML = [
        state.muted ? `<span class="conversation-state-badge">${escapeHtmlForSidebar(i18n.state.muted)}</span>` : '',
        state.blocked ? `<span class="conversation-state-badge conversation-state-badge-blocked">${escapeHtmlForSidebar(i18n.state.blocked)}</span>` : '',
        state.reported ? `<span class="conversation-state-badge conversation-state-badge-reported">${escapeHtmlForSidebar(i18n.state.reported)}</span>` : ''
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
    const i18n = getChatHistoryI18n();
    const blockedPlaceholder = i18n.settings.blockedPlaceholder;

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
        button.textContent = state.muted ? i18n.settings.muteEnable : i18n.settings.muteDisable;
    });

    document.querySelectorAll('.chat-settings-item[data-chat-action="block"]').forEach(button => {
        button.textContent = state.blocked ? i18n.settings.blockEnable : i18n.settings.blockDisable;
    });

    document.querySelectorAll('.chat-settings-item[data-chat-action="report"]').forEach(button => {
        button.textContent = state.reported ? i18n.settings.reportDone : i18n.settings.reportAction;
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
    const desktopActionsWrapper = document.getElementById('chatProposalActionsWrapper');
    const mobileActionsWrapper = document.getElementById('mobileProposalActionsWrapper');
    const status = options.status || '';
    const statusLabel = options.statusLabel || '';
    const isOwner = options.isOwner === true;
    const proposalId = options.proposalId || '';
    const isBusy = options.isBusy === true;
    const actions = getProposalActions(status, isOwner);

    [desktopActionsWrapper, mobileActionsWrapper].forEach(wrapper => {
        if (!wrapper) {
            return;
        }

        const trigger = wrapper.querySelector('.proposal-actions-btn');
        const triggerLabel = trigger ? trigger.querySelector('span') : null;
        const triggerIcon = trigger ? trigger.querySelector('i') : null;
        const hasActions = actions.length > 0;
        const hasStatus = Boolean(statusLabel);
        wrapper.classList.toggle('is-hidden', !hasStatus);
        if (!hasStatus || !hasActions) {
            wrapper.classList.remove('is-open');
            if (trigger) {
                trigger.setAttribute('aria-expanded', 'false');
            }
        }

        if (trigger) {
            trigger.disabled = !hasActions || isBusy;
            trigger.classList.toggle('is-readonly', !hasActions);
            trigger.classList.remove('is-pending', 'is-negotiating', 'is-accepted', 'is-in-progress', 'is-completed', 'is-cancelled');
            const toneClass = getProposalStatusToneClass(status);
            if (toneClass) {
                trigger.classList.add(toneClass);
            }
        }

        if (triggerLabel) {
            const i18n = getProposalI18n();
            const prefix = i18n.prefix || 'Swap';
            const defaultLabel = i18n.defaultLabel || 'Status';
            const resolvedStatusLabel = hasStatus
                ? (statusLabel && statusLabel !== status ? statusLabel : mapProposalStatusLabel(status))
                : '';
            triggerLabel.textContent = hasStatus ? `${prefix}: ${resolvedStatusLabel}` : defaultLabel;
        }

        if (triggerIcon) {
            triggerIcon.classList.toggle('is-hidden', !hasActions);
        }

        const menu = wrapper.querySelector('.proposal-actions-dropdown');
        if (menu) {
            renderProposalActions(menu, proposalId, actions, isBusy);
        }
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
        isOwner: item.dataset.proposalOwner === 'true'
    });
}

function getProposalActions(status, isOwner) {
    if (!status) {
        return [];
    }

    if (status === 'PENDING' || (status === 'NEGOTIATING' && isOwner)) {
        return isOwner ? ['accept', 'cancel'] : [];
    }

    if (status === 'ACCEPTED') {
        return ['start', 'cancel'];
    }

    if (status === 'IN_PROGRESS') {
        return ['complete', 'cancel'];
    }

    return [];
}

function renderProposalActions(menuElement, proposalId, actions, isBusy) {
    if (!menuElement) {
        return;
    }

    if (!proposalId || !actions.length) {
        menuElement.innerHTML = '';
        return;
    }

    const i18n = getProposalI18n();
    const labels = {
        accept: i18n.actions?.accept || 'Accept',
        start: i18n.actions?.start || 'Start',
        complete: i18n.actions?.complete || 'Complete',
        cancel: i18n.actions?.cancel || 'Cancel'
    };

    const actionToStatusClass = {
        accept: 'status-accepted',
        start: 'status-in-progress',
        complete: 'status-completed',
        cancel: 'status-cancelled'
    };

    menuElement.innerHTML = actions
        .map(action => {
            const toneClass = actionToStatusClass[action] || '';
            return `<button type="button" class="chat-settings-item proposal-action-item ${toneClass}" data-proposal-action="${action}" data-proposal-id="${proposalId}" ${isBusy ? 'disabled' : ''}>${labels[action] || action}</button>`;
        })
        .join('');
}

function getProposalStatusToneClass(status) {
    switch (status) {
        case 'PENDING':
            return 'is-pending';
        case 'NEGOTIATING':
            return 'is-negotiating';
        case 'ACCEPTED':
            return 'is-accepted';
        case 'IN_PROGRESS':
            return 'is-in-progress';
        case 'COMPLETED':
            return 'is-completed';
        case 'CANCELLED':
            return 'is-cancelled';
        default:
            return '';
    }
}

function mapProposalStatusLabel(status) {
    const labels = getProposalI18n().statuses || {};
    return labels[status] || status || '';
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
    const userName = item.dataset.userName || getChatHistoryI18n().unknownUser;
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
    const i18n = getChatHistoryI18n();
    const statusLabel = status === 'online' ? i18n.presence.online : i18n.presence.offline;
    const localTime = formatUserLocalTime(timeZoneId);

    if (!localTime) {
        return statusLabel;
    }

    return `${statusLabel} \u2022 ${localTime} ${i18n.presence.localTimeSuffix}`;
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
        throw new Error(getChatHistoryI18n().settings.updateError);
    }

    return response.json();
}

async function handleChatSettingsAction(action) {
    const activeConversation = document.querySelector('#conversationsList .conversation-item.active[data-conversation-id]');
    if (!activeConversation) {
        showChatToast(getChatHistoryI18n().settings.selectConversation, 'warning');
        return;
    }

    const chatRoomId = activeConversation.dataset.conversationId;
    const chatPartnerName = activeConversation.dataset.userName || getChatHistoryI18n().unknownUser;
    const state = getConversationSettingsState(activeConversation);
    const i18n = getChatHistoryI18n();

    try {
        if (action === 'mute') {
            const settings = await updateConversationSettingsOnServer(chatRoomId, { muted: !state.muted });
            updateConversationSettingsState(chatRoomId, settings);
            showChatToast(
                settings.muted
                    ? formatI18nTemplate(i18n.toast.mutedOn, { name: chatPartnerName })
                    : formatI18nTemplate(i18n.toast.mutedOff, { name: chatPartnerName }),
                settings.muted ? 'info' : 'success'
            );
            return;
        }

        if (action === 'block') {
            const willBlock = !state.blocked;
            const settings = await updateConversationSettingsOnServer(chatRoomId, { blocked: willBlock });
            updateConversationSettingsState(chatRoomId, settings);
            showChatToast(
                settings.blocked
                    ? formatI18nTemplate(i18n.toast.blockedOn, { name: chatPartnerName })
                    : formatI18nTemplate(i18n.toast.blockedOff, { name: chatPartnerName }),
                settings.blocked ? 'warning' : 'success'
            );
            return;
        }

        if (action === 'report') {
            if (state.reported) {
                showChatToast(i18n.toast.reportedAlready, 'warning');
                return;
            }

            const settings = await updateConversationSettingsOnServer(chatRoomId, { reported: true });
            updateConversationSettingsState(chatRoomId, settings);
            showChatToast(formatI18nTemplate(i18n.toast.reportedDone, { name: chatPartnerName }), 'success');
        }
    } catch (error) {
        showChatToast(error.message || i18n.settings.updateError, 'error');
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
let proposalActionInProgress = false;

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
        showChatToast(getChatHistoryI18n().video.selectConversation, 'warning');
        return;
    }

    if (window.currentConversationSettings?.blocked === true) {
        showChatToast(getChatHistoryI18n().video.blocked, 'warning');
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

            const message = payload?.message || getChatHistoryI18n().video.startFailed;
            window.alert(message);
            return;
        }

        const meetingUrl = payload?.meetingUrl;
        if (!meetingUrl) {
            showChatToast(getChatHistoryI18n().video.missingUrl, 'error');
            return;
        }

        const newWindow = window.open(meetingUrl, '_blank', 'noopener,noreferrer');
        if (!newWindow) {
            showChatToast(getChatHistoryI18n().video.popupBlocked, 'warning');
        }
    } catch (error) {
        console.error('Error starting video call:', error);
        showChatToast(getChatHistoryI18n().video.errorStart, 'error');
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

async function runProposalAction(action) {
    if (proposalActionInProgress) {
        return;
    }

    const activeItem = document.querySelector('#conversationsList .conversation-item.active[data-conversation-id]');
    const proposalId = activeItem ? activeItem.dataset.proposalId : '';
    if (!activeItem || !proposalId) {
        return;
    }

    const allowedActions = getProposalActions(activeItem.dataset.proposalStatus || '', activeItem.dataset.proposalOwner === 'true');
    if (!allowedActions.includes(action)) {
        return;
    }

    proposalActionInProgress = true;
    updateProposalHeaderFromConversation(activeItem);
    setProposalHeaderState({
        proposalId,
        status: activeItem.dataset.proposalStatus || '',
        statusLabel: activeItem.dataset.proposalStatusLabel || '',
        isOwner: activeItem.dataset.proposalOwner === 'true',
        isBusy: true
    });

    try {
        const response = await fetch(`/api/skill-swap-proposals/${proposalId}/${action}`, {
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
            throw new Error(payload.message || getChatHistoryI18n().proposal.updateError);
        }

        const nextStatus = payload.status || activeItem.dataset.proposalStatus || '';
        updateConversationProposalState(activeItem.dataset.conversationId, {
            proposalId,
            status: nextStatus,
            statusLabel: mapProposalStatusLabel(nextStatus),
            isOwner: activeItem.dataset.proposalOwner === 'true',
            canAccept: false
        });
        showChatToast(payload.message || getChatHistoryI18n().proposal.updated, 'success');
    } catch (error) {
        window.alert(error.message || getChatHistoryI18n().proposal.updateError);
    } finally {
        proposalActionInProgress = false;
        const refreshedActiveItem = document.querySelector('#conversationsList .conversation-item.active[data-conversation-id]');
        if (refreshedActiveItem) {
            updateProposalHeaderFromConversation(refreshedActiveItem);
        } else {
            setProposalHeaderState();
        }
    }
}

function attachProposalActionDropdowns() {
    const wrappers = document.querySelectorAll('.proposal-actions-wrapper');
    if (!wrappers.length) {
        return;
    }

    wrappers.forEach(wrapper => {
        const trigger = wrapper.querySelector('.proposal-actions-btn');
        const menu = wrapper.querySelector('.proposal-actions-dropdown');
        if (!trigger || !menu) {
            return;
        }

        trigger.addEventListener('click', function (event) {
            event.preventDefault();
            event.stopPropagation();

            if (trigger.disabled) {
                return;
            }

            const willOpen = !wrapper.classList.contains('is-open');
            closeAllChatSettingsDropdowns(wrapper);
            wrapper.classList.toggle('is-open', willOpen);
            trigger.setAttribute('aria-expanded', willOpen ? 'true' : 'false');
        });

        menu.addEventListener('click', function (event) {
            const actionButton = event.target.closest('.proposal-action-item[data-proposal-action]');
            if (!actionButton || actionButton.disabled) {
                return;
            }

            event.preventDefault();
            event.stopPropagation();

            closeAllChatSettingsDropdowns();
            runProposalAction(actionButton.dataset.proposalAction);
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
    attachProposalActionDropdowns();

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
