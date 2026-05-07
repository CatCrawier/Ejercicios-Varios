const API = 'http://localhost:3000/api';

let usuarioActual = null;

function validarContrasena(contrasena) {
    if (contrasena.length < 8) {
        return { valido: false, mensaje: 'La contraseña debe tener al menos 8 caracteres.' };
    }
    if (!/[a-z]/.test(contrasena)) {
        return { valido: false, mensaje: 'La contraseña debe contener al menos una letra minúscula.' };
    }
    if (!/[A-Z]/.test(contrasena)) {
        return { valido: false, mensaje: 'La contraseña debe contener al menos una letra mayúscula.' };
    }
    if (!/[0-9]/.test(contrasena)) {
        return { valido: false, mensaje: 'La contraseña debe contener al menos un número.' };
    }
    if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(contrasena)) {
        return { valido: false, mensaje: 'La contraseña debe contener al menos un símbolo especial (!@#$%^&*...).' };
    }
    return { valido: true, mensaje: '' };
}

function validarCorreo(correo) {
    const patronCorreo = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!patronCorreo.test(correo)) {
        return { valido: false, mensaje: 'El correo electrónico no tiene un formato válido.' };
    }
    const dominiosValidos = ['gmail.com', 'outlook.com', 'hotmail.com', 'yahoo.com', 'live.com', 'icloud.com', 'protonmail.com', 'mail.com'];
    const dominio = correo.split('@')[1].toLowerCase();
    if (!dominiosValidos.includes(dominio)) {
        return { valido: false, mensaje: 'El correo debe ser de un proveedor válido (Gmail, Outlook, Hotmail, Yahoo, etc.).' };
    }
    return { valido: true, mensaje: '' };
}

function mostrarSolo(idPanel) {
    ['formulario-inicio-sesion', 'formulario-registro', 'pantalla-bienvenida', 'formulario-edicion', 'confirmacion-eliminacion']
        .forEach(id => {
            const elemento = document.getElementById(id);
            if (elemento) elemento.classList.add('oculto');
        });
    const panel = document.getElementById(idPanel);
    if (panel) panel.classList.remove('oculto');
}

function alternarFormulario() {
    const inicioSesionOculto = document.getElementById('formulario-inicio-sesion').classList.contains('oculto');
    mostrarSolo(inicioSesionOculto ? 'formulario-inicio-sesion' : 'formulario-registro');
}

function volverABienvenida() {
    mostrarSolo('pantalla-bienvenida');
}

document.getElementById('autenticacion-registro').addEventListener('submit', async (evento) => {
    evento.preventDefault();

    const usuario    = document.getElementById('registro-usuario').value.trim();
    const correo     = document.getElementById('registro-correo').value.trim();
    const contrasena = document.getElementById('registro-contrasena').value;

    const validacionCorreo = validarCorreo(correo);
    if (!validacionCorreo.valido) {
        alert(validacionCorreo.mensaje);
        return;
    }

    const validacionContrasena = validarContrasena(contrasena);
    if (!validacionContrasena.valido) {
        alert(validacionContrasena.mensaje);
        return;
    }

    try {
        const respuesta = await fetch(`${API}/registro`, {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify({ usuario, correo, contrasena })
        });
        const datos = await respuesta.json();

        if (!respuesta.ok) {
            alert(datos.error || 'Error al registrarse.');
            return;
        }

        alert('✅ Registro exitoso. Ahora puedes iniciar sesión.');
        document.getElementById('autenticacion-registro').reset();
        mostrarSolo('formulario-inicio-sesion');

    } catch {
        alert('❌ No se pudo conectar con el servidor.\nAsegúrate de que server.py esté corriendo.');
    }
});

document.getElementById('autenticacion-inicio-sesion').addEventListener('submit', async (evento) => {
    evento.preventDefault();

    const correo     = document.getElementById('inicio-sesion-correo').value.trim();
    const contrasena = document.getElementById('inicio-sesion-contrasena').value;

    try {
        const respuesta = await fetch(`${API}/login`, {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify({ correo, contrasena })
        });
        const datos = await respuesta.json();

        if (!respuesta.ok) {
            alert(datos.error || 'Credenciales incorrectas.');
            return;
        }

        usuarioActual = datos.usuario;
        mostrarBienvenida(usuarioActual.usuario);

    } catch {
        alert('❌ No se pudo conectar con el servidor.\nAsegúrate de que server.py esté corriendo.');
    }
});

function mostrarBienvenida(nombre) {
    mostrarSolo('pantalla-bienvenida');
    document.getElementById('mensaje-bienvenida').innerText = `¡Bienvenido, ${nombre}!`;
}

function cerrarSesion() {
    usuarioActual = null;
    document.getElementById('autenticacion-inicio-sesion').reset();
    mostrarSolo('formulario-inicio-sesion');
}

function mostrarEdicion() {
    if (!usuarioActual) return;
    document.getElementById('edicion-usuario').value    = usuarioActual.usuario;
    document.getElementById('edicion-correo').value     = usuarioActual.correo;
    document.getElementById('edicion-contrasena').value = '';
    mostrarSolo('formulario-edicion');
}

document.getElementById('autenticacion-edicion').addEventListener('submit', async (evento) => {
    evento.preventDefault();

    const nuevoUsuario    = document.getElementById('edicion-usuario').value.trim();
    const nuevoCorreo     = document.getElementById('edicion-correo').value.trim();
    const nuevaContrasena = document.getElementById('edicion-contrasena').value;

    if (nuevoCorreo && nuevoCorreo !== usuarioActual.correo) {
        const validacionCorreo = validarCorreo(nuevoCorreo);
        if (!validacionCorreo.valido) {
            alert(validacionCorreo.mensaje);
            return;
        }
    }

    const cambios = {};
    if (nuevoUsuario    && nuevoUsuario    !== usuarioActual.usuario)    cambios.usuario    = nuevoUsuario;
    if (nuevoCorreo     && nuevoCorreo     !== usuarioActual.correo)     cambios.correo     = nuevoCorreo;
    if (nuevaContrasena) {
        const validacionContrasena = validarContrasena(nuevaContrasena);
        if (!validacionContrasena.valido) {
            alert(validacionContrasena.mensaje);
            return;
        }
        cambios.contrasena = nuevaContrasena;
    }

    if (Object.keys(cambios).length === 0) {
        alert('No se detectaron cambios.');
        return;
    }

    try {
        const respuesta = await fetch(`${API}/usuarios/${usuarioActual.id}`, {
            method:  'PUT',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify(cambios)
        });
        const datos = await respuesta.json();

        if (!respuesta.ok) {
            alert(datos.error || 'Error al actualizar.');
            return;
        }

        usuarioActual = datos.usuario;
        alert('✅ Datos actualizados correctamente.\nLos cambios se guardaron en usuarios.json.');
        mostrarBienvenida(usuarioActual.usuario);

    } catch {
        alert('❌ No se pudo conectar con el servidor.');
    }
});

function mostrarConfirmacionEliminacion() {
    if (!usuarioActual) return;
    mostrarSolo('confirmacion-eliminacion');
}

document.getElementById('confirmar-eliminacion').addEventListener('click', async () => {
    if (!usuarioActual) return;

    const confirmacion = confirm('¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.');
    if (!confirmacion) return;

    try {
        const respuesta = await fetch(`${API}/usuarios/${usuarioActual.id}`, {
            method: 'DELETE'
        });
        const datos = await respuesta.json();

        if (!respuesta.ok) {
            alert(datos.error || 'Error al eliminar la cuenta.');
            return;
        }

        alert('✅ Cuenta eliminada correctamente.');
        usuarioActual = null;
        mostrarSolo('formulario-inicio-sesion');

    } catch {
        alert('❌ No se pudo conectar con el servidor.');
    }
});

document.getElementById('cancelar-eliminacion').addEventListener('click', () => {
    if (usuarioActual) {
        mostrarBienvenida(usuarioActual.usuario);
    } else {
        mostrarSolo('formulario-inicio-sesion');
    }
});