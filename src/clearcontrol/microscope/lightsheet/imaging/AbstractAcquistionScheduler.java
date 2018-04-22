package clearcontrol.microscope.lightsheet.imaging;

import clearcl.util.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionProcessor;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import clearcontrol.stack.sourcesink.sink.FileStackSinkInterface;
import coremem.recycling.RecyclerInterface;

import java.util.concurrent.TimeUnit;

/**
 * This class contains generalised methods for all AcquisitionSchedulers
 *
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public abstract class AbstractAcquistionScheduler extends SchedulerBase implements
                                                                        SchedulerInterface,
                                                                        LoggingFeature
{

  protected String mImageKeyToSave = "fused";
  protected String mChannelName = "default";

  protected StackInterface mLastFusedStack;
  protected StackInterface mLastAcquiredStack;

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pDeviceName device name
   */
  public AbstractAcquistionScheduler(String pDeviceName)
  {
    super(pDeviceName);
  }

  protected LightSheetMicroscope mLightSheetMicroscope;
  protected InterpolatedAcquisitionState mCurrentState;
  protected LightSheetTimelapse mTimelapse;

  @Override public boolean initialize()
  {
    if (!(mMicroscope instanceof LightSheetMicroscope)) {
      warning("" + this + " needs a lightsheet microscope!");
      return false;
    }

    mLightSheetMicroscope = (LightSheetMicroscope) mMicroscope;
    mCurrentState = (InterpolatedAcquisitionState) mLightSheetMicroscope.getAcquisitionStateManager().getCurrentState();
    mTimelapse = mLightSheetMicroscope.getDevice(LightSheetTimelapse.class, 0);

    LightSheetFastFusionProcessor
        lProcessor =
        mLightSheetMicroscope.getDevice(
            LightSheetFastFusionProcessor.class,
            0);
    if (lProcessor != null) {
      lProcessor.initializeEngine();
    }

    return true;
  }

  protected void putStackInContainer(String pKey,
                                     StackInterface pStack,
                                     StackInterfaceContainer pContainer)
  {
    RecyclerInterface<StackInterface, StackRequest> lRecycler = mLightSheetMicroscope.getDataWarehouse().getRecycler();

    Variable<StackInterface>
        lStackCopyVariable = new Variable<StackInterface>("stackcopy", null);
    ElapsedTime.measureForceOutput("Copy stack (" + pKey + ") for container", () -> {
      lStackCopyVariable.set(
          lRecycler.getOrWait(1000, TimeUnit.SECONDS, StackRequest.build(pStack.getDimensions())));

      // we need to copy the data out of the input-buffer from the camera
      pStack.getContiguousMemory().copyTo(lStackCopyVariable.get().getContiguousMemory());
      lStackCopyVariable.get().setMetaData(pStack.getMetaData().clone());
    });

    info(pKey + " in a container " + MetaDataView.getCxLyString(lStackCopyVariable.get().getMetaData()));
    pContainer.put(pKey,
                   lStackCopyVariable.get());

  }


  @Deprecated
  protected void initializeStackSaving(FileStackSinkInterface pFileStackSinkInterface) {
    warning("initializeStackSaving is deprecated and will be removed");
  }

  protected void goToInitialPosition(LightSheetMicroscope lLightsheetMicroscope,
                                   LightSheetMicroscopeQueue lQueue,
                                   double lIlluminationZStart,
                                   double lDetectionZZStart)
  {
    double widthBefore = lQueue.getIW(0);

    ((InterpolatedAcquisitionState)lLightsheetMicroscope.getAcquisitionStateManager().getCurrentState()).applyAcquisitionStateAtZ(lQueue, lIlluminationZStart);
    for (int l = 0; l
                    < lLightsheetMicroscope.getNumberOfLightSheets(); l++)
    {
      lQueue.setI(l, false);
      lQueue.setIZ(lIlluminationZStart);
    }
    for (int d = 0; d
                    < lLightsheetMicroscope.getNumberOfDetectionArms(); d++)
    {
      lQueue.setDZ(d, lDetectionZZStart);
      lQueue.setC(d, false);

    }
    double widthAfter = lQueue.getIW(0);

    if (Math.abs(widthAfter - widthBefore) > 0.1)
    {
      // if the width of the light sheets changed significantly, we
      // need to wait a second until the iris has been moved...
      lQueue.setExp(0.5);
    }
    lQueue.addCurrentStateToQueue();
    lQueue.addCurrentStateToQueue();
    lQueue.addVoxelDimMetaData(lLightsheetMicroscope, mCurrentState.getStackZStepVariable().get().doubleValue());
  }


  @Deprecated
  protected void handleImageFromCameras(long pTimepoint) {
    warning("handleImagesFromCameras is deprecated and will be removed ");
  }

  /**
   * Deprecated: access resultimg image stacks from the data warehouse
   * @return
   */
  @Deprecated
  public StackInterface getLastAcquiredStack()
  {
    warning("getLastAcquiredStack is deprecated and will be removed. Access acquired images via the DataWarehouse instead!");
    return mLastFusedStack;
  }


  protected boolean isCameraOn(int pCameraIndex) {
    return mCurrentState.getCameraOnOffVariable(pCameraIndex).get();
  }

  protected boolean isFused() {
    return true;
  }
}
