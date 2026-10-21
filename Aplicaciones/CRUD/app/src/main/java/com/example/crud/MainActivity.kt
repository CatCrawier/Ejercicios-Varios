package com.example.crud

import android.app.Dialog
import android.os.Bundle
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var dao: ContactoDAO
    private lateinit var adapter: ContactoAdapter
    private lateinit var listViewContactos: ListView
    private lateinit var btnAgregar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dao = ContactoDAO(this)
        btnAgregar = findViewById(R.id.btnAgregar)
        listViewContactos = findViewById(R.id.listViewContactos)

        // Migración moderna para el botón Atrás (OnBackPressedDispatcher)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle(R.string.salir)
                    .setMessage(R.string.mensaje_salir)
                    .setPositiveButton(R.string.si) { _, _ -> finish() }
                    .setNegativeButton(R.string.no, null)
                    .show()
            }
        })

        // Mostrar login al inicio
        mostrarDialogLogin()

        val lista = dao.verTodos()
        adapter = ContactoAdapter(this, lista, dao)
        listViewContactos.adapter = adapter

        btnAgregar.setOnClickListener {
            mostrarFormularioCreacion()
        }

        listViewContactos.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val contactoSeleccionado = adapter.getItem(position) as Contacto
            mostrarVistaDetalle(contactoSeleccionado.id)
        }
    }

    private fun mostrarFormularioCreacion() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialogo_formulario)

        val etNombre = dialog.findViewById<EditText>(R.id.etNombre)
        val etApellido1 = dialog.findViewById<EditText>(R.id.etApellido1)
        val etApellido2 = dialog.findViewById<EditText>(R.id.etApellido2)
        val etOcupacion = dialog.findViewById<EditText>(R.id.etOcupacion)
        val etEdad = dialog.findViewById<EditText>(R.id.etEdad)
        val btnGuardar = dialog.findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelar)

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val ap1 = etApellido1.text.toString()
            val ap2 = etApellido2.text.toString()
            val ocupacion = etOcupacion.text.toString()
            val edad = etEdad.text.toString().toIntOrNull() ?: 0

            if (nombre.isNotBlank() && ap1.isNotBlank()) {
                val nuevoContacto = Contacto(nombre, ap1, ap2, ocupacion, edad)
                dao.insertar(nuevoContacto)
                adapter.actualizarLista()
                dialog.dismiss()
                Toast.makeText(this, R.string.registro_guardado, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.campos_requeridos, Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarVistaDetalle(id: Int) {
        val contacto = dao.verUno(id) ?: return
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialogo_ver_contacto)

        val tvNombre = dialog.findViewById<TextView>(R.id.tvVerNombre)
        val tvApellido1 = dialog.findViewById<TextView>(R.id.tvVerApellido1)
        val tvApellido2 = dialog.findViewById<TextView>(R.id.tvVerApellido2)
        val tvOcupacion = dialog.findViewById<TextView>(R.id.tvVerOcupacion)
        val tvEdad = dialog.findViewById<TextView>(R.id.tvVerEdad)
        val btnCerrar = dialog.findViewById<Button>(R.id.btnVerCerrar)

        // Asignación de recursos con placeholders
        tvNombre.text = getString(R.string.formato_nombre, contacto.nombre)
        tvApellido1.text = getString(R.string.formato_apellido1, contacto.apellido1)
        tvApellido2.text = getString(R.string.formato_apellido2, contacto.apellido2)
        tvOcupacion.text = getString(R.string.formato_ocupacion, contacto.ocupacion)
        tvEdad.text = getString(R.string.formato_edad, contacto.edad)

        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarDialogLogin() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialogo_login)
        dialog.setCancelable(false)

        val etUsuario = dialog.findViewById<EditText>(R.id.etUsuario)
        val etPassword = dialog.findViewById<EditText>(R.id.etPassword)
        val btnLoginAceptar = dialog.findViewById<Button>(R.id.btnLoginAceptar)

        btnLoginAceptar.setOnClickListener {
            val user = etUsuario.text.toString()
            val pass = etPassword.text.toString()

            if (user == "admin" && pass == "1234") {
                Toast.makeText(this, R.string.bienvenido, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, R.string.credenciales_incorrectas, Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}
