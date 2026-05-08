// ============================================================
//  proveedores.js — Carga proveedores desde Supabase
// ============================================================
import { supabase } from './supabase.js';

export async function obtenerProveedores() {
  const { data, error } = await supabase
    .from('proveedores')
    .select('id, nombre')
    .eq('activo', true)
    .order('nombre');

  if (error) { console.error('Error al cargar proveedores:', error); return []; }
  return data ?? [];
}
