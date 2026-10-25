(() => {
  const money = n => `S/ ${Number(n).toFixed(2)}`;
  const readCart = () => { try { const v = JSON.parse(localStorage.getItem('dokyOrder') || '[]'); return Array.isArray(v) ? v : []; } catch (_) { return []; } };
  const save = cart => localStorage.setItem('dokyOrder', JSON.stringify(cart));
  const esc = value => String(value ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  function render() {
    const cart = readCart(), host = document.getElementById('orderItems');
    host.innerHTML = '';
    document.getElementById('emptyCart').classList.toggle('d-none', cart.length !== 0);
    document.getElementById('itemCount').textContent = `${cart.reduce((n, p) => n + Number(p.quantity || 1), 0)} unidades`;
    document.getElementById('continueBtn').disabled = cart.length === 0;
    let total = 0;
    cart.forEach((p, index) => {
      const qty = Number(p.quantity || 1), unit = Number(p.unitPrice ?? p.basePrice ?? 0), line = unit * qty; total += line;
      const toppings = Array.isArray(p.toppings) && p.toppings.length ? p.toppings.join(', ') : 'Sin extras';
      const image = p.image || '/images/logo.png';
      const article = document.createElement('article'); article.className = 'order-line';
      article.innerHTML = `<img class="order-image" src="${esc(image)}" alt="${esc(p.name)}"><div><h3 class="h6 fw-bold mb-1">${esc(p.name)}</h3><p class="small text-secondary mb-1">${esc(p.size || 'Personal')} · ${esc(p.crust || 'Tradicional')}</p><p class="small text-secondary mb-2">${esc(p.sauce || 'Salsa de tomate')} · ${esc(toppings)}</p><div class="d-flex align-items-center gap-2 qty-control"><button type="button" data-action="minus" data-index="${index}" aria-label="Reducir cantidad">−</button><span class="fw-semibold">${qty}</span><button type="button" data-action="plus" data-index="${index}" aria-label="Aumentar cantidad">+</button><button class="btn btn-link text-danger p-0 ms-2 small" type="button" data-action="remove" data-index="${index}"><i class="bi bi-trash me-1"></i>Quitar</button></div></div><div class="line-price text-end"><strong>${money(line)}</strong><small class="d-block text-secondary">${money(unit)} c/u</small></div>`;
      host.appendChild(article);
    });
    document.getElementById('subtotal').textContent = money(total);
    document.getElementById('grandTotal').textContent = money(total);
  }
  document.getElementById('orderItems').addEventListener('click', e => {
    const btn = e.target.closest('button[data-action]'); if (!btn) return;
    const cart = readCart(), i = Number(btn.dataset.index); if (!cart[i]) return;
    if (btn.dataset.action === 'remove') cart.splice(i, 1);
    if (btn.dataset.action === 'plus') cart[i].quantity = Math.min(20, Number(cart[i].quantity || 1) + 1);
    if (btn.dataset.action === 'minus') { cart[i].quantity = Math.max(1, Number(cart[i].quantity || 1) - 1); }
    cart.forEach(p => p.subtotal = Number(p.unitPrice ?? p.basePrice ?? 0) * Number(p.quantity || 1)); save(cart); render();
  });
  document.getElementById('continueBtn').addEventListener('click', () => {
    if (readCart().length) window.location.href = '/pedido/entrega';
  });
  render();
})();
