import os
import glob
import pandas as pd
import matplotlib.pyplot as plt

# Path to your root folder
root_path = '/Users/tanayabowade/Downloads/ABM_GliobMul/HALModeling2024Outs'

# Scenarios you want to include
scenarios = ['BB5', 'BB10', 'BB15', 'MRT200', 'MRT400', 'MRT600', 'MB180', 'MB350']


# ------------------------------------------------------------------------
# Tumour Volume Estimation: Matching Paper's "% of Brain Volume" Graph
# ------------------------------------------------------------------------
# In the Romano et al. (2021) study, tumour volume is reported as a
# percentage of total brain volume (Y-axis in Figure 6b):
#
#     Tumour % = (Tumour Volume / Brain Volume) × 100
#
# ------------------------------------------------------------------------
#  How We Approximate It in Our ABM:
#
# - In our simulation, each tumour agent occupies one grid site.
# - The grid is 2D (e.g., 100×100), so we assume each site represents a
#   3D voxel of tissue with depth — a common approximation.
#
# - We assume:
#       Each grid site = 100 µm × 100 µm × 100 µm = 0.001 mm³
#       → CELL_VOLUME_MM3 = 0.001
#
# - Rat brain volume ≈ 2000 mm³ → BRAIN_VOLUME_MM3 = 2000
#
# Therefore:
#     Tumour Volume (mm³) = TumorCells.count × 0.001
#     Tumour % Volume     = (Tumour Volume / 2000) × 100
#
# This allows us to convert model tumour cell counts into realistic
# tumour volume percentages directly comparable to experimental data.
# ------------------------------------------------------------------------

# Assumptions
CELL_VOLUME_MM3 = 0.001  # Each cell occupies 0.001 mm³
BRAIN_VOLUME_MM3 = 2000  # Approximate brain volume of male fischer rat in mm³, source 'An MRI-Derived Neuroanatomical Atlas of the Fischer 344 Rat Brain'

tumor_volume_df = pd.DataFrame()
tumor_volume_percent_df = pd.DataFrame()  # For storing % tumour volume

for scenario in scenarios:
    scenario_folder = os.path.join(root_path, f'Scenario{scenario}')
    pattern = os.path.join(scenario_folder, 'TrialRunCounts_*.csv')
    matching_files = glob.glob(pattern)

    if not matching_files:
        print(f"No file found for {scenario}")
        continue

    # Pick latest file by modification time
    latest_file = max(matching_files, key=os.path.getmtime)
    print(f"{scenario}: Using file {os.path.basename(latest_file)}")

    df = pd.read_csv(latest_file)

    if 'Timestep' not in df.columns or 'TumorCells' not in df.columns:
        print(f"Missing columns in {latest_file}, skipping.")
        continue

    # Compute tumour volume in mm³ and as % of brain
    tumour_volume_mm3 = df['TumorCells'] * CELL_VOLUME_MM3
    tumour_volume_percent = (tumour_volume_mm3 / BRAIN_VOLUME_MM3) * 100

    if tumor_volume_df.empty:
        tumor_volume_df['Timestep'] = df['Timestep']
        tumor_volume_percent_df['Timestep'] = df['Timestep']

    tumor_volume_df[scenario] = df['TumorCells']
    tumor_volume_percent_df[scenario] = tumour_volume_percent

# Save both absolute and percentage volume
tumor_volume_df.to_csv('TumorVolume.csv', index=False)
tumor_volume_percent_df.to_csv('TumorVolumePercent.csv', index=False)
print("TumorVolume.csv and TumorVolumePercent.csv saved.")

# --------- Plotting ---------
plt.figure(figsize=(10, 6))

for scenario in scenarios:
    if scenario in tumor_volume_df.columns:
        plt.plot(tumor_volume_df['Timestep'], tumor_volume_df[scenario], label=scenario)

plt.title("Tumour Volume Over Time", fontsize=18)
plt.xlabel("Timestep", fontsize=14)
plt.ylabel("Tumour Cells Count", fontsize=14)
plt.legend(title="Scenario")
plt.grid(True)
plt.tight_layout()

plt.savefig("TumorVolumeGraph.png", dpi=300)
plt.show()

# --------- Plot Tumour Volume as % of Brain ---------
plt.figure(figsize=(10, 6))
for scenario in scenarios:
    if scenario in tumor_volume_percent_df.columns:
        plt.plot(tumor_volume_percent_df['Timestep'], tumor_volume_percent_df[scenario], label=scenario)

plt.title("Tumour Volume (% Brain) Over Time", fontsize=18)
plt.xlabel("Days", fontsize=14)
plt.ylabel("Tumour Volume (% of Brain)", fontsize=14)
plt.legend(title="Scenario")
plt.grid(True)
plt.tight_layout()
plt.savefig("TumorVolumePercentGraph.png", dpi=300)
plt.show()

# ---------------- PLOT: % Brain Volume after treatment-------------------

start_timestep = 200
end_timestep = 300

# Filter for range 200–300
# Create a filtered copy, keeping original intact
tumor_volume_percent_df_filtered = tumor_volume_percent_df[
    (tumor_volume_percent_df['Timestep'] >= start_timestep) &
    (tumor_volume_percent_df['Timestep'] <= end_timestep)
    ].reset_index(drop=True)

plt.figure(figsize=(10, 6))
for scenario in scenarios:
    if scenario in tumor_volume_percent_df_filtered.columns:
        plt.plot(tumor_volume_percent_df_filtered['Timestep'], tumor_volume_percent_df_filtered[scenario], label=scenario)

plt.title("Tumour Volume (% Brain) — Timestep 200–300", fontsize=18)
plt.xlabel("Timestep", fontsize=14)
plt.ylabel("Tumour Volume (% of Brain)", fontsize=14)
plt.legend(title="Scenario")
plt.grid(True)
plt.tight_layout()
plt.savefig("TumorVolumePercent_T200_T300.png", dpi=300)
plt.show()