package clearcontrol.microscope.lightsheet.warehouse.containers.io;

import java.io.File;

import clearcl.imagej.ClearCLIJ;
import clearcl.util.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;

/**
 * The WriteStackInterfaceContainerAsTifToDiscInstruction writes a container to
 * disc in TIF format.
 *
 * Author: @haesleinhuepf 05 2018
 */
public class WriteStackInterfaceContainerAsTifToDiscInstruction extends
                                                                LightSheetMicroscopeInstructionBase
                                                                implements
                                                                LoggingFeature
{
  protected Class mContainerClass;
  protected String[] mImageKeys = null;
  protected String mChannelName = null;

  public WriteStackInterfaceContainerAsTifToDiscInstruction(Class pContainerClass,
                                                            LightSheetMicroscope pLightSheetMicroscope)
  {
    this("IO: Write " + pContainerClass.getSimpleName()
         + " as TIF to disc",
         pContainerClass,
         null,
         null,
         pLightSheetMicroscope);
  }

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pDeviceName
   *          device name
   */
  public WriteStackInterfaceContainerAsTifToDiscInstruction(String pDeviceName,
                                                            Class pContainerClass,
                                                            String[] pImageKeys,
                                                            String pChannelName,
                                                            LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pDeviceName, pLightSheetMicroscope);
    mContainerClass = pContainerClass;
    mImageKeys = pImageKeys;
    if (pChannelName != null && pChannelName.length() > 0)
    {
      mChannelName = pChannelName;
    }
  }

  @Override
  public boolean initialize()
  {
    return false;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    LightSheetTimelapse lTimelapse =
                                   getLightSheetMicroscope().getTimelapse();
    File lWorkingDirectory = lTimelapse.getWorkingDirectory();

    DataWarehouse lDataWarehouse =
                                 getLightSheetMicroscope().getDataWarehouse();

    StackInterfaceContainer lContainer =
                                       lDataWarehouse.getOldestContainer(mContainerClass);
    if (lContainer == null)
    {
      warning("No " + mContainerClass.getCanonicalName()
              + " found for saving");
      return false;
    }

    String[] lImageKeys = mImageKeys;
    if (lImageKeys == null)
    {
      lImageKeys = new String[lContainer.keySet().size()];
      lContainer.keySet().toArray(lImageKeys);
    }
    for (String key : lImageKeys)
    {
      StackInterface lStack = lContainer.get(key);
      if (mChannelName != null)
      {
        saveStack(lWorkingDirectory,
                  mChannelName,
                  lStack,
                  pTimePoint);
      }
      else
      {
        saveStack(lWorkingDirectory, key, lStack, pTimePoint);
      }
    }
    return true;
  }

  @Override
  public WriteStackInterfaceContainerAsTifToDiscInstruction copy()
  {
    return new WriteStackInterfaceContainerAsTifToDiscInstruction(getName(),
                                                                  mContainerClass,
                                                                  mImageKeys,
                                                                  mChannelName,
                                                                  getLightSheetMicroscope());
  }

  private void saveStack(File lWorkingDirectory,
                         String pChannelName,
                         StackInterface lStack,
                         long lTimePoint)
  {
    ElapsedTime.measureForceOutput(this + " stack saving", () -> {

      new File(lWorkingDirectory + "/stacks/"
               + pChannelName
               + "/").mkdirs();

      int lDigits = 6;

      ImagePlus lConvertedImp = ClearCLIJ.getInstance()
                                         .converter(lStack)
                                         .getImagePlus();
      if (lStack.getMetaData() != null)
      {
        Calibration lCalibration = lConvertedImp.getCalibration();
        lCalibration.pixelWidth = lStack.getMetaData().getVoxelDimX();
        lCalibration.pixelHeight =
                                 lStack.getMetaData().getVoxelDimY();
        lCalibration.pixelDepth = lStack.getMetaData().getVoxelDimZ();
        lCalibration.setUnit("micron");
      }
      IJ.saveAsTiff(lConvertedImp,
                    lWorkingDirectory + "/stacks/"
                                   + pChannelName
                                   + "/"
                                   + String.format("%0" + lDigits
                                                   + "d",
                                                   lTimePoint)
                                   + ".tif");
    });

  }
}
