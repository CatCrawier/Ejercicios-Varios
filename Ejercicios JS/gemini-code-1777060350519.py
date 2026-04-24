import os
import subprocess
import requests

def clone_all_js_repos(org):
    page = 1
    total_cloned = 0
    
    print(f"Iniciando la búsqueda de repositorios en {org}...")

    while True:
        # Cambiamos "orgs" por "users"
        url = f"https://api.github.com/users/{org}/repos?per_page=100&page={page}"
        response = requests.get(url)
        
        # Verificación de errores de conexión o límites de API
        if response.status_code != 200:
            print(f"Error al conectar con la API: {response.status_code}")
            break
            
        repos = response.json()
        
        # Si la respuesta está vacía, ya recorrimos todas las páginas
        if not repos:
            break
            
        for repo in repos:
            # Filtramos estrictamente por JavaScript
            if repo['language'] == 'JavaScript':
                repo_name = repo['name']
                clone_url = repo['clone_url']
                
                # Verificamos si ya existe para evitar errores
                if os.path.exists(repo_name):
                    print(f"Saltando '{repo_name}': ya existe en la carpeta.")
                else:
                    print(f"Clonando '{repo_name}'...")
                    try:
                        subprocess.run(["git", "clone", clone_url], check=True)
                        total_cloned += 1
                    except subprocess.CalledProcessError:
                        print(f"Error al clonar '{repo_name}'.")
        
        # Pasamos a la siguiente página
        page += 1
        
    print(f"\nProceso finalizado. Se han clonado {total_cloned} nuevos repositorios.")

if __name__ == "__main__":
    # Nombre de la organización
    ORGANIZACION = "CodinGitHub"
    clone_all_js_repos(ORGANIZACION)