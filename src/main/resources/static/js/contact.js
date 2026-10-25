// --- CONTACTO ---
document.getElementById('contactForm').addEventListener('submit', function (e) {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(this).entries());
    data.fecha = new Date().toLocaleString('es-PE');
    data.estado = 'Nuevo';
    const mensajes = JSON.parse(localStorage.getItem('dokyMensajes') || '[]');
    mensajes.unshift(data);
    localStorage.setItem('dokyMensajes', JSON.stringify(mensajes));
    this.reset();
    document.getElementById('contactSuccess').classList.remove('d-none');
    setTimeout(() => document.getElementById('contactSuccess').classList.add('d-none'), 4000);
});