package OnLattice2DCells;

import HAL.Gui.GridWindow;

import java.util.ArrayList;
import java.util.List;

public class RadiationManager {
    private final OnLattice2DGrid grid;
    private final SimulationParameters params;

    public RadiationManager(OnLattice2DGrid grid,SimulationParameters params) {
        this.grid = grid;
        this.params = params;
    }

    public void centerRadiationArea(GridWindow win, int[] tumorCoord) {
        //int centerX = xDim/2; int centerY = yDim/2;
        int targetPixelsInCircle = (int) (TumorCells.count * params.targetPercentage);

        int radius = 0;
        for (int testRadius = (int) Math.sqrt(targetPixelsInCircle / Math.PI); testRadius < grid.xDim / 2; testRadius++) {
            params.radiatedPixels.clear();
            int count = 0;
            for (int i = tumorCoord[0]; i <= tumorCoord[1]; i++) {
                for (int j = tumorCoord[2]; j <= tumorCoord[3]; j++) {
                    if (isInsideCircle(i, j, tumorCoord[4], tumorCoord[5], testRadius)) {
                        params.radiatedPixels.add(new int[]{i, j});
                        if (grid.GetAgent(i, j) != null && grid.GetAgent(i, j).type == CellFunctions.Type.TUMOR) {
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

        if (tumorCoord[4] - params.radius - 1 >= 0 && tumorCoord[4] + params.radius + 1 < grid.xDim &&
                tumorCoord[5] - params.radius - 1 >= 0 && tumorCoord[5] + params.radius + 1 < grid.yDim) {
            tumorCenters.add(new int[]{tumorCoord[4], tumorCoord[5]});
            minX = tumorCoord[4];
            maxX = tumorCoord[4];
            minY = tumorCoord[5];
            maxY = tumorCoord[5];
        } else {
            System.out.println("Grid isn't big enough for spatial radiation with radius of " + params.radius);
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
                int xOffset = directions[i][0] * ((count + 1) * 2 + (2 * (count + 1)) * params.radius);
                int yOffset = directions[i][1] * ((count + 1) * 2 + (2 * (count + 1)) * params.radius);
                int newX = tumorCoord[4] + xOffset;
                int newY = tumorCoord[5] + yOffset;

                if (newX - params.radius - 1 >= 0 && newX + params.radius + 1 < grid.xDim &&
                        newY - params.radius - 1 >= 0 && newY + params.radius + 1 < grid.yDim) {
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
                    if (diagonalX - params.radius - 1 >= 0 && diagonalX + params.radius + 1 < grid.xDim &&
                            diagonalY - params.radius - 1 >= 0 && diagonalY + params.radius + 1 < grid.yDim &&
                            diagonalX != tumorCoord[4] && diagonalY != tumorCoord[5]) {
                        tumorCenters.add(new int[]{diagonalX, diagonalY});
                    }
                }
            }
        }

        minX = minX - params.radius;
        maxX = maxX + params.radius;
        minY = minY - params.radius;
        maxY = maxY + params.radius;

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
                    if (isInsideCircle(i, j, tumorCenters.get(k)[0], tumorCenters.get(k)[1], params.radius)) {
                        radiatedPixelCircle[k].add(new int[]{i, j});
                        if (grid.GetAgent(i, j) != null && grid.GetAgent(i, j).type == CellFunctions.Type.TUMOR) {
                            tumorCount[k]++;
                        } else if (grid.GetAgent(i, j) != null && grid.GetAgent(i, j).type == CellFunctions.Type.DOOMED) {
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
            if ((double) (tumorCount[k] + doomedCount[k]) / radiatedPixelCircle[k].size() >= params.thresholdPercentage) {
                params.radiatedPixels.addAll(radiatedPixelCircle[k]);
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
        SimulationParameters.currentRadiationDose = SimulationParameters.appliedRadiationDose;
        double LDieProb = CellFunctions.getLymphocytesProb(SimulationParameters.currentRadiationDose);
        double[] Tvalues = CellFunctions.getTumorCellsProb(SimulationParameters.currentRadiationDose);
        double[] Avalues = CellFunctions.getTriggeringCellsProb(SimulationParameters.currentRadiationDose);

        for (int[] pixel : params.radiatedPixels) {
            CellFunctions cell = grid.GetAgent(pixel[0], pixel[1]);
            if (cell != null) {
                cell.radiationDose = SimulationParameters.currentRadiationDose;
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
        SimulationParameters.currentRadiationDose = SimulationParameters.baseRadiationDose;

        for (int[] pixel : params.radiatedPixels) {
            CellFunctions cell = grid.GetAgent(pixel[0], pixel[1]);
            if (cell != null) {
                cell.radiationDose = SimulationParameters.currentRadiationDose;
                if (cell.type == CellFunctions.Type.LYMPHOCYTE) {
                    cell.dieProb = Lymphocytes.dieProb;
                }
            }
        }
    }
}
