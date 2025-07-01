package OnLattice2DCells;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

class ScenarioParameters {
    char scenario;

    public ScenarioParameters(char scenario) {
        this.scenario = scenario;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String dateTime = LocalDateTime.now().format(formatter);

        String fileNameWithDate = OnLattice2DGrid.fileName1.replace(".csv", "_" + dateTime + ".csv");

        // Ensure the parent folder exists
        File parentFolder = new File(OnLattice2DGrid.directory);
        if (!parentFolder.exists()) {
            parentFolder.mkdirs();
        }

        // Create the scenario folder
        File scenarioFolder = new File(OnLattice2DGrid.directory, "Scenario" + scenario);
        if (!scenarioFolder.exists()) {
            scenarioFolder.mkdirs();
        }
        OnLattice2DGrid.fullPath1 = new File(scenarioFolder, fileNameWithDate).getAbsolutePath();

        if (scenario == 'A')   // Baseline, No Radiation, Weak Immune Suppression
        {
            OnLattice2DGrid.figure = 2;
            new FigParameters(OnLattice2DGrid.figure);
            OnLattice2DGrid.totalRadiation = false;
            OnLattice2DGrid.centerRadiation = false;
            OnLattice2DGrid.spatialRadiation = false;
            FigParameters.immuneSuppressionEffect = 0.031;
        } else if (scenario == 'B')  // No Radiation, Stronger Immune Suppression
        {
            OnLattice2DGrid.figure = 3;
            new FigParameters(OnLattice2DGrid.figure);
            OnLattice2DGrid.totalRadiation = false;
            OnLattice2DGrid.centerRadiation = false;
            OnLattice2DGrid.spatialRadiation = false;
            FigParameters.immuneSuppressionEffect = 0.1;
        } else if (scenario == 'C') //Centre Radiation Targeting 70% of Tumour
        {
            OnLattice2DGrid.figure = 3;
            new FigParameters(OnLattice2DGrid.figure);
            OnLattice2DGrid.baseRadiationDose = 0;
            OnLattice2DGrid.appliedRadiationDose = 10;
            OnLattice2DGrid.radiationTimesteps = List.of(200);
            OnLattice2DGrid.totalRadiation = false;
            OnLattice2DGrid.centerRadiation = true;
            OnLattice2DGrid.spatialRadiation = false;
            OnLattice2DGrid.targetPercentage = 0.7;
            FigParameters.immuneSuppressionEffect = 0.1;
        } else if (scenario == 'D') // Centre Radiation Targeting 85% of Tumour
        {
            OnLattice2DGrid.figure = 3;
            new FigParameters(OnLattice2DGrid.figure);
            OnLattice2DGrid.baseRadiationDose = 0;
            OnLattice2DGrid.appliedRadiationDose = 10;
            OnLattice2DGrid.radiationTimesteps = List.of(200);
            OnLattice2DGrid.totalRadiation = false;
            OnLattice2DGrid.centerRadiation = true;
            OnLattice2DGrid.spatialRadiation = false;
            OnLattice2DGrid.targetPercentage = .85;
            FigParameters.immuneSuppressionEffect = 0.1;
        } else if (scenario == 'E') //Centre Radiation Targeting 100% of Tumour
        {
            OnLattice2DGrid.figure = 3;
            new FigParameters(OnLattice2DGrid.figure);
            OnLattice2DGrid.baseRadiationDose = 0;
            OnLattice2DGrid.appliedRadiationDose = 10;
            OnLattice2DGrid.radiationTimesteps = List.of(200);
            OnLattice2DGrid.totalRadiation = false;
            OnLattice2DGrid.centerRadiation = true;
            OnLattice2DGrid.spatialRadiation = false;
            OnLattice2DGrid.targetPercentage = 1;
            FigParameters.immuneSuppressionEffect = 0.1;
        } else if (scenario == 'F') // TMZ Continuous Therapy
        {
            OnLattice2DGrid.figure = 3;
            new FigParameters(OnLattice2DGrid.figure);
            OnLattice2DGrid.baseRadiationDose = 0;
            OnLattice2DGrid.appliedRadiationDose = 0; // No radiation
            OnLattice2DGrid.radiationTimesteps = List.of(); // No radiation timesteps
            OnLattice2DGrid.totalRadiation = false;
            OnLattice2DGrid.centerRadiation = false;
            OnLattice2DGrid.spatialRadiation = false;
            OnLattice2DGrid.tmzTimesteps = "continuous"; // Custom field you can create to track TMZ type
            FigParameters.immuneSuppressionEffect = 0.1;
        } else if (scenario == 'G') // TMZ Pulsed Therapy
        {
            OnLattice2DGrid.figure = 3;
            new FigParameters(OnLattice2DGrid.figure);
            OnLattice2DGrid.baseRadiationDose = 0;
            OnLattice2DGrid.appliedRadiationDose = 0; // No radiation
            OnLattice2DGrid.radiationTimesteps = List.of(); // No radiation timesteps
            OnLattice2DGrid.totalRadiation = false;
            OnLattice2DGrid.centerRadiation = false;
            OnLattice2DGrid.spatialRadiation = false;
            OnLattice2DGrid.tmzTimesteps = List.of(200, 400, 600, 800); // Pulsed delivery at intervals
            FigParameters.immuneSuppressionEffect = 0.1;
        } else if (scenario == 'H') // RT + TMZ (Concurrent)
        {
            OnLattice2DGrid.figure = 3;
            new FigParameters(OnLattice2DGrid.figure);
            OnLattice2DGrid.baseRadiationDose = 0;
            OnLattice2DGrid.appliedRadiationDose = 10;
            OnLattice2DGrid.radiationTimesteps = List.of(200, 400, 600); // Example of fractionated RT
            OnLattice2DGrid.totalRadiation = false;
            OnLattice2DGrid.centerRadiation = true;
            OnLattice2DGrid.spatialRadiation = false;
            OnLattice2DGrid.targetPercentage = 1;
            OnLattice2DGrid.tmzTimesteps = "continuous"; // Continuous TMZ during radiation
            FigParameters.immuneSuppressionEffect = 0.1;
        } else if (scenario == 'I') // RT + TMZ (Adjuvant)
        {
            OnLattice2DGrid.figure = 3;
            new FigParameters(OnLattice2DGrid.figure);
            OnLattice2DGrid.baseRadiationDose = 0;
            OnLattice2DGrid.appliedRadiationDose = 10;
            OnLattice2DGrid.radiationTimesteps = List.of(200, 400, 600); // RT first
            OnLattice2DGrid.totalRadiation = false;
            OnLattice2DGrid.centerRadiation = true;
            OnLattice2DGrid.spatialRadiation = false;
            OnLattice2DGrid.targetPercentage = 1;
            OnLattice2DGrid.tmzTimesteps = List.of(700, 800, 900); // TMZ after radiation
            FigParameters.immuneSuppressionEffect = 0.1;
        } else {
            System.err.printf("Invalid scenario: %s.%nPlease provide a valid scenario (A, B, C, D, E) or set 'scenarioActive' to false.%n", scenario);
            System.exit(0);
        }
    }
}
