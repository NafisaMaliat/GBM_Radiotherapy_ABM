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

    /**
     * Initializes the oxygen levels in a 2D field based on the dimensions of the grid.
     * Oxygen levels are higher near the edges of the field and lower near the center.
     * Each oxygen level is set to a value between 0.1 and 1.0, where 0.1 represents
     * the minimum oxygen level to prevent fully zero oxygen concentration.
     *
     * The initialization is performed by calculating the distance of each grid cell
     * from the center of the field and normalizing the value relative to the maximum
     * possible distance in the field.
     */
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

    /**
     * Updates the oxygen field by simulating oxygen diffusion and consumption.
     * The method calculates the new oxygen levels for each cell in the grid
     * based on a diffusion rate and the oxygen consumption rate of tumor cells.
     * Non-tumor cells do not consume oxygen. The new oxygen levels are capped
     * between 0.0 (minimum) and 1.0 (maximum).
     *
     * @param grid The 2D grid containing cell agents. Tumor cells in the grid
     *             consume oxygen, affecting the oxygen levels of the field.
     */
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

    /**
     * Calculates the average oxygen level within the tumor regions of the grid.
     * If there are no tumor cells present, defaults to a fully oxygenated value of 1.0.
     *
     * @param grid The 2D grid containing tumor cell regions. The grid provides
     *             the locations of tumor cells within the simulation space.
     * @return The average oxygen level across all tumor regions. Returns 1.0
     *         if the tumor region count is zero.
     */
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
