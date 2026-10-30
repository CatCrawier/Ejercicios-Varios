"""Recorta los sprites esenciales de Pac-Man usando cajas explícitas.

Ejecutar desde la raíz del proyecto o desde cualquier otra carpeta:
    python Python/Recortar_Sprites.py

Cada caja de SPRITE_BOXES sigue el formato (izquierda, arriba, derecha, abajo).
Las coordenadas son píxeles de la hoja original; no se presupone una cuadrícula
común porque los grupos del atlas están distribuidos de forma independiente.
"""

from pathlib import Path
from typing import Dict, Tuple

from PIL import Image


PROJECT_DIR = Path(__file__).resolve().parent.parent
SOURCE_IMAGE = (
    PROJECT_DIR
    / "Aplicaciones"
    / "Pacman"
    / "app"
    / "src"
    / "main"
    / "res"
    / "drawable"
    / "sprites.png"
)
OUTPUT_DIR = PROJECT_DIR / "sprites_pacman"

# (izquierda, arriba, derecha, abajo); el límite derecho e inferior es exclusivo,
# tal como lo espera Image.crop de Pillow.
SPRITE_BOXES: Dict[str, Tuple[int, int, int, int]] = {
    # Animación de boca de Pac-Man orientada a la derecha.
    "pacman_right_1": (457, 1, 466, 14),
    "pacman_right_2": (473, 1, 485, 14),
    "pacman_right_3": (489, 1, 502, 14),
    # Pac-Man hacia la izquierda. Las cajas cubren las celdas originales
    # (x=456, 472, 488); el dibujo está desplazado dentro de cada celda.
    "pacman_left_1": (456, 16, 472, 32),
    "pacman_left_2": (472, 16, 488, 32),
    "pacman_left_3": (488, 16, 504, 32),
    # Las filas señaladas como arriba y abajo en sprites.png.
    "pacman_up_1": (456, 32, 472, 48),
    "pacman_up_2": (472, 32, 488, 48),
    "pacman_up_3": (488, 32, 504, 48),
    "pacman_down_1": (456, 48, 472, 64),
    "pacman_down_2": (472, 48, 488, 64),
    "pacman_down_3": (488, 48, 504, 64),
    # Secuencia de muerte: fotogramas consecutivos de la franja superior,
    # desde x=504 hasta el pequeño destello final a la derecha.
    "pacman_death_1": (504, 0, 520, 16),
    "pacman_death_2": (520, 0, 536, 16),
    "pacman_death_3": (536, 0, 552, 16),
    "pacman_death_4": (552, 0, 568, 16),
    "pacman_death_5": (568, 0, 584, 16),
    "pacman_death_6": (584, 0, 600, 16),
    "pacman_death_7": (600, 0, 616, 16),
    "pacman_death_8": (616, 0, 632, 16),
    "pacman_death_9": (632, 0, 648, 16),
    "pacman_death_10": (648, 0, 664, 16),
    "pacman_death_11": (664, 0, 680, 16),

    # Cada franja de color contiene ocho celdas. Dos fotogramas consecutivos
    # muestran cada dirección: derecha, izquierda, arriba y abajo.
    # Blinky (rojo).
    "ghost_blinky_right": (456, 64, 472, 80),
    "ghost_blinky_right_2": (472, 64, 488, 80),
    "ghost_blinky_left": (488, 64, 504, 80),
    "ghost_blinky_left_2": (504, 64, 520, 80),
    "ghost_blinky_up": (520, 64, 536, 80),
    "ghost_blinky_up_2": (536, 64, 552, 80),
    "ghost_blinky_down": (552, 64, 568, 80),
    "ghost_blinky_down_2": (568, 64, 584, 80),
    # Pinky (rosa).
    "ghost_pinky_right": (456, 80, 472, 96),
    "ghost_pinky_right_2": (472, 80, 488, 96),
    "ghost_pinky_left": (488, 80, 504, 96),
    "ghost_pinky_left_2": (504, 80, 520, 96),
    "ghost_pinky_up": (520, 80, 536, 96),
    "ghost_pinky_up_2": (536, 80, 552, 96),
    "ghost_pinky_down": (552, 80, 568, 96),
    "ghost_pinky_down_2": (568, 80, 584, 96),
    # Inky (cian).
    "ghost_inky_right": (456, 96, 472, 112),
    "ghost_inky_right_2": (472, 96, 488, 112),
    "ghost_inky_left": (488, 96, 504, 112),
    "ghost_inky_left_2": (504, 96, 520, 112),
    "ghost_inky_up": (520, 96, 536, 112),
    "ghost_inky_up_2": (536, 96, 552, 112),
    "ghost_inky_down": (552, 96, 568, 112),
    "ghost_inky_down_2": (568, 96, 584, 112),
    # Clyde (naranja).
    "ghost_clyde_right": (456, 112, 472, 128),
    "ghost_clyde_right_2": (472, 112, 488, 128),
    "ghost_clyde_left": (488, 112, 504, 128),
    "ghost_clyde_left_2": (504, 112, 520, 128),
    "ghost_clyde_up": (520, 112, 536, 128),
    "ghost_clyde_up_2": (536, 112, 552, 128),
    "ghost_clyde_down": (552, 112, 568, 128),
    "ghost_clyde_down_2": (568, 112, 584, 128),
    "ghost_scared_blue_1": (584, 64, 600, 80),
    "ghost_scared_blue_2": (600, 64, 616, 80),
    "ghost_scared_white_1": (616, 64, 632, 80),
    "ghost_scared_white_2": (632, 64, 648, 80),
    # Ojos sueltos para el estado en que los fantasmas han sido comidos.
    "ghost_eyes_right": (588, 84, 598, 89),
    "ghost_eyes_left": (602, 84, 612, 89),
    "ghost_eyes_up": (619, 82, 629, 87),
    "ghost_eyes_down": (635, 85, 645, 90),

    # Puntuaciones del atlas, recortadas por valor.
    "score_200": (456, 132, 471, 139),
    "score_400": (472, 132, 487, 139),
    "score_800": (488, 132, 503, 139),
    "score_1600": (504, 132, 520, 139),
    "score_100": (458, 148, 473, 155),
    "score_300": (473, 148, 487, 155),
    "score_500": (488, 148, 503, 155),
    "score_700": (504, 148, 519, 155),
    "score_1000": (520, 148, 538, 155),
    "score_2000": (518, 164, 538, 171),
    "score_3000": (518, 180, 538, 187),
    "score_5000": (518, 196, 538, 203),

    # Los dos tableros ocupan paneles separados de 224x248 píxeles. El borde
    # inferior llega hasta la última fila de la imagen, y antes se recortaba.
    "map_left": (0, 0, 224, 248),
    "map_right": (228, 0, 452, 248),

    # Frutas y llave en la fila de bonificaciones del atlas.
    "fruit_cherry": (504, 48, 520, 64),
    "fruit_strawberry": (520, 48, 536, 64),
    "fruit_orange": (536, 48, 552, 64),
    "fruit_apple": (552, 48, 568, 64),
    "fruit_melon": (568, 48, 584, 64),
    "fruit_galaxian": (584, 48, 600, 64),
    "fruit_bell": (600, 48, 616, 64),
    "door_key": (616, 48, 632, 64),
}


def main() -> None:
    if not SOURCE_IMAGE.is_file():
        raise FileNotFoundError(f"No se encontró la hoja de sprites: {SOURCE_IMAGE}")

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    with Image.open(SOURCE_IMAGE) as image:
        source = image.convert("RGBA")

    for sprite_name, box in SPRITE_BOXES.items():
        left, top, right, bottom = box
        if not (0 <= left < right <= source.width and 0 <= top < bottom <= source.height):
            raise ValueError(
                f"Caja inválida para {sprite_name}: {box}; "
                f"la imagen mide {source.width}x{source.height}."
            )

        output_path = OUTPUT_DIR / f"{sprite_name}.png"
        source.crop(box).save(output_path, format="PNG")
        print(f"Guardado correctamente: {output_path}")

    print(f"Listo: {len(SPRITE_BOXES)} sprites guardados en {OUTPUT_DIR}")


if __name__ == "__main__":
    main()
