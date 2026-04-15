package OnLattice2DCells;

class TumorCells {
    public static String name = "Tumor Cells";

    // --- CLONE SETUP ---
    // How many clones we’re modelling
    public static final int NUM_CLONES = 3;

    // Names and colours per clone
    public static String[] cloneNames = {
            "Baseline",          // clone 0
            "Proliferative",     // clone 1
            "InvasiveResistant"  // clone 2
    };

    // Color index per clone (HAL's Util.CategorialColor uses these)
    public static int[] cloneColorIndex = {
            1, // baseline – red
            10, // proliferative – light red
            8  // invasive/resistant – brown
    };

    // Per-clone counts
    public static int[] cloneCount    = new int[NUM_CLONES];
    public static int[] cloneCountRad = new int[NUM_CLONES];

    // Legacy global fields (still used in CSV, etc.)
    public static double dieProbRad;
    public static double dieProbImm;
    public static double divProb;

    // Per-clone average probabilities (updated each timestep in DrawModelandUpdateProb)
    public static double[] cloneDieProbRad = new double[NUM_CLONES];
    public static double[] cloneDieProbImm = new double[NUM_CLONES];
    public static double[] cloneDivProb    = new double[NUM_CLONES];
    public static int colorIndex = 1; // keep for now if used elsewhere

    public static int count;
    public static int countRad;

    // Utility to reset counts at the start of each timestep
    public static void resetCounts() {
        count = 0;
        countRad = 0;
        for (int i = 0; i < NUM_CLONES; i++) {
            cloneCount[i] = 0;
            cloneCountRad[i] = 0;
        }
    }

}
