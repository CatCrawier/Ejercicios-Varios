// ============================================================
//  app.js — Punto de entrada del inventario
// ============================================================
import { mostrarProductos, agregarProducto, actualizarProducto, resetFormulario } from './crud.js';
import { obtenerCategorias }  from './categorizar.js';
import { obtenerProveedores } from './proveedores.js';
import * as crud from './crud.js';

const btnAgregar         = document.getElementById('btnAgregar');
const btnCancelarEdicion = document.getElementById('btnCancelarEdicion');

// ─── Poblar selects ──────────────────────────────────────────
async function cargarSelects() {
  // Categorías
  const selCat = document.getElementById('categoria_id');
  const categorias = await obtenerCategorias();
  selCat.innerHTML = '<option value="">— Selecciona una categoría —</option>';
  categorias.forEach(cat => {
    const o = document.createElement('option');
    o.value = cat.id;
    o.textContent = `${cat.emoji} ${cat.nombre}`;
    selCat.appendChild(o);
  });

  // Proveedores
  const selProv = document.getElementById('proveedor_id');
  const proveedores = await obtenerProveedores();
  selProv.innerHTML = '<option value="">— Sin proveedor —</option>';
  proveedores.forEach(p => {
    const o = document.createElement('option');
    o.value = p.id;
    o.textContent = p.nombre;
    selProv.appendChild(o);
  });
}

// ─── Enviar formulario ───────────────────────────────────────
if (btnAgregar) {
  btnAgregar.addEventListener('click', async () => {
    const nombre        = document.getElementById('nombre').value.trim();
    const descripcion   = document.getElementById('descripcion').value.trim();
    const sku           = document.getElementById('sku').value.trim();
    const precio        = parseFloat(document.getElementById('precio').value);
    const precio_costo  = parseFloat(document.getElementById('precio_costo').value);
    const cantidad      = parseInt(document.getElementById('cantidad').value, 10);
    const stock_minimo  = parseInt(document.getElementById('stock_minimo').value, 10) || 0;
    const unidad        = document.getElementById('unidad').value;
    const imagen        = document.getElementById('imagen').value.trim();
    const categoria_id  = document.getElementById('categoria_id').value  ? Number(document.getElementById('categoria_id').value)  : null;
    const proveedor_id  = document.getElementById('proveedor_id').value  ? Number(document.getElementById('proveedor_id').value)  : null;

    // Validaciones
    if (!nombre || !descripcion || isNaN(precio) || isNaN(cantidad)) {
      alert('Completa todos los campos obligatorios.'); return;
    }
    if (precio < 0 || cantidad < 0 || stock_minimo < 0) {
      alert('Precio, cantidad y stock mínimo no pueden ser negativos.'); return;
    }
    if (!isNaN(precio_costo) && precio_costo > precio) {
      if (!confirm('El precio de costo es mayor al precio de venta. ¿Continuar?')) return;
    }
    if (!categoria_id) {
      alert('Selecciona una categoría.'); return;
    }

    const datos = {
      nombre, descripcion, precio,
      precio_costo: isNaN(precio_costo) ? null : precio_costo,
      cantidad, stock_minimo, unidad,
      imagen: imagen || null,
      categoria_id,
      proveedor_id,
      sku: sku || null,
    };

    if (crud.editandoId) {
      await actualizarProducto(crud.editandoId, datos);
    } else {
      await agregarProducto(datos);
    }
  });

  await cargarSelects();
  await mostrarProductos();
}

if (btnCancelarEdicion) {
  btnCancelarEdicion.addEventListener('click', resetFormulario);
}
