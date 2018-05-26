package clearcontrol.microscope.lightsheet.postprocessing.visualisation.schedulers;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstruction;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;
import ij.IJ;
import ij.ImagePlus;

import java.io.File;

/**
 * The thumbnail generator create a thumbnail of the oldest image stack in the warehouse and saves it the current working directory
 *
 *
 * Author: @haesleinhuepf
 * April 2018
 */
public class MaxProjectionInstruction<T extends StackInterfaceContainer> extends LightSheetMicroscopeInstruction implements LoggingFeature {

    private final Class<T> mClass;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public MaxProjectionInstruction(Class<T> pClass, LightSheetMicroscope pLightSheetMicroscope) {
        super("Post-processing: Thumbnail (max projection) generator for " + pClass.getSimpleName(), pLightSheetMicroscope);
        mClass = pClass;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        // Read oldest image from the warehouse
        DataWarehouse lDataWarehouse = getLightSheetMicroscope().getDataWarehouse();

        T lContainer = lDataWarehouse.getOldestContainer(mClass);

        String key = lContainer.keySet().iterator().next();
        StackInterface lStack = lContainer.get(key);

        String targetFolder = getLightSheetMicroscope().getDevice(LightSheetTimelapse.class, 0).getWorkingDirectory().toString();
        long lTimePoint = lContainer.getTimepoint();
        int lDigits = 6;

        // Process the image
        ClearCLIJ clij = ClearCLIJ.getInstance();
        ClearCLImage lCLImage = clij.converter(lStack).getClearCLImage();
        ClearCLImage lCLMaximumProjectionImage = clij.createCLImage(new long[]{lCLImage.getWidth(), lCLImage.getHeight()}, lCLImage.getChannelDataType());

        Kernels.maxProjection(clij, lCLImage, lCLMaximumProjectionImage);

        ImagePlus lImpMaximumProjection = clij.converter(lCLMaximumProjectionImage).getImagePlus();
        lCLImage.close();
        lCLMaximumProjectionImage.close();

        new File(targetFolder + "/stacks/thumbnails_max/").mkdirs();

        IJ.run(lImpMaximumProjection, "Enhance Contrast", "saturated=0.35");
        IJ.saveAsTiff(lImpMaximumProjection, targetFolder + "/stacks/thumbnails_max/" +  String.format("%0" + lDigits + "d", lTimePoint) + ".tif");
        return true;
    }
}
