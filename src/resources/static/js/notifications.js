/**
 * Notifications Dropdown JavaScript
 * API-backed in-app notifications with polling and optional WebSocket realtime.
 */

const NOTIFICATION_LIMIT = 20;
const NOTIFICATIONS_POLL_INTERVAL_MS = 20000;

const NOTIFICATION_ICON_BY_TYPE = {
    WELCOME: 'fa-heart',
    NEW_MESSAGE: 'fa-comment',
    SKILL_REQUEST: 'fa-handshake-o',
    REQUEST_ACCEPTED: 'fa-check-circle',
    REQUEST_NEGOTIATING: 'fa-comments-o',
    REQUEST_REJECTED: 'fa-times-circle',
    NEW_REVIEW: 'fa-star',
    SYSTEM: 'fa-info-circle'
};

const notificationsState = {
    items: [],
    pollingTimer: null,
    stompClient: null,
    realtimeConnected: false
};

/**
 * Returns true when viewport is in mobile range
 */
function isMobileViewport() {
    return window.innerWidth <= 767;
}

/**
 * Ensures mobile notifications panel exists and is attached directly to body
 * (avoids stacking/position issues when navbar is inside positioned containers)
 */
function ensureMobilePanel() {
    let panel = document.getElementById('mobileNotificationsPanel');

    if (!panel) {
        const wrapper = document.createElement('div');
        wrapper.innerHTML = `
            <div class="mobile-notifications-panel" id="mobileNotificationsPanel" aria-hidden="true">
                <div class="mobile-notifications-panel__header">
                    <button class="mnp-btn mnp-btn--back" id="mnpBackBtn" type="button" aria-label="Înapoi">
                        <i class="fa fa-arrow-left"></i>
                    </button>
                    <div class="mnp-title-wrap">
                        <span class="mnp-title">Notificări</span>
                        <span class="mnp-unread-badge hidden" id="mnpUnreadBadge">0</span>
                    </div>
                    <button class="mnp-btn mnp-btn--menu" id="mnpMenuBtn" type="button" aria-label="Opțiuni">
                        <i class="fa fa-ellipsis-v"></i>
                    </button>
                </div>
                <div class="mobile-notifications-panel__list" id="mobileNotificationsList"></div>
            </div>`;
        panel = wrapper.firstElementChild;
        document.body.appendChild(panel);
    } else if (panel.parentElement !== document.body) {
        document.body.appendChild(panel);
    }

    return panel;
}

/**
 * Initializes the notifications dropdown
 */
function initializeNotifications() {
    const notificationsButton = document.getElementById('notificationsButton');
    const notificationsDropdown = document.getElementById('notificationsDropdown');
    const notificationsList = document.getElementById('notificationsList');
    const notificationsMarkAllButton = document.getElementById('notificationsMarkAll');
    const mobilePanel = ensureMobilePanel();
    const mnpBackBtn = mobilePanel ? mobilePanel.querySelector('#mnpBackBtn') : null;

    if (!notificationsButton || !notificationsDropdown || !notificationsList) {
        return;
    }

    renderNotifications();
    refreshNotifications();

    // Toggle: use mobile panel on mobile, desktop dropdown on desktop
    notificationsButton.addEventListener('click', function(e) {
        e.stopPropagation();
        resetNotificationCounterOnOpen();

        if (isMobileViewport()) {
            toggleMobilePanel();
        } else {
            toggleNotificationsDropdown();
        }
    });

    // Mobile panel back button
    if (mnpBackBtn) {
        mnpBackBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            closeMobilePanel();
        });
    }

    if (notificationsMarkAllButton) {
        notificationsMarkAllButton.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            markAllNotificationsAsRead();
        });
    }

    // Close desktop dropdown when clicking outside
    document.addEventListener('click', function(e) {
        if (!notificationsDropdown.contains(e.target) &&
            !notificationsButton.contains(e.target)) {
            closeNotificationsDropdown();
        }
    });

    // On resize to desktop, close mobile panel if open
    window.addEventListener('resize', function() {
        if (!isMobileViewport()) {
            closeMobilePanel();
        } else {
            closeNotificationsDropdown();
        }
    });

    window.addEventListener('focus', refreshNotifications);
    document.addEventListener('visibilitychange', function() {
        if (!document.hidden) {
            refreshNotifications();
        }
    });

    if (!notificationsState.pollingTimer) {
        notificationsState.pollingTimer = window.setInterval(function() {
            if (!document.hidden) {
                refreshNotifications();
            }
        }, NOTIFICATIONS_POLL_INTERVAL_MS);
    }

    connectNotificationsRealtime();
}

/**
 * Resets unread counter immediately on bell click and syncs read-all in background.
 */
function resetNotificationCounterOnOpen() {
    const hasUnread = notificationsState.items.some(item => !item.read);
    if (!hasUnread) {
        return;
    }

    notificationsState.items = notificationsState.items.map(item =>
        Object.assign({}, item, { read: true })
    );

    renderNotifications();
    renderMobileNotifications();
    updateNotificationBadge();

    fetch('/api/notifications/read-all', {
        method: 'POST',
        headers: {
            'Accept': 'application/json'
        }
    }).catch(error => {
        console.warn('Could not sync read-all on notification bell click:', error);
    });
}

/**
 * Toggles the desktop notifications dropdown (desktop only)
 */
function toggleNotificationsDropdown() {
    const dropdown = document.getElementById('notificationsDropdown');
    if (dropdown) {
        dropdown.classList.toggle('is-open');
    }
}

/**
 * Closes the desktop notifications dropdown
 */
function closeNotificationsDropdown() {
    const dropdown = document.getElementById('notificationsDropdown');
    if (dropdown) {
        dropdown.classList.remove('is-open');
    }
}

/**
 * Opens the mobile full-screen notifications panel
 */
function openMobilePanel() {
    const panel = ensureMobilePanel();
    if (!panel) return;

    // Position panel below the app navbar dynamically
    const header = document.querySelector('.header-section') || document.querySelector('header');
    if (header) {
        const navBottom = header.getBoundingClientRect().bottom;
        panel.style.top = navBottom + 'px';
        panel.style.bottom = '0';
        panel.style.height = 'auto';
    }

    renderMobileNotifications();
    refreshNotifications();
    panel.classList.add('is-open');
    panel.setAttribute('aria-hidden', 'false');
    document.body.classList.add('notifications-mobile-open');
}

/**
 * Closes the mobile full-screen notifications panel
 */
function closeMobilePanel() {
    const panel = document.getElementById('mobileNotificationsPanel');
    if (!panel) return;
    panel.classList.remove('is-open');
    panel.setAttribute('aria-hidden', 'true');
    document.body.classList.remove('notifications-mobile-open');
}

/**
 * Toggles the mobile panel
 */
function toggleMobilePanel() {
    const panel = document.getElementById('mobileNotificationsPanel');
    if (!panel) return;
    if (panel.classList.contains('is-open')) {
        closeMobilePanel();
    } else {
        openMobilePanel();
    }
}

/**
 * Renders notifications in the desktop dropdown
 */
function renderNotifications() {
    const notificationsList = document.getElementById('notificationsList');
    if (!notificationsList) return;
    notificationsList.innerHTML = '';
    notificationsState.items.forEach(notification => {
        notificationsList.appendChild(createNotificationElement(notification));
    });

    if (!notificationsState.items.length) {
        notificationsList.innerHTML = '<div class="notification-item"><div class="notification-content"><div class="notification-title">Nu ai notificari</div><div class="notification-text">Cand apare ceva nou, il vezi aici.</div></div></div>';
    }
}

/**
 * Renders notifications in the mobile panel
 */
function renderMobileNotifications() {
    const list = document.getElementById('mobileNotificationsList');
    if (!list) return;
    list.innerHTML = '';
    notificationsState.items.forEach(notification => {
        list.appendChild(createNotificationElement(notification));
    });

    if (!notificationsState.items.length) {
        list.innerHTML = '<div class="notification-item"><div class="notification-content"><div class="notification-title">Nu ai notificari</div><div class="notification-text">Cand apare ceva nou, il vezi aici.</div></div></div>';
    }
}

/**
 * Creates a notification element
 * @param {Object} notification - Notification object
 * @returns {HTMLElement} Notification element
 */
function createNotificationElement(notification) {
    if (hasProposalPayload(notification)) {
        return createProposalNotificationElement(notification);
    }

    const item = document.createElement('div');
    item.className = 'notification-item';
    const isRead = notification.read === true;
    item.classList.add(isRead ? 'read' : 'unread');
    item.setAttribute('data-notification-id', notification.id);

    const icon = document.createElement('div');
    icon.className = 'notification-icon';
    const iconElement = document.createElement('i');
    iconElement.className = `fa ${NOTIFICATION_ICON_BY_TYPE[notification.type] || 'fa-bell-o'}`;
    icon.appendChild(iconElement);

    const content = document.createElement('div');
    content.className = 'notification-content';

    const title = document.createElement('div');
    title.className = 'notification-title';
    title.textContent = notification.title || 'Notificare';

    const text = document.createElement('div');
    text.className = 'notification-text';
    text.textContent = notification.message || '';

    const time = document.createElement('div');
    time.className = 'notification-time';
    time.textContent = formatNotificationTime(notification.createdAt);

    content.appendChild(title);
    content.appendChild(text);
    content.appendChild(time);

    item.appendChild(icon);
    item.appendChild(content);

    // Add click handler
    item.addEventListener('click', function() {
        handleNotificationClick(notification);
    });

    return item;
}

function hasProposalPayload(notification) {
    return Boolean(notification && notification.proposal && notification.proposal.proposalId);
}

function createProposalNotificationElement(notification) {
    const proposal = notification.proposal;
    const item = document.createElement('div');
    item.className = 'notification-item notification-item--proposal';
    item.classList.add(notification.read === true ? 'read' : 'unread');
    item.setAttribute('data-notification-id', notification.id);
    item.setAttribute('data-proposal-id', proposal.proposalId);

    const avatar = document.createElement('button');
    avatar.type = 'button';
    avatar.className = 'notification-avatar';
    avatar.innerHTML = `<img src="${escapeHtml(notification.proposal.actorAvatarUrl || '/img/default-avatar.png')}" alt="${escapeHtml(notification.proposal.actorName || 'Utilizator')}">`;
    avatar.addEventListener('click', function (event) {
        event.preventDefault();
        event.stopPropagation();
        openProposalProfile(notification);
    });

    const content = document.createElement('div');
    content.className = 'notification-content notification-content--proposal';

    const title = document.createElement('div');
    title.className = 'notification-title notification-proposal-title';

    const actorName = (proposal.actorName || '').trim();
    const rawTitle = (notification.title || '').trim();
    const fallbackTitle = proposal.actionable === true
        ? 'ti-a propus un Skill Swap'
        : (rawTitle || 'a actualizat propunerea de Skill Swap');
    let titleSuffix = fallbackTitle;

    if (actorName && rawTitle) {
        const rawTitleLower = rawTitle.toLowerCase();
        const actorNameLower = actorName.toLowerCase();

        if (rawTitleLower.startsWith(actorNameLower)) {
            titleSuffix = rawTitle.slice(actorName.length).trim();
        } else {
            titleSuffix = rawTitle;
        }
    }

    if (actorName) {
        const profileNameButton = document.createElement('button');
        profileNameButton.type = 'button';
        profileNameButton.className = 'notification-profile-trigger notification-profile-trigger--name';
        profileNameButton.textContent = actorName;
        profileNameButton.addEventListener('click', function (event) {
            event.preventDefault();
            event.stopPropagation();
            openProposalProfile(notification);
        });
        title.appendChild(profileNameButton);
    }

    if (titleSuffix) {
        const titleText = document.createElement('span');
        titleText.className = 'notification-proposal-title-text';
        titleText.textContent = actorName ? titleSuffix : (rawTitle || fallbackTitle);
        title.appendChild(titleText);
    }

    const summary = document.createElement('div');
    summary.className = 'notification-proposal-summary';
    summary.textContent = `${proposal.offeredSkill || ''} \u2194 ${proposal.requestedSkill || ''}`;

    const metaRow = document.createElement('div');
    metaRow.className = 'notification-meta-row';

    const time = document.createElement('div');
    time.className = 'notification-time';
    time.textContent = formatNotificationTime(notification.createdAt);
    metaRow.appendChild(time);

    if (proposal.statusLabel) {
        const statusPill = document.createElement('span');
        statusPill.className = 'notification-proposal-status';
        statusPill.textContent = proposal.statusLabel;
        metaRow.appendChild(statusPill);
    }

    content.appendChild(title);
    content.appendChild(summary);

    if (proposal.requesterMessage) {
        const note = document.createElement('div');
        note.className = 'notification-proposal-note';
        note.textContent = proposal.requesterMessage;
        content.appendChild(note);
    }

    content.appendChild(metaRow);

    if (proposal.actionable === true) {
        const actions = document.createElement('div');
        actions.className = 'notification-actions';

        actions.appendChild(createNotificationActionButton('Refuza', 'secondary', function (event) {
            event.preventDefault();
            event.stopPropagation();
            handleProposalAction(notification, 'reject');
        }));

        actions.appendChild(createNotificationActionButton('Accepta', 'primary', function (event) {
            event.preventDefault();
            event.stopPropagation();
            handleProposalAction(notification, 'accept');
        }));

        content.appendChild(actions);
    } else {
        item.addEventListener('click', function() {
            handleNotificationClick(notification);
        });
    }

    item.appendChild(avatar);
    item.appendChild(content);
    return item;
}

function createNotificationActionButton(label, variant, onClick) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = `notification-action-btn notification-action-btn--${variant}`;
    button.textContent = label;
    button.addEventListener('click', onClick);
    return button;
}

async function handleProposalAction(notification, action) {
    const proposal = notification && notification.proposal;
    if (!proposal || !proposal.proposalId) {
        return;
    }

    const endpoint = `/api/skill-swap-proposals/${proposal.proposalId}/${action}`;
    const item = document.querySelector(`.notification-item[data-notification-id="${notification.id}"]`);
    const actionButtons = item ? item.querySelectorAll('.notification-action-btn') : [];
    actionButtons.forEach(button => {
        button.disabled = true;
    });

    try {
        const response = await fetch(endpoint, {
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
            throw new Error(payload.message || 'Nu am putut procesa propunerea.');
        }

        await refreshNotifications();
        updateConversationBadge();

        closeNotificationsDropdown();
        closeMobilePanel();

        if ((action === 'accept' || action === 'negotiate') && payload.redirectUrl) {
            window.location.href = payload.redirectUrl;
            return;
        }
    } catch (error) {
        window.alert(error.message || 'Nu am putut procesa propunerea.');
    } finally {
        actionButtons.forEach(button => {
            button.disabled = false;
        });
    }
}

async function openProposalProfile(notification) {
    if (!notification) {
        return;
    }

    const targetUrl = resolveProposalProfileUrl(notification);
    if (!targetUrl) {
        return;
    }

    if (!notification.read) {
        await markNotificationAsRead(notification.id);
    }

    closeNotificationsDropdown();
    closeMobilePanel();
    window.location.href = targetUrl;
}

function resolveProposalProfileUrl(notification) {
    const proposal = notification && notification.proposal;
    if (!proposal) {
        return notification ? notification.targetUrl : null;
    }

    return proposal.viewProfileUrl || (proposal.actorUserId ? `/profile/${proposal.actorUserId}` : notification.targetUrl);
}

function resolveNotificationDestination(notification) {
    if (!hasProposalPayload(notification)) {
        return notification.targetUrl;
    }

    const proposal = notification.proposal;
    if (proposal.chatUrl && (proposal.status === 'ACCEPTED' || proposal.status === 'NEGOTIATING')) {
        return proposal.chatUrl;
    }

    if (proposal.status === 'REJECTED') {
        return resolveProposalProfileUrl(notification);
    }

    if (proposal.actionable !== true) {
        return resolveProposalProfileUrl(notification) || notification.targetUrl;
    }

    return notification.targetUrl;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
}

/**
 * Handles notification click
 * @param {number} notificationId - ID of the clicked notification
 */
async function handleNotificationClick(notification) {
    if (!notification) {
        return;
    }

    if (!notification.read) {
        await markNotificationAsRead(notification.id);
    }

    closeNotificationsDropdown();
    closeMobilePanel();

    const destination = resolveNotificationDestination(notification);
    if (destination) {
        window.location.href = destination;
    }
}

/**
 * Updates the notification badge count
 */
function updateNotificationBadge() {
    const badge = document.getElementById('notificationBadge');
    const mnpBadge = document.getElementById('mnpUnreadBadge');
    const markAllButton = document.getElementById('notificationsMarkAll');
    if (!badge) return;

    const unreadCount = notificationsState.items.filter(n => !n.read).length;

    if (markAllButton) {
        markAllButton.disabled = unreadCount === 0;
    }

    if (unreadCount > 0) {
        const displayValue = unreadCount > 99 ? '99+' : unreadCount.toString();
        badge.textContent = displayValue;
        badge.classList.remove('hidden');
        if (mnpBadge) {
            mnpBadge.textContent = displayValue;
            mnpBadge.classList.remove('hidden');
        }
    } else {
        badge.classList.add('hidden');
        if (mnpBadge) {
            mnpBadge.classList.add('hidden');
        }
    }
}

function formatNotificationTime(timestamp) {
    if (!timestamp) {
        return '';
    }

    const date = new Date(timestamp);
    if (Number.isNaN(date.getTime())) {
        return '';
    }

    const now = Date.now();
    const diffMs = Math.max(0, now - date.getTime());
    const diffMinutes = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMinutes / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMinutes < 1) return 'Acum';
    if (diffMinutes < 60) return `Acum ${diffMinutes} min`;
    if (diffHours < 24) return `Acum ${diffHours} h`;
    if (diffDays < 7) return `Acum ${diffDays} zile`;

    return date.toLocaleDateString('ro-RO', { day: '2-digit', month: '2-digit' });
}

async function refreshNotifications() {
    const notificationsButton = document.getElementById('notificationsButton');
    if (!notificationsButton) {
        return;
    }

    try {
        const response = await fetch(`/api/notifications?limit=${NOTIFICATION_LIMIT}`, {
            headers: {
                'Accept': 'application/json'
            }
        });

        if (response.status === 401 || response.status === 403) {
            notificationsState.items = [];
            renderNotifications();
            renderMobileNotifications();
            updateNotificationBadge();
            return;
        }

        if (!response.ok) {
            return;
        }

        const payload = await response.json();
        notificationsState.items = Array.isArray(payload) ? payload : [];
        renderNotifications();
        renderMobileNotifications();
        updateNotificationBadge();
    } catch (error) {
        console.warn('Could not refresh notifications:', error);
    }
}

async function markNotificationAsRead(notificationId) {
    try {
        const response = await fetch(`/api/notifications/${notificationId}/read`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok && response.status !== 404) {
            return false;
        }

        notificationsState.items = notificationsState.items.map(item => {
            if (item.id === notificationId) {
                return Object.assign({}, item, { read: true });
            }
            return item;
        });

        renderNotifications();
        renderMobileNotifications();
        updateNotificationBadge();
        return true;
    } catch (error) {
        console.warn('Could not mark notification as read:', error);
        return false;
    }
}

async function markAllNotificationsAsRead() {
    const markAllButton = document.getElementById('notificationsMarkAll');
    const hasUnreadNotifications = notificationsState.items.some(item => !item.read);

    if (!hasUnreadNotifications) {
        if (markAllButton) {
            markAllButton.disabled = true;
        }
        return;
    }

    if (markAllButton) {
        markAllButton.disabled = true;
    }

    try {
        const response = await fetch('/api/notifications/read-all', {
            method: 'POST',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            return;
        }

        notificationsState.items = notificationsState.items.map(item =>
            Object.assign({}, item, { read: true })
        );

        renderNotifications();
        renderMobileNotifications();
        updateNotificationBadge();
    } catch (error) {
        console.warn('Could not mark all notifications as read:', error);
    } finally {
        updateNotificationBadge();
    }
}

function connectNotificationsRealtime() {
    const notificationsButton = document.getElementById('notificationsButton');
    if (!notificationsButton) {
        return;
    }

    if (notificationsState.realtimeConnected) {
        return;
    }

    if (typeof window.SockJS !== 'function' || !window.Stomp) {
        return;
    }

    try {
        const socket = new SockJS('/ws');
        const stompClient = Stomp.over(socket);
        stompClient.debug = null;

        stompClient.connect({}, function() {
            notificationsState.realtimeConnected = true;
            notificationsState.stompClient = stompClient;

            stompClient.subscribe('/user/queue/notifications', function(message) {
                try {
                    const notification = JSON.parse(message.body);
                    if (!notification || !notification.id) {
                        refreshNotifications();
                        return;
                    }

                    notificationsState.items = [notification].concat(
                        notificationsState.items.filter(item => item.id !== notification.id)
                    ).slice(0, NOTIFICATION_LIMIT);

                    renderNotifications();
                    renderMobileNotifications();
                    updateNotificationBadge();
                } catch (_) {
                    refreshNotifications();
                }
            });
        }, function() {
            notificationsState.realtimeConnected = false;
            notificationsState.stompClient = null;
        });
    } catch (error) {
        console.warn('Could not initialize notifications realtime channel:', error);
    }
}

function setConversationBadgeCount(count) {
    const badge = document.getElementById('conversationBadge');
    if (!badge) {
        return;
    }

    const normalizedCount = Number.isFinite(count) ? Math.max(0, Math.floor(count)) : 0;

    if (normalizedCount > 0) {
        badge.textContent = normalizedCount > 99 ? '99+' : String(normalizedCount);
        badge.classList.remove('hidden');
        return;
    }

    badge.textContent = '0';
    badge.classList.add('hidden');
}

async function updateConversationBadge() {
    const badge = document.getElementById('conversationBadge');
    if (!badge) {
        return;
    }

    try {
        const response = await fetch('/api/chat/rooms', {
            headers: {
                'Accept': 'application/json'
            }
        });

        if (response.status === 401 || response.status === 403) {
            setConversationBadgeCount(0);
            return;
        }

        if (!response.ok) {
            return;
        }

        const conversations = await response.json();
        if (!Array.isArray(conversations)) {
            setConversationBadgeCount(0);
            return;
        }

        const unreadTotal = conversations.reduce((sum, conversation) => {
            const unread = Number(conversation && conversation.unreadCount);
            return sum + (Number.isFinite(unread) && unread > 0 ? unread : 0);
        }, 0);

        setConversationBadgeCount(unreadTotal);
    } catch (error) {
        console.warn('Could not refresh conversation badge:', error);
    }
}

function initializeConversationBadge() {
    const badge = document.getElementById('conversationBadge');
    if (!badge) {
        return;
    }

    updateConversationBadge();

    const refreshBadge = function () {
        updateConversationBadge();
    };

    window.addEventListener('focus', refreshBadge);

    document.addEventListener('visibilitychange', function () {
        if (!document.hidden) {
            refreshBadge();
        }
    });

    window.addEventListener('chat:conversations-updated', function (event) {
        const totalUnread = event && event.detail ? Number(event.detail.totalUnread) : NaN;
        if (Number.isFinite(totalUnread)) {
            setConversationBadgeCount(totalUnread);
        } else {
            refreshBadge();
        }
    });

    window.setInterval(refreshBadge, 30000);
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    initializeNotifications();
    initializeConversationBadge();
});

