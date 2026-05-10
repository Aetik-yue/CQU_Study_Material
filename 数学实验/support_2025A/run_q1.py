from __future__ import annotations

import time

from smoke_model import (
    SmokeShot,
    final_eval,
    format_point,
    interval_duration,
    intervals_for,
    save_runtime_note,
    shot_points,
    write_csv,
)


def main() -> None:
    tic = time.perf_counter()
    shot = SmokeShot("FY1", "M1", 180.0, 120.0, 1.5, 3.6, "Q1")
    full = final_eval(shot, dt=0.005)
    center_intervals = intervals_for("M1", [shot], dt=0.005, center_only=True)
    drop, burst = shot_points(shot)
    runtime = time.perf_counter() - tic

    rows = [
        {
            "case": "cylinder_samples",
            "duration_s": f"{full.duration:.3f}",
            "intervals": str([(round(a, 3), round(b, 3)) for a, b in full.intervals]),
            "drop_point": format_point(drop),
            "burst_point": format_point(burst),
            "runtime_s": f"{runtime:.3f}",
        },
        {
            "case": "center_point_check",
            "duration_s": f"{interval_duration(center_intervals):.3f}",
            "intervals": str([(round(a, 3), round(b, 3)) for a, b in center_intervals]),
            "drop_point": format_point(drop),
            "burst_point": format_point(burst),
            "runtime_s": f"{runtime:.3f}",
        },
    ]
    write_csv(
        "support_2025A/results/q1_result.csv",
        rows,
        ["case", "duration_s", "intervals", "drop_point", "burst_point", "runtime_s"],
    )
    save_runtime_note("q1", runtime)
    print(f"Q1 full-cylinder duration: {full.duration:.3f} s")
    print(f"Q1 center-point check: {interval_duration(center_intervals):.3f} s")
    print(f"Drop point: {format_point(drop)}")
    print(f"Burst point: {format_point(burst)}")
    print(f"Runtime: {runtime:.3f} s")


if __name__ == "__main__":
    main()
