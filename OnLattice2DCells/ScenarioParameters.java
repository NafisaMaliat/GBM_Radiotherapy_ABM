package OnLattice2DCells;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

class ScenarioParameters {
    String scenario;

    public ScenarioParameters(String scenario, SimulationParameters params, boolean overwriteParams) {

        if (!overwriteParams) return;

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

        if (scenario.equalsIgnoreCase("Control") )  // Control, No Radiation, Strong Immune
        {
            Main.figure = 2;
            new FigParameters(Main.figure);
            params.totalRadiation = false;
            params.centerRadiation = false;
            params.spatialRadiation = false;
            FigParameters.immuneSuppressionEffect = 0.031;
        } else if (scenario.equalsIgnoreCase("B"))  // Control, No Radiation, Weak Immune
        {
            Main.figure = 3;
            new FigParameters(Main.figure);
            params.totalRadiation = false;
            params.centerRadiation = false;
            params.spatialRadiation = false;
            FigParameters.immuneSuppressionEffect = 0.1;
        } else if (scenario.equalsIgnoreCase("C")) //Centre Radiation Targeting 70% of Tumour
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
        } else if (scenario.equalsIgnoreCase( "D")) // Centre Radiation Targeting 85% of Tumour
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
        } else if (scenario.equalsIgnoreCase( "E")) //Centre Radiation Targeting 100% of Tumour
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
        }else if (scenario.equalsIgnoreCase("BB5")) // Broad Beam Radiation(Uniform dose), 5 Gy,
        {
            Main.figure = 3;
            new FigParameters(Main.figure);

            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 5;
            Main.radiationTimesteps = List.of(200);

            params.totalRadiation = true;
            params.centerRadiation = false;
            params.spatialRadiation = false;

            FigParameters.immuneSuppressionEffect = 0.51;   // 0.51 because in BB treatments (e.g., BB5, BB10), the tumour is not heavily damaged
            FigParameters.tumorInfiltrationRate = 0.1;
            FigParameters.radiationInducedInfiltration = 2.0;

        }
        else if (scenario.equalsIgnoreCase("BB10"))
        {
            Main.figure = 3;
            new FigParameters(Main.figure);

            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 10;
            Main.radiationTimesteps = List.of(200);

            params.totalRadiation = true;
            params.centerRadiation = false;
            params.spatialRadiation = false;

            FigParameters.immuneSuppressionEffect = 0.51;
            FigParameters.tumorInfiltrationRate = 0.1;
            FigParameters.radiationInducedInfiltration = 2.0;

        }
        else if (scenario.equalsIgnoreCase("BB15"))
        {
            Main.figure = 3;
            new FigParameters(Main.figure);

            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 15;
            Main.radiationTimesteps = List.of(200);

            params.totalRadiation = true;
            params.centerRadiation = false;
            params.spatialRadiation = false;

            FigParameters.immuneSuppressionEffect = 0.51;
            FigParameters.tumorInfiltrationRate = 0.1;
            FigParameters.radiationInducedInfiltration = 2.0;

        }
        else if (scenario.equalsIgnoreCase("MRT200"))
        {
            Main.figure = 3;
            new FigParameters(Main.figure);

            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 200;
            Main.radiationTimesteps = List.of(200);

            params.totalRadiation = false;
            params.centerRadiation = false;
            params.spatialRadiation = true;

            FigParameters.immuneSuppressionEffect = 1.1; // 1.1 because MRT and MB cause extensive physical damage to tumour cells and vasculature.
            FigParameters.tumorInfiltrationRate = 0.1;
            FigParameters.radiationInducedInfiltration = 50.0 ; //Strong radiation damage leads to higher infiltration

            params.radius = 5; // <- Smaller beams for MRT
        }
        else if (scenario.equalsIgnoreCase("MRT400"))
        {

            Main.figure = 3;
            new FigParameters(Main.figure);

            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 400;
            Main.radiationTimesteps = List.of(200);

            params.totalRadiation = false;
            params.centerRadiation = false;
            params.spatialRadiation = true;

            FigParameters.immuneSuppressionEffect = 1.1;
            FigParameters.tumorInfiltrationRate = 0.1;
            FigParameters.radiationInducedInfiltration = 75.0;

            params.radius = 5; // <- Smaller beams for MRT
        }
        else if (scenario.equalsIgnoreCase("MRT600"))
        {
            Main.figure = 3;
            new FigParameters(Main.figure);

            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 600;
            Main.radiationTimesteps = List.of(200);

            params.totalRadiation = false;
            params.centerRadiation = false;
            params.spatialRadiation = true;

            FigParameters.immuneSuppressionEffect = 1.1;
            FigParameters.tumorInfiltrationRate = 0.1;
            FigParameters.radiationInducedInfiltration = 100.0;

            params.radius = 5; // <- Smaller beams for MRT
        }
        else if (scenario.equalsIgnoreCase("MB180"))
        {
            Main.figure = 3;
            new FigParameters(Main.figure);

            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 180;
            Main.radiationTimesteps = List.of(200);

            params.totalRadiation = false;
            params.centerRadiation = false;
            params.spatialRadiation = true;

            FigParameters.immuneSuppressionEffect = 1.1;
            FigParameters.tumorInfiltrationRate = 0.1;
            FigParameters.radiationInducedInfiltration = 30.0;

            params.radius = 10; // <- Thicker beams
        }
        else if (scenario.equalsIgnoreCase("MB350"))
        {
            Main.figure = 3;
            new FigParameters(Main.figure);

            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 350;
            Main.radiationTimesteps = List.of(200);

            params.totalRadiation = false;
            params.centerRadiation = false;
            params.spatialRadiation = true;

            FigParameters.immuneSuppressionEffect = 1.1;
            FigParameters.tumorInfiltrationRate = 0.1;
            FigParameters.radiationInducedInfiltration = 60.0;

            params.radius = 10; // <- Thicker beams
        }
        else if (scenario.equalsIgnoreCase("Pred_MRT180"))
        {
            Main.figure = 3;
            new FigParameters(Main.figure);

            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 180;
            Main.radiationTimesteps = List.of(200);

            params.totalRadiation = false;
            params.centerRadiation = false;
            params.spatialRadiation = true;

            FigParameters.immuneSuppressionEffect = 1.1;
            FigParameters.tumorInfiltrationRate = 0.1;
            FigParameters.radiationInducedInfiltration = 50.0;

            params.radius = 5;
        }
        else if (scenario.equalsIgnoreCase("Pred_MRT350"))
        {
            Main.figure = 3;
            new FigParameters(Main.figure);

            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 350;
            Main.radiationTimesteps = List.of(200);

            params.totalRadiation = false;
            params.centerRadiation = false;
            params.spatialRadiation = true;

            FigParameters.immuneSuppressionEffect = 1.1;
            FigParameters.tumorInfiltrationRate = 0.1;
            FigParameters.radiationInducedInfiltration = 75.0;

            params.radius = 5;
        }
        else if (scenario.equalsIgnoreCase("Pred_MB200"))
        {
            Main.figure = 3;
            new FigParameters(Main.figure);

            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 200;
            Main.radiationTimesteps = List.of(200);

            params.totalRadiation = false;
            params.centerRadiation = false;
            params.spatialRadiation = true;

            FigParameters.immuneSuppressionEffect = 1.1;
            FigParameters.tumorInfiltrationRate = 0.1;
            FigParameters.radiationInducedInfiltration = 30.0;

            params.radius = 10; // <- Thicker beams
        }
        else if (scenario.equalsIgnoreCase("Pred_MB400"))
        {
            Main.figure = 3;
            new FigParameters(Main.figure);

            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 400;
            Main.radiationTimesteps = List.of(200);

            params.totalRadiation = false;
            params.centerRadiation = false;
            params.spatialRadiation = true;

            FigParameters.immuneSuppressionEffect = 1.1;
            FigParameters.tumorInfiltrationRate = 0.1;
            FigParameters.radiationInducedInfiltration = 60.0;

            params.radius = 10; // <- Thicker beams
        }
        else if (scenario.equalsIgnoreCase("Pred_MB600"))
        {
            Main.figure = 3;
            new FigParameters(Main.figure);

            SimulationParameters.baseRadiationDose = 0;
            SimulationParameters.appliedRadiationDose = 600;
            Main.radiationTimesteps = List.of(200);

            params.totalRadiation = false;
            params.centerRadiation = false;
            params.spatialRadiation = true;

            FigParameters.immuneSuppressionEffect = 1.1;
            FigParameters.tumorInfiltrationRate = 0.1;
            FigParameters.radiationInducedInfiltration = 100.0;

            params.radius = 10; // <- Thicker beams
        }
        else {
            System.err.printf("Invalid scenario: %s.%nPlease provide a valid scenario or set 'scenarioActive' to false.%n", scenario);
            System.exit(0);
        }

        System.out.println("\nFigure: " + Main.figure + "\nTotal Radiation: " + params.totalRadiation +
                "   Center Radiation: " + params.centerRadiation + "    Spatial Radiation: " + params.spatialRadiation);
        if (params.totalRadiation || params.centerRadiation || params.spatialRadiation) {
            System.out.println("Base Radiation Dose: " + SimulationParameters.baseRadiationDose + " Gy\nApplied Radiation Dose: " + SimulationParameters.appliedRadiationDose + " Gy" +
                    "\nTimesteps Applied: " + Main.radiationTimesteps);
        }
        if (params.centerRadiation) {
            System.out.println("Center radiation target percentage is " + Main.targetPercentage);
        } else if (params.spatialRadiation) {
            System.out.println("Spatial radiation threshold percentage is " + Main.thresholdPercentage + " and preset radius is " + params.radius);
        }
        if (!params.immuneSuppressionEffectThreshold) {
            System.out.println("Immune Suppression Effect: " + FigParameters.immuneSuppressionEffect);
        }
    }
}
