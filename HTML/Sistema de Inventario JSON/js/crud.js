import { confirmar } from './confirmar.js';
import {
  addMovimiento,
  addProducto as dbAddProducto,
  deleteProducto as dbDeleteProducto,
  getSession,
  listProductos,
  softDeleteProducto,
  updateProducto as dbUpdateProducto,
} from './jsonDB.js';

let editandoId = null;
let filtroCategoria = 'todas';
let _allProductos = [];

function formatPrecio(valor) {
  return Number(valor).toLocaleString('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  });
}

function mostrarMensaje(msg, tipo = 'success') {
  const toast = document.getElementById('toast');
  if (!toast) return;
  toast.textContent = msg;
  toast.className = `toast toast--${tipo} toast--visible`;
  clearTimeout(toast._timer);
  toast._timer = setTimeout(() => toast.classList.remove('toast--visible'), 3500);
}

async function registrarMovimiento(producto_id, cantidad_anterior, cantidad_nueva, motivo = 'Edicion manual') {
  const diff = cantidad_nueva - cantidad_anterior;
  if (diff === 0) return;

  addMovimiento({
    producto_id,
    tipo: diff > 0 ? 'entrada' : 'salida',
    cantidad: diff,
    cantidad_anterior,
    cantidad_nueva,
    motivo,
  });
}

function renderizarFiltros(productos) {
  const barra = document.getElementById('barraFiltros');
  if (!barra) return;

  const usadas = new Map();
  productos.forEach(p => { if (p.categorias) usadas.set(p.categoria_id, p.categorias); });

  barra.innerHTML = [
    '<button class="filtro-btn filtro--activo" data-id="todas">Todas</button>',
    ...[...usadas.entries()].map(([id, cat]) =>
      `<button class="filtro-btn" data-id="${id}">${cat.emoji ?? '📦'} ${cat.nombre}</button>`),
  ].join('');

  barra.querySelectorAll('.filtro-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      filtroCategoria = btn.dataset.id === 'todas' ? 'todas' : Number(btn.dataset.id);
      barra.querySelectorAll('.filtro-btn').forEach(b => b.classList.remove('filtro--activo'));
      btn.classList.add('filtro--activo');
      renderizarTarjetas(_allProductos);
    });
  });
}

function renderizarTarjetas(productos) {
  const contenedor = document.getElementById('contenedorProductos');
  if (!contenedor) return;

  const filtrados = filtroCategoria === 'todas'
    ? productos
    : productos.filter(p => p.categoria_id === filtroCategoria);

  if (filtrados.length === 0) {
    contenedor.innerHTML = `
      <div class="empty-state">
        <span class="empty-icon">🔍</span>
        <p>No hay productos en esta categoria.</p>
      </div>`;
    return;
  }

  contenedor.innerHTML = '';
  filtrados.forEach(p => {
    const catNombre = p.categorias?.nombre ?? 'Sin categoria';
    const catEmoji = p.categorias?.emoji ?? '📦';
    const provNombre = p.proveedores?.nombre ?? null;
    const stockBajo = p.cantidad <= p.stock_minimo;
    const margen = p.precio_costo ? (((p.precio - p.precio_costo) / p.precio) * 100).toFixed(1) : null;

    const imagenHTML = p.imagen
      ? `<img src="${p.imagen}" alt="${p.nombre}" onerror="this.style.display='none';this.nextElementSibling.style.display='flex'">`
      : '';
    const placeholderStyle = p.imagen ? 'display:none;' : '';

    const card = document.createElement('div');
    card.className = `producto${stockBajo ? ' producto--stock-bajo' : ''}`;
    card.innerHTML = `
      <div class="producto-img-wrap">
        ${imagenHTML}
        <div class="producto-placeholder" style="${placeholderStyle}">
          <span class="producto-placeholder-icon">${catEmoji}</span>
          <span class="producto-placeholder-text">Sin imagen</span>
        </div>
        <span class="producto-categoria-badge">${catEmoji} ${catNombre}</span>
        ${stockBajo ? '<span class="producto-stock-alerta">Stock bajo</span>' : ''}
      </div>
      <div class="producto-body">
        <h3>${p.nombre}</h3>
        ${p.sku ? `<p class="producto-sku">SKU: ${p.sku}</p>` : ''}
        <p class="producto-desc">${p.descripcion}</p>
        <div class="producto-meta">
          <span class="producto-price">${formatPrecio(p.precio)}</span>
          <span class="producto-stock ${stockBajo ? 'stock--bajo' : ''}">
            ${p.cantidad} ${p.unidad ?? 'unid.'}
          </span>
        </div>
        <div class="producto-extra">
          ${margen !== null ? `<span class="badge badge--margen">Margen: ${margen}%</span>` : ''}
          ${provNombre ? `<span class="badge badge--prov">${provNombre}</span>` : ''}
          ${p.stock_minimo > 0 ? `<span class="badge badge--min">Min: ${p.stock_minimo}</span>` : ''}
        </div>
        <div class="botones">
          <button class="btn-edit" data-id="${p.id}">Editar</button>
          <button class="btn-danger btn-desactivar" data-id="${p.id}">Desactivar</button>
          <button class="btn-eliminar-def" data-id="${p.id}" data-nombre="${p.nombre}">Eliminar</button>
        </div>
      </div>`;

    card.querySelector('.btn-edit').addEventListener('click', () => cargarParaEditar(p));
    card.querySelector('.btn-desactivar').addEventListener('click', () => desactivarProducto(p.id, p.nombre));
    card.querySelector('.btn-eliminar-def').addEventListener('click', () => eliminarProductoDefinitivo(p.id, p.nombre));
    contenedor.appendChild(card);
  });
}

export async function mostrarProductos() {
  const contenedor = document.getElementById('contenedorProductos');
  if (!contenedor) return;

  contenedor.innerHTML = '<p class="loading-text">Cargando productos...</p>';
  _allProductos = listProductos({ activos: true });

  if (_allProductos.length === 0) {
    const barra = document.getElementById('barraFiltros');
    if (barra) barra.innerHTML = '';
    contenedor.innerHTML = `
      <div class="empty-state">
        <span class="empty-icon">📭</span>
        <p>No hay productos registrados aun.<br>Agrega tu primer producto arriba.</p>
      </div>`;
    return;
  }

  renderizarFiltros(_allProductos);
  renderizarTarjetas(_allProductos);
}

export async function agregarProducto(producto) {
  if (!getSession()) {
    window.location.href = 'login.html';
    return;
  }

  try {
    const data = dbAddProducto(producto);
    await registrarMovimiento(data.id, 0, data.cantidad, 'Alta de producto');
    mostrarMensaje('Producto agregado correctamente.');
    await mostrarProductos();
  } catch (error) {
    mostrarMensaje('Error al agregar producto.', 'error');
    console.error(error);
  }
}

async function desactivarProducto(id, nombre) {
  const ok = await confirmar({
    titulo: 'Desactivar producto',
    mensaje: `"${nombre}" dejara de aparecer en el inventario, pero se conservara su historial de movimientos.`,
    btnTexto: 'Desactivar',
    tipo: 'warning',
  });
  if (!ok) return;

  try {
    softDeleteProducto(id);
    mostrarMensaje('Producto desactivado.');
    await mostrarProductos();
  } catch (error) {
    mostrarMensaje('Error al desactivar producto.', 'error');
    console.error(error);
  }
}

async function eliminarProductoDefinitivo(id, nombre) {
  const ok = await confirmar({
    titulo: 'Eliminar producto definitivamente',
    mensaje: `Eliminar "${nombre}" para siempre? Esta accion no se puede deshacer y borrara tambien su historial de movimientos.`,
    btnTexto: 'Eliminar definitivamente',
    tipo: 'danger',
  });
  if (!ok) return;

  try {
    dbDeleteProducto(id);
    mostrarMensaje('Producto eliminado definitivamente.');
    await mostrarProductos();
  } catch (error) {
    mostrarMensaje('Error al eliminar producto.', 'error');
    console.error(error);
  }
}

function cargarParaEditar(producto) {
  editandoId = producto.id;

  document.getElementById('nombre').value = producto.nombre;
  document.getElementById('descripcion').value = producto.descripcion;
  document.getElementById('sku').value = producto.sku ?? '';
  document.getElementById('precio').value = producto.precio;
  document.getElementById('precio_costo').value = producto.precio_costo ?? '';
  document.getElementById('cantidad').value = producto.cantidad;
  document.getElementById('stock_minimo').value = producto.stock_minimo ?? 0;
  document.getElementById('unidad').value = producto.unidad ?? 'unidad';
  document.getElementById('imagen').value = producto.imagen ?? '';

  const selCat = document.getElementById('categoria_id');
  const selProv = document.getElementById('proveedor_id');
  if (selCat) selCat.value = producto.categoria_id ?? '';
  if (selProv) selProv.value = producto.proveedor_id ?? '';

  const btnAgregar = document.getElementById('btnAgregar');
  if (btnAgregar) { btnAgregar.textContent = 'Guardar cambios'; btnAgregar.classList.add('btn--editing'); }

  const cancelBtn = document.getElementById('btnCancelarEdicion');
  if (cancelBtn) cancelBtn.style.display = 'inline-block';

  document.getElementById('nombre').focus();
  document.querySelector('.card').scrollIntoView({ behavior: 'smooth' });
}

export async function actualizarProducto(id, datos) {
  const productoAnterior = _allProductos.find(p => p.id === id);
  const cantidadAnterior = productoAnterior?.cantidad ?? datos.cantidad;

  try {
    dbUpdateProducto(id, datos);
    if (cantidadAnterior !== datos.cantidad) {
      await registrarMovimiento(id, cantidadAnterior, datos.cantidad, 'Edicion de producto');
    }
    mostrarMensaje('Producto actualizado correctamente.');
    resetFormulario();
    await mostrarProductos();
  } catch (error) {
    mostrarMensaje('Error al actualizar producto.', 'error');
    console.error(error);
  }
}

export function resetFormulario() {
  editandoId = null;
  ['nombre', 'descripcion', 'sku', 'precio', 'precio_costo', 'cantidad', 'stock_minimo', 'imagen'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = '';
  });

  const unidad = document.getElementById('unidad');
  if (unidad) unidad.value = 'unidad';

  const selCat = document.getElementById('categoria_id');
  const selProv = document.getElementById('proveedor_id');
  if (selCat) selCat.value = '';
  if (selProv) selProv.value = '';

  const btnAgregar = document.getElementById('btnAgregar');
  if (btnAgregar) { btnAgregar.textContent = 'Agregar producto'; btnAgregar.classList.remove('btn--editing'); }

  const cancelBtn = document.getElementById('btnCancelarEdicion');
  if (cancelBtn) cancelBtn.style.display = 'none';
}

export { editandoId };
