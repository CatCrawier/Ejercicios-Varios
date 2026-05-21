// ============================================================
//  reset.js - Actualizacion local de contrasena
// ============================================================
import { signOut, updatePassword } from './jsonDB.js';
import { validarPassword } from './validaciones.js';

const RECOVERY_KEY = 'inventario_json_recovery_email';
const recoveryEmail = localStorage.getItem(RECOVERY_KEY);

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

if (!recoveryEmail) {
  document.getElementById('formReset').style.display = 'none';
  document.getElementById('mensajeInvalido').style.display = 'block';
}

const btnReset = document.getElementById('btnReset');

if (btnReset) {
  btnReset.addEventListener('click', () => {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    showError('');

    if (!newPassword || !confirmPassword) {
      showError('Completa ambos campos.');
      return;
    }
    const passwordError = validarPassword(newPassword);
    if (passwordError) {
      showError(passwordError);
      return;
    }
    if (newPassword !== confirmPassword) {
      showError('Las contrasenas no coinciden.');
      return;
    }

    setLoading(btnReset, true);

    try {
      updatePassword(recoveryEmail, newPassword);
      localStorage.removeItem(RECOVERY_KEY);
      signOut();
      document.getElementById('formReset').style.display = 'none';
      document.getElementById('mensajeExito').style.display = 'block';
    } catch (error) {
      showError(error.message);
    } finally {
      setLoading(btnReset, false);
    }
  });
}
