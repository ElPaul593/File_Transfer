#!/usr/bin/env python3
"""Comprobacion final del archivo que Camel haya creado (requiere ejecutar la ruta)."""
from pathlib import Path
import sys
ROOT = Path(__file__).resolve().parents[1]
EXPECTED = (ROOT / "ejemplos" / "ventas-transformadas-esperado.csv").read_text(encoding="utf-8")
checks = []
for folder in ("output", "archived"):
    path = ROOT / folder / "ventas.csv"
    if not path.is_file():
        checks.append((False, f"FALTA {path.relative_to(ROOT)}: primero ejecuta mvn exec:java"))
    else:
        actual = path.read_text(encoding="utf-8-sig")
        checks.append((actual.strip() == EXPECTED.strip(),
                       f"{folder}/ventas.csv coincide con el esperado"))
source_removed = not (ROOT / 'input' / 'ventas.csv').exists()
checks.append((source_removed, "input/ventas.csv se retira tras el procesamiento exitoso"))
for ok, desc in checks:
    print(("[OK] " if ok else "[ERROR] ") + desc)
if all(ok for ok, _ in checks):
    print("RESULTADO: VALIDACION CORRECTA DEL FLUJO")
    sys.exit(0)
print("RESULTADO: INCOMPLETO O INCORRECTO (asegura que Camel termino de procesar)")
sys.exit(1)
