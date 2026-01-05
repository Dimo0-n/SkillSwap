/**
 * Profile Completion Page - Interactive Functionality
 * Handles step navigation, scroll tracking, and form interactions
 */

(function() {
    'use strict';

    // State management
    const state = {
        skills: [],
        limitations: [],
        customStrengths: [],
        profilePicture: null,
        currentSection: 'identity',
        completedSections: new Set()
    };

    // Section mapping
    const sections = [
        { id: 'identity', element: '#section-identity' },
        { id: 'bio', element: '#section-bio' },
        { id: 'skills', element: '#section-skills' },
        { id: 'description', element: '#section-description' },
        { id: 'availability', element: '#section-availability' },
        { id: 'strengths', element: '#section-strengths' },
        { id: 'settings', element: '#section-settings' }
    ];

    // Initialize on DOM ready
    $(document).ready(function() {
        initializeEventListeners();
        initializeStepNavigation();
        initializeScrollTracking();
        initializeLivePreview();
        updateProgress();
    });

    /**
     * Initialize step navigation
     */
    function initializeStepNavigation() {
        $('.step-item').on('click', function(e) {
            e.preventDefault();
            const sectionId = $(this).data('section');
            const section = sections.find(s => s.id === sectionId);
            
            if (section) {
                const $target = $(section.element);
                if ($target.length) {
                    $('html, body').animate({
                        scrollTop: $target.offset().top - 120
                    }, 600);
                }
            }
        });
    }

    /**
     * Initialize scroll tracking to highlight active section
     */
    function initializeScrollTracking() {
        let ticking = false;

        function updateActiveSection() {
            const scrollPos = $(window).scrollTop() + 200; // Offset for sticky header
            
            let currentActive = null;
            
            sections.forEach(function(section) {
                const $section = $(section.element);
                if ($section.length) {
                    const sectionTop = $section.offset().top;
                    const sectionHeight = $section.outerHeight();
                    
                    if (scrollPos >= sectionTop && scrollPos < sectionTop + sectionHeight) {
                        currentActive = section.id;
                    }
                }
            });

            if (currentActive && currentActive !== state.currentSection) {
                state.currentSection = currentActive;
                updateActiveStep();
                updateProgress();
            }
        }

        function requestTick() {
            if (!ticking) {
                window.requestAnimationFrame(function() {
                    updateActiveSection();
                    ticking = false;
                });
                ticking = true;
            }
        }

        $(window).on('scroll', requestTick);
        updateActiveSection(); // Initial call
    }

    /**
     * Update active step in navigation
     */
    function updateActiveStep() {
        $('.step-item').removeClass('active');
        $('.step-item[data-section="' + state.currentSection + '"]').addClass('active');
    }

    /**
     * Check if section is completed and mark it
     */
    function checkSectionCompletion() {
        sections.forEach(function(section) {
            const sectionId = section.id;
            let isCompleted = false;

            switch(sectionId) {
                case 'identity':
                    isCompleted = $('#displayName').val().trim() !== '' && 
                                 $('#professionalTitle').val().trim() !== '';
                    break;
                case 'bio':
                    isCompleted = $('#shortBio').val().trim() !== '';
                    break;
                case 'skills':
                    isCompleted = state.skills.length > 0;
                    break;
                case 'description':
                    isCompleted = $('#detailedDescription').val().trim() !== '';
                    break;
                case 'availability':
                    isCompleted = $('input[type="checkbox"][id="morning"], input[type="checkbox"][id="afternoon"], input[type="checkbox"][id="evening"], input[type="checkbox"][id="weekend"]').is(':checked') ||
                                 state.limitations.length > 0;
                    break;
                case 'strengths':
                    isCompleted = $('.strength-checkbox:checked').length > 0 || 
                                 state.customStrengths.length > 0;
                    break;
                case 'settings':
                    isCompleted = true; // Settings are always considered "complete"
                    break;
            }

            const $stepItem = $('.step-item[data-section="' + sectionId + '"]');
            if (isCompleted) {
                state.completedSections.add(sectionId);
                $stepItem.addClass('completed');
            } else {
                state.completedSections.delete(sectionId);
                $stepItem.removeClass('completed');
            }
        });

        updateProgress();
    }

    /**
     * Update progress indicator
     */
    function updateProgress() {
        const total = sections.length;
        const completed = state.completedSections.size;
        const currentIndex = sections.findIndex(s => s.id === state.currentSection) + 1;

        // Update mobile progress
        $('#currentStep').text(currentIndex);
        $('#totalSteps').text(total);
        const progressPercent = (completed / total) * 100;
        $('#progressFill').css('width', progressPercent + '%');
    }

    /**
     * Initialize all event listeners
     */
    function initializeEventListeners() {
        // Profile picture upload
        $('#profilePicture').on('change', handlePictureUpload);
        $('.picture-preview').on('click', function() {
            $('#profilePicture').click();
        });

        // Bio character counter
        $('#shortBio').on('input', function() {
            updateBioCharCount();
            checkSectionCompletion();
        });

        // Skills management
        $('#addSkillBtn').on('click', addSkill);
        $('#skillInput').on('keypress', function(e) {
            if (e.which === 13) {
                e.preventDefault();
                addSkill();
            }
        });

        // Limitations management
        $('#addLimitationBtn').on('click', addLimitation);
        $('#limitationsInput').on('keypress', function(e) {
            if (e.which === 13) {
                e.preventDefault();
                addLimitation();
            }
        });

        // Custom strengths
        $('#addCustomStrengthBtn').on('click', addCustomStrength);
        $('#customStrengthInput').on('keypress', function(e) {
            if (e.which === 13) {
                e.preventDefault();
                addCustomStrength();
            }
        });

        // Live preview updates
        $('#displayName, #professionalTitle, #shortBio, #detailedDescription').on('input', function() {
            updateLivePreview();
            checkSectionCompletion();
        });

        $('.strength-checkbox').on('change', function() {
            updateLivePreview();
            checkSectionCompletion();
        });

        // Availability checkboxes
        $('#morning, #afternoon, #evening, #weekend').on('change', checkSectionCompletion);

        // Save buttons
        $('#saveProfileBtn').on('click', saveProfile);
        $('#saveDraftBtn').on('click', saveDraft);
    }

    /**
     * Handle profile picture upload (placeholder - no actual upload)
     */
    function handlePictureUpload(e) {
        const file = e.target.files[0];
        if (!file) return;

        // Validate file type
        if (!file.type.startsWith('image/')) {
            alert('Te rugăm să selectezi o imagine validă.');
            return;
        }

        // Validate file size (max 5MB)
        if (file.size > 5 * 1024 * 1024) {
            alert('Imaginea este prea mare. Te rugăm să selectezi o imagine mai mică de 5MB.');
            return;
        }

        // Show preview (placeholder - no actual upload)
        const reader = new FileReader();
        reader.onload = function(e) {
            const preview = $('#picturePreview');
            preview.html('<img src="' + e.target.result + '" alt="Profile Picture">');
            state.profilePicture = e.target.result;
            updateLivePreview();
            checkSectionCompletion();
        };
        reader.readAsDataURL(file);
    }

    /**
     * Update bio character counter
     */
    function updateBioCharCount() {
        const text = $('#shortBio').val();
        const count = text.length;
        $('#bioCharCount').text(count);
        
        // Change color if approaching limit
        const counter = $('.char-counter');
        if (count > 180) {
            counter.css('color', '#ff6b6b');
        } else if (count > 150) {
            counter.css('color', '#ffa500');
        } else {
            counter.css('color', '#a0a0b8');
        }
        
        updateLivePreview();
    }

    /**
     * Add skill to list
     */
    function addSkill() {
        const input = $('#skillInput');
        const skill = input.val().trim();
        
        if (!skill) {
            return;
        }

        if (state.skills.includes(skill)) {
            alert('Această competență este deja adăugată.');
            return;
        }

        if (state.skills.length >= 15) {
            alert('Poți adăuga maximum 15 competențe.');
            return;
        }

        state.skills.push(skill);
        input.val('');
        renderSkills();
        updateLivePreview();
        checkSectionCompletion();
    }

    /**
     * Remove skill from list
     */
    function removeSkill(index) {
        state.skills.splice(index, 1);
        renderSkills();
        updateLivePreview();
        checkSectionCompletion();
    }

    /**
     * Render skills as badges
     */
    function renderSkills() {
        const container = $('#skillsContainer');
        container.empty();

        state.skills.forEach(function(skill, index) {
            const badge = $('<div>')
                .addClass('skill-badge')
                .html('<span>' + escapeHtml(skill) + '</span>' +
                      '<button type="button" class="remove-btn" data-index="' + index + '">×</button>');
            container.append(badge);
        });

        // Add remove event listeners
        container.find('.remove-btn').on('click', function() {
            const index = parseInt($(this).data('index'));
            removeSkill(index);
        });
    }

    /**
     * Add limitation to list
     */
    function addLimitation() {
        const input = $('#limitationsInput');
        const limitation = input.val().trim();
        
        if (!limitation) {
            return;
        }

        if (state.limitations.includes(limitation)) {
            alert('Această limitare este deja adăugată.');
            return;
        }

        state.limitations.push(limitation);
        input.val('');
        renderLimitations();
        checkSectionCompletion();
    }

    /**
     * Remove limitation from list
     */
    function removeLimitation(index) {
        state.limitations.splice(index, 1);
        renderLimitations();
        checkSectionCompletion();
    }

    /**
     * Render limitations as badges
     */
    function renderLimitations() {
        const container = $('#limitationsContainer');
        container.empty();

        state.limitations.forEach(function(limitation, index) {
            const badge = $('<div>')
                .addClass('limitation-badge')
                .html('<span>' + escapeHtml(limitation) + '</span>' +
                      '<button type="button" class="remove-btn" data-index="' + index + '">×</button>');
            container.append(badge);
        });

        // Add remove event listeners
        container.find('.remove-btn').on('click', function() {
            const index = parseInt($(this).data('index'));
            removeLimitation(index);
        });
    }

    /**
     * Add custom strength
     */
    function addCustomStrength() {
        const input = $('#customStrengthInput');
        const strength = input.val().trim();
        
        if (!strength) {
            return;
        }

        if (state.customStrengths.includes(strength)) {
            alert('Această trăsătură este deja adăugată.');
            return;
        }

        state.customStrengths.push(strength);
        input.val('');
        renderCustomStrengths();
        checkSectionCompletion();
    }

    /**
     * Remove custom strength
     */
    function removeCustomStrength(index) {
        state.customStrengths.splice(index, 1);
        renderCustomStrengths();
        checkSectionCompletion();
    }

    /**
     * Render custom strengths as badges
     */
    function renderCustomStrengths() {
        const container = $('#customStrengthsContainer');
        container.empty();

        state.customStrengths.forEach(function(strength, index) {
            const badge = $('<div>')
                .addClass('strength-badge')
                .html('<span>' + escapeHtml(strength) + '</span>' +
                      '<button type="button" class="remove-btn" data-index="' + index + '">×</button>');
            container.append(badge);
        });

        // Add remove event listeners
        container.find('.remove-btn').on('click', function() {
            const index = parseInt($(this).data('index'));
            removeCustomStrength(index);
        });
    }

    /**
     * Update live preview in real-time
     * (Preview section removed - function kept for compatibility)
     */
    function updateLivePreview() {
        // Preview section removed
    }

    /**
     * Initialize live preview with default values
     */
    function initializeLivePreview() {
        // Preview section removed
    }

    /**
     * Save profile (placeholder)
     */
    function saveProfile() {
        // Collect all form data
        const profileData = {
            displayName: $('#displayName').val(),
            professionalTitle: $('#professionalTitle').val(),
            shortBio: $('#shortBio').val(),
            detailedDescription: $('#detailedDescription').val(),
            skills: state.skills,
            limitations: state.limitations,
            customStrengths: state.customStrengths,
            availability: {
                morning: $('#morning').is(':checked'),
                afternoon: $('#afternoon').is(':checked'),
                evening: $('#evening').is(':checked'),
                weekend: $('#weekend').is(':checked')
            },
            visibility: {
                public: $('#profilePublic').is(':checked'),
                allowDirectContact: $('#allowDirectContact').is(':checked')
            },
            selectedStrengths: []
        };

        // Collect selected predefined strengths
        $('.strength-checkbox:checked').each(function() {
            profileData.selectedStrengths.push($(this).val());
        });

        // In a real implementation, this would send data to the server
        console.log('Profile data to save:', profileData);

        // Show success message
        showSuccessModal();
    }

    /**
     * Save as draft (placeholder)
     */
    function saveDraft() {
        // Same as save, but marked as draft
        const profileData = {
            draft: true,
            displayName: $('#displayName').val(),
            professionalTitle: $('#professionalTitle').val(),
            shortBio: $('#shortBio').val(),
            detailedDescription: $('#detailedDescription').val(),
            skills: state.skills,
            limitations: state.limitations,
            customStrengths: state.customStrengths
        };

        console.log('Draft data to save:', profileData);
        showSuccessModal('Draft salvat cu succes!');
    }

    /**
     * Show success modal
     */
    function showSuccessModal(message) {
        if (message) {
            $('#successModal .modal-content p').text(message);
        }
        $('#successModal').fadeIn(300);
    }

    /**
     * Close success modal (called from HTML onclick)
     */
    window.closeSuccessModal = function() {
        $('#successModal').fadeOut(300);
    };

    // Close modal when clicking outside
    $(document).on('click', '.modal-overlay', function(e) {
        if ($(e.target).hasClass('modal-overlay')) {
            window.closeSuccessModal();
        }
    });

    /**
     * Escape HTML to prevent XSS
     */
    function escapeHtml(text) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.replace(/[&<>"']/g, function(m) { return map[m]; });
    }

})();
