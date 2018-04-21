package clearcontrol.microscope.lightsheet.processor.fusion;

import clearcl.util.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionProcessor;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import clearcontrol.stack.metadata.MetaDataOrdinals;
import coremem.recycling.RecyclerInterface;

import java.util.Arrays;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public abstract class FusionScheduler extends SchedulerBase implements
                                                   LoggingFeature
{

  private static Object mLock = new Object();
  private StackInterface mFusedStack = null;

  protected LightSheetMicroscope mLightSheetMicroscope;


  /**
   * INstanciates a virtual device with a given name
   *
   * @param pDeviceName device name
   */
  public FusionScheduler(String pDeviceName)
  {
    super(pDeviceName);
  }

  @Override public boolean initialize()
  {
    if (!(mMicroscope instanceof LightSheetMicroscope)) {
      warning("I'm only compatible to LightSheetMicroscopes!");
      return false;
    }

    mLightSheetMicroscope =
        (LightSheetMicroscope) mMicroscope;
    return true;
  }

  protected StackInterface fuseStacks(StackInterfaceContainer pContainer, String[] pImageKeys)
  {
    mFusedStack = null;

    final LightSheetFastFusionProcessor
        lProcessor =
        mLightSheetMicroscope.getDevice(
            LightSheetFastFusionProcessor.class,
            0);

    final RecyclerInterface<StackInterface, StackRequest> lRecycler = mLightSheetMicroscope.getDataWarehouse().getRecycler();

    ElapsedTime.measure("Handle container (" + pContainer + ") and fuse", () ->
    {
      synchronized (mLock)
      {
        info("available keys in container: " + pContainer.keySet());
        info("needed keys in container: " + Arrays.toString(pImageKeys));
        for (String key : pImageKeys)
        {
          StackInterface lResultingStack = pContainer.get(key);


          info("sending(" + key + "): " + lResultingStack + " aka " + MetaDataView
              .getCxLyString(lResultingStack.getMetaData()));
          StackInterface
              lStackInterface =
              lProcessor.process(lResultingStack, lRecycler);
          info("Got back: " + lStackInterface);
          if (lStackInterface != null)
          {
            mFusedStack = lStackInterface;
          }
        }
        if (mFusedStack == null) {
          lProcessor.getEngine().executeAllTasks();
          warning("Finished, but there are just " + lProcessor.getEngine().getAvailableImagesSlotKeys());
        }
      }
    });

    return mFusedStack;
  }

  protected void storeFusedContainer(StackInterface lFusedStack) {
    long lTimePoint = lFusedStack.getMetaData().getValue(
        MetaDataOrdinals.TimePoint);
    DataWarehouse lDataWarehouse = mLightSheetMicroscope.getDataWarehouse();
    FusedImageDataContainer
        lFusedContainer = new FusedImageDataContainer(mLightSheetMicroscope);
    lFusedContainer.put("fused", lFusedStack);
    lDataWarehouse.put("fused_" + lTimePoint, lFusedContainer);
  }

  public StackInterface getFusedStack()
  {
    return mFusedStack;
  }
}