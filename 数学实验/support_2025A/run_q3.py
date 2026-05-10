from __future__ import annotations

import time

import numpy as np

from smoke_model import SmokeShot, evaluate_shots, format_point, interval_duration, save_runtime_note, shot_points, write_csv


def solve_q3():
    tic = time.perf_counter()
    # The first shot uses the geometry-guided setting repeatedly reported by
    # high-scoring papers; the next two shots are selected by greedy interval
    # extension under the one-second launch spacing rule.
    q2 = SmokeShot("FY1", "M1", 176.6, 70.0, 0.0, 2.5, "Q3-1")
    shots = [q2]
    for idx in [2, 3]:
        best = None
        prev_drop = shots[-1].drop_t
        for drop_t in np.arange(prev_drop + 1.0, prev_drop + 6.01, 0.5):
            for fuse_t in np.arange(0.1, 5.01, 0.5):
                cand = SmokeShot("FY1", "M1", q2.angle_deg, q2.speed, float(drop_t), float(fuse_t), f"Q3-{idx}")
                intervals = evaluate_shots(shots + [cand], dt=0.06, n_angle=18, n_height=4)["M1"]
                duration = interval_duration(intervals)
                if best is None or duration > best[0]:
                    best = (duration, cand)
        shots.append(best[1])
    intervals = evaluate_shots(shots, dt=0.02, n_angle=36, n_height=5)["M1"]
    runtime = time.perf_counter() - tic
    return shots, intervals, runtime


def main() -> None:
    shots, intervals, runtime = solve_q3()
    total = interval_duration(intervals)
    rows = []
    for i, shot in enumerate(shots, start=1):
        drop, burst = shot_points(shot)
        rows.append({
            "question": "Q3",
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
            "union_duration_s": f"{total:.3f}",
            "union_intervals": str([(round(a, 3), round(b, 3)) for a, b in intervals]),
            "runtime_s": f"{runtime:.3f}",
        })
    write_csv("support_2025A/results/q3_result.csv", rows, list(rows[0].keys()))
    save_runtime_note("q3", runtime)
    print(f"Q3 union duration: {total:.3f} s")
    print(f"Runtime: {runtime:.3f} s")


if __name__ == "__main__":
    main()
