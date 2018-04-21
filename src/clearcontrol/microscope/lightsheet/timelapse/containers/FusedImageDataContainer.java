package clearcontrol.microscope.lightsheet.timelapse.containers;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.StackInterfaceContainer;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class FusedImageDataContainer extends StackInterfaceContainer
{
  public FusedImageDataContainer(LightSheetMicroscope pLightSheetMicroscope) {
    super(pLightSheetMicroscope);
  }

  @Override public boolean isDataComplete()
  {
    return super.containsKey("fused");
  }
}