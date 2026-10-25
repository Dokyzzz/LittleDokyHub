(() => {
    const form = document.getElementById('checkoutForm');
    const entrega = document.getElementById('entrega');
    const direccion = document.getElementById('direccion');
    function update() {
        const delivery = entrega.value === 'DELIVERY', shipping = delivery ? 5 : 0;
        document.getElementById('addressFields').hidden = !delivery;
        direccion.required = delivery;
        direccion.minLength = delivery ? 5 : 0;
        document.getElementById('shippingCost').textContent = `S/ ${shipping.toFixed(2)}`;
        document.getElementById('finalTotal').textContent = `S/ ${(Number(form.dataset.subtotal) + shipping).toFixed(2)}`;
    }
    entrega.addEventListener('change', update);
    update();
})();
