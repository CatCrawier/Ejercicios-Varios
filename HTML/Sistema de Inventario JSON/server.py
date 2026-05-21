from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
import json
import os
import sys

ROOT = Path(__file__).resolve().parent
DB_FILE = ROOT / "data" / "inventario.json"
PORT = int(sys.argv[1] if len(sys.argv) > 1 else os.environ.get("PORT", "5502"))

class InventoryHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(ROOT), **kwargs)

    def end_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, PUT, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        super().end_headers()

    def do_OPTIONS(self):
        self.send_response(204)
        self.end_headers()

    def do_GET(self):
        if self.path.split("?", 1)[0] == "/api/db":
            self.send_json_file()
            return

        super().do_GET()

    def do_PUT(self):
        if self.path.split("?", 1)[0] != "/api/db":
            self.send_error(405, "Metodo no permitido")
            return

        try:
            content_length = int(self.headers.get("Content-Length", "0"))
            raw_body = self.rfile.read(content_length).decode("utf-8")
            data = json.loads(raw_body)

            DB_FILE.parent.mkdir(parents=True, exist_ok=True)
            DB_FILE.write_text(
                json.dumps(data, ensure_ascii=False, indent=2),
                encoding="utf-8",
            )

            self.send_json({"ok": True})
        except Exception as error:
            self.send_json({"error": str(error)}, status=400)

    def send_json_file(self):
        try:
            body = DB_FILE.read_text(encoding="utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.end_headers()
            self.wfile.write(body.encode("utf-8"))
        except Exception as error:
            self.send_json({"error": str(error)}, status=500)

    def send_json(self, data, status=200):
        body = json.dumps(data, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

if __name__ == "__main__":
    server = ThreadingHTTPServer(("localhost", PORT), InventoryHandler)
    print(f"Inventario JSON disponible en http://localhost:{PORT}/")
    print(f"Datos visibles en {DB_FILE}")
    server.serve_forever()
