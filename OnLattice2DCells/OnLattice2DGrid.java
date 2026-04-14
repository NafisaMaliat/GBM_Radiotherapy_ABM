package OnLattice2DCells;

import HAL.GridsAndAgents.AgentGrid2D;
import HAL.Gui.GridWindow;
import HAL.Rand;
import HAL.Util;
import HAL.Gui.GifMaker;

import java.util.ArrayList;
import java.util.List;



public class OnLattice2DGrid extends AgentGrid2D<CellFunctions> {

    Rand rng = new Rand();
    int[] divHood = Util.VonNeumannHood(false);
    public static double immuneResponse, primaryImmuneResponse, secondaryImmuneResponse = 0;
    public static int newLymphocytesAttempted;
    public static boolean triggeringDied;
    public static List<int[]> tumorSpaces = new ArrayList<>();
    public static List<int[]> triggeringSpaces = new ArrayList<>();
    public static List<int[]> lymphocyteSpaces = new ArrayList<>();
    public static int[][] lymphocyteNeighbors;
    // Persists the radiation-induced immune activation signal across timesteps.
    // Set to 1.0 when radiation is applied; decays by immuneSignalDecayRate each timestep.
    public static double postRadiationSignal = 0.0;




    public OnLattice2DGrid(int x, int y) {
        super(x, y, CellFunctions.class);
    }


    /**
     * Initializes the simulation grid and related components for modeling tumor and immune cell interactions.
     * This method sets up the radiation parameters, validates input values, initializes lymphocyte and tumor cell populations,
     * and prepares relevant probabilities and agents for simulation.
     *
     * @param win The GridWindow instance used for displaying the simulation results.
     * @param model The OnLattice2DGrid instance representing the simulation grid.
     * @param params The SimulationParameters instance containing the configuration parameters for the simulation, such as radiation settings, cell populations, and probabilities
     * .
     */



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
        postRadiationSignal = 0.0;
        int lymphocitePopulation = 0;
        int tumorSize = TumorCells.NUM_CLONES; // one seed cell per clone
        int triggeringPopulation = 500;

        if (lymphocitePopulation + tumorSize + triggeringPopulation > model.xDim * model.yDim) {
            System.err.println("Error: Number of cells exceeds grid size.\n" +
                    "Maximum Grid Capacity: " + (model.xDim * model.yDim) + " cells");
            System.exit(0);
        }

        SimulationParameters.currentRadiationDose = SimulationParameters.baseRadiationDose;
        Lymphocytes.dieProb = CellFunctions.getLymphocytesProb(SimulationParameters.baseRadiationDose);
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

        // Nafisa
        CellFunctions.getImmuneResponse();
        double[] Tvalues = CellFunctions.getTumorCellsProb(
                SimulationParameters.baseRadiationDose,
                0 // baseline clone
        );        TumorCells.count -= tumorSize;
        TumorCells.dieProbRad = Tvalues[0];
        TumorCells.dieProbImm = Tvalues[1];
        TumorCells.divProb = Tvalues[2];

// --- Seed one cell per clone, spread around the grid centre ---
// Each clone starts at its own position so it can grow outward freely
// (a compact multi-cell cluster causes immediate contact inhibition).
        int cx = model.xDim / 2;
        int cy = model.yDim / 2;

        // Offset positions so the three seed cells are not adjacent to each other
        int[][] seedPositions = {
                {cx,     cy    },   // Clone 0 – Baseline
                {cx + 4, cy    },   // Clone 1 – Proliferative
                {cx,     cy + 4},   // Clone 2 – InvasiveResistant
        };

        for (int cloneIdx = 0; cloneIdx < TumorCells.NUM_CLONES; cloneIdx++) {
            int sx = seedPositions[cloneIdx][0];
            int sy = seedPositions[cloneIdx][1];
            CellFunctions seed = model.NewAgentSQ(sx, sy);
            seed.cloneId = cloneIdx;
            seed.Init(CellFunctions.Type.TUMOR, params);
            TumorCells.count++;
        }
        // Initialise count and cloneCount[] from the actual grid state (one-time scan at startup)
        TumorCells.count = 0;
        for (CellFunctions cell : this) {
            if (cell.type == CellFunctions.Type.TUMOR) {
                int c = cell.cloneId;
                if (c >= 0 && c < TumorCells.NUM_CLONES) {
                    TumorCells.count++;
                    TumorCells.cloneCount[c]++;
                }
            }
        }
        System.out.println("Initial tumour clone counts:");
        for (int i = 0; i < TumorCells.NUM_CLONES; i++) {
            System.out.println("  Clone " + i + " (" + TumorCells.cloneNames[i] + "): " + TumorCells.cloneCount[i]);
        }


        double[] Avalues = CellFunctions.getTriggeringCellsProb(SimulationParameters.baseRadiationDose);
        TriggeringCells.dieProb = Avalues[0];
        TriggeringCells.activateProb = Avalues[1];
        if (triggeringPopulation > 0) {
            updateSpaces(win, params);
            new CellFunctions().randomInitialization(this, triggeringPopulation, CellFunctions.Type.TRIGGERING, params);
        }
    }

    /**
     * Advances the simulation by executing a single step for all cells in the grid.
     * This method iterates over the grid, invoking the step functionality for each cell
     * and optionally disposing of random triggering cells if certain conditions are met.
     *
     * @param model The OnLattice2DGrid instance representing the simulation grid
     *              where the cell interactions and updates take place.
     * @param params The SimulationParameters instance containing the configuration
     *               parameters required for the cells' behavior during the simulation step.
     */

    // Nafisa
    public void StepCells(OnLattice2DGrid model, SimulationParameters params) {
        triggeringDied = false;

        // 1) advance each cell by one timestep
        for (CellFunctions cell : this)  //this is a for-each loop, "this" refers to this grid
        {
            cell.StepCell(params);
        }

        // 2) triggering cell logic (unchanged)
        if (TriggeringCells.count > 0 && !triggeringDied) {
            new CellFunctions().disposeRandomTriggering(model);
        }
    }


    /**
     * Updates the classification of spaces in the simulation grid.
     * This method iterates through all grid cells, clearing and updating lists for different types of spaces:
     * available spaces, tumor spaces, triggering spaces, and lymphocyte spaces. Each space type is populated based on
     * the state and type of the cell at the given grid location.
     *
     * @param win The GridWindow instance used for rendering and interacting with the simulation grid.
     * @param params The SimulationParameters instance containing configuration data, including the list of available spaces to update.
     */
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




    /**
     * Draws the current state of the simulation model on the provided grid window and updates the probabilities
     * for tumor and triggering cells based on the current simulation parameters. This method also adjusts
     * probabilities for immune suppression effects and optionally appends frames to a GIF if enabled.
     *
     * @param win The GridWindow instance that visually represents the current state of the simulation grid.
     * @param gif The GifMaker instance used to generate and save GIFs of the simulation if GIF generation is enabled.
     * @param params The SimulationParameters instance containing the configurable parameters such as radiation dose,
     *               immune suppression effects, and other associated properties relevant to the simulation dynamics.
     */
    public void DrawModelandUpdateProb(GridWindow win, GifMaker gif, SimulationParameters params) {
        int color;
        double tumorDieProbRadSum = 0;
        double tumorDieProbImmSum = 0;
        double tumorDivProbSum = 0;
        int tumorCellCount = 0;

        // Decay post-radiation immune signal each timestep
        postRadiationSignal *= (1.0 - FigParameters.immuneSignalDecayRate);

        if (params.immuneSuppressionEffectThreshold) {
            CellFunctions.getImmuneSuppressionEffectThreshold(Lymphocytes.count <= 1);
        }
        CellFunctions.getImmuneResponse();

        double[] Avalues = CellFunctions.getTriggeringCellsProb(SimulationParameters.baseRadiationDose);
        TriggeringCells.dieProb = Avalues[0];
        TriggeringCells.activateProb = Avalues[1];

        for (int i = 0; i < length; i++) {
            CellFunctions cell = GetAgent(i);
            if (cell != null) {
                color = cell.color;
                if (cell.type == CellFunctions.Type.TUMOR) {
                    double[] probs = CellFunctions.getTumorCellsProb(cell.radiationDose, cell.cloneId);
                    cell.dieProbRad = probs[0];
                    cell.dieProbImm = probs[1];
                    cell.divProb = probs[2];

                    tumorDieProbRadSum += probs[0];
                    tumorDieProbImmSum += probs[1];
                    tumorDivProbSum += probs[2];
                    tumorCellCount++;
                } else if (cell.type == CellFunctions.Type.TRIGGERING) {
                    cell.dieProb = TriggeringCells.dieProb;
                    cell.activateProb = TriggeringCells.activateProb;
                }
            } else {
                color = Util.BLACK;
            }
            win.SetPix(i, color);
        }
        if (tumorCellCount > 0) {
            TumorCells.dieProbRad = tumorDieProbRadSum / tumorCellCount;
            TumorCells.dieProbImm = tumorDieProbImmSum / tumorCellCount;
            TumorCells.divProb = tumorDivProbSum / tumorCellCount;
        } else {
            TumorCells.dieProbRad = 0;
            TumorCells.dieProbImm = 0;
            TumorCells.divProb = 0;
        }
        if (Main.writeGIF) gif.AddFrame(win);
    }

    /**
     * Calculates and returns the coordinates defining the boundaries and center of the tumor region.
     * This method iterates through the list of tumor cells to find the minimum and maximum
     * x and y coordinates, which represent the bounding box around the tumor. It also calculates
     * the center of this bounding box.
     *
     * @return An integer array containing, in order:
     *         - Minimum x-coordinate of the tumor region.
     *         - Maximum x-coordinate of the tumor region.
     *         - Minimum y-coordinate of the tumor region.
     *         - Maximum y-coordinate of the tumor region.
     *         - x-coordinate of the center of the tumor region.
     *         - y-coordinate of the center of the tumor region.
     */
    public int[] getTumorCoord() {
        if (tumorSpaces.isEmpty()) {
            return null;
        }

        int minX = xDim;
        int maxX = 0;
        int minY = yDim;
        int maxY = 0;
        for (int[] tumorCell : tumorSpaces) {
            if (tumorCell[0] < minX) minX = tumorCell[0];
            if (tumorCell[0] > maxX) maxX = tumorCell[0];
            if (tumorCell[1] < minY) minY = tumorCell[1];
            if (tumorCell[1] > maxY) maxY = tumorCell[1];
        }
        int centerX = (minX + maxX) / 2;
        int centerY = (minY + maxY) / 2;
        return new int[]{minX, maxX, minY, maxY, centerX, centerY};
    }



}
