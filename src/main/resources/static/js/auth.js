document.addEventListener('DOMContentLoaded', () => {
    // Elimina credenciales y sesiones de la implementación antigua.
    ['dokyUsuarios', 'dokyUsuarioActivo'].forEach(key => localStorage.removeItem(key));
    document.querySelectorAll('#togglePassword,.toggle-pass').forEach(button => button.addEventListener('click', () => {
        const input = document.getElementById(button.dataset.target || 'password');
        input.type = input.type === 'password' ? 'text' : 'password';
    }));
});
