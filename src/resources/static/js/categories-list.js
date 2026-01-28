(function () {
    const itemsPerPage = 9;
    let currentPage = 1;
    let allItems = [];
    let filteredItems = [];
    let currentQuery = "";
    let pageConfig;

    // Detect layout (grid vs list) to adapt selectors and scroll target.
    const detectPageConfig = () => {
        // user-announces-list uses .cl-item
        if (document.querySelector('.cl-item')) {
            return {
                itemSelector: '.cl-item',
                hideTarget: (item) => item, // direct element
                scrollTargetSelector: '.categories-list-section'
            };
        }

        // default: announces-list grid cards
        return {
            itemSelector: '.cg-item',
            hideTarget: (item) => item.parentNode, // include grid column wrapper
            scrollTargetSelector: '.categories-grid-section'
        };
    };

    function init() {
        pageConfig = detectPageConfig();

        allItems = Array.from(document.querySelectorAll(pageConfig.itemSelector)).map(item => ({
            element: pageConfig.hideTarget(item),
            content: item.innerText.toLowerCase(),
            dataSkill: item.getAttribute('data-skill') || ''
        }));

        if (allItems.length === 0) {
            const paginationContainer = document.getElementById('pagination-container');
            if (paginationContainer) {
                paginationContainer.style.display = 'none';
            }
            return;
        }

        filteredItems = [...allItems];
        updateDisplay();
        setupEventListeners();
    }

    function setupEventListeners() {
        const searchInput = document.getElementById("skill-search");
        if (searchInput) {
            searchInput.addEventListener("keyup", function () {
                currentQuery = this.value.trim().toLowerCase();
                currentPage = 1;
                filterItems();
                updateDisplay();
            });
        }
    }

    function filterItems() {
        if (!currentQuery) {
            filteredItems = [...allItems];
        } else {
            filteredItems = allItems.filter(item =>
                item.content.includes(currentQuery) ||
                item.dataSkill.toLowerCase().includes(currentQuery)
            );
        }
    }

    function updateDisplay() {
        updateItemsDisplay();
        updatePagination();
        updateResultsInfo();
    }

    function updateItemsDisplay() {
        const startIndex = (currentPage - 1) * itemsPerPage;
        const endIndex = startIndex + itemsPerPage;

        allItems.forEach(item => {
            item.element.style.display = 'none';
        });

        filteredItems.slice(startIndex, endIndex).forEach(item => {
            // empty string keeps original display (block/flex)
            item.element.style.display = '';
        });
    }

    function updatePagination() {
        const totalPages = Math.ceil(filteredItems.length / itemsPerPage);
        const paginationContainer = document.getElementById('pagination-container');

        if (!paginationContainer) {
            return;
        }

        if (totalPages <= 1) {
            paginationContainer.style.display = 'none';
            return;
        }

        paginationContainer.style.display = 'flex';
        paginationContainer.innerHTML = '';

        if (currentPage > 1) {
            const prevBtn = createPaginationButton('Prev', currentPage - 1);
            paginationContainer.appendChild(prevBtn);
        }

        for (let i = 1; i <= totalPages; i++) {
            if (
                i === currentPage ||
                i === 1 ||
                i === totalPages ||
                (i >= currentPage - 1 && i <= currentPage + 1)
            ) {
                const pageBtn = createPaginationButton(i.toString(), i);
                if (i === currentPage) {
                    pageBtn.classList.add('active');
                }
                paginationContainer.appendChild(pageBtn);
            } else if (i === currentPage - 2 || i === currentPage + 2) {
                const dots = document.createElement('span');
                dots.textContent = '...';
                dots.className = 'pagination-dots';
                paginationContainer.appendChild(dots);
            }
        }

        if (currentPage < totalPages) {
            const nextBtn = createPaginationButton('Next', currentPage + 1);
            paginationContainer.appendChild(nextBtn);
        }
    }

    function createPaginationButton(text, page) {
        const button = document.createElement('a');
        button.href = '#';
        button.innerHTML = `<span>${text}</span>`;

        button.addEventListener('click', function (e) {
            e.preventDefault();
            currentPage = page;
            updateDisplay();

            const scrollTarget = document.querySelector(pageConfig.scrollTargetSelector);
            if (scrollTarget) {
                scrollTarget.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });

        return button;
    }

    function updateResultsInfo() {
        const totalResults = document.getElementById('total-results');
        const currentResults = document.getElementById('current-results');

        if (totalResults && currentResults) {
            const startIndex = (currentPage - 1) * itemsPerPage + 1;
            const endIndex = Math.min(currentPage * itemsPerPage, filteredItems.length);

            totalResults.textContent = filteredItems.length;

            if (filteredItems.length === 0) {
                currentResults.textContent = '0';
            } else {
                currentResults.textContent = `${startIndex}-${endIndex}`;
            }
        }
    }

    document.addEventListener('DOMContentLoaded', init);
})();
