// ============================================================
//  proveedores.js - Carga proveedores desde JSON local
// ============================================================
import { listProveedores } from './jsonDB.js';

export async function obtenerProveedores() {
  return listProveedores({ activos: true }).map(({ id, nombre }) => ({ id, nombre }));
}
