
# GBM_Radiotherapy_ABM: Agent-Based Model of Glioblastoma Radiotherapy Response

---

## Project Provenance

This project extends the work of **Tanaya Bowade**, whose MSc dissertation produced an
agent-based model (ABM) of glioblastoma (GBM) response to radiotherapy, built in Java
using the HAL (Hybrid Automata Library) framework. Tanaya's original model serves as the
baseline predecessor for this work and is available at:
https://github.com/tanayab/ABM_GliobMul

The commit tagged `tanaya-baseline` in this repository marks the point at which the
predecessor codebase was taken as the starting point.

This repository is the official implementation for **Nafisa Maliat's BSc Computer Science
Final Year Dissertation** at the University of Roehampton. The primary dissertation goal
is to extend the model toward tumour heterogeneity, introducing three tumour subclone
populations (Baseline, Proliferative, InvasiveResistant), with a longer-term architecture
that supports virtual-patient style extensions with patient-to-patient variability.

---

## Overview

This repository contains the implementation of an **Agent-Based Model (ABM)** for
glioblastoma multiforme (GBM), focusing on tumour–immune–radiation interactions. The
model is built on the **HALModeling2024** framework and has been extended to incorporate
biologically realistic modules for:

- Tumour cell proliferation and immune-mediated killing
- Spatially fractionated radiotherapy (BBT, MRT, MBRT)
- Immune infiltration triggered by radiation damage
- Scenario-based dose and spatial parameter configuration
- Clonal heterogeneity: three tumour subclone populations

Credit goes to Cho et al. (2023) and Romano et al. (2021), whose empirical data guided
parameterisation and experimental validation.

---

## Repository Structure

```
/
├── OnLattice2DCells/
│   ├── Main.java
│   ├── TumorCells.java
│   ├── CellFunctions.java
│   ├── OnLattice2DGrid.java
│   ├── RadiationManager.java
│   ├── ScenarioParameters.java
│   ├── FigParameters.java
│   ├── CSVWriter.java
│   └── ...
├── HAL/                        ← HALModeling2024 framework
├── analysis/                   ← Python analysis scripts
└── HALModeling2024Outs/        ← Simulation output (gitignored)
```

---

## Getting Started

### Requirements

- Java 11+ (for running the ABM simulation)
- Python 3.8+ with `pandas`, `numpy`, `matplotlib`, `scipy`, and `scikit-learn` (analysis scripts)

### Running the ABM Simulation

1. Clone the repository:
   ```bash
   git clone https://github.com/NafisaMaliat/GBM_Radiotherapy_ABM.git
   cd GBM_Radiotherapy_ABM
   ```

2. Open in IntelliJ IDEA and run `Main.java`, or compile and run via terminal.

---

## Scenarios & Parameters

| Scenario     | Radiation Dose (Gy) | Beam Radius (grid units) | Immune Suppression Effect | Radiotherapy Mode |
| ------------ | ------------------- | ------------------------ | ------------------------- | ----------------- |
| Control      | 0                   | —                        | ~0.03                     | None              |
| BB5          | 5                   | —                        | ~0.51                     | Broad beam        |
| BB10         | 10                  | —                        | ~0.51                     | Broad beam        |
| BB15         | 15                  | —                        | ~0.51                     | Broad beam        |
| MRT200       | 200                 | 5                        | ~1.1                      | Microbeam         |
| MRT400       | 400                 | 5                        | ~1.1                      | Microbeam         |
| MRT600       | 600                 | 5                        | ~1.1                      | Microbeam         |
| MB180        | 180                 | 10                       | ~1.1                      | Minibeam          |
| MB350        | 350                 | 10                       | ~1.1                      | Minibeam          |

---

## Citation

If you make use of this code, please cite:

- Cho, Y.-B., Yoon, N., Suh, J.H., Scott, J.G.: Radio-immune response modelling
  for spatially fractionated radiotherapy. Physics in Medicine & Biology 68(16),
  165010 (2023) https://doi.org/10.1088/1361-6560/ace819

- Romano, M., et al.: A multi-scale and multi-technique approach for the
  characterization of the effects of spatially fractionated x-ray radiation therapies
  in a preclinical model. Cancers 13(19), 4953 (2021)
  https://doi.org/10.3390/cancers13194953

- Hannah G. Simon. HALModeling2024. https://github.com/hannahgsimon/HALModeling2024

- Bowade, T. (2025). Agent-Based Modelling of Glioblastoma Radiotherapy Response
  (MSc dissertation). University of Roehampton. [Predecessor project]
  https://github.com/tanayab/ABM_GliobMul

---

## Contributions & Licensing

All code is distributed under the **MIT License** — see `LICENSE` for details.

---

## Contact

For questions about this dissertation project, contact **Nafisa Maliat**
via GitHub: https://github.com/NafisaMaliat

---
