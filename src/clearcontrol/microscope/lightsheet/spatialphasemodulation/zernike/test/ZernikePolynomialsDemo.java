package clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.test;

import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomialsDenseMatrix64F;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.ImageProcessor;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) January 2018
 */
public class ZernikePolynomialsDemo
{

  public static void main(String... args)
  {
    new ImageJ();

    testZernike(0, 0, "").getWindow().setLocation(0, 0);
    testZernike(-1, 1, "").getWindow().setLocation(0, 150);
    testZernike(1, 1, "").getWindow().setLocation(150, 150);
    testZernike(-2, 2, "").getWindow().setLocation(0, 300);
    testZernike(0, 2, "").getWindow().setLocation(150, 300);
    testZernike(2, 2, "").getWindow().setLocation(300, 300);
    testZernike(-3, 3, "").getWindow().setLocation(0, 450);
    testZernike(-1, 3, "").getWindow().setLocation(150, 450);
    testZernike(1, 3, "").getWindow().setLocation(300, 450);
    testZernike(3, 3, "").getWindow().setLocation(450, 450);
    testZernike(-4, 4, "").getWindow().setLocation(0, 600);
    testZernike(-2, 4, "").getWindow().setLocation(150, 600);
    testZernike(0, 4, "").getWindow().setLocation(300, 600);
    testZernike(2, 4, "").getWindow().setLocation(450, 600);
    testZernike(4, 4, "").getWindow().setLocation(600, 600);
  }

  private static ImagePlus testZernike(int m, int n, String path)
  {
    int lWidth = 101;
    int lHeight = 101;

    ZernikePolynomialsDenseMatrix64F lZernikePolynomialsDenseMatrix64F =
                                                     new ZernikePolynomialsDenseMatrix64F(lWidth,
                                                                                 lHeight,
                                                                                 m,
                                                                                 n);

    ImagePlus lImagePlus = NewImage.createByteImage("Z " + m
                                             + " "
                                             + n,
                                             (int) lWidth,
                                             (int) lHeight,
                                             1,
                                             NewImage.FILL_BLACK);
    ImageProcessor lImageProcessor = lImagePlus.getProcessor();

    for (int x = 0; x < lWidth; x++)
    {
      for (int y = 0; y < lHeight; y++)
      {
        lImageProcessor.set((int) x,
               (int) y,
               (int) ((lZernikePolynomialsDenseMatrix64F.get(x, y) + 1)
                      * 127));
      }
    }
    lImagePlus.show();

    return lImagePlus;
  }
}
