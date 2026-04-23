package OnLattice2DCells;

import java.util.List;

public class SimulationParameters {
    public double targetPercentage;
    public double thresholdPercentage;
    public int radius;
    public List<int[]> radiatedPixels;
    public static int baseRadiationDose, currentRadiationDose , appliedRadiationDose ;
    public boolean totalRadiation, centerRadiation, spatialRadiation;
    public boolean immuneSuppressionEffectThreshold;
    public List<int[]> availableSpaces;
    // Valley-to-Peak Dose Ratio for spatial radiation (MRT/MB)
    // Physical VPDR is ~5% (MRT) / ~10% (MB), but LQ model applies all damage
    // in a single timestep rather than over multiple cell divisions.  Effective
    // values are scaled down to compensate: MRT ~0.015, MB ~0.03.
    // BB/Control: 0.0 (not applicable — total radiation has no valley)
    public static double valleyDoseRatio = 0.0;



    public SimulationParameters(double targetPercentage, double thresholdPercentage, int radius, List<int[]> radiatedPixels, int baseRadiationDose, int currentRadiationDose, int appliedRadiationDose, boolean totalRadiation, boolean centerRadiation, boolean spatialRadiation, boolean immuneSuppressionEffectThreshold, List<int[]> availableSpaces) {
        this.targetPercentage = targetPercentage;
        this.thresholdPercentage = thresholdPercentage;
        this.radius = radius;
        this.radiatedPixels = radiatedPixels;
        this.baseRadiationDose = baseRadiationDose;
        this.currentRadiationDose = currentRadiationDose;
        this.appliedRadiationDose = appliedRadiationDose;
        this.totalRadiation = totalRadiation;
        this.centerRadiation = centerRadiation;
        this.spatialRadiation = spatialRadiation;
        this.immuneSuppressionEffectThreshold = immuneSuppressionEffectThreshold;
        this.availableSpaces = availableSpaces;
    }
}
