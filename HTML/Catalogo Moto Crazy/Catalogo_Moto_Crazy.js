import { URL_SUPABASE, CLAVE_SUPABASE } from './config.js';

const TAMANO_PAGINA = 8;
let paginaActual  = 0;
let filtroActivo  = 'todas';
let todoCargado   = false;
let totalConteo   = 0;
let conteoActual  = 0;

async function obtenerDatosSupabase(ruta) {
  const respuesta = await fetch(`${URL_SUPABASE}/rest/v1/${ruta}`, {
    headers: {
      'apikey': CLAVE_SUPABASE,
      'Authorization': `Bearer ${CLAVE_SUPABASE}`,
      'Content-Type': 'application/json',
      'Prefer': 'count=exact'
    }
  });
  const conteo = respuesta.headers.get('content-range')?.split('/')[1] ?? null;
  const datos  = await respuesta.json();
  return { datos, conteo: conteo ? parseInt(conteo) : null };
}

function htmlTarjeta(elemento, indice) {
  const marca      = elemento.marcas?.nombre     ?? '—';
  const categoria  = elemento.categorias?.nombre ?? '—';
  const modelo     = elemento.nombre             ?? '—';
  const retraso    = (indice % TAMANO_PAGINA) * 50;

  const contenidoImagen = elemento.imagen_url
    ? `<img src="${elemento.imagen_url}" alt="${modelo}" loading="lazy" onerror="this.parentElement.innerHTML=svgSinImagen('${modelo}')">`
    : `<div class="imagen-vacia">
         <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M3 10l1.5-4.5A2 2 0 016.4 4h11.2a2 2 0 011.9 1.5L21 10M3 10h18M3 10v7a1 1 0 001 1h1m16-8v7a1 1 0 01-1 1h-1m-14 0h10"/></svg>
         <span>SIN IMAGEN</span>
       </div>`;

  return `
    <div class="tarjeta-moto" style="animation-delay:${retraso}ms">
      <div class="contenedor-imagen">
        ${contenidoImagen}
        <span class="insignia-categoria">${categoria}</span>
      </div>
      <div class="cuerpo-tarjeta">
        <div class="marca-tarjeta">${marca}</div>
        <div class="modelo-tarjeta">${modelo}</div>
        <div class="categoria-tarjeta">${categoria}</div>
      </div>
    </div>`;
}

function svgSinImagen(nombre) {
  return `<div class="imagen-vacia"><svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M3 10l1.5-4.5A2 2 0 016.4 4h11.2a2 2 0 011.9 1.5L21 10M3 10h18M3 10v7a1 1 0 001 1h1m16-8v7a1 1 0 01-1 1h-1m-14 0h10"/></svg><span>SIN IMAGEN</span></div>`;
}

async function cargarCategorias() {
  const { datos } = await obtenerDatosSupabase('categorias?select=nombre&order=nombre.asc');
  const barra = document.querySelector('.barra-filtros');
  datos.forEach(cat => {
    const boton = document.createElement('button');
    boton.className        = 'btn-filtro';
    boton.dataset.categoria = cat.nombre;
    boton.textContent      = cat.nombre;
    boton.onclick          = () => establecerFiltro(cat.nombre);
    barra.appendChild(boton);
  });
}

async function cargarProductos(reiniciar = false) {
  if (reiniciar) {
    paginaActual = 0;
    conteoActual = 0;
    todoCargado  = false;
    document.getElementById('cuadriculaCatalogo').innerHTML =
      Array(4).fill('<div class="tarjeta-esqueleto"><div class="imagen-esqueleto"></div><div class="cuerpo-esqueleto"><div class="linea-esqueleto ancho40"></div><div class="linea-esqueleto ancho60"></div></div></div>').join('');
    document.getElementById('contenedorCargarMas').style.display = 'none';
  }

  const boton = document.getElementById('botonCargarMas');
  if (boton) boton.disabled = true;

  const desde = paginaActual * TAMANO_PAGINA;
  let ruta;

  if (filtroActivo !== 'todas') {
    ruta = `modelos?select=id_modelo,nombre,imagen_url,marcas(nombre),categorias!inner(nombre)&categorias.nombre=eq.${encodeURIComponent(filtroActivo)}&order=id_modelo.asc&offset=${desde}&limit=${TAMANO_PAGINA}`;
  } else {
    ruta = `modelos?select=id_modelo,nombre,imagen_url,marcas(nombre),categorias(nombre)&order=id_modelo.asc&offset=${desde}&limit=${TAMANO_PAGINA}`;
  }

  const { datos, conteo } = await obtenerDatosSupabase(ruta);

  if (conteo !== null) totalConteo = conteo;

  const cuadricula = document.getElementById('cuadriculaCatalogo');

  if (reiniciar) cuadricula.innerHTML = '';

  if (datos.length === 0 && reiniciar) {
    cuadricula.innerHTML = `<div class="estado-vacio">
      <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
      <p>No hay productos en esta categoría.</p></div>`;
  } else {
    const indiceInicial = conteoActual;
    datos.forEach((elemento, i) => {
      cuadricula.insertAdjacentHTML('beforeend', htmlTarjeta(elemento, indiceInicial + i));
    });
    conteoActual += datos.length;
  }

  paginaActual++;
  todoCargado = conteoActual >= totalConteo;

  document.getElementById('contadorCabecera').textContent =
    `${conteoActual} de ${totalConteo} MOTOS`;

  const contenedor = document.getElementById('contenedorCargarMas');
  if (!todoCargado && totalConteo > TAMANO_PAGINA) {
    contenedor.style.display = 'flex';
    const informacion = document.getElementById('infoCargarMas');
    informacion.textContent = `Mostrando ${conteoActual} de ${totalConteo} productos`;
    if (boton) boton.disabled = false;
  } else {
    contenedor.style.display = conteoActual > TAMANO_PAGINA ? 'flex' : 'none';
    if (boton) {
      boton.disabled  = true;
      boton.innerHTML = '✓ TODOS LOS PRODUCTOS CARGADOS';
    }
    const informacion = document.getElementById('infoCargarMas');
    if (informacion) informacion.textContent = `${totalConteo} motos en total`;
  }
}

function establecerFiltro(categoria) {
  filtroActivo = categoria;
  document.querySelectorAll('.btn-filtro').forEach(boton => {
    boton.classList.toggle('activo', boton.dataset.categoria === categoria);
  });
  cargarProductos(true);
}

function cargarMas() {
  cargarProductos(false);
}

window.establecerFiltro = establecerFiltro;
window.cargarMas = cargarMas;

(async () => {
  await cargarCategorias();
  await cargarProductos(true);
})();
