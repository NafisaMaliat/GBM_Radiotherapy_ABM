import matplotlib.pyplot as plt
import numpy as np
from scipy.stats import linregress
import pandas as pd
from sklearn.linear_model import LinearRegression


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


# ----- Calculating ABM graphs slopes -----------

# Load ABM tumour % data
df = pd.read_csv('TumorVolumePercent.csv')

# Define the range to fit slope
start = 212
end = 260
timesteps = df['Timestep'].values.reshape(-1, 1)
selected_range = (df['Timestep'] >= start) & (df['Timestep'] <= end)

abm_slopes = {}

for scenario in df.columns[1:]:  # Skip 'Timestep'
    y = df.loc[selected_range, scenario].values
    x = df.loc[selected_range, 'Timestep'].values.reshape(-1, 1)

    model = LinearRegression().fit(x, y)
    slope = model.coef_[0]
    abm_slopes[scenario] = slope * 100  # Convert to % per day

# Display
for scenario, slope in abm_slopes.items():
    print(f"{scenario}: {slope:.4f}% tumour growth per day")


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
