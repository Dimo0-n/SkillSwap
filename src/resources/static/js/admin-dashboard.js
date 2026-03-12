(function () {
    const root = document.body;
    const themeToggle = document.getElementById('themeToggle');
    const modal = document.getElementById('confirmModal');
    const modalText = document.getElementById('confirmModalText');
    const modalSubmit = document.getElementById('confirmModalSubmit');
    let pendingFormId = null;

    function applySavedTheme() {
        const savedTheme = window.localStorage.getItem('skillswap-admin-theme');
        if (savedTheme === 'dark') {
            root.classList.add('admin-theme-dark');
        }
    }

    function toggleTheme() {
        root.classList.toggle('admin-theme-dark');
        const nextTheme = root.classList.contains('admin-theme-dark') ? 'dark' : 'light';
        window.localStorage.setItem('skillswap-admin-theme', nextTheme);
    }

    function openModal(message, formId) {
        if (!modal || !modalText) {
            submitForm(formId);
            return;
        }

        pendingFormId = formId;
        modalText.textContent = message || 'Are you sure?';
        modal.classList.add('is-open');
        modal.setAttribute('aria-hidden', 'false');
    }

    function closeModal() {
        if (!modal) {
            return;
        }

        pendingFormId = null;
        modal.classList.remove('is-open');
        modal.setAttribute('aria-hidden', 'true');
    }

    function submitForm(formId) {
        const form = document.getElementById(formId);
        if (form) {
            form.submit();
        }
    }

    function attachConfirmButtons() {
        document.querySelectorAll('[data-form-id]').forEach(function (button) {
            button.addEventListener('click', function () {
                openModal(button.getAttribute('data-confirm-message'), button.getAttribute('data-form-id'));
            });
        });

        document.querySelectorAll('[data-close-modal="true"]').forEach(function (button) {
            button.addEventListener('click', closeModal);
        });

        if (modalSubmit) {
            modalSubmit.addEventListener('click', function () {
                const formId = pendingFormId;
                closeModal();
                submitForm(formId);
            });
        }
    }

    function toLabels(points) {
        return (points || []).map(function (point) { return point.label; });
    }

    function toValues(points) {
        return (points || []).map(function (point) { return point.value; });
    }

    function renderChart(id, config) {
        const canvas = document.getElementById(id);
        if (!canvas || !window.Chart || !config || !config.points) {
            return;
        }

        const context = canvas.getContext('2d');
        const isDark = root.classList.contains('admin-theme-dark');
        const axisColor = isDark ? 'rgba(237, 244, 242, 0.16)' : 'rgba(25, 35, 33, 0.08)';
        const labelColor = isDark ? '#edf4f2' : '#192321';

        new Chart(context, {
            type: config.type,
            data: {
                labels: toLabels(config.points),
                datasets: [{
                    label: config.label,
                    data: toValues(config.points),
                    borderColor: config.borderColor,
                    backgroundColor: config.backgroundColor,
                    borderWidth: 2,
                    fill: config.fill !== false,
                    tension: 0.34,
                    borderRadius: config.type === 'bar' ? 12 : 0,
                    maxBarThickness: 34
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false,
                        labels: {
                            color: labelColor
                        }
                    }
                },
                scales: {
                    x: {
                        ticks: { color: labelColor },
                        grid: { color: axisColor }
                    },
                    y: {
                        beginAtZero: true,
                        ticks: { color: labelColor, precision: 0 },
                        grid: { color: axisColor }
                    }
                }
            }
        });
    }

    function renderCharts() {
        const charts = window.adminCharts || {};

        renderChart('dashboardUsersChart', {
            type: 'line',
            label: 'Users',
            points: charts.dashboardUsers,
            borderColor: '#0f766e',
            backgroundColor: 'rgba(15, 118, 110, 0.18)'
        });

        renderChart('dashboardSessionsChart', {
            type: 'bar',
            label: 'Sessions',
            points: charts.dashboardSessions,
            borderColor: '#b7791f',
            backgroundColor: 'rgba(183, 121, 31, 0.34)',
            fill: false
        });

        renderChart('dashboardSkillsChart', {
            type: 'bar',
            label: 'Skills',
            points: charts.dashboardSkills,
            borderColor: '#c2410c',
            backgroundColor: 'rgba(194, 65, 12, 0.28)',
            fill: false
        });

        renderChart('statisticsUsersChart', {
            type: 'line',
            label: 'User growth',
            points: charts.statisticsUsers,
            borderColor: '#0f766e',
            backgroundColor: 'rgba(15, 118, 110, 0.18)'
        });

        renderChart('statisticsActiveSessionsChart', {
            type: 'line',
            label: 'Active sessions',
            points: charts.statisticsActiveSessions,
            borderColor: '#2563eb',
            backgroundColor: 'rgba(37, 99, 235, 0.18)'
        });

        renderChart('statisticsCompletedSessionsChart', {
            type: 'line',
            label: 'Completed sessions',
            points: charts.statisticsCompletedSessions,
            borderColor: '#16a34a',
            backgroundColor: 'rgba(22, 163, 74, 0.18)'
        });

        renderChart('statisticsSkillsChart', {
            type: 'bar',
            label: 'Popular skills',
            points: charts.statisticsSkills,
            borderColor: '#c2410c',
            backgroundColor: 'rgba(194, 65, 12, 0.28)',
            fill: false
        });
    }

    applySavedTheme();
    attachConfirmButtons();
    renderCharts();

    if (themeToggle) {
        themeToggle.addEventListener('click', function () {
            toggleTheme();
            window.location.reload();
        });
    }
})();