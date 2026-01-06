/**
 * Chat History JavaScript
 * Handles conversation selection, message display, and search functionality
 * All data is mock/static - ready for backend integration
 */

// Mock messages data - organized by conversation ID
const mockMessages = {
    1: [
        {
            type: 'incoming',
            text: 'Salut! Ai putea să-mi explici cum funcționează autentificarea în Spring Security?',
            time: '10:15'
        },
        {
            type: 'outgoing',
            text: 'Desigur! Îți pot explica pas cu pas. Ai deja un proiect Spring Boot configurat?',
            time: '10:18'
        },
        {
            type: 'incoming',
            text: 'Da, am deja un proiect de bază. Cum încep?',
            time: '10:20'
        },
        {
            type: 'outgoing',
            text: 'Perfect! Primul pas este să adaugi dependența Spring Security în pom.xml. Dorești să discutăm mai multe detalii?',
            time: '10:22'
        },
        {
            type: 'incoming',
            text: 'Mulțumesc pentru ajutor! Totul e clar acum.',
            time: '14:32'
        }
    ],
    2: [
        {
            type: 'incoming',
            text: 'Bună! Poți să-mi explici cum funcționează Spring Boot?',
            time: '09:00'
        },
        {
            type: 'outgoing',
            text: 'Desigur! Spring Boot este un framework care simplifică dezvoltarea aplicațiilor Java.',
            time: '09:05'
        },
        {
            type: 'incoming',
            text: 'Ce avantaje are față de Spring clasic?',
            time: '09:10'
        },
        {
            type: 'outgoing',
            text: 'Principalul avantaj este auto-configurarea și dependency management simplificat.',
            time: '09:12'
        }
    ],
    3: [
        {
            type: 'incoming',
            text: 'Salut! Ești disponibil mâine pentru un meeting?',
            time: '11:00'
        },
        {
            type: 'outgoing',
            text: 'Da, sunt disponibil. La ce oră te-ar conveni?',
            time: '11:15'
        },
        {
            type: 'incoming',
            text: 'Perfect! Ne vedem mâine la ora 10.',
            time: '12:15'
        }
    ],
    4: [
        {
            type: 'incoming',
            text: 'Salut! Ai timp să discutăm despre React?',
            time: '15:00'
        },
        {
            type: 'outgoing',
            text: 'Bună! Da, cu siguranță. Ce aspecte te interesează?',
            time: '15:30'
        }
    ],
    5: [
        {
            type: 'incoming',
            text: 'Mulțumesc mult pentru tutorial! A fost foarte util.',
            time: '16:00'
        },
        {
            type: 'outgoing',
            text: 'Mă bucur că ți-a fost de folos! Dacă mai ai întrebări, nu ezita să întrebi.',
            time: '16:10'
        }
    ],
    6: [
        {
            type: 'incoming',
            text: 'Poți să-mi recomanzi niște resurse pentru JavaScript?',
            time: '14:00'
        },
        {
            type: 'outgoing',
            text: 'Desigur! Îți recomand MDN Web Docs și JavaScript.info - sunt excelente resurse.',
            time: '14:20'
        }
    ],
    7: [
        {
            type: 'incoming',
            text: 'Excelent! Chiar am nevoie de ajutor cu CSS.',
            time: '13:00'
        },
        {
            type: 'outgoing',
            text: 'Perfect! CSS-ul poate fi complicat, dar cu practică devine mai ușor. Cu ce aspecte vrei să începem?',
            time: '13:15'
        }
    ]
};

/**
 * Selects a conversation and displays its messages
 * @param {number} conversationId - The ID of the conversation to select
 */
function selectConversation(conversationId) {
    // Remove active class from all conversation items
    document.querySelectorAll('.conversation-item').forEach(item => {
        item.classList.remove('active');
    });

    // Add active class to selected conversation
    const selectedItem = document.querySelector(`[data-conversation-id="${conversationId}"]`);
    if (selectedItem) {
        selectedItem.classList.add('active');
        
        // Remove unread badge when conversation is selected
        const unreadBadge = selectedItem.querySelector('.unread-badge');
        if (unreadBadge) {
            unreadBadge.remove();
        }

        // Update chat header with user info
        const userName = selectedItem.getAttribute('data-user-name');
        const userAvatar = selectedItem.getAttribute('data-user-avatar');
        const userStatus = selectedItem.getAttribute('data-user-status');

        updateChatHeader(userName, userAvatar, userStatus);

        // Display messages for this conversation
        displayMessages(conversationId);

        // Scroll to bottom of messages
        scrollToBottom();
    }
}

/**
 * Updates the chat header with user information
 * @param {string} userName - Name of the user
 * @param {string} userAvatar - Path to avatar image
 * @param {string} userStatus - Status (online/offline)
 */
function updateChatHeader(userName, userAvatar, userStatus) {
    const headerName = document.getElementById('chatHeaderName');
    const headerAvatar = document.getElementById('chatHeaderAvatar');
    const headerStatus = document.getElementById('chatHeaderStatus');
    const headerStatusText = document.getElementById('chatHeaderStatusText');

    if (headerName) headerName.textContent = userName;
    if (headerAvatar) headerAvatar.src = userAvatar;
    
    if (headerStatus) {
        headerStatus.className = 'status-indicator';
        headerStatus.classList.add(userStatus === 'online' ? 'status-online' : 'status-offline');
    }
    
    if (headerStatusText) {
        headerStatusText.textContent = userStatus === 'online' ? 'Online' : 'Offline';
    }
}

/**
 * Displays messages for a specific conversation
 * @param {number} conversationId - The ID of the conversation
 */
function displayMessages(conversationId) {
    const messagesContainer = document.getElementById('messagesContainer');
    if (!messagesContainer) return;

    // Hide all message groups
    document.querySelectorAll('.message-group').forEach(group => {
        group.classList.remove('active');
        group.style.display = 'none';
    });

    // Check if message group exists for this conversation
    let messageGroup = document.querySelector(`.message-group[data-conversation-id="${conversationId}"]`);
    
    if (!messageGroup) {
        // Create new message group if it doesn't exist
        messageGroup = document.createElement('div');
        messageGroup.className = 'message-group';
        messageGroup.setAttribute('data-conversation-id', conversationId);
        
        // Get messages for this conversation
        const messages = mockMessages[conversationId] || [];
        
        // Create message elements
        messages.forEach(message => {
            const messageElement = createMessageElement(message);
            messageGroup.appendChild(messageElement);
        });
        
        messagesContainer.appendChild(messageGroup);
    }

    // Show the message group
    messageGroup.classList.add('active');
    messageGroup.style.display = 'flex';
}

/**
 * Creates a message element
 * @param {Object} message - Message object with type, text, and time
 * @returns {HTMLElement} Message element
 */
function createMessageElement(message) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message message-${message.type}`;

    const messageContent = document.createElement('div');
    messageContent.className = 'message-content';

    const messageBubble = document.createElement('div');
    messageBubble.className = 'message-bubble';
    
    const messageText = document.createElement('p');
    messageText.textContent = message.text;
    messageBubble.appendChild(messageText);

    const messageTime = document.createElement('div');
    messageTime.className = 'message-time';
    messageTime.textContent = message.time;

    messageContent.appendChild(messageBubble);
    messageContent.appendChild(messageTime);

    // Add avatar for incoming messages
    if (message.type === 'incoming') {
        const avatar = document.createElement('div');
        avatar.className = 'message-avatar';
        
        const avatarImg = document.createElement('img');
        const selectedConversation = document.querySelector('.conversation-item.active');
        if (selectedConversation) {
            avatarImg.src = selectedConversation.getAttribute('data-user-avatar');
            avatarImg.alt = selectedConversation.getAttribute('data-user-name');
        }
        
        avatar.appendChild(avatarImg);
        messageDiv.appendChild(avatar);
    }

    messageDiv.appendChild(messageContent);

    return messageDiv;
}

/**
 * Filters conversations based on search term
 * @param {string} searchTerm - The search term to filter by
 */
function filterConversations(searchTerm) {
    const conversationItems = document.querySelectorAll('.conversation-item');
    const searchTermLower = searchTerm.toLowerCase().trim();

    conversationItems.forEach(item => {
        const userName = item.getAttribute('data-user-name').toLowerCase();
        const previewText = item.querySelector('.conversation-preview').textContent.toLowerCase();

        if (userName.includes(searchTermLower) || previewText.includes(searchTermLower)) {
            item.style.display = 'flex';
        } else {
            item.style.display = 'none';
        }
    });
}

/**
 * Scrolls to the bottom of the messages container
 */
function scrollToBottom() {
    const messagesContainer = document.getElementById('messagesContainer');
    if (messagesContainer) {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
}

/**
 * Initializes the chat history page
 */
function initializeChatHistory() {
    // Set first conversation as active on page load
    const firstConversation = document.querySelector('.conversation-item');
    if (firstConversation) {
        const conversationId = parseInt(firstConversation.getAttribute('data-conversation-id'));
        selectConversation(conversationId);
    }

    // Add click listeners to conversation items
    document.querySelectorAll('.conversation-item').forEach(item => {
        item.addEventListener('click', function() {
            const conversationId = parseInt(this.getAttribute('data-conversation-id'));
            selectConversation(conversationId);
        });
    });

    // Add search functionality
    const searchInput = document.getElementById('conversationSearch');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            filterConversations(this.value);
        });
    }

    // Handle message form submission (mock - doesn't actually send)
    const messageForm = document.getElementById('messageForm');
    if (messageForm) {
        messageForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const messageInput = document.getElementById('messageInput');
            if (messageInput && messageInput.value.trim()) {
                // Mock: Just clear the input
                // In a real implementation, this would send the message to the backend
                messageInput.value = '';
            }
        });
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    initializeChatHistory();
});

