/**
 * Notifications Dropdown JavaScript
 * Handles notifications button toggle and dropdown functionality
 */

// Mock notifications data - ready for backend integration
const mockNotifications = [
    {
        id: 1,
        type: 'message',
        icon: 'fa-comment',
        title: 'Mesaj nou',
        text: 'Maria Popescu ți-a trimis un mesaj nou',
        time: 'Acum 5 minute',
        unread: true
    },
    {
        id: 2,
        type: 'announce',
        icon: 'fa-bullhorn',
        title: 'Anunț nou',
        text: 'Un anunț nou pentru skill-ul tău a fost publicat',
        time: 'Acum 1 oră',
        unread: true
    },
    {
        id: 3,
        type: 'request',
        icon: 'fa-handshake-o',
        title: 'Cerere nouă',
        text: 'Ion Ionescu a trimis o cerere de schimb de skill-uri',
        time: 'Ieri',
        unread: true
    },
    {
        id: 4,
        type: 'message',
        icon: 'fa-comment',
        title: 'Mesaj nou',
        text: 'Ana Dumitrescu ți-a răspuns la mesaj',
        time: 'Acum 2 zile',
        unread: false
    },
    {
        id: 5,
        type: 'system',
        icon: 'fa-info-circle',
        title: 'Actualizare sistem',
        text: 'Platforma a fost actualizată cu noi funcționalități',
        time: 'Acum 3 zile',
        unread: false
    },
    {
        id: 6,
        type: 'announce',
        icon: 'fa-bullhorn',
        title: 'Anunț nou',
        text: 'Un anunț nou pentru skill-ul tău a fost publicat',
        time: 'Acum 4 zile',
        unread: false
    },
    {
        id: 7,
        type: 'request',
        icon: 'fa-handshake-o',
        title: 'Cerere aprobată',
        text: 'Cererea ta de schimb de skill-uri a fost aprobată',
        time: 'Săptămâna trecută',
        unread: false
    }
];

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
    const mobilePanel = ensureMobilePanel();
    const mnpBackBtn = mobilePanel ? mobilePanel.querySelector('#mnpBackBtn') : null;

    if (!notificationsButton || !notificationsDropdown || !notificationsList) {
        return;
    }

    // Render notifications into the desktop dropdown
    renderNotifications();

    // Toggle: use mobile panel on mobile, desktop dropdown on desktop
    notificationsButton.addEventListener('click', function(e) {
        e.stopPropagation();
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

    // Update badge count
    updateNotificationBadge();
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
    mockNotifications.forEach(notification => {
        notificationsList.appendChild(createNotificationElement(notification));
    });
}

/**
 * Renders notifications in the mobile panel
 */
function renderMobileNotifications() {
    const list = document.getElementById('mobileNotificationsList');
    if (!list) return;
    list.innerHTML = '';
    mockNotifications.forEach(notification => {
        list.appendChild(createNotificationElement(notification));
    });
}

/**
 * Creates a notification element
 * @param {Object} notification - Notification object
 * @returns {HTMLElement} Notification element
 */
function createNotificationElement(notification) {
    const item = document.createElement('div');
    item.className = 'notification-item';
    if (notification.unread) {
        item.classList.add('unread');
    }
    item.setAttribute('data-notification-id', notification.id);

    const icon = document.createElement('div');
    icon.className = 'notification-icon';
    const iconElement = document.createElement('i');
    iconElement.className = `fa ${notification.icon}`;
    icon.appendChild(iconElement);

    const content = document.createElement('div');
    content.className = 'notification-content';

    const title = document.createElement('div');
    title.className = 'notification-title';
    title.textContent = notification.title;

    const text = document.createElement('div');
    text.className = 'notification-text';
    text.textContent = notification.text;

    const time = document.createElement('div');
    time.className = 'notification-time';
    time.textContent = notification.time;

    content.appendChild(title);
    content.appendChild(text);
    content.appendChild(time);

    item.appendChild(icon);
    item.appendChild(content);

    // Add click handler
    item.addEventListener('click', function() {
        handleNotificationClick(notification.id);
    });

    return item;
}

/**
 * Handles notification click
 * @param {number} notificationId - ID of the clicked notification
 */
function handleNotificationClick(notificationId) {
    // Mark as read
    const notification = mockNotifications.find(n => n.id === notificationId);
    if (notification && notification.unread) {
        notification.unread = false;
        
        // Update UI
        const item = document.querySelector(`[data-notification-id="${notificationId}"]`);
        if (item) {
            item.classList.remove('unread');
        }
        
        // Update badge
        updateNotificationBadge();
    }

    // In a real implementation, this would navigate to the relevant page
    // or perform the appropriate action based on notification type
    console.log('Notification clicked:', notificationId);
}

/**
 * Updates the notification badge count
 */
function updateNotificationBadge() {
    const badge = document.getElementById('notificationBadge');
    const mnpBadge = document.getElementById('mnpUnreadBadge');
    if (!badge) return;

    const unreadCount = mockNotifications.filter(n => n.unread).length;

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

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    initializeNotifications();
});

