function procesarNumero(valor) {
  const entrada = document.getElementById('entradaNumero');
  const errorElemento = document.getElementById('errorNumero');
  const cajas = document.querySelectorAll('#vistaDigitos .caja-digito');

  const limpio = valor.replace(/\D/g, '');
  if (valor !== limpio) {
    entrada.value = limpio;
    valor = limpio;
  }

  cajas.forEach(caja => {
    caja.className = 'caja-digito';
    caja.childNodes[0].textContent = '—';
  });

  const idsResultados = ['r-suma', 'r-multiplicacion', 'r-resta', 'r-potencia'];
  const idsValores = ['v-suma', 'v-multiplicacion', 'v-resta', 'v-potencia'];

  if (valor.length === 0) {
    errorElemento.textContent = '';
    entrada.classList.remove('error');
    idsResultados.forEach(id => document.getElementById(id).classList.remove('activo'));
    idsValores.forEach(id => document.getElementById(id).textContent = '—');
    document.getElementById('f-suma').textContent = 'd4 + d5';
    document.getElementById('f-multiplicacion').textContent = 'd1 × d8';
    document.getElementById('f-resta').textContent = 'd7 − d2';
    document.getElementById('f-potencia').textContent = 'd3 ^ d6';
    return;
  }

  if (valor.length < 8) {
    errorElemento.textContent = `Faltan ${8 - valor.length} dígito(s)`;
    entrada.classList.add('error');
    valor.split('').forEach((digito, indice) => {
      cajas[indice].childNodes[0].textContent = digito;
    });
    idsResultados.forEach(id => document.getElementById(id).classList.remove('activo'));
    return;
  }

  errorElemento.textContent = '';
  entrada.classList.remove('error');

  const digitos = valor.split('').map(Number);
  const clases = ['extremo', 'd2', 'd3', 'centro', 'centro', 'd6', 'd7', 'extremo'];

  digitos.forEach((digito, indice) => {
    cajas[indice].className = 'caja-digito ' + clases[indice] + ' activo';
    cajas[indice].childNodes[0].textContent = digito;
  });

  const d = digitos;
  const suma = d[3] + d[4];
  document.getElementById('v-suma').textContent = suma;
  document.getElementById('f-suma').textContent = `${d[3]} + ${d[4]}`;
  document.getElementById('r-suma').classList.add('activo');

  const multiplicacion = d[0] * d[7];
  document.getElementById('v-multiplicacion').textContent = multiplicacion;
  document.getElementById('f-multiplicacion').textContent = `${d[0]} × ${d[7]}`;
  document.getElementById('r-multiplicacion').classList.add('activo');

  const resta = d[6] - d[1];
  document.getElementById('v-resta').textContent = resta;
  document.getElementById('f-resta').textContent = `${d[6]} − ${d[1]}`;
  document.getElementById('r-resta').classList.add('activo');

  const potencia = Math.pow(d[2], d[5]);
  document.getElementById('v-potencia').textContent = potencia;
  document.getElementById('f-potencia').textContent = `${d[2]} ^ ${d[5]}`;
  document.getElementById('r-potencia').classList.add('activo');
}

let _bloqueoTemperatura = false;

function formatearNumero(numero) {
  return parseFloat(numero.toFixed(4));
}

function convertirTemperatura(desde, valorCrudo) {
  if (_bloqueoTemperatura) return;
  _bloqueoTemperatura = true;

  const valor = parseFloat(valorCrudo);
  const esValido = !isNaN(valor);

  let celsius;
  if (desde === 'celsius') celsius = valor;
  else if (desde === 'kelvin') celsius = valor - 273.15;
  else celsius = ((valor - 32) * 5) / 9;

  const kelvin = celsius + 273.15;
  const fahrenheit = (celsius * 9) / 5 + 32;

  const campos = {
    celsius: document.getElementById('t-celsius'),
    kelvin: document.getElementById('t-kelvin'),
    fahrenheit: document.getElementById('t-fahrenheit'),
  };
  const convertidos = {
    celsius: formatearNumero(celsius),
    kelvin: formatearNumero(kelvin),
    fahrenheit: formatearNumero(fahrenheit),
  };

  Object.keys(campos).forEach(clave => {
    if (clave === desde) {
      campos[clave].value = valorCrudo;
    } else {
      campos[clave].value = esValido ? convertidos[clave] : '';
    }
  });

  if (esValido) {
    document.getElementById('td-celsius').innerHTML =
      `→ ${formatearNumero(kelvin)} K<br>→ ${formatearNumero(fahrenheit)} °F`;
    document.getElementById('td-kelvin').innerHTML =
      `→ ${formatearNumero(celsius)} °C<br>→ ${formatearNumero(fahrenheit)} °F`;
    document.getElementById('td-fahrenheit').innerHTML =
      `→ ${formatearNumero(celsius)} °C<br>→ ${formatearNumero(kelvin)} K`;
  } else {
    ['td-celsius', 'td-kelvin', 'td-fahrenheit'].forEach(id => {
      document.getElementById(id).innerHTML = '';
    });
  }

  _bloqueoTemperatura = false;
}

let _bloqueoLongitud = false;

const KM_POR_MILLA = 1.60934;
const M_POR_KM = 1000;

function convertirLongitud(desde, valorCrudo) {
  if (_bloqueoLongitud) return;
  _bloqueoLongitud = true;

  const valor = parseFloat(valorCrudo);
  const esValido = !isNaN(valor) && valor >= 0;

  let kilometros;
  if (desde === 'km') kilometros = valor;
  else if (desde === 'mi') kilometros = valor * KM_POR_MILLA;
  else kilometros = valor / M_POR_KM;

  const millas = kilometros / KM_POR_MILLA;
  const metros = kilometros * M_POR_KM;

  const valorKm = esValido ? parseFloat(kilometros.toFixed(6)) : '';
  const valorMi = esValido ? parseFloat(millas.toFixed(6)) : '';
  const valorMt = esValido ? parseFloat(metros.toFixed(4)) : '';

  const entradas = {
    km: document.getElementById('l-km'),
    mi: document.getElementById('l-mi'),
    mt: document.getElementById('l-mt'),
  };
  const convertidos = { km: valorKm, mi: valorMi, mt: valorMt };

  Object.keys(entradas).forEach(clave => {
    entradas[clave].value = clave === desde ? valorCrudo : esValido ? convertidos[clave] : '';
  });

  if (esValido) {
    document.getElementById('ld-km').innerHTML = `→ ${valorMi} mi<br>→ ${valorMt} m`;
    document.getElementById('ld-mi').innerHTML = `→ ${valorKm} km<br>→ ${valorMt} m`;
    document.getElementById('ld-mt').innerHTML = `→ ${valorKm} km<br>→ ${valorMi} mi`;
  } else {
    ['ld-km', 'ld-mi', 'ld-mt'].forEach(id => {
      document.getElementById(id).innerHTML = '';
    });
  }

  _bloqueoLongitud = false;
}
