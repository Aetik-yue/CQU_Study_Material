from __future__ import annotations

import time

import numpy as np

from smoke_model import evaluate_shots, format_point, grid_search_single, interval_duration, refine_single, save_runtime_note, shot_points, write_csv


def solve_one(drone: str, missile: str, label: str):
    if drone == "FY1":
        angles = np.arange(160.0, 191.0, 5.0)
    elif drone == "FY2":
        angles = np.arange(230.0, 301.0, 10.0)
    else:
        angles = np.arange(60.0, 131.0, 10.0)
    coarse = grid_search_single(
        drone,
        missile,
        angles,
        [70.0, 100.0, 130.0],
        np.arange(0.0, 30.1, 3.0),
        np.arange(0.5, 9.1, 1.0),
        label,
        dt=0.10,
    )
    return refine_single(coarse, dt=0.06)


def solve_q4():
    tic = time.perf_counter()
    results = [solve_one("FY1", "M1", "Q4-FY1"), solve_one("FY2", "M1", "Q4-FY2"), solve_one("FY3", "M1", "Q4-FY3")]
    shots = [r.shot for r in results]
    intervals = evaluate_shots(shots, dt=0.02, n_angle=36, n_height=5)["M1"]
    runtime = time.perf_counter() - tic
    return results, intervals, runtime


def main() -> None:
    results, intervals, runtime = solve_q4()
    total = interval_duration(intervals)
    rows = []
    for i, result in enumerate(results, start=1):
        s = result.shot
        drop, burst = shot_points(s)
        rows.append({
            "question": "Q4",
            "shot_no": i,
            "drone": s.drone,
            "missile": s.missile,
            "angle_deg": f"{s.angle_deg:.2f}",
            "speed_mps": f"{s.speed:.2f}",
            "drop_t_s": f"{s.drop_t:.2f}",
            "fuse_t_s": f"{s.fuse_t:.2f}",
            "burst_t_s": f"{s.burst_t:.2f}",
            "single_duration_s": f"{result.duration:.3f}",
            "drop_point": format_point(drop),
            "burst_point": format_point(burst),
            "union_duration_s": f"{total:.3f}",
            "union_intervals": str([(round(a, 3), round(b, 3)) for a, b in intervals]),
            "runtime_s": f"{runtime:.3f}",
        })
    write_csv("support_2025A/results/q4_result.csv", rows, list(rows[0].keys()))
    save_runtime_note("q4", runtime)
    print(f"Q4 union duration: {total:.3f} s")
    print(f"Runtime: {runtime:.3f} s")


if __name__ == "__main__":
    main()
