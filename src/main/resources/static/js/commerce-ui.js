document.addEventListener('DOMContentLoaded', () => {
    const search = document.getElementById('buscarProducto');
    if (search) search.addEventListener('input', () => {
        const rows = [...document.querySelectorAll('[data-product-row]')];
        let visible = 0;
        rows.forEach(row => {
            const show = row.textContent.toLocaleLowerCase().includes(search.value.toLocaleLowerCase());
            row.hidden = !show;
            if (show) visible++;
        });
        const emptyState = document.getElementById('sinResultados');
        if (emptyState) emptyState.classList.toggle('d-none', rows.length === 0 || visible > 0);
    });

    const categorySearch = document.getElementById('buscarCategoria');
    if (categorySearch) categorySearch.addEventListener('input', () => {
        const rows = [...document.querySelectorAll('[data-category-row]')];
        const query = categorySearch.value.trim().toLocaleLowerCase();
        let visible = 0;
        rows.forEach(row => {
            const matches = row.textContent.toLocaleLowerCase().includes(query);
            row.hidden = !matches;
            if (matches) visible++;
        });
        const noResults = document.getElementById('sinCategoriasResultados');
        if (noResults) noResults.classList.toggle('d-none', rows.length === 0 || visible > 0);
    });

    if (location.pathname === '/administracion/gestion') {
        document.querySelectorAll('form[action*="/categoria/"][action*="/eliminar"]').forEach(form => {
            form.addEventListener('submit', event => {
                if (!confirm('¿Eliminar esta categoría? Solo se permite si no tiene productos asociados.'))
                    event.preventDefault();
            });
        });
    }
});
