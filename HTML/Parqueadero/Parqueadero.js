let datos = {};
let montoIngresado = 0;

const DENOMINACIONES = [50000, 20000, 10000, 5000, 2000, 1000, 500, 200, 100, 50];

const PICO_PLACA = {
  1: [1, 2],
  2: [1, 2],
  3: [2, 3],
  4: [2, 3],
  5: [3, 4],
  6: [3, 4],
  7: [4, 5],
  8: [4, 5],
  9: [5, 1],
  0: [5, 1],
};

function tienePicoPlaca(ultimoDigito) {
  const hoy = new Date().getDay();
  const diasRestriccion = PICO_PLACA[ultimoDigito] || [];
  return diasRestriccion.includes(hoy);
}

function formatearCOP(n) {
  return '$' + n.toLocaleString('es-CO');
}

function establecerPantalla(mensaje, tipo = '') {
  const elemento = document.getElementById('mensajePantalla');
  elemento.textContent = '► ' + mensaje;
  elemento.className = 'texto-pantalla' + (tipo ? ' ' + tipo : '');
}

function irA(num) {
  for (let i = 1; i <= 4; i++) {
    document.getElementById('seccion' + i).classList.toggle('activo', i === num);
    const pestana = document.getElementById('pestana' + i);
    pestana.classList.remove('activo', 'completado');
    if (i === num)     pestana.classList.add('activo');
    else if (i < num)  pestana.classList.add('completado');
  }

  if (num === 3) {
    montoIngresado = 0;
    actualizarInterfazPago();
    document.getElementById('errorPago').style.display = 'none';
  }
}

function calcular() {
  let valido = true;

  const tipo = document.getElementById('tipoVehiculo').value;
  document.getElementById('tipoVehiculo').classList.toggle('con-error', !tipo);
  document.getElementById('errorTipo').classList.toggle('visible', !tipo);
  if (!tipo) valido = false;

  const placa = document.getElementById('placa').value.trim().toUpperCase();
  const placaValida = /^[A-Z]{3}[0-9]{3}$/.test(placa);
  document.getElementById('placa').classList.toggle('con-error', !placaValida);
  document.getElementById('errorPlaca').classList.toggle('visible', !placaValida);
  if (!placaValida) valido = false;

  const ingreso = document.getElementById('horaIngreso').value;
  const ingresoValido = !!ingreso;
  document.getElementById('horaIngreso').classList.toggle('con-error', !ingresoValido);
  document.getElementById('errorIngreso').classList.toggle('visible', !ingresoValido);
  if (!ingresoValido) valido = false;

  const salida = document.getElementById('horaSalida').value;
  let salidaValida = !!salida;
  if (ingreso && salida && salida <= ingreso) salidaValida = false;
  document.getElementById('horaSalida').classList.toggle('con-error', !salidaValida);
  document.getElementById('errorSalida').classList.toggle('visible', !salidaValida);
  if (!salidaValida) valido = false;

  if (!valido) {
    establecerPantalla('ERROR DE VALIDACIÓN. REVISE LOS CAMPOS MARCADOS.', 'error');
    return;
  }

  const [hEntrada, mEntrada] = ingreso.split(':').map(Number);
  const [hSalida,  mSalida]  = salida.split(':').map(Number);
  const minutos = (hSalida * 60 + mSalida) - (hEntrada * 60 + mEntrada);

  const tarifa = tipo === 'auto' ? 110 : 80;
  const costoBase = minutos * tarifa;

  const ultimoDigito = parseInt(placa[placa.length - 1]);
  const aplicaDescuento = tipo === 'auto' && tienePicoPlaca(ultimoDigito);
  const descuento = aplicaDescuento ? Math.round(costoBase * 0.25) : 0;
  const costoConDescuento = costoBase - descuento;

  const resto = costoConDescuento % 50;
  const ajuste = resto === 0 ? 0 : 50 - resto;
  const totalFinal = costoConDescuento + ajuste;

  datos = {
    tipo, placa, ingreso, salida,
    minutos, tarifa, costoBase,
    aplicaDescuento, descuento, ultimoDigito,
    costoConDescuento, ajuste, totalFinal
  };

  mostrarResumen();
  irA(2);
  establecerPantalla('TOTAL A PAGAR: ' + formatearCOP(totalFinal), 'exito');
}

function mostrarResumen() {
  const d = datos;
  const horas = Math.floor(d.minutos / 60);
  const minutosRestantes = d.minutos % 60;
  const tiempoTexto = horas > 0
    ? horas + 'h ' + minutosRestantes + 'min (' + d.minutos + ' min)'
    : d.minutos + ' min';

  const fila = (etiqueta, valor, clase = '') =>
    '<div class="fila-resumen">' +
      '<span class="etiqueta-resumen">' + etiqueta + '</span>' +
      '<span class="valor-resumen ' + clase + '">' + valor + '</span>' +
    '</div>';

  let contenidoHtml = '';
  contenidoHtml += fila('VEHÍCULO',           d.tipo === 'auto' ? 'AUTOMÓVIL' : 'MOTOCICLETA');
  contenidoHtml += fila('PLACA',              d.placa);
  contenidoHtml += fila('INGRESO / SALIDA',   d.ingreso + ' → ' + d.salida);
  contenidoHtml += fila('TIEMPO ESTACIONADO', tiempoTexto);
  contenidoHtml += fila('TARIFA',             formatearCOP(d.tarifa) + '/min');
  contenidoHtml += fila('COSTO BASE',         formatearCOP(d.costoBase));

  if (d.aplicaDescuento) {
    contenidoHtml += fila(
      'PICO Y PLACA (dígito ' + d.ultimoDigito + ')',
      '<span class="insignia">APLICA HOY</span>'
    );
    contenidoHtml += fila('DESCUENTO 25%', '- ' + formatearCOP(d.descuento), 'desc');
    contenidoHtml += fila('SUBTOTAL',       formatearCOP(d.costoConDescuento));
  }

  if (d.ajuste > 0) {
    contenidoHtml += fila('AJUSTE (múlt. de $50)', '+ ' + formatearCOP(d.ajuste));
  }

  contenidoHtml += fila('TOTAL A PAGAR', formatearCOP(d.totalFinal), 'destacado');

  document.getElementById('cuadroResumen').innerHTML = contenidoHtml;
}

function construirDenominaciones() {
  const rejilla = document.getElementById('rejillaDenominaciones');
  rejilla.innerHTML = '';
  DENOMINACIONES.forEach(function(monto) {
    const boton = document.createElement('button');
    boton.className   = 'btn-denominacion';
    boton.textContent = formatearCOP(monto);
    boton.onclick     = function() { agregarPago(monto); };
    rejilla.appendChild(boton);
  });
}

function agregarPago(valor) {
  montoIngresado += valor;
  actualizarInterfazPago();
}

function reiniciarPago() {
  montoIngresado = 0;
  actualizarInterfazPago();
  document.getElementById('errorPago').style.display = 'none';
}

function actualizarInterfazPago() {
  document.getElementById('etiquetaMontoIngresado').textContent = formatearCOP(montoIngresado);
  document.getElementById('etiquetaTotalPagar').textContent     = formatearCOP(datos.totalFinal);

  const suficiente = montoIngresado >= datos.totalFinal;
  const boton = document.getElementById('botonPagar');
  boton.style.opacity = suficiente ? '1' : '0.4';
  boton.style.cursor  = suficiente ? 'pointer' : 'not-allowed';
}

function pagar() {
  if (montoIngresado < datos.totalFinal) {
    const errorElem = document.getElementById('errorPago');
    errorElem.textContent  = '⚠ Monto insuficiente. Faltan ' + formatearCOP(datos.totalFinal - montoIngresado) + '.';
    errorElem.style.display = 'block';
    return;
  }
  document.getElementById('errorPago').style.display = 'none';
  calcularCambio();
}

function calcularCambio() {
  const cambio = montoIngresado - datos.totalFinal;
  let restante  = cambio;
  const desglose = [];

  DENOMINACIONES.forEach(function(monto) {
    if (restante >= monto) {
      const cantidad = Math.floor(restante / monto);
      desglose.push({ monto: monto, cantidad: cantidad });
      restante -= cantidad * monto;
    }
  });

  const fila = (etiqueta, valor, colorTexto) =>
    '<div class="fila-cambio">' +
      '<span>' + etiqueta + '</span>' +
      '<span style="color:' + (colorTexto || 'var(--verde)') + '">' + valor + '</span>' +
    '</div>';

  let contenidoHtml = '<div class="titulo-cambio">✓ PAGO RECIBIDO</div>';
  contenidoHtml += fila('TOTAL PAGADO',      formatearCOP(montoIngresado),   'var(--acento2)');
  contenidoHtml += fila('VALOR SERVICIO',    formatearCOP(datos.totalFinal), 'var(--amarillo)');
  contenidoHtml += fila('CAMBIO A DEVOLVER', formatearCOP(cambio));

  if (cambio === 0) {
    contenidoHtml += '<div style="margin-top:12px; font-size:0.75rem; color:var(--verde); text-align:center; letter-spacing:2px;">✓ SIN CAMBIO</div>';
  } else {
    contenidoHtml += '<div style="font-size:0.63rem; color:var(--tenue); letter-spacing:1.5px; margin:12px 0 6px;">DESGLOSE DEL CAMBIO:</div>';
    desglose.forEach(function(elemento) {
      const tipoMoneda = elemento.monto >= 1000
        ? 'billete' + (elemento.cantidad > 1 ? 's' : '')
        : 'moneda'  + (elemento.cantidad > 1 ? 's' : '');
      contenidoHtml += fila(
        elemento.cantidad + ' ' + tipoMoneda + ' de ' + formatearCOP(elemento.monto),
        formatearCOP(elemento.monto * elemento.cantidad)
      );
    });
  }

  document.getElementById('cuadroCambio').innerHTML = contenidoHtml;
  irA(4);
  establecerPantalla('OPERACIÓN COMPLETADA. CAMBIO: ' + formatearCOP(cambio), 'exito');
}

function nuevaOperacion() {
  datos = {};
  montoIngresado = 0;

  document.getElementById('tipoVehiculo').value = '';
  document.getElementById('placa').value        = '';
  document.getElementById('horaIngreso').value  = '';
  document.getElementById('horaSalida').value   = '';

  ['tipoVehiculo', 'placa', 'horaIngreso', 'horaSalida'].forEach(function(idCampo) {
    document.getElementById(idCampo).classList.remove('con-error');
  });
  ['errorTipo', 'errorPlaca', 'errorIngreso', 'errorSalida'].forEach(function(idCampo) {
    document.getElementById(idCampo).classList.remove('visible');
  });

  irA(1);
  establecerPantalla('BIENVENIDO. INGRESE LOS DATOS DEL VEHÍCULO.');
}

construirDenominaciones();
