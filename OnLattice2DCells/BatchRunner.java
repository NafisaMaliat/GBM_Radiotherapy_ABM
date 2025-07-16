package OnLattice2DCells;

import java.time.Duration;
import java.time.Instant;

public class BatchRunner {

    public static void main(String[] args) {
        String[] scenarios = {
                "Control", "BB5", "BB10", "BB15",
                "MRT200", "MRT400", "MRT600",
                "MB180", "MB350",
                "Pred_MRT180", "Pred_MRT350", "Pred_MB200", "Pred_MB400", "Pred_MB600"
        };

        int trialsPerScenario = 10;
        Instant batchStart = Instant.now();

        for (String scenario : scenarios) {
            for (int trial = 1; trial <= trialsPerScenario; trial++) {
                System.out.printf("Running Scenario: %-12s | Trial: %d%n", scenario, trial);

                Instant trialStart = Instant.now();

                // Run simulation with scenario as argument
                System.out.printf("▶️ Starting Scenario: %s | Trial: %d%n", scenario, trial);
                Main.main(new String[]{scenario});
                System.out.printf("✅ Finished Scenario: %s | Trial: %d%n%n", scenario, trial);


                Instant trialEnd = Instant.now();
                Duration trialDuration = Duration.between(trialStart, trialEnd);

                System.out.printf("Finished %s | Trial %d in %d seconds (%d mins)\n\n",
                        scenario, trial,
                        trialDuration.getSeconds(),
                        trialDuration.toMinutes());
            }
        }

        Instant batchEnd = Instant.now();
        Duration totalDuration = Duration.between(batchStart, batchEnd);

        System.out.println("All simulations completed.");
        System.out.printf("Total time taken: %d seconds (%d minutes)\n",
                totalDuration.getSeconds(),
                totalDuration.toMinutes());

        System.exit(0); // Terminate Java process
    }
}
