package com.example.crud

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

@Suppress("unused")
class ContactoAdapter(
    private val context: Context,
    private var listaContactos: ArrayList<Contacto>,
    private val dao: ContactoDAO
) : BaseAdapter() {

    override fun getCount(): Int = listaContactos.size

    override fun getItem(position: Int): Any = listaContactos[position]

    override fun getItemId(position: Int): Long = listaContactos[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_contacto, parent, false)

        val tvNombreCompleto = view.findViewById<TextView>(R.id.tvItemNombreCompleto)
        val tvOcupacion = view.findViewById<TextView>(R.id.tvItemOcupacion)
        val tvEdad = view.findViewById<TextView>(R.id.tvItemEdad)
        val btnEditar = view.findViewById<Button>(R.id.btnItemEditar)
        val btnEliminar = view.findViewById<Button>(R.id.btnItemEliminar)

        val contacto = listaContactos[position]

        // Assign text with formatted resources instead of concatenating strings.
        tvNombreCompleto.text = context.getString(
            R.string.formato_nombre_completo,
            contacto.nombre,
            contacto.apellido1,
            contacto.apellido2
        )
        tvOcupacion.text = context.getString(R.string.formato_ocupacion, contacto.ocupacion)
        tvEdad.text = context.getString(R.string.formato_edad, contacto.edad)

        btnEditar.setOnClickListener { mostrarFormularioEdicion(contacto) }

        btnEliminar.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle(R.string.confirmacion)
                .setMessage(context.getString(R.string.formato_mensaje_eliminar, contacto.nombre))
                .setPositiveButton(R.string.si) { _, _ ->
                    dao.eliminar(contacto.id)
                    actualizarLista()
                    Toast.makeText(context, R.string.registro_eliminado, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.no, null)
                .show()
        }

        return view
    }

    fun actualizarLista() {
        listaContactos = dao.verTodos()
        notifyDataSetChanged()
    }

    private fun mostrarFormularioEdicion(contacto: Contacto) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialogo_formulario)

        val etNombre = dialog.findViewById<EditText>(R.id.etNombre)
        val etApellido1 = dialog.findViewById<EditText>(R.id.etApellido1)
        val etApellido2 = dialog.findViewById<EditText>(R.id.etApellido2)
        val etOcupacion = dialog.findViewById<EditText>(R.id.etOcupacion)
        val etEdad = dialog.findViewById<EditText>(R.id.etEdad)
        val btnGuardar = dialog.findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelar)

        etNombre.setText(contacto.nombre)
        etApellido1.setText(contacto.apellido1)
        etApellido2.setText(contacto.apellido2)
        etOcupacion.setText(contacto.ocupacion)
        etEdad.setText(contacto.edad.toString())

        btnGuardar.setOnClickListener {
            contacto.nombre = etNombre.text.toString()
            contacto.apellido1 = etApellido1.text.toString()
            contacto.apellido2 = etApellido2.text.toString()
            contacto.ocupacion = etOcupacion.text.toString()
            contacto.edad = etEdad.text.toString().toIntOrNull() ?: 0

            dao.editar(contacto)
            actualizarLista()
            dialog.dismiss()
            Toast.makeText(context, R.string.registro_actualizado, Toast.LENGTH_SHORT).show()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
