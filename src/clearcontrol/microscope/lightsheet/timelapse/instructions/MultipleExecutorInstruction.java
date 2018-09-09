package clearcontrol.microscope.lightsheet.timelapse.instructions;

import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;

/**
 * MultipleExecutorInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class MultipleExecutorInstruction extends InstructionBase
{
  private final InstructionInterface[] schedulersToExecute;

  /**
   * INstanciates a virtual device with a given name
   *
   */
  public MultipleExecutorInstruction(InstructionInterface[] schedulersToExecute)
  {
    super("Smart: Execute several instructions "
          + schedulersToExecute);
    this.schedulersToExecute = schedulersToExecute;
  }

  @Override
  public boolean initialize()
  {
    for (InstructionInterface scheduler : schedulersToExecute)
    {
      scheduler.initialize();
    }
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    for (InstructionInterface scheduler : schedulersToExecute)
    {
      scheduler.enqueue(pTimePoint);
    }
    return false;
  }

  @Override
  public MultipleExecutorInstruction copy()
  {
    return new MultipleExecutorInstruction(schedulersToExecute);
  }
}
