import { supabase } from './supabase.js';

function setLoading(btn, loading) {
    btn.disabled = loading;
    btn.dataset.originalText = btn.dataset.originalText || btn.textContent;
    btn.textContent = loading ? 'Enviando...' : btn.dataset.originalText;
}

function showError(msg) {
    const el = document.getElementById('errorRecuperar');
    if (!el) return;
    el.textContent = msg;
    el.style.display = msg ? 'block' : 'none';
}

function showSuccess(msg) {
    const el = document.getElementById('successRecuperar');
    if (!el) return;
    el.textContent = msg;
    el.style.display = msg ? 'block' : 'none';
}

const btnRecuperar = document.getElementById('btnRecuperar');

if (btnRecuperar) {
    btnRecuperar.addEventListener('click', async () => {
        const email = document.getElementById('email').value.trim();

        showError('');
        showSuccess('');

        if (!email) {
            showError('Ingresa tu correo electrónico.');
            return;
        }

        setLoading(btnRecuperar, true);

        const { error } = await supabase.auth.resetPasswordForEmail(email, {
            redirectTo: 'http://localhost:5500/reset.html',
        });

        setLoading(btnRecuperar, false);

        if (error) {
            showError('Ocurrió un error. Verifica el correo e intenta de nuevo.');
            console.error(error);
        } else {

            showSuccess('Si ese correo está registrado, recibirás un enlace en unos minutos. Revisa también tu carpeta de spam.');
            btnRecuperar.disabled = true;
        }
    });

    document.getElementById('email')?.addEventListener('keydown', e => {
        if (e.key === 'Enter') btnRecuperar.click();
    });
}
