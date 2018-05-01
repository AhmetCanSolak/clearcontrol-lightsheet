package clearcontrol.microscope.lightsheet.warehouse.containers.io;

import clearcl.imagej.ClearCLIJ;
import clearcl.util.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.timelapse.TimelapseInterface;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.sourcesink.sink.FileStackSinkInterface;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;

import java.io.File;

/**
 * WriteStackInterfaceContainerAsTifToDiscScheduler
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class WriteStackInterfaceContainerAsTifToDiscScheduler extends
        SchedulerBase implements
        LoggingFeature
{
    Class mContainerClass;
    String[] mImageKeys = null;
    String mChannelName = null;

    /**
     * INstanciates a virtual device with a given name
     *
     * @param pDeviceName device name
     */
    public WriteStackInterfaceContainerAsTifToDiscScheduler(String pDeviceName, Class pContainerClass, String[] pImageKeys, String pChannelName)
    {
        super(pDeviceName);
        mContainerClass = pContainerClass;
        mImageKeys = pImageKeys;
        if (pChannelName != null && pChannelName.length() > 0)
        {
            mChannelName = pChannelName;
        }
    }

    @Override public boolean initialize()
    {
        return false;
    }

    @Override public boolean enqueue(long pTimePoint)
    {

        if (!(mMicroscope instanceof LightSheetMicroscope)) {
            warning("I need a LightSheetMicroscope!");
            return false;
        }


        LightSheetTimelapse lTimelapse = (LightSheetTimelapse) mMicroscope.getDevice(TimelapseInterface.class, 0);
        File lWorkingDirectory = lTimelapse.getWorkingDirectory();

        DataWarehouse lDataWarehouse = ((LightSheetMicroscope) mMicroscope).getDataWarehouse();

        StackInterfaceContainer lContainer = lDataWarehouse.getOldestContainer(mContainerClass);
        if (lContainer == null) {
            warning("No " + mContainerClass.getCanonicalName() + " found for saving");
            return false;
        }

        for (String key : mImageKeys)
        {
            StackInterface lStack = lContainer.get(key);
            if (mChannelName != null)
            {
                saveStack(lWorkingDirectory, mChannelName, lStack, pTimePoint);
            } else {
                saveStack(lWorkingDirectory, key, lStack, pTimePoint);
            }
        }
        return true;
    }

    private void saveStack(File lWorkingDirectory, String pChannelName, StackInterface lStack, long lTimePoint){
        ElapsedTime.measureForceOutput(this + " stack saving",
                () -> {

                    new File(lWorkingDirectory + "/stacks/" + pChannelName + "/").mkdirs();

                    int lDigits = 6;

                    ImagePlus lConvertedImp = ClearCLIJ.getInstance().converter(lStack).getImagePlus();
                    if (lStack.getMetaData() != null) {
                        Calibration lCalibration = lConvertedImp.getCalibration();
                        lCalibration.pixelWidth = lStack.getMetaData().getVoxelDimX();
                        lCalibration.pixelHeight = lStack.getMetaData().getVoxelDimY();
                        lCalibration.pixelDepth = lStack.getMetaData().getVoxelDimZ();
                        lCalibration.setUnit("micron");
                    }
                    IJ.saveAsTiff(lConvertedImp, lWorkingDirectory + "/stacks/" + pChannelName + "/" + String.format("%0" + lDigits + "d", lTimePoint) + ".tif");
        });

    }
}
