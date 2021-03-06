package clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.gui;

import javafx.scene.control.Button;

import clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.ViewStack3DInBigDataViewerInstruction;

/**
 * ViewStack3DInBigDataViewerInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class ViewStack3DInBigDataViewerInstructionPanel extends
                                                        ViewStackInstructionBasePanel
{
  public ViewStack3DInBigDataViewerInstructionPanel(ViewStack3DInBigDataViewerInstruction pInstruction)
  {
    super(pInstruction);

    Button lResetButton = new Button("Reset");
    lResetButton.setOnAction((e) -> {
      pInstruction.resetBigDataViewer();
    });
    add(lResetButton, 0, 1);
  }
}
