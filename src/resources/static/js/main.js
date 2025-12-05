/*  ---------------------------------------------------
    Template Name: Amin
    Description:  Amin magazine HTML Template
    Author: Colorlib
    Author URI: https://colorlib.com
    Version: 1.0
    Created: Colorlib
---------------------------------------------------------  */

'use strict';

(function ($) {

    /*------------------
        Preloader
    --------------------*/
    $(window).on('load', function () {
        $(".loader").fadeOut();
        $("#preloder").delay(200).fadeOut("slow");
    });

    /*------------------
        Background Set
    --------------------*/
    $('.set-bg').each(function () {
        var bg = $(this).data('setbg');
        $(this).css('background-image', 'url(' + bg + ')');
    });

    // Humberger Menu
    $(".humberger-open").on('click', function () {
        $(".humberger-menu-wrapper").addClass("show-humberger-menu");
        $(".humberger-menu-overlay").addClass("active");
        $(".nav-options").addClass("humberger-change");
    });

    $(".humberger-menu-overlay").on('click', function () {
        $(".humberger-menu-wrapper").removeClass("show-humberger-menu");
        $(".humberger-menu-overlay").removeClass("active");
        $(".nav-options").removeClass("humberger-change");
    });

    // Navbar search panel
    const $navbarSearchPanel = $('.navbar-search-panel');
    const $navbarSearchInput = $('#navbar-search-input');

    $('.navbar-search-toggle').on('click', function (e) {
        e.stopPropagation();
        $navbarSearchPanel.toggleClass('is-open');
        if ($navbarSearchPanel.hasClass('is-open')) {
            $navbarSearchInput.trigger('focus');
        }
    });

    $(document).on('click', function (e) {
        if ($navbarSearchPanel.hasClass('is-open') &&
            !$(e.target).closest('.navbar-search-panel, .navbar-search-toggle').length) {
            $navbarSearchPanel.removeClass('is-open');
        }
    });

    $('.navbar-search-panel form').on('submit', function () {
        $navbarSearchPanel.removeClass('is-open');
    });

    // Search model (fallback)
    $('.search-switch').on('click', function () {
        $('.search-model').fadeIn(400);
    });

    $('.search-close-switch').on('click', function () {
        $('.search-model').fadeOut(400, function () {
            $('#search-input').val('');
        });
    });

    // Sign Up Form
    $('.signup-switch').on('click', function () {
        $('.signup-section').fadeIn(400);
    });

    $('.signup-close').on('click', function () {
        $('.signup-section').fadeOut(400);
    });

    function switchToLogin() {
        console.log('Switching to login form');
        // Hide register form and show login form
        $('.signup-section').find('h2').text('Sign in');
        $('.signup-section').find('p').text('Fill out the form below to recieve a free and confidential');
        $('.signup-section').find('form').attr('action', '/login');

        // Clear any existing error messages
        $(".error-message").remove();
        $(".success-message").remove();

        // Update form fields for login
        var formHtml = `
            <div class="sf-input-list">
                <input type="text" class="input-value" placeholder="Email" name="email" required>
                <input type="password" class="input-value" placeholder="Password" name="password" required>
            </div>
            <button type="submit"><span>LOGIN</span></button>
            <button type="button" class="button" id="go-to-register">
                <span>GO TO REGISTER</span>
            </button>
        `;
        $('.signup-section').find('form').html(formHtml);
    }

    function switchToRegister() {
        // Hide login form and show register form
        $('.signup-section').find('h2').text('Sign up');
        $('.signup-section').find('p').text('Fill out the form below to recieve a free and confidential');
        $('.signup-section').find('form').attr('action', '/register');

        // Clear any existing error messages
        $(".error-message").remove();
        $(".success-message").remove();

        // Update form fields for register
        var formHtml = `
            <div class="sf-input-list">
                <input type="email" class="input-value" placeholder="Email Address" name="email" required>
                <input type="password" class="input-value" placeholder="Password" name="password" required>
                <input type="password" class="input-value" placeholder="Confirm Password" name="confirmPassword" required>
                <input type="text" class="input-value" placeholder="Full Name" name="fullName" required>
            </div>
            <div class="radio-check">
                <label for="rc-agree-dynamic">
                    <input type="checkbox" id="rc-agree-dynamic" name="termsAndConditions">
                    <span class="checkbox"></span>
                    I agree with the terms & conditions
                </label>
            </div>
            <button type="submit"><span>REGISTER NOW</span></button>
            <button type="button" class="button" id="go-to-login">
                <span>GO TO LOGIN</span>
            </button>
        `;
        $('.signup-section').find('form').html(formHtml);
    }

    // Interceptăm submit-ul pentru orice form din .signup-section
    $(document).on("submit", ".signup-section form", function (e) {
        e.preventDefault(); // Prevenim submit-ul normal

        var $form = $(this);
        var actionUrl = $form.attr("action"); // /register sau /login
        var formData = $form.serialize();

        // Clear previous messages
        $(".error-message").remove();
        $(".success-message").remove();

        if (actionUrl === '/login') {
            // Pentru login, facem AJAX request pentru autentificare
            $.ajax({
                url: '/login',
                type: 'POST',
                data: formData,
                success: function() {
                    // Login reușit - Spring Security va face redirect
                    // Dar pentru UX, afișăm mesaj și redirectăm manual
                    $(".signup-section").prepend(
                        `<div class="success-message">Login realizat cu succes!</div>`
                    );

                    setTimeout(function() {
                        window.location.href = '/index';
                    }, 1000);
                },
                error: function(xhr) {
                    // Login eșuat
                    if (xhr.status === 401 || xhr.status === 403) {
                        $(".signup-section").prepend(
                            `<div class="error-message">Datele introduse sunt greșite!</div>`
                        );
                    } else {
                        $(".signup-section").prepend(
                            `<div class="error-message">Datele introduse sunt greșite!</div>`
                        );
                    }
                }
            });
            return;
        }

        // Pentru register - păstrăm logica existentă
        if (actionUrl === '/register') {
            // Check terms checkbox
            var $termsCheckbox = $form.find('input[name="termsAndConditions"]');
            if (!$termsCheckbox.prop('checked')) {
                $(".signup-section").prepend(
                    `<div class="error-message">Acceptă termenii și condițiile, boss!</div>`
                );
                return;
            }

            $.ajax({
                url: actionUrl,
                type: "POST",
                data: formData,
                success: function (response) {
                    if (response.success) {
                        $(".signup-section").prepend(
                            `<div class="success-message">${response.success}</div>`
                        );

                        // Închide formularul după 1 secundă
                        setTimeout(function () {
                            $('.signup-section').fadeOut(400);
                        }, 1000);
                    }
                },
                error: function (xhr) {
                    var err = xhr.responseJSON?.error || "A crăpat ceva pe server!";
                    $(".signup-section").prepend(
                        `<div class="error-message">${err}</div>`
                    );
                },
            });
        }
    });

    // Initial binding for go-to-login button (if it exists on page load)
    $(document).on('click', '#go-to-login', function() {
        console.log('Go to login clicked');
        switchToLogin();
    });

    // Initial binding for go-to-register button (if it exists on page load)
    $(document).on('click', '#go-to-register', function() {
        console.log('Go to register clicked');
        switchToRegister();
    });

    // Custom checkbox functionality - make the checkbox square itself clickable
    $(document).on('click', '.radio-check label .checkbox', function(e) {
        e.preventDefault();
        e.stopPropagation();

        var $label = $(this).closest('label');
        var $checkbox = $label.find('input[type="checkbox"]');
        var isChecked = $checkbox.prop('checked');

        console.log('Checkbox clicked, current state:', isChecked);

        // Toggle checkbox state
        $checkbox.prop('checked', !isChecked);

        console.log('Checkbox new state:', $checkbox.prop('checked'));

        // Trigger change event for form validation
        $checkbox.trigger('change');
    });

    //logout
    $(document).on('click', '#logout-btn', function(e) {
        e.preventDefault(); // nu facem redirect automat

        fetch('/logout', {
            method: 'POST',
            headers: {
                // Dacă ai CSRF activ, altfel poți scoate această linie
                'X-CSRF-TOKEN': $('meta[name="_csrf"]').attr('content')
            }
        }).then(res => {
            if(res.ok) {
                // redirecționează direct la index
                window.location.href = '/index';
            } else {
                console.error('Ceva a crăpat la logout!');
            }
        }).catch(err => console.error(err));
    });

    // Also make the label clickable for better UX
    $(document).on('click', '.radio-check label', function(e) {
        // Only handle if not clicking on the checkbox span itself
        if (!$(e.target).hasClass('checkbox')) {
            e.preventDefault();
            var $checkbox = $(this).find('input[type="checkbox"]');
            var isChecked = $checkbox.prop('checked');

            console.log('Label clicked, current state:', isChecked);

            // Toggle checkbox state
            $checkbox.prop('checked', !isChecked);

            console.log('Checkbox new state:', $checkbox.prop('checked'));

            // Trigger change event for form validation
            $checkbox.trigger('change');
        }
    });

    // Debug checkbox state changes
    $(document).on('change', 'input[name="termsAndConditions"]', function() {
        console.log('Checkbox state changed via change event:', $(this).prop('checked'));
    });

    /*------------------
		Navigation
	--------------------*/
    $(".mobile-menu").slicknav({
        prependTo: '#mobile-menu-wrap',
        allowParentLinks: true
    });

    /*------------------
        Hero Slider
    --------------------*/
    var hero_s = $(".hero-slider");
    hero_s.owlCarousel({
        loop: true,
        margin: 0,
        items: 1,
        dots: true,
        animateOut: 'fadeOut',
        animateIn: 'fadeIn',
        smartSpeed: 1200,
        autoHeight: false,
        autoplay: false
    });

    /*------------------
        Trending Slider
    --------------------*/
    $(".trending-slider").owlCarousel({
        loop: true,
        margin: 0,
        items: 1,
        dots: false,
        nav: true,
        navText: ['<span class="arrow_carrot-left"></span>', '<span class="arrow_carrot-right"></span>'],
        dotsEach: 2,
        smartSpeed: 1200,
        autoHeight: false,
        autoplay: true
    });

    /*------------------------
        Latest Review Slider
    --------------------------*/
    $(".lp-slider").owlCarousel({
        loop: true,
        margin: 0,
        items: 4,
        dots: true,
        nav: true,
        navText: ['<span class="arrow_carrot-left"></span>', '<span class="arrow_carrot-right"></span>'],
        smartSpeed: 1200,
        autoHeight: false,
        dotsEach: 2,
        autoplay: true,
        responsive: {
            320: {
                items: 1
            },
            480: {
                items: 2
            },
            768: {
                items: 3
            },
            992: {
                items: 4
            }
        }
    });

    /*------------------------
        Update News Slider
    --------------------------*/
    $(".un-slider").owlCarousel({
        loop: true,
        margin: 0,
        items: 1,
        dots: false,
        nav: true,
        navText: ['<span class="arrow_carrot-left"></span>', '<span class="arrow_carrot-right"></span>'],
        smartSpeed: 1200,
        autoHeight: false,
        dotsEach: 2,
        autoplay: true
    });

    /*------------------------
        Video Guide Slider
    --------------------------*/
    $(".vg-slider").owlCarousel({
        loop: true,
        margin: 0,
        items: 1,
        dots: false,
        nav: true,
        navText: ['<span class="arrow_carrot-left"></span>', '<span class="arrow_carrot-right"></span>'],
        smartSpeed: 1200,
        autoHeight: false,
        autoplay: true
    });

    /*------------------------
        Gallery Slider
    --------------------------*/
    $(".dg-slider").owlCarousel({
        loop: true,
        margin: 0,
        items: 1,
        dots: false,
        nav: true,
        navText: ['<span class="arrow_carrot-left"></span>', '<span class="arrow_carrot-right"></span>'],
        smartSpeed: 1200,
        autoHeight: false,
        autoplay: true
    });

    /*------------------
        Video Popup
    --------------------*/
    $('.video-popup').magnificPopup({
        type: 'iframe'
    });

    /*------------------
        Barfiller
    --------------------*/
    $('#bar-1').barfiller({
        barColor: '#ffffff',
        duration: 2000
    });
    $('#bar-2').barfiller({
        barColor: '#ffffff',
        duration: 2000
    });
    $('#bar-3').barfiller({
        barColor: '#ffffff',
        duration: 2000
    });
    $('#bar-4').barfiller({
        barColor: '#ffffff',
        duration: 2000
    });
    $('#bar-5').barfiller({
        barColor: '#ffffff',
        duration: 2000
    });
    $('#bar-6').barfiller({
        barColor: '#ffffff',
        duration: 2000
    });

    /*------------------
        Circle Progress
    --------------------*/
    $('.circle-progress').each(function () {
        var cpvalue = $(this).data("cpvalue");
        var cpcolor = $(this).data("cpcolor");
        var cpid = $(this).data("cpid");

        $(this).append('<div class="' + cpid + '"></div><div class="progress-value"></div>');

        if (cpvalue < 100) {

            $('.' + cpid).circleProgress({
                value: '0.' + cpvalue,
                size: 40,
                thickness: 2,
                startAngle: -190,
                fill: cpcolor,
                emptyFill: "rgba(0, 0, 0, 0)"
            });
        } else {
            $('.' + cpid).circleProgress({
                value: 1,
                size: 40,
                thickness: 5,
                fill: cpcolor,
                emptyFill: "rgba(0, 0, 0, 0)"
            });
        }
    });

    $('.circle-progress-1').each(function () {
        var cpvalue = $(this).data("cpvalue");
        var cpcolor = $(this).data("cpcolor");
        var cpid = $(this).data("cpid");

        $(this).append('<div class="' + cpid + '"></div><div class="progress-value"></div>');

        if (cpvalue < 100) {

            $('.' + cpid).circleProgress({
                value: '0.' + cpvalue,
                size: 60,
                thickness: 2,
                startAngle: -190,
                fill: cpcolor,
                emptyFill: "rgba(0, 0, 0, 0)"
            });
        } else {
            $('.' + cpid).circleProgress({
                value: 1,
                size: 60,
                thickness: 5,
                fill: cpcolor,
                emptyFill: "rgba(0, 0, 0, 0)"
            });
        }
    });

    $('.circle-progress-2').each(function () {
        var cpvalue = $(this).data("cpvalue");
        var cpcolor = $(this).data("cpcolor");
        var cpid = $(this).data("cpid");

        $(this).append('<div class="' + cpid + '"></div><div class="progress-value"></div>');

        if (cpvalue < 100) {

            $('.' + cpid).circleProgress({
                value: '0.' + cpvalue,
                size: 200,
                thickness: 5,
                startAngle: -190,
                fill: cpcolor,
                emptyFill: "rgba(0, 0, 0, 0)"
            });
        } else {
            $('.' + cpid).circleProgress({
                value: 1,
                size: 200,
                thickness: 5,
                fill: cpcolor,
                emptyFill: "rgba(0, 0, 0, 0)"
            });
        }
    });

})(jQuery);
