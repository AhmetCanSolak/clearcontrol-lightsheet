package clearcontrol.microscope.lightsheet.spatialphasemodulation.io;

import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import org.ejml.data.DenseMatrix64F;

import java.io.*;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class DenseMatrix64FWriter
{

  private boolean flipHorizontal = true;
  private boolean flipVertical = false;
  private boolean flipXY = true;

  File mTargetFile;
  DenseMatrix64F mSourceMatrix;
  public DenseMatrix64FWriter(File pTargetFile, DenseMatrix64F pSourceMatrix) {
    mTargetFile = pTargetFile;
    mSourceMatrix = pSourceMatrix;
  }

  public boolean write() {
    Double[][] data = new Double[mSourceMatrix.numRows][mSourceMatrix.numCols];

    DenseMatrix64F lSourceMatrix = mSourceMatrix.copy();
    DenseMatrix64F lTargetMatrix = mSourceMatrix.copy();

    if (flipXY)
    {
      TransformMatrices.flipSquareMatrixXY(lSourceMatrix,
                                           lTargetMatrix);
      lSourceMatrix = lTargetMatrix.copy();
    }
    if (flipVertical)
    {
      TransformMatrices.flipSquareMatrixVertical(lSourceMatrix,
                                                 lTargetMatrix);
      lSourceMatrix = lTargetMatrix.copy();
    }
    if (flipHorizontal)
    {
      TransformMatrices.flipSquareMatrixHorizontal(lSourceMatrix,
                                                   lTargetMatrix);
      lSourceMatrix = lTargetMatrix.copy();
    }

    for (int y = 0; y < mSourceMatrix.numRows; y++) {
      for (int x = 0; x < mSourceMatrix.numCols; x++) {
        data[x][y] = lSourceMatrix.get(x, y);
      }
    }

    ObjectMapper lObjectMapper = new ObjectMapper();
    lObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    lObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            false);

    try
    {
      lObjectMapper.writeValue(mTargetFile, data);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return false;
    }

    return true;
  }
}
