package com.example.crud

import android.app.Dialog
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageButton
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class ContactoAdapter(
    private val context: Context,
    contactos: ArrayList<Contacto>,
    private val dao: ContactoDAO,
    private val onContactosCambiados: () -> Unit,
    private val onEliminarContacto: (Contacto) -> Unit
) : BaseAdapter(), Filterable {

    private var listaCompleta = ArrayList(contactos)
    private var listaVisible = ArrayList(contactos)
    private var consultaActual = ""

    override fun getCount(): Int = listaVisible.size

    override fun getItem(position: Int): Any = listaVisible[position]

    override fun getItemId(position: Int): Long = listaVisible[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_contacto, parent, false)
        val contacto = listaVisible[position]

        view.findViewById<TextView>(R.id.tvItemNombreCompleto).text = context.getString(
            R.string.formato_nombre_completo,
            contacto.nombre,
            contacto.apellido1,
            contacto.apellido2
        ).trim()
        view.findViewById<TextView>(R.id.tvItemOcupacion).text =
            context.getString(R.string.formato_ocupacion, contacto.ocupacion)
        view.findViewById<TextView>(R.id.tvItemEdad).text =
            context.getString(R.string.formato_edad, contacto.edad)
        view.findViewById<TextView>(R.id.tvItemCategoria).text =
            context.getString(R.string.formato_categoria, contacto.categoriaNombre)

        view.findViewById<View>(R.id.btnItemEditar).setOnClickListener {
            mostrarFormularioEdicion(contacto)
        }
        view.findViewById<View>(R.id.btnItemEliminar).setOnClickListener {
            onEliminarContacto(contacto)
        }
        return view
    }

    /** Reemplaza la fuente de datos por la categoría seleccionada y conserva la búsqueda actual. */
    fun actualizarLista(categoriaId: Int?) {
        listaCompleta = categoriaId?.let(dao::verPorCategoria) ?: dao.verTodos()
        filter.filter(consultaActual)
    }

    fun buscar(consulta: String) {
        consultaActual = consulta
        filter.filter(consulta)
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val consulta = constraint?.toString()?.trim()?.lowercase(Locale.getDefault()).orEmpty()
            val fuente = listaCompleta.toList()
            val resultado = if (consulta.isBlank()) {
                fuente
            } else {
                fuente.filter { contacto ->
                    listOf(contacto.nombre, contacto.apellido1, contacto.apellido2)
                        .any { it.lowercase(Locale.getDefault()).contains(consulta) }
                }
            }
            return FilterResults().apply {
                values = ArrayList(resultado)
                count = resultado.size
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            listaVisible = (results?.values as? ArrayList<Contacto>) ?: arrayListOf()
            notifyDataSetChanged()
        }
    }

    private fun mostrarFormularioEdicion(contacto: Contacto) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialogo_formulario)

        val tilNombre = dialog.findViewById<TextInputLayout>(R.id.tilNombre)
        val tilApellido1 = dialog.findViewById<TextInputLayout>(R.id.tilApellido1)
        val tilOcupacion = dialog.findViewById<TextInputLayout>(R.id.tilOcupacion)
        val tilEdad = dialog.findViewById<TextInputLayout>(R.id.tilEdad)
        val tilCategoria = dialog.findViewById<TextInputLayout>(R.id.tilCategoria)
        val etNombre = dialog.findViewById<EditText>(R.id.etNombre)
        val etApellido1 = dialog.findViewById<EditText>(R.id.etApellido1)
        val etApellido2 = dialog.findViewById<EditText>(R.id.etApellido2)
        val etOcupacion = dialog.findViewById<EditText>(R.id.etOcupacion)
        val etEdad = dialog.findViewById<EditText>(R.id.etEdad)
        val spCategoria = dialog.findViewById<Spinner>(R.id.spCategoria)

        val categorias = listOf(Categoria(0, context.getString(R.string.selecciona_categoria))) + dao.verCategorias()
        spCategoria.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, categorias)
        spCategoria.setSelection(categorias.indexOfFirst { it.id == contacto.categoriaId }.coerceAtLeast(0))
        etNombre.setText(contacto.nombre)
        etApellido1.setText(contacto.apellido1)
        etApellido2.setText(contacto.apellido2)
        etOcupacion.setText(contacto.ocupacion)
        etEdad.setText(contacto.edad.toString())

        dialog.findViewById<ImageButton>(R.id.btnGuardar).setOnClickListener {
            val categoria = spCategoria.selectedItem as Categoria
            if (!validarFormulario(tilNombre, tilApellido1, tilOcupacion, tilEdad, tilCategoria,
                    etNombre, etApellido1, etOcupacion, etEdad, categoria)) return@setOnClickListener

            contacto.nombre = etNombre.text.toString().trim()
            contacto.apellido1 = etApellido1.text.toString().trim()
            contacto.apellido2 = etApellido2.text.toString().trim()
            contacto.ocupacion = etOcupacion.text.toString().trim()
            contacto.edad = etEdad.text.toString().toInt()
            contacto.categoriaId = categoria.id
            dao.editar(contacto)
            onContactosCambiados()
            dialog.dismiss()
            Toast.makeText(context, R.string.registro_actualizado, Toast.LENGTH_SHORT).show()
        }
        dialog.findViewById<ImageButton>(R.id.btnCancelar).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun validarFormulario(
        tilNombre: TextInputLayout,
        tilApellido1: TextInputLayout,
        tilOcupacion: TextInputLayout,
        tilEdad: TextInputLayout,
        tilCategoria: TextInputLayout,
        etNombre: EditText,
        etApellido1: EditText,
        etOcupacion: EditText,
        etEdad: EditText,
        categoria: Categoria
    ): Boolean {
        val nombreValido = etNombre.text.toString().trim().length >= 2
        val apellidoValido = etApellido1.text.toString().trim().length >= 2
        val ocupacionValida = etOcupacion.text.toString().trim().length >= 2
        val edad = etEdad.text.toString().toIntOrNull()
        tilNombre.error = if (nombreValido) null else context.getString(R.string.error_nombre)
        tilApellido1.error = if (apellidoValido) null else context.getString(R.string.error_apellido)
        tilOcupacion.error = if (ocupacionValida) null else context.getString(R.string.error_ocupacion)
        tilEdad.error = if (edad in 1..120) null else context.getString(R.string.error_edad)
        tilCategoria.error = if (categoria.id > 0) null else context.getString(R.string.error_categoria)
        return nombreValido && apellidoValido && ocupacionValida && edad in 1..120 && categoria.id > 0
    }
}
