# GBM_Radiotherapy_ABM

**Agent-Based Modelling of Heterogeneous Glioblastoma Radiotherapy Response — Extending a HAL-Based Simulation with Clonal Heterogeneity and Spatially Fractionated Radiation**

This repository is the official implementation for **Nafisa Maliat's BSc Computer Science Final Year Dissertation** at the University of Roehampton (2025–2026).

---

## What is this project?

Glioblastoma multiforme (GBM) is the most aggressive primary brain tumour in adults, with a median survival of approximately 15 months despite maximal treatment. A core reason for this poor outcome is **intratumour heterogeneity**: GBM contains subpopulations of cells that differ in growth rate, radiation sensitivity, and immune evasion capacity, so a single radiotherapy strategy is unlikely to control all subpopulations at once.

This project is an **agent-based model (ABM)** that simulates how a heterogeneous GBM tumour responds to different radiotherapy strategies — Broad Beam (BB), Microbeam Radiation Therapy (MRT), and Minibeam Therapy (MB). Each tumour cell is an autonomous agent on a 2D lattice. Cells divide, die, are killed by lymphocytes, or are damaged by radiation according to biologically informed rules. Population-level behaviour — clonal selection, immune dynamics, treatment response — emerges from these local interactions.

The project extends a HAL-based GBM ABM originally written by **Tanaya Bowade (MSc, University of Roehampton, 2024)**. The inherited model treated the tumour as a single homogeneous population. This dissertation adds three tumour subclones with distinct phenotypes, a more biologically faithful Spatially Fractionated Radiation Therapy (SFRT) module, and a fully reproducible batch simulation pipeline.

---

## Research question

> *How does explicit clonal heterogeneity affect simulated tumour response to spatially fractionated radiotherapy, and do SFRT approaches demonstrate differential advantages when the tumour contains resistant subpopulations?*

---

## What does it do?

The simulation runs a 100×100 2D lattice for 540 timesteps (each step represents ~1 biological day). At every step:

1. **Tumour cells** decide their fate (radiation death, immune death, division, or quiescence) by drawing a single random number against cumulative probability thresholds derived from the Linear-Quadratic radiobiology model and the local immune response.
2. **Triggering cells** (antigen-presenting cells, APCs) are activated by radiation-damaged tumour debris and recruit lymphocytes.
3. **Lymphocytes** infiltrate the tumour at a baseline rate plus a radiation-amplified rate, kill tumour cells they neighbour, and decay over time.
4. **Radiation** is applied at timestep 200 (volume-matched to Romano et al.'s Day 12 in vivo treatment point) according to the chosen scenario:
   - **Broad Beam:** uniform dose over the whole grid.
   - **MRT/MB:** spatial peaks (small/large radius beam circles) with a low valley dose between them; lymphocytes in valley regions are spared.
5. **Three tumour subclones** are tracked independently:
   - **Clone 0 (Baseline):** standard parameters.
   - **Clone 1 (Proliferative):** higher division rate (1.1× growth multiplier).
   - **Clone 2 (Invasive/Resistant):** more radiation-resistant (0.4× radiosensitivity) and more immune-evasive (0.3× immune kill).

Outputs include per-timestep CSV files of cell counts (per population and per clone), MP4 videos of the simulation grid, and Python analysis notebooks that compute tumour growth slopes and compare them against the published in vivo data of Romano et al. (2021).

---

## How does it work? — Architecture

The simulation is built on top of the **HAL (Hybrid Automata Library)** framework. Cells are agents on a 2D grid; the grid manages stepping and visualisation; analysis is performed in Python notebooks against the CSV outputs.

```
                       ┌────────────────────┐
                       │  BatchRunner.java  │  ← drives 14 scenarios × 10 trials
                       └─────────┬──────────┘
                                 │
                       ┌─────────▼──────────┐
                       │     Main.java      │  ← orchestrates one simulation
                       └─────────┬──────────┘
                                 │
                       ┌─────────▼──────────┐
                       │ OnLattice2DGrid    │  ← grid, stepping, neighbours
                       └─────────┬──────────┘
                                 │
   ┌─────────────┬───────────────┼───────────────┬──────────────┐
   ▼             ▼               ▼               ▼              ▼
TumorCells  Lymphocytes  TriggeringCells  DoomedCells   RadiationManager
   (×3 clones)                                         (BB / MRT / MB)
                                 │
                                 ▼
                          CellFunctions
              (probability calc, fate, immune response)
                                 │
                                 ▼
                          CSVWriter → outputs/
```

### Key files

| File | Role |
|------|------|
| `OnLattice2DCells/Main.java` | Orchestrates one full simulation run (initialisation, stepping, radiation, output) |
| `OnLattice2DCells/BatchRunner.java` | Runs all 14 scenarios × 10 trials in one process, with per-trial state reset |
| `OnLattice2DCells/OnLattice2DGrid.java` | The HAL grid; manages cell positions and timestep updates |
| `OnLattice2DCells/CellFunctions.java` | Per-cell fate decisions, probability budgets, immune response calculation |
| `OnLattice2DCells/TumorCells.java` | Tumour cell agent and per-clone tracking arrays |
| `OnLattice2DCells/Lymphocytes.java` | Lymphocyte agent (immune effector cells) |
| `OnLattice2DCells/TriggeringCells.java` | Antigen-presenting cell agent |
| `OnLattice2DCells/DoomedCells.java` | Cells flagged for death (radiation- or immune-induced) |
| `OnLattice2DCells/RadiationManager.java` | Applies BB/MRT/MB radiation patterns, valley dose mechanism |
| `OnLattice2DCells/ScenarioParameters.java` | Per-scenario configuration (dose, beam radius, immune suppression) |
| `OnLattice2DCells/FigParameters.java` | Per-figure tumour biology parameters and clone multipliers |
| `OnLattice2DCells/SimulationParameters.java` | Cross-scenario dose and beam parameters |
| `OnLattice2DCells/CSVWriter.java` | Per-timestep output of cell counts and probabilities |
| `GBM_SimulationResults.ipynb` | Python analysis: tumour volume curves, slope comparison, clone composition |
| `Notebook_BugFix_Comparison.ipynb` | Before/after bug fix visualisations |

---

## Scenarios

All 14 scenarios share identical tumour biology parameters; only the radiation modality and dose differ.

| Scenario | Radiation Type | Peak Dose (Gy) | Beam Radius | Valley:Peak Ratio |
|----------|---------------|----------------|-------------|-------------------|
| Control | None | 0 | — | — |
| BB5 | Broad Beam | 5 | Full grid | — |
| BB10 | Broad Beam | 10 | Full grid | — |
| BB15 | Broad Beam | 15 | Full grid | — |
| MRT200 | Microbeam | 200 | 5 | 1.5 % |
| MRT400 | Microbeam | 400 | 5 | 1.5 % |
| MRT600 | Microbeam | 600 | 5 | 1.5 % |
| MB180 | Minibeam | 180 | 10 | 3 % |
| MB350 | Minibeam | 350 | 10 | 3 % |
| Pred_MRT180 | Microbeam (predictive) | 180 | 5 | 1.5 % |
| Pred_MRT350 | Microbeam (predictive) | 350 | 5 | 1.5 % |
| Pred_MB200 | Minibeam (predictive) | 200 | 10 | 3 % |
| Pred_MB400 | Minibeam (predictive) | 400 | 10 | 3 % |
| Pred_MB600 | Minibeam (predictive) | 600 | 10 | 3 % |

---

## What was changed from the inherited code?

This dissertation's contributions on top of the `tanaya-baseline` tag are:

**Bug fixes (25 across 8 categories, 23 inherited + 1 introduced):**
- Probability and decision logic — single random draw, clamped probability budget
- Immune system — bounded immune response, division-by-zero guards, removed APC gate on lymphocyte recruitment
- Radiation mechanics — spatial threshold lowered from 0.8 → 0.01 so peripheral SFRT beams fire correctly
- Batch infrastructure — `resetStaticState()` between trials, `batchMode` flag for headless runs
- Initialisation, CSV output, and performance fixes

**New features:**
- **Clonal heterogeneity:** three tumour subclones with phenotype-specific division, radiosensitivity, and immune evasion parameters
- **Valley dose mechanism:** SFRT cells outside beam peaks receive a fraction of the peak dose (1.5 % MRT, 3 % MB), modelling photon scatter; lymphocytes in valley regions are spared
- **Post-radiation immune signal decay:** sustains immune activation for 50–100 timesteps after radiation, modelling DAMP/cytokine persistence
- **Persistent sublethal radiation damage:** surviving tumour cells carry residual DNA damage that decays over time and dilutes through division

A full catalogue of every bug fix with before/after code is provided in `BugFixes_Comprehensive.pdf` (in the parent folder of this repo, alongside the dissertation report).

---

## Validation

The model is validated against the in vivo preclinical data of **Romano et al. (2021)** — a Fischer rat 9L gliosarcoma model comparing BBT, MRT, and MB at Day 12 post-implantation.

**Expected ordering** (from Romano et al.): Control > BB > MB > MRT (greatest tumour suppression).

**Observed simulation ordering:** matches across all 140 trials. Tumour growth slopes follow the expected sequence, with higher SFRT doses producing greater suppression and better immune preservation.

The notebook `GBM_SimulationResults.ipynb` reproduces the validation figures: per-scenario volume curves, normalised slope comparison vs. in vivo, and per-clone composition shifts after treatment.

---

## Getting started

### Requirements

- **Java 11+** (Java 17 recommended)
- **IntelliJ IDEA Community** (or any Java IDE / `javac`)
- **Python 3.8+** with `pandas`, `numpy`, `matplotlib`, `scipy`, `scikit-learn`
- **ffmpeg** (optional — for MP4 video output of simulations)

### Clone

```bash
git clone https://github.com/NafisaMaliat/GBM_Radiotherapy_ABM.git
cd GBM_Radiotherapy_ABM
```

### Run a single scenario

Open the project in IntelliJ, then run `OnLattice2DCells.Main`. The default scenario (`BB15`) is set at the top of `Main.java`; change it or pass a scenario name as the program argument.

### Run all 140 trials

Run `OnLattice2DCells.BatchRunner` from IntelliJ. This executes all 14 scenarios × 10 trials in one process, with full static-state reset between trials. CSV outputs are written to `HALModeling2024Outs/Scenario<NAME>/`.

### Analyse results

Open `GBM_SimulationResults.ipynb` in Jupyter. The notebook reads the CSVs from `HALModeling2024Outs/`, plots tumour volume trajectories per scenario, computes growth slopes, and compares against Romano et al.'s in vivo data.

---

## Project provenance

- **Predecessor:** Tanaya Bowade, *Agent-Based Modelling of Glioblastoma Radiotherapy Response* (MSc dissertation, University of Roehampton, 2024). The commit tagged `tanaya-baseline` marks the starting point of this project.
- **Original framework:** Hannah G. Simon, [HALModeling2024](https://github.com/hannahgsimon/HALModeling2024).
- **Empirical reference data:** Romano et al. (2021); Cho et al. (2023).
- **This work:** Nafisa Maliat (BSc Computer Science, University of Roehampton, 2025–2026) — supervisor: [supervisor name].

---

## Citation

If you use this code, please cite:

- **Maliat, N. (2026).** *Agent-Based Modelling of Heterogeneous Glioblastoma Radiotherapy Response: Extending a HAL-Based Simulation with Clonal Heterogeneity and Spatially Fractionated Radiation* (BSc dissertation). University of Roehampton. https://github.com/NafisaMaliat/GBM_Radiotherapy_ABM
- **Bowade, T. (2024).** *Agent-Based Modelling of Glioblastoma Radiotherapy Response* (MSc dissertation, predecessor project). University of Roehampton. https://github.com/tanayab/ABM_GliobMul
- **Cho, Y.-B., Yoon, N., Suh, J.H., Scott, J.G. (2023).** Radio-immune response modelling for spatially fractionated radiotherapy. *Physics in Medicine & Biology* 68(16), 165010. https://doi.org/10.1088/1361-6560/ace819
- **Romano, M., et al. (2021).** A multi-scale and multi-technique approach for the characterization of the effects of spatially fractionated x-ray radiation therapies in a preclinical model. *Cancers* 13(19), 4953. https://doi.org/10.3390/cancers13194953
- **Simon, H.G.** [HALModeling2024](https://github.com/hannahgsimon/HALModeling2024).

---

## License

MIT License — see `LICENSE`.

---

## Contact

**Nafisa Maliat** — BSc Computer Science, University of Roehampton
GitHub: [NafisaMaliat](https://github.com/NafisaMaliat)
