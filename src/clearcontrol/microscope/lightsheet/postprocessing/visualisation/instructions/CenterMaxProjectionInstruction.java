package clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions;

import clearcl.imagej.ClearCLIJ;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.TimeStampContainer;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;
import de.mpicbg.rhaase.spimcat.postprocessing.fijiplugins.projection.presentation.HalfStackProjectionPlugin;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.*;
import java.io.File;
import java.time.Duration;

/**
 * HalfStackMaxProjectionInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class CenterMaxProjectionInstruction<T extends StackInterfaceContainer> extends LightSheetMicroscopeInstructionBase implements LoggingFeature {

    private final Class<T> mClass;
    private Variable<String> mMustContainStringVariable = new Variable<String>("", "");
    private Variable<Boolean> mPrintSequenceNameVariable = new Variable<Boolean>("Print sequence name", true);
    private Variable<Boolean> mPrintTimePointVariable = new Variable<Boolean>("Print time point", true);

    private BoundedVariable<Integer> mFontSizeVariable = new BoundedVariable<Integer>("Font size", 14, 5, Integer.MAX_VALUE);
    private BoundedVariable<Integer> mStartZPlaneIndex = new BoundedVariable<Integer>("Start Z plane index", 0, 0, Integer.MAX_VALUE);
    private BoundedVariable<Integer> mEndZPlaneIndex = new BoundedVariable<Integer>("End Z plane index", 0, 0, Integer.MAX_VALUE);

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public CenterMaxProjectionInstruction(Class<T> pClass, LightSheetMicroscope pLightSheetMicroscope) {
        super("Post-processing: Thumbnail (center max projection) generator for " + pClass.getSimpleName(), pLightSheetMicroscope);
        mClass = pClass;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        // Read oldest image from the warehouse
        DataWarehouse lDataWarehouse = getLightSheetMicroscope().getDataWarehouse();

        T lContainer = lDataWarehouse.getOldestContainer(mClass);

        String key = lContainer.keySet().iterator().next();
        StackInterface lStack = lContainer.get(key);

        String targetFolder = getLightSheetMicroscope().getDevice(LightSheetTimelapse.class, 0).getWorkingDirectory().toString();
        long lTimePoint = lContainer.getTimepoint();
        int lDigits = 6;

        // Process the image
        ClearCLIJ clij = ClearCLIJ.getInstance();
        ImagePlus lImagePlus = clij.converter(lStack).getImagePlus();

        if (lStack.getMetaData() != null) {
            lImagePlus.getCalibration().setUnit("micron");
            lImagePlus.getCalibration().pixelWidth = lStack.getMetaData().getVoxelDimX();
            lImagePlus.getCalibration().pixelHeight = lStack.getMetaData().getVoxelDimY();
            lImagePlus.getCalibration().pixelDepth = lStack.getMetaData().getVoxelDimZ();
        }

        HalfStackProjectionPlugin halfStackProjectionPlugin = new HalfStackProjectionPlugin();
        halfStackProjectionPlugin.setInputImage(lImagePlus);
        halfStackProjectionPlugin.minSlice = mStartZPlaneIndex.get(); //(int)(lImagePlus.getNSlices() * 0.25);
        halfStackProjectionPlugin.maxSlice = mEndZPlaneIndex.get(); //(int)(lImagePlus.getNSlices() * 0.75);

        halfStackProjectionPlugin.setSilent(true);
        halfStackProjectionPlugin.setShowResult(false);
        halfStackProjectionPlugin.run();
        ImagePlus lResultImagePlus = halfStackProjectionPlugin.getOutputImage();

        String folderName = "thumbnails_center";

        new File(targetFolder + "/stacks/" + folderName + "/").mkdirs();

        IJ.run(lResultImagePlus, "Enhance Contrast", "saturated=0.35");
        IJ.saveAsTiff(lResultImagePlus, targetFolder + "/stacks/" + folderName + "/" +  String.format("%0" + lDigits + "d", lTimePoint) + ".tif");


        //
        if (lStack.getMetaData() != null) {
            IJ.run(lResultImagePlus, "16-bit", "");
            Font font = new Font("SanSerif", Font.PLAIN, mFontSizeVariable.get());
            ImageProcessor ip = lResultImagePlus.getProcessor();

            ip.setFont(font);
            ip.setColor(new Color(255, 255, 255));

            TimeStampContainer lStartTimeInNanoSecondsContainer = TimeStampContainer.getGlobalTimeSinceStart(getLightSheetMicroscope().getDataWarehouse(), pTimePoint, lStack);

            Duration duration = Duration.ofNanos(lStack.getMetaData().getTimeStampInNanoseconds() - lStartTimeInNanoSecondsContainer.getTimeStampInNanoSeconds());
            long s = duration.getSeconds();
            ip.drawString(String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60)) + (mPrintTimePointVariable.get()?" (tp " + pTimePoint + ")":"") + "\n" + (mPrintSequenceNameVariable.get()?key:""), 20, 30);

            lResultImagePlus.updateAndDraw();

            IJ.run(lResultImagePlus, "Scale Bar...", "width=100 height=3 font=" + mFontSizeVariable.get() + " color=White background=None location=[Lower Left]");

            String foldername = targetFolder + "/stacks/" + folderName + "_" + mStartZPlaneIndex.get() + "_" + mEndZPlaneIndex.get() + "_text/";

            new File(foldername).mkdirs();
            IJ.saveAsTiff(lResultImagePlus, foldername + String.format("%0" + lDigits + "d", lTimePoint) + ".tif");
        } else {
            warning("Error: No meta data provided!");
        }

        return true;
    }

    public BoundedVariable<Integer> getFontSizeVariable() {
        return mFontSizeVariable;
    }

    public Variable<String> getMustContainStringVariable() {
        return mMustContainStringVariable;
    }

    @Override
    public CenterMaxProjectionInstruction copy() {
        CenterMaxProjectionInstruction copied = new CenterMaxProjectionInstruction(mClass, getLightSheetMicroscope());
        copied.mMustContainStringVariable.set(mMustContainStringVariable.get());
        return copied;
    }

    public BoundedVariable<Integer> getStartZPlaneIndex() {
        return mStartZPlaneIndex;
    }

    public BoundedVariable<Integer> getEndZPlaneIndex() {
        return mEndZPlaneIndex;
    }

    public Variable<Boolean> getPrintSequenceNameVariable() {
        return mPrintSequenceNameVariable;
    }

    public Variable<Boolean> getPrintTimePointVariable() {
        return mPrintTimePointVariable;
    }

}
