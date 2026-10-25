function cargarMensajes() { 
    const mensajes = JSON.parse(localStorage.getItem('dokyMensajes') || '[]');
    document.getElementById('messageCount').textContent = mensajes.length;
    const sb = document.getElementById('sidebarMsgCount');
    if (sb) sb.textContent = mensajes.length;
    const tbody = document.getElementById('messagesTable');
    if (!mensajes.length) { 
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">No hay mensajes registrados.</td></tr>';
        return; 
    } 
    tbody.innerHTML = mensajes.map((m, i) => `
        <tr>
            <td class="fw-semibold">
                ${esc(m.nombre)}
            </td>
            <td>
                ${esc(m.email)}
            </td>
            <td>
                ${esc(m.asunto)}
            </td>
            <td style="min-width:220px">
                ${esc(m.mensaje)}
            </td>
            <td>
                ${esc(m.fecha)}
            </td>
            <td>
                <button class="btn btn-sm ${m.estado==='Nuevo'?'btn-outline-danger':'btn-outline-secondary'}" onclick="cambiarEstado(${i})">
                    ${esc(m.estado)}
                </button>
            </td>
            <td>
                <button class="btn btn-sm btn-outline-dark" onclick="verMensaje(${i})">
                    <i class="bi bi-eye"></i> Ver
                </button>
            </td>
        </tr>`).join(''); 
}

function cambiarEstado(i) { 
    const mensajes = JSON.parse(localStorage.getItem('dokyMensajes') || '[]'); 
    mensajes[i].estado = mensajes[i].estado === 'Nuevo' ? 'Atendido' : 'Nuevo'; 
    localStorage.setItem('dokyMensajes', JSON.stringify(mensajes)); 
    cargarMensajes(); 
}

function limpiarMensajes() {
    bootstrap.Modal.getOrCreateInstance(document.getElementById('modalLimpiarMensajes')).show();
}

document.getElementById('btnConfirmarLimpiar').addEventListener('click', () => {
    localStorage.removeItem('dokyMensajes');
    cargarMensajes();
    bootstrap.Modal.getInstance(document.getElementById('modalLimpiarMensajes')).hide();
});

let mensajeActual = null;

function verMensaje(i) {
    const mensajes = JSON.parse(localStorage.getItem('dokyMensajes') || '[]');
    const m = mensajes[i];
    if (!m) return;
    mensajeActual = i;
    document.getElementById('msgNombre').textContent = m.nombre;
    document.getElementById('msgEmail').textContent = m.email;
    document.getElementById('msgAsunto').textContent = m.asunto;
    document.getElementById('msgFecha').textContent = m.fecha;
    document.getElementById('msgTexto').textContent = m.mensaje;
    actualizarEstadoModal(m.estado);
    bootstrap.Modal.getOrCreateInstance(document.getElementById('modalMensaje')).show();
}

function actualizarEstadoModal(estado) {
    const badge = document.getElementById('msgEstado');
    badge.textContent = estado;
    badge.className = 'badge ' + (estado === 'Nuevo' ? 'text-bg-danger' : 'text-bg-secondary');
}

document.getElementById('btnCambiarEstadoModal').addEventListener('click', () => {
    if (mensajeActual === null) return;
    cambiarEstado(mensajeActual);
    const mensajes = JSON.parse(localStorage.getItem('dokyMensajes') || '[]');
    actualizarEstadoModal(mensajes[mensajeActual].estado);
});

function esc(v) { 
    return String(v ?? '').replace(/[&<>"']/g, c => ({ 
        '&': '&amp;', 
        '<': '&lt;', 
        '>': '&gt;', 
        '"': '&quot;', 
        "'": '&#039;' 
    }[c])); 
} 

cargarMensajes();

// ===================== CRUD de productos =====================

function nuevoProducto() {
    document.getElementById('modalProductoLabel').textContent = 'Nuevo producto';
    document.getElementById('prodId').value = '';
    document.getElementById('prodNombre').value = '';
    document.getElementById('prodDescripcion').value = '';
    document.getElementById('prodPrecio').value = '';
    document.getElementById('prodCategoria').value = 'clasicas';
    document.getElementById('prodImagen').value = '';
    bootstrap.Modal.getOrCreateInstance(document.getElementById('modalProducto')).show();
}

function editarProducto(btn) {
    const d = btn.dataset;
    document.getElementById('modalProductoLabel').textContent = 'Editar producto';
    document.getElementById('prodId').value = d.id;
    document.getElementById('prodNombre').value = d.nombre;
    document.getElementById('prodDescripcion').value = d.descripcion;
    document.getElementById('prodPrecio').value = d.precio;
    document.getElementById('prodCategoria').value = d.categoria;
    document.getElementById('prodImagen').value = d.imagen;
    bootstrap.Modal.getOrCreateInstance(document.getElementById('modalProducto')).show();
}

function confirmarEliminarProducto(btn) {
    const form = document.getElementById('formEliminarProducto');
    form.action = form.action.replace(/\/\d+\/eliminar$/, '/' + btn.dataset.id + '/eliminar');
    document.getElementById('elimProdNombre').textContent = btn.dataset.nombre;
    bootstrap.Modal.getOrCreateInstance(document.getElementById('modalEliminarProducto')).show();
}


// ===================== Sidebar: resaltar la sección visible =====================

(function () {
    const links = document.querySelectorAll('#adminSidebar .admin-menu a[href^="#"]');
    const secciones = Array.from(links)
        .map(a => document.querySelector(a.getAttribute('href')))
        .filter(Boolean);

    let pausaHasta = 0;

    // al hacer clic en el menú, se respeta el ítem elegido mientras dura el desplazamiento
    links.forEach(a => a.addEventListener('click', () => {
        pausaHasta = Date.now() + 1000;
        links.forEach(l => l.classList.toggle('active', l === a));
    }));

    function marcarActivo() {
        if (Date.now() < pausaHasta) return;
        let actual = secciones[0];
        secciones.forEach(s => {
            if (s.getBoundingClientRect().top <= window.innerHeight * 0.35) actual = s;
        });
        // al llegar al final de la página, se marca la última sección
        if (window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 4) {
            actual = secciones[secciones.length - 1];
        }
        links.forEach(a => a.classList.toggle('active', a.getAttribute('href') === '#' + actual.id));
    }

    window.addEventListener('scroll', marcarActivo, { passive: true });
    marcarActivo();
})();
