"""
Size chart normalizer for extracted tables and OCR output.
Converts inches to centimetres and tags BODY vs GARMENT measurement basis.
"""
from typing import Dict, List, Any

CM_PER_INCH = 2.54


def normalize_chart_rows(rows: List[Dict[str, Any]], unit: str = "CM", basis: str = "BODY") -> List[Dict[str, Any]]:
    factor = CM_PER_INCH if unit.upper() in ("IN", "INCH", "INCHES") else 1.0
    normalized = []
    for row in rows:
        measurements = []
        for dim, bounds in row.get("dimensions", {}).items():
            min_v = round(float(bounds["min"]) * factor, 2)
            max_v = round(float(bounds.get("max", bounds["min"])) * factor, 2)
            measurements.append({
                "dimension": dim.upper(),
                "minValueCm": min_v,
                "maxValueCm": max_v,
                "measurementBasis": basis.upper(),
            })
        normalized.append({
            "sizeLabel": str(row["sizeLabel"]).upper(),
            "measurements": measurements,
        })
    return normalized
