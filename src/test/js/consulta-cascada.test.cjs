const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { JSDOM } = require('jsdom');
const rootDir = path.resolve(__dirname, '../../..');
const source = fs.readFileSync(path.join(rootDir, 'src/main/resources/static/js/consulta-cascada.js'), 'utf8');
const fragment = fs.readFileSync(path.join(rootDir, 'src/main/resources/templates/fragments/consulta-cascada.html'), 'utf8');
const tick = () => new Promise(resolve => setImmediate(resolve));
const products = [{ id: 1, nombre: 'Pizza', precio: 32, stock: 5 }, { id: 2, nombre: 'Pan', precio: 10, stock: 2 }];
const options = [{ id: 10, productoId: 1, grupo: 'EXTRA', nombre: 'Queso', precio: 4, activo: true }];

async function setup(admin = false) {
    const dom = new JSDOM(fragment, { url: 'https://example.test/doky/categorias', runScripts: 'outside-only' });
    await tick(); // dejar terminar el DOMContentLoaded propio de jsdom
    const document = dom.window.document, root = document.querySelector('[data-cascade]');
    root.dataset.base = admin ? '/doky/administracion/consultas' : '/doky/api/catalogo';
    root.dataset.admin = String(admin);
    root.dataset.personalizarBase = '/doky/personalizar/';
    root.querySelector(admin ? '[data-personalizar]' : '[data-editar]').remove();
    const categoria = root.querySelector('[data-categoria]');
    categoria.replaceChildren(...['', '1', '2', '3'].map(id => new dom.window.Option(id || 'Categoría', id)));
    if (admin) {
        document.body.insertAdjacentHTML('beforeend', '<table><tbody>' +
            '<tr data-option-row data-producto-id="1" data-opcion-id="10"><td>Queso</td></tr>' +
            '<tr data-option-row data-producto-id="1" data-opcion-id="11"><td>Masa</td></tr>' +
            '<tr data-option-row data-producto-id="2" data-opcion-id="12"><td>Salsa</td></tr>' +
            '<tr id="sinOpcionesConsulta" hidden><td>Vacío</td></tr></tbody></table>');
    }
    const requests = [];
    dom.window.fetch = (url, init) => new Promise((resolve, reject) => requests.push({
        url, init, reject,
        respond: (rows, ok = true, type = 'application/json') => resolve({ ok, headers: { get: () => type }, json: async () => rows })
    }));
    dom.window.eval(source);
    document.dispatchEvent(new dom.window.Event('DOMContentLoaded'));
    const producto = root.querySelector('[data-producto]'), opcion = root.querySelector('[data-opcion]');
    const change = (element, value) => {
        element.value = value;
        element.dispatchEvent(new dom.window.Event('change'));
    };
    return { dom, document, root, requests, categoria, producto, opcion, change };
}

async function loadProducts(ui, rows = products) {
    ui.change(ui.categoria, '1');
    ui.requests.at(-1).respond(rows);
    await tick();
}

async function loadOptions(ui, rows = options) {
    ui.change(ui.producto, '1');
    ui.requests.at(-1).respond(rows);
    await tick();
}

test('consulta real de cada nivel, precios y enlace con contexto de aplicación', async t => {
    const ui = await setup(); t.after(() => ui.dom.window.close());
    assert.equal(ui.producto.disabled, true);
    assert.equal(ui.opcion.disabled, true);
    assert.equal(ui.requests.length, 0);
    await loadProducts(ui);
    assert.equal(ui.requests[0].url, '/doky/api/catalogo/categorias/1/productos');
    assert.equal(ui.producto.disabled, false);
    await loadOptions(ui);
    assert.equal(ui.requests[1].url, '/doky/api/catalogo/categorias/1/productos/1/opciones');
    assert.equal(ui.opcion.disabled, false);
    ui.change(ui.opcion, '10');
    assert.match(ui.root.querySelector('[data-detalle]').textContent, /S\/ 32\.00.*S\/ 4\.00/);
    assert.equal(ui.root.querySelector('[data-personalizar]').getAttribute('href'), '/doky/personalizar/1');
    ui.change(ui.categoria, '2');
    assert.equal(ui.producto.value, '');
    assert.equal(ui.producto.disabled, true);
    assert.equal(ui.opcion.value, '');
    assert.equal(ui.opcion.options.length, 1);
    assert.equal(ui.root.querySelector('[data-personalizar]').hasAttribute('href'), false);
});

test('una respuesta tardía de otra categoría no sustituye la selección actual', async t => {
    const ui = await setup(); t.after(() => ui.dom.window.close());
    ui.change(ui.categoria, '1'); const previous = ui.requests.at(-1);
    ui.change(ui.categoria, '2'); const current = ui.requests.at(-1);
    current.respond([products[1]]); await tick();
    previous.respond([products[0]]); await tick();
    assert.equal(ui.producto.options.length, 2);
    assert.equal(ui.producto.options[1].value, '2');
});

test('cambiar de producto invalida opciones anteriores y permite volver al estado inicial', async t => {
    const ui = await setup(); t.after(() => ui.dom.window.close());
    await loadProducts(ui);
    ui.change(ui.producto, '1'); const previous = ui.requests.at(-1);
    ui.change(ui.producto, '2'); const current = ui.requests.at(-1);
    current.respond([{ ...options[0], id: 12, productoId: 2, nombre: 'Salsa' }]); await tick();
    previous.respond(options); await tick();
    assert.equal(ui.opcion.options[1].value, '12');
    ui.change(ui.categoria, '');
    assert.equal(ui.producto.disabled, true);
    assert.equal(ui.opcion.disabled, true);
    assert.equal(ui.root.querySelector('[data-detalle]').textContent, '');
});

test('errores de red o sesión muestran reintento sin mantener datos anteriores', async t => {
    const ui = await setup(); t.after(() => ui.dom.window.close());
    ui.change(ui.categoria, '1');
    ui.requests.at(-1).reject(new Error('Red no disponible')); await tick();
    const retry = ui.root.querySelector('[data-reintentar]');
    assert.equal(retry.hidden, false);
    assert.equal(ui.producto.disabled, true);
    retry.click(); ui.requests.at(-1).respond(products); await tick();
    assert.equal(retry.hidden, true);
    ui.change(ui.producto, '1');
    ui.requests.at(-1).respond([], true, 'text/html'); await tick();
    assert.equal(ui.opcion.disabled, true);
    assert.equal(retry.hidden, false);
    assert.match(ui.root.querySelector('[data-estado]').textContent, /No se pudieron consultar las opciones/);
    retry.click(); ui.requests.at(-1).respond(options); await tick();
    assert.equal(ui.opcion.disabled, false);
});

test('categorías y productos sin hijos muestran estado vacío y no bloquean personalización', async t => {
    const ui = await setup(); t.after(() => ui.dom.window.close());
    await loadProducts(ui, []);
    assert.equal(ui.producto.disabled, true);
    assert.equal(ui.opcion.disabled, true);
    assert.match(ui.root.querySelector('[data-estado]').textContent, /no tiene productos/);
    await loadProducts(ui);
    await loadOptions(ui, []);
    assert.equal(ui.opcion.disabled, true);
    assert.equal(ui.root.querySelector('[data-personalizar]').hidden, false);
    assert.match(ui.root.querySelector('[data-estado]').textContent, /no tiene opciones/);
});

test('administración filtra el listado, muestra inactivas y enlaza la edición correcta', async t => {
    const ui = await setup(true); t.after(() => ui.dom.window.close());
    await loadProducts(ui, [products[0]]);
    const rows = [...ui.document.querySelectorAll('[data-option-row]')];
    assert.equal(rows[2].hidden, true);
    await loadOptions(ui, [{ ...options[0], activo: false }, { ...options[0], id: 11 }]);
    assert.match(ui.opcion.options[1].textContent, /Inactiva/);
    ui.change(ui.opcion, '10');
    assert.deepEqual(rows.map(row => row.hidden), [false, true, true]);
    const edit = ui.root.querySelector('[data-editar]');
    assert.equal(edit.hidden, false);
    assert.equal(edit.dataset.bsTarget, '#editarOpcion10');
    ui.change(ui.categoria, '');
    assert.deepEqual(rows.map(row => row.hidden), [false, false, false]);
    assert.equal(edit.hidden, true);
    assert.equal(edit.hasAttribute('data-bs-target'), false);
});

test('nombres de la base se muestran como texto sin interpretar HTML', async t => {
    const ui = await setup(); t.after(() => ui.dom.window.close());
    await loadProducts(ui, [{ ...products[0], nombre: '<img src=x onerror=alert(1)>' }]);
    assert.match(ui.producto.options[1].textContent, /<img/);
    await loadOptions(ui, [{ ...options[0], nombre: '<script>alert(1)</script>' }]);
    ui.change(ui.opcion, '10');
    assert.equal(ui.root.querySelectorAll('script, img').length, 0);
    assert.match(ui.root.querySelector('[data-detalle]').textContent, /<script>alert\(1\)<\/script>/);
});
