from __future__ import annotations

import time

import numpy as np

from smoke_model import final_eval, format_point, grid_search_single, refine_single, save_runtime_note, write_csv


def solve_q2():
    tic = time.perf_counter()
    coarse = grid_search_single(
        "FY1",
        "M1",
        np.arange(174.0, 185.0, 2.0),
        [70.0, 90.0, 110.0, 130.0],
        np.arange(0.0, 2.1, 0.5),
        np.arange(2.5, 5.6, 0.5),
        "Q2",
        dt=0.08,
    )
    refined = refine_single(coarse, dt=0.04)
    final = final_eval(refined.shot, dt=0.01)
    final.runtime_s = time.perf_counter() - tic
    return final


def main() -> None:
    result = solve_q2()
    row = result_row(result, "Q2")
    write_csv("support_2025A/results/q2_result.csv", [row], list(row.keys()))
    save_runtime_note("q2", result.runtime_s)
    print(f"Q2 duration: {result.duration:.3f} s")
    print(row)


def result_row(result, qname: str):
    s = result.shot
    return {
        "question": qname,
        "drone": s.drone,
        "missile": s.missile,
        "angle_deg": f"{s.angle_deg:.2f}",
        "speed_mps": f"{s.speed:.2f}",
        "drop_t_s": f"{s.drop_t:.2f}",
        "fuse_t_s": f"{s.fuse_t:.2f}",
        "burst_t_s": f"{s.burst_t:.2f}",
        "duration_s": f"{result.duration:.3f}",
        "intervals": str([(round(a, 3), round(b, 3)) for a, b in result.intervals]),
        "drop_point": format_point(result.drop_point),
        "burst_point": format_point(result.burst_point),
        "runtime_s": f"{result.runtime_s:.3f}",
    }


if __name__ == "__main__":
    main()
