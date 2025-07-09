package OnLattice2DCells;

import java.util.List;

public class SimulationParameters {
    public double targetPercentage;
    public double thresholdPercentage;
    public static int radius;
    public List<int[]> radiatedPixels;
    public static int baseRadiationDose, currentRadiationDose , appliedRadiationDose ;
    public boolean totalRadiation, centerRadiation, spatialRadiation;
    public boolean immuneSuppressionEffectThreshold;
    public List<int[]> availableSpaces;



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
