/**
 * Namma Metro — Theme Toggle Script
 * Reads/writes preference to localStorage.
 * Toggles data-theme="light" on <body>.
 */
(function() {
    const STORAGE_KEY = 'namma-metro-theme';

    // Apply saved theme immediately (before paint)
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved === 'light') {
        document.body.setAttribute('data-theme', 'light');
    }

    // Create toggle button after DOM loads
    document.addEventListener('DOMContentLoaded', function() {
        const btn = document.createElement('button');
        btn.className = 'theme-toggle';
        btn.id = 'themeToggleBtn';
        btn.title = 'Toggle Dark/Light Mode';
        updateIcon(btn);
        btn.addEventListener('click', function() {
            const current = document.body.getAttribute('data-theme');
            if (current === 'light') {
                document.body.removeAttribute('data-theme');
                localStorage.setItem(STORAGE_KEY, 'dark');
            } else {
                document.body.setAttribute('data-theme', 'light');
                localStorage.setItem(STORAGE_KEY, 'light');
            }
            updateIcon(btn);
        });
        document.body.appendChild(btn);
    });

    function updateIcon(btn) {
        const isLight = document.body.getAttribute('data-theme') === 'light';
        btn.textContent = isLight ? '🌙' : '☀️';
    }
})();
