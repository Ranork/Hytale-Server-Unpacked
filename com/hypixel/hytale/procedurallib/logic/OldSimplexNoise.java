package com.hypixel.hytale.procedurallib.logic;

import com.hypixel.hytale.procedurallib.NoiseFunction;
import javax.annotation.Nonnull;

public class OldSimplexNoise implements NoiseFunction {
   public static final OldSimplexNoise INSTANCE = new OldSimplexNoise();
   private static final double STRETCH_CONSTANT_2D = -0.211324865405187;
   private static final double SQUISH_CONSTANT_2D = 0.366025403784439;
   private static final double STRETCH_CONSTANT_3D = -0.16666666666666666;
   private static final double SQUISH_CONSTANT_3D = 0.3333333333333333;
   private static final double NORM_CONSTANT_2D = 47.0;
   private static final double NORM_CONSTANT_3D = 103.0;
   @Nonnull
   private static DoubleArray.Double2[] gradients2D = new DoubleArray.Double2[]{
      new DoubleArray.Double2(5.0, 2.0),
      new DoubleArray.Double2(2.0, 5.0),
      new DoubleArray.Double2(-5.0, 2.0),
      new DoubleArray.Double2(-2.0, 5.0),
      new DoubleArray.Double2(5.0, -2.0),
      new DoubleArray.Double2(2.0, -5.0),
      new DoubleArray.Double2(-5.0, -2.0),
      new DoubleArray.Double2(-2.0, -5.0)
   };
   @Nonnull
   private static DoubleArray.Double3[] gradients3D = new DoubleArray.Double3[]{
      new DoubleArray.Double3(-11.0, 4.0, 4.0),
      new DoubleArray.Double3(-4.0, 11.0, 4.0),
      new DoubleArray.Double3(-4.0, 4.0, 11.0),
      new DoubleArray.Double3(11.0, 4.0, 4.0),
      new DoubleArray.Double3(4.0, 11.0, 4.0),
      new DoubleArray.Double3(4.0, 4.0, 11.0),
      new DoubleArray.Double3(-11.0, -4.0, 4.0),
      new DoubleArray.Double3(-4.0, -11.0, 4.0),
      new DoubleArray.Double3(-4.0, -4.0, 11.0),
      new DoubleArray.Double3(11.0, -4.0, 4.0),
      new DoubleArray.Double3(4.0, -11.0, 4.0),
      new DoubleArray.Double3(4.0, -4.0, 11.0),
      new DoubleArray.Double3(-11.0, 4.0, -4.0),
      new DoubleArray.Double3(-4.0, 11.0, -4.0),
      new DoubleArray.Double3(-4.0, 4.0, -11.0),
      new DoubleArray.Double3(11.0, 4.0, -4.0),
      new DoubleArray.Double3(4.0, 11.0, -4.0),
      new DoubleArray.Double3(4.0, 4.0, -11.0),
      new DoubleArray.Double3(-11.0, -4.0, -4.0),
      new DoubleArray.Double3(-4.0, -11.0, -4.0),
      new DoubleArray.Double3(-4.0, -4.0, -11.0),
      new DoubleArray.Double3(11.0, -4.0, -4.0),
      new DoubleArray.Double3(4.0, -11.0, -4.0),
      new DoubleArray.Double3(4.0, -4.0, -11.0)
   };

   private OldSimplexNoise() {
   }

   @Override
   public double get(int seed, int offsetSeed, double x, double y) {
      double stretchOffset = (x + y) * -0.211324865405187;
      double xs = x + stretchOffset;
      double ys = y + stretchOffset;
      int xsb = fastFloor(xs);
      int ysb = fastFloor(ys);
      double squishOffset = (xsb + ysb) * 0.366025403784439;
      double xb = xsb + squishOffset;
      double yb = ysb + squishOffset;
      double xins = xs - xsb;
      double yins = ys - ysb;
      double inSum = xins + yins;
      double dx0 = x - xb;
      double dy0 = y - yb;
      double value = 0.0;
      double dx1 = dx0 - 1.0 - 0.366025403784439;
      double dy1 = dy0 - 0.0 - 0.366025403784439;
      double attn1 = 2.0 - dx1 * dx1 - dy1 * dy1;
      if (attn1 > 0.0) {
         attn1 *= attn1;
         value += attn1 * attn1 * extrapolate(offsetSeed, xsb + 1, ysb + 0, dx1, dy1);
      }

      double dx2 = dx0 - 0.0 - 0.366025403784439;
      double dy2 = dy0 - 1.0 - 0.366025403784439;
      double attn2 = 2.0 - dx2 * dx2 - dy2 * dy2;
      if (attn2 > 0.0) {
         attn2 *= attn2;
         value += attn2 * attn2 * extrapolate(offsetSeed, xsb + 0, ysb + 1, dx2, dy2);
      }

      double dx_ext;
      double dy_ext;
      int xsv_ext;
      int ysv_ext;
      if (inSum <= 1.0) {
         double zins = 1.0 - inSum;
         if (!(zins > xins) && !(zins > yins)) {
            xsv_ext = xsb + 1;
            ysv_ext = ysb + 1;
            dx_ext = dx0 - 1.0 - 0.732050807568878;
            dy_ext = dy0 - 1.0 - 0.732050807568878;
         } else if (xins > yins) {
            xsv_ext = xsb + 1;
            ysv_ext = ysb - 1;
            dx_ext = dx0 - 1.0;
            dy_ext = dy0 + 1.0;
         } else {
            xsv_ext = xsb - 1;
            ysv_ext = ysb + 1;
            dx_ext = dx0 + 1.0;
            dy_ext = dy0 - 1.0;
         }
      } else {
         double zins = 2.0 - inSum;
         if (!(zins < xins) && !(zins < yins)) {
            dx_ext = dx0;
            dy_ext = dy0;
            xsv_ext = xsb;
            ysv_ext = ysb;
         } else if (xins > yins) {
            xsv_ext = xsb + 2;
            ysv_ext = ysb + 0;
            dx_ext = dx0 - 2.0 - 0.732050807568878;
            dy_ext = dy0 + 0.0 - 0.732050807568878;
         } else {
            xsv_ext = xsb + 0;
            ysv_ext = ysb + 2;
            dx_ext = dx0 + 0.0 - 0.732050807568878;
            dy_ext = dy0 - 2.0 - 0.732050807568878;
         }

         xsb++;
         ysb++;
         dx0 = dx0 - 1.0 - 0.732050807568878;
         dy0 = dy0 - 1.0 - 0.732050807568878;
      }

      double attn0 = 2.0 - dx0 * dx0 - dy0 * dy0;
      if (attn0 > 0.0) {
         attn0 *= attn0;
         value += attn0 * attn0 * extrapolate(offsetSeed, xsb, ysb, dx0, dy0);
      }

      double attn_ext = 2.0 - dx_ext * dx_ext - dy_ext * dy_ext;
      if (attn_ext > 0.0) {
         attn_ext *= attn_ext;
         value += attn_ext * attn_ext * extrapolate(offsetSeed, xsv_ext, ysv_ext, dx_ext, dy_ext);
      }

      return value / 47.0;
   }

   @Override
   public double get(int seed, int offsetSeed, double x, double y, double z) {
      double stretchOffset = (x + y + z) * -0.16666666666666666;
      double xs = x + stretchOffset;
      double ys = y + stretchOffset;
      double zs = z + stretchOffset;
      int xsb = fastFloor(xs);
      int ysb = fastFloor(ys);
      int zsb = fastFloor(zs);
      double squishOffset = (xsb + ysb + zsb) * 0.3333333333333333;
      double xb = xsb + squishOffset;
      double yb = ysb + squishOffset;
      double zb = zsb + squishOffset;
      double xins = xs - xsb;
      double yins = ys - ysb;
      double zins = zs - zsb;
      double inSum = xins + yins + zins;
      double dx0 = x - xb;
      double dy0 = y - yb;
      double dz0 = z - zb;
      double value = 0.0;
      double dx_ext0;
      double dy_ext0;
      double dz_ext0;
      double dx_ext1;
      double dy_ext1;
      double dz_ext1;
      int xsv_ext0;
      int ysv_ext0;
      int zsv_ext0;
      int xsv_ext1;
      int ysv_ext1;
      int zsv_ext1;
      if (inSum <= 1.0) {
         byte aPoint = 1;
         double aScore = xins;
         byte bPoint = 2;
         double bScore = yins;
         if (xins >= yins && zins > yins) {
            bScore = zins;
            bPoint = 4;
         } else if (xins < yins && zins > xins) {
            aScore = zins;
            aPoint = 4;
         }

         double wins = 1.0 - inSum;
         if (!(wins > aScore) && !(wins > bScore)) {
            byte c = (byte)(aPoint | bPoint);
            if ((c & 1) == 0) {
               xsv_ext0 = xsb;
               xsv_ext1 = xsb - 1;
               dx_ext0 = dx0 - 0.6666666666666666;
               dx_ext1 = dx0 + 1.0 - 0.3333333333333333;
            } else {
               xsv_ext0 = xsv_ext1 = xsb + 1;
               dx_ext0 = dx0 - 1.0 - 0.6666666666666666;
               dx_ext1 = dx0 - 1.0 - 0.3333333333333333;
            }

            if ((c & 2) == 0) {
               ysv_ext0 = ysb;
               ysv_ext1 = ysb - 1;
               dy_ext0 = dy0 - 0.6666666666666666;
               dy_ext1 = dy0 + 1.0 - 0.3333333333333333;
            } else {
               ysv_ext0 = ysv_ext1 = ysb + 1;
               dy_ext0 = dy0 - 1.0 - 0.6666666666666666;
               dy_ext1 = dy0 - 1.0 - 0.3333333333333333;
            }

            if ((c & 4) == 0) {
               zsv_ext0 = zsb;
               zsv_ext1 = zsb - 1;
               dz_ext0 = dz0 - 0.6666666666666666;
               dz_ext1 = dz0 + 1.0 - 0.3333333333333333;
            } else {
               zsv_ext0 = zsv_ext1 = zsb + 1;
               dz_ext0 = dz0 - 1.0 - 0.6666666666666666;
               dz_ext1 = dz0 - 1.0 - 0.3333333333333333;
            }
         } else {
            byte cx = bScore > aScore ? bPoint : aPoint;
            if ((cx & 1) == 0) {
               xsv_ext0 = xsb - 1;
               xsv_ext1 = xsb;
               dx_ext0 = dx0 + 1.0;
               dx_ext1 = dx0;
            } else {
               xsv_ext0 = xsv_ext1 = xsb + 1;
               dx_ext0 = dx_ext1 = dx0 - 1.0;
            }

            if ((cx & 2) == 0) {
               ysv_ext1 = ysb;
               ysv_ext0 = ysb;
               dy_ext1 = dy0;
               dy_ext0 = dy0;
               if ((cx & 1) == 0) {
                  ysv_ext1 = ysb - 1;
                  dy_ext1 = dy0 + 1.0;
               } else {
                  ysv_ext0 = ysb - 1;
                  dy_ext0 = dy0 + 1.0;
               }
            } else {
               ysv_ext0 = ysv_ext1 = ysb + 1;
               dy_ext0 = dy_ext1 = dy0 - 1.0;
            }

            if ((cx & 4) == 0) {
               zsv_ext0 = zsb;
               zsv_ext1 = zsb - 1;
               dz_ext0 = dz0;
               dz_ext1 = dz0 + 1.0;
            } else {
               zsv_ext0 = zsv_ext1 = zsb + 1;
               dz_ext0 = dz_ext1 = dz0 - 1.0;
            }
         }

         double attn0 = 2.0 - dx0 * dx0 - dy0 * dy0 - dz0 * dz0;
         if (attn0 > 0.0) {
            attn0 *= attn0;
            value += attn0 * attn0 * extrapolate(offsetSeed, xsb + 0, ysb + 0, zsb + 0, dx0, dy0, dz0);
         }

         double dx1 = dx0 - 1.0 - 0.3333333333333333;
         double dy1 = dy0 - 0.0 - 0.3333333333333333;
         double dz1 = dz0 - 0.0 - 0.3333333333333333;
         double attn1 = 2.0 - dx1 * dx1 - dy1 * dy1 - dz1 * dz1;
         if (attn1 > 0.0) {
            attn1 *= attn1;
            value += attn1 * attn1 * extrapolate(offsetSeed, xsb + 1, ysb + 0, zsb + 0, dx1, dy1, dz1);
         }

         double dx2 = dx0 - 0.0 - 0.3333333333333333;
         double dy2 = dy0 - 1.0 - 0.3333333333333333;
         double attn2 = 2.0 - dx2 * dx2 - dy2 * dy2 - dz1 * dz1;
         if (attn2 > 0.0) {
            attn2 *= attn2;
            value += attn2 * attn2 * extrapolate(offsetSeed, xsb + 0, ysb + 1, zsb + 0, dx2, dy2, dz1);
         }

         double dz3 = dz0 - 1.0 - 0.3333333333333333;
         double attn3 = 2.0 - dx2 * dx2 - dy1 * dy1 - dz3 * dz3;
         if (attn3 > 0.0) {
            attn3 *= attn3;
            value += attn3 * attn3 * extrapolate(offsetSeed, xsb + 0, ysb + 0, zsb + 1, dx2, dy1, dz3);
         }
      } else if (inSum >= 2.0) {
         byte aPointx = 6;
         double aScorex = xins;
         byte bPointx = 5;
         double bScorex = yins;
         if (xins <= yins && zins < yins) {
            bScorex = zins;
            bPointx = 3;
         } else if (xins > yins && zins < xins) {
            aScorex = zins;
            aPointx = 3;
         }

         double winsx = 3.0 - inSum;
         if (!(winsx < aScorex) && !(winsx < bScorex)) {
            byte cxx = (byte)(aPointx & bPointx);
            if ((cxx & 1) != 0) {
               xsv_ext0 = xsb + 1;
               xsv_ext1 = xsb + 2;
               dx_ext0 = dx0 - 1.0 - 0.3333333333333333;
               dx_ext1 = dx0 - 2.0 - 0.6666666666666666;
            } else {
               xsv_ext1 = xsb;
               xsv_ext0 = xsb;
               dx_ext0 = dx0 - 0.3333333333333333;
               dx_ext1 = dx0 - 0.6666666666666666;
            }

            if ((cxx & 2) != 0) {
               ysv_ext0 = ysb + 1;
               ysv_ext1 = ysb + 2;
               dy_ext0 = dy0 - 1.0 - 0.3333333333333333;
               dy_ext1 = dy0 - 2.0 - 0.6666666666666666;
            } else {
               ysv_ext1 = ysb;
               ysv_ext0 = ysb;
               dy_ext0 = dy0 - 0.3333333333333333;
               dy_ext1 = dy0 - 0.6666666666666666;
            }

            if ((cxx & 4) != 0) {
               zsv_ext0 = zsb + 1;
               zsv_ext1 = zsb + 2;
               dz_ext0 = dz0 - 1.0 - 0.3333333333333333;
               dz_ext1 = dz0 - 2.0 - 0.6666666666666666;
            } else {
               zsv_ext1 = zsb;
               zsv_ext0 = zsb;
               dz_ext0 = dz0 - 0.3333333333333333;
               dz_ext1 = dz0 - 0.6666666666666666;
            }
         } else {
            byte cxxx = bScorex < aScorex ? bPointx : aPointx;
            if ((cxxx & 1) != 0) {
               xsv_ext0 = xsb + 2;
               xsv_ext1 = xsb + 1;
               dx_ext0 = dx0 - 2.0 - 1.0;
               dx_ext1 = dx0 - 1.0 - 1.0;
            } else {
               xsv_ext1 = xsb;
               xsv_ext0 = xsb;
               dx_ext0 = dx_ext1 = dx0 - 1.0;
            }

            if ((cxxx & 2) != 0) {
               ysv_ext0 = ysv_ext1 = ysb + 1;
               dy_ext0 = dy_ext1 = dy0 - 1.0 - 1.0;
               if ((cxxx & 1) != 0) {
                  ysv_ext1++;
                  dy_ext1--;
               } else {
                  ysv_ext0++;
                  dy_ext0--;
               }
            } else {
               ysv_ext1 = ysb;
               ysv_ext0 = ysb;
               dy_ext0 = dy_ext1 = dy0 - 1.0;
            }

            if ((cxxx & 4) != 0) {
               zsv_ext0 = zsb + 1;
               zsv_ext1 = zsb + 2;
               dz_ext0 = dz0 - 1.0 - 1.0;
               dz_ext1 = dz0 - 2.0 - 1.0;
            } else {
               zsv_ext1 = zsb;
               zsv_ext0 = zsb;
               dz_ext0 = dz_ext1 = dz0 - 1.0;
            }
         }

         double dx3 = dx0 - 1.0 - 0.6666666666666666;
         double dy3 = dy0 - 1.0 - 0.6666666666666666;
         double dz3 = dz0 - 0.0 - 0.6666666666666666;
         double attn3 = 2.0 - dx3 * dx3 - dy3 * dy3 - dz3 * dz3;
         if (attn3 > 0.0) {
            attn3 *= attn3;
            value += attn3 * attn3 * extrapolate(offsetSeed, xsb + 1, ysb + 1, zsb + 0, dx3, dy3, dz3);
         }

         double dy2x = dy0 - 0.0 - 0.6666666666666666;
         double dz2 = dz0 - 1.0 - 0.6666666666666666;
         double attn2x = 2.0 - dx3 * dx3 - dy2x * dy2x - dz2 * dz2;
         if (attn2x > 0.0) {
            attn2x *= attn2x;
            value += attn2x * attn2x * extrapolate(offsetSeed, xsb + 1, ysb + 0, zsb + 1, dx3, dy2x, dz2);
         }

         double dx1x = dx0 - 0.0 - 0.6666666666666666;
         double attn1x = 2.0 - dx1x * dx1x - dy3 * dy3 - dz2 * dz2;
         if (attn1x > 0.0) {
            attn1x *= attn1x;
            value += attn1x * attn1x * extrapolate(offsetSeed, xsb + 0, ysb + 1, zsb + 1, dx1x, dy3, dz2);
         }

         dx0 = dx0 - 1.0 - 1.0;
         dy0 = dy0 - 1.0 - 1.0;
         dz0 = dz0 - 1.0 - 1.0;
         double attn0x = 2.0 - dx0 * dx0 - dy0 * dy0 - dz0 * dz0;
         if (attn0x > 0.0) {
            attn0x *= attn0x;
            value += attn0x * attn0x * extrapolate(offsetSeed, xsb + 1, ysb + 1, zsb + 1, dx0, dy0, dz0);
         }
      } else {
         double p1 = xins + yins;
         byte aPointxx;
         double aScorexx;
         boolean aIsFurtherSide;
         if (p1 > 1.0) {
            aScorexx = p1 - 1.0;
            aPointxx = 3;
            aIsFurtherSide = true;
         } else {
            aScorexx = 1.0 - p1;
            aPointxx = 4;
            aIsFurtherSide = false;
         }

         double p2 = xins + zins;
         boolean bIsFurtherSide;
         double bScorexx;
         byte bPointxx;
         if (p2 > 1.0) {
            bScorexx = p2 - 1.0;
            bPointxx = 5;
            bIsFurtherSide = true;
         } else {
            bScorexx = 1.0 - p2;
            bPointxx = 2;
            bIsFurtherSide = false;
         }

         double p3 = yins + zins;
         if (p3 > 1.0) {
            double score = p3 - 1.0;
            if (aScorexx <= bScorexx && aScorexx < score) {
               aPointxx = 6;
               aIsFurtherSide = true;
            } else if (aScorexx > bScorexx && bScorexx < score) {
               bPointxx = 6;
               bIsFurtherSide = true;
            }
         } else {
            double score = 1.0 - p3;
            if (aScorexx <= bScorexx && aScorexx < score) {
               aPointxx = 1;
               aIsFurtherSide = false;
            } else if (aScorexx > bScorexx && bScorexx < score) {
               bPointxx = 1;
               bIsFurtherSide = false;
            }
         }

         if (aIsFurtherSide == bIsFurtherSide) {
            if (aIsFurtherSide) {
               dx_ext0 = dx0 - 1.0 - 1.0;
               dy_ext0 = dy0 - 1.0 - 1.0;
               dz_ext0 = dz0 - 1.0 - 1.0;
               xsv_ext0 = xsb + 1;
               ysv_ext0 = ysb + 1;
               zsv_ext0 = zsb + 1;
               byte cxxxx = (byte)(aPointxx & bPointxx);
               if ((cxxxx & 1) != 0) {
                  dx_ext1 = dx0 - 2.0 - 0.6666666666666666;
                  dy_ext1 = dy0 - 0.6666666666666666;
                  dz_ext1 = dz0 - 0.6666666666666666;
                  xsv_ext1 = xsb + 2;
                  ysv_ext1 = ysb;
                  zsv_ext1 = zsb;
               } else if ((cxxxx & 2) != 0) {
                  dx_ext1 = dx0 - 0.6666666666666666;
                  dy_ext1 = dy0 - 2.0 - 0.6666666666666666;
                  dz_ext1 = dz0 - 0.6666666666666666;
                  xsv_ext1 = xsb;
                  ysv_ext1 = ysb + 2;
                  zsv_ext1 = zsb;
               } else {
                  dx_ext1 = dx0 - 0.6666666666666666;
                  dy_ext1 = dy0 - 0.6666666666666666;
                  dz_ext1 = dz0 - 2.0 - 0.6666666666666666;
                  xsv_ext1 = xsb;
                  ysv_ext1 = ysb;
                  zsv_ext1 = zsb + 2;
               }
            } else {
               dx_ext0 = dx0;
               dy_ext0 = dy0;
               dz_ext0 = dz0;
               xsv_ext0 = xsb;
               ysv_ext0 = ysb;
               zsv_ext0 = zsb;
               byte cxxxx = (byte)(aPointxx | bPointxx);
               if ((cxxxx & 1) == 0) {
                  dx_ext1 = dx0 + 1.0 - 0.3333333333333333;
                  dy_ext1 = dy0 - 1.0 - 0.3333333333333333;
                  dz_ext1 = dz0 - 1.0 - 0.3333333333333333;
                  xsv_ext1 = xsb - 1;
                  ysv_ext1 = ysb + 1;
                  zsv_ext1 = zsb + 1;
               } else if ((cxxxx & 2) == 0) {
                  dx_ext1 = dx0 - 1.0 - 0.3333333333333333;
                  dy_ext1 = dy0 + 1.0 - 0.3333333333333333;
                  dz_ext1 = dz0 - 1.0 - 0.3333333333333333;
                  xsv_ext1 = xsb + 1;
                  ysv_ext1 = ysb - 1;
                  zsv_ext1 = zsb + 1;
               } else {
                  dx_ext1 = dx0 - 1.0 - 0.3333333333333333;
                  dy_ext1 = dy0 - 1.0 - 0.3333333333333333;
                  dz_ext1 = dz0 + 1.0 - 0.3333333333333333;
                  xsv_ext1 = xsb + 1;
                  ysv_ext1 = ysb + 1;
                  zsv_ext1 = zsb - 1;
               }
            }
         } else {
            byte c2;
            byte c1;
            if (aIsFurtherSide) {
               c1 = aPointxx;
               c2 = bPointxx;
            } else {
               c1 = bPointxx;
               c2 = aPointxx;
            }

            if ((c1 & 1) == 0) {
               dx_ext0 = dx0 + 1.0 - 0.3333333333333333;
               dy_ext0 = dy0 - 1.0 - 0.3333333333333333;
               dz_ext0 = dz0 - 1.0 - 0.3333333333333333;
               xsv_ext0 = xsb - 1;
               ysv_ext0 = ysb + 1;
               zsv_ext0 = zsb + 1;
            } else if ((c1 & 2) == 0) {
               dx_ext0 = dx0 - 1.0 - 0.3333333333333333;
               dy_ext0 = dy0 + 1.0 - 0.3333333333333333;
               dz_ext0 = dz0 - 1.0 - 0.3333333333333333;
               xsv_ext0 = xsb + 1;
               ysv_ext0 = ysb - 1;
               zsv_ext0 = zsb + 1;
            } else {
               dx_ext0 = dx0 - 1.0 - 0.3333333333333333;
               dy_ext0 = dy0 - 1.0 - 0.3333333333333333;
               dz_ext0 = dz0 + 1.0 - 0.3333333333333333;
               xsv_ext0 = xsb + 1;
               ysv_ext0 = ysb + 1;
               zsv_ext0 = zsb - 1;
            }

            dx_ext1 = dx0 - 0.6666666666666666;
            dy_ext1 = dy0 - 0.6666666666666666;
            dz_ext1 = dz0 - 0.6666666666666666;
            xsv_ext1 = xsb;
            ysv_ext1 = ysb;
            zsv_ext1 = zsb;
            if ((c2 & 1) != 0) {
               dx_ext1 -= 2.0;
               xsv_ext1 = xsb + 2;
            } else if ((c2 & 2) != 0) {
               dy_ext1 -= 2.0;
               ysv_ext1 = ysb + 2;
            } else {
               dz_ext1 -= 2.0;
               zsv_ext1 = zsb + 2;
            }
         }

         double dx1xx = dx0 - 1.0 - 0.3333333333333333;
         double dy1x = dy0 - 0.0 - 0.3333333333333333;
         double dz1x = dz0 - 0.0 - 0.3333333333333333;
         double attn1xx = 2.0 - dx1xx * dx1xx - dy1x * dy1x - dz1x * dz1x;
         if (attn1xx > 0.0) {
            attn1xx *= attn1xx;
            value += attn1xx * attn1xx * extrapolate(offsetSeed, xsb + 1, ysb + 0, zsb + 0, dx1xx, dy1x, dz1x);
         }

         double dx2x = dx0 - 0.0 - 0.3333333333333333;
         double dy2xx = dy0 - 1.0 - 0.3333333333333333;
         double attn2xx = 2.0 - dx2x * dx2x - dy2xx * dy2xx - dz1x * dz1x;
         if (attn2xx > 0.0) {
            attn2xx *= attn2xx;
            value += attn2xx * attn2xx * extrapolate(offsetSeed, xsb + 0, ysb + 1, zsb + 0, dx2x, dy2xx, dz1x);
         }

         double dz3x = dz0 - 1.0 - 0.3333333333333333;
         double attn3x = 2.0 - dx2x * dx2x - dy1x * dy1x - dz3x * dz3x;
         if (attn3x > 0.0) {
            attn3x *= attn3x;
            value += attn3x * attn3x * extrapolate(offsetSeed, xsb + 0, ysb + 0, zsb + 1, dx2x, dy1x, dz3x);
         }

         double dx4 = dx0 - 1.0 - 0.6666666666666666;
         double dy4 = dy0 - 1.0 - 0.6666666666666666;
         double dz4 = dz0 - 0.0 - 0.6666666666666666;
         double attn4 = 2.0 - dx4 * dx4 - dy4 * dy4 - dz4 * dz4;
         if (attn4 > 0.0) {
            attn4 *= attn4;
            value += attn4 * attn4 * extrapolate(offsetSeed, xsb + 1, ysb + 1, zsb + 0, dx4, dy4, dz4);
         }

         double dy5 = dy0 - 0.0 - 0.6666666666666666;
         double dz5 = dz0 - 1.0 - 0.6666666666666666;
         double attn5 = 2.0 - dx4 * dx4 - dy5 * dy5 - dz5 * dz5;
         if (attn5 > 0.0) {
            attn5 *= attn5;
            value += attn5 * attn5 * extrapolate(offsetSeed, xsb + 1, ysb + 0, zsb + 1, dx4, dy5, dz5);
         }

         double dx6 = dx0 - 0.0 - 0.6666666666666666;
         double attn6 = 2.0 - dx6 * dx6 - dy4 * dy4 - dz5 * dz5;
         if (attn6 > 0.0) {
            attn6 *= attn6;
            value += attn6 * attn6 * extrapolate(offsetSeed, xsb + 0, ysb + 1, zsb + 1, dx6, dy4, dz5);
         }
      }

      double attn_ext0 = 2.0 - dx_ext0 * dx_ext0 - dy_ext0 * dy_ext0 - dz_ext0 * dz_ext0;
      if (attn_ext0 > 0.0) {
         attn_ext0 *= attn_ext0;
         value += attn_ext0 * attn_ext0 * extrapolate(offsetSeed, xsv_ext0, ysv_ext0, zsv_ext0, dx_ext0, dy_ext0, dz_ext0);
      }

      double attn_ext1 = 2.0 - dx_ext1 * dx_ext1 - dy_ext1 * dy_ext1 - dz_ext1 * dz_ext1;
      if (attn_ext1 > 0.0) {
         attn_ext1 *= attn_ext1;
         value += attn_ext1 * attn_ext1 * extrapolate(offsetSeed, xsv_ext1, ysv_ext1, zsv_ext1, dx_ext1, dy_ext1, dz_ext1);
      }

      return value / 103.0;
   }

   @Nonnull
   @Override
   public String toString() {
      return "OldSimplexNoise{}";
   }

   private static double extrapolate(int seed, int x, int y, double xd, double yd) {
      int hash = seed ^ 1619 * x;
      hash ^= 31337 * y;
      hash = hash * hash * hash * 60493;
      hash = hash >> 13 ^ hash;
      DoubleArray.Double2 g = gradients2D[hash & 7];
      return xd * g.x + yd * g.y;
   }

   private static double extrapolate(int seed, int x, int y, int z, double xd, double yd, double zd) {
      int hash = seed ^ 1619 * x;
      hash ^= 31337 * y;
      hash ^= 6971 * z;
      hash = hash * hash * hash * 60493;
      hash = hash >> 13 ^ hash;
      DoubleArray.Double3 g = gradients3D[hash % gradients3D.length];
      return xd * g.x + yd * g.y + zd * g.z;
   }

   private static int fastFloor(double x) {
      int xi = (int)x;
      return x < xi ? xi - 1 : xi;
   }
}
