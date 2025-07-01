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
        if (figure == 2) {
            radiationSensitivityOfTumorCellsAlpha = 0; //null
            radiationSensitivityOfTumorCellsBeta = 0;  //null
            radiationSensitivityOfLymphocytesAlpha = 0; //null
            radiationSensitivityOfLymphocytesBeta = 0; //null
            tumorGrowthRate = 0.217;
            tumorInfiltrationRate = 0.1;
            rateOfCellKilling = 0.05;
            decayConstantOfD = 0.039;
            decayConstantOfL = 0.335;
            recoveryConstantOfA = 0.039;
            radiationInducedInfiltration = 0; //null
            immuneSuppressionEffect = 0.012;
        } else if (figure == 3) {
            radiationSensitivityOfTumorCellsAlpha = 0.05;
            radiationSensitivityOfTumorCellsBeta = 0.0114;
            radiationSensitivityOfLymphocytesAlpha = 0.182;
            radiationSensitivityOfLymphocytesBeta = 0.143;
            tumorGrowthRate = 0.217;
            tumorInfiltrationRate = 0.05; //in original ODE model, is 0.5
            rateOfCellKilling = 0.135;
            decayConstantOfD = 0.045;
            decayConstantOfL = 0.045;
            recoveryConstantOfA = 0.045;
            radiationInducedInfiltration = 0; //null
            immuneSuppressionEffect = 0.51;
        } else if (figure == 4) {
            radiationSensitivityOfTumorCellsAlpha = 0.05;
            radiationSensitivityOfTumorCellsBeta = 0.0114;
            radiationSensitivityOfLymphocytesAlpha = 0.182;
            radiationSensitivityOfLymphocytesBeta = 0.143;
            tumorGrowthRate = 0.217;
            tumorInfiltrationRate = 0.5;
            rateOfCellKilling = 0.135;
            decayConstantOfD = 0.045;
            decayConstantOfL = 0.045;
            recoveryConstantOfA = 0.045;
            radiationInducedInfiltration = 300;
            immuneSuppressionEffect = 1.1;
        } else if (figure == 5) {
            radiationSensitivityOfTumorCellsAlpha = 0.05;
            radiationSensitivityOfTumorCellsBeta = 0.0114;
            radiationSensitivityOfLymphocytesAlpha = 0.182;
            radiationSensitivityOfLymphocytesBeta = 0.143;
            tumorGrowthRate = 0.217;
            tumorInfiltrationRate = 0.5;
            rateOfCellKilling = 0.135;
            decayConstantOfD = 0.045;
            decayConstantOfL = 0.045;
            recoveryConstantOfA = 0.045;
            radiationInducedInfiltration = 300;
            immuneSuppressionEffect = 1.1;
        } else if (figure == 6) {
            radiationSensitivityOfTumorCellsAlpha = 0.214;
            radiationSensitivityOfTumorCellsBeta = 0.0214;
            radiationSensitivityOfLymphocytesAlpha = 0.182;
            radiationSensitivityOfLymphocytesBeta = 0.143;
            tumorGrowthRate = 0.03;
            tumorInfiltrationRate = 0.1;
            rateOfCellKilling = 0.004;
            decayConstantOfD = 0.045;
            decayConstantOfL = 0.056;
            recoveryConstantOfA = 0.045;
            radiationInducedInfiltration = 4.6;
            immuneSuppressionEffect = 0.5;
        } else {
            System.err.println("Figure " + figure + " is not a valid figure number.");
            System.exit(0);
        }
    }
}
