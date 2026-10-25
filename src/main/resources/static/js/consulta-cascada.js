document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('[data-cascade]').forEach(root => {
        const categoria = root.querySelector('[data-categoria]');
        const producto = root.querySelector('[data-producto]');
        const opcion = root.querySelector('[data-opcion]');
        const estado = root.querySelector('[data-estado]');
        const detalle = root.querySelector('[data-detalle]');
        const personalizar = root.querySelector('[data-personalizar]');
        const editar = root.querySelector('[data-editar]');
        const reintentar = root.querySelector('[data-reintentar]');
        const admin = root.dataset.admin === 'true';
        let productos = [], opciones = [], peticionProductos = 0, peticionOpciones = 0;
        let consultaFallida = null;
        const money = value => `S/ ${Number(value).toFixed(2)}`;

        function resetSelect(select, placeholder) {
            select.replaceChildren(new Option(placeholder, ''));
            select.disabled = true;
        }

        function cargarSelect(select, rows, label, placeholder) {
            resetSelect(select, placeholder);
            for (const row of rows) select.add(new Option(label(row), String(row.id)));
            select.disabled = rows.length === 0;
        }

        function mostrarEstado(message, error = false) {
            estado.textContent = message;
            estado.classList.toggle('text-danger', error);
            estado.classList.toggle('text-secondary', !error);
        }

        function limpiarAcciones() {
            detalle.textContent = '';
            if (personalizar) {
                personalizar.hidden = true;
                personalizar.removeAttribute('href');
            }
            if (editar) {
                editar.hidden = true;
                editar.removeAttribute('data-bs-target');
            }
            reintentar.hidden = true;
            consultaFallida = null;
        }

        function filtrarListado() {
            if (!admin) return;
            const rows = document.querySelectorAll('[data-option-row]');
            const ids = new Set(productos.map(p => String(p.id)));
            let visibles = 0;
            rows.forEach(row => {
                row.hidden = Boolean(categoria.value) && !ids.has(row.dataset.productoId)
                    || Boolean(producto.value) && row.dataset.productoId !== producto.value
                    || Boolean(opcion.value) && row.dataset.opcionId !== opcion.value;
                if (!row.hidden) visibles++;
            });
            const vacio = document.getElementById('sinOpcionesConsulta');
            if (vacio) vacio.hidden = visibles !== 0;
        }

        async function consultar(path) {
            const response = await fetch(`${root.dataset.base}${path}`, {
                headers: { Accept: 'application/json' }, cache: 'no-store'
            });
            if (!response.ok || !response.headers.get('content-type')?.includes('application/json'))
                throw new Error('No se pudo completar la consulta.');
            const rows = await response.json();
            if (!Array.isArray(rows)) throw new Error('Respuesta no válida.');
            return rows;
        }

        function mostrarProducto() {
            const seleccionado = productos.find(p => String(p.id) === producto.value);
            if (!seleccionado) return;
            detalle.textContent = `Precio base: ${money(seleccionado.precio)} · Stock: ${seleccionado.stock}.`;
            if (personalizar) {
                personalizar.href = `${root.dataset.personalizarBase}${encodeURIComponent(seleccionado.id)}`;
                personalizar.hidden = false;
            }
        }

        async function cambiarCategoria() {
            const peticion = ++peticionProductos;
            ++peticionOpciones; // invalida una consulta de opciones del padre anterior
            productos = [];
            opciones = [];
            limpiarAcciones();
            resetSelect(producto, categoria.value ? 'Cargando productos…' : 'Primero selecciona una categoría');
            resetSelect(opcion, 'Primero selecciona un producto');
            filtrarListado();
            if (!categoria.value) {
                mostrarEstado('Selecciona una categoría para comenzar.');
                return;
            }
            const categoriaId = categoria.value;
            mostrarEstado('Consultando productos…');
            try {
                const rows = await consultar(`/categorias/${encodeURIComponent(categoriaId)}/productos`);
                if (peticion !== peticionProductos) return; // ignora respuestas que llegan fuera de orden
                productos = rows;
                cargarSelect(producto, productos, p => `${p.nombre}${p.stock === 0 ? ' · Agotado' : ''}`,
                    rows.length ? 'Selecciona un producto' : 'Sin productos disponibles');
                mostrarEstado(rows.length ? 'Selecciona un producto para consultar sus opciones.' : 'Esta categoría no tiene productos disponibles.');
                filtrarListado();
            } catch (_) {
                if (peticion !== peticionProductos) return;
                resetSelect(producto, 'No se pudieron cargar los productos');
                mostrarEstado('No se pudieron consultar los productos. Reintenta la consulta.', true);
                consultaFallida = cambiarCategoria;
                reintentar.hidden = false;
            }
        }

        async function cambiarProducto() {
            const peticion = ++peticionOpciones;
            opciones = [];
            limpiarAcciones();
            resetSelect(opcion, producto.value ? 'Cargando opciones…' : 'Primero selecciona un producto');
            filtrarListado();
            if (!producto.value) {
                mostrarEstado('Selecciona un producto para consultar sus opciones.');
                return;
            }
            const productoId = producto.value, categoriaId = categoria.value;
            mostrarProducto();
            mostrarEstado('Consultando opciones…');
            try {
                const rows = await consultar(`/categorias/${encodeURIComponent(categoriaId)}/productos/${encodeURIComponent(productoId)}/opciones`);
                if (peticion !== peticionOpciones) return;
                opciones = rows;
                cargarSelect(opcion, opciones, o => `${o.grupo} · ${o.nombre} · + ${money(o.precio)}${o.activo ? '' : ' · Inactiva'}`,
                    rows.length ? 'Selecciona una opción' : 'Sin opciones registradas');
                mostrarEstado(rows.length ? 'Selecciona una opción para consultar su detalle.' : 'Este producto no tiene opciones disponibles.');
            } catch (_) {
                if (peticion !== peticionOpciones) return;
                resetSelect(opcion, 'No se pudieron cargar las opciones');
                mostrarEstado('No se pudieron consultar las opciones. Reintenta la consulta.', true);
                consultaFallida = cambiarProducto;
                reintentar.hidden = false;
            }
        }

        categoria.addEventListener('change', cambiarCategoria);
        producto.addEventListener('change', cambiarProducto);
        opcion.addEventListener('change', () => {
            if (editar) {
                editar.hidden = true;
                editar.removeAttribute('data-bs-target');
            }
            mostrarProducto();
            const seleccionada = opciones.find(o => String(o.id) === opcion.value);
            if (seleccionada) {
                detalle.textContent += ` ${seleccionada.grupo}: ${seleccionada.nombre} · Adicional: ${money(seleccionada.precio)}${seleccionada.activo ? '' : ' · Inactiva'}.`;
                mostrarEstado(admin ? 'El listado muestra la opción seleccionada.' : 'Continúa a personalización para configurar tu pedido.');
                if (editar) {
                    editar.dataset.bsTarget = `#editarOpcion${seleccionada.id}`;
                    editar.hidden = false;
                }
            } else {
                mostrarEstado(opciones.length ? 'Selecciona una opción para consultar su detalle.' : 'Este producto no tiene opciones disponibles.');
            }
            filtrarListado();
        });
        reintentar.addEventListener('click', () => consultaFallida?.());
    });
});
