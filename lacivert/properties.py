import os


ROOT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)))

DEPENDENCIES_PATH: str = os.path.join(ROOT_DIR, "dependencies")

ZEMBEREK_PATH: str = os.path.join(DEPENDENCIES_PATH, "zemberek-nlp", "zemberek-full.jar")


ENCODING: str = "utf-8"
