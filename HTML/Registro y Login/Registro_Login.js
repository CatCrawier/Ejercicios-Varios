function alternarFormulario() {
    document.getElementById('formulario-login').classList.toggle('oculto');
    document.getElementById('formulario-registro').classList.toggle('oculto');
}

document.getElementById('autenticacion-registro').addEventListener('submit', (evento) => {
    evento.preventDefault();

    const usuario    = document.getElementById('registro-usuario').value;
    const correo     = document.getElementById('registro-correo').value;
    const contrasena = document.getElementById('registro-contrasena').value;

    const usuarios = JSON.parse(localStorage.getItem('usuarios')) || [];

    if (usuarios.find(u => u.correo === correo)) {
        alert("El correo ya está registrado.");
        return;
    }

    usuarios.push({ usuario, correo, contrasena });
    localStorage.setItem('usuarios', JSON.stringify(usuarios));

    alert("Registro exitoso. Ahora puedes iniciar sesión.");
    alternarFormulario();
});

document.getElementById('autenticacion-login').addEventListener('submit', (evento) => {
    evento.preventDefault();

    const correo     = document.getElementById('login-correo').value;
    const contrasena = document.getElementById('login-contrasena').value;

    const usuarios = JSON.parse(localStorage.getItem('usuarios')) || [];

    const usuarioValido = usuarios.find(u => u.correo === correo && u.contrasena === contrasena);

    if (usuarioValido) {
        mostrarBienvenida(usuarioValido.usuario);
    } else {
        alert("Credenciales incorrectas.");
    }
});

function mostrarBienvenida(nombre) {
    document.getElementById('formulario-login').classList.add('oculto');
    document.getElementById('formulario-registro').classList.add('oculto');
    document.getElementById('pantalla-bienvenida').classList.remove('oculto');
    document.getElementById('mensaje-bienvenida').innerText = `¡Bienvenido, ${nombre}!`;
}

function cerrarSesion() {
    document.getElementById('pantalla-bienvenida').classList.add('oculto');
    document.getElementById('formulario-login').classList.remove('oculto');
    document.getElementById('autenticacion-login').reset();
}
