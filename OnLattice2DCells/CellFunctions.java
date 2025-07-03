package OnLattice2DCells;

import HAL.GridsAndAgents.AgentSQ2Dunstackable;
import HAL.Gui.GridWindow;
import HAL.Util;

import java.util.*;

class CellFunctions extends AgentSQ2Dunstackable<OnLattice2DGrid> {
    Type type;
    int color;
    int radiationDose;
    boolean radiated;
    boolean deathFromRadiation;
    Double dieProb;
    Double dieProbRad;
    Double dieProbImm;
    Double divProb;
    Double activateProb;

    public enum Type {
        LYMPHOCYTE,
        TUMOR,
        DOOMED,
        TRIGGERING
    }

    public void Init(Type type, SimulationParameters params) {
        this.type = type;
        this.radiationDose = params.baseRadiationDose;
        if (params.baseRadiationDose == 0) {
            this.radiated = false;
        } else {
            this.radiated = true;
            if (type == Type.TUMOR) {
                TumorCells.countRad++;
            }
        }
        this.deathFromRadiation = false;

        if (type == Type.LYMPHOCYTE) {
            this.color = Util.CategorialColor(Lymphocytes.colorIndex);
            this.dieProb = Lymphocytes.dieProb;
            this.dieProbRad = null;
            this.dieProbImm = null;
            this.divProb = null;
            this.activateProb = null;
        } else if (type == Type.TUMOR) {
            this.color = Util.CategorialColor(TumorCells.colorIndex);
            this.dieProb = null;
            this.dieProbRad = TumorCells.dieProbRad;
            this.dieProbImm = TumorCells.dieProbImm;
            this.divProb = TumorCells.divProb;
            this.activateProb = null;

        } else if (type == Type.TRIGGERING) {
            this.color = Util.CategorialColor(TriggeringCells.colorIndex);
            this.dieProb = TriggeringCells.dieProb;
            this.dieProbRad = null;
            this.dieProbImm = null;
            this.divProb = null;
            this.activateProb = TriggeringCells.activateProb;
        }
    }

    public void InitDoomed(boolean radiation) {
        this.type = Type.DOOMED;
        this.color = Util.CategorialColor(DoomedCells.colorIndex);
        this.dieProb = DoomedCells.dieProb;
        this.dieProbRad = null;
        this.dieProbImm = null;
        this.divProb = null;
        this.activateProb = null;
        if (radiation) {
            this.deathFromRadiation = true;
        }
    }

    public void StepCell(SimulationParameters params) {
        if (this.type == Type.LYMPHOCYTE) {
            if (G.rng.Double() < this.dieProb) {
                Lymphocytes.count--;
                Dispose();
                int[] space = {this.Xsq(), this.Ysq()};
                reduceLymphocyteDensity(G, space);
            }
        } else if (this.type == Type.TUMOR) {
            // Update probabilities based on local oxygen
            assert G != null;
            double localOxygen = G.getOxygenField().getOxygenLevel(this.Xsq(), this.Ysq());

            double[] probs = CellFunctions.getTumorCellsProb(this.radiationDose, localOxygen);
            this.dieProbRad = probs[0];
            this.dieProbImm = probs[1];
            this.divProb = probs[2];

            if (G.rng.Double() < this.dieProbRad) {
                if (this.radiated) {
                    TumorCells.countRad--;
                }
                this.InitDoomed(true);
                TumorCells.count--;
                DoomedCells.count++;
                DoomedCells.countRad++;
            } else if (G.rng.Double() < (this.dieProbRad + this.dieProbImm)) {
                if (this.radiated) {
                    TumorCells.countRad--;
                }
                this.InitDoomed(false);
                TumorCells.count--;
                DoomedCells.count++;
                DoomedCells.countImm++;
            } else if (G.rng.Double() < (this.dieProbRad + this.dieProbImm + this.divProb)) {
                mapEmptyHood(params);
            }
        } else if (this.type == Type.DOOMED) {
            if (G.rng.Double() < this.dieProb) {
                Dispose();
                DoomedCells.count--;
                if (this.deathFromRadiation) {
                    DoomedCells.countRad--;
                } else {
                    DoomedCells.countImm--;
                }
            }
        } else if (this.type == Type.TRIGGERING) {
            if (G.rng.Double() < this.dieProb) {
                Dispose();
                TriggeringCells.count--;
                OnLattice2DGrid.triggeringDied = true;
                ;
            } else if (G.rng.Double() < (this.dieProb + this.activateProb)) {
                Dispose();
                TriggeringCells.count--;
                OnLattice2DGrid.triggeringDied = true;
            }
        }
    }

    public void mapEmptyHood(SimulationParameters params) {
        int options = MapEmptyHood(G.divHood);
        if (options > 0) {
            G.NewAgentSQ(G.divHood[G.rng.Int(options)]).Init(Type.TUMOR, params);
            TumorCells.count++;
        }
    }

    public void disposeRandomTriggering(OnLattice2DGrid model) {
        Collections.shuffle(OnLattice2DGrid.triggeringSpaces);
        model.GetAgent(OnLattice2DGrid.triggeringSpaces.get(0)[0], OnLattice2DGrid.triggeringSpaces.get(0)[1]).Dispose();
        TriggeringCells.count--;
    }

    private static final int[][] DIRECTIONS = {
            {0, 1}, // N
            {1, 1}, // NE
            {1, 0},  // E
            {1, -1},  // SE
            {0, -1},  // S
            {-1, -1}, // SW
            {-1, 0}, // W
            {-1, 1} // NW
    };

    public boolean checkLymphocyteDensity(OnLattice2DGrid model, int[] space) {
        int maxNeighbors = 4;
        if (OnLattice2DGrid.lymphocyteNeighbors[space[0]][space[1]] > maxNeighbors) {
            return false;
        }
        for (int[] dir : DIRECTIONS) {
            int xNeighbor = space[0] + dir[0];
            int yNeighbor = space[1] + dir[1];
            if (xNeighbor >= 0 && xNeighbor < model.xDim && yNeighbor >= 0 && yNeighbor < model.yDim &&
                    model.GetAgent(xNeighbor, yNeighbor) != null && model.GetAgent(xNeighbor, yNeighbor).type == Type.LYMPHOCYTE &&
                    OnLattice2DGrid.lymphocyteNeighbors[xNeighbor][yNeighbor] == maxNeighbors) {
                return false;
            }
        }

        for (int[] dir : DIRECTIONS) {
            int xNeighbor = space[0] + dir[0];
            int yNeighbor = space[1] + dir[1];
            if (xNeighbor >= 0 && xNeighbor < model.xDim && yNeighbor >= 0 && yNeighbor < model.yDim) {
                OnLattice2DGrid.lymphocyteNeighbors[xNeighbor][yNeighbor]++;
            }
        }
        // Note: Each lymphocyte must be added immediately after running this method, or will cause a bug.
        return true;
    }

    public void reduceLymphocyteDensity(OnLattice2DGrid model, int[] space) {
        for (int[] dir : DIRECTIONS) {
            int xNeighbor = space[0] + dir[0];
            int yNeighbor = space[1] + dir[1];
            if (xNeighbor >= 0 && xNeighbor < model.xDim && yNeighbor >= 0 && yNeighbor < model.yDim) {
                OnLattice2DGrid.lymphocyteNeighbors[xNeighbor][yNeighbor]--;
            }
        }
    }

    public void lymphocyteMigration(OnLattice2DGrid G, GridWindow win, SimulationParameters params) {
        double volumeDamagedTumorCells = (double) DoomedCells.countRad / (DoomedCells.count + TumorCells.count);
        double survivingFractionT;
        if (params.currentRadiationDose == params.baseRadiationDose) {
            survivingFractionT = getSurvivingFraction(params.baseRadiationDose, FigParameters.radiationSensitivityOfTumorCellsAlpha, FigParameters.radiationSensitivityOfTumorCellsBeta);
        } else {
            double survivingFractionTUnradiated = getSurvivingFraction(params.baseRadiationDose, FigParameters.radiationSensitivityOfTumorCellsAlpha, FigParameters.radiationSensitivityOfTumorCellsBeta);
            double survivingFractionTRadiated = getSurvivingFraction(params.currentRadiationDose, FigParameters.radiationSensitivityOfTumorCellsAlpha, FigParameters.radiationSensitivityOfTumorCellsBeta);
            survivingFractionT = (TumorCells.countRad * survivingFractionTRadiated + (TumorCells.count - TumorCells.countRad) * survivingFractionTUnradiated) / TumorCells.count;
        }

        double activation = Math.tanh((1 - survivingFractionT) * volumeDamagedTumorCells);
        OnLattice2DGrid.newLymphocytesAttempted = (int) (FigParameters.tumorInfiltrationRate * TumorCells.count + FigParameters.radiationInducedInfiltration * activation * TriggeringCells.count * TumorCells.count);

        int minDim = Math.min(win.xDim, win.yDim);
        double radiusFraction = 0.75; //Maximum value is 1
        int neighborhoodRadius = (int) Math.max(1, (double) minDim / 2 * radiusFraction); // Ensure radius is at least 1

        //Calculate weights and probabilities for each pixel
        double[][] probabilities = new double[win.xDim][win.yDim]; //default value of all entries is initially zero
        double totalProbability = 0;
        List<int[]> availableSpacesInRadius = new ArrayList<>();

        for (int[] availableSpace : params.availableSpaces) {
            double weightSum = 0;
            boolean possible = false;
            for (int[] tumorCell : OnLattice2DGrid.tumorSpaces) {
                double distance = Math.sqrt(Math.pow(availableSpace[0] - tumorCell[0], 2) + Math.pow(availableSpace[1] - tumorCell[1], 2));
                if (distance <= neighborhoodRadius) {
                    double weight = 1.0 / (distance + 1); // Higher weight for closer pixels
                    weightSum += weight;
                    possible = true;
                }
            }
            if (possible) {
                availableSpacesInRadius.add(availableSpace);
            }
            probabilities[availableSpace[0]][availableSpace[1]] = weightSum;
            totalProbability += weightSum;
        }

        //Select `spacesToPick` pixels based on the weighted probability distribution
        int spacesToPick = Math.min(OnLattice2DGrid.newLymphocytesAttempted, availableSpacesInRadius.size());
        Random random = new Random();
        List<int[]> selectedPixels = new ArrayList<>();

        int count = 0;
        WhileLoop:
        while (!availableSpacesInRadius.isEmpty() && spacesToPick > 0) {
            double rand = totalProbability * random.nextDouble(); //This normalizes the probabilities more efficiently! :)
            double cumulativeProbability = 0.0;
            Iterator<int[]> iterator = availableSpacesInRadius.iterator();
            while (iterator.hasNext()) {
                int[] space = iterator.next();
                cumulativeProbability += probabilities[space[0]][space[1]];
                if (rand < cumulativeProbability && checkLymphocyteDensity(G, space)) {
                    selectedPixels.add(space);
                    G.NewAgentSQ(space[0], space[1]).Init(Type.LYMPHOCYTE, params);
                    Lymphocytes.count++;
                    iterator.remove();
                    //OnLattice2DGrid.availableSpaces.remove(space);
                    totalProbability -= probabilities[space[0]][space[1]];
                    count++;
                    if (count == spacesToPick) {
                        break WhileLoop;
                    }
                    break;
                } else if (rand < cumulativeProbability && !checkLymphocyteDensity(G, space)) {
                    iterator.remove();
                    //OnLattice2DGrid.availableSpaces.remove(space);
                    totalProbability -= probabilities[space[0]][space[1]];
                }
            }
        }
        /* If less lymphocytes are added than what's in spacesToPick, it means that there weren't enough available
        spaces in the radius with max # of lymphocyte neighbors permitted */
    }

    public void randomInitialization(OnLattice2DGrid G, int cellPopulation, Type type, SimulationParameters params) {
        int spacesToPick = Math.min(cellPopulation, params.availableSpaces.size());
        Collections.shuffle(params.availableSpaces);

        if (type == Type.LYMPHOCYTE) {
            int count = 0;
            for (int i = 0; i < params.availableSpaces.size(); i++) {
                int x = params.availableSpaces.get(i)[0];
                int y = params.availableSpaces.get(i)[1];
                int[] space = {x, y};
                if (checkLymphocyteDensity(G, space)) {
                    G.NewAgentSQ(x, y).Init(type, params);
                    Lymphocytes.count++;
                    count++;
                }
                if (count == spacesToPick) {
                    break;
                }
            }
        } else if (type == Type.TRIGGERING) {
            for (int i = 0; i < spacesToPick; i++) {
                int x = params.availableSpaces.get(i)[0];
                int y = params.availableSpaces.get(i)[1];
                G.NewAgentSQ(x, y).Init(type, params);
                OnLattice2DGrid.triggeringSpaces.add(new int[]{x, y});
                TriggeringCells.count++;
            }
        }

    }

    public static void getImmuneSuppressionEffectThreshold(boolean init) {
        if (init) {
            FigParameters.immuneSuppressionEffect =
                    (FigParameters.rateOfCellKilling / (FigParameters.tumorGrowthRate * Math.pow(TumorCells.count, ((double) 2 / 3))));
        } else {
            FigParameters.immuneSuppressionEffect =
                    (FigParameters.rateOfCellKilling / (FigParameters.tumorGrowthRate * Math.pow(TumorCells.count, ((double) 2 / 3)))
                            - 1 / (Lymphocytes.count * Math.pow(TumorCells.count, ((double) 2 / 3))));
        }
    }

    public static void getImmuneResponse() {
        double concentrationAntiPD1_PDL1 = 0;
        OnLattice2DGrid.primaryImmuneResponse = ((Double) FigParameters.rateOfCellKilling * Lymphocytes.count) /
                (1 + ((FigParameters.immuneSuppressionEffect * Math.pow(TumorCells.count, ((double) 2 / 3)) * Lymphocytes.count) / (1 + concentrationAntiPD1_PDL1)));

        double concentrationAntiCTLA4 = 0;
        double sensitivityFactorZs = 0.0314;
        int NormalizationFactor = 5;
        OnLattice2DGrid.secondaryImmuneResponse += sensitivityFactorZs * ((1 + concentrationAntiCTLA4) /
                (NormalizationFactor + concentrationAntiCTLA4)) * OnLattice2DGrid.primaryImmuneResponse;

        OnLattice2DGrid.immuneResponse = OnLattice2DGrid.primaryImmuneResponse + OnLattice2DGrid.secondaryImmuneResponse;
    }

    public static double getSurvivingFraction(double radiationDose, double alpha, double beta) {
        return Math.exp(alpha * -radiationDose - beta * Math.pow(radiationDose, 2));
    }

    public static double getLymphocytesProb(int radiationDose) {
        double survivingFractionL = getSurvivingFraction(radiationDose, FigParameters.radiationSensitivityOfLymphocytesAlpha, FigParameters.radiationSensitivityOfLymphocytesBeta);
        return 1 - survivingFractionL + (survivingFractionL * FigParameters.decayConstantOfL);
    }


    public static double[] getTumorCellsProb(int radiationDose, double localOxygen) {
        double survivingFractionT = getSurvivingFraction(radiationDose, FigParameters.radiationSensitivityOfTumorCellsAlpha, FigParameters.radiationSensitivityOfTumorCellsBeta);
        double dieProbRad = 1 - survivingFractionT;

        // Hypoxia reduces radiation-induced death
        double hypoxiaResistanceFactor = 1.0 - 0.5 * (1.0 - localOxygen);
        // At 0% oxygen → 50% reduction in radiation death
        dieProbRad *= hypoxiaResistanceFactor;

        // GBM-specific immune suppression factor
        double gbmImmuneSuppressionFactor = 0.3; // Tune between 0.1 to 0.5 for strong suppression
        double dieProbImm = survivingFractionT * OnLattice2DGrid.immuneResponse * gbmImmuneSuppressionFactor;

        // GBM-specific invasion boost factor ( > 1 increases invasion likelihood)
        double gbmInvasionBoost = 1.5;  // Tune between 1.2 to 2.0 for more aggressive invasion

        double divProb = survivingFractionT * (1 - OnLattice2DGrid.immuneResponse) * FigParameters.tumorGrowthRate * gbmInvasionBoost;
        return new double[]{dieProbRad, dieProbImm, divProb};
    }

    public static double[] getTriggeringCellsProb(int radiationDose) {
        double volumeDamagedTumorCells = (double) DoomedCells.countRad / (DoomedCells.count + TumorCells.count);
        double survivingFractionTUnradiated = getSurvivingFraction(SimulationParameters.baseRadiationDose, FigParameters.radiationSensitivityOfTumorCellsAlpha, FigParameters.radiationSensitivityOfTumorCellsBeta);
        double survivingFractionTRadiated = getSurvivingFraction(SimulationParameters.appliedRadiationDose, FigParameters.radiationSensitivityOfTumorCellsAlpha, FigParameters.radiationSensitivityOfTumorCellsBeta);
        TriggeringCells.SurvivingFractionTLast = (TumorCells.countRad * survivingFractionTRadiated + (TumorCells.count - TumorCells.countRad) * survivingFractionTUnradiated) / TumorCells.count;

        double activation = Math.tanh((1 - TriggeringCells.SurvivingFractionTLast) * volumeDamagedTumorCells);
        double survivingFractionL = getSurvivingFraction(radiationDose, FigParameters.radiationSensitivityOfLymphocytesAlpha, FigParameters.radiationSensitivityOfLymphocytesBeta);
        double survivingFractionI = survivingFractionL;
        double dieProb = (1 - survivingFractionI) * (1 - FigParameters.recoveryConstantOfA);
        double activateProb = (1 - survivingFractionI) * FigParameters.recoveryConstantOfA * activation + survivingFractionI * activation;
        return new double[]{dieProb, activateProb};
    }
}
