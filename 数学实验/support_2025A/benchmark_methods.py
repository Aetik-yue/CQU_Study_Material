from __future__ import annotations

import csv
from pathlib import Path

import matplotlib.pyplot as plt

from smoke_model import write_simple_xlsx


BASE = Path("support_2025A")
RESULTS = BASE / "results"
FIGURES = BASE / "figures"


PUBLIC_METHODS = [
    ["A1", "Geometry + greedy + improved grid search", 1.41, 4.680, 7.56, 13.91, 42.20, "Fast and interpretable; target simplified in parts."],
    ["A2", "Viewing cone + PSO-SQP", 1.3916, 4.587, 6.45, 11.549, 20.40, "Good cylinder modeling; high-dimensional problem simplified."],
    ["A3", "Kinematics + layered grid/SLSQP + GA", 1.392, 4.619, 6.800, 11.281, 22.870, "Balanced accuracy and optimization; relies on stochastic GA."],
    ["A4", "Two-step geometry + DEGA", 1.391, 4.58, 6.46, 11.59, 21.29, "Strong multi-stage heuristic; parameter sensitivity remains."],
]


def read_csv_dict(path: Path) -> list[dict[str, str]]:
    with path.open("r", encoding="utf-8-sig", newline="") as f:
        return list(csv.DictReader(f))


def first_float(rows: list[dict[str, str]], key: str, default: float = 0.0) -> float:
    if not rows:
        return default
    try:
        return float(rows[0][key])
    except Exception:
        return default


def collect_ours() -> dict[str, float]:
    q1 = read_csv_dict(RESULTS / "q1_result.csv")
    q2 = read_csv_dict(RESULTS / "q2_result.csv")
    q3 = read_csv_dict(RESULTS / "q3_result.csv")
    q4 = read_csv_dict(RESULTS / "q4_result.csv")
    q5 = read_csv_dict(RESULTS / "q5_result.csv")
    return {
        "Q1": first_float(q1, "duration_s"),
        "Q2": first_float(q2, "duration_s"),
        "Q3": first_float(q3, "union_duration_s"),
        "Q4": first_float(q4, "union_duration_s"),
        "Q5": first_float(q5, "total_by_missile_sum_s"),
        "T1": first_float(q1, "runtime_s"),
        "T2": first_float(q2, "runtime_s"),
        "T3": first_float(q3, "runtime_s"),
        "T4": first_float(q4, "runtime_s"),
        "T5": first_float(q5, "runtime_s"),
    }


def write_workbooks(ours: dict[str, float]) -> None:
    method_headers = ["paper", "method", "Q1", "Q2", "Q3", "Q4", "Q5", "comment"]
    method_rows = PUBLIC_METHODS + [[
        "This paper",
        "Unified cylinder sampling + deterministic coarse-to-local search",
        ours["Q1"],
        ours["Q2"],
        ours["Q3"],
        ours["Q4"],
        ours["Q5"],
        "Reproducible scripts with runtime logging; conservative full-cylinder criterion.",
    ]]
    write_simple_xlsx(BASE / "method_comparison.xlsx", {"methods": (method_headers, method_rows)})

    result_headers = ["question", "duration_s", "runtime_s", "note"]
    result_rows = [
        ["Q1", ours["Q1"], ours["T1"], "Full cylinder sampling; center-point value is also reported in q1_result.csv."],
        ["Q2", ours["Q2"], ours["T2"], "Coarse grid plus local refinement."],
        ["Q3", ours["Q3"], ours["T3"], "Greedy interval extension with one-second spacing."],
        ["Q4", ours["Q4"], ours["T4"], "Three independent single-shot searches, then interval union."],
        ["Q5", ours["Q5"], ours["T5"], "Layered assignment and deterministic local searches."],
    ]
    write_simple_xlsx(BASE / "results_q1_q5.xlsx", {"results": (result_headers, result_rows)})


def write_figures(ours: dict[str, float]) -> None:
    FIGURES.mkdir(parents=True, exist_ok=True)
    labels = ["A1", "A2", "A3", "A4", "This"]
    q1 = [r[2] for r in PUBLIC_METHODS] + [ours["Q1"]]
    q2 = [r[3] for r in PUBLIC_METHODS] + [ours["Q2"]]
    q3 = [r[4] for r in PUBLIC_METHODS] + [ours["Q3"]]
    q4 = [r[5] for r in PUBLIC_METHODS] + [ours["Q4"]]
    q5 = [r[6] for r in PUBLIC_METHODS] + [ours["Q5"]]

    plt.figure(figsize=(8, 4.5))
    x = range(len(labels))
    plt.bar(x, q1, color="#4C78A8")
    plt.xticks(x, labels)
    plt.ylabel("duration / s")
    plt.title("Problem 1: public results vs this paper")
    plt.tight_layout()
    plt.savefig(FIGURES / "q1_public_comparison.png", dpi=180)
    plt.close()

    plt.figure(figsize=(9, 5))
    for values, name in [(q2, "Q2"), (q3, "Q3"), (q4, "Q4")]:
        plt.plot(labels, values, marker="o", label=name)
    plt.ylabel("duration / s")
    plt.title("Single-target tasks: public results vs this paper")
    plt.legend()
    plt.grid(alpha=0.3)
    plt.tight_layout()
    plt.savefig(FIGURES / "single_target_comparison.png", dpi=180)
    plt.close()

    plt.figure(figsize=(8, 4.5))
    plt.bar(labels, q5, color="#F58518")
    plt.ylabel("summed duration / s")
    plt.title("Problem 5: public results vs this paper")
    plt.tight_layout()
    plt.savefig(FIGURES / "q5_public_comparison.png", dpi=180)
    plt.close()

    plt.figure(figsize=(8, 4.5))
    plt.bar(["Q1", "Q2", "Q3", "Q4", "Q5"], [ours[f"T{i}"] for i in range(1, 6)], color="#54A24B")
    plt.ylabel("runtime / s")
    plt.title("Runtime of reproducible scripts")
    plt.tight_layout()
    plt.savefig(FIGURES / "runtime_comparison.png", dpi=180)
    plt.close()


def main() -> None:
    missing = [p for p in ["q1_result.csv", "q2_result.csv", "q3_result.csv", "q4_result.csv", "q5_result.csv"] if not (RESULTS / p).exists()]
    if missing:
        raise SystemExit(f"Missing result files: {missing}. Run run_q1.py ... run_q5.py first.")
    ours = collect_ours()
    write_workbooks(ours)
    write_figures(ours)
    print("Wrote support_2025A/method_comparison.xlsx")
    print("Wrote support_2025A/results_q1_q5.xlsx")
    print("Wrote support_2025A/figures/*.png")


if __name__ == "__main__":
    main()
