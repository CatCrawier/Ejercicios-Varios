// ============================================================
//  categorias.js - CRUD de categorias con JSON local
// ============================================================
import { confirmar } from './confirmar.js';
import { addCategoria, deleteCategoria, listCategorias, updateCategoria } from './jsonDB.js';

let editandoCatId = null;

function mostrarMensaje(msg, tipo = 'success') {
  const toast = document.getElementById('toast');
  if (!toast) return;
  toast.textContent = msg;
  toast.className = `toast toast--${tipo} toast--visible`;
  clearTimeout(toast._timer);
  toast._timer = setTimeout(() => toast.classList.remove('toast--visible'), 3500);
}

function renderTablaCategoria(categorias) {
  const tbody = document.getElementById('tbodyCategorias');
  if (!tbody) return;

  if (!categorias.length) {
    tbody.innerHTML = '<tr><td colspan="4" class="tabla-empty">No hay categorias registradas.</td></tr>';
    return;
  }

  tbody.innerHTML = categorias.map(c => `
    <tr>
      <td><span style="font-size:1.3rem">${c.emoji ?? '📦'}</span></td>
      <td>${c.nombre}</td>
      <td>${c.descripcion ?? '<span class="muted">-</span>'}</td>
      <td>
        <div class="botones">
          <button class="btn-edit btn-sm" data-id="${c.id}">Editar</button>
          <button class="btn-eliminar-def btn-sm" data-id="${c.id}" data-nombre="${c.nombre}">Eliminar</button>
        </div>
      </td>
    </tr>
  `).join('');

  tbody.querySelectorAll('.btn-edit').forEach(btn =>
    btn.addEventListener('click', () => cargarCatEditar(categorias.find(c => c.id == btn.dataset.id)))
  );
  tbody.querySelectorAll('.btn-eliminar-def').forEach(btn =>
    btn.addEventListener('click', () => eliminarCategoria(Number(btn.dataset.id), btn.dataset.nombre))
  );
}

export async function mostrarCategorias() {
  renderTablaCategoria(listCategorias());
}

export async function agregarCategoria(datos) {
  try {
    addCategoria(datos);
    mostrarMensaje('Categoria agregada.');
    resetFormCat();
    await mostrarCategorias();
  } catch (error) {
    mostrarMensaje('Error al agregar categoria.', 'error');
    console.error(error);
  }
}

export async function actualizarCategoria(id, datos) {
  try {
    updateCategoria(id, datos);
    mostrarMensaje('Categoria actualizada.');
    resetFormCat();
    await mostrarCategorias();
  } catch (error) {
    mostrarMensaje('Error al actualizar categoria.', 'error');
    console.error(error);
  }
}

async function eliminarCategoria(id, nombre) {
  const ok = await confirmar({
    titulo: 'Eliminar categoria definitivamente',
    mensaje: `Eliminar "${nombre}"? Los productos asociados quedaran sin categoria. Esta accion no se puede deshacer.`,
    btnTexto: 'Eliminar definitivamente',
    tipo: 'danger',
  });
  if (!ok) return;

  try {
    deleteCategoria(id);
    mostrarMensaje('Categoria eliminada.');
    await mostrarCategorias();
  } catch (error) {
    mostrarMensaje('Error al eliminar categoria.', 'error');
    console.error(error);
  }
}

function cargarCatEditar(cat) {
  editandoCatId = cat.id;
  document.getElementById('catNombre').value = cat.nombre;
  document.getElementById('catDescripcion').value = cat.descripcion ?? '';
  document.getElementById('catEmoji').value = cat.emoji ?? '';

  const btn = document.getElementById('btnGuardarCat');
  if (btn) { btn.textContent = 'Guardar cambios'; btn.classList.add('btn--editing'); }
  const cancelBtn = document.getElementById('btnCancelarCat');
  if (cancelBtn) cancelBtn.style.display = 'inline-block';
  document.getElementById('catNombre').focus();
}

export function resetFormCat() {
  editandoCatId = null;
  ['catNombre', 'catDescripcion', 'catEmoji'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = '';
  });
  const btn = document.getElementById('btnGuardarCat');
  if (btn) { btn.textContent = 'Agregar categoria'; btn.classList.remove('btn--editing'); }
  const cancelBtn = document.getElementById('btnCancelarCat');
  if (cancelBtn) cancelBtn.style.display = 'none';
}

export { editandoCatId };

export function initCategorias() {
  const btnGuardar = document.getElementById('btnGuardarCat');
  const btnCancelar = document.getElementById('btnCancelarCat');

  if (btnGuardar) {
    btnGuardar.addEventListener('click', async () => {
      const nombre = document.getElementById('catNombre').value.trim();
      const descripcion = document.getElementById('catDescripcion').value.trim();
      const emoji = document.getElementById('catEmoji').value.trim();

      if (!nombre) { alert('El nombre es obligatorio.'); return; }

      const datos = { nombre, descripcion: descripcion || null, emoji: emoji || '📦' };

      if (editandoCatId) await actualizarCategoria(editandoCatId, datos);
      else await agregarCategoria(datos);
    });
  }

  if (btnCancelar) btnCancelar.addEventListener('click', resetFormCat);

  mostrarCategorias();
}
