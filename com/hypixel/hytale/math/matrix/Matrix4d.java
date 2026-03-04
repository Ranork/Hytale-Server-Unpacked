package com.hypixel.hytale.math.matrix;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector4d;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class Matrix4d {
   public static final int M00 = 0;
   public static final int M10 = 4;
   public static final int M20 = 8;
   public static final int M30 = 12;
   public static final int M01 = 1;
   public static final int M11 = 5;
   public static final int M21 = 9;
   public static final int M31 = 13;
   public static final int M02 = 2;
   public static final int M12 = 6;
   public static final int M22 = 10;
   public static final int M32 = 14;
   public static final int M03 = 3;
   public static final int M13 = 7;
   public static final int M23 = 11;
   public static final int M33 = 15;
   public static final int COLUMNS = 4;
   public static final int ROWS = 4;
   public static final int FIELDS = 16;
   private final double[] m;

   public Matrix4d() {
      this(new double[16]);
   }

   public Matrix4d(@Nonnull Matrix4d other) {
      this();
      this.assign(other);
   }

   public Matrix4d(double[] m) {
      this.m = m;
   }

   public double get(int idx) {
      return this.m[idx];
   }

   public double get(int col, int row) {
      return this.get(idx(col, row));
   }

   @Nonnull
   public Matrix4d set(int idx, double val) {
      this.m[idx] = val;
      return this;
   }

   @Nonnull
   public Matrix4d set(int col, int row, double val) {
      return this.set(idx(col, row), val);
   }

   @Nonnull
   public Matrix4d add(int idx, double val) {
      this.m[idx] = this.m[idx] + val;
      return this;
   }

   @Nonnull
   public Matrix4d add(int col, int row, double val) {
      return this.set(idx(col, row), val);
   }

   @Nonnull
   public Matrix4d identity() {
      Arrays.fill(this.m, 0.0);

      for (int i = 0; i < 16; i += 5) {
         this.m[i] = 1.0;
      }

      return this;
   }

   @Nonnull
   public Matrix4d assign(@Nonnull Matrix4d other) {
      System.arraycopy(other.m, 0, this.m, 0, 16);
      return this;
   }

   @Nonnull
   public Matrix4d assign(
      double m00,
      double m10,
      double m20,
      double m30,
      double m01,
      double m11,
      double m21,
      double m31,
      double m02,
      double m12,
      double m22,
      double m32,
      double m03,
      double m13,
      double m23,
      double m33
   ) {
      this.m[0] = m00;
      this.m[1] = m01;
      this.m[2] = m02;
      this.m[3] = m03;
      this.m[4] = m10;
      this.m[5] = m11;
      this.m[6] = m12;
      this.m[7] = m13;
      this.m[8] = m20;
      this.m[9] = m21;
      this.m[10] = m22;
      this.m[11] = m23;
      this.m[12] = m30;
      this.m[13] = m31;
      this.m[14] = m32;
      this.m[15] = m33;
      return this;
   }

   @Nonnull
   public Matrix4d translate(@Nonnull Vector3d vec) {
      return this.translate(vec.x, vec.y, vec.z);
   }

   @Nonnull
   public Matrix4d translate(double x, double y, double z) {
      for (int i = 0; i < 4; i++) {
         this.m[i + 12] = this.m[i + 12] + (this.m[i] * x + this.m[i + 4] * y + this.m[i + 8] * z);
      }

      return this;
   }

   @Nonnull
   public Matrix4d scale(double x, double y, double z) {
      for (int i = 0; i < 4; i++) {
         this.m[i] = this.m[i] * x;
         this.m[i + 4] = this.m[i + 4] * y;
         this.m[i + 8] = this.m[i + 8] * z;
      }

      return this;
   }

   @Nonnull
   public Vector3d multiplyPosition(@Nonnull Vector3d vec) {
      return this.multiplyPosition(vec, vec);
   }

   @Nonnull
   public Vector3d multiplyPosition(@Nonnull Vector3d vec, @Nonnull Vector3d result) {
      double x = this.m[0] * vec.x + this.m[4] * vec.y + this.m[8] * vec.z + this.m[12];
      double y = this.m[1] * vec.x + this.m[5] * vec.y + this.m[9] * vec.z + this.m[13];
      double z = this.m[2] * vec.x + this.m[6] * vec.y + this.m[10] * vec.z + this.m[14];
      double w = this.m[3] * vec.x + this.m[7] * vec.y + this.m[11] * vec.z + this.m[15];
      double invW = 1.0 / w;
      result.assign(x * invW, y * invW, z * invW);
      return result;
   }

   @Nonnull
   public Vector3d multiplyDirection(@Nonnull Vector3d vec) {
      double x = this.m[0] * vec.x + this.m[4] * vec.y + this.m[8] * vec.z;
      double y = this.m[1] * vec.x + this.m[5] * vec.y + this.m[9] * vec.z;
      double z = this.m[2] * vec.x + this.m[6] * vec.y + this.m[10] * vec.z;
      vec.assign(x, y, z);
      return vec;
   }

   @Nonnull
   public Vector4d multiply(@Nonnull Vector4d vec) {
      return this.multiply(vec, vec);
   }

   @Nonnull
   public Vector4d multiply(@Nonnull Vector4d vec, @Nonnull Vector4d result) {
      double x = this.m[0] * vec.x + this.m[4] * vec.y + this.m[8] * vec.z + this.m[12] * vec.w;
      double y = this.m[1] * vec.x + this.m[5] * vec.y + this.m[9] * vec.z + this.m[13] * vec.w;
      double z = this.m[2] * vec.x + this.m[6] * vec.y + this.m[10] * vec.z + this.m[14] * vec.w;
      double w = this.m[3] * vec.x + this.m[7] * vec.y + this.m[11] * vec.z + this.m[15] * vec.w;
      result.assign(x, y, z, w);
      return result;
   }

   @Nonnull
   public Matrix4d multiply(@Nonnull Matrix4d other) {
      double a00 = this.m[0];
      double a01 = this.m[1];
      double a02 = this.m[2];
      double a03 = this.m[3];
      double a10 = this.m[4];
      double a11 = this.m[5];
      double a12 = this.m[6];
      double a13 = this.m[7];
      double a20 = this.m[8];
      double a21 = this.m[9];
      double a22 = this.m[10];
      double a23 = this.m[11];
      double a30 = this.m[12];
      double a31 = this.m[13];
      double a32 = this.m[14];
      double a33 = this.m[15];
      double b00 = other.m[0];
      double b01 = other.m[1];
      double b02 = other.m[2];
      double b03 = other.m[3];
      double b10 = other.m[4];
      double b11 = other.m[5];
      double b12 = other.m[6];
      double b13 = other.m[7];
      double b20 = other.m[8];
      double b21 = other.m[9];
      double b22 = other.m[10];
      double b23 = other.m[11];
      double b30 = other.m[12];
      double b31 = other.m[13];
      double b32 = other.m[14];
      double b33 = other.m[15];
      this.m[0] = a00 * b00 + a10 * b01 + a20 * b02 + a30 * b03;
      this.m[1] = a01 * b00 + a11 * b01 + a21 * b02 + a31 * b03;
      this.m[2] = a02 * b00 + a12 * b01 + a22 * b02 + a32 * b03;
      this.m[3] = a03 * b00 + a13 * b01 + a23 * b02 + a33 * b03;
      this.m[4] = a00 * b10 + a10 * b11 + a20 * b12 + a30 * b13;
      this.m[5] = a01 * b10 + a11 * b11 + a21 * b12 + a31 * b13;
      this.m[6] = a02 * b10 + a12 * b11 + a22 * b12 + a32 * b13;
      this.m[7] = a03 * b10 + a13 * b11 + a23 * b12 + a33 * b13;
      this.m[8] = a00 * b20 + a10 * b21 + a20 * b22 + a30 * b23;
      this.m[9] = a01 * b20 + a11 * b21 + a21 * b22 + a31 * b23;
      this.m[10] = a02 * b20 + a12 * b21 + a22 * b22 + a32 * b23;
      this.m[11] = a03 * b20 + a13 * b21 + a23 * b22 + a33 * b23;
      this.m[12] = a00 * b30 + a10 * b31 + a20 * b32 + a30 * b33;
      this.m[13] = a01 * b30 + a11 * b31 + a21 * b32 + a31 * b33;
      this.m[14] = a02 * b30 + a12 * b31 + a22 * b32 + a32 * b33;
      this.m[15] = a03 * b30 + a13 * b31 + a23 * b32 + a33 * b33;
      return this;
   }

   public boolean invert() {
      double src0 = this.m[0];
      double src4 = this.m[1];
      double src8 = this.m[2];
      double src12 = this.m[3];
      double src1 = this.m[4];
      double src5 = this.m[5];
      double src9 = this.m[6];
      double src13 = this.m[7];
      double src2 = this.m[8];
      double src6 = this.m[9];
      double src10 = this.m[10];
      double src14 = this.m[11];
      double src3 = this.m[12];
      double src7 = this.m[13];
      double src11 = this.m[14];
      double src15 = this.m[15];
      double atmp0 = src10 * src15;
      double atmp1 = src11 * src14;
      double atmp2 = src9 * src15;
      double atmp3 = src11 * src13;
      double atmp4 = src9 * src14;
      double atmp5 = src10 * src13;
      double atmp6 = src8 * src15;
      double atmp7 = src11 * src12;
      double atmp8 = src8 * src14;
      double atmp9 = src10 * src12;
      double atmp10 = src8 * src13;
      double atmp11 = src9 * src12;
      double dst0 = atmp0 * src5 + atmp3 * src6 + atmp4 * src7 - (atmp1 * src5 + atmp2 * src6 + atmp5 * src7);
      double dst1 = atmp1 * src4 + atmp6 * src6 + atmp9 * src7 - (atmp0 * src4 + atmp7 * src6 + atmp8 * src7);
      double dst2 = atmp2 * src4 + atmp7 * src5 + atmp10 * src7 - (atmp3 * src4 + atmp6 * src5 + atmp11 * src7);
      double dst3 = atmp5 * src4 + atmp8 * src5 + atmp11 * src6 - (atmp4 * src4 + atmp9 * src5 + atmp10 * src6);
      double dst4 = atmp1 * src1 + atmp2 * src2 + atmp5 * src3 - (atmp0 * src1 + atmp3 * src2 + atmp4 * src3);
      double dst5 = atmp0 * src0 + atmp7 * src2 + atmp8 * src3 - (atmp1 * src0 + atmp6 * src2 + atmp9 * src3);
      double dst6 = atmp3 * src0 + atmp6 * src1 + atmp11 * src3 - (atmp2 * src0 + atmp7 * src1 + atmp10 * src3);
      double dst7 = atmp4 * src0 + atmp9 * src1 + atmp10 * src2 - (atmp5 * src0 + atmp8 * src1 + atmp11 * src2);
      double btmp0 = src2 * src7;
      double btmp1 = src3 * src6;
      double btmp2 = src1 * src7;
      double btmp3 = src3 * src5;
      double btmp4 = src1 * src6;
      double btmp5 = src2 * src5;
      double btmp6 = src0 * src7;
      double btmp7 = src3 * src4;
      double btmp8 = src0 * src6;
      double btmp9 = src2 * src4;
      double btmp10 = src0 * src5;
      double btmp11 = src1 * src4;
      double dst8 = btmp0 * src13 + btmp3 * src14 + btmp4 * src15 - (btmp1 * src13 + btmp2 * src14 + btmp5 * src15);
      double dst9 = btmp1 * src12 + btmp6 * src14 + btmp9 * src15 - (btmp0 * src12 + btmp7 * src14 + btmp8 * src15);
      double dst10 = btmp2 * src12 + btmp7 * src13 + btmp10 * src15 - (btmp3 * src12 + btmp6 * src13 + btmp11 * src15);
      double dst11 = btmp5 * src12 + btmp8 * src13 + btmp11 * src14 - (btmp4 * src12 + btmp9 * src13 + btmp10 * src14);
      double dst12 = btmp2 * src10 + btmp5 * src11 + btmp1 * src9 - (btmp4 * src11 + btmp0 * src9 + btmp3 * src10);
      double dst13 = btmp8 * src11 + btmp0 * src8 + btmp7 * src10 - (btmp6 * src10 + btmp9 * src11 + btmp1 * src8);
      double dst14 = btmp6 * src9 + btmp11 * src11 + btmp3 * src8 - (btmp10 * src11 + btmp2 * src8 + btmp7 * src9);
      double dst15 = btmp10 * src10 + btmp4 * src8 + btmp9 * src9 - (btmp8 * src9 + btmp11 * src10 + btmp5 * src8);
      double det = src0 * dst0 + src1 * dst1 + src2 * dst2 + src3 * dst3;
      if (det == 0.0) {
         return false;
      } else {
         double invdet = 1.0 / det;
         this.m[0] = dst0 * invdet;
         this.m[1] = dst1 * invdet;
         this.m[2] = dst2 * invdet;
         this.m[3] = dst3 * invdet;
         this.m[4] = dst4 * invdet;
         this.m[5] = dst5 * invdet;
         this.m[6] = dst6 * invdet;
         this.m[7] = dst7 * invdet;
         this.m[8] = dst8 * invdet;
         this.m[9] = dst9 * invdet;
         this.m[10] = dst10 * invdet;
         this.m[11] = dst11 * invdet;
         this.m[12] = dst12 * invdet;
         this.m[13] = dst13 * invdet;
         this.m[14] = dst14 * invdet;
         this.m[15] = dst15 * invdet;
         return true;
      }
   }

   @Nonnull
   public Matrix4d projectionOrtho(double left, double right, double bottom, double top, double near, double far) {
      double r_width = 1.0 / (right + left);
      double r_height = 1.0 / (top + bottom);
      double r_depth = -1.0 / (far - near);
      double x = 2.0 * r_width;
      double y = 2.0 * r_height;
      double z = 2.0 * r_depth;
      this.m[1] = this.m[2] = this.m[3] = 0.0;
      this.m[4] = this.m[6] = this.m[7] = 0.0;
      this.m[8] = this.m[9] = this.m[11] = 0.0;
      this.m[15] = 1.0;
      this.m[0] = x;
      this.m[5] = y;
      this.m[10] = z;
      this.m[12] = -(right - left) * r_width;
      this.m[13] = -(top - bottom) * r_height;
      this.m[14] = (far + near) * r_depth;
      return this;
   }

   @Nonnull
   public Matrix4d projectionFrustum(double left, double right, double bottom, double top, double near, double far) {
      double r_width = 1.0 / (right + left);
      double r_height = 1.0 / (top + bottom);
      double r_depth = 1.0 / (near - far);
      this.m[1] = this.m[2] = this.m[3] = 0.0;
      this.m[4] = this.m[6] = this.m[7] = 0.0;
      this.m[12] = this.m[13] = this.m[15] = 0.0;
      this.m[11] = -1.0;
      this.m[0] = 2.0 * (near * r_width);
      this.m[5] = 2.0 * (near * r_height);
      this.m[14] = 2.0 * (far * near * r_depth);
      this.m[8] = 2.0 * (right - left) * r_width;
      this.m[9] = (top - bottom) * r_height;
      this.m[10] = (far + near) * r_depth;
      return this;
   }

   @Nonnull
   public Matrix4d projectionCone(double fov, double aspect, double near, double far) {
      double f = 1.0 / Math.tan(fov * 0.5);
      double r = 1.0 / (near - far);
      this.m[0] = f / aspect;
      this.m[1] = this.m[2] = this.m[3] = 0.0;
      this.m[5] = f;
      this.m[4] = this.m[6] = this.m[7] = 0.0;
      this.m[8] = this.m[9] = 0.0;
      this.m[10] = (far + near) * r;
      this.m[11] = -1.0;
      this.m[12] = this.m[13] = this.m[15] = 0.0;
      this.m[14] = 2.0 * far * near * r;
      return this;
   }

   @Nonnull
   public Matrix4d viewTarget(double eyeX, double eyeY, double eyeZ, double centerX, double centerY, double centerZ, double upX, double upY, double upZ) {
      double dirX = centerX - eyeX;
      double dirY = centerY - eyeY;
      double dirZ = centerZ - eyeZ;
      return this.viewDirection(eyeX, eyeY, eyeZ, dirX, dirY, dirZ, upX, upY, upZ);
   }

   @Nonnull
   public Matrix4d viewDirection(double eyeX, double eyeY, double eyeZ, double dirX, double dirY, double dirZ, double upX, double upY, double upZ) {
      double rlf = 1.0 / MathUtil.length(dirX, dirY, dirZ);
      dirX *= rlf;
      dirY *= rlf;
      dirZ *= rlf;
      double sx = dirY * upZ - dirZ * upY;
      double sy = dirZ * upX - dirX * upZ;
      double sz = dirX * upY - dirY * upX;
      double rls = 1.0 / MathUtil.length(sx, sy, sz);
      sx *= rls;
      sy *= rls;
      sz *= rls;
      double ux = sy * dirZ - sz * dirY;
      double uy = sz * dirX - sx * dirZ;
      double uz = sx * dirY - sy * dirX;
      this.m[0] = sx;
      this.m[1] = ux;
      this.m[2] = -dirX;
      this.m[3] = 0.0;
      this.m[4] = sy;
      this.m[5] = uy;
      this.m[6] = -dirY;
      this.m[7] = 0.0;
      this.m[8] = sz;
      this.m[9] = uz;
      this.m[10] = -dirZ;
      this.m[11] = 0.0;
      this.m[12] = 0.0;
      this.m[13] = 0.0;
      this.m[14] = 0.0;
      this.m[15] = 1.0;
      this.translate(-eyeX, -eyeY, -eyeZ);
      return this;
   }

   @Nonnull
   public Matrix4d rotateAxis(double a, double x, double y, double z, @Nonnull Matrix4d tmp) {
      return this.multiply(tmp.setRotateAxis(a, x, y, z));
   }

   @Nonnull
   public Matrix4d setRotateAxis(double a, double x, double y, double z) {
      double sin = TrigMathUtil.sin(a);
      double cos = TrigMathUtil.cos(a);
      this.m[0] = cos + x * x * (1.0 - cos);
      this.m[1] = x * y * (1.0 - cos) - z * sin;
      this.m[2] = x * z * (1.0 - cos) + y * sin;
      this.m[3] = 0.0;
      this.m[4] = y * x * (1.0 - cos) + z * sin;
      this.m[5] = cos + y * y * (1.0 - cos);
      this.m[6] = y * z * (1.0 - cos) - x * sin;
      this.m[7] = 0.0;
      this.m[8] = z * x * (1.0 - cos) - y * sin;
      this.m[9] = z * y * (1.0 - cos) + x * sin;
      this.m[10] = cos + z * z * (1.0 - cos);
      this.m[11] = 0.0;
      this.m[12] = 0.0;
      this.m[13] = 0.0;
      this.m[14] = 0.0;
      this.m[15] = 1.0;
      return this;
   }

   @Nonnull
   public Matrix4d rotateEuler(double x, double y, double z, @Nonnull Matrix4d tmp) {
      return this.multiply(tmp.setRotateEuler(x, y, z));
   }

   @Nonnull
   public Matrix4d setRotateEuler(double x, double y, double z) {
      double cx = TrigMathUtil.cos(x);
      double sx = TrigMathUtil.sin(x);
      double cy = TrigMathUtil.cos(y);
      double sy = TrigMathUtil.sin(y);
      double cz = TrigMathUtil.cos(z);
      double sz = TrigMathUtil.sin(z);
      double cxsy = cx * sy;
      double sxsy = sx * sy;
      this.m[0] = cy * cz;
      this.m[1] = -cy * sz;
      this.m[2] = sy;
      this.m[4] = sxsy * cz + cx * sz;
      this.m[5] = -sxsy * sz + cx * cz;
      this.m[6] = -sx * cy;
      this.m[8] = -cxsy * cz + sx * sz;
      this.m[9] = cxsy * sz + sx * cz;
      this.m[10] = cx * cy;
      this.m[3] = this.m[7] = this.m[11] = 0.0;
      this.m[12] = this.m[13] = this.m[14] = 0.0;
      this.m[15] = 1.0;
      return this;
   }

   public double[] getData() {
      return this.m;
   }

   public float[] asFloatData() {
      float[] data = new float[16];

      for (int i = 0; i < 16; i++) {
         data[i] = (float)this.m[i];
      }

      return data;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Matrix4d{\n  "
         + this.m[0]
         + " "
         + this.m[4]
         + " "
         + this.m[8]
         + " "
         + this.m[12]
         + "\n  "
         + this.m[1]
         + " "
         + this.m[5]
         + " "
         + this.m[9]
         + " "
         + this.m[13]
         + "\n  "
         + this.m[2]
         + " "
         + this.m[6]
         + " "
         + this.m[10]
         + " "
         + this.m[14]
         + "\n  "
         + this.m[3]
         + " "
         + this.m[7]
         + " "
         + this.m[11]
         + " "
         + this.m[15]
         + "\n}";
   }

   public static int idx(int col, int row) {
      return col << 2 | row;
   }
}
