(() => {
    const form = document.getElementById('personalizar');
    if (!form) return;
    const quantity = form.elements.namedItem('cantidad');
    function update() {
        const extra = [...form.querySelectorAll('[data-precio]:checked')]
            .reduce((sum, input) => sum + Number(input.dataset.precio), 0);
        const qty = Math.max(1, Math.min(Number(quantity.max) || 99, Number(quantity.value) || 1));
        document.getElementById('precio').textContent = ((Number(form.dataset.base) + extra) * qty).toFixed(2);
    }
    form.addEventListener('input', update);
    form.querySelectorAll('[data-quantity-step]').forEach(button => button.addEventListener('click', () => {
        quantity.value = Math.max(1, Math.min(Number(quantity.max) || 99,
            (Number(quantity.value) || 1) + Number(button.dataset.quantityStep)));
        update();
    }));
    update();
})();
