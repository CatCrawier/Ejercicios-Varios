const btnAgregar   = document.getElementById("btnAgregar");
const cuerpoTabla  = document.getElementById("cuerpoTabla");
const totalGeneral = document.getElementById("totalGeneral");

document.addEventListener("DOMContentLoaded", () => {
    fetch('inventario.json')
        .then(res => res.json())
        .then(productos => {
            productos.forEach(p => agregarProducto(p.nombre, p.cantidad, p.precio, false));
            recalcularTotal();
        })
        .catch(() => console.log("Inventario vacío o servidor no iniciado."));
});

function guardarCambios() {
    const productos = [];
    document.querySelectorAll("#cuerpoTabla tr").forEach(fila => {
        const enEdicion      = fila.classList.contains("editando");
        const entradaNombre  = fila.querySelector(".entrada-nombre");
        const entradaPrecio  = fila.querySelector(".entrada-precio");

        const nombre = (enEdicion && entradaNombre)
            ? entradaNombre.value.trim()
            : fila.cells[0].textContent.trim();

        const precio = (enEdicion && entradaPrecio)
            ? parseFloat(entradaPrecio.value)
            : parseFloat(fila.querySelector(".precio").textContent);

        productos.push({
            nombre,
            cantidad: parseInt(fila.querySelector(".cant").textContent),
            precio: isNaN(precio) ? 0 : precio
        });
    });

    fetch('http://localhost:8000', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(productos)
    }).catch(() => console.log("Servidor Python fuera de línea."));
}

btnAgregar.addEventListener("click", () => {
    const nombre   = document.getElementById("nombre").value.trim();
    const cantidad = parseInt(document.getElementById("cantidad").value);
    const precio   = parseFloat(document.getElementById("precio").value);

    if (nombre && !isNaN(cantidad) && !isNaN(precio) && precio >= 0) {
        agregarProducto(nombre, cantidad, precio);
        document.getElementById("nombre").value   = "";
        document.getElementById("cantidad").value = "1";
        document.getElementById("precio").value   = "";
        document.getElementById("nombre").focus();
    }
});

function agregarProducto(nombre, cantidad, precio, debaGuardar = true) {
    const fila = document.createElement("tr");
    fila.innerHTML = `
        <td class="celda-nombre">${nombre}</td>
        <td><button class="menos">–</button> <span class="cant">${cantidad}</span> <button class="mas">+</button></td>
        <td class="celda-precio">$<span class="precio">${precio.toFixed(2)}</span></td>
        <td>$<span class="total">${(cantidad * precio).toFixed(2)}</span></td>
        <td class="celda-acciones">
            <button class="editar" title="Editar">✏️</button>
            <button class="eliminar" title="Eliminar">🗑️</button>
        </td>
    `;
    cuerpoTabla.appendChild(fila);
    actualizarFila(fila);
    if (debaGuardar) { recalcularTotal(); guardarCambios(); }
}

cuerpoTabla.addEventListener("click", (evento) => {
    const fila = evento.target.closest("tr");
    if (!fila) return;

    if (evento.target.classList.contains("editar")) {
        iniciarModoEdicion(fila, evento.target);
        return;
    }
    if (evento.target.classList.contains("guardar-edicion")) {
        guardarEdicion(fila, evento.target);
        return;
    }
    if (evento.target.classList.contains("eliminar")) {
        fila.remove();
        recalcularTotal();
        guardarCambios();
        return;
    }
    if (evento.target.classList.contains("mas")) {
        fila.querySelector(".cant").textContent =
            parseInt(fila.querySelector(".cant").textContent) + 1;
    }
    if (evento.target.classList.contains("menos")) {
        const cantidad = parseInt(fila.querySelector(".cant").textContent);
        if (cantidad > 0) fila.querySelector(".cant").textContent = cantidad - 1;
    }
    actualizarFila(fila);
    recalcularTotal();
    guardarCambios();
});

function iniciarModoEdicion(fila, boton) {
    if (fila.classList.contains("editando")) return;
    fila.classList.add("editando");

    const celdaNombre    = fila.cells[0];
    const celdaPrecio    = fila.querySelector(".celda-precio");
    const nombreOriginal = celdaNombre.textContent.trim();
    const precioOriginal = parseFloat(fila.querySelector(".precio").textContent);

    celdaNombre.innerHTML = `<input type="text"   class="entrada-edicion entrada-nombre" value="${nombreOriginal}">`;
    celdaPrecio.innerHTML  = `<input type="number" class="entrada-edicion entrada-precio" value="${precioOriginal.toFixed(2)}" step="0.01" min="0">`;

    const entradaNombre = celdaNombre.querySelector(".entrada-nombre");
    const entradaPrecio = celdaPrecio.querySelector(".entrada-precio");

    entradaNombre.focus();
    entradaNombre.select();

    const manejarTecla = (evento) => {
        if (evento.key === "Enter")  guardarEdicion(fila, boton);
        if (evento.key === "Escape") cancelarEdicion(fila, boton, nombreOriginal, precioOriginal);
    };
    entradaNombre.addEventListener("keydown", manejarTecla);
    entradaPrecio.addEventListener("keydown", manejarTecla);

    boton.textContent = "✓";
    boton.classList.remove("editar");
    boton.classList.add("guardar-edicion");
    boton.title = "Guardar cambios";
}

function guardarEdicion(fila, boton) {
    const celdaNombre  = fila.cells[0];
    const celdaPrecio  = fila.querySelector(".celda-precio");
    const nuevoNombre  = (celdaNombre.querySelector(".entrada-nombre")?.value || "").trim();
    const nuevoPrecio  = parseFloat(celdaPrecio.querySelector(".entrada-precio")?.value);

    if (!nuevoNombre || isNaN(nuevoPrecio) || nuevoPrecio < 0) return;

    celdaNombre.textContent  = nuevoNombre;
    celdaPrecio.innerHTML     = `$<span class="precio">${nuevoPrecio.toFixed(2)}</span>`;
    fila.classList.remove("editando");

    boton.textContent = "✏️";
    boton.classList.remove("guardar-edicion");
    boton.classList.add("editar");
    boton.title = "Editar";

    actualizarFila(fila);
    recalcularTotal();
    guardarCambios();
}

function cancelarEdicion(fila, boton, nombreOriginal, precioOriginal) {
    fila.cells[0].textContent                    = nombreOriginal;
    fila.querySelector(".celda-precio").innerHTML = `$<span class="precio">${precioOriginal.toFixed(2)}</span>`;
    fila.classList.remove("editando");

    boton.textContent = "✏️";
    boton.classList.remove("guardar-edicion");
    boton.classList.add("editar");
    boton.title = "Editar";
}

function actualizarFila(fila) {
    if (fila.classList.contains("editando")) return;
    const cantidad = parseInt(fila.querySelector(".cant").textContent);
    const precio   = parseFloat(fila.querySelector(".precio").textContent);
    fila.querySelector(".total").textContent = (cantidad * precio).toFixed(2);
    fila.classList.toggle("stock-bajo", cantidad === 0);
}

function recalcularTotal() {
    let suma = 0;
    document.querySelectorAll(".total").forEach(elemento => suma += parseFloat(elemento.textContent) || 0);
    totalGeneral.textContent = "Total general: $" + suma.toFixed(2);
}
