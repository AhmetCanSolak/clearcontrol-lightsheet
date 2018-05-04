package clearcontrol.microscope.lightsheet.imaging.interleaved;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.component.scheduler.implementations.MeasureTimeScheduler;
import clearcontrol.microscope.lightsheet.component.scheduler.implementations.PauseUntilTimeAfterMeasuredTimeScheduler;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.OpticsPrefusedAcquisitionScheduler;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.OpticsPrefusedFusionScheduler;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.OpticsPrefusedImageDataContainer;
import clearcontrol.microscope.lightsheet.processor.fusion.FusedImageDataContainer;
import clearcontrol.microscope.lightsheet.processor.fusion.WriteFusedImageAsRawToDiscScheduler;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.schedulers.DropOldestStackInterfaceContainerScheduler;

import java.util.ArrayList;

/**
 * The AppendConsecutiveSequentialImagingScheduler appends a list of imaging, fusion and io schedulers at the current position
 * in the timelapse
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class AppendConsecutiveInterleavedImagingScheduler extends SchedulerBase implements LoggingFeature {
    private final int mNumberOfImages;
    private final double mIntervalInSeconds;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public AppendConsecutiveInterleavedImagingScheduler(int pNumberOfImages, double pIntervalInSeconds) {
        super("Smart: Append an interleaved scan with " + pNumberOfImages + " images every " + pIntervalInSeconds + " s to the schedulers"  );
        mNumberOfImages = pNumberOfImages;
        mIntervalInSeconds = pIntervalInSeconds;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        if (!(mMicroscope instanceof LightSheetMicroscope)) {
            warning("I need a LightSheetMicroscope!");
            return false;
        }

        String timeMeasurementKey = "interleaved_" + System.currentTimeMillis();

        LightSheetTimelapse lTimelapse = ((LightSheetMicroscope) mMicroscope).getTimelapse();
        ArrayList<SchedulerInterface> schedule = lTimelapse.getListOfActivatedSchedulers();

        int index = (int)pTimePoint + 1;
        for (int i = 0; i < mNumberOfImages; i ++) {
            schedule.add(index, new MeasureTimeScheduler(timeMeasurementKey));
            index++;
            schedule.add(index, new InterleavedAcquisitionScheduler());
            index++;
            schedule.add(index, new InterleavedFusionScheduler());
            index++;
            schedule.add(index, new DropOldestStackInterfaceContainerScheduler(InterleavedImageDataContainer.class));
            index++;
            schedule.add(index, new WriteFusedImageAsRawToDiscScheduler("interleaved"));
            index++;
            schedule.add(index, new DropOldestStackInterfaceContainerScheduler(FusedImageDataContainer.class));
            index++;
            schedule.add(index, new PauseUntilTimeAfterMeasuredTimeScheduler(timeMeasurementKey, (long)(mIntervalInSeconds * 1000)));
            index++;
        }
        return true;
    }
}
