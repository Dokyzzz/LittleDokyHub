document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('modalCarrito');
    const host = document.getElementById('cartItemsContainer');
    if (!modal || !host) return;
    const money = amount => `S/ ${Number(amount).toFixed(2)}`;
    async function renderCart() {
        host.textContent = 'Cargando tu carrito…';
        try {
            const response = await fetch(modal.querySelector('[data-cart-url]').dataset.cartUrl, {
                headers: { Accept: 'application/json' }, cache: 'no-store'
            });
            if (!response.ok) throw new Error('No se pudo cargar el carrito.');
            const cart = await response.json();
            host.replaceChildren();
            if (!cart.lineas.length) host.textContent = 'Tu carrito está vacío.';
            for (const item of cart.lineas) {
                const article = document.createElement('article');
                article.className = 'cart-modal-item';
                const detail = document.createElement('div');
                const name = document.createElement('strong');
                name.textContent = item.nombre;
                const config = document.createElement('p');
                config.className = 'small text-secondary';
                config.textContent = item.configuracion;
                const subtotal = document.createElement('p');
                subtotal.textContent = `${item.cantidad} unidad(es) · ${money(item.subtotal)}`;
                detail.append(name, config, subtotal);
                article.append(detail);
                host.append(article);
            }
            document.getElementById('cartSubtotal').textContent = money(cart.total);
            document.getElementById('cartTotal').textContent = money(cart.total);
            document.getElementById('cartItemCount').textContent = `${cart.lineas.reduce((sum, item) => sum + item.cantidad, 0)} unidades`;
        } catch (_) {
            host.textContent = 'No se pudo cargar el carrito. Reintenta o abre el resumen.';
        }
    }
    modal.addEventListener('show.bs.modal', renderCart);
});
