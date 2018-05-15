package clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler;

import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FReader;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.timelapse.TimelapseInterface;
import org.ejml.data.DenseMatrix64F;

import java.io.File;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class MirrorModeScheduler extends SchedulerBase implements
                                                                            LoggingFeature
{
  private Variable<File> mRootFolderVariable =
      new Variable("RootFolder",
                   (Object) null);

  private SpatialPhaseModulatorDeviceInterface mSpatialPhaseModulatorDeviceInterface;

  public MirrorModeScheduler(SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface) {
    super("Adaptation: Mirror mode scheduler for " + pSpatialPhaseModulatorDeviceInterface.getName());

    mSpatialPhaseModulatorDeviceInterface = pSpatialPhaseModulatorDeviceInterface;
  }



  public Variable<File> getRootFolderVariable()
  {
    return mRootFolderVariable;
  }
  private int mStopVariable = 0;
  private int mTimePointCount = 0;

  @Override public boolean initialize()
  {
    mTimePointCount = 0;
    mStopVariable = 0;
    return true;
  }

  @Override public boolean enqueue(long pTimePoint) {

    File lFolder = mRootFolderVariable.get();
    /*if(mStopVariable == 1){
      TimelapseInterface lTimelapse = (TimelapseInterface) mMicroscope.getDevice(TimelapseInterface.class, 0);

      if (lTimelapse != null) {
        lTimelapse.stopTimelapse();
      }
      LaserDeviceInterface lLaser = (LaserDeviceInterface) mMicroscope.getDevice(LaserDeviceInterface.class, 0);
      lLaser.setLaserOn(false);
      lLaser.setLaserPowerOn(false);
      lLaser.setLaserOn(false);
      lLaser.setLaserPowerOn(false);
    }
    if(mTimePointCount >= lFolder.listFiles().length) {
      mStopVariable = 1;
    }*/

    if (lFolder == null || !lFolder.isDirectory()) {
      warning("Error: given root folder is no directory");
      return false;
    }
    long lFileIndex = mTimePointCount;

    File lFile = lFolder.listFiles()[(int)lFileIndex];

    DenseMatrix64F lMatrix = mSpatialPhaseModulatorDeviceInterface.getMatrixReference().get();
        //new DenseMatrix64F(mSpatialPhaseModulatorDeviceInterface.getMatrixHeight(), mSpatialPhaseModulatorDeviceInterface.getMatrixWidth());

    if (mMicroscope instanceof LightSheetMicroscope) {
      ((LightSheetMicroscope) mMicroscope).getTimelapse().log("Loading " + lFile);
    }
    info("Loading " + lFile);
    DenseMatrix64FReader lMatrixReader = new DenseMatrix64FReader(lFile, lMatrix);

    if (!lMatrixReader.read()) {
      if (mMicroscope instanceof LightSheetMicroscope) {
        ((LightSheetMicroscope) mMicroscope).getTimelapse().log("Error: matrix file could not be loaded");
      }
      warning("Error: matrix file could not be loaded");


    }

    info("Sending matrix to mirror");
    mSpatialPhaseModulatorDeviceInterface.getMatrixReference().set(lMatrix);

    info("Sent. Scheduler done");

    mTimePointCount++;
    if(mTimePointCount >= lFolder.listFiles().length)
    {
      mTimePointCount = 0;

    }
    return true;
  }
}
