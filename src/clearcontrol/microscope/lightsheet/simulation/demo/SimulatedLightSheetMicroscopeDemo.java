package clearcontrol.microscope.lightsheet.simulation.demo;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackends;
import clearcontrol.core.concurrent.thread.ThreadSleep;
import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.microscope.lightsheet.gui.LightSheetMicroscopeGUI;
import clearcontrol.microscope.lightsheet.simulation.LightSheetMicroscopeSimulationDevice;
import clearcontrol.microscope.lightsheet.simulation.SimulatedLightSheetMicroscope;
import clearcontrol.microscope.lightsheet.simulation.SimulationUtils;

import org.junit.Test;

/**
 * Simulated lightsheet microscope demo
 *
 * @author royer
 */
public class SimulatedLightSheetMicroscopeDemo
{

  /**
   * Demo
   * 
   * @throws Exception
   *           NA
   */
  @Test
  public void demo() throws Exception
  {
    boolean lDummySimulation = false;
    boolean lUniformFluorescence = false;

    boolean l2DDisplayFlag = true;
    boolean l3DDisplayFlag = true;

    int lMaxNumberOfStacks = 32;

    int lMaxCameraResolution = 512;

    int lNumberOfLightSheets = 4;
    int lNumberOfDetectionArms = 2;

    float lDivisionTime = 11f;

    int lPhantomWidth = 128;
    int lPhantomHeight = lPhantomWidth;
    int lPhantomDepth = lPhantomWidth;

    ClearCL lClearCL = new ClearCL(ClearCLBackends.getBestBackend());

    for (ClearCLDevice lClearCLDevice : lClearCL.getAllDevices())
      System.out.println(lClearCLDevice.getName());

    MachineConfiguration lMachineConfiguration =
                                               MachineConfiguration.get();

    ClearCLContext lSimulationContext =
                                      getClearCLDeviceByName(lClearCL,
                                                             lMachineConfiguration.getStringProperty("clearcl.device.simulation",
                                                                                                     "HD"));

    ClearCLContext lMicroscopeContext =
                                      getClearCLDeviceByName(lClearCL,
                                                             lMachineConfiguration.getStringProperty("clearcl.device.fusion",
                                                                                                     "HD"));

    LightSheetMicroscopeSimulationDevice lSimulatorDevice =
                                                          SimulationUtils.getSimulatorDevice(lSimulationContext,
                                                                                             lNumberOfDetectionArms,
                                                                                             lNumberOfLightSheets,
                                                                                             lMaxCameraResolution,
                                                                                             lDivisionTime,
                                                                                             lPhantomWidth,
                                                                                             lPhantomHeight,
                                                                                             lPhantomDepth,
                                                                                             lUniformFluorescence);

    SimulatedLightSheetMicroscope lMicroscope =
                                              new SimulatedLightSheetMicroscope("SimulatedLightSheetMicroscope",
                                                                                lMicroscopeContext,
                                                                                lMaxNumberOfStacks,
                                                                                1);

    lMicroscope.addSimulatedDevices(lDummySimulation,
                                    true,
                                    true,
                                    lSimulatorDevice);

    lMicroscope.addStandardDevices();

    if (lMicroscope.open())
      if (lMicroscope.start())
      {

        LightSheetMicroscopeGUI lMicroscopeGUI =
                                               new LightSheetMicroscopeGUI(lMicroscope,
                                                                           l2DDisplayFlag,
                                                                           l3DDisplayFlag);

        lMicroscopeGUI.setup();

        assertTrue(lMicroscopeGUI.open());

        ThreadSleep.sleep(1000, TimeUnit.MILLISECONDS);
        lMicroscopeGUI.waitForVisible(true, 1L, TimeUnit.MINUTES);

        lMicroscopeGUI.connectGUI();

        lMicroscopeGUI.waitForVisible(false, null, null);

        lMicroscopeGUI.disconnectGUI();
        lMicroscopeGUI.close();

        lMicroscope.stop();
        lMicroscope.close();
      }

    lSimulatorDevice.getSimulator().close();

    lClearCL.close();

  }

  protected ClearCLContext getClearCLDeviceByName(ClearCL pClearCL,
                                                  String lDeviceName)
  {
    ClearCLDevice lSimulationGPUDevice =
                                       pClearCL.getFastestGPUDeviceForImages(); // (lDeviceName);
    ClearCLContext lSimulationContext =
                                      lSimulationGPUDevice.createContext();
    return lSimulationContext;
  }

}
