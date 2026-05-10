"""Reusable model for CUMCM 2025 problem A smoke screening tests.

The implementation is intentionally deterministic: all scripts use the same
kinematics, target sampling, time step integration, and interval union logic.
"""

from __future__ import annotations

from dataclasses import dataclass
import csv
import math
import os
import time
import zipfile
from pathlib import Path
from typing import Iterable
from xml.sax.saxutils import escape

import numpy as np


G = 9.8
MISSILE_SPEED = 300.0
SMOKE_RADIUS = 10.0
SMOKE_VALID_TIME = 20.0
SMOKE_SINK_SPEED = 3.0

FAKE_TARGET = np.array([0.0, 0.0, 0.0])
TRUE_TARGET_CENTER = np.array([0.0, 200.0, 5.0])
TRUE_TARGET_BASE = np.array([0.0, 200.0, 0.0])
TRUE_TARGET_RADIUS = 7.0
TRUE_TARGET_HEIGHT = 10.0

MISSILES = {
    "M1": np.array([20000.0, 0.0, 2000.0]),
    "M2": np.array([19000.0, 600.0, 2100.0]),
    "M3": np.array([18000.0, -600.0, 1900.0]),
}

DRONES = {
    "FY1": np.array([17800.0, 0.0, 1800.0]),
    "FY2": np.array([12000.0, 1400.0, 1400.0]),
    "FY3": np.array([6000.0, -3000.0, 700.0]),
    "FY4": np.array([11000.0, 2000.0, 1800.0]),
    "FY5": np.array([13000.0, -2000.0, 1300.0]),
}


@dataclass(frozen=True)
class SmokeShot:
    drone: str
    missile: str
    angle_deg: float
    speed: float
    drop_t: float
    fuse_t: float
    label: str = ""

    @property
    def burst_t(self) -> float:
        return self.drop_t + self.fuse_t


@dataclass
class ShotResult:
    shot: SmokeShot
    duration: float
    intervals: list[tuple[float, float]]
    drop_point: np.ndarray
    burst_point: np.ndarray
    runtime_s: float


def missile_position(missile: str, t: float) -> np.ndarray:
    p0 = MISSILES[missile]
    direction = -p0 / np.linalg.norm(p0)
    return p0 + MISSILE_SPEED * direction * t


def missile_impact_time(missile: str) -> float:
    return float(np.linalg.norm(MISSILES[missile]) / MISSILE_SPEED)


def drone_velocity(angle_deg: float, speed: float) -> np.ndarray:
    rad = math.radians(angle_deg)
    return np.array([speed * math.cos(rad), speed * math.sin(rad), 0.0])


def shot_points(shot: SmokeShot) -> tuple[np.ndarray, np.ndarray]:
    u0 = DRONES[shot.drone]
    v = drone_velocity(shot.angle_deg, shot.speed)
    drop = u0 + v * shot.drop_t
    burst = drop + v * shot.fuse_t + np.array([0.0, 0.0, -0.5 * G * shot.fuse_t**2])
    return drop, burst


def cloud_center(shot: SmokeShot, t: float) -> np.ndarray | None:
    if t < shot.burst_t or t > shot.burst_t + SMOKE_VALID_TIME:
        return None
    _, burst = shot_points(shot)
    return burst + np.array([0.0, 0.0, -SMOKE_SINK_SPEED * (t - shot.burst_t)])


def target_samples(n_angle: int = 36, n_height: int = 5, center_only: bool = False) -> np.ndarray:
    if center_only:
        return TRUE_TARGET_CENTER.reshape(1, 3)
    pts: list[list[float]] = []
    heights = np.linspace(0.0, TRUE_TARGET_HEIGHT, n_height)
    for z in heights:
        for i in range(n_angle):
            a = 2.0 * math.pi * i / n_angle
            pts.append([
                TRUE_TARGET_RADIUS * math.cos(a),
                200.0 + TRUE_TARGET_RADIUS * math.sin(a),
                z,
            ])
    pts.append(TRUE_TARGET_CENTER.tolist())
    return np.array(pts, dtype=float)


def line_segment_hits_sphere(start: np.ndarray, end: np.ndarray, center: np.ndarray) -> bool:
    seg = end - start
    denom = float(np.dot(seg, seg))
    if denom <= 1e-12:
        return False
    s = float(np.dot(center - start, seg) / denom)
    if s < 0.0 or s > 1.0:
        return False
    closest = start + s * seg
    return float(np.linalg.norm(closest - center)) <= SMOKE_RADIUS


def covered_at(
    missile: str,
    shots: Iterable[SmokeShot],
    t: float,
    samples: np.ndarray,
    cover_ratio: float = 1.0,
) -> bool:
    mpos = missile_position(missile, t)
    active_centers = [cloud_center(shot, t) for shot in shots if shot.missile == missile]
    active_centers = [c for c in active_centers if c is not None and c[2] >= 0.0]
    if not active_centers:
        return False

    seg = samples - mpos
    denom = np.einsum("ij,ij->i", seg, seg)
    blocked_any = np.zeros(len(samples), dtype=bool)
    valid = denom > 1e-12
    for center in active_centers:
        s = np.zeros(len(samples))
        s[valid] = (seg[valid] @ (center - mpos)) / denom[valid]
        in_segment = (s >= 0.0) & (s <= 1.0) & valid
        closest = mpos + seg * s[:, None]
        dist = np.linalg.norm(closest - center, axis=1)
        blocked_any |= in_segment & (dist <= SMOKE_RADIUS)
    return float(blocked_any.mean()) >= cover_ratio


def intervals_for(
    missile: str,
    shots: Iterable[SmokeShot],
    dt: float = 0.02,
    n_angle: int = 36,
    n_height: int = 5,
    center_only: bool = False,
    cover_ratio: float = 1.0,
) -> list[tuple[float, float]]:
    shots = list(shots)
    samples = target_samples(n_angle=n_angle, n_height=n_height, center_only=center_only)
    start = max(0.0, min((s.burst_t for s in shots if s.missile == missile), default=0.0))
    end = min(
        missile_impact_time(missile),
        max((s.burst_t + SMOKE_VALID_TIME for s in shots if s.missile == missile), default=0.0),
    )
    intervals: list[tuple[float, float]] = []
    in_interval = False
    t0 = start
    t = start
    while t <= end + 1e-9:
        flag = covered_at(missile, shots, t, samples, cover_ratio=cover_ratio)
        if flag and not in_interval:
            t0 = t
            in_interval = True
        if in_interval and (not flag):
            intervals.append((t0, t))
            in_interval = False
        t += dt
    if in_interval:
        intervals.append((t0, min(t, end)))
    return merge_intervals(intervals)


def merge_intervals(intervals: Iterable[tuple[float, float]]) -> list[tuple[float, float]]:
    clean = sorted((a, b) for a, b in intervals if b > a)
    if not clean:
        return []
    merged = [clean[0]]
    for a, b in clean[1:]:
        la, lb = merged[-1]
        if a <= lb + 1e-9:
            merged[-1] = (la, max(lb, b))
        else:
            merged.append((a, b))
    return merged


def interval_duration(intervals: Iterable[tuple[float, float]]) -> float:
    return float(sum(b - a for a, b in intervals))


def evaluate_shots(
    shots: Iterable[SmokeShot],
    dt: float = 0.02,
    n_angle: int = 36,
    n_height: int = 5,
    cover_ratio: float = 1.0,
) -> dict[str, list[tuple[float, float]]]:
    shots = list(shots)
    out = {}
    for missile in sorted({s.missile for s in shots}):
        out[missile] = intervals_for(
            missile,
            shots,
            dt=dt,
            n_angle=n_angle,
            n_height=n_height,
            cover_ratio=cover_ratio,
        )
    return out


def objective_duration(
    shots: Iterable[SmokeShot],
    missile: str,
    dt: float = 0.05,
    n_angle: int = 24,
    n_height: int = 4,
) -> float:
    return interval_duration(intervals_for(missile, shots, dt=dt, n_angle=n_angle, n_height=n_height))


def grid_search_single(
    drone: str,
    missile: str,
    angle_values: Iterable[float],
    speed_values: Iterable[float],
    drop_values: Iterable[float],
    fuse_values: Iterable[float],
    fixed_label: str = "",
    dt: float = 0.08,
) -> ShotResult:
    best: ShotResult | None = None
    t_start = time.perf_counter()
    for angle in angle_values:
        for speed in speed_values:
            for drop_t in drop_values:
                for fuse_t in fuse_values:
                    if fuse_t < 0 or drop_t < 0:
                        continue
                    shot = SmokeShot(drone, missile, angle, speed, drop_t, fuse_t, fixed_label)
                    _, burst = shot_points(shot)
                    if burst[2] < 0:
                        continue
                    intervals = intervals_for(missile, [shot], dt=dt, n_angle=18, n_height=4)
                    duration = interval_duration(intervals)
                    if best is None or duration > best.duration:
                        drop, burst = shot_points(shot)
                        best = ShotResult(shot, duration, intervals, drop, burst, 0.0)
    if best is None:
        shot = SmokeShot(drone, missile, 180.0, 70.0, 0.0, 0.1, fixed_label)
        drop, burst = shot_points(shot)
        best = ShotResult(shot, 0.0, [], drop, burst, 0.0)
    best.runtime_s = time.perf_counter() - t_start
    return best


def refine_single(base: ShotResult, dt: float = 0.04) -> ShotResult:
    s = base.shot
    return grid_search_single(
        s.drone,
        s.missile,
        np.arange(s.angle_deg - 2.0, s.angle_deg + 2.01, 1.0),
        np.arange(max(70.0, s.speed - 5.0), min(140.0, s.speed + 5.0) + 0.01, 5.0),
        np.arange(max(0.0, s.drop_t - 0.4), s.drop_t + 0.41, 0.2),
        np.arange(max(0.0, s.fuse_t - 0.4), s.fuse_t + 0.41, 0.2),
        fixed_label=s.label,
        dt=dt,
    )


def final_eval(shot: SmokeShot, dt: float = 0.01) -> ShotResult:
    tic = time.perf_counter()
    intervals = intervals_for(shot.missile, [shot], dt=dt, n_angle=48, n_height=6)
    duration = interval_duration(intervals)
    drop, burst = shot_points(shot)
    return ShotResult(shot, duration, intervals, drop, burst, time.perf_counter() - tic)


def union_duration_by_missile(shots: Iterable[SmokeShot], dt: float = 0.02) -> dict[str, float]:
    intervals = evaluate_shots(shots, dt=dt, n_angle=36, n_height=5)
    return {m: interval_duration(v) for m, v in intervals.items()}


def format_point(p: np.ndarray) -> str:
    return f"({p[0]:.2f}, {p[1]:.2f}, {p[2]:.2f})"


def write_csv(path: str | Path, rows: list[dict[str, object]], fieldnames: list[str]) -> None:
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", newline="", encoding="utf-8-sig") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)


def write_simple_xlsx(path: str | Path, sheets: dict[str, tuple[list[str], list[list[object]]]]) -> None:
    """Write a minimal XLSX workbook without third-party dependencies."""
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)

    def col_name(idx: int) -> str:
        name = ""
        idx += 1
        while idx:
            idx, rem = divmod(idx - 1, 26)
            name = chr(65 + rem) + name
        return name

    content_types = [
        '<?xml version="1.0" encoding="UTF-8"?>',
        '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">',
        '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>',
        '<Default Extension="xml" ContentType="application/xml"/>',
        '<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>',
        '<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>',
    ]
    for i in range(1, len(sheets) + 1):
        content_types.append(
            f'<Override PartName="/xl/worksheets/sheet{i}.xml" '
            'ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>'
        )
    content_types.append("</Types>")

    workbook_sheets = []
    workbook_rels = []
    for i, name in enumerate(sheets, start=1):
        workbook_sheets.append(
            f'<sheet name="{escape(name[:31])}" sheetId="{i}" r:id="rId{i}"/>'
        )
        workbook_rels.append(
            f'<Relationship Id="rId{i}" '
            'Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" '
            f'Target="worksheets/sheet{i}.xml"/>'
        )
    workbook_rels.append(
        f'<Relationship Id="rId{len(sheets)+1}" '
        'Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" '
        'Target="styles.xml"/>'
    )

    with zipfile.ZipFile(path, "w", compression=zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("[Content_Types].xml", "\n".join(content_types))
        zf.writestr(
            "_rels/.rels",
            '<?xml version="1.0" encoding="UTF-8"?>'
            '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
            '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>'
            "</Relationships>",
        )
        zf.writestr(
            "xl/workbook.xml",
            '<?xml version="1.0" encoding="UTF-8"?>'
            '<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" '
            'xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">'
            f"<sheets>{''.join(workbook_sheets)}</sheets></workbook>",
        )
        zf.writestr(
            "xl/_rels/workbook.xml.rels",
            '<?xml version="1.0" encoding="UTF-8"?>'
            '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
            f"{''.join(workbook_rels)}</Relationships>",
        )
        zf.writestr(
            "xl/styles.xml",
            '<?xml version="1.0" encoding="UTF-8"?>'
            '<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">'
            '<fonts count="1"><font><sz val="11"/><name val="Calibri"/></font></fonts>'
            '<fills count="1"><fill><patternFill patternType="none"/></fill></fills>'
            '<borders count="1"><border/></borders>'
            '<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>'
            '<cellXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellXfs>'
            "</styleSheet>",
        )
        for sheet_idx, (_name, (headers, rows)) in enumerate(sheets.items(), start=1):
            all_rows = [headers] + rows
            xml_rows = []
            for r_idx, row in enumerate(all_rows, start=1):
                cells = []
                for c_idx, val in enumerate(row):
                    ref = f"{col_name(c_idx)}{r_idx}"
                    if isinstance(val, (int, float)) and not isinstance(val, bool):
                        cells.append(f'<c r="{ref}"><v>{val}</v></c>')
                    else:
                        cells.append(f'<c r="{ref}" t="inlineStr"><is><t>{escape(str(val))}</t></is></c>')
                xml_rows.append(f'<row r="{r_idx}">{"".join(cells)}</row>')
            zf.writestr(
                f"xl/worksheets/sheet{sheet_idx}.xml",
                '<?xml version="1.0" encoding="UTF-8"?>'
                '<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">'
                f'<sheetData>{"".join(xml_rows)}</sheetData></worksheet>',
            )


def ensure_dirs() -> None:
    Path("support_2025A/figures").mkdir(parents=True, exist_ok=True)
    Path("support_2025A/results").mkdir(parents=True, exist_ok=True)


def save_runtime_note(name: str, seconds: float) -> None:
    ensure_dirs()
    with open(Path("support_2025A/results") / f"{name}_runtime.txt", "w", encoding="utf-8") as f:
        f.write(f"{seconds:.6f}\n")
