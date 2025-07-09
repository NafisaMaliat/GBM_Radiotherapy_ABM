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

            radiationSensitivityOfTumorCellsAlpha = 0.05;
            radiationSensitivityOfTumorCellsBeta = 0.0114;
            radiationSensitivityOfLymphocytesAlpha = 0.182;
            radiationSensitivityOfLymphocytesBeta = 0.143;
            tumorGrowthRate = 0.217;
            tumorInfiltrationRate = 0.5; // 0.1-0.2 (BB), 0.2-0.5 (MRT/MB, higher immune response)
            rateOfCellKilling = 0.135;
            decayConstantOfD = 0.045;
            decayConstantOfL = 0.045;
            recoveryConstantOfA = 0.045;
            radiationInducedInfiltration = 2; // 0.5-2 (Mild RT , BB5-BB15), 2-10 (Moderate RT, MRT200–400), 10-100 (Strong RT damage, MRT600 / MB350)
            immuneSuppressionEffect = 0.51; // 1.1 immune escape, suppressed scenario
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
