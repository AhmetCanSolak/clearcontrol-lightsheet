package clearcontrol.microscope.lightsheet.imaging.sequential;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * This container contains the raw images resulting from sequential acquisition.
 * For example for a microscope with two cameras, the stack have these keys:
 *
 * C0L0 C1L0 C0L1 C1L1
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) April 2018
 */
public class SequentialImageDataContainer extends
                                          StackInterfaceContainer
{
  LightSheetMicroscope mLightSheetMicroscope;

  public SequentialImageDataContainer(LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pLightSheetMicroscope.getDevice(LightSheetTimelapse.class,
                                          0)
                               .getTimePointCounterVariable()
                               .get());

    mLightSheetMicroscope = pLightSheetMicroscope;
  }

  @Override
  public boolean isDataComplete()
  {
    for (int l =
               0; l < mLightSheetMicroscope.getNumberOfDetectionArms(); l++)
    {
      for (int d =
                 0; d < mLightSheetMicroscope.getNumberOfDetectionArms(); d++)
      {
        if (!super.containsKey("C" + d + "L" + l))
        {
          return false;
        }
      }
    }
    return true;
  }
}
