package clearcontrol.microscope.lightsheet.timelapse;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionProcessor;
import clearcontrol.microscope.lightsheet.processor.MetaDataFusion;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.state.LightSheetAcquisitionStateInterface;
import clearcontrol.microscope.stacks.metadata.MetaDataAcquisitionType;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.metadata.MetaDataOrdinals;
import clearcontrol.stack.metadata.StackMetaData;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class SequentialAcquisitionScheduler extends SchedulerBase implements
                                                                  SchedulerInterface,
                                                                  LoggingFeature
{
  LightSheetMicroscope mLightSheetMicroscope;
  InterpolatedAcquisitionState mCurrentState;
  LightSheetTimelapse mTimelapse;

  /**
   * INstanciates a virtual device with a given name
   *
   */
  public SequentialAcquisitionScheduler()
  {
    super("Sequential acquisition");
  }

  @Override public boolean doExperiment(long pTimePoint)
  {
    if (!(mMicroscope instanceof LightSheetMicroscope)) {
      warning("" + this + " needs a lightsheet microscope!");
      return false;
    }
    mLightSheetMicroscope = (LightSheetMicroscope) mMicroscope;
    mCurrentState = (InterpolatedAcquisitionState) mLightSheetMicroscope.getAcquisitionStateManager().getCurrentState();
    mTimelapse = mLightSheetMicroscope.getDevice(LightSheetTimelapse.class, 0);

    // reconfigure FastFusion engine
    LightSheetFastFusionProcessor lLightSheetFastFusionProcessor = mLightSheetMicroscope.getDevice(LightSheetFastFusionProcessor.class, 0);
    lLightSheetFastFusionProcessor.getInterleavedSwitchVariable().set(false);

    int lNumberOfDetectionArms = mLightSheetMicroscope.getNumberOfDetectionArms();

    int lNumberOfLightSheets = mLightSheetMicroscope.getNumberOfLightSheets();

    HashMap<Integer, LightSheetMicroscopeQueue> lViewToQueueMap = new HashMap<>();

    // preparing queues:
    for (int l = 0; l < lNumberOfLightSheets; l++)
      if (mCurrentState.getLightSheetOnOffVariable(l).get())
      {
        LightSheetMicroscopeQueue
            lQueueForView =
            getQueueForSingleLightSheet(mCurrentState, l);

        lViewToQueueMap.put(l, lQueueForView);
      }

    // playing the queues in sequence:

    for (int l = 0; l < lNumberOfLightSheets; l++) {
      if (mCurrentState.getLightSheetOnOffVariable(l).get())
      {
        LightSheetMicroscopeQueue lQueueForView = lViewToQueueMap.get(l);

        for (int c = 0; c < lNumberOfDetectionArms; c++)
          if (mCurrentState.getCameraOnOffVariable(c).get())
          {

            StackMetaData
                lMetaData =
                lQueueForView.getCameraDeviceQueue(c)
                             .getMetaDataVariable()
                             .get();

            lMetaData.addEntry(MetaDataAcquisitionType.AcquisitionType,
                               AcquisitionType.TimelapseSequential);
            lMetaData.addEntry(MetaDataView.Camera, c);
            lMetaData.addEntry(MetaDataView.LightSheet, l);

            if (mTimelapse.getFuseStacksVariable().get())
            {
              if (mTimelapse.getFuseStacksPerCameraVariable().get())
                lMetaData.addEntry(MetaDataFusion.RequestPerCameraFusion,
                                   true);
              else
                lMetaData.addEntry(MetaDataFusion.RequestFullFusion,
                                   true);

              lMetaData.addEntry(MetaDataChannel.Channel,  "sequential");
            }
            else
            {
              String lCxLyString = MetaDataView.getCxLyString(lMetaData);
              lMetaData.addEntry(MetaDataChannel.Channel,
                                 lCxLyString);
            }
          }

        try
        {
          mLightSheetMicroscope.playQueueAndWait(lQueueForView,
                                                 mTimelapse.getTimeOut(),
                                                 TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
          return false;
        }
        catch (ExecutionException e)
        {
          e.printStackTrace();
          return false;
        }
        catch (TimeoutException e)
        {
          e.printStackTrace();
          return false;
        }

      }
    }

    return true;
  }

  protected LightSheetMicroscopeQueue getQueueForSingleLightSheet(LightSheetAcquisitionStateInterface<?> pCurrentState,
                                                                  int pLightSheetIndex)
  {
    int lNumberOfDetectionArms =
        mLightSheetMicroscope.getNumberOfDetectionArms();

    @SuppressWarnings("unused")
    int lNumberOfLightSheets =
        mLightSheetMicroscope.getNumberOfLightSheets();

    int lNumberOfLaserLines =
        mLightSheetMicroscope.getNumberOfLaserLines();

    int lNumberOfEDFSlices = mTimelapse.getExtendedDepthOfFieldAcquisitionVariable().get()?10:0;

    LightSheetMicroscopeQueue lQueue =
        pCurrentState.getQueue(0,
                               lNumberOfDetectionArms,
                               pLightSheetIndex,
                               pLightSheetIndex + 1,
                               0,
                               lNumberOfLaserLines,
                               lNumberOfEDFSlices);

    for (int l = 0; l < mLightSheetMicroscope.getNumberOfLightSheets(); l++)
    {
      info("Light sheet " + l + " W: " + lQueue.getIW(l));
    }
    for (int l = 0; l < mLightSheetMicroscope.getNumberOfLightSheets(); l++)
    {
      info("Light sheet " + l + " H: " + lQueue.getIH(l));
    }

    lQueue.addMetaDataEntry(MetaDataOrdinals.TimePoint,
                            mTimelapse.getTimePointCounterVariable().get());

    return lQueue;
  }

}
