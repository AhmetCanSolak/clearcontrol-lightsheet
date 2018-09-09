package clearcontrol.microscope.lightsheet.state.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.state.instructions.ChangeExposureTimeInstruction;

/**
 * ChangeExposureTimeInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 06 2018
 */
public class ChangeExposureTimeInstructionPanel extends CustomGridPane
{
  public ChangeExposureTimeInstructionPanel(ChangeExposureTimeInstruction pInstruction)
  {
    addDoubleField(pInstruction.getExposureTimeInSecondsVariable(),
                   0);
  }
}
