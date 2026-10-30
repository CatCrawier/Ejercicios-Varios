package com.example.crud

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import java.io.OutputStreamWriter

class MainActivity : AppCompatActivity() {

    private lateinit var dao: ContactoDAO
    private lateinit var adapter: ContactoAdapter
    private lateinit var listViewContactos: ListView
    private lateinit var contenedorCategorias: GridLayout
    private val botonesCategorias = mutableMapOf<Int, MaterialButton>()
    private var categoriaFiltradaId: Int? = null

    private val crearArchivoCsv = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let(::exportarContactos) }

    override fun onCreate(savedInstanceState: Bundle?) {
        aplicarTemaGuardado()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dao = ContactoDAO(this)
        listViewContactos = findViewById(R.id.listViewContactos)
        contenedorCategorias = findViewById(R.id.contenedorCategorias)
        adapter = ContactoAdapter(this, dao.verTodos(), dao, ::refrescarContactos, ::eliminarConDeshacer)
        listViewContactos.adapter = adapter

        configurarTema()
        configurarBusqueda()
        configurarFiltrosCategorias()
        findViewById<ImageButton>(R.id.btnAgregar).setOnClickListener { mostrarFormularioCreacion() }
        findViewById<MaterialButton>(R.id.btnExportar).setOnClickListener {
            crearArchivoCsv.launch("contactos.csv")
        }
        listViewContactos.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            mostrarVistaDetalle((adapter.getItem(position) as Contacto).id)
        }

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
        if (!sesionIniciada()) {
            mostrarDialogLogin()
        }
    }

    private fun configurarBusqueda() {
        findViewById<SearchView>(R.id.svBuscar).setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.buscar(newText.orEmpty())
                return true
            }
        })
    }

    private fun configurarFiltrosCategorias() {
        val categorias = listOf(Categoria(0, getString(R.string.todas_las_categorias))) + dao.verCategorias()
        val margen = (4 * resources.displayMetrics.density).toInt()
        categorias.forEach { categoria ->
            MaterialButton(this).apply {
                id = View.generateViewId()
                text = categoria.nombre
                isCheckable = true
                layoutParams = GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f)
                ).apply {
                    width = 0
                    setMargins(margen, margen, margen, margen)
                }
                botonesCategorias[categoria.id] = this
                setOnClickListener { seleccionarCategoria(categoria.id) }
                contenedorCategorias.addView(this)
            }
        }
        seleccionarCategoria(0)
    }

    private fun seleccionarCategoria(categoriaId: Int) {
        botonesCategorias.forEach { (id, boton) ->
            boton.isChecked = id == categoriaId
        }
        categoriaFiltradaId = categoriaId.takeIf { it != 0 }
        refrescarContactos()
    }

    private fun configurarTema() {
        val interruptor = findViewById<MaterialSwitch>(R.id.swTemaOscuro)
        interruptor.isChecked = getSharedPreferences(PREFERENCIAS, MODE_PRIVATE).getBoolean(CLAVE_TEMA_OSCURO, false)
        interruptor.setOnCheckedChangeListener { _, oscuro ->
            getSharedPreferences(PREFERENCIAS, MODE_PRIVATE).edit {
                putBoolean(CLAVE_TEMA_OSCURO, oscuro)
            }
            val modo = if (oscuro) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            if (AppCompatDelegate.getDefaultNightMode() != modo) AppCompatDelegate.setDefaultNightMode(modo)
        }
    }

    private fun aplicarTemaGuardado() {
        val oscuro = getSharedPreferences(PREFERENCIAS, MODE_PRIVATE).getBoolean(CLAVE_TEMA_OSCURO, false)
        AppCompatDelegate.setDefaultNightMode(if (oscuro) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun mostrarFormularioCreacion() {
        val dialog = Dialog(this)
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
        configurarSelectorCategoria(spCategoria)

        dialog.findViewById<ImageButton>(R.id.btnGuardar).setOnClickListener {
            val categoria = spCategoria.selectedItem as Categoria
            if (!validarFormulario(tilNombre, tilApellido1, tilOcupacion, tilEdad, tilCategoria,
                    etNombre, etApellido1, etOcupacion, etEdad, categoria)) return@setOnClickListener
            dao.insertar(Contacto(
                etNombre.text.toString().trim(), etApellido1.text.toString().trim(),
                etApellido2.text.toString().trim(), etOcupacion.text.toString().trim(),
                etEdad.text.toString().toInt(), categoria.id
            ))
            refrescarContactos()
            dialog.dismiss()
            Toast.makeText(this, R.string.registro_guardado, Toast.LENGTH_SHORT).show()
        }
        dialog.findViewById<ImageButton>(R.id.btnCancelar).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun configurarSelectorCategoria(spinner: Spinner, categoriaIdSeleccionada: Int = 0) {
        val categorias = listOf(Categoria(0, getString(R.string.selecciona_categoria))) + dao.verCategorias()
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categorias)
        spinner.setSelection(categorias.indexOfFirst { it.id == categoriaIdSeleccionada }.coerceAtLeast(0))
    }

    private fun validarFormulario(
        tilNombre: TextInputLayout, tilApellido1: TextInputLayout, tilOcupacion: TextInputLayout,
        tilEdad: TextInputLayout, tilCategoria: TextInputLayout, etNombre: EditText,
        etApellido1: EditText, etOcupacion: EditText, etEdad: EditText, categoria: Categoria
    ): Boolean {
        val nombreValido = etNombre.text.toString().trim().length >= 2
        val apellidoValido = etApellido1.text.toString().trim().length >= 2
        val ocupacionValida = etOcupacion.text.toString().trim().length >= 2
        val edad = etEdad.text.toString().toIntOrNull()
        tilNombre.error = if (nombreValido) null else getString(R.string.error_nombre)
        tilApellido1.error = if (apellidoValido) null else getString(R.string.error_apellido)
        tilOcupacion.error = if (ocupacionValida) null else getString(R.string.error_ocupacion)
        tilEdad.error = if (edad in 1..120) null else getString(R.string.error_edad)
        tilCategoria.error = if (categoria.id > 0) null else getString(R.string.error_categoria)
        return nombreValido && apellidoValido && ocupacionValida && edad in 1..120 && categoria.id > 0
    }

    private fun eliminarConDeshacer(contacto: Contacto) {
        dao.eliminar(contacto.id)
        refrescarContactos()
        Snackbar.make(findViewById(R.id.main), getString(R.string.contacto_eliminado, contacto.nombre), Snackbar.LENGTH_LONG)
            .setAction(R.string.deshacer) {
                dao.restaurar(contacto)
                refrescarContactos()
            }
            .show()
    }

    private fun mostrarVistaDetalle(id: Int) {
        val contacto = dao.verUno(id) ?: return
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialogo_ver_contacto)
        dialog.findViewById<TextView>(R.id.tvVerNombre).text = getString(R.string.formato_nombre, contacto.nombre)
        dialog.findViewById<TextView>(R.id.tvVerApellido1).text = getString(R.string.formato_apellido1, contacto.apellido1)
        dialog.findViewById<TextView>(R.id.tvVerApellido2).text = getString(R.string.formato_apellido2, contacto.apellido2)
        dialog.findViewById<TextView>(R.id.tvVerOcupacion).text = getString(R.string.formato_ocupacion, contacto.ocupacion)
        dialog.findViewById<TextView>(R.id.tvVerEdad).text = getString(R.string.formato_edad, contacto.edad)
        dialog.findViewById<TextView>(R.id.tvVerCategoria).text = getString(R.string.formato_categoria, contacto.categoriaNombre)
        dialog.findViewById<View>(R.id.btnVerCerrar).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun mostrarDialogLogin() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialogo_login)
        dialog.setCancelable(false)
        val etUsuario = dialog.findViewById<EditText>(R.id.etUsuario)
        val etPassword = dialog.findViewById<EditText>(R.id.etPassword)
        dialog.findViewById<Button>(R.id.btnLoginAceptar).setOnClickListener {
            if (etUsuario.text.toString() == "admin" && etPassword.text.toString() == "1234") {
                getSharedPreferences(PREFERENCIAS, MODE_PRIVATE).edit {
                    putBoolean(CLAVE_SESION_INICIADA, true)
                }
                Toast.makeText(this, R.string.bienvenido, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else Toast.makeText(this, R.string.credenciales_incorrectas, Toast.LENGTH_SHORT).show()
        }
        dialog.show()
    }

    private fun sesionIniciada(): Boolean =
        getSharedPreferences(PREFERENCIAS, MODE_PRIVATE).getBoolean(CLAVE_SESION_INICIADA, false)

    private fun refrescarContactos() = adapter.actualizarLista(categoriaFiltradaId)

    private fun exportarContactos(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { salida ->
                OutputStreamWriter(salida, Charsets.UTF_8).use { escritor ->
                    escritor.write("\uFEFFNombre,Apellido paterno,Apellido materno,Ocupacion,Edad,Categoria\n")
                    dao.verTodos().forEach { contacto ->
                        escritor.write(listOf(contacto.nombre, contacto.apellido1, contacto.apellido2,
                            contacto.ocupacion, contacto.edad.toString(), contacto.categoriaNombre)
                            .joinToString(",") { valor -> "\"${valor.replace("\"", "\"\"")}\"" })
                        escritor.write("\n")
                    }
                }
            } ?: error("No se pudo abrir el archivo")
            Toast.makeText(this, R.string.exportacion_exitosa, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(this, R.string.error_exportacion, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val PREFERENCIAS = "preferencias_crud"
        private const val CLAVE_TEMA_OSCURO = "tema_oscuro"
        private const val CLAVE_SESION_INICIADA = "sesion_iniciada"
    }
}
