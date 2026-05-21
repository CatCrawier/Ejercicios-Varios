import { supabase } from './supabase.js';

function setLoading(btn, loading) {
    btn.disabled = loading;
    btn.dataset.originalText = btn.dataset.originalText || btn.textContent;
    btn.textContent = loading ? 'Cargando...' : btn.dataset.originalText;
}

function showError(id, msg) {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = msg;
    el.style.display = msg ? 'block' : 'none';
}

const btnRegister = document.getElementById('btnRegister');

if (btnRegister) {
    btnRegister.addEventListener('click', async () => {
        const email    = document.getElementById('registerEmail').value.trim();
        const password = document.getElementById('registerPassword').value;

        showError('errorRegister', '');

        if (!email || !password) {
            showError('errorRegister', 'Completa todos los campos.');
            return;
        }
        if (password.length < 6) {
            showError('errorRegister', 'La contraseña debe tener al menos 6 caracteres.');
            return;
        }

        setLoading(btnRegister, true);

        const { error } = await supabase.auth.signUp({ email, password });

        setLoading(btnRegister, false);

        if (error) {
            showError('errorRegister', error.message);
        } else {
            alert('¡Registro exitoso! Revisa tu correo para confirmar la cuenta, luego inicia sesión.');
            document.getElementById('registerEmail').value    = '';
            document.getElementById('registerPassword').value = '';
        }
    });
}

const btnLogin = document.getElementById('btnLogin');

if (btnLogin) {
    btnLogin.addEventListener('click', async () => {
        const email    = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;

        showError('errorLogin', '');

        if (!email || !password) {
            showError('errorLogin', 'Ingresa tu correo y contraseña.');
            return;
        }

        setLoading(btnLogin, true);

        const { error } = await supabase.auth.signInWithPassword({ email, password });

        setLoading(btnLogin, false);

        if (error) {
            showError('errorLogin', 'Credenciales incorrectas. Intenta de nuevo.');
        } else {
            window.location.href = 'inventario.html';
        }
    });

    ['email', 'password'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener('keydown', e => { if (e.key === 'Enter') btnLogin.click(); });
    });
}

if (window.location.pathname.includes('inventario.html')) {
    supabase.auth.getSession().then(({ data: { session } }) => {
        if (!session) window.location.href = 'login.html';
    });
}

const btnLogout = document.getElementById('btnLogout');

if (btnLogout) {
    btnLogout.addEventListener('click', async () => {
        await supabase.auth.signOut();
        window.location.href = 'login.html';
    });
}
