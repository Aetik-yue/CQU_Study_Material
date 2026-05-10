from __future__ import annotations

import time

import numpy as np

from smoke_model import SmokeShot, evaluate_shots, format_point, grid_search_single, interval_duration, save_runtime_note, shot_points, write_csv


def quick_single(drone: str, missile: str, label: str):
    angle_windows = {
        ("FY2", "M2"): np.arange(230.0, 310.0, 10.0),
        ("FY2", "M3"): np.arange(230.0, 310.0, 10.0),
        ("FY3", "M2"): np.arange(60.0, 140.0, 10.0),
        ("FY3", "M3"): np.arange(60.0, 140.0, 10.0),
        ("FY4", "M2"): np.arange(250.0, 330.0, 10.0),
        ("FY5", "M1"): np.arange(60.0, 130.0, 10.0),
    }
    return grid_search_single(
        drone,
        missile,
        angle_windows.get((drone, missile), np.arange(0.0, 360.0, 20.0)),
        [90.0, 120.0, 140.0],
        np.arange(0.0, 35.1, 4.0),
        np.arange(0.5, 9.1, 1.5),
        label,
        dt=0.12,
    )


def solve_q5():
    tic = time.perf_counter()
    fy1 = [
        SmokeShot("FY1", "M1", 176.6, 70.0, 0.0, 2.5, "Q5-FY1-1"),
        SmokeShot("FY1", "M1", 176.6, 70.0, 1.0, 0.1, "Q5-FY1-2"),
        SmokeShot("FY1", "M1", 176.6, 70.0, 2.0, 0.1, "Q5-FY1-3"),
    ]
    q4_fy2 = SmokeShot("FY2", "M1", 290.0, 105.0, 8.6, 5.3, "Q5-FY2-M1")
    q4_fy3 = SmokeShot("FY3", "M1", 78.0, 100.0, 30.2, 0.7, "Q5-FY3-M1")
    extra = [
        quick_single("FY2", "M2", "Q5-FY2-M2").shot,
        q4_fy2,
        quick_single("FY2", "M3", "Q5-FY2-M3").shot,
        quick_single("FY3", "M3", "Q5-FY3-M3").shot,
        q4_fy3,
        quick_single("FY3", "M2", "Q5-FY3-M2").shot,
        quick_single("FY4", "M2", "Q5-FY4-M2").shot,
        quick_single("FY5", "M1", "Q5-FY5-M1").shot,
    ]
    shots = fy1 + extra
    intervals = evaluate_shots(shots, dt=0.03, n_angle=30, n_height=5)
    runtime = time.perf_counter() - tic
    return shots, intervals, runtime


def main() -> None:
    shots, intervals, runtime = solve_q5()
    totals = {m: interval_duration(v) for m, v in intervals.items()}
    rows = []
    for i, shot in enumerate(shots, start=1):
        drop, burst = shot_points(shot)
        rows.append({
            "question": "Q5",
            "shot_no": i,
            "drone": shot.drone,
            "missile": shot.missile,
            "angle_deg": f"{shot.angle_deg:.2f}",
            "speed_mps": f"{shot.speed:.2f}",
            "drop_t_s": f"{shot.drop_t:.2f}",
            "fuse_t_s": f"{shot.fuse_t:.2f}",
            "burst_t_s": f"{shot.burst_t:.2f}",
            "drop_point": format_point(drop),
            "burst_point": format_point(burst),
            "M1_union_s": f"{totals.get('M1', 0.0):.3f}",
            "M2_union_s": f"{totals.get('M2', 0.0):.3f}",
            "M3_union_s": f"{totals.get('M3', 0.0):.3f}",
            "total_by_missile_sum_s": f"{sum(totals.values()):.3f}",
            "runtime_s": f"{runtime:.3f}",
        })
    write_csv("support_2025A/results/q5_result.csv", rows, list(rows[0].keys()))
    save_runtime_note("q5", runtime)
    print(f"Q5 totals by missile: {totals}")
    print(f"Q5 summed duration: {sum(totals.values()):.3f} s")
    print(f"Runtime: {runtime:.3f} s")


if __name__ == "__main__":
    main()
