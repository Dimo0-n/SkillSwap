(function () {
    const TIMEZONE_SYNC_INTERVAL_MS = 60000;
    let syncInFlight = null;

    function detectBrowserTimeZone() {
        try {
            const detected = Intl.DateTimeFormat().resolvedOptions().timeZone;
            return typeof detected === 'string' && detected.trim() ? detected.trim() : null;
        } catch (_) {
            return null;
        }
    }

    async function syncUserTimeZone() {
        if (syncInFlight) {
            return syncInFlight;
        }

        const timeZoneId = detectBrowserTimeZone();
        if (!timeZoneId) {
            return Promise.resolve(false);
        }

        syncInFlight = fetch('/api/users/me/timezone', {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'same-origin',
            body: JSON.stringify({ timeZoneId: timeZoneId })
        })
            .then(function (response) {
                if (!response.ok) {
                    return false;
                }

                window.dispatchEvent(new CustomEvent('skillswap:timezone-synced', {
                    detail: { timeZoneId: timeZoneId }
                }));
                return true;
            })
            .catch(function () {
                return false;
            })
            .finally(function () {
                syncInFlight = null;
            });

        return syncInFlight;
    }

    function startUserTimeZoneSync() {
        syncUserTimeZone();

        window.setInterval(function () {
            syncUserTimeZone();
        }, TIMEZONE_SYNC_INTERVAL_MS);

        window.addEventListener('focus', syncUserTimeZone);
        document.addEventListener('visibilitychange', function () {
            if (!document.hidden) {
                syncUserTimeZone();
            }
        });
    }

    window.SkillSwapUserTimeZone = {
        detect: detectBrowserTimeZone,
        start: startUserTimeZoneSync,
        sync: syncUserTimeZone
    };
})();
