import { supabase }  from './supabase.js';
import { confirmar } from './confirmar.js';

let editandoProvId = null;

function mostrarMensaje(msg, tipo = 'success') {
  const toast = document.getElementById('toast');
  if (!toast) return;
  toast.textContent = msg;
  toast.className = `toast toast--${tipo} toast--visible`;
  clearTimeout(toast._timer);
  toast._timer = setTimeout(() => toast.classList.remove('toast--visible'), 3500);
}

function renderTablaProv(proveedores) {
  const tbody = document.getElementById('tbodyProveedores');
  if (!tbody) return;

  if (!proveedores.length) {
    tbody.innerHTML = `<tr><td colspan="6" class="tabla-empty">No hay proveedores registrados.</td></tr>`;
    return;
  }

  tbody.innerHTML = proveedores.map(p => `
    <tr class="${p.activo ? '' : 'fila-inactiva'}">
      <td>${p.nombre}</td>
      <td>${p.contacto ?? '<span class="muted">—</span>'}</td>
      <td>${p.telefono ?? '<span class="muted">—</span>'}</td>
      <td>${p.email ?? '<span class="muted">—</span>'}</td>
      <td><span class="badge ${p.activo ? 'badge--margen' : 'badge--inactivo'}">${p.activo ? 'Activo' : 'Inactivo'}</span></td>
      <td>
        <div class="botones">
          <button class="btn-edit btn-sm" data-id="${p.id}">Editar</button>
          <button class="btn-danger btn-sm btn-toggle-prov" data-id="${p.id}" data-activo="${p.activo}">
            ${p.activo ? 'Desactivar' : 'Activar'}
          </button>
          <button class="btn-eliminar-def btn-sm" data-id="${p.id}" data-nombre="${p.nombre}">🗑️ Eliminar</button>
        </div>
      </td>
    </tr>
  `).join('');

  tbody.querySelectorAll('.btn-edit').forEach(btn =>
    btn.addEventListener('click', () => cargarProvEditar(proveedores.find(p => p.id == btn.dataset.id)))
  );
  tbody.querySelectorAll('.btn-toggle-prov').forEach(btn =>
    btn.addEventListener('click', () => toggleProvActivo(Number(btn.dataset.id), btn.dataset.activo === 'true', btn.dataset.nombre))
  );
  tbody.querySelectorAll('.btn-eliminar-def').forEach(btn =>
    btn.addEventListener('click', () => eliminarProveedorDefinitivo(Number(btn.dataset.id), btn.dataset.nombre))
  );
}

export async function mostrarProveedores() {
  const tbody = document.getElementById('tbodyProveedores');
  if (!tbody) return;
  tbody.innerHTML = `<tr><td colspan="6" class="loading-text">Cargando…</td></tr>`;

  const { data, error } = await supabase
    .from('proveedores')
    .select('*')
    .order('nombre');

  if (error) {
    tbody.innerHTML = `<tr><td colspan="6" class="error-text">Error al cargar proveedores.</td></tr>`;
    console.error(error); return;
  }
  renderTablaProv(data ?? []);
}

export async function agregarProveedor(datos) {
  const { error } = await supabase.from('proveedores').insert(datos);
  if (error) { mostrarMensaje('Error al agregar proveedor.', 'error'); console.error(error); }
  else { mostrarMensaje('Proveedor agregado. ✅'); resetFormProv(); await mostrarProveedores(); }
}

export async function actualizarProveedor(id, datos) {
  const { error } = await supabase.from('proveedores').update(datos).eq('id', id);
  if (error) { mostrarMensaje('Error al actualizar proveedor.', 'error'); console.error(error); }
  else { mostrarMensaje('Proveedor actualizado. ✅'); resetFormProv(); await mostrarProveedores(); }
}

async function toggleProvActivo(id, activo, nombre) {
  const accion = activo ? 'desactivar' : 'activar';
  const ok = await confirmar({
    titulo:   `${accion.charAt(0).toUpperCase() + accion.slice(1)} proveedor`,
    mensaje:  `¿${accion.charAt(0).toUpperCase() + accion.slice(1)} a "${nombre}"?`,
    btnTexto: accion.charAt(0).toUpperCase() + accion.slice(1),
    tipo:     'warning',
  });
  if (!ok) return;
  const { error } = await supabase.from('proveedores').update({ activo: !activo }).eq('id', id);
  if (error) { mostrarMensaje(`Error al ${accion}.`, 'error'); console.error(error); }
  else { mostrarMensaje(`Proveedor ${activo ? 'desactivado' : 'activado'}. ✅`); await mostrarProveedores(); }
}

async function eliminarProveedorDefinitivo(id, nombre) {
  const ok = await confirmar({
    titulo:   'Eliminar proveedor definitivamente',
    mensaje:  `¿Eliminar "${nombre}" para siempre? Los productos asociados quedarán sin proveedor. Esta acción no se puede deshacer.`,
    btnTexto: '🗑️ Eliminar definitivamente',
    tipo:     'danger',
  });
  if (!ok) return;
  const { error } = await supabase.from('proveedores').delete().eq('id', id);
  if (error) { mostrarMensaje('Error al eliminar proveedor.', 'error'); console.error(error); }
  else { mostrarMensaje('Proveedor eliminado definitivamente. 🗑️'); await mostrarProveedores(); }
}

function cargarProvEditar(prov) {
  editandoProvId = prov.id;
  document.getElementById('provNombre').value   = prov.nombre;
  document.getElementById('provContacto').value = prov.contacto  ?? '';
  document.getElementById('provTelefono').value = prov.telefono  ?? '';
  document.getElementById('provEmail').value    = prov.email     ?? '';
  document.getElementById('provDireccion').value= prov.direccion ?? '';
  document.getElementById('provNotas').value    = prov.notas     ?? '';

  const btn = document.getElementById('btnGuardarProv');
  if (btn) { btn.textContent = '💾 Guardar cambios'; btn.classList.add('btn--editing'); }
  const cancelBtn = document.getElementById('btnCancelarProv');
  if (cancelBtn) cancelBtn.style.display = 'inline-block';
  document.getElementById('provNombre').focus();
}

export function resetFormProv() {
  editandoProvId = null;
  ['provNombre','provContacto','provTelefono','provEmail','provDireccion','provNotas'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = '';
  });
  const btn = document.getElementById('btnGuardarProv');
  if (btn) { btn.textContent = 'Agregar proveedor'; btn.classList.remove('btn--editing'); }
  const cancelBtn = document.getElementById('btnCancelarProv');
  if (cancelBtn) cancelBtn.style.display = 'none';
}

export { editandoProvId };

export function initProveedores() {
  const btnGuardar  = document.getElementById('btnGuardarProv');
  const btnCancelar = document.getElementById('btnCancelarProv');

  if (btnGuardar) {
    btnGuardar.addEventListener('click', async () => {
      const nombre    = document.getElementById('provNombre').value.trim();
      const contacto  = document.getElementById('provContacto').value.trim();
      const telefono  = document.getElementById('provTelefono').value.trim();
      const email     = document.getElementById('provEmail').value.trim();
      const direccion = document.getElementById('provDireccion').value.trim();
      const notas     = document.getElementById('provNotas').value.trim();

      if (!nombre) { alert('El nombre es obligatorio.'); return; }

      const datos = {
        nombre,
        contacto:  contacto  || null,
        telefono:  telefono  || null,
        email:     email     || null,
        direccion: direccion || null,
        notas:     notas     || null,
      };

      if (editandoProvId) {
        await actualizarProveedor(editandoProvId, datos);
      } else {
        await agregarProveedor(datos);
      }
    });
  }

  if (btnCancelar) {
    btnCancelar.addEventListener('click', resetFormProv);
  }

  mostrarProveedores();
}
