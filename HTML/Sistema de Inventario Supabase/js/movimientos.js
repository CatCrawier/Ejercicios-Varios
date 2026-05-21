import { supabase }  from './supabase.js';
import { confirmar } from './confirmar.js';

function mostrarMensaje(msg, tipo = 'success') {
  const toast = document.getElementById('toast');
  if (!toast) return;
  toast.textContent = msg;
  toast.className = `toast toast--${tipo} toast--visible`;
  clearTimeout(toast._timer);
  toast._timer = setTimeout(() => toast.classList.remove('toast--visible'), 3500);
}

function formatFecha(iso) {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'short' });
}

function formatCantidad(tipo, cantidad) {
  const abs = Math.abs(cantidad);
  if (tipo === 'entrada') return `<span class="badge badge--margen">+${abs}</span>`;
  if (tipo === 'salida')  return `<span class="badge badge--danger">-${abs}</span>`;
  return `<span class="badge badge--min">${abs}</span>`;
}

async function cargarSelectProductosMov() {
  const sel = document.getElementById('movProductoId');
  if (!sel) return;

  const { data, error } = await supabase
    .from('productos')
    .select('id, nombre')
    .eq('activo', true)
    .order('nombre');

  if (error) { console.error(error); return; }

  sel.innerHTML = '<option value="">— Todos los productos —</option>';
  (data ?? []).forEach(p => {
    const o = document.createElement('option');
    o.value = p.id;
    o.textContent = p.nombre;
    sel.appendChild(o);
  });
}

function renderTablaMovimientos(movimientos) {
  const tbody = document.getElementById('tbodyMovimientos');
  if (!tbody) return;

  if (!movimientos.length) {
    tbody.innerHTML = `<tr><td colspan="7" class="tabla-empty">No hay movimientos registrados.</td></tr>`;
    return;
  }

  tbody.innerHTML = movimientos.map(m => `
    <tr>
      <td>${formatFecha(m.created_at)}</td>
      <td>${m.productos?.nombre ?? '<span class="muted">—</span>'}</td>
      <td><span class="badge ${m.tipo === 'entrada' ? 'badge--margen' : 'badge--danger'}">${m.tipo}</span></td>
      <td>${formatCantidad(m.tipo, m.cantidad)}</td>
      <td>${m.cantidad_anterior ?? '—'} → ${m.cantidad_nueva ?? '—'}</td>
      <td>${m.motivo ?? '<span class="muted">—</span>'}</td>
      <td>
        <button class="btn-danger btn-sm btn-del-mov" data-id="${m.id}">Eliminar</button>
      </td>
    </tr>
  `).join('');

  tbody.querySelectorAll('.btn-del-mov').forEach(btn =>
    btn.addEventListener('click', () => eliminarMovimiento(Number(btn.dataset.id)))
  );
}

export async function mostrarMovimientos(filtros = {}) {
  const tbody = document.getElementById('tbodyMovimientos');
  if (!tbody) return;
  tbody.innerHTML = `<tr><td colspan="7" class="loading-text">Cargando…</td></tr>`;

  let query = supabase
    .from('movimientos_inventario')
    .select('*, productos(id, nombre)')
    .order('created_at', { ascending: false })
    .limit(200);

  if (filtros.productoId) query = query.eq('producto_id', filtros.productoId);
  if (filtros.tipo)       query = query.eq('tipo', filtros.tipo);

  const { data, error } = await query;

  if (error) {
    tbody.innerHTML = `<tr><td colspan="7" class="error-text">Error al cargar movimientos.</td></tr>`;
    console.error(error); return;
  }
  renderTablaMovimientos(data ?? []);
}

async function agregarMovimientoManual() {
  const producto_id       = Number(document.getElementById('movProductoIdForm').value);
  const tipo              = document.getElementById('movTipo').value;
  const cantidad          = parseInt(document.getElementById('movCantidad').value, 10);
  const motivo            = document.getElementById('movMotivo').value.trim();

  if (!producto_id) { alert('Selecciona un producto.'); return; }
  if (!cantidad || cantidad <= 0) { alert('La cantidad debe ser mayor a 0.'); return; }

  const { data: prod, error: errProd } = await supabase
    .from('productos')
    .select('cantidad')
    .eq('id', producto_id)
    .single();

  if (errProd) { mostrarMensaje('Error al obtener producto.', 'error'); return; }

  const cantidadAnterior = prod.cantidad;
  const cantidadNueva    = tipo === 'entrada'
    ? cantidadAnterior + cantidad
    : Math.max(0, cantidadAnterior - cantidad);

  const { data: { session } } = await supabase.auth.getSession();

  const { error } = await supabase.from('movimientos_inventario').insert({
    producto_id,
    user_id: session?.user?.id ?? null,
    tipo,
    cantidad: tipo === 'salida' ? -cantidad : cantidad,
    cantidad_anterior: cantidadAnterior,
    cantidad_nueva:    cantidadNueva,
    motivo: motivo || (tipo === 'entrada' ? 'Entrada manual' : 'Salida manual'),
  });

  if (error) { mostrarMensaje('Error al registrar movimiento.', 'error'); console.error(error); return; }

  await supabase.from('productos').update({ cantidad: cantidadNueva }).eq('id', producto_id);

  mostrarMensaje(`Movimiento registrado. Stock: ${cantidadAnterior} → ${cantidadNueva} ✅`);
  resetFormMov();
  await mostrarMovimientos();
}

async function eliminarMovimiento(id) {
  const ok = await confirmar({
    titulo:   'Eliminar movimiento',
    mensaje:  '¿Eliminar este registro? El stock del producto NO se revertirá automáticamente.',
    btnTexto: '🗑️ Eliminar registro',
    tipo:     'danger',
  });
  if (!ok) return;
  const { error } = await supabase.from('movimientos_inventario').delete().eq('id', id);
  if (error) { mostrarMensaje('Error al eliminar movimiento.', 'error'); console.error(error); }
  else { mostrarMensaje('Movimiento eliminado. 🗑️'); await mostrarMovimientos(); }
}

function resetFormMov() {
  ['movProductoIdForm','movCantidad','movMotivo'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = '';
  });
  const sel = document.getElementById('movTipo');
  if (sel) sel.value = 'entrada';
}

export function initMovimientos() {
  cargarSelectProductosMov();
  mostrarMovimientos();

  const selForm = document.getElementById('movProductoIdForm');
  if (selForm) {
    supabase.from('productos').select('id, nombre').eq('activo', true).order('nombre').then(({ data }) => {
      selForm.innerHTML = '<option value="">— Selecciona producto —</option>';
      (data ?? []).forEach(p => {
        const o = document.createElement('option');
        o.value = p.id;
        o.textContent = p.nombre;
        selForm.appendChild(o);
      });
    });
  }

  document.getElementById('btnGuardarMov')?.addEventListener('click', agregarMovimientoManual);

  document.getElementById('filtroMovProducto')?.addEventListener('change', aplicarFiltrosMov);
  document.getElementById('filtroMovTipo')?.addEventListener('change', aplicarFiltrosMov);
}

function aplicarFiltrosMov() {
  const productoId = document.getElementById('filtroMovProducto')?.value;
  const tipo       = document.getElementById('filtroMovTipo')?.value;
  mostrarMovimientos({
    productoId: productoId ? Number(productoId) : null,
    tipo:       tipo || null,
  });
}
