// ============================================================
//  auth.js - Autenticacion con JSON local
// ============================================================
import { getSession, signIn, signOut, signUp } from './jsonDB.js';
import { validarEmail, validarPassword } from './validaciones.js';

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
  btnRegister.addEventListener('click', () => {
    const email = document.getElementById('registerEmail').value.trim();
    const password = document.getElementById('registerPassword').value;

    showError('errorRegister', '');

    const emailError = validarEmail(email);
    if (emailError) {
      showError('errorRegister', emailError);
      return;
    }

    const passwordError = validarPassword(password);
    if (passwordError) {
      showError('errorRegister', passwordError);
      return;
    }

    setLoading(btnRegister, true);

    try {
      signUp(email, password);
      alert('Registro exitoso. Ya puedes iniciar sesion.');
      document.getElementById('registerEmail').value = '';
      document.getElementById('registerPassword').value = '';
    } catch (error) {
      showError('errorRegister', error.message);
    } finally {
      setLoading(btnRegister, false);
    }
  });
}

const btnLogin = document.getElementById('btnLogin');

if (btnLogin) {
  btnLogin.addEventListener('click', () => {
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;

    showError('errorLogin', '');

    const emailError = validarEmail(email);
    if (emailError) {
      showError('errorLogin', emailError);
      return;
    }

    if (!password) {
      showError('errorLogin', 'Ingresa tu contrasena.');
      return;
    }

    setLoading(btnLogin, true);

    try {
      signIn(email, password);
      window.location.href = 'inventario.html';
    } catch {
      showError('errorLogin', 'Credenciales incorrectas. Intenta de nuevo.');
    } finally {
      setLoading(btnLogin, false);
    }
  });

  ['email', 'password'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.addEventListener('keydown', e => { if (e.key === 'Enter') btnLogin.click(); });
  });
}

if (window.location.pathname.includes('inventario.html')) {
  if (!getSession()) window.location.href = 'login.html';
}

const btnLogout = document.getElementById('btnLogout');

if (btnLogout) {
  btnLogout.addEventListener('click', () => {
    signOut();
    window.location.href = 'login.html';
  });
}
