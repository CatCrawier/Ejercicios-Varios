import { supabase }  from './supabase.js';
import { confirmar } from './confirmar.js';

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
    tbody.innerHTML = `<tr><td colspan="4" class="tabla-empty">No hay categorías registradas.</td></tr>`;
    return;
  }

  tbody.innerHTML = categorias.map(c => `
    <tr>
      <td><span style="font-size:1.3rem">${c.emoji ?? '📦'}</span></td>
      <td>${c.nombre}</td>
      <td>${c.descripcion ?? '<span class="muted">—</span>'}</td>
      <td>
        <div class="botones">
          <button class="btn-edit btn-sm" data-id="${c.id}">Editar</button>
          <button class="btn-eliminar-def btn-sm" data-id="${c.id}" data-nombre="${c.nombre}">🗑️ Eliminar</button>
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
  const tbody = document.getElementById('tbodyCategorias');
  if (!tbody) return;
  tbody.innerHTML = `<tr><td colspan="4" class="loading-text">Cargando…</td></tr>`;

  const { data, error } = await supabase
    .from('categorias')
    .select('*')
    .order('nombre');

  if (error) {
    tbody.innerHTML = `<tr><td colspan="4" class="error-text">Error al cargar categorías.</td></tr>`;
    console.error(error); return;
  }
  renderTablaCategoria(data ?? []);
}

export async function agregarCategoria(datos) {
  const { error } = await supabase.from('categorias').insert(datos);
  if (error) { mostrarMensaje('Error al agregar categoría.', 'error'); console.error(error); }
  else { mostrarMensaje('Categoría agregada. ✅'); resetFormCat(); await mostrarCategorias(); }
}

export async function actualizarCategoria(id, datos) {
  const { error } = await supabase.from('categorias').update(datos).eq('id', id);
  if (error) { mostrarMensaje('Error al actualizar categoría.', 'error'); console.error(error); }
  else { mostrarMensaje('Categoría actualizada. ✅'); resetFormCat(); await mostrarCategorias(); }
}

async function eliminarCategoria(id, nombre) {
  const ok = await confirmar({
    titulo:   'Eliminar categoría definitivamente',
    mensaje:  `¿Eliminar "${nombre}"? Los productos asociados quedarán sin categoría. Esta acción no se puede deshacer.`,
    btnTexto: '🗑️ Eliminar definitivamente',
    tipo:     'danger',
  });
  if (!ok) return;
  const { error } = await supabase.from('categorias').delete().eq('id', id);
  if (error) { mostrarMensaje('Error al eliminar. ¿Tiene productos asociados?', 'error'); console.error(error); }
  else { mostrarMensaje('Categoría eliminada. 🗑️'); await mostrarCategorias(); }
}

function cargarCatEditar(cat) {
  editandoCatId = cat.id;
  document.getElementById('catNombre').value      = cat.nombre;
  document.getElementById('catDescripcion').value = cat.descripcion ?? '';
  document.getElementById('catEmoji').value       = cat.emoji ?? '';

  const btn = document.getElementById('btnGuardarCat');
  if (btn) { btn.textContent = '💾 Guardar cambios'; btn.classList.add('btn--editing'); }
  const cancelBtn = document.getElementById('btnCancelarCat');
  if (cancelBtn) cancelBtn.style.display = 'inline-block';
  document.getElementById('catNombre').focus();
}

export function resetFormCat() {
  editandoCatId = null;
  ['catNombre','catDescripcion','catEmoji'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = '';
  });
  const btn = document.getElementById('btnGuardarCat');
  if (btn) { btn.textContent = 'Agregar categoría'; btn.classList.remove('btn--editing'); }
  const cancelBtn = document.getElementById('btnCancelarCat');
  if (cancelBtn) cancelBtn.style.display = 'none';
}

export { editandoCatId };

export function initCategorias() {
  const btnGuardar  = document.getElementById('btnGuardarCat');
  const btnCancelar = document.getElementById('btnCancelarCat');

  if (btnGuardar) {
    btnGuardar.addEventListener('click', async () => {
      const nombre      = document.getElementById('catNombre').value.trim();
      const descripcion = document.getElementById('catDescripcion').value.trim();
      const emoji       = document.getElementById('catEmoji').value.trim();

      if (!nombre) { alert('El nombre es obligatorio.'); return; }

      const datos = { nombre, descripcion: descripcion || null, emoji: emoji || '📦' };

      if (editandoCatId) {
        await actualizarCategoria(editandoCatId, datos);
      } else {
        await agregarCategoria(datos);
      }
    });
  }

  if (btnCancelar) {
    btnCancelar.addEventListener('click', resetFormCat);
  }

  mostrarCategorias();
}
