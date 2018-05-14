package clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike;

import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

public class ZernikePolynomials
{

  /**
   * Computes the value of the Zernike polynomial Z(i,j) at position (x,y) This
   * is a re-parametrization of the Zernike basis so that it is easier to
   * interpret as a transform.
   * 
   * @param i
   *          non-negative integer: i = (n-m)/2
   * @param j
   *          non-negative integer: j = (n+m)/2
   * @param x
   *          x-coordinate
   * @param y
   *          y-coordinate
   * @return returns value
   */
  public static double computeZijxy(int i, int j, double x, double y)
  {
    final double r2 = (x * x + y * y);
    final double t = atan2(y, x);

    final int n = j + i;
    final int m = j - i;

    return computeZnmr2t(n, m, r2, t);
  }

  /**
   * Computes the value of the Zernike polynomial Z(n,m) at position (x,y)
   * 
   * @param n
   *          non-negative integer
   * @param m
   *          integer
   * @param x
   *          x-coordinate
   * @param y
   *          y-coordinate
   * @return returns value
   */
  public static double computeZnmxy(int n, int m, double x, double y)
  {
    final double r2 = (x * x + y * y);
    final double t = atan2(y, x);
    return computeZnmr2t(n, m, r2, t);
  }

  /**
   * Computes the value of the Zernike polynomial Z(n,m) at position (r,t)
   * (polar coordinates)
   * 
   * @param n
   *          non-negative integer
   * @param m
   *          integer
   * @param r
   *          x-coordinate
   * @param t
   *          y-coordinate
   * @return returns value
   */
  public static double computeZnmrt(int n, int m, double r, double t)
  {
    final double r2 = r * r;
    return computeZnmr2t(n, m, r2, t);
  }

  /**
   * Computes the value of the Zernike polynomial Z(n,m) at position (r,t)
   * (polar coordinates)
   * 
   * @param n
   *          non-negative integer
   * @param m
   *          integer
   * @param r2
   *          squared modulus (ro)
   * @param t
   *          argument (theta)
   * @return returns value
   */
  public static double computeZnmr2t(int n,
                                     int m,
                                     double r2,
                                     double t)
  {
    /*if (r2 > 1.0)
    {
    	return 0;
    }
    else/**/
    {
      final double zr = computeRnmr2(n, m, r2);

      if (m >= 0)
      {
        return zr * cos(m * t);
      }
      else
      {
        return zr * sin(-m * t);
      }
    }
  }

  /**
   * ComputesZernike radial polynomial R(n,m) value for modulus r
   * 
   * @param n
   *          parameter n
   * @param m
   *          parameter m
   * @param r
   *          modulus r
   * @return Zernike radial polynomial R(n,m) value for modulus r
   */
  public static double computeRnmr(final int n,
                                   final int m,
                                   final double r)
  {
    return computeRnmr2(n, m, r * r);
  }

  /**
   * ComputesZernike radial polynomial R(n,m) value for square modulus r^2
   * 
   * @param n
   *          paameter n
   * @param m
   *          parmeter m
   * @param r2
   *          squared modulus r^2
   * @return Zernike radial polynomial R(n,m) value for square modulus r^2
   */
  public static double computeRnmr2(final int n,
                                    final int m,
                                    final double r2)
  {
    int i;
    int k;

    final int lm = abs(m);
    final int ln = n;

    if ((ln - lm) % 2 != 0)
    {
      return 0;
    }

    double result = 0;
    // if (r2 <= 1.0)
    {
      double sign = 1;

      double a = 1; // (n-k)!
      final double b = 1; // k!
      double c = 1; // [(n+|m|)/2-k]!
      double d = 1; // [(n-|m|)/2-k]!

      for (i = 2; i <= ln; i++)
      {
        a *= i;
      }
      for (i = 2; i <= (ln + lm) / 2; i++)
      {
        c *= i;
      }
      for (i = 2; i <= (ln - lm) / 2; i++)
      {
        d *= i;
      }

      double f = 1.0 * a / (b * c * d);

      for (k = 0; k <= (ln - lm) / 2; k++)
      {
        result += sign * f * pow(r2, ln / 2.0 - k);

        if (k < (ln - lm) / 2)
        {
          sign = -sign;

          // For more numerical stability:
          final double da = ln - k;
          final double db = k + 1;
          final double dc = (ln + lm) / 2 - k;
          final double dd = (ln - lm) / 2 - k;
          f *= (dc * dd) / (da * db);

          // a = a / (ln - k);
          // b = b * (k + 1);
          // c = c / ((ln + lm) / 2 - k);
          // d = d / ((ln - lm) / 2 - k);
        }
      }
    }
    return result;
  }

  public static int jANSI(int n, int m) {
    // source: https://en.wikipedia.org/wiki/Zernike_polynomials
    return (n * (n + 2) + m) / 2;
  }

  public static int jNoll(int n, int m) {
    return jNoll(jANSI(n, m));
  }

  // source: https://oeis.org/A176988/b176988.txt
  private static int[] jNollIndices = {
        1,
        3,
        2,
        5,
        4,
        6,
        9,
        7,
        8,
        10,
        15,
        13,
        11,
        12,
        14,
        21,
        19,
        17,
        16,
        18,
        20,
        27,
        25,
        23,
        22,
        24,
        26,
        28,
        35,
        33,
        31,
        29,
        30,
        32,
        34,
        36,
        45,
        43,
        41,
        39,
        37,
        38,
        40,
        42,
        44,
        55,
        53,
        51,
        49,
        47,
        46,
        48,
        50,
        52,
        54,
        65,
        63,
        61,
        59,
        57,
        56,
        58,
        60,
        62,
        64,
        66,
        77,
        75,
        73,
        71,
        69,
        67,
        68,
        70,
        72,
        74,
        76,
        78,
        91,
        89,
        87,
        85,
        83,
        81,
        79,
        80,
        82,
        84,
        86,
        88,
        90,
        105,
        103,
        101,
        99,
        97,
        95,
        93,
        92,
        94,
        96,
        98,
        100,
        102,
        104,
        119,
        117,
        115,
        113,
        111,
        109,
        107,
        106,
        108,
        110,
        112,
        114,
        116,
        118,
        120};

  public static int jNoll(int jANSI) {
    return jNollIndices[jANSI];
  }
}
