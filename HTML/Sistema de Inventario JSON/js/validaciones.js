// ============================================================
//  validaciones.js - Reglas de autenticacion
// ============================================================

const EMAIL_RE = /^[a-z0-9._%+-]+@[a-z0-9-]+(\.[a-z]{2,})+$/i;
const PASSWORD_MIN_LENGTH = 8;

export function validarEmail(email) {
  const value = email.trim();

  if (!value) return 'Ingresa un correo electronico.';
  if (!EMAIL_RE.test(value)) {
    return 'Usa un correo valido, por ejemplo usuario@gmail.com.';
  }

  return '';
}

export function validarPassword(password) {
  if (!password) return 'Ingresa una contrasena.';
  if (password.length < PASSWORD_MIN_LENGTH) {
    return `La contrasena debe tener al menos ${PASSWORD_MIN_LENGTH} caracteres.`;
  }
  if (!/[A-Z]/.test(password)) {
    return 'La contrasena debe incluir al menos una letra mayuscula.';
  }
  if (!/[a-z]/.test(password)) {
    return 'La contrasena debe incluir al menos una letra minuscula.';
  }
  if (!/[0-9]/.test(password)) {
    return 'La contrasena debe incluir al menos un numero.';
  }
  if (!/[^A-Za-z0-9]/.test(password)) {
    return 'La contrasena debe incluir al menos un signo especial.';
  }

  return '';
}
