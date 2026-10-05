#!/usr/bin/env python3
"""Convierte el dump PostgreSQL de sepomex-db-postgresql a datos MySQL locales.

Uso:
    python3 database/tools/convert_sepomex_postgres_to_mysql.py \
      /tmp/sepomex_postgresql.sql database/sepomex_data.sql
"""

from __future__ import annotations

import sys
from pathlib import Path


COPY_PREFIXES = {
    "asentamiento_tipo": "COPY public.asentamiento_tipo ",
    "ciudad": "COPY public.ciudad ",
    "codigo_postal": "COPY public.codigo_postal ",
    "colonia": "COPY public.colonia ",
    "estado": "COPY public.estado ",
    "municipio": "COPY public.municipio ",
}


def sql(value: str | None) -> str:
    if value is None or value == "":
        return "NULL"
    return "'" + value.replace("\\", "\\\\").replace("'", "''") + "'"


def read_copy_tables(path: Path) -> dict[str, list[list[str | None]]]:
    tables: dict[str, list[list[str | None]]] = {name: [] for name in COPY_PREFIXES}
    active: str | None = None

    with path.open("r", encoding="utf-8") as source:
        for raw_line in source:
            line = raw_line.rstrip("\n")
            if active:
                if line == r"\.":
                    active = None
                    continue
                tables[active].append([None if value == r"\N" else value for value in line.split("\t")])
                continue

            for name, prefix in COPY_PREFIXES.items():
                if line.startswith(prefix):
                    active = name
                    break

    return tables


def dict_by_id(rows: list[list[str | None]]) -> dict[int, list[str | None]]:
    return {int(row[0] or 0): row for row in rows}


def write_insert_batch(output, values: list[str]) -> None:
    output.write("INSERT INTO postal_settlements (\n")
    output.write("  id, source_settlement_id, postal_code, settlement_name, settlement_type,\n")
    output.write("  municipality_name, state_name, city_name, municipal_identifier\n")
    output.write(") VALUES\n")
    output.write(",\n".join(values))
    output.write(";\n\n")


def main() -> int:
    if len(sys.argv) != 3:
        print("Uso: convert_sepomex_postgres_to_mysql.py <postgres.sql> <mysql.sql>", file=sys.stderr)
        return 2

    source = Path(sys.argv[1])
    target = Path(sys.argv[2])
    tables = read_copy_tables(source)

    settlement_types = dict_by_id(tables["asentamiento_tipo"])
    cities = dict_by_id(tables["ciudad"])
    postal_codes = dict_by_id(tables["codigo_postal"])
    states = dict_by_id(tables["estado"])
    municipalities = dict_by_id(tables["municipio"])
    colonies = tables["colonia"]

    target.parent.mkdir(parents=True, exist_ok=True)
    with target.open("w", encoding="utf-8") as output:
        output.write("-- Catalogo SEPOMEX local convertido desde sepomex-db-postgresql.\n")
        output.write("-- Fuente: https://github.com/ripper2hl/sepomex-db-postgresql\n")
        output.write("USE taller_db;\n\n")
        output.write("TRUNCATE TABLE postal_settlements;\n\n")

        values: list[str] = []
        generated_id = 1
        for colony in colonies:
            source_settlement_id = int(colony[0] or 0)
            municipal_identifier = colony[1] or ""
            settlement_name = colony[2] or ""
            settlement_type = settlement_types.get(int(colony[3] or 0), ["", ""])[1] or ""
            city = cities.get(int(colony[4] or 0)) if colony[4] else None
            postal_code = postal_codes.get(int(colony[5] or 0), ["", ""])
            state = states.get(int(colony[8] or 0), ["", "", ""])
            municipality = municipalities.get(int(colony[11] or 0), ["", ""])

            values.append(
                "("
                f"{generated_id}, "
                f"{source_settlement_id}, "
                f"{sql(postal_code[1] if len(postal_code) > 1 else '')}, "
                f"{sql(settlement_name)}, "
                f"{sql(settlement_type)}, "
                f"{sql(municipality[1] if len(municipality) > 1 else '')}, "
                f"{sql(state[2] if len(state) > 2 else '')}, "
                f"{sql(city[1] if city and len(city) > 1 else None)}, "
                f"{sql(municipal_identifier)}"
                ")"
            )
            generated_id += 1

            if len(values) == 500:
                write_insert_batch(output, values)
                values = []

        if values:
            write_insert_batch(output, values)

    print(f"Generado {target} con {generated_id - 1} asentamientos.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
