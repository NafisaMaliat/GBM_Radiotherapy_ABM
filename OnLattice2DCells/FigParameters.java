package OnLattice2DCells;

class FigParameters {
    int figure;
    public static double radiationSensitivityOfTumorCellsAlpha; //null
    public static double radiationSensitivityOfTumorCellsBeta;  //null
    public static double radiationSensitivityOfLymphocytesAlpha; //null
    public static double radiationSensitivityOfLymphocytesBeta; //null
    public static double tumorGrowthRate;
    public static double tumorInfiltrationRate;
    public static double rateOfCellKilling;
    public static double decayConstantOfD;
    public static double decayConstantOfL;
    public static double recoveryConstantOfA;
    public static double radiationInducedInfiltration; //null
    public static double immuneSuppressionEffect;
    // Rate at which the post-radiation immune signal decays per timestep
    // decay = 0.02 → half-life ≈ 35 timesteps; signal reaches ~13% after 100 timesteps
    public static double immuneSignalDecayRate = 0.02;

// clone-specific multipliers
// index: 0 = Baseline, 1 = Proliferative, 2 = Invasive/Resistant

    // How fast each clone grows relative to baseline
    public static double[] cloneGrowthMultiplier = {
            1.0,  // baseline
            1.1,  // proliferative: 10% faster growth
            0.95   // invasive/resistant: 5% slower growth
    };

    // How sensitive each clone is to radiation (alpha & beta of LQ model)
    public static double[] cloneAlphaMultiplier = {
            1.0,  // baseline
            1.1,  // proliferative: slightly more radiosensitive
            0.4   // invasive/resistant: more radioresistant
    };

    // Relative radiation sensitivity (beta) for each clone
    public static double[] cloneBetaMultiplier = {
            1.0,    // baseline
            1.1,    // proliferative: slightly more radiosensitive
            0.4     // invasive/resistant: more radioresistant
    };

    // Relative immune kill strength for each clone or  how easily each clone is killed by immune system
    public static double[] cloneImmuneKillMultiplier = {
            1.0,  // baseline
            1.1,  // proliferative – more immunogenic (easier for immune system to kill)
            0.3   // invasive/resistant – more immune-evasive (harder to kill)
    };




    public FigParameters(int figure) {
        this.figure = figure;
        if (figure == 2) {   // Baseline Control , mimic a mouse model
            radiationSensitivityOfTumorCellsAlpha = 0; //null
            radiationSensitivityOfTumorCellsBeta = 0;  //null
            radiationSensitivityOfLymphocytesAlpha = 0; //null
            radiationSensitivityOfLymphocytesBeta = 0; //null
            tumorGrowthRate = 0.217; // tumour growth rate is fast in mouse
            tumorInfiltrationRate = 0.1;
            rateOfCellKilling = 0.05;
            decayConstantOfD = 0.039;
            decayConstantOfL = 0.335;
            recoveryConstantOfA = 0.039;
            radiationInducedInfiltration = 0; //null
            immuneSuppressionEffect = 0.012;

        } else if (figure == 3) {

//
//            radiationSensitivityOfTumorCellsAlpha = 0.36;
//            radiationSensitivityOfTumorCellsBeta = 0.02;
//
//            radiationSensitivityOfLymphocytesAlpha = 0.182;
//            radiationSensitivityOfLymphocytesBeta = 0.143;
//
//            tumorGrowthRate = 0.014;
//            tumorInfiltrationRate = 0.5;
//
//            rateOfCellKilling = 0.1; // 0.05-0.135
//            decayConstantOfD = 0.039;
//            decayConstantOfL = 0.335;
//            recoveryConstantOfA = 0.039;
//            radiationInducedInfiltration = 10;
//            immuneSuppressionEffect = 1.1;

            // Glioblastoma specific parameters, mimic a mouse model

            radiationSensitivityOfTumorCellsAlpha = 0.1;   // GBM preclinical: 0.1–0.3 Gy⁻¹ (was 0.05, too low)
            radiationSensitivityOfTumorCellsBeta = 0.0114;
            radiationSensitivityOfLymphocytesAlpha = 0.25;  // lymphocytes > tumour radiosensitivity (was 0.182)
            radiationSensitivityOfLymphocytesBeta = 0.025;  // alpha/beta ≈ 10 Gy — apoptotic death, not mitotic (was 0.143 → alpha/beta 1.3 Gy, wrong)
            tumorGrowthRate = 0.217;
            tumorInfiltrationRate = 0.1;
            rateOfCellKilling = 0.135;
            decayConstantOfD = 0.045;
            decayConstantOfL = 0.12;       // TIL half-life ~6 days
            recoveryConstantOfA = 0.045;
            radiationInducedInfiltration = 2;
            immuneSuppressionEffect = 1.1;
        }
//         else if (figure == 4) { //Highly Invasive/Highly Suppressed Scenario
//            radiationSensitivityOfTumorCellsAlpha = 0.05;
//            radiationSensitivityOfTumorCellsBeta = 0.0114;
//            radiationSensitivityOfLymphocytesAlpha = 0.182;
//            radiationSensitivityOfLymphocytesBeta = 0.143;
//            tumorGrowthRate = 0.217;
//            tumorInfiltrationRate = 0.5;
//            rateOfCellKilling = 0.135;
//            decayConstantOfD = 0.045;
//            decayConstantOfL = 0.045;
//            recoveryConstantOfA = 0.045;
//            radiationInducedInfiltration = 300;
//            immuneSuppressionEffect = 1.1;
//
//
//        } else if (figure == 5) { // same as 4
//            radiationSensitivityOfTumorCellsAlpha = 0.05;
//            radiationSensitivityOfTumorCellsBeta = 0.0114;
//            radiationSensitivityOfLymphocytesAlpha = 0.182;
//            radiationSensitivityOfLymphocytesBeta = 0.143;
//            tumorGrowthRate = 0.217;
//            tumorInfiltrationRate = 0.5;
//            rateOfCellKilling = 0.135;
//            decayConstantOfD = 0.045;
//            decayConstantOfL = 0.045;
//            recoveryConstantOfA = 0.045;
//            radiationInducedInfiltration = 300;
//            immuneSuppressionEffect = 1.1;
//
//
//        } else if (figure == 6) { //Radiation-Resistant / Low Growth Scenario
//            radiationSensitivityOfTumorCellsAlpha = 0.214;
//            radiationSensitivityOfTumorCellsBeta = 0.0214;
//            radiationSensitivityOfLymphocytesAlpha = 0.182;
//            radiationSensitivityOfLymphocytesBeta = 0.143;
//            tumorGrowthRate = 0.03;
//            tumorInfiltrationRate = 0.1;
//            rateOfCellKilling = 0.004;
//            decayConstantOfD = 0.045;
//            decayConstantOfL = 0.056;
//            recoveryConstantOfA = 0.045;
//            radiationInducedInfiltration = 4.6;
//            immuneSuppressionEffect = 0.5;
//
//
//        }
        else {
            System.err.println("Figure " + figure + " is not a valid figure number.");
            System.exit(0);
        }
    }
}
