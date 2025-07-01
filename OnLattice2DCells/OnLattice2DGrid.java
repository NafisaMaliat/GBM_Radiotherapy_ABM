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
    Rand rng = new Rand();
    int[] divHood = Util.VonNeumannHood(false);

    public static int figure = 3;
    public static int baseRadiationDose = 0, currentRadiationDose = baseRadiationDose, appliedRadiationDose = 10;
    public static List<Integer> radiationTimesteps = List.of(200);
    public static boolean totalRadiation = false, centerRadiation = true, spatialRadiation = false;
    public static double targetPercentage = 0.7;
    public static double thresholdPercentage = 0.8;
    public static int radius = 10;
    public static boolean scenarioActive = true;
    public static char scenario = 'E';

    public static double immuneResponse, primaryImmuneResponse, secondaryImmuneResponse = 0;
    public static int newLymphocytesAttempted;
    public static boolean triggeringDied;
    public static boolean immuneSuppressionEffectThreshold = false;

    public double[][] oxygenLevels;
    public static Object tmzTimesteps = null; // Can be "continuous" or List<Integer>


    public static List<int[]> availableSpaces = new ArrayList<>();
    public static List<int[]> tumorSpaces = new ArrayList<>();
    public static List<int[]> triggeringSpaces = new ArrayList<>();
    public static List<int[]> lymphocyteSpaces = new ArrayList<>();
    public static List<int[]> radiatedPixels = new ArrayList<>();
    public static List<int[]> allPixels = new ArrayList<>();
    public static int[][] lymphocyteNeighbors;

    public static final String directory = "HALModeling2024Outs";
    public static final String fileName1 = "TrialRunCounts.csv";
    public static String fullPath1 = directory + fileName1;
    public static final String fileName2 = "TrialRunProbabilities.csv";
    public static final String fullPath2 = directory + fileName2;
    public static final String fileName3 = "LymphocyteNeighbors.csv";
    public static final String fullPath3 = directory + fileName3;
    public static final boolean printCounts = true, printProbabilities = true, printNeighbors = true;
    public static boolean writeGIF = false;

    public OnLattice2DGrid(int x, int y) {
        super(x, y, CellFunctions.class);
        initialiseOxygen();
    }

    void initialiseOxygen() {
        oxygenLevels = new double[xDim][yDim];
        int centerX = xDim / 2;
        int centerY = yDim / 2;
        double maxDist = Math.sqrt(Math.pow(centerX, 2) + Math.pow(centerY, 2));

        for (int i = 0; i < xDim; i++) {
            for (int j = 0; j < yDim; j++) {
                double dist = Math.sqrt(Math.pow(i - centerX, 2) + Math.pow(j - centerY, 2));
                // Oxygen higher near edges, lower near centre
                oxygenLevels[i][j] = 1.0 - (dist / maxDist);
                oxygenLevels[i][j] = Math.max(0.1, oxygenLevels[i][j]); // Prevent fully zero oxygen
            }
        }
    }


    public void Init(GridWindow win, OnLattice2DGrid model) {
        if ((totalRadiation && centerRadiation) ||
                (totalRadiation && spatialRadiation) ||
                (centerRadiation && spatialRadiation)) {
            System.err.println("Two types of radiation are on; choose one for the model to run, or will not run as intended.");
            System.exit(0);
        }
        if (centerRadiation && (targetPercentage <= 0 || targetPercentage > 1)) {
            System.err.println(
                    "Error: Target percentage for center radiation must be greater than 0 and less than or equal to 1.\n" +
                            "Current values:\n" +
                            "  Center Radiation: " + centerRadiation + "\n" +
                            "  Target Percentage: " + targetPercentage + "\n" +
                            "Please update the targetPercentage to a valid value.");
            System.exit(0);
        } else if (spatialRadiation && (thresholdPercentage <= 0 || thresholdPercentage > 1)) {
            System.err.println(
                    "Error: Threshold percentage for spatial radiation must be greater than 0 and less than or equal to 1.\n" +
                            "Current values:\n" +
                            "  Spatial Radiation: " + spatialRadiation + "\n" +
                            "  Threshold Percentage: " + thresholdPercentage + "\n" +
                            "Please update the thresholdPercentage to a valid value.");
            System.exit(0);
        } else if (spatialRadiation && (radius <= 0 || radius > xDim / 2 || radius > yDim / 2)) {
            System.err.println(
                    "Error: Radius for spatial radiation must be greater than 0 and less than or equal to half the grid dimensions.\n" +
                            "Current values:\n" +
                            "  Spatial Radiation: " + spatialRadiation + "\n" +
                            "  Radius: " + radius + "\n" +
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

        currentRadiationDose = baseRadiationDose;
        Lymphocytes.dieProb = CellFunctions.getLymphocytesProb(baseRadiationDose);
        if (lymphocitePopulation > 0) {
            updateSpaces(win);
            if (tumorSize > 0) {
                OnLattice2DGrid.availableSpaces.removeIf(arr -> arr[0] == xDim / 2 && arr[1] == yDim / 2);

            }
            new CellFunctions().randomInitialization(this, lymphocitePopulation, CellFunctions.Type.LYMPHOCYTE);
        }

        TumorCells.count += tumorSize;
        if (immuneSuppressionEffectThreshold) {
            CellFunctions.getImmuneSuppressionEffectThreshold(Lymphocytes.count <= 1);
        }
        CellFunctions.getImmuneResponse();
        double[] Tvalues = CellFunctions.getTumorCellsProb(baseRadiationDose, 1.0);
        TumorCells.count -= tumorSize;
        TumorCells.dieProbRad = Tvalues[0];
        TumorCells.dieProbImm = Tvalues[1];
        TumorCells.divProb = Tvalues[2];

        if (tumorSize > 0) {
            model.NewAgentSQ(model.xDim / 2, model.yDim / 2).Init(CellFunctions.Type.TUMOR);
            TumorCells.count++;
        }
        if (tumorSize > 1) {
            for (int i = 0; i < tumorSize; i++) {
                for (CellFunctions cell : this) {
                    cell.mapEmptyHood();
                    if (TumorCells.count == tumorSize) {
                        i = tumorSize;
                        break;
                    }
                }
            }
        }

        double[] Avalues = CellFunctions.getTriggeringCellsProb(baseRadiationDose);
        TriggeringCells.dieProb = Avalues[1];
        TriggeringCells.activateProb = Avalues[1];
        if (triggeringPopulation > 0) {
            updateSpaces(win);
            new CellFunctions().randomInitialization(this, triggeringPopulation, CellFunctions.Type.TRIGGERING);
        }
    }

    public void StepCells(OnLattice2DGrid model) {
        triggeringDied = false;
        for (CellFunctions cell : this) //this is a for-each loop, "this" refers to this grid
        {
            cell.StepCell();
        }
        if (TriggeringCells.count > 0 && !triggeringDied) {
            new CellFunctions().disposeRandomTriggering(model);
        }
    }

    public void updateSpaces(GridWindow win) {
        availableSpaces.clear();
        tumorSpaces.clear();
        triggeringSpaces.clear();
        lymphocyteSpaces.clear();

        for (int i = 0; i < length; i++) {
            CellFunctions cell = GetAgent(i);
            if (cell == null) {
                cell = NewAgentSQ(i);
                availableSpaces.add(new int[]{cell.Xsq(), cell.Ysq()});
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

    public void updateOxygenField() {
        double[][] newOxygen = new double[xDim][yDim];
        double diffusionRate = 0.1;
        double consumptionRate = 0.02;

        for (int i = 1; i < xDim - 1; i++) {
            for (int j = 1; j < yDim - 1; j++) {
                double laplacian = oxygenLevels[i + 1][j] + oxygenLevels[i - 1][j] + oxygenLevels[i][j + 1] + oxygenLevels[i][j - 1] - 4 * oxygenLevels[i][j];
                double consumption = 0;
                CellFunctions cell = GetAgent(i, j);
                if (cell != null && cell.type == CellFunctions.Type.TUMOR) {
                    consumption = consumptionRate;
                }
                newOxygen[i][j] = oxygenLevels[i][j] + diffusionRate * laplacian - consumption;
                newOxygen[i][j] = Math.max(0.0, Math.min(1.0, newOxygen[i][j]));
            }
        }

        oxygenLevels = newOxygen;
    }

    public double getAverageTumorOxygen() {
        double totalOxygen = 0;
        int count = 0;

        for (int[] tumorSpace : tumorSpaces) {
            totalOxygen += oxygenLevels[tumorSpace[0]][tumorSpace[1]];
            count++;
        }

        if (count == 0) {
            return 1.0; // Default to fully oxygenated if no tumor present
        }

        return totalOxygen / count;
    }


    public void DrawModelandUpdateProb(GridWindow win, GifMaker gif) {
        int color;

        if (immuneSuppressionEffectThreshold) {
            CellFunctions.getImmuneSuppressionEffectThreshold(Lymphocytes.count <= 1);
        }
        CellFunctions.getImmuneResponse();
        double avgOxygen = getAverageTumorOxygen();
        double[] Tvalues = CellFunctions.getTumorCellsProb(baseRadiationDose, avgOxygen);
        TumorCells.dieProbRad = Tvalues[0];
        TumorCells.dieProbImm = Tvalues[1];
        TumorCells.divProb = Tvalues[2];
        double[] Avalues = CellFunctions.getTriggeringCellsProb(baseRadiationDose);
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
        if (writeGIF) gif.AddFrame(win);
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

    public void centerRadiationArea(GridWindow win, int[] tumorCoord) {
        //int centerX = xDim/2; int centerY = yDim/2;
        int targetPixelsInCircle = (int) (TumorCells.count * targetPercentage);

        int radius = 0;
        for (int testRadius = (int) Math.sqrt(targetPixelsInCircle / Math.PI); testRadius < xDim / 2; testRadius++) {
            radiatedPixels.clear();
            int count = 0;
            for (int i = tumorCoord[0]; i <= tumorCoord[1]; i++) {
                for (int j = tumorCoord[2]; j <= tumorCoord[3]; j++) {
                    if (isInsideCircle(i, j, tumorCoord[4], tumorCoord[5], testRadius)) {
                        radiatedPixels.add(new int[]{i, j});
                        if (GetAgent(i, j) != null && GetAgent(i, j).type == CellFunctions.Type.TUMOR) {
                            count++;
                        }
                    }
                }
            }
            if (count >= targetPixelsInCircle) {
                radius = testRadius;
                break;
            }
        }
    }

    public void spatialRadiationArea(GridWindow win, int[] tumorCoord) {
        List<int[]> tumorCenters = new ArrayList<>();
        List<int[]> combinedDirections = new ArrayList<>();
        int minX, maxX, minY, maxY;

        if (tumorCoord[4] - radius - 1 >= 0 && tumorCoord[4] + radius + 1 < xDim &&
                tumorCoord[5] - radius - 1 >= 0 && tumorCoord[5] + radius + 1 < yDim) {
            tumorCenters.add(new int[]{tumorCoord[4], tumorCoord[5]});
            minX = tumorCoord[4];
            maxX = tumorCoord[4];
            minY = tumorCoord[5];
            maxY = tumorCoord[5];
        } else {
            System.out.println("Grid isn't big enough for spatial radiation with radius of " + radius);
            return;
        }

        final int[][] directions = {
                {0, 1}, // N
                {1, 0},  // E
                {0, -1},  // S
                {-1, 0}, // W
        };

        for (int i = 0; i < 4; i++) {
            int count = 0;
            while (true) {
                int xOffset = directions[i][0] * ((count + 1) * 2 + (2 * (count + 1)) * radius);
                int yOffset = directions[i][1] * ((count + 1) * 2 + (2 * (count + 1)) * radius);
                int newX = tumorCoord[4] + xOffset;
                int newY = tumorCoord[5] + yOffset;

                if (newX - radius - 1 >= 0 && newX + radius + 1 < xDim &&
                        newY - radius - 1 >= 0 && newY + radius + 1 < yDim) {
                    tumorCenters.add(new int[]{newX, newY});
                    combinedDirections.add(new int[]{newX, newY}); // Store for diagonal checking
                    if (newX < minX)
                        minX = newX;
                    else if (newX > maxX)
                        maxX = newX;
                    if (newY < minY)
                        minY = newY;
                    else if (newY > maxY)
                        maxY = newY;
                    count++;
                } else {
                    break;
                }
            }
        }

        // Check additional combinations for diagonal overlaps
        for (int[] point1 : combinedDirections) {
            for (int[] point2 : combinedDirections) {
                if (point1 != point2) {
                    int diagonalX = point1[0];
                    int diagonalY = point2[1];
                    if (diagonalX - radius - 1 >= 0 && diagonalX + radius + 1 < xDim &&
                            diagonalY - radius - 1 >= 0 && diagonalY + radius + 1 < yDim &&
                            diagonalX != tumorCoord[4] && diagonalY != tumorCoord[5]) {
                        tumorCenters.add(new int[]{diagonalX, diagonalY});
                    }
                }
            }
        }

        minX = minX - radius;
        maxX = maxX + radius;
        minY = minY - radius;
        maxY = maxY + radius;

        //for (int[] center : tumorCenters) {System.out.println(Arrays.toString(center));}

        int numCenters = tumorCenters.size();
        List<int[]>[] radiatedPixelCircle = new ArrayList[numCenters];
        for (int k = 0; k < numCenters; k++) {
            radiatedPixelCircle[k] = new ArrayList<>();
        }
        int[] tumorCount = new int[numCenters];
        int[] doomedCount = new int[numCenters];

        for (int i = minX; i <= maxX; i++) {
            for (int j = minY; j <= maxY; j++) {
                for (int k = 0; k < numCenters; k++) {
                    if (isInsideCircle(i, j, tumorCenters.get(k)[0], tumorCenters.get(k)[1], radius)) {
                        radiatedPixelCircle[k].add(new int[]{i, j});
                        if (GetAgent(i, j) != null && GetAgent(i, j).type == CellFunctions.Type.TUMOR) {
                            tumorCount[k]++;
                        } else if (GetAgent(i, j) != null && GetAgent(i, j).type == CellFunctions.Type.DOOMED) {
                            doomedCount[k]++;
                        }
                        break; // No need to check other centers if this one matches
                    }
                }
            }
        }

        int count = 0;
        System.out.println("Attempting spatial radiation. " + numCenters + " circles being checked.");
        for (int k = 0; k < numCenters; k++) {
            if ((double) (tumorCount[k] + doomedCount[k]) / radiatedPixelCircle[k].size() >= thresholdPercentage) {
                radiatedPixels.addAll(radiatedPixelCircle[k]);
                count++;
            }
        }
        System.out.println("Circles radiated: " + count + "\n");

        //for (int[] pixel : radiatedPixels) {win.SetPix(pixel[0], pixel[1], Util.GREEN);}
    }

    public static boolean isInsideCircle(int i, int j, int centerX, int centerY, int radius) {
        int dx = i - centerX;
        int dy = j - centerY;
        return dx * dx + dy * dy <= radius * radius;
    }

    public void radiationApplied() {
        currentRadiationDose = appliedRadiationDose;
        double LDieProb = CellFunctions.getLymphocytesProb(currentRadiationDose);
        double avgOxygen = getAverageTumorOxygen();
        double[] Tvalues = CellFunctions.getTumorCellsProb(currentRadiationDose, avgOxygen);
        double[] Avalues = CellFunctions.getTriggeringCellsProb(currentRadiationDose);

        for (int[] pixel : radiatedPixels) {
            CellFunctions cell = GetAgent(pixel[0], pixel[1]);
            if (cell != null) {
                cell.radiationDose = currentRadiationDose;
                if (cell.type == CellFunctions.Type.LYMPHOCYTE) {
                    cell.dieProb = LDieProb;
                } else if (cell.type == CellFunctions.Type.TUMOR) {
                    cell.dieProbRad = Tvalues[0];
                    cell.dieProbImm = Tvalues[1];
                    cell.divProb = Tvalues[2];
                    if (!cell.radiated) {
                        TumorCells.countRad++;
                    }
                } else if (cell.type == CellFunctions.Type.TRIGGERING) {

                    cell.dieProb = Avalues[0];
                    cell.activateProb = Avalues[1];
                }
                cell.radiated = true;
            }
        }
    }

    public void radiationUnapplied() {
        currentRadiationDose = baseRadiationDose;

        for (int[] pixel : radiatedPixels) {
            CellFunctions cell = GetAgent(pixel[0], pixel[1]);
            if (cell != null) {
                cell.radiationDose = currentRadiationDose;
                if (cell.type == CellFunctions.Type.LYMPHOCYTE) {
                    cell.dieProb = Lymphocytes.dieProb;
                }
            }
        }
    }

    public void applyTMZ() {
        for (CellFunctions cell : this) {
            if (cell.type == CellFunctions.Type.TUMOR) {
                cell.dieProbRad += 0.005; // Example: increase death probability slightly
            }
        }
    }


    public String findColor(int colorIndex) {
        if (colorIndex == 0) {
            return "blue";
        } else if (colorIndex == 1) {
            return "red";
        } else if (colorIndex == 2) {
            return "green";
        } else if (colorIndex == 3) {
            return "yellow";
        } else if (colorIndex == 4) {
            return "orange";
        } else if (colorIndex == 5) {
            return "cyan";
        } else if (colorIndex == 6) {
            return "pink";
        } else if (colorIndex == 7) {
            return "blue";
        } else if (colorIndex == 8) {
            return "brown";
        } else if (colorIndex == 9) {
            return "light blue";
        } else if (colorIndex == 10) {
            return "light red";
        } else if (colorIndex == 11) {
            return "light green";
        } else if (colorIndex == 12) {
            return "light yellow";
        } else if (colorIndex == 13) {
            return "light purple";
        } else if (colorIndex == 14) {
            return "light orange";
        } else if (colorIndex == 15) {
            return "light cyan";
        } else if (colorIndex == 16) {
            return "light pink";
        } else if (colorIndex == 17) {
            return "light brown";
        } else if (colorIndex == 18) {
            return "light gray";
        } else if (colorIndex == 19) {
            return "dark gray";
        }
        return "unknown color";
    }

    public void saveCountsToCSV(String fullPath1, boolean append, int timestep) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullPath1, append))) {
            if (timestep == 0) {
                writer.write("Timestep,Lymphocytes,TriggeringCells,TumorCells,TumorCellsRad,DoomedCells," +
                        "DoomedCellsRad,Lymphocytes DieProb,Tumor DieProbRad,Tumor DieProbImm,Tumor DivProb," +
                        "SurvivingFractionTLast,PrimaryImmuneResponse,SecondaryImmuneResponse,ImmuneResponse," +
                        "LymphocyteMigrationAttempted,ImmuneSuppression");
                writer.newLine();
            }
            writer.write(timestep + "," + Lymphocytes.count + "," + TriggeringCells.count + "," + TumorCells.count + "," + TumorCells.countRad + "," +
                    DoomedCells.count + "," + DoomedCells.countRad + "," + Lymphocytes.dieProb + "," + TumorCells.dieProbRad + "," +
                    TumorCells.dieProbImm + "," + TumorCells.divProb + "," + TriggeringCells.SurvivingFractionTLast + "," +
                    OnLattice2DGrid.primaryImmuneResponse + "," + OnLattice2DGrid.secondaryImmuneResponse + "," +
                    OnLattice2DGrid.immuneResponse + "," + OnLattice2DGrid.newLymphocytesAttempted + "," + FigParameters.immuneSuppressionEffect);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write CSV file: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void saveProbabilitiesToCSV(String fullPath2, boolean append, int timestep, boolean duringRadiation) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullPath2, append))) {
            if (timestep == 0) {
                writer.write("Timestep,Cell,Type,Color,Radiated,RadiationDose,DeathFromRadiation," +
                        "DieProb,ActivateProb,DieProbRad,DieProbImm,DivProb,LymphocyteNeighbors");
                writer.newLine();
            }

            if (duringRadiation) {
                writer.write("Before Radiation Effects\n");
            }

            if (timestep >= 0) {
                for (int i = 0; i < length; i++) {
                    OnLattice2DCells.CellFunctions cell = GetAgent(i);
                    if (cell != null) {
                        writer.write(timestep + "," + cell + "," + cell.type + "," + cell.color + "," + cell.radiated + "," +
                                cell.radiationDose + "," + cell.deathFromRadiation + "," + cell.dieProb + "," + cell.activateProb + "," +
                                cell.dieProbRad + "," + cell.dieProbImm + "," + cell.divProb + "," + lymphocyteNeighbors[cell.Xsq()][cell.Ysq()]);
                        writer.newLine();
                    }
                }
            }

            if (duringRadiation) {
                writer.write("\nAfter Radiation Effects");
            }

            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write CSV file: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void saveLymphocyteNeighborstoCSV(String fullPath3, boolean append, int timestep) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullPath3, append))) {
            if (timestep == 0) {
                writer.write("Timestep,Type,Lymphocyte Neighbors");
                writer.newLine();
            }
            if (timestep >= 0) {
                for (int i = 0; i < length; i++) {
                    OnLattice2DCells.CellFunctions cell = GetAgent(i);
                    if (cell != null) {
                        writer.write(timestep + "," + cell.type + "," + lymphocyteNeighbors[cell.Xsq()][cell.Ysq()]);
                        writer.newLine();
                    } else {
                        cell = NewAgentSQ(i);
                        writer.write(timestep + ",empty," + lymphocyteNeighbors[cell.Xsq()][cell.Ysq()]);
                        writer.newLine();
                        cell.Dispose();
                    }
                }
                /* Alternate Visual Format:
                writer.write(timestep  + ",");
                for (int i = 0; i < xDim; i++)
                {
                    writer.write(i + (i < xDim - 1 ? "," : ""));
                }
                writer.newLine();
                for (int j = 0; j < yDim; j++)
                {
                    writer.write(j + "");
                    for (int i = 0; i < xDim; i++)
                    {
                        String cellContent;
                        OnLattice2DCells.CellFunctions cell = GetAgent(i, j);
                        if (cell != null)
                        {
                            cellContent = cell.type + " " + lymphocyteNeighbors[i][j];
                        }
                        else
                        {
                            cellContent = String.valueOf(lymphocyteNeighbors[i][j]);
                        }
                        writer.write("," + cellContent);
                    }
                    writer.newLine();
                }*/
            }

            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write CSV file: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void printPopulation(String name, int colorIndex, int count) {
        System.out.println("Population of " + name + " (" + findColor(colorIndex) + "): " + count);
    }

    public static void main(String[] args) {
        System.out.print("Scenario Active: " + scenarioActive);
        if (scenarioActive) {
            System.out.print("    Scenario: " + scenario);
            new ScenarioParameters(scenario);
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

        new Lymphocytes().Lymphocytes();
        new TumorCells().TumorCells();
        new DoomedCells().DoomedCells();
        new TriggeringCells().TriggeringCells();

        model.Init(win, model);
        if (printCounts) model.saveCountsToCSV(fullPath1, false, 0);
        if (printProbabilities) model.saveProbabilitiesToCSV(fullPath2, false, 0, false);
        if (printNeighbors) model.saveLymphocyteNeighborstoCSV(fullPath3, false, 0);

        GifMaker gif = new GifMaker(directory + "TrialRunGif.gif", 1, false);

        for (int i = 1; i <= timesteps; i++) {
            win.TickPause(1);

            if (radiationTimesteps.contains(i) && TumorCells.count > 20) {
                if (totalRadiation) {
                    radiatedPixels.addAll(allPixels);
                    model.radiationApplied();
                } else if (centerRadiation) {
                    model.centerRadiationArea(win, new OnLattice2DGrid(x, y).getTumorCoord());
                    model.radiationApplied();
                } else if (spatialRadiation) {
                    model.spatialRadiationArea(win, new OnLattice2DGrid(x, y).getTumorCoord());
                    model.radiationApplied();
                }
                if (printProbabilities) model.saveProbabilitiesToCSV(fullPath2, true, i, true);

            } else if (radiationTimesteps.contains(i - 1)) {
                model.radiationUnapplied();
                radiatedPixels.clear();
            }

            model.StepCells(model);

            model.updateSpaces(win);

            // Update oxygen AFTER cells have moved/divided/died
            model.updateOxygenField();

            // Lymphocyte Migration
            if (TriggeringCells.count > 0) {
                new CellFunctions().lymphocyteMigration(model, win);
            }
            // Apply TMZ effect
            if ((tmzTimesteps instanceof String && tmzTimesteps.equals("continuous")) ||
                    (tmzTimesteps instanceof List && ((List<Integer>) tmzTimesteps).contains(i))) {
                // Apply TMZ effect on tumor cells
                model.applyTMZ();
            }


            if (printCounts) model.saveCountsToCSV(fullPath1, true, i);
            if (printProbabilities) model.saveProbabilitiesToCSV(fullPath2, true, i, false);
            if (printNeighbors) model.saveLymphocyteNeighborstoCSV(fullPath3, true, i);

            if (i == timesteps) writeGIF = true;
            model.DrawModelandUpdateProb(win, gif); //get occupied spaces to use for stepCells method, rerun if model pop goes to 0

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

        model.printPopulation(Lymphocytes.name, Lymphocytes.colorIndex, Lymphocytes.count);
        model.printPopulation(TumorCells.name, TumorCells.colorIndex, TumorCells.count);
        model.printPopulation(DoomedCells.name, DoomedCells.colorIndex, DoomedCells.count);
        model.printPopulation(TriggeringCells.name, TriggeringCells.colorIndex, TriggeringCells.count);
        System.out.println("Population Total: " + model.Pop());
        model.updateSpaces(win);
        System.out.println("Unoccupied Spaces: " + availableSpaces.size());
        System.out.println();

        System.out.println(java.time.LocalDateTime.now() + " - Simulation execution completed successfully.");

        win.Close(); // Close GUI window
        System.exit(0); // Terminate Java process
    }
}
