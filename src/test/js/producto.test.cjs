const { test } = require('node:test');
const assert = require('node:assert/strict');
const { JSDOM } = require('jsdom');
const fs = require('node:fs');
const path = require('node:path');
const source = fs.readFileSync(path.resolve(__dirname, '../../main/resources/static/js/producto.js'), 'utf8');
function setup() {
    const dom = new JSDOM(`<form id="personalizar" data-base="32">
        <input type="radio" name="TAMANO" data-precio="0" checked>
        <input id="mediana" type="radio" name="TAMANO" data-precio="10">
        <input id="masa" type="checkbox" data-precio="7">
        <input id="extra" type="checkbox" data-precio="3.5">
        <input name="cantidad" type="number" min="1" max="3" value="1">
        <button type="button" data-quantity-step="-1">Menos</button>
        <button type="button" data-quantity-step="1">Más</button>
        <span id="precio"></span></form>`, { runScripts: 'outside-only' });
    dom.window.eval(source); return dom;
}
test('el total cambia con opciones y cantidad, sin sumar dos tamaños', t => {
    const dom = setup(); t.after(() => dom.window.close());
    const d = dom.window.document;
    assert.equal(d.getElementById('precio').textContent, '32.00');
    for (const id of ['mediana','masa','extra']) d.getElementById(id).checked = true;
    d.querySelector('[name=cantidad]').value = '2';
    d.querySelector('form').dispatchEvent(new dom.window.Event('input'));
    assert.equal(d.getElementById('precio').textContent, '105.00');
});
test('botones de cantidad respetan mínimo y stock máximo', t => {
    const dom = setup(); t.after(() => dom.window.close());
    const d = dom.window.document, qty = d.querySelector('[name=cantidad]');
    d.querySelector('[data-quantity-step="-1"]').click();
    assert.equal(qty.value, '1');
    for (let i = 0; i < 5; i++) d.querySelector('[data-quantity-step="1"]').click();
    assert.equal(qty.value, '3');
    assert.equal(d.getElementById('precio').textContent, '96.00');
});
