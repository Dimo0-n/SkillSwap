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
 * Initializes the notifications dropdown
 */
function initializeNotifications() {
    const notificationsButton = document.getElementById('notificationsButton');
    const notificationsDropdown = document.getElementById('notificationsDropdown');
    const notificationsList = document.getElementById('notificationsList');
    const notificationBadge = document.getElementById('notificationBadge');

    if (!notificationsButton || !notificationsDropdown || !notificationsList) {
        return;
    }

    // Render notifications
    renderNotifications();

    // Toggle dropdown on button click
    notificationsButton.addEventListener('click', function(e) {
        e.stopPropagation();
        toggleNotificationsDropdown();
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', function(e) {
        if (!notificationsDropdown.contains(e.target) && 
            !notificationsButton.contains(e.target)) {
            closeNotificationsDropdown();
        }
    });

    // Update badge count
    updateNotificationBadge();
}

/**
 * Toggles the notifications dropdown
 */
function toggleNotificationsDropdown() {
    const dropdown = document.getElementById('notificationsDropdown');
    const button = document.getElementById('notificationsButton');
    
    if (dropdown && button) {
        const isOpen = dropdown.classList.contains('is-open');
        dropdown.classList.toggle('is-open');
    }
}

/**
 * Closes the notifications dropdown
 */
function closeNotificationsDropdown() {
    const dropdown = document.getElementById('notificationsDropdown');
    if (dropdown) {
        dropdown.classList.remove('is-open');
    }
}

/**
 * Renders notifications in the dropdown
 */
function renderNotifications() {
    const notificationsList = document.getElementById('notificationsList');
    if (!notificationsList) return;

    // Clear existing notifications
    notificationsList.innerHTML = '';

    // Render notifications (max 5 visible, but allow scroll for more)
    mockNotifications.forEach(notification => {
        const notificationElement = createNotificationElement(notification);
        notificationsList.appendChild(notificationElement);
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
    if (!badge) return;

    const unreadCount = mockNotifications.filter(n => n.unread).length;
    
    if (unreadCount > 0) {
        badge.textContent = unreadCount > 99 ? '99+' : unreadCount.toString();
        badge.classList.remove('hidden');
    } else {
        badge.classList.add('hidden');
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    initializeNotifications();
});

