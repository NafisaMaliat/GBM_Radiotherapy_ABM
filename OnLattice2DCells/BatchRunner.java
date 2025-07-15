package OnLattice2DCells;

import java.io.File;

public class BatchRunner {

    public static void main(String[] args) {
        String[] scenarios = {
                "Control","BB5", "BB10", "BB15",
                "MRT200", "MRT400", "MRT600",
                "MB180", "MB350","Pred_MRT180", "Pred_MRT350","Pred_MB200", "Pred_MB400","Pred_MB600"
        };

        int trialsPerScenario = 10;

        for (String scenario : scenarios) {
            for (int trial = 1; trial <= trialsPerScenario; trial++) {
                System.out.println("Running Scenario: " + scenario + " | Trial: " + trial);

                // Run the simulation
                Main.main(new String[]{scenario});
            }
        }

        System.out.println("\nAll simulations completed.");
    }
}
