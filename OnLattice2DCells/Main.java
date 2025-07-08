package OnLattice2DCells;

import HAL.Gui.GifMaker;
import HAL.Gui.GridWindow;
import HAL.Rand;
import HAL.Util;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static int figure = 3;
    public static int baseRadiationDose = 0, currentRadiationDose = baseRadiationDose, appliedRadiationDose = 10;
    public static List<Integer> radiationTimesteps = List.of(200);
    public static boolean totalRadiation = false, centerRadiation = true, spatialRadiation = false;
    public static double targetPercentage = 0.7;
    public static double thresholdPercentage = 0.8;
    public static int radius = 10;
    public static boolean scenarioActive = true;
    public static char scenario = 'E';

    public static boolean immuneSuppressionEffectThreshold = false;


    public static Object tmzTimesteps = null; // Can be "continuous" or List<Integer>


    public static List<int[]> availableSpaces = new ArrayList<>();
    public static List<int[]> radiatedPixels = new ArrayList<>();
    public static List<int[]> allPixels = new ArrayList<>();

    public static final String directory = "HALModeling2024Outs";
    public static final String fileName1 = "TrialRunCounts.csv";
    public static String fullPath1 = directory + fileName1;
    public static final String fileName2 = "TrialRunProbabilities.csv";
    public static final String fullPath2 = directory + fileName2;
    public static final String fileName3 = "LymphocyteNeighbors.csv";
    public static final String fullPath3 = directory + fileName3;
    public static final boolean printCounts = true, printProbabilities = true, printNeighbors = true;
    public static boolean writeGIF = false;


    public static void main(String[] args) {
        SimulationParameters params = new SimulationParameters(targetPercentage, thresholdPercentage, radius,radiatedPixels, baseRadiationDose, currentRadiationDose, appliedRadiationDose, totalRadiation,centerRadiation,spatialRadiation,immuneSuppressionEffectThreshold, availableSpaces);

        System.out.print("Scenario Active: " + scenarioActive);
        if (scenarioActive) {
            System.out.print("    Scenario: " + scenario);
            new ScenarioParameters(scenario, params);
        } else {
            new FigParameters(figure);
        }
        System.out.println("\nFigure: " + figure + "\nTotal Radiation: " + totalRadiation +
                "   Center Radiation: " + centerRadiation + "    Spatial Radiation: " + spatialRadiation);
        if (totalRadiation || centerRadiation || spatialRadiation) {
            System.out.println("Base Radiation Dose: " + baseRadiationDose + " Gy\nApplied Radiation Dose: " + appliedRadiationDose + " Gy" +
                    "\nTimesteps Applied: " + radiationTimesteps);
        }
        if (centerRadiation) {
            System.out.println("Center radiation target percentage is " + targetPercentage);
        } else if (spatialRadiation) {
            System.out.println("Spatial radiation threshold percentage is " + thresholdPercentage + " and preset radius is " + radius);
        }
        if (!immuneSuppressionEffectThreshold) {
            System.out.println("Immune Suppression Effect: " + FigParameters.immuneSuppressionEffect);
        }
        System.out.println("\nSave Counts to CSV: " + printCounts + "   Save Probabilities to CSV: " + printProbabilities +
                "   Save GIF (slows code down): " + writeGIF + "\n");

        int x = 100;
        int y = 100;
        int timesteps = 1000;
        GridWindow win = new GridWindow(x, y, 5);
        OnLattice2DGrid model = new OnLattice2DGrid(x, y);
        for (int i = 0; i < model.xDim; i++) {
            for (int j = 0; j < model.yDim; j++) {
                allPixels.add(new int[]{i, j});
            }
        }
        RadiationManager radiationManager = new RadiationManager(model, params);

        new Lymphocytes().Lymphocytes();
        new TumorCells().TumorCells();
        new DoomedCells().DoomedCells();
        new TriggeringCells().TriggeringCells();

        model.Init(win, model,params);

        CSVWriter writer = new CSVWriter(model);

        if (printCounts) writer.saveCountsToCSV(fullPath1, false, 0);
        if (printProbabilities) writer.saveProbabilitiesToCSV(fullPath2, false, 0, false);
        if (printNeighbors) writer.saveLymphocyteNeighborstoCSV(fullPath3, false, 0);

        GifMaker gif = new GifMaker(directory + "TrialRunGif.gif", 1, false);

        for (int i = 1; i <= timesteps; i++) {
            win.TickPause(1);

            if (radiationTimesteps.contains(i) && TumorCells.count > 20) {
                if (totalRadiation) {
                    radiatedPixels.addAll(allPixels);
                    radiationManager.radiationApplied();
                } else if (centerRadiation) {
                    radiationManager.centerRadiationArea(win, new OnLattice2DGrid(x, y).getTumorCoord());
                    radiationManager.radiationApplied();
                } else if (spatialRadiation) {
                    radiationManager.spatialRadiationArea(win, new OnLattice2DGrid(x, y).getTumorCoord());
                    radiationManager.radiationApplied();
                }
                if (printProbabilities) writer.saveProbabilitiesToCSV(fullPath2, true, i, true);

            } else if (radiationTimesteps.contains(i - 1)) {
                radiationManager.radiationUnapplied();
                radiatedPixels.clear();
            }

            model.StepCells(model, params);

            model.updateSpaces(win,params);

            // Lymphocyte Migration
            if (TriggeringCells.count > 0) {
                new CellFunctions().lymphocyteMigration(model, win, params);
            }


            if (printCounts) writer.saveCountsToCSV(fullPath1, true, i);
            if (printProbabilities) writer.saveProbabilitiesToCSV(fullPath2, true, i, false);
            if (printNeighbors) writer.saveLymphocyteNeighborstoCSV(fullPath3, true, i);

            if (i == timesteps) writeGIF = true;
            model.DrawModelandUpdateProb(win, gif, params); //get occupied spaces to use for stepCells method, rerun if model pop goes to 0

            //if (model.Pop() == 0)
            if (TumorCells.count == 0) {
                System.out.println("Timestep tumor population reached 0: " + i + "\n");
                break;
                /*model.Init(win, model);
                if (printCounts) model.saveCountsToCSV(fullPath1, true, 0);
                if (printProbabilities) model.saveProbabilitiesToCSV(fullPath2, true, 0, win, false);
                if (printNeighbors) model.saveLymphocyteNeighborstoCSV(fullPath3, true, 0);
                i = 1;*/
            }
        }

        gif.Close();

        writer.printPopulation(Lymphocytes.name, Lymphocytes.colorIndex, Lymphocytes.count);
        writer.printPopulation(TumorCells.name, TumorCells.colorIndex, TumorCells.count);
        writer.printPopulation(DoomedCells.name, DoomedCells.colorIndex, DoomedCells.count);
        writer.printPopulation(TriggeringCells.name, TriggeringCells.colorIndex, TriggeringCells.count);
        System.out.println("Population Total: " + model.Pop());
        model.updateSpaces(win,params);
        System.out.println("Unoccupied Spaces: " + availableSpaces.size());
        System.out.println();

        System.out.println(java.time.LocalDateTime.now() + " - Simulation execution completed successfully.");

        win.Close(); // Close GUI window
        System.exit(0); // Terminate Java process
    }
}
