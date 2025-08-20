 (function () {
    const itemsPerPage = 9; // Câte anunțuri pe pagină
    let currentPage = 1;
    let allItems = [];
    let filteredItems = [];
    let currentQuery = "";

    // Initializare
    function init() {
    // Colectează toate itemurile
    allItems = Array.from(document.querySelectorAll('.cg-item')).map(item => ({
    element: item.parentNode, // col-lg-4 container
    content: item.innerText.toLowerCase(),
    dataSkill: item.getAttribute('data-skill') || ''
}));

    filteredItems = [...allItems];
    updateDisplay();
    setupEventListeners();
}

    // Event listeners
    function setupEventListeners() {
    const searchInput = document.getElementById("skill-search");
    if (searchInput) {
    searchInput.addEventListener("keyup", function () {
    currentQuery = this.value.trim().toLowerCase();
    currentPage = 1; // Reset la prima pagină
    filterItems();
    updateDisplay();
});
}
}

    // Filtrează itemurile
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

    // Actualizează afișarea
    function updateDisplay() {
    updateItemsDisplay();
    updatePagination();
    updateResultsInfo();
}

    // Actualizează afișarea itemurilor
    function updateItemsDisplay() {
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;

    // Ascunde toate itemurile
    allItems.forEach(item => {
    item.element.style.display = 'none';
});

    // Afișează doar itemurile pentru pagina curentă
    filteredItems.slice(startIndex, endIndex).forEach(item => {
    item.element.style.display = 'block';
});
}

    // Actualizează paginarea
    function updatePagination() {
    const totalPages = Math.ceil(filteredItems.length / itemsPerPage);
    const paginationContainer = document.getElementById('pagination-container');

    if (totalPages <= 1) {
    paginationContainer.style.display = 'none';
    return;
}

    paginationContainer.style.display = 'block';
    paginationContainer.innerHTML = '';

    // Buton Previous
    if (currentPage > 1) {
    const prevBtn = createPaginationButton('Prev', currentPage - 1);
    paginationContainer.appendChild(prevBtn);
}

    // Numerele paginilor
    for (let i = 1; i <= totalPages; i++) {
    if (i === currentPage ||
    i === 1 ||
    i === totalPages ||
    (i >= currentPage - 1 && i <= currentPage + 1)) {

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

    // Buton Next
    if (currentPage < totalPages) {
    const nextBtn = createPaginationButton('Next', currentPage + 1);
    paginationContainer.appendChild(nextBtn);
}
}

    // Creează buton de paginare
    function createPaginationButton(text, page) {
    const button = document.createElement('a');
    button.href = '#';
    button.innerHTML = `<span>${text}</span>`;

    button.addEventListener('click', function(e) {
    e.preventDefault();
    currentPage = page;
    updateDisplay();

    // Scroll la început
    document.querySelector('.categories-grid-section').scrollIntoView({
    behavior: 'smooth',
    block: 'start'
});
});

    return button;
}

    // Actualizează informațiile despre rezultate
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

    // Pornește aplicația
    document.addEventListener('DOMContentLoaded', init);
})();