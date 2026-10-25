document.addEventListener('DOMContentLoaded', () => {
	['dokyUsuarios', 'dokyUsuarioActivo', 'dokyCarrito'].forEach(key => localStorage.removeItem(key));

	const modal = document.getElementById('modalCarrito');
	const itemsHost = document.getElementById('cartItemsContainer');
	if (!modal || !itemsHost) return;

	const money = amount => `S/ ${Number(amount || 0).toFixed(2)}`;
	const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, character => ({
		'&': '&amp;',
		'<': '&lt;',
		'>': '&gt;',
		'"': '&quot;',
		"'": '&#39;'
	}[character]));

	function readCart() {
		try {
			const cart = JSON.parse(localStorage.getItem('dokyOrder') || '[]');
			return Array.isArray(cart) ? cart : [];
		} catch (_) {
			return [];
		}
	}

	function renderCart() {
		const cart = readCart();
		let total = 0;

		if (cart.length === 0) {
			itemsHost.innerHTML = '<p class="cart-modal-empty">Tu carrito está vacío.</p>';
		} else {
			itemsHost.innerHTML = cart.map((item, index) => {
				const quantity = Math.max(1, Number(item.quantity) || 1);
				const unitPrice = Number(item.unitPrice ?? item.basePrice) || 0;
				const lineTotal = unitPrice * quantity;
				total += lineTotal;
				const details = [item.size, item.crust, item.sauce, ...(Array.isArray(item.toppings) ? item.toppings : [])]
					.filter(Boolean)
					.join(' · ');

				return `<article class="cart-modal-item">
					<img src="${escapeHtml(item.image || '/images/logo.png')}" alt="${escapeHtml(item.name || 'Producto')}" class="cart-modal-image">
					<div class="cart-modal-details">
						<strong>${escapeHtml(item.name || 'Producto')}</strong>
						<small>${escapeHtml(details || 'Personalizado')}</small>
						<div class="cart-modal-controls">
							<button type="button" data-cart-action="minus" data-cart-index="${index}" aria-label="Reducir cantidad">−</button>
							<span>${quantity}</span>
							<button type="button" data-cart-action="plus" data-cart-index="${index}" aria-label="Aumentar cantidad">+</button>
							<button type="button" class="cart-modal-remove" data-cart-action="remove" data-cart-index="${index}">Quitar</button>
						</div>
					</div>
					<strong class="cart-modal-price">${money(lineTotal)}</strong>
				</article>`;
			}).join('');
		}

		document.getElementById('cartSubtotal').textContent = money(total);
		document.getElementById('cartTotal').textContent = money(total);
		document.getElementById('cartItemCount').textContent = `${cart.reduce((sum, item) => sum + (Number(item.quantity) || 1), 0)} unidades`;
	}

	modal.addEventListener('show.bs.modal', renderCart);
	modal.addEventListener('click', event => {
		const button = event.target.closest('button[data-cart-action]');
		if (!button) return;

		const cart = readCart();
		const index = Number(button.dataset.cartIndex);
		const item = cart[index];
		if (!item) return;

		if (button.dataset.cartAction === 'remove') cart.splice(index, 1);
		if (button.dataset.cartAction === 'plus') item.quantity = Math.min(20, (Number(item.quantity) || 1) + 1);
		if (button.dataset.cartAction === 'minus') {
			item.quantity = Math.max(1, (Number(item.quantity) || 1) - 1);
		}

		item.subtotal = (Number(item.unitPrice ?? item.basePrice) || 0) * (Number(item.quantity) || 1);
		localStorage.setItem('dokyOrder', JSON.stringify(cart));
		renderCart();
	});

	renderCart();
});
