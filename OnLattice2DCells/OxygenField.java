package OnLattice2DCells;

public class OxygenField {
    private double[][] oxygenLevels;
    private final int xDim;
    private final int yDim;

    public OxygenField(int xDim, int yDim) {
        this.xDim = xDim;
        this.yDim = yDim;
        initialiseOxygen();
    }

    public double getOxygenLevel(int x, int y) {
        return oxygenLevels[x][y];
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

    public void updateOxygenField(OnLattice2DGrid grid) {
        double[][] newOxygen = new double[xDim][yDim];
        double diffusionRate = 0.1;
        double consumptionRate = 0.02;

        for (int i = 1; i < xDim - 1; i++) {
            for (int j = 1; j < yDim - 1; j++) {
                double laplacian = oxygenLevels[i + 1][j] + oxygenLevels[i - 1][j] + oxygenLevels[i][j + 1] + oxygenLevels[i][j - 1] - 4 * oxygenLevels[i][j];
                double consumption = 0;
                CellFunctions cell = grid.GetAgent(i, j);
                if (cell != null && cell.type == CellFunctions.Type.TUMOR) {
                    consumption = consumptionRate;
                }
                newOxygen[i][j] = oxygenLevels[i][j] + diffusionRate * laplacian - consumption;
                newOxygen[i][j] = Math.max(0.0, Math.min(1.0, newOxygen[i][j]));
            }
        }

        oxygenLevels = newOxygen;
    }

    public double getAverageTumorOxygen(OnLattice2DGrid grid) {
        double totalOxygen = 0;
        int count = 0;

        for (int[] tumorSpace : grid.tumorSpaces) {
            totalOxygen += oxygenLevels[tumorSpace[0]][tumorSpace[1]];
            count++;
        }

        if (count == 0) {
            return 1.0; // Default to fully oxygenated if no tumor present
        }

        return totalOxygen / count;
    }
}
