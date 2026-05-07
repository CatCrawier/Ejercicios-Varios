function establecerTexto(id, texto) {
  document.getElementById(id).textContent = texto;
}

function limpiarDigitos() {
  for (let i = 1; i <= 8; i += 1) {
    establecerTexto(`digito${i}`, '-');
  }
}

function limpiarResultadosNumero() {
  establecerTexto('v-suma', '-');
  establecerTexto('v-multiplicacion', '-');
  establecerTexto('v-resta', '-');
  establecerTexto('v-potencia', '-');
  establecerTexto('f-suma', 'd4 + d5');
  establecerTexto('f-multiplicacion', 'd1 x d8');
  establecerTexto('f-resta', 'd7 - d2');
  establecerTexto('f-potencia', 'd3 ^ d6');
}

function procesarNumero(valor) {
  const entrada = document.getElementById('entradaNumero');
  const limpio = valor.replace(/\D/g, '');

  if (valor !== limpio) {
    entrada.value = limpio;
  }

  limpiarDigitos();
  limpiarResultadosNumero();

  if (limpio.length === 0) {
    establecerTexto('errorNumero', '');
    return;
  }

  for (let i = 0; i < limpio.length; i += 1) {
    establecerTexto(`digito${i + 1}`, limpio[i]);
  }

  if (limpio.length < 8) {
    establecerTexto('errorNumero', `Faltan ${8 - limpio.length} digito(s).`);
    return;
  }

  establecerTexto('errorNumero', '');

  const digitos = limpio.split('').map(Number);
  const suma = digitos[3] + digitos[4];
  const multiplicacion = digitos[0] * digitos[7];
  const resta = digitos[6] - digitos[1];
  const potencia = Math.pow(digitos[2], digitos[5]);

  establecerTexto('v-suma', String(suma));
  establecerTexto('v-multiplicacion', String(multiplicacion));
  establecerTexto('v-resta', String(resta));
  establecerTexto('v-potencia', String(potencia));

  establecerTexto('f-suma', `${digitos[3]} + ${digitos[4]}`);
  establecerTexto('f-multiplicacion', `${digitos[0]} x ${digitos[7]}`);
  establecerTexto('f-resta', `${digitos[6]} - ${digitos[1]}`);
  establecerTexto('f-potencia', `${digitos[2]} ^ ${digitos[5]}`);
}

let bloqueoTemperatura = false;

function formatearNumero(numero, decimales = 4) {
  return String(parseFloat(numero.toFixed(decimales)));
}

function convertirTemperatura(desde, valorCrudo) {
  if (bloqueoTemperatura) {
    return;
  }

  bloqueoTemperatura = true;

  const valor = parseFloat(valorCrudo);
  const esValido = !Number.isNaN(valor);

  const campos = {
    celsius: document.getElementById('t-celsius'),
    kelvin: document.getElementById('t-kelvin'),
    fahrenheit: document.getElementById('t-fahrenheit'),
  };

  if (!esValido) {
    Object.values(campos).forEach((campo) => {
      if (campo.id !== `t-${desde}`) {
        campo.value = '';
      }
    });
    establecerTexto('td-celsius', '');
    establecerTexto('td-kelvin', '');
    establecerTexto('td-fahrenheit', '');
    bloqueoTemperatura = false;
    return;
  }

  let celsius;
  if (desde === 'celsius') {
    celsius = valor;
  } else if (desde === 'kelvin') {
    celsius = valor - 273.15;
  } else {
    celsius = ((valor - 32) * 5) / 9;
  }

  const kelvin = celsius + 273.15;
  const fahrenheit = (celsius * 9) / 5 + 32;

  if (desde !== 'celsius') {
    campos.celsius.value = formatearNumero(celsius);
  }
  if (desde !== 'kelvin') {
    campos.kelvin.value = formatearNumero(kelvin);
  }
  if (desde !== 'fahrenheit') {
    campos.fahrenheit.value = formatearNumero(fahrenheit);
  }

  establecerTexto(
    'td-celsius',
    `Equivale a ${formatearNumero(kelvin)} K y ${formatearNumero(fahrenheit)} F.`
  );
  establecerTexto(
    'td-kelvin',
    `Equivale a ${formatearNumero(celsius)} C y ${formatearNumero(fahrenheit)} F.`
  );
  establecerTexto(
    'td-fahrenheit',
    `Equivale a ${formatearNumero(celsius)} C y ${formatearNumero(kelvin)} K.`
  );

  bloqueoTemperatura = false;
}

let bloqueoLongitud = false;
const KM_POR_MILLA = 1.60934;
const M_POR_KM = 1000;

function convertirLongitud(desde, valorCrudo) {
  if (bloqueoLongitud) {
    return;
  }

  bloqueoLongitud = true;

  const valor = parseFloat(valorCrudo);
  const esValido = !Number.isNaN(valor) && valor >= 0;

  const entradas = {
    km: document.getElementById('l-km'),
    mi: document.getElementById('l-mi'),
    mt: document.getElementById('l-mt'),
  };

  if (!esValido) {
    Object.values(entradas).forEach((entrada) => {
      if (entrada.id !== `l-${desde}`) {
        entrada.value = '';
      }
    });
    establecerTexto('ld-km', '');
    establecerTexto('ld-mi', '');
    establecerTexto('ld-mt', '');
    bloqueoLongitud = false;
    return;
  }

  let kilometros;
  if (desde === 'km') {
    kilometros = valor;
  } else if (desde === 'mi') {
    kilometros = valor * KM_POR_MILLA;
  } else {
    kilometros = valor / M_POR_KM;
  }

  const millas = kilometros / KM_POR_MILLA;
  const metros = kilometros * M_POR_KM;

  if (desde !== 'km') {
    entradas.km.value = formatearNumero(kilometros, 6);
  }
  if (desde !== 'mi') {
    entradas.mi.value = formatearNumero(millas, 6);
  }
  if (desde !== 'mt') {
    entradas.mt.value = formatearNumero(metros, 4);
  }

  establecerTexto(
    'ld-km',
    `Equivale a ${formatearNumero(millas, 6)} mi y ${formatearNumero(metros, 4)} m.`
  );
  establecerTexto(
    'ld-mi',
    `Equivale a ${formatearNumero(kilometros, 6)} km y ${formatearNumero(metros, 4)} m.`
  );
  establecerTexto(
    'ld-mt',
    `Equivale a ${formatearNumero(kilometros, 6)} km y ${formatearNumero(millas, 6)} mi.`
  );

  bloqueoLongitud = false;
}

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('ejemploNumero').addEventListener('click', () => {
    document.getElementById('entradaNumero').value = '12345678';
    procesarNumero('12345678');
  });

  document.getElementById('ejemploTemperatura').addEventListener('click', () => {
    document.getElementById('t-celsius').value = '100';
    convertirTemperatura('celsius', '100');
  });

  document.getElementById('ejemploLongitud').addEventListener('click', () => {
    document.getElementById('l-mi').value = '1';
    convertirLongitud('mi', '1');
  });

  limpiarDigitos();
  limpiarResultadosNumero();
});
