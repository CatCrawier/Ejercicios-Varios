import { validarEmail } from './validaciones.js';

const RECOVERY_KEY = 'inventario_json_recovery_email';

function setLoading(btn, loading) {
  btn.disabled = loading;
  btn.dataset.originalText = btn.dataset.originalText || btn.textContent;
  btn.textContent = loading ? 'Preparando...' : btn.dataset.originalText;
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
  el.innerHTML = msg;
  el.style.display = msg ? 'block' : 'none';
}

const btnRecuperar = document.getElementById('btnRecuperar');

if (btnRecuperar) {
  btnRecuperar.addEventListener('click', () => {
    const email = document.getElementById('email').value.trim();

    showError('');
    showSuccess('');

    const emailError = validarEmail(email);
    if (emailError) {
      showError(emailError);
      return;
    }

    setLoading(btnRecuperar, true);
    localStorage.setItem(RECOVERY_KEY, email.toLowerCase());
    setLoading(btnRecuperar, false);

    showSuccess('Recuperacion local lista. Abre <a href="reset.html">restablecer contrasena</a> para guardar una nueva clave.');
  });

  document.getElementById('email')?.addEventListener('keydown', e => {
    if (e.key === 'Enter') btnRecuperar.click();
  });
}
