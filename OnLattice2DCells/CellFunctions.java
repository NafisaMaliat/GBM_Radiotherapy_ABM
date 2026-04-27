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

    // Clone identity for tumour heterogeneity
    // 0 = Baseline, 1 = Proliferative, 2 = Invasive/Resistant
    int cloneId = 0;

    // Persistent sublethal radiation damage (0 = undamaged, up to ~0.9 = heavily damaged)
    // Reduces division probability while repairing; decays each timestep.
    // Models G2/M checkpoint arrest and DNA double-strand break repair delay.
    double radiationDamage = 0.0;
    static final double DAMAGE_REPAIR_RATE = 0.03; // decay per timestep; half-life ~23 days



    public enum Type {
        LYMPHOCYTE,
        TUMOR,
        DOOMED,
        TRIGGERING
    }



    public void Init(Type type, SimulationParameters params) {
        this.type = type;
        this.radiationDose = SimulationParameters.baseRadiationDose;
        if (SimulationParameters.baseRadiationDose == 0) {
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
            // If cloneId wasn't set yet, default to baseline (0),
            // otherwise keep whatever was already assigned.
            if (this.cloneId < 0 || this.cloneId >= TumorCells.NUM_CLONES) {
                this.cloneId = 0;  // default baseline only if invalid
            }

            // Colour based on clone id (so that heterogeneity can be seen visually)
            this.color = Util.CategorialColor(TumorCells.cloneColorIndex[this.cloneId]);

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
                int[] space = {this.Xsq(), this.Ysq()};
                Dispose();
                reduceLymphocyteDensity(G, space);
            }

        } else if (this.type == Type.TUMOR) {

            // Clone-specific behaviour: each clone has its own growth/RT/immune response
            assert G != null;

            int c = this.cloneId;
            double[] probs = CellFunctions.getTumorCellsProb(this.radiationDose, c);

            this.dieProbRad = probs[0];
            this.dieProbImm = probs[1];
            this.divProb    = probs[2];

            // Persistent sublethal damage slows division (G2/M arrest for DNA repair)
            if (this.radiationDamage > 0) {
                this.divProb *= (1.0 - this.radiationDamage);
                this.radiationDamage *= (1.0 - DAMAGE_REPAIR_RATE);
                if (this.radiationDamage < 0.001) this.radiationDamage = 0;
            }

            // one random draw to decide outcome
            double r = G.rng.Double();

            if (r < this.dieProbRad) {
                // death from radiation
                if (this.radiated) {
                    TumorCells.countRad--;
                    TumorCells.cloneCountRad[c]--;
                }
                TumorCells.cloneCount[c]--;
                this.InitDoomed(true);
                TumorCells.count--;
                DoomedCells.count++;
                DoomedCells.countRad++;

            } else if (r < this.dieProbRad + this.dieProbImm) {
                // death from immune
                if (this.radiated) {
                    TumorCells.countRad--;
                    TumorCells.cloneCountRad[c]--;
                }
                TumorCells.cloneCount[c]--;
                this.InitDoomed(false);
                TumorCells.count--;
                DoomedCells.count++;
                DoomedCells.countImm++;

            } else if (r < this.dieProbRad + this.dieProbImm + this.divProb) {
                // division
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

            double r = G.rng.Double();
            if (r < this.dieProb) {
                Dispose();
                TriggeringCells.count--;
                OnLattice2DGrid.triggeringDied = true;
            } else if (r < this.dieProb + this.activateProb) {
                Dispose();
                TriggeringCells.count--;
                OnLattice2DGrid.triggeringDied = true;
            }
        }
    }



    public void mapEmptyHood(SimulationParameters params) {
        int options = MapEmptyHood(G.divHood);
        if (options > 0) {
            int iNew = G.divHood[G.rng.Int(options)];

            // Create new tumour cell
            CellFunctions daughter = G.NewAgentSQ(iNew);

            // Inherit clone from parent BEFORE Init
            daughter.cloneId = this.cloneId;

            // Daughter inherits diluted radiation damage (damage shared across division)
            daughter.radiationDamage = this.radiationDamage * 0.5;

            // Initialise as tumour – Init will set colour etc. using cloneId
            daughter.Init(Type.TUMOR, params);

            TumorCells.count++;
            TumorCells.cloneCount[daughter.cloneId]++;
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
        // Fraction of tumour microenvironment showing radiation damage.
        // Use actual doomed-cell fraction when available; supplement with
        // postRadiationSignal to model persistent immunogenic debris (DAMPs,
        // released antigens) after doomed cells have cleared.
        double volumeDamagedTumorCells;
        int totalBurden = DoomedCells.count + TumorCells.count;
        if (totalBurden > 0) {
            double actualFraction = (double) DoomedCells.countRad / totalBurden;
            double signalFraction = OnLattice2DGrid.postRadiationSignal * 0.3;
            volumeDamagedTumorCells = Math.max(actualFraction, signalFraction);
        } else {
            volumeDamagedTumorCells = 0;
        }
        // Clone-weighted surviving fraction: irradiated cells use currentDose, others use baseDose (SF=1.0)
        // Use active dose when radiation is on; otherwise use the decaying post-radiation signal.
        // This sustains immune activation for ~50-100 timesteps after radiation, reflecting
        // the persistence of danger signals (DAMPs, cytokines) in vivo.
        double doseForActivation = (SimulationParameters.currentRadiationDose > 0)
                ? SimulationParameters.currentRadiationDose
                : SimulationParameters.appliedRadiationDose * OnLattice2DGrid.postRadiationSignal;

        double survivingFractionT;
        if (TumorCells.count > 0) {
            double sfWeightedSum = 0;
            for (int c = 0; c < TumorCells.NUM_CLONES; c++) {
                double alpha = FigParameters.radiationSensitivityOfTumorCellsAlpha * FigParameters.cloneAlphaMultiplier[c];
                double beta  = FigParameters.radiationSensitivityOfTumorCellsBeta  * FigParameters.cloneBetaMultiplier[c];
                double sfRad = getSurvivingFraction(doseForActivation, alpha, beta);
                // Use actual radiated count when radiation is on; otherwise, use
                // postRadiationSignal to estimate the "immunogenically affected"
                // fraction.  High-dose cells die immediately so cloneCountRad drops
                // to 0, but danger signals (DAMPs, cytokines, released antigens)
                // persist — postRadiationSignal decays slowly to model this.
                double affectedCount;
                if (TumorCells.cloneCountRad[c] > 0) {
                    affectedCount = TumorCells.cloneCountRad[c];
                } else if (OnLattice2DGrid.postRadiationSignal > 0.01) {
                    affectedCount = TumorCells.cloneCount[c] * OnLattice2DGrid.postRadiationSignal;
                } else {
                    affectedCount = 0;
                }
                sfWeightedSum += affectedCount * sfRad
                               + (TumorCells.cloneCount[c] - affectedCount) * 1.0;
            }
            survivingFractionT = sfWeightedSum / TumorCells.count;
        } else {
            survivingFractionT = 1.0;
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
        if (TumorCells.count <= 0 || Lymphocytes.count <= 0) {
            OnLattice2DGrid.primaryImmuneResponse = 0;
            OnLattice2DGrid.secondaryImmuneResponse = 0;
            OnLattice2DGrid.immuneResponse = 0;
            return;
        }

        double concentrationAntiPD1_PDL1 = 0;
        OnLattice2DGrid.primaryImmuneResponse = ((Double) FigParameters.rateOfCellKilling * Lymphocytes.count) /
                (1 + ((FigParameters.immuneSuppressionEffect * Math.pow(TumorCells.count, ((double) 2 / 3)) * Lymphocytes.count) / (1 + concentrationAntiPD1_PDL1)));

        double concentrationAntiCTLA4 = 0;
        double sensitivityFactorZs = 0.0314;
        int NormalizationFactor = 5;
        OnLattice2DGrid.secondaryImmuneResponse = sensitivityFactorZs * ((1 + concentrationAntiCTLA4) /
                (NormalizationFactor + concentrationAntiCTLA4)) * OnLattice2DGrid.primaryImmuneResponse;

        OnLattice2DGrid.immuneResponse = Math.min(1.0,
                OnLattice2DGrid.primaryImmuneResponse + OnLattice2DGrid.secondaryImmuneResponse);
    }

    public static double getSurvivingFraction(double radiationDose, double alpha, double beta) {
        return Math.exp(alpha * -radiationDose - beta * Math.pow(radiationDose, 2));
    }

    public static double getLymphocytesProb(int radiationDose) {
        double survivingFractionL = getSurvivingFraction(radiationDose, FigParameters.radiationSensitivityOfLymphocytesAlpha, FigParameters.radiationSensitivityOfLymphocytesBeta);
        return 1 - survivingFractionL + (survivingFractionL * FigParameters.decayConstantOfL);
    }

    private static double clampProbability(double value) {
        return Math.max(0, Math.min(1, value));
    }

    // clone-specific tumour probabilities (heterogeneous behaviour)
    public static double[] getTumorCellsProb(int radiationDose, int cloneId) {

        // safety: clamp cloneId to valid range
        if (cloneId < 0 || cloneId >= FigParameters.cloneAlphaMultiplier.length) {
            cloneId = 0;
        }

        // 1) clone-specific alpha/beta
        double alphaBase = FigParameters.radiationSensitivityOfTumorCellsAlpha;
        double betaBase  = FigParameters.radiationSensitivityOfTumorCellsBeta;

        double alpha = alphaBase * FigParameters.cloneAlphaMultiplier[cloneId];
        double beta  = betaBase  * FigParameters.cloneBetaMultiplier[cloneId];

        // 2) surviving fraction with clone-specific radiosensitivity
        double survivingFractionT = getSurvivingFraction(radiationDose, alpha, beta);

        // 3) baseline probs
        double dieProbRad = clampProbability(1 - survivingFractionT);
        double effectiveImmuneResponse = clampProbability(OnLattice2DGrid.immuneResponse);

        double dieProbImm = survivingFractionT * effectiveImmuneResponse;

        double divProb = survivingFractionT
                * (1 - effectiveImmuneResponse)
                * FigParameters.tumorGrowthRate;

        // 4) apply clone-specific growth + immune modifiers
        double growthMult = FigParameters.cloneGrowthMultiplier[cloneId];
        double immuneMult = FigParameters.cloneImmuneKillMultiplier[cloneId];

        divProb    *= growthMult;
        dieProbImm *= immuneMult;

        double remaining = 1 - dieProbRad;
        dieProbImm = Math.min(Math.max(0, dieProbImm), remaining);
        remaining -= dieProbImm;
        divProb = Math.min(Math.max(0, divProb), remaining);

        return new double[]{dieProbRad, dieProbImm, divProb};
    }



    public static double[] getTriggeringCellsProb(int radiationDose) {
        int totalTumorBurden = DoomedCells.count + TumorCells.count;
        if (totalTumorBurden <= 0 || TumorCells.count <= 0) {
            TriggeringCells.SurvivingFractionTLast = 1.0;
            double survivingFractionL = getSurvivingFraction(radiationDose, FigParameters.radiationSensitivityOfLymphocytesAlpha, FigParameters.radiationSensitivityOfLymphocytesBeta);
            double survivingFractionI = survivingFractionL;
            double dieProb = (1 - survivingFractionI) * (1 - FigParameters.recoveryConstantOfA);
            return new double[]{dieProb, 0};
        }

        double volumeDamagedTumorCells = (double) DoomedCells.countRad / totalTumorBurden;
        // Clone-weighted surviving fraction: irradiated cells use appliedDose, others use baseDose (SF=1.0)
        // NOTE: this method intentionally does NOT use postRadiationSignal.
        // Triggering cells (APCs) should not activate faster post-radiation — that
        // would deplete them and collapse lymphocyte recruitment.  The immune boost
        // from radiation is handled in lymphocyteMigration's recruitment formula.
        double sfWeightedSum = 0;
        for (int c = 0; c < TumorCells.NUM_CLONES; c++) {
            double alpha = FigParameters.radiationSensitivityOfTumorCellsAlpha * FigParameters.cloneAlphaMultiplier[c];
            double beta  = FigParameters.radiationSensitivityOfTumorCellsBeta  * FigParameters.cloneBetaMultiplier[c];
            double sfRad = getSurvivingFraction(SimulationParameters.appliedRadiationDose, alpha, beta);
            sfWeightedSum += TumorCells.cloneCountRad[c] * sfRad
                           + (TumorCells.cloneCount[c] - TumorCells.cloneCountRad[c]) * 1.0;
        }
        TriggeringCells.SurvivingFractionTLast = sfWeightedSum / TumorCells.count;

        double activation = Math.tanh((1 - TriggeringCells.SurvivingFractionTLast) * volumeDamagedTumorCells);
        double survivingFractionL = getSurvivingFraction(radiationDose, FigParameters.radiationSensitivityOfLymphocytesAlpha, FigParameters.radiationSensitivityOfLymphocytesBeta);
        double survivingFractionI = survivingFractionL;
        double dieProb = (1 - survivingFractionI) * (1 - FigParameters.recoveryConstantOfA);
        double activateProb = (1 - survivingFractionI) * FigParameters.recoveryConstantOfA * activation + survivingFractionI * activation;
        return new double[]{dieProb, activateProb};
    }
}
