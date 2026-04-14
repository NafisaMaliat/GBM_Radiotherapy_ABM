package OnLattice2DCells;

class TumorCells {
    public static String name = "Tumor Cells";

    // --- CLONE SETUP ---
    // How many clones we’re modelling
    public static final int NUM_CLONES = 3;

    // Stylised fractions: 60% baseline, 25% proliferative, 15% resistant
    public static final double[] INITIAL_CLONE_FRACTIONS = {0.6, 0.25, 0.15};

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

    // ----Helper to pick a clone id using 60/25/15 fractions ----
    // we take a random double in [0,1)
    public static int sampleInitialCloneId(double r) {
        double f0 = INITIAL_CLONE_FRACTIONS[0];
        double f1 = INITIAL_CLONE_FRACTIONS[1];

        if (r < f0) {
            return 0; // baseline
        } else if (r < f0 + f1) {
            return 1; // proliferative
        } else {
            return 2; // invasive / resistant
        }
    }
}
