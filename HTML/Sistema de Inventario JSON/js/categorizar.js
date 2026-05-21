import { listCategorias } from './jsonDB.js';

export async function obtenerCategorias() {
  return listCategorias().map(({ id, nombre, emoji }) => ({ id, nombre, emoji }));
}
