
# ABM_GliobMul: Agent‑Based Model of Glioblastoma Growth & Radiotherapy Response

**Tanaya Jayprakash Bowade**  
Master of Science (Data Science), University of Roehampton  
August 2025

---

## Overview

This repository contains the implementation of an **Agent-Based Model (ABM)** for glioblastoma multiforme (GBM), focusing on tumour–immune–radiation interactions. The model is built on the **HALModeling2024** framework by H.G. Simon and has been extended to incorporate biologically realistic modules for:

- Tumour cell proliferation and immune‑mediated killing  
- Spatially fractionated radiotherapy (BBT, MRT, MBRT)  
- Immune infiltration triggered by radiation damage  
- Scenario‑based dose and spatial parameter configuration  

Also, credit goes to Cho et al. (2023) and Romano et al. (2021), whose empirical data guided our parameterisation and experimental validation.

---

## Repository Structure

```

/
├── src/
│   ├── Main.java
│   ├── FigParameters.java
│   ├── SimulationParameters.java
│   └── …
├── outputs/
│   └── ScenarioBB15/
│       └── TrialRunCounts\_001.csv
│   └── ...
├── figures/
│   └── TumorVolumePercent\_T200\_T300\_ErrorBars.png
├── notebooks/
│   └── slope\_analysis.ipynb
├── docs/
│   └── README.md                ← this file
│   └── Appendix.tex
└── environment/
└── requirements.txt         ← Python dependencies (for slope‑analysis)

````

---

## Getting Started

### Requirements

- Java 11+ and Maven (for running the ABM simulation)  
- Python 3.8+ with `pandas`, `numpy`, `matplotlib`, `scipy`, and `scikit-learn` (analysis scripts)  

### Running the ABM Simulation

1. Clone the repository:
   ```bash
   git clone https://github.com/tanayab/ABM_GliobMul.git
   cd ABM_GliobMul
   ```

2. Build and run:

   ```bash
   cd src
   mvn compile
   mvn exec:java -Dexec.mainClass="Main" -Dexec.args="BB15"
   ```

   Replace `"BB15"` with any scenario such as `Control`, `MRT200`, `MB350`, etc.

This will generate **TrialRunCounts\_\*.csv** files under the relevant scenario folders in `outputs/`.

### Plotting and Slope Comparisons

Switch to the `notebooks/` directory and run:

```bash
pip install -r ../environment/requirements.txt
```

Then open `slope_analysis.ipynb` to:

* Load CSV outputs
* Convert tumour cell counts to volume percentages
* Normalise ABM curves and superimpose with in vivo data
* Compute and compare tumour growth slopes

---

## Scenarios & Parameters

| Scenario     | Radiation Dose (Gy) | Beam Radius (grid units) | Immune Suppression Effect | Radiotherapy Mode |
| ------------ | ------------------- | ------------------------ | ------------------------- | ----------------- |
| Control      | 0                   | —                        | \~0.03                    | None              |
| BB5          | 5                   | —                        | \~0.51                    | Broad beam        |
| BB10         | 10                  | —                        | \~0.51                    | Broad beam        |
| BB15         | 15                  | —                        | \~0.51                    | Broad beam        |
| MRT200       | 200                 | 5                        | \~1.1                     | Microbeam         |
| MRT400       | 400                 | 5                        | \~1.1                     | Microbeam         |
| MRT600       | 600                 | 5                        | \~1.1                     | Microbeam         |
| MB180        | 180                 | 10                       | \~1.1                     | Minibeam          |
| MB350        | 350                 | 10                       | \~1.1                     | Minibeam          |
| Pred\_MRT180 | 180                 | 5                        | \~1.1                     | Predictive        |
| Pred\_MRT350 | 350                 | 5                        | \~1.1                     | Predictive        |
| Pred\_MB200  | 200                 | 10                       | \~1.1                     | Predictive        |
| Pred\_MB400  | 400                 | 10                       | \~1.1                     | Predictive        |
| Pred\_MB600  | 600                 | 10                       | \~1.1                     | Predictive        |

Use scenario names to automatically load appropriate parameter sets.

---

## Citation

If you make use of this code, please cite:

* Cho, Y.-B., Yoon, N., Suh, J.H., Scott, J.G.: Radio-immune response modelling
for spatially fractionated radiotherapy. Physics in medicine biology 68(16),
165010 (2023) https://doi.org/10.1088/1361-6560/ace819
  
* Romano, M., Bravin, A., Mittone, A., Eckhardt, A., Barbone, G.E., Sancey, L.,
Dinkel, J., Bartzsch, S., Ricke, J., Alunni-Fabbroni, M., Hirner-Eppeneder, H.,
Karpov, D., Giannini, C., Bunk, O., Bouchet, A., Ruf, V., Giese, A., Coan, P.: A
multi-scale and multi-technique approach for the characterization of the effects
of spatially fractionated x-ray radiation therapies in a preclinical model. Cancers
13(19), 4953–30 (2021) https://doi.org/10.3390/cancers13194953
  
* Hannah G. Simon. HALModeling2024. https://github.com/hannahgsimon/HALModeling2024
  
* This repository and associated thesis (Bowade, 2025)

---

## Contributions & Licensing

All code is distributed under the **MIT License**—please see `LICENSE` for details. Contributions are welcome via GitHub issues or pull requests.

---

## Contact

For questions or issues, open an issue or contact **Tanaya Jayprakash Bowade** via email : tanaya.bowade@gmail.com 


---

