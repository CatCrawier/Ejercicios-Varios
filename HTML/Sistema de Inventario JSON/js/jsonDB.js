import { validarEmail, validarPassword } from './validaciones.js';

const API_DB_URL = '/api/db';
const DB_KEY = 'inventario_json_db';
const SESSION_KEY = 'inventario_json_session';

const DEFAULT_DB = {
  usuarios: [],
  categorias: [
    { id: 1, nombre: 'General', descripcion: 'Productos sin categoria especifica', emoji: '📦', created_at: new Date().toISOString() },
  ],
  proveedores: [],
  productos: [],
  movimientos_inventario: [],
  counters: {
    usuarios: 1,
    categorias: 2,
    proveedores: 1,
    productos: 1,
    movimientos_inventario: 1,
  },
};

function clone(value) {
  return JSON.parse(JSON.stringify(value));
}

function readDB() {
  const serverDB = requestServerDB('GET');
  if (serverDB) return mergeDB(serverDB);

  const raw = localStorage.getItem(DB_KEY);
  if (!raw) {
    localStorage.setItem(DB_KEY, JSON.stringify(DEFAULT_DB));
    return clone(DEFAULT_DB);
  }

  try {
    return { ...clone(DEFAULT_DB), ...JSON.parse(raw) };
  } catch (error) {
    console.error('No se pudo leer el JSON local:', error);
    localStorage.setItem(DB_KEY, JSON.stringify(DEFAULT_DB));
    return clone(DEFAULT_DB);
  }
}

function writeDB(db) {
  if (requestServerDB('PUT', db)) return;
  localStorage.setItem(DB_KEY, JSON.stringify(db));
}

function mergeDB(db) {
  return {
    ...clone(DEFAULT_DB),
    ...db,
    counters: {
      ...clone(DEFAULT_DB).counters,
      ...(db.counters ?? {}),
    },
  };
}

function requestServerDB(method, body = null) {
  if (!window.location.protocol.startsWith('http')) return null;

  try {
    const xhr = new XMLHttpRequest();
    xhr.open(method, API_DB_URL, false);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send(body ? JSON.stringify(body) : null);

    if (xhr.status < 200 || xhr.status >= 300) return null;
    return method === 'GET' ? JSON.parse(xhr.responseText) : true;
  } catch (error) {
    console.warn('No se pudo usar data/inventario.json; usando localStorage.', error);
    return null;
  }
}

function nextId(db, table) {
  const id = db.counters[table] ?? 1;
  db.counters[table] = id + 1;
  return id;
}

function now() {
  return new Date().toISOString();
}

export function getSession() {
  const raw = localStorage.getItem(SESSION_KEY);
  if (!raw) return null;

  try {
    const session = JSON.parse(raw);
    const user = readDB().usuarios.find(u => u.id === session.user.id);
    return user ? { user: { id: user.id, email: user.email } } : null;
  } catch {
    return null;
  }
}

export function signUp(email, password) {
  const db = readDB();
  const normalizedEmail = email.toLowerCase();
  const emailError = validarEmail(normalizedEmail);
  const passwordError = validarPassword(password);

  if (emailError) throw new Error(emailError);
  if (passwordError) throw new Error(passwordError);

  if (db.usuarios.some(u => u.email === normalizedEmail)) {
    throw new Error('Este correo ya esta registrado.');
  }

  const user = {
    id: nextId(db, 'usuarios'),
    email: normalizedEmail,
    password,
    created_at: now(),
  };

  db.usuarios.push(user);
  writeDB(db);
  return { id: user.id, email: user.email };
}

export function signIn(email, password) {
  const normalizedEmail = email.toLowerCase();
  const emailError = validarEmail(normalizedEmail);

  if (emailError) throw new Error(emailError);

  const user = readDB().usuarios.find(u => u.email === normalizedEmail && u.password === password);

  if (!user) throw new Error('Credenciales incorrectas.');

  const session = { user: { id: user.id, email: user.email } };
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  return session;
}

export function signOut() {
  localStorage.removeItem(SESSION_KEY);
}

export function updatePassword(email, password) {
  const db = readDB();
  const emailError = validarEmail(email);
  const passwordError = validarPassword(password);

  if (emailError) throw new Error(emailError);
  if (passwordError) throw new Error(passwordError);

  const user = db.usuarios.find(u => u.email === email.toLowerCase());
  if (!user) throw new Error('Correo no encontrado.');
  user.password = password;
  writeDB(db);
}

export function listCategorias() {
  return readDB().categorias.sort((a, b) => a.nombre.localeCompare(b.nombre));
}

export function addCategoria(datos) {
  const db = readDB();
  db.categorias.push({ id: nextId(db, 'categorias'), ...datos, created_at: now() });
  writeDB(db);
}

export function updateCategoria(id, datos) {
  const db = readDB();
  const categoria = db.categorias.find(c => c.id === id);
  if (!categoria) throw new Error('Categoria no encontrada.');
  Object.assign(categoria, datos);
  writeDB(db);
}

export function deleteCategoria(id) {
  const db = readDB();
  db.categorias = db.categorias.filter(c => c.id !== id);
  db.productos.forEach(p => {
    if (p.categoria_id === id) p.categoria_id = null;
  });
  writeDB(db);
}

export function listProveedores({ activos = false } = {}) {
  return readDB().proveedores
    .filter(p => !activos || p.activo)
    .sort((a, b) => a.nombre.localeCompare(b.nombre));
}

export function addProveedor(datos) {
  const db = readDB();
  db.proveedores.push({ id: nextId(db, 'proveedores'), activo: true, ...datos, created_at: now() });
  writeDB(db);
}

export function updateProveedor(id, datos) {
  const db = readDB();
  const proveedor = db.proveedores.find(p => p.id === id);
  if (!proveedor) throw new Error('Proveedor no encontrado.');
  Object.assign(proveedor, datos);
  writeDB(db);
}

export function deleteProveedor(id) {
  const db = readDB();
  db.proveedores = db.proveedores.filter(p => p.id !== id);
  db.productos.forEach(p => {
    if (p.proveedor_id === id) p.proveedor_id = null;
  });
  writeDB(db);
}

function withRelations(db, producto) {
  return {
    ...producto,
    categorias: db.categorias.find(c => c.id === producto.categoria_id) ?? null,
    proveedores: db.proveedores.find(p => p.id === producto.proveedor_id) ?? null,
  };
}

export function listProductos({ activos = false, relaciones = true } = {}) {
  const db = readDB();
  return db.productos
    .filter(p => !activos || p.activo)
    .sort((a, b) => new Date(b.created_at) - new Date(a.created_at))
    .map(p => relaciones ? withRelations(db, p) : { ...p });
}

export function getProducto(id) {
  const db = readDB();
  const producto = db.productos.find(p => p.id === id);
  return producto ? withRelations(db, producto) : null;
}

export function addProducto(datos) {
  const db = readDB();
  const session = getSession();
  const producto = {
    id: nextId(db, 'productos'),
    ...datos,
    activo: true,
    user_id: session?.user?.id ?? null,
    created_at: now(),
  };

  db.productos.push(producto);
  writeDB(db);
  return { ...producto };
}

export function updateProducto(id, datos) {
  const db = readDB();
  const producto = db.productos.find(p => p.id === id);
  if (!producto) throw new Error('Producto no encontrado.');
  Object.assign(producto, datos);
  writeDB(db);
}

export function softDeleteProducto(id) {
  updateProducto(id, { activo: false });
}

export function deleteProducto(id) {
  const db = readDB();
  db.productos = db.productos.filter(p => p.id !== id);
  db.movimientos_inventario = db.movimientos_inventario.filter(m => m.producto_id !== id);
  writeDB(db);
}

export function addMovimiento(datos) {
  const db = readDB();
  const session = getSession();
  db.movimientos_inventario.push({
    id: nextId(db, 'movimientos_inventario'),
    user_id: session?.user?.id ?? null,
    ...datos,
    created_at: now(),
  });
  writeDB(db);
}

export function listMovimientos({ productoId = null, tipo = null, limit = 200 } = {}) {
  const db = readDB();
  return db.movimientos_inventario
    .filter(m => !productoId || m.producto_id === productoId)
    .filter(m => !tipo || m.tipo === tipo)
    .sort((a, b) => new Date(b.created_at) - new Date(a.created_at))
    .slice(0, limit)
    .map(m => ({
      ...m,
      productos: db.productos.find(p => p.id === m.producto_id) ?? null,
    }));
}

export function deleteMovimiento(id) {
  const db = readDB();
  db.movimientos_inventario = db.movimientos_inventario.filter(m => m.id !== id);
  writeDB(db);
}
