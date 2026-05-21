import { supabase } from './supabase.js';

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
