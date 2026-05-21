import { confirmar } from './confirmar.js';
import {
  addMovimiento,
  deleteMovimiento as dbDeleteMovimiento,
  getProducto,
  listMovimientos,
  listProductos,
  updateProducto,
} from './jsonDB.js';

function mostrarMensaje(msg, tipo = 'success') {
  const toast = document.getElementById('toast');
  if (!toast) return;
  toast.textContent = msg;
  toast.className = `toast toast--${tipo} toast--visible`;
  clearTimeout(toast._timer);
  toast._timer = setTimeout(() => toast.classList.remove('toast--visible'), 3500);
}

function formatFecha(iso) {
  if (!iso) return '-';
  return new Date(iso).toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'short' });
}

function formatCantidad(tipo, cantidad) {
  const abs = Math.abs(cantidad);
  if (tipo === 'entrada') return `<span class="badge badge--margen">+${abs}</span>`;
  if (tipo === 'salida') return `<span class="badge badge--danger">-${abs}</span>`;
  return `<span class="badge badge--min">${abs}</span>`;
}

function cargarOptionsProductos(select, placeholder) {
  if (!select) return;
  select.innerHTML = `<option value="">${placeholder}</option>`;
  listProductos({ activos: true, relaciones: false })
    .sort((a, b) => a.nombre.localeCompare(b.nombre))
    .forEach(p => {
      const option = document.createElement('option');
      option.value = p.id;
      option.textContent = p.nombre;
      select.appendChild(option);
    });
}

function renderTablaMovimientos(movimientos) {
  const tbody = document.getElementById('tbodyMovimientos');
  if (!tbody) return;

  if (!movimientos.length) {
    tbody.innerHTML = '<tr><td colspan="7" class="tabla-empty">No hay movimientos registrados.</td></tr>';
    return;
  }

  tbody.innerHTML = movimientos.map(m => `
    <tr>
      <td>${formatFecha(m.created_at)}</td>
      <td>${m.productos?.nombre ?? '<span class="muted">-</span>'}</td>
      <td><span class="badge ${m.tipo === 'entrada' ? 'badge--margen' : 'badge--danger'}">${m.tipo}</span></td>
      <td>${formatCantidad(m.tipo, m.cantidad)}</td>
      <td>${m.cantidad_anterior ?? '-'} -> ${m.cantidad_nueva ?? '-'}</td>
      <td>${m.motivo ?? '<span class="muted">-</span>'}</td>
      <td><button class="btn-danger btn-sm btn-del-mov" data-id="${m.id}">Eliminar</button></td>
    </tr>
  `).join('');

  tbody.querySelectorAll('.btn-del-mov').forEach(btn =>
    btn.addEventListener('click', () => eliminarMovimiento(Number(btn.dataset.id)))
  );
}

export async function mostrarMovimientos(filtros = {}) {
  const tbody = document.getElementById('tbodyMovimientos');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="7" class="loading-text">Cargando...</td></tr>';
  renderTablaMovimientos(listMovimientos({ ...filtros, limit: 200 }));
}

async function agregarMovimientoManual() {
  const producto_id = Number(document.getElementById('movProductoIdForm').value);
  const tipo = document.getElementById('movTipo').value;
  const cantidad = parseInt(document.getElementById('movCantidad').value, 10);
  const motivo = document.getElementById('movMotivo').value.trim();

  if (!producto_id) { alert('Selecciona un producto.'); return; }
  if (!cantidad || cantidad <= 0) { alert('La cantidad debe ser mayor a 0.'); return; }

  const prod = getProducto(producto_id);
  if (!prod) { mostrarMensaje('Error al obtener producto.', 'error'); return; }

  const cantidadAnterior = prod.cantidad;
  const cantidadNueva = tipo === 'entrada'
    ? cantidadAnterior + cantidad
    : Math.max(0, cantidadAnterior - cantidad);

  addMovimiento({
    producto_id,
    tipo,
    cantidad: tipo === 'salida' ? -cantidad : cantidad,
    cantidad_anterior: cantidadAnterior,
    cantidad_nueva: cantidadNueva,
    motivo: motivo || (tipo === 'entrada' ? 'Entrada manual' : 'Salida manual'),
  });

  updateProducto(producto_id, { cantidad: cantidadNueva });

  mostrarMensaje(`Movimiento registrado. Stock: ${cantidadAnterior} -> ${cantidadNueva}`);
  resetFormMov();
  await mostrarMovimientos();
}

async function eliminarMovimiento(id) {
  const ok = await confirmar({
    titulo: 'Eliminar movimiento',
    mensaje: 'Eliminar este registro? El stock del producto NO se revertira automaticamente.',
    btnTexto: 'Eliminar registro',
    tipo: 'danger',
  });
  if (!ok) return;

  try {
    dbDeleteMovimiento(id);
    mostrarMensaje('Movimiento eliminado.');
    await mostrarMovimientos();
  } catch (error) {
    mostrarMensaje('Error al eliminar movimiento.', 'error');
    console.error(error);
  }
}

function resetFormMov() {
  ['movProductoIdForm', 'movCantidad', 'movMotivo'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = '';
  });
  const sel = document.getElementById('movTipo');
  if (sel) sel.value = 'entrada';
}

export function initMovimientos() {
  cargarOptionsProductos(document.getElementById('movProductoId'), '- Todos los productos -');
  cargarOptionsProductos(document.getElementById('movProductoIdForm'), '- Selecciona producto -');
  mostrarMovimientos();

  document.getElementById('btnGuardarMov')?.addEventListener('click', agregarMovimientoManual);
  document.getElementById('filtroMovProducto')?.addEventListener('change', aplicarFiltrosMov);
  document.getElementById('filtroMovTipo')?.addEventListener('change', aplicarFiltrosMov);
}

function aplicarFiltrosMov() {
  const productoId = document.getElementById('filtroMovProducto')?.value;
  const tipo = document.getElementById('filtroMovTipo')?.value;
  mostrarMovimientos({
    productoId: productoId ? Number(productoId) : null,
    tipo: tipo || null,
  });
}
