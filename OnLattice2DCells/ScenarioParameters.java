package OnLattice2DCells;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

class ScenarioParameters {
    char scenario;

    public ScenarioParameters(char scenario, SimulationParameters params) {
        this.scenario = scenario;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String dateTime = LocalDateTime.now().format(formatter);

        String fileNameWithDate = Main.fileName1.replace(".csv", "_" + dateTime + ".csv");

        // Ensure the parent folder exists
        File parentFolder = new File(Main.directory);
        if (!parentFolder.exists()) {
            parentFolder.mkdirs();
        }

        // Create the scenario folder
        File scenarioFolder = new File(Main.directory, "Scenario" + scenario);
        if (!scenarioFolder.exists()) {
            scenarioFolder.mkdirs();
        }
        Main.fullPath1 = new File(scenarioFolder, fileNameWithDate).getAbsolutePath();

        if (scenario == 'A')   // Baseline, No Radiation, Weak Immune Suppression
        {
            Main.figure = 2;
            new FigParameters(Main.figure);
            params.totalRadiation = false;
            params.centerRadiation = false;
            params.spatialRadiation = false;
            FigParameters.immuneSuppressionEffect = 0.031;
        } else if (scenario == 'B')  // No Radiation, Stronger Immune Suppression
        {
            Main.figure = 3;
            new FigParameters(Main.figure);
            params.totalRadiation = false;
            params.centerRadiation = false;
            params.spatialRadiation = false;
            FigParameters.immuneSuppressionEffect = 0.1;
        } else if (scenario == 'C') //Centre Radiation Targeting 70% of Tumour
        {
            Main.figure = 3;
            new FigParameters(Main.figure);
            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 10;
            Main.radiationTimesteps = List.of(200);
            params.totalRadiation = false;
            params.centerRadiation = true;
            params.spatialRadiation = false;
            params.targetPercentage = 0.7;
            FigParameters.immuneSuppressionEffect = 0.1;
        } else if (scenario == 'D') // Centre Radiation Targeting 85% of Tumour
        {
            Main.figure = 3;
            new FigParameters(Main.figure);
            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 10;
            Main.radiationTimesteps = List.of(200);
            params.totalRadiation = false;
            params.centerRadiation = true;
            params.spatialRadiation = false;
            params.targetPercentage = .85;
            FigParameters.immuneSuppressionEffect = 0.1;
        } else if (scenario == 'E') //Centre Radiation Targeting 100% of Tumour
        {
            Main.figure = 3;
            new FigParameters(Main.figure);
            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 10;
            Main.radiationTimesteps = List.of(200);
            params.totalRadiation = false;
            params.centerRadiation = true;
            params.spatialRadiation = false;
            params.targetPercentage = 1;
            FigParameters.immuneSuppressionEffect = 0.1;
        } else if (scenario == 'F') // TMZ Continuous Therapy
        {
            Main.figure = 3;
            new FigParameters(Main.figure);
            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 0; // No radiation
            Main.radiationTimesteps = List.of(); // No radiation timesteps
            params.totalRadiation = false;
            params.centerRadiation = false;
            params.spatialRadiation = false;
            Main.tmzTimesteps = "continuous"; // Custom field you can create to track TMZ type
            FigParameters.immuneSuppressionEffect = 0.1;
        } else if (scenario == 'G') // TMZ Pulsed Therapy
        {
            Main.figure = 3;
            new FigParameters(Main.figure);
            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 0; // No radiation
            Main.radiationTimesteps = List.of(); // No radiation timesteps
            params.totalRadiation = false;
            params.centerRadiation = false;
            params.spatialRadiation = false;
            Main.tmzTimesteps = List.of(200, 400, 600, 800); // Pulsed delivery at intervals
            FigParameters.immuneSuppressionEffect = 0.1;
        } else if (scenario == 'H') // RT + TMZ (Concurrent)
        {
            Main.figure = 3;
            new FigParameters(Main.figure);
            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 10;
            Main.radiationTimesteps = List.of(200, 400, 600); // Example of fractionated RT
            params.totalRadiation = false;
            params.centerRadiation = true;
            params.spatialRadiation = false;
            params.targetPercentage = 1;
            Main.tmzTimesteps = "continuous"; // Continuous TMZ during radiation
            FigParameters.immuneSuppressionEffect = 0.1;
        } else if (scenario == 'I') // RT + TMZ (Adjuvant)
        {
            Main.figure = 3;
            new FigParameters(Main.figure);
            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 10;
            Main.radiationTimesteps = List.of(200, 400, 600); // RT first
            params.totalRadiation = false;
            params.centerRadiation = true;
            params.spatialRadiation = false;
            params.targetPercentage = 1;
            Main.tmzTimesteps = List.of(700, 800, 900); // TMZ after radiation
            FigParameters.immuneSuppressionEffect = 0.1;
        } else {
            System.err.printf("Invalid scenario: %s.%nPlease provide a valid scenario (A, B, C, D, E) or set 'scenarioActive' to false.%n", scenario);
            System.exit(0);
        }
    }
}
