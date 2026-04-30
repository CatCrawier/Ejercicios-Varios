import http.server
import json
import os
import re
import time
import mimetypes
from datetime import datetime, timezone

PUERTO      = 3000
ARCHIVO_JSON = os.path.join(os.path.dirname(os.path.abspath(__file__)), "usuarios.json")

DOMINIOS_VALIDOS = ['gmail.com', 'outlook.com', 'hotmail.com', 'yahoo.com', 'live.com', 'icloud.com', 'protonmail.com', 'mail.com']

def validar_formato_correo(correo):
    patron = r'^[^\s@]+@[^\s@]+\.[^\s@]+$'
    return bool(re.match(patron, correo))

def validar_dominio_correo(correo):
    partes = correo.split('@')
    if len(partes) != 2:
        return False
    dominio = partes[1].lower()
    return dominio in DOMINIOS_VALIDOS

def validar_contrasena(contrasena):
    if len(contrasena) < 8:
        return False, 'La contraseña debe tener al menos 8 caracteres.'
    if not re.search(r'[a-z]', contrasena):
        return False, 'La contraseña debe contener al menos una letra minúscula.'
    if not re.search(r'[A-Z]', contrasena):
        return False, 'La contraseña debe contener al menos una letra mayúscula.'
    if not re.search(r'[0-9]', contrasena):
        return False, 'La contraseña debe contener al menos un número.'
    if not re.search(r'[!@#$%^&*()_+\-=\[\]{};\':"\\|,.<>\/?]', contrasena):
        return False, 'La contraseña debe contener al menos un símbolo especial.'
    return True, ''

def leer_usuarios():
    if not os.path.exists(ARCHIVO_JSON):
        with open(ARCHIVO_JSON, "w", encoding="utf-8") as archivo:
            json.dump([], archivo, indent=2, ensure_ascii=False)
    with open(ARCHIVO_JSON, "r", encoding="utf-8") as archivo:
        return json.load(archivo)

def guardar_usuarios(usuarios):
    with open(ARCHIVO_JSON, "w", encoding="utf-8") as archivo:
        json.dump(usuarios, archivo, indent=2, ensure_ascii=False)

def ahora_iso():
    return datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S.000Z")

class Manejador(http.server.BaseHTTPRequestHandler):

    def log_message(self, formato, *args):
        pass

    def _cabeceras_cors(self, estado=200, tipo_contenido="application/json"):
        self.send_response(estado)
        self.send_header("Content-Type", tipo_contenido)
        self.send_header("Access-Control-Allow-Origin",  "*")
        self.send_header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.end_headers()

    def _responder_json(self, estado, datos):
        cuerpo = json.dumps(datos, ensure_ascii=False).encode("utf-8")
        self._cabeceras_cors(estado, "application/json")
        self.wfile.write(cuerpo)

    def _leer_cuerpo(self):
        longitud = int(self.headers.get("Content-Length", 0))
        if longitud == 0:
            return {}
        return json.loads(self.rfile.read(longitud).decode("utf-8"))

    def do_OPTIONS(self):
        self._cabeceras_cors(204)

    def do_GET(self):
        ruta_url = self.path.split("?")[0]

        if ruta_url == "/api/usuarios":
            self._responder_json(200, leer_usuarios())
            return

        if ruta_url == "/":
            ruta_url = "/Registro_Login.html"

        directorio = os.path.dirname(os.path.abspath(__file__))
        ruta_disco = os.path.normpath(os.path.join(directorio, ruta_url.lstrip("/")))

        if not ruta_disco.startswith(directorio):
            self._responder_json(403, {"error": "Acceso denegado."})
            return

        if os.path.isfile(ruta_disco):
            mime, _ = mimetypes.guess_type(ruta_disco)
            mime = mime or "text/plain"
            with open(ruta_disco, "rb") as archivo:
                contenido = archivo.read()
            self._cabeceras_cors(200, mime)
            self.wfile.write(contenido)
        else:
            self._responder_json(404, {"error": "Archivo no encontrado."})

    def do_POST(self):
        ruta_url = self.path.split("?")[0]

        if ruta_url == "/api/registro":
            datos    = self._leer_cuerpo()
            usuario  = datos.get("usuario", "").strip()
            correo   = datos.get("correo",   "").strip()
            contrasena = datos.get("contrasena", "")

            if not usuario or not correo or not contrasena:
                self._responder_json(400, {"error": "Faltan campos."})
                return

            if not validar_formato_correo(correo):
                self._responder_json(400, {"error": "El correo electrónico no tiene un formato válido."})
                return

            if not validar_dominio_correo(correo):
                self._responder_json(400, {"error": "El correo debe ser de un proveedor válido (Gmail, Outlook, Hotmail, Yahoo, etc.)."})
                return

            contrasena_valida, mensaje_error = validar_contrasena(contrasena)
            if not contrasena_valida:
                self._responder_json(400, {"error": mensaje_error})
                return

            usuarios = leer_usuarios()
            if any(u["correo"] == correo for u in usuarios):
                self._responder_json(409, {"error": "El correo ya está registrado."})
                return

            nuevo = {
                "id":        int(time.time() * 1000),
                "usuario":   usuario,
                "correo":    correo,
                "contrasena": contrasena,
                "creadoEn":  ahora_iso()
            }
            usuarios.append(nuevo)
            guardar_usuarios(usuarios)
            print(f"  [+] Nuevo usuario registrado: {usuario} <{correo}>")
            self._responder_json(201, {"mensaje": "Registro exitoso.", "usuario": nuevo})
            return

        if ruta_url == "/api/login":
            datos      = self._leer_cuerpo()
            correo     = datos.get("correo",     "").strip()
            contrasena = datos.get("contrasena", "")

            usuarios   = leer_usuarios()
            encontrado = next(
                (u for u in usuarios if u["correo"] == correo and u["contrasena"] == contrasena),
                None
            )

            if not encontrado:
                self._responder_json(401, {"error": "Credenciales incorrectas."})
                return

            print(f"  [→] Login exitoso: {encontrado['usuario']}")
            self._responder_json(200, {"mensaje": "Login exitoso.", "usuario": encontrado})
            return

        self._responder_json(404, {"error": "Ruta no encontrada."})

    def do_PUT(self):
        ruta_url = self.path.split("?")[0]

        partes = ruta_url.strip("/").split("/")
        if len(partes) == 3 and partes[0] == "api" and partes[1] == "usuarios":
            try:
                uid = int(partes[2])
            except ValueError:
                self._responder_json(400, {"error": "ID inválido."})
                return

            cambios  = self._leer_cuerpo()
            usuarios = leer_usuarios()
            indice   = next((i for i, u in enumerate(usuarios) if u["id"] == uid), -1)

            if indice == -1:
                self._responder_json(404, {"error": "Usuario no encontrado."})
                return

            nuevo_correo = cambios.get("correo", "").strip()
            if nuevo_correo and nuevo_correo != usuarios[indice]["correo"]:
                if not validar_formato_correo(nuevo_correo):
                    self._responder_json(400, {"error": "El correo electrónico no tiene un formato válido."})
                    return
                if not validar_dominio_correo(nuevo_correo):
                    self._responder_json(400, {"error": "El correo debe ser de un proveedor válido (Gmail, Outlook, Hotmail, Yahoo, etc.)."})
                    return
                if any(u["correo"] == nuevo_correo for u in usuarios):
                    self._responder_json(409, {"error": "Ese correo ya está en uso."})
                    return

            nueva_contrasena = cambios.get("contrasena", "")
            if nueva_contrasena:
                contrasena_valida, mensaje_error = validar_contrasena(nueva_contrasena)
                if not contrasena_valida:
                    self._responder_json(400, {"error": mensaje_error})
                    return

            if cambios.get("usuario"):    usuarios[indice]["usuario"]    = cambios["usuario"].strip()
            if nuevo_correo:              usuarios[indice]["correo"]     = nuevo_correo
            if nueva_contrasena:          usuarios[indice]["contrasena"] = nueva_contrasena
            usuarios[indice]["actualizadoEn"] = ahora_iso()

            guardar_usuarios(usuarios)
            print(f"  [✎] Usuario actualizado: {usuarios[indice]['usuario']} <{usuarios[indice]['correo']}>")
            self._responder_json(200, {"mensaje": "Datos actualizados.", "usuario": usuarios[indice]})
            return

        self._responder_json(404, {"error": "Ruta no encontrada."})

    def do_DELETE(self):
        ruta_url = self.path.split("?")[0]

        partes = ruta_url.strip("/").split("/")
        if len(partes) == 3 and partes[0] == "api" and partes[1] == "usuarios":
            try:
                uid = int(partes[2])
            except ValueError:
                self._responder_json(400, {"error": "ID inválido."})
                return

            usuarios = leer_usuarios()
            indice = next((i for i, u in enumerate(usuarios) if u["id"] == uid), -1)

            if indice == -1:
                self._responder_json(404, {"error": "Usuario no encontrado."})
                return

            usuario_eliminado = usuarios.pop(indice)
            guardar_usuarios(usuarios)
            print(f"  [✗] Usuario eliminado: {usuario_eliminado['usuario']} <{usuario_eliminado['correo']}>")
            self._responder_json(200, {"mensaje": "Cuenta eliminada correctamente.", "usuario": usuario_eliminado})
            return

        self._responder_json(404, {"error": "Ruta no encontrada."})

if __name__ == "__main__":
    with http.server.HTTPServer(("", PUERTO), Manejador) as httpd:
        print(f"\n✅  Servidor corriendo en http://localhost:{PUERTO}")
        print(f"📁  Usuarios guardados en: {ARCHIVO_JSON}\n")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n⛔  Servidor detenido.")