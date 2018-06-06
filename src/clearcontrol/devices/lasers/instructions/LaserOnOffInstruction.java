package clearcontrol.devices.lasers.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class LaserOnOffInstruction extends InstructionBase implements
        InstructionInterface,
                                                       LoggingFeature
{
  LaserDeviceInterface mLaserDevice;
  boolean mTurnOn;

  /**
   * INstanciates a virtual device with a given name
   *
   */
  public LaserOnOffInstruction(LaserDeviceInterface pLaserDevice, boolean pTurnOn)
  {
    super("Laser: Turn " + pLaserDevice.getName() + " " + (pTurnOn?"ON":"OFF"));
    mLaserDevice = pLaserDevice;
    mTurnOn = pTurnOn;
  }

  @Override public boolean initialize()
  {
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    mLaserDevice.setLaserPowerOn(mTurnOn);
    mLaserDevice.setLaserOn(mTurnOn);
    mLaserDevice.setLaserPowerOn(mTurnOn);
    mLaserDevice.setLaserOn(mTurnOn);
    return true;
  }

  @Override
  public LaserOnOffInstruction copy() {
    return new LaserOnOffInstruction(mLaserDevice, mTurnOn);
  }

}