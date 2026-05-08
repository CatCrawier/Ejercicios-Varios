// ============================================================
//  categorizar.js — Carga las categorías desde Supabase
// ============================================================
import { supabase } from './supabase.js';

/**
 * Obtiene todas las categorías ordenadas por nombre.
 * @returns {Promise<Array<{id, nombre, emoji}>>}
 */
export async function obtenerCategorias() {
  const { data, error } = await supabase
    .from('categorias')
    .select('id, nombre, emoji')
    .order('nombre');

  if (error) {
    console.error('Error al cargar categorías:', error);
    return [];
  }
  return data ?? [];
}
