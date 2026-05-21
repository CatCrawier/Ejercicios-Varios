export function confirmar({ titulo, mensaje, btnTexto = 'Eliminar', tipo = 'danger' }) {
  return new Promise(resolve => {
    const overlay = document.getElementById('modalOverlay');
    const modalTitulo  = document.getElementById('modalTitulo');
    const modalMensaje = document.getElementById('modalMensaje');
    const btnConfirmar = document.getElementById('modalBtnConfirmar');
    const btnCancelar  = document.getElementById('modalBtnCancelar');

    if (!overlay) { resolve(false); return; }

    modalTitulo.textContent  = titulo;
    modalMensaje.textContent = mensaje;
    btnConfirmar.textContent = btnTexto;
    btnConfirmar.className   = `btn btn-modal-confirmar btn-modal--${tipo}`;

    overlay.classList.add('modal-overlay--visible');

    const cerrar = (resultado) => {
      overlay.classList.remove('modal-overlay--visible');
      btnConfirmar.replaceWith(btnConfirmar.cloneNode(true));
      btnCancelar.replaceWith(btnCancelar.cloneNode(true));
      resolve(resultado);
    };

    document.getElementById('modalBtnConfirmar').addEventListener('click', () => cerrar(true));
    document.getElementById('modalBtnCancelar') .addEventListener('click', () => cerrar(false));
    overlay.addEventListener('click', e => { if (e.target === overlay) cerrar(false); }, { once: true });
  });
}
