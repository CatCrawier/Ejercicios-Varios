import { supabase } from './supabase.js';

function showError(msg) {
    const el = document.getElementById('errorReset');
    if (!el) return;
    el.textContent = msg;
    el.style.display = msg ? 'block' : 'none';
}

function setLoading(btn, loading) {
    btn.disabled = loading;
    btn.dataset.originalText = btn.dataset.originalText || btn.textContent;
    btn.textContent = loading ? 'Guardando...' : btn.dataset.originalText;
}

supabase.auth.onAuthStateChange(async (event, session) => {
    if (event === 'PASSWORD_RECOVERY') {

        document.getElementById('formReset').style.display = 'block';
        document.getElementById('mensajeInvalido').style.display = 'none';
    }
});

setTimeout(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
        if (!session) {
            document.getElementById('formReset').style.display = 'none';
            document.getElementById('mensajeInvalido').style.display = 'block';
        }
    });
}, 1500);

const btnReset = document.getElementById('btnReset');

if (btnReset) {
    btnReset.addEventListener('click', async () => {
        const newPassword     = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        showError('');

        if (!newPassword || !confirmPassword) {
            showError('Completa ambos campos.');
            return;
        }
        if (newPassword.length < 6) {
            showError('La contraseña debe tener al menos 6 caracteres.');
            return;
        }
        if (newPassword !== confirmPassword) {
            showError('Las contraseñas no coinciden.');
            return;
        }

        setLoading(btnReset, true);

        const { error } = await supabase.auth.updateUser({ password: newPassword });

        setLoading(btnReset, false);

        if (error) {
            showError('Error al actualizar la contraseña. El enlace puede haber expirado.');
            console.error(error);
        } else {
            document.getElementById('formReset').style.display    = 'none';
            document.getElementById('mensajeExito').style.display = 'block';

            await supabase.auth.signOut();
        }
    });
}
