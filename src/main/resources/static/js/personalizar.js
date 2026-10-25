(() => {
  const product = window.pizzaProduct;
  const money = n => `S/ ${Number(n).toFixed(2)}`;
  let quantity = 1;
  const selected = name => document.querySelector(`input[name="${name}"]:checked`);
  const calculate = () => {
    const size = selected('size');
    const crust = selected('crust');
    const toppings = [...document.querySelectorAll('.topping:checked')];
    const unit = Number(product.price) + Number(size?.dataset.extra || 0) + Number(crust?.dataset.extra || 0) + toppings.reduce((s, el) => s + Number(el.dataset.extra || 0), 0);
    document.getElementById('totalPrice').textContent = money(unit * quantity);
    document.getElementById('qtyLabel').textContent = quantity;
    return { unit, size: size?.value || 'Personal', crust: crust?.value || 'Tradicional', sauce: selected('sauce')?.value || 'Salsa de tomate', toppings: toppings.map(el => el.value) };
  };
  document.querySelectorAll('input').forEach(el => el.addEventListener('change', calculate));
  document.getElementById('minusQty').addEventListener('click', () => { quantity = Math.max(1, quantity - 1); calculate(); });
  document.getElementById('plusQty').addEventListener('click', () => { quantity = Math.min(20, quantity + 1); calculate(); });
  document.getElementById('addPizza').addEventListener('click', () => {
    const config = calculate();
    const item = { key: `${product.id}-${Date.now()}`, productId: product.id, name: product.name, image: product.image, basePrice: Number(product.price), size: config.size, crust: config.crust, sauce: config.sauce, toppings: config.toppings, unitPrice: config.unit, quantity, subtotal: config.unit * quantity };
    let cart = [];
    try { cart = JSON.parse(localStorage.getItem('dokyOrder') || '[]'); } catch (_) { cart = []; }
    cart.push(item);
    localStorage.setItem('dokyOrder', JSON.stringify(cart));
    window.location.href = '/pedido/resumen';
  });
  calculate();
})();
