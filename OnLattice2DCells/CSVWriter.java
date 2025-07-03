package OnLattice2DCells;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CSVWriter {

    private final OnLattice2DGrid grid;

    public CSVWriter(OnLattice2DGrid grid) {
        this.grid = grid;
    }

    public void saveCountsToCSV(String fullPath1, boolean append, int timestep) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullPath1, append))) {
            if (timestep == 0) {
                writer.write("Timestep,Lymphocytes,TriggeringCells,TumorCells,TumorCellsRad,DoomedCells," +
                        "DoomedCellsRad,Lymphocytes DieProb,Tumor DieProbRad,Tumor DieProbImm,Tumor DivProb," +
                        "SurvivingFractionTLast,PrimaryImmuneResponse,SecondaryImmuneResponse,ImmuneResponse," +
                        "LymphocyteMigrationAttempted,ImmuneSuppression");
                writer.newLine();
            }
            writer.write(timestep + "," + Lymphocytes.count + "," + TriggeringCells.count + "," + TumorCells.count + "," + TumorCells.countRad + "," +
                    DoomedCells.count + "," + DoomedCells.countRad + "," + Lymphocytes.dieProb + "," + TumorCells.dieProbRad + "," +
                    TumorCells.dieProbImm + "," + TumorCells.divProb + "," + TriggeringCells.SurvivingFractionTLast + "," +
                    OnLattice2DGrid.primaryImmuneResponse + "," + OnLattice2DGrid.secondaryImmuneResponse + "," +
                    OnLattice2DGrid.immuneResponse + "," + OnLattice2DGrid.newLymphocytesAttempted + "," + FigParameters.immuneSuppressionEffect);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write CSV file: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void saveProbabilitiesToCSV(String fullPath2, boolean append, int timestep, boolean duringRadiation) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullPath2, append))) {
            if (timestep == 0) {
                writer.write("Timestep,Cell,Type,Color,Radiated,RadiationDose,DeathFromRadiation," +
                        "DieProb,ActivateProb,DieProbRad,DieProbImm,DivProb,LymphocyteNeighbors");
                writer.newLine();
            }

            if (duringRadiation) {
                writer.write("Before Radiation Effects\n");
            }

            if (timestep >= 0) {
                for (int i = 0; i < grid.length; i++) {
                    OnLattice2DCells.CellFunctions cell = grid.GetAgent(i);
                    if (cell != null) {
                        writer.write(timestep + "," + cell + "," + cell.type + "," + cell.color + "," + cell.radiated + "," +
                                cell.radiationDose + "," + cell.deathFromRadiation + "," + cell.dieProb + "," + cell.activateProb + "," +
                                cell.dieProbRad + "," + cell.dieProbImm + "," + cell.divProb + "," + grid.lymphocyteNeighbors[cell.Xsq()][cell.Ysq()]);
                        writer.newLine();
                    }
                }
            }

            if (duringRadiation) {
                writer.write("\nAfter Radiation Effects");
            }

            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write CSV file: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void saveLymphocyteNeighborstoCSV(String fullPath3, boolean append, int timestep) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullPath3, append))) {
            if (timestep == 0) {
                writer.write("Timestep,Type,Lymphocyte Neighbors");
                writer.newLine();
            }
            if (timestep >= 0) {
                for (int i = 0; i < grid.length; i++) {
                    OnLattice2DCells.CellFunctions cell = grid.GetAgent(i);
                    if (cell != null) {
                        writer.write(timestep + "," + cell.type + "," + grid.lymphocyteNeighbors[cell.Xsq()][cell.Ysq()]);
                        writer.newLine();
                    } else {
                        cell = grid.NewAgentSQ(i);
                        writer.write(timestep + ",empty," + grid.lymphocyteNeighbors[cell.Xsq()][cell.Ysq()]);
                        writer.newLine();
                        cell.Dispose();
                    }
                }
                /* Alternate Visual Format:
                writer.write(timestep  + ",");
                for (int i = 0; i < xDim; i++)
                {
                    writer.write(i + (i < xDim - 1 ? "," : ""));
                }
                writer.newLine();
                for (int j = 0; j < yDim; j++)
                {
                    writer.write(j + "");
                    for (int i = 0; i < xDim; i++)
                    {
                        String cellContent;
                        OnLattice2DCells.CellFunctions cell = GetAgent(i, j);
                        if (cell != null)
                        {
                            cellContent = cell.type + " " + lymphocyteNeighbors[i][j];
                        }
                        else
                        {
                            cellContent = String.valueOf(lymphocyteNeighbors[i][j]);
                        }
                        writer.write("," + cellContent);
                    }
                    writer.newLine();
                }*/
            }

            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write CSV file: " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void printPopulation(String name, int colorIndex, int count) {
        System.out.println("Population of " + name + " (" + findColor(colorIndex) + "): " + count);
    }

    public String findColor(int colorIndex) {
        if (colorIndex == 0) {
            return "blue";
        } else if (colorIndex == 1) {
            return "red";
        } else if (colorIndex == 2) {
            return "green";
        } else if (colorIndex == 3) {
            return "yellow";
        } else if (colorIndex == 4) {
            return "orange";
        } else if (colorIndex == 5) {
            return "cyan";
        } else if (colorIndex == 6) {
            return "pink";
        } else if (colorIndex == 7) {
            return "blue";
        } else if (colorIndex == 8) {
            return "brown";
        } else if (colorIndex == 9) {
            return "light blue";
        } else if (colorIndex == 10) {
            return "light red";
        } else if (colorIndex == 11) {
            return "light green";
        } else if (colorIndex == 12) {
            return "light yellow";
        } else if (colorIndex == 13) {
            return "light purple";
        } else if (colorIndex == 14) {
            return "light orange";
        } else if (colorIndex == 15) {
            return "light cyan";
        } else if (colorIndex == 16) {
            return "light pink";
        } else if (colorIndex == 17) {
            return "light brown";
        } else if (colorIndex == 18) {
            return "light gray";
        } else if (colorIndex == 19) {
            return "dark gray";
        }
        return "unknown color";
    }
}
