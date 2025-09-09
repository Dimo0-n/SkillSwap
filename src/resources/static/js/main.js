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

    // Search model
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

    // interceptăm submitul pentru orice form din .signup-section
    $(document).on("submit", ".signup-section form", function (e) {
        var $form = $(this);
        var actionUrl = $form.attr("action"); // /register sau /login

        if (actionUrl === '/login') {
            // Pentru login, lasă submit-ul să meargă normal
            // Spring Security va face redirect-ul la /index
            return;
        }

        // Dacă ajunge aici, e /register -> AJAX
        e.preventDefault(); // nu mai face reload

        // Check terms checkbox
        var $termsCheckbox = $form.find('input[name="termsAndConditions"]');
        if (!$termsCheckbox.prop('checked')) {
            $(".error-message").remove();
            $(".success-message").remove();
            $(".signup-section").prepend(
                `<div class="error-message">Acceptă termenii și condițiile, boss!</div>`
            );
            return;
        }

        var formData = $form.serialize();

        $.ajax({
            url: actionUrl,
            type: "POST",
            data: formData,
            success: function (response) {
                if (response.success) {
                    $(".error-message").remove();
                    $(".success-message").remove();

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
                $(".error-message").remove();
                var err = xhr.responseJSON?.error || "A crăpat ceva pe server!";
                $(".signup-section").prepend(
                    `<div class="error-message">${err}</div>`
                );
            },
        });
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