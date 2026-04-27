package OnLattice2DCells;

import HAL.Gui.GifMaker;
import HAL.Gui.GridWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    // Default values, unless specified explicitly
    public static int figure = 2;
    public static int baseRadiationDose = 0, currentRadiationDose = baseRadiationDose, appliedRadiationDose = 10;
    public static List<Integer> radiationTimesteps = List.of(200);
    public static boolean totalRadiation = false, centerRadiation = false, spatialRadiation = false;
    public static double targetPercentage = 0.7;
    public static double thresholdPercentage = 0.01; // lowered from 0.8: real SFRT beams irradiate any tissue they overlap, not just dense tumour regions
    public static int radius = 10;
    public static boolean scenarioActive = true;
    public static boolean immuneSuppressionEffectThreshold = false;
    public static boolean batchMode = false; // set by BatchRunner to skip GUI
    public static boolean saveVideo = true; // save full simulation as MP4 video
    public static int videoFrameInterval = 5; // capture a frame every N timesteps
    public static int videoFPS = 20; // frames per second in the output MP4

    public static List<int[]> availableSpaces = new ArrayList<>();
    public static List<int[]> radiatedPixels = new ArrayList<>();
    public static List<int[]> allPixels = new ArrayList<>();

    // Resets every static field to its default value.
    // This is needed because BatchRunner calls Main.main() in a loop — without
    // resetting, values from a previous scenario/trial leak into the next one.
    // Currently all scenarios happen to overwrite what they need, so existing
    // results are unaffected, but any future scenario that skips a field would
    // silently inherit the previous trial's value.
    public static void resetStaticState() {
        figure = 2;
        baseRadiationDose = 0;
        currentRadiationDose = 0;
        appliedRadiationDose = 10;
        radiationTimesteps = List.of(200);
        totalRadiation = false;
        centerRadiation = false;
        spatialRadiation = false;
        targetPercentage = 0.7;
        thresholdPercentage = 0.01;
        radius = 10;
        scenarioActive = true;
        immuneSuppressionEffectThreshold = false;
        writeGIF = false;

        availableSpaces.clear();
        radiatedPixels.clear();
        allPixels.clear();

        // SimulationParameters static fields
        SimulationParameters.baseRadiationDose = 0;
        SimulationParameters.currentRadiationDose = 0;
        SimulationParameters.appliedRadiationDose = 10;
        SimulationParameters.valleyDoseRatio = 0.0;

        // Cell population counters
        Lymphocytes.count = 0;
        Lymphocytes.dieProb = 0;
        TumorCells.resetCounts();
        DoomedCells.count = 0;
        DoomedCells.countRad = 0;
        DoomedCells.countImm = 0;
        DoomedCells.dieProb = 0;
        TriggeringCells.count = 0;
        TriggeringCells.dieProb = 0;
        TriggeringCells.activateProb = 0;
        TriggeringCells.SurvivingFractionTLast = 0;

        // Grid-level static fields
        OnLattice2DGrid.immuneResponse = 0;
        OnLattice2DGrid.primaryImmuneResponse = 0;
        OnLattice2DGrid.secondaryImmuneResponse = 0;
        OnLattice2DGrid.newLymphocytesAttempted = 0;
        OnLattice2DGrid.triggeringDied = false;
        OnLattice2DGrid.postRadiationSignal = 0.0;
        OnLattice2DGrid.tumorSpaces.clear();
        OnLattice2DGrid.triggeringSpaces.clear();
        OnLattice2DGrid.lymphocyteSpaces.clear();
    }

    //Setting file/folder names
    public static final String directory = "HALModeling2024Outs";
    public static final String fileName1 = "TrialRunCounts.csv";
    public static String fullPath1 = directory + "/" + fileName1;
    public static final String fileName2 = "TrialRunProbabilities.csv";
    public static final String fullPath2 = directory +  "/" + fileName2;
    public static final String fileName3 = "LymphocyteNeighbors.csv";
    public static final String fullPath3 = directory +  "/" + fileName3;
    public static final boolean printCounts = true, printProbabilities = false, printNeighbors = false;
    public static boolean writeGIF = false;


    //Input scenario to run
    public static String scenario = "BB15";


    public static void main(String[] args) {

        // Reset all static state so that BatchRunner trials start clean.
        // Without this, values from a previous scenario could leak into the next.
        resetStaticState();

        // Allow scenario override via args
        if (args != null && args.length > 0) {
            scenario = args[0];
        }

        SimulationParameters params = new SimulationParameters(
                targetPercentage,
                thresholdPercentage,
                radius,radiatedPixels,
                baseRadiationDose,
                currentRadiationDose,
                appliedRadiationDose,
                totalRadiation,
                centerRadiation,
                spatialRadiation,
                immuneSuppressionEffectThreshold,
                availableSpaces );

        System.out.print("Scenario Active: " + scenarioActive);
        if (scenarioActive) {
            System.out.print("    Scenario: " + scenario);

            new ScenarioParameters(scenario, params, true);
        } else {
            new FigParameters(figure);
        }

        System.out.println("\nSave Counts to CSV: " + printCounts + "   Save Probabilities to CSV: " + printProbabilities +
                "   Save Video: " + saveVideo + " (every " + videoFrameInterval + " steps)\n");

        int x = 100;
        int y = 100;
        int timesteps = 540;// 540 days is the survival with treatment [https://www.thebraintumourcharity.org/brain-tumour-diagnosis-treatment/types-of-brain-tumour-adult/glioblastoma/glioblastoma-prognosis/]

        // In batch mode: smaller window and killOnClose=false so BatchRunner can continue
        GridWindow win = batchMode
                ? new GridWindow(x, y, 1, false, null)
                : new GridWindow(x, y, 5);
        OnLattice2DGrid model = new OnLattice2DGrid(x, y);

        // cache all pixel coordinates for total-radiation mode
        allPixels.clear(); // must clear before adding — static list persists across BatchRunner trials
        for (int i = 0; i < model.xDim; i++) {
            for (int j = 0; j < model.yDim; j++) {
                allPixels.add(new int[]{i, j});
            }
        }

        radiatedPixels.clear(); // must clear — if previous trial broke mid-radiation, list carries stale data
        RadiationManager radiationManager = new RadiationManager(model, params);

        new Lymphocytes().Lymphocytes();
        TumorCells.resetCounts();
        new DoomedCells().DoomedCells();
        new TriggeringCells().TriggeringCells();

        model.Init(win, model,params);

        CSVWriter writer = new CSVWriter(model);

        if (printCounts) writer.saveCountsToCSV(fullPath1, false, 0);
        if (printProbabilities) writer.saveProbabilitiesToCSV(fullPath2, false, 0);
        if (printNeighbors) writer.saveLymphocyteNeighborstoCSV(fullPath3, false, 0);

        // Set up video frame directory if saving video
        String framesDir = null;
        String videoPath = null;
        int frameCount = 0;
        if (saveVideo) {
            String scenarioDir = scenarioActive
                    ? new File(directory, "Scenario" + scenario).getPath()
                    : directory;
            framesDir = new File(scenarioDir, "frames").getPath();
            videoPath = new File(scenarioDir, (scenarioActive ? scenario : "TrialRun") + "_simulation.mp4").getPath();
            // Clean up any leftover frames from a previous run
            File framesDirFile = new File(framesDir);
            if (framesDirFile.exists()) {
                File[] oldFrames = framesDirFile.listFiles();
                if (oldFrames != null) {
                    for (File f : oldFrames) f.delete();
                }
            } else {
                framesDirFile.mkdirs();
            }
        }

        // GifMaker still required by DrawModelandUpdateProb — create a dummy that writes no frames
        GifMaker gif = new GifMaker(directory + "/tmp.gif", 1, false);

        for (int i = 1; i <= timesteps; i++) {
            if (!batchMode) win.TickPause(1);

            // Radiation application
            if (radiationTimesteps.contains(i) && TumorCells.count > 20) {
                if (params.totalRadiation) {
                    radiatedPixels.addAll(allPixels);
                    radiationManager.radiationApplied();
                } else if (params.centerRadiation) {
                    int[] tumorCoord = model.getTumorCoord();
                    if (tumorCoord != null) {
                        radiationManager.centerRadiationArea(win, tumorCoord);
                        radiationManager.radiationApplied();
                    }
                } else if (params.spatialRadiation) {
                    int[] tumorCoord = model.getTumorCoord();
                    if (tumorCoord != null) {
                        radiationManager.spatialRadiationArea(win, tumorCoord);
                        radiationManager.radiationApplied();
                    }
                }
                if (printProbabilities) writer.saveProbabilitiesToCSV(fullPath2, true, i);

            } else if (radiationTimesteps.contains(i - 1)) {
                radiationManager.radiationUnapplied();
                radiatedPixels.clear();
            }

            // Core dynamics
            model.StepCells(model, params);
            model.updateSpaces(win,params);

            // Lymphocyte Migration
            // Always run: baseline infiltration (infiltrationRate × TumorCells) provides
            // continuous immune surveillance independent of APCs or radiation signals.
            // The radiation-amplified term naturally drops to 0 when APCs are gone and
            // postRadiationSignal has decayed.
            new CellFunctions().lymphocyteMigration(model, win, params);


            if (printCounts) writer.saveCountsToCSV(fullPath1, true, i);

            if (printProbabilities) writer.saveProbabilitiesToCSV(fullPath2, true, i);
            if (printNeighbors) writer.saveLymphocyteNeighborstoCSV(fullPath3, true, i);
            model.DrawModelandUpdateProb(win, gif, params); //get occupied spaces to use for stepCells method, rerun if model pop goes to 0

            // Save PNG frame for video at regular intervals and at the final timestep
            if (saveVideo && (i % videoFrameInterval == 0 || i == timesteps)) {
                win.ToPNG(framesDir + "/frame_" + String.format("%05d", frameCount) + ".png");
                frameCount++;
            }

            if (TumorCells.count == 0) {
                // Capture final frame before breaking (skip if already saved this timestep)
                if (saveVideo && i % videoFrameInterval != 0 && i != timesteps) {
                    win.ToPNG(framesDir + "/frame_" + String.format("%05d", frameCount) + ".png");
                    frameCount++;
                }
                System.out.println("Timestep tumor population reached 0: " + i + "\n");
                break;
            }
        }

        gif.Close();
        // Delete the dummy GIF file
        new File(directory + "/tmp.gif").delete();

        // Convert PNG frames to MP4 video using ffmpeg
        if (saveVideo && frameCount > 0) {
            System.out.println("Converting " + frameCount + " frames to MP4...");
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "ffmpeg", "-y",
                        "-framerate", String.valueOf(videoFPS),
                        "-i", framesDir + "/frame_%05d.png",
                        "-vcodec", "libx264",
                        "-pix_fmt", "yuv420p",
                        videoPath
                );
                pb.inheritIO();
                Process proc = pb.start();
                int exitCode = proc.waitFor();
                if (exitCode == 0) {
                    System.out.println("Simulation video saved to: " + videoPath);
                    // Clean up frame PNGs
                    File framesDirFile = new File(framesDir);
                    File[] frames = framesDirFile.listFiles();
                    if (frames != null) {
                        for (File f : frames) f.delete();
                    }
                    framesDirFile.delete();
                } else {
                    System.err.println("ffmpeg exited with code " + exitCode + " — PNG frames kept in: " + framesDir);
                }
            } catch (Exception e) {
                System.err.println("Could not run ffmpeg (is it installed?). PNG frames kept in: " + framesDir);
            }
        }

        writer.printPopulation(Lymphocytes.name, Lymphocytes.colorIndex, Lymphocytes.count);
        writer.printPopulation(TumorCells.name, TumorCells.colorIndex, TumorCells.count);
        writer.printPopulation(DoomedCells.name, DoomedCells.colorIndex, DoomedCells.count);
        writer.printPopulation(TriggeringCells.name, TriggeringCells.colorIndex, TriggeringCells.count);
        System.out.println("Population Total: " + model.Pop());
        model.updateSpaces(win,params);
        System.out.println("Unoccupied Spaces: " + availableSpaces.size());
        System.out.println();

        System.out.println(java.time.LocalDateTime.now() + " - Simulation execution completed successfully.");

        win.Close();
    }
}
