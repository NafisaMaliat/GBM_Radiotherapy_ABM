package OnLattice2DCells;

import HAL.GridsAndAgents.AgentGrid2D;
import HAL.Gui.GridWindow;
import HAL.Rand;
import HAL.Util;
import HAL.Gui.GifMaker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;



public class OnLattice2DGrid extends AgentGrid2D<CellFunctions> {
    private final OxygenField oxygenField;

    Rand rng = new Rand();
    int[] divHood = Util.VonNeumannHood(false);
    public static double immuneResponse, primaryImmuneResponse, secondaryImmuneResponse = 0;
    public static int newLymphocytesAttempted;
    public static boolean triggeringDied;
    public static List<int[]> tumorSpaces = new ArrayList<>();
    public static List<int[]> triggeringSpaces = new ArrayList<>();
    public static List<int[]> lymphocyteSpaces = new ArrayList<>();
    public static int[][] lymphocyteNeighbors;



    public OnLattice2DGrid(int x, int y) {
        super(x, y, CellFunctions.class);
        this.oxygenField  = new OxygenField(xDim, yDim);
    }

    public OxygenField getOxygenField() {
        return oxygenField;
    }

    public void updateOxygenField() {
        oxygenField.updateOxygenField(this);
    }

    public double getAverageTumorOxygen() {
        return oxygenField.getAverageTumorOxygen(this);
    }

    public void Init(GridWindow win, OnLattice2DGrid model, SimulationParameters params) {

        if ((params.totalRadiation && params.centerRadiation) ||
                (params.totalRadiation && params.spatialRadiation) ||
                (params.centerRadiation && params.spatialRadiation)) {
            System.err.println("Two types of radiation are on; choose one for the model to run, or will not run as intended.");
            System.exit(0);
        }
        if (params.centerRadiation && (params.targetPercentage <= 0 || params.targetPercentage > 1)) {
            System.err.println(
                    "Error: Target percentage for center radiation must be greater than 0 and less than or equal to 1.\n" +
                            "Current values:\n" +
                            "  Center Radiation: " + params.centerRadiation + "\n" +
                            "  Target Percentage: " + params.targetPercentage + "\n" +
                            "Please update the targetPercentage to a valid value.");
            System.exit(0);
        } else if (params.spatialRadiation && (params.thresholdPercentage <= 0 || params.thresholdPercentage > 1)) {
            System.err.println(
                    "Error: Threshold percentage for spatial radiation must be greater than 0 and less than or equal to 1.\n" +
                            "Current values:\n" +
                            "  Spatial Radiation: " + params.spatialRadiation + "\n" +
                            "  Threshold Percentage: " + params.thresholdPercentage + "\n" +
                            "Please update the thresholdPercentage to a valid value.");
            System.exit(0);
        } else if (params.spatialRadiation && (params.radius <= 0 || params.radius > xDim / 2 || params.radius > yDim / 2)) {
            System.err.println(
                    "Error: Radius for spatial radiation must be greater than 0 and less than or equal to half the grid dimensions.\n" +
                            "Current values:\n" +
                            "  Spatial Radiation: " + params.spatialRadiation + "\n" +
                            "  Radius: " + params.radius + "\n" +
                            "  Grid Dimensions: xDim = " + xDim + ", yDim = " + yDim + "\n" +
                            "Please update the radius to a valid value.");
            System.exit(0);
        }

        lymphocyteNeighbors = new int[model.xDim][model.yDim];
        int lymphocitePopulation = 0;
        int tumorSize = 1;
        int triggeringPopulation = 500;
        if (lymphocitePopulation + tumorSize + triggeringPopulation > model.xDim * model.yDim) {
            System.err.println("Error: Number of cells exceeds grid size.\n" +
                    "Maximum Grid Capacity: " + (model.xDim * model.yDim) + " cells");
            System.exit(0);
        }

        params.currentRadiationDose = params.baseRadiationDose;
        Lymphocytes.dieProb = CellFunctions.getLymphocytesProb(params.baseRadiationDose);
        if (lymphocitePopulation > 0) {
            updateSpaces(win, params);
            if (tumorSize > 0) {
                params.availableSpaces.removeIf(arr -> arr[0] == xDim / 2 && arr[1] == yDim / 2);

            }
            new CellFunctions().randomInitialization(this, lymphocitePopulation, CellFunctions.Type.LYMPHOCYTE, params);
        }

        TumorCells.count += tumorSize;
        if (params.immuneSuppressionEffectThreshold) {
            CellFunctions.getImmuneSuppressionEffectThreshold(Lymphocytes.count <= 1);
        }
        CellFunctions.getImmuneResponse();
        double[] Tvalues = CellFunctions.getTumorCellsProb(params.baseRadiationDose, 1.0);
        TumorCells.count -= tumorSize;
        TumorCells.dieProbRad = Tvalues[0];
        TumorCells.dieProbImm = Tvalues[1];
        TumorCells.divProb = Tvalues[2];

        if (tumorSize > 0) {
            model.NewAgentSQ(model.xDim / 2, model.yDim / 2).Init(CellFunctions.Type.TUMOR, params);
            TumorCells.count++;
        }
        if (tumorSize > 1) {
            for (int i = 0; i < tumorSize; i++) {
                for (CellFunctions cell : this) {
                    cell.mapEmptyHood(params);
                    if (TumorCells.count == tumorSize) {
                        i = tumorSize;
                        break;
                    }
                }
            }
        }

        double[] Avalues = CellFunctions.getTriggeringCellsProb(SimulationParameters.baseRadiationDose);
        TriggeringCells.dieProb = Avalues[1];
        TriggeringCells.activateProb = Avalues[1];
        if (triggeringPopulation > 0) {
            updateSpaces(win, params);
            new CellFunctions().randomInitialization(this, triggeringPopulation, CellFunctions.Type.TRIGGERING, params);
        }
    }

    public void StepCells(OnLattice2DGrid model , SimulationParameters params) {
        triggeringDied = false;
        for (CellFunctions cell : this) //this is a for-each loop, "this" refers to this grid
        {
            cell.StepCell(params);
        }
        if (TriggeringCells.count > 0 && !triggeringDied) {
            new CellFunctions().disposeRandomTriggering(model);
        }
    }

    public void updateSpaces(GridWindow win, SimulationParameters params) {
        params.availableSpaces.clear();
        tumorSpaces.clear();
        triggeringSpaces.clear();
        lymphocyteSpaces.clear();

        for (int i = 0; i < length; i++) {
            CellFunctions cell = GetAgent(i);
            if (cell == null) {
                cell = NewAgentSQ(i);
                params.availableSpaces.add(new int[]{cell.Xsq(), cell.Ysq()});
                cell.Dispose();
            } else if (cell.type == CellFunctions.Type.TUMOR) {
                tumorSpaces.add(new int[]{cell.Xsq(), cell.Ysq()});
            } else if (cell.type == CellFunctions.Type.TRIGGERING) {
                triggeringSpaces.add(new int[]{cell.Xsq(), cell.Ysq()});
            } else if (cell != null && cell.type == CellFunctions.Type.LYMPHOCYTE) {
                lymphocyteSpaces.add(new int[]{cell.Xsq(), cell.Ysq()});
            }
            /* Didn't put condition for doomed cell spaces because not necessary for this algorithm. If add it later,
            then need to add the condition cell != null and just make availableSpaces last condition
             */
        }
    }

    public void DrawModelandUpdateProb(GridWindow win, GifMaker gif, SimulationParameters params) {
        int color;

        if (params.immuneSuppressionEffectThreshold) {
            CellFunctions.getImmuneSuppressionEffectThreshold(Lymphocytes.count <= 1);
        }
        CellFunctions.getImmuneResponse();
        double avgOxygen = oxygenField.getAverageTumorOxygen(this);
        double[] Tvalues = CellFunctions.getTumorCellsProb(params.baseRadiationDose, avgOxygen);
        TumorCells.dieProbRad = Tvalues[0];
        TumorCells.dieProbImm = Tvalues[1];
        TumorCells.divProb = Tvalues[2];
        double[] Avalues = CellFunctions.getTriggeringCellsProb(params.baseRadiationDose);
        TriggeringCells.dieProb = Avalues[0];
        TriggeringCells.activateProb = Avalues[1];

        for (int i = 0; i < length; i++) {
            CellFunctions cell = GetAgent(i);
            if (cell != null) {
                color = cell.color;
                if (cell.type == CellFunctions.Type.TUMOR) {
                    cell.dieProbRad = TumorCells.dieProbRad;
                    cell.dieProbImm = TumorCells.dieProbImm;
                    cell.divProb = TumorCells.divProb;
                    //If radiating twice in a row, this is not needed only for tumor cells in the circle. But not worth writing code for.
                } else if (cell.type == CellFunctions.Type.TRIGGERING) {
                    cell.dieProb = TriggeringCells.dieProb;
                    cell.activateProb = TriggeringCells.activateProb;
                }
            } else {
                color = Util.BLACK;
            }
            win.SetPix(i, color);
        }
        if (Main.writeGIF) gif.AddFrame(win);
    }

    public int[] getTumorCoord() {
        int minX = tumorSpaces.get(0)[0];
        int maxX = tumorSpaces.get(tumorSpaces.size() - 1)[0];
        int minY = yDim;
        int maxY = 0;
        for (int[] tumorCell : tumorSpaces) {
            if (tumorCell[1] < minY) minY = tumorCell[1];
            if (tumorCell[1] > maxY) maxY = tumorCell[1];
        }
        int centerX = (minX + maxX) / 2;
        int centerY = (minY + maxY) / 2;
        return new int[]{minX, maxX, minY, maxY, centerX, centerY};
    }



    public void applyTMZ() {
        for (CellFunctions cell : this) {
            if (cell.type == CellFunctions.Type.TUMOR) {
                cell.dieProbRad += 0.005; // Example: increase death probability slightly
            }
        }
    }



}
