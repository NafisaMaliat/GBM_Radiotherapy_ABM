import os
import glob
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.linear_model import LinearRegression
import numpy as np
from scipy.stats import linregress

# Path to your root folder
root_path = '/Users/tanayabowade/Downloads/ABM_GliobMul/HALModeling2024Outs'

# Scenarios you want to include
scenarios = ['Control','BB5', 'BB10', 'BB15', 'MRT200', 'MRT400', 'MRT600', 'MB180', 'MB350','Pred_MRT180','Pred_MRT350','Pred_MB200','Pred_MB400','Pred_MB600']


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


# -------------------- All plots together with error bars from timetep 200-300 -----------------------

start_timestep = 200
end_timestep = 300

selected_timesteps = [212, 220, 230, 240, 250, 260]
scenarios = ['Control','BB5', 'BB10', 'BB15', 'MRT200', 'MRT400', 'MRT600', 'MB180', 'MB350']


# Storage for mean/std by scenario
scenario_means = {}
scenario_stds = {}

plt.figure(figsize=(10, 6))

for scenario in scenarios:
    scenario_folder = os.path.join(root_path, f'Scenario{scenario}')
    pattern = os.path.join(scenario_folder, 'TrialRunCounts_*.csv')
    files = glob.glob(pattern)

    if not files:
        print(f"❌ No files for {scenario}")
        continue

    all_trials = []

    for file in files:
        df = pd.read_csv(file)
        df['TumourVolumePercent'] = (df['TumorCells'] * CELL_VOLUME_MM3 / BRAIN_VOLUME_MM3) * 100
        df = df[(df['Timestep'] >= start_timestep) & (df['Timestep'] <= end_timestep)]
        all_trials.append(df[['Timestep', 'TumourVolumePercent']])

    merged_df = pd.concat(all_trials, axis=0)
    grouped = merged_df.groupby('Timestep')['TumourVolumePercent']
    mean_series = grouped.mean()
    std_series = grouped.std()

    # Save for error bars
    scenario_means[scenario] = mean_series
    scenario_stds[scenario] = std_series

    # Plot mean line
    plt.plot(mean_series.index, mean_series.values, label=scenario)

    # Add error bars at selected timesteps
    err_x = [t for t in selected_timesteps if t in mean_series.index]
    err_y = mean_series.loc[err_x].values
    err_std = std_series.loc[err_x].values
    plt.errorbar(err_x, err_y, yerr=err_std, fmt='o', capsize=4, color=plt.gca().lines[-1].get_color())

# Final touches
plt.title("Tumour Volume (% Brain) — Timestep 200–300 with Error Bars", fontsize=18)
plt.xlabel("Timestep", fontsize=14)
plt.ylabel("Tumour Volume (% of Brain)", fontsize=14)
plt.legend(title="Scenario", fontsize=9)
plt.grid(True)
plt.tight_layout()
plt.savefig("TumorVolumePercent_T200_T300_ErrorBars.png", dpi=300)
plt.show()
plt.close()


# ------------------- Saving individual plots treatment-wise with error bars--------------

output_dir = "IndividualScenarioErrorPlots"
os.makedirs(output_dir, exist_ok=True)

scenarios = ['Control','BB5', 'BB10', 'BB15', 'MRT200', 'MRT400', 'MRT600', 'MB180', 'MB350','Pred_MRT180','Pred_MRT350','Pred_MB200','Pred_MB400','Pred_MB600']


for scenario in scenarios:
    scenario_folder = os.path.join(root_path, f'Scenario{scenario}')
    pattern = os.path.join(scenario_folder, 'TrialRunCounts_*.csv')
    files = glob.glob(pattern)

    if len(files) < 10:
        print(f"Not enough trials for error bar calculation in {scenario}")
        continue

    print(f"{scenario}: Found {len(files)} trials")

    # Collect data across all trials
    all_trials = []

    for file in files:
        df = pd.read_csv(file)
        df['TumourVolumePercent'] = (df['TumorCells'] * CELL_VOLUME_MM3 / BRAIN_VOLUME_MM3) * 100
        trial_df = df[['Timestep', 'TumourVolumePercent']].copy()
        all_trials.append(trial_df)

    # Merge by timestep
    merged_df = pd.concat(all_trials, axis=0)

    # Group by timestep and aggregate
    grouped = merged_df.groupby('Timestep')['TumourVolumePercent']
    mean_series = grouped.mean()
    std_series = grouped.std()

    # Filter only selected timesteps
    mean_vals = mean_series.loc[mean_series.index.isin(selected_timesteps)]
    std_vals = std_series.loc[std_series.index.isin(selected_timesteps)]

    # Plot with error bars
    plt.figure(figsize=(8, 5))
    plt.errorbar(mean_vals.index, mean_vals.values, yerr=std_vals.values, fmt='o-', label=scenario, capsize=5)
    plt.title(f"Tumour Volume (% Brain) with Error Bars — {scenario}")
    plt.xlabel("Days")
    plt.ylabel("Tumour Volume (% of Brain)")
    plt.grid(True)
    plt.tight_layout()
    plt.legend()
    plt.savefig(os.path.join(output_dir, f"{scenario}_ErrorBarPlot.png"), dpi=300)
    plt.close()



# ------------------------- Calculating ABM graphs' slopes -----------

# Construct dataframe
df = pd.DataFrame(scenario_means)
df = df.reset_index()  # 'Timestep' becomes a column

# Define the range to fit slope
start = 212
end = 260
selected_range = (df['Timestep'] >= start) & (df['Timestep'] <= end)

abm_slopes = {}

for scenario in df.columns[1:]:  # Skip 'Timestep'
    y = df.loc[selected_range, scenario].values
    x = df.loc[selected_range, 'Timestep'].values.reshape(-1, 1)

    model = LinearRegression().fit(x, y)
    slope = model.coef_[0]
    abm_slopes[scenario] = slope * 100  # Convert to % per day

# Display
print("\n📈 Estimated tumour growth slope (% per day) from ABM between Day 212–260:\n")
for scenario, slope in abm_slopes.items():
    print(f"{scenario}: {slope:.4f}%")

# ----------- Calculating clinical trial slope -------------

# Provided coordinates (x = survival days, y = tumour volume %)
data = {
    "Control": [(12, 2), (20, 12), (24, 18)],
    "BB5": [(12, 2), (21, 20)],
    "BB10": [(12, 2), (28, 15), (38, 28)],
    "BB15": [(12, 2), (42, 25)],
    "MB180": [(12, 2), (25, 10), (29, 13)],
    "MRT200": [(12, 2), (21, 2), (26, 14), (29, 15)],
    "MRT400": [(12, 2), (49, 68), (57, 40), (62, 0)],
    "MRT600": [(12, 2), (54, 18), (62, 0)]
}

clinical_slopes = {}
plt.figure(figsize=(10, 6))

# Plot each treatment and compute slope
for treatment, points in data.items():
    x, y = zip(*points)
    slope, intercept, r_value, p_value, std_err = linregress(x, y)
    clinical_slopes[treatment] = slope
    plt.plot(x, y, marker='o', label=f"{treatment} (slope={slope:.2f})")

# Graph formatting
plt.title("Tumour Volume Growth Slopes (Clinical Data)")
plt.xlabel("Survival Days")
plt.ylabel("Tumour Volume (%)")
plt.legend()
plt.grid(True)
plt.tight_layout()

# Display slopes
slope_df = pd.DataFrame(clinical_slopes.items(), columns=["Treatment", "Slope"])

print(slope_df)

# -------------- Comparing slopes Clinical vs ABM --------------------

treatments = clinical_slopes.keys()
x = np.arange(len(treatments))

plt.figure(figsize=(10, 5))
plt.bar(x - 0.15, [clinical_slopes[t] for t in treatments], width=0.3, label='Clinical')
plt.bar(x + 0.15, [abm_slopes[t] for t in treatments], width=0.3, label='ABM')
plt.xticks(x, treatments)
plt.ylabel("Tumour Growth Slope (%/day)")
plt.title("Comparison of Tumour Growth Slopes (Clinical vs ABM)")
plt.legend()
plt.tight_layout()
plt.savefig("SlopeComparison.png")
plt.show()
plt.close()


