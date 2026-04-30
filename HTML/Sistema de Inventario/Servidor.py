from http.server import BaseHTTPRequestHandler, HTTPServer
import json
import os

DIR_BASE  = os.path.dirname(os.path.abspath(__file__))
RUTA_JSON = os.path.join(DIR_BASE, 'inventario.json')

class ManejadorServidor(BaseHTTPRequestHandler):
    def _establecer_cabeceras(self):
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        self.end_headers()

    def do_OPTIONS(self):
        self._establecer_cabeceras()

    def do_POST(self):
        longitud_contenido = int(self.headers['Content-Length'])
        datos_post         = self.rfile.read(longitud_contenido)
        productos          = json.loads(datos_post)

        with open(RUTA_JSON, 'w', encoding='utf-8') as archivo:
            json.dump(productos, archivo, indent=4, ensure_ascii=False)

        self._establecer_cabeceras()
        print(f"Inventario actualizado en: {RUTA_JSON}")

def ejecutar(puerto=8000):
    direccion_servidor = ('', puerto)
    servidor_http      = HTTPServer(direccion_servidor, ManejadorServidor)
    print(f'Servidor activo en http://localhost:{puerto} - Esperando cambios...')
    servidor_http.serve_forever()

if __name__ == "__main__":
    ejecutar()
