package OnLattice2DCells;

class DoomedCells {
    public static String name = "Doomed Cells";
    public static String nameRad = "Doomed Cells Radiation";
    public static String nameImm = "Doomed Cells Immune";
    public static double dieProb;
    public static int colorIndex = 3;
    public static int count, countRad, countImm;

    public void DoomedCells() {
        count = 0;
        countRad = 0;
        countImm = 0;
        dieProb = FigParameters.decayConstantOfD;
    }
}
