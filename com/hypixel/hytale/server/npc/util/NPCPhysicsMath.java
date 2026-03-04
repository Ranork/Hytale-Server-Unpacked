package com.hypixel.hytale.server.npc.util;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import javax.annotation.Nonnull;

public class NPCPhysicsMath {
   public static final double EPSILON_LENGTH = 1.0E-6;
   public static final double EPSILON_LENGTH_2 = 1.0E-12;

   private NPCPhysicsMath() {
   }

   public static boolean near(@Nonnull Vector3d v, @Nonnull Vector3d w) {
      return near(v, w, 1.0E-12);
   }

   public static boolean near(@Nonnull Vector3d v, @Nonnull Vector3d w, double epsilonLength) {
      return v.distanceSquaredTo(w) <= epsilonLength;
   }

   public static boolean near(double v, double w) {
      return near(v, w, 1.0E-6);
   }

   public static boolean near(double v, double w, double epsilonLength) {
      return Math.abs(v - w) <= epsilonLength;
   }

   public static float headingFromDirection(double x, double z, float def) {
      double s = x * x + z * z;
      return s < 1.0E-12 ? def : PhysicsMath.headingFromDirection(x, z);
   }

   public static float pitchFromDirection(double x, double y, double z, float def) {
      double s = x * x + z * z;
      return s < 1.0E-12 ? def : TrigMathUtil.atan2(y, Math.sqrt(s));
   }

   @Nonnull
   public static Vector3d getViewDirection(@Nonnull Vector3f lookDirection, @Nonnull Vector3d outDirection) {
      return PhysicsMath.vectorFromAngles(lookDirection.getYaw(), lookDirection.getPitch(), outDirection);
   }

   public static double cosAngleBetweenVectors(@Nonnull Vector3d v, @Nonnull Vector3d w) {
      return cosAngleBetweenVectors(v, v.length(), w, w.length());
   }

   public static double cosAngleBetweenVectors(@Nonnull Vector3d v, double vLen, @Nonnull Vector3d w, double wLen) {
      return v.dot(w) / (vLen * wLen);
   }

   public static double cosAngleBetweenUnitVectors(@Nonnull Vector3d v, @Nonnull Vector3d w) {
      return v.dot(w);
   }

   public static void realignVector(@Nonnull Vector3d v, @Nonnull Vector3d w, double cosine, @Nonnull Vector3d result) {
      realignVector(v, v.length(), w, w.length(), cosine, result);
   }

   public static void realignVector(@Nonnull Vector3d v, double vLen, @Nonnull Vector3d w, double wLen, double cosine, @Nonnull Vector3d result) {
      double sine = Math.sqrt(1.0 - cosine * cosine);
      double mX = v.y * w.z - v.z * w.y;
      double mY = v.z * w.x - v.x * w.z;
      double mZ = v.x * w.y - v.y * w.x;
      double nX = v.y * mZ - v.z * mY;
      double nY = v.z * mX - v.x * mZ;
      double nZ = v.x * mY - v.y * mX;
      double nLen = Math.sqrt(dotProduct(nX, nY, nZ));
      double c = cosine * wLen / vLen;
      double d = sine * wLen / nLen;
      result.x = c * v.x - d * nX;
      result.y = c * v.y - d * nY;
      result.z = c * v.z - d * nZ;
   }

   public static void realignUnitVector(@Nonnull Vector3d v, @Nonnull Vector3d w, double cosine, @Nonnull Vector3d result) {
      double sine = Math.sqrt(1.0 - cosine * cosine);
      double mX = v.y * w.z - v.z * w.y;
      double mY = v.z * w.x - v.x * w.z;
      double mZ = v.x * w.y - v.y * w.x;
      double nX = v.y * mZ - v.z * mY;
      double nY = v.z * mX - v.x * mZ;
      double nZ = v.x * mY - v.y * mX;
      double nLen = Math.sqrt(dotProduct(nX, nY, nZ));
      double d = sine / nLen;
      result.x = cosine * v.x - d * nX;
      result.y = cosine * v.y - d * nY;
      result.z = cosine * v.z - d * nZ;
      result.normalize();
   }

   public static boolean realignVectorReturnDirection(
      @Nonnull Vector3d v, double vLen, @Nonnull Vector3d w, double wLen, double cosine, @Nonnull Vector3d result
   ) {
      double sine = Math.sqrt(1.0 - cosine * cosine);
      double mX = v.y * w.z - v.z * w.y;
      double mY = v.z * w.x - v.x * w.z;
      double mZ = v.x * w.y - v.y * w.x;
      double nX = v.y * mZ - v.z * mY;
      double nY = v.z * mX - v.x * mZ;
      double nZ = v.x * mY - v.y * mX;
      double nLen = Math.sqrt(dotProduct(nX, nY, nZ));
      double c = cosine * wLen / vLen;
      double d = sine * wLen / nLen;
      result.x = c * v.x - d * nX;
      result.y = c * v.y - d * nY;
      result.z = c * v.z - d * nZ;
      return dotProduct(v.x, v.y, v.z, nX, nY, nZ) > 0.0;
   }

   @Nonnull
   public static Vector3d createOrthogonalvector(@Nonnull Vector3d in, @Nonnull Vector3d out) {
      double x = in.x;
      double y = in.y;
      double z = in.z;
      double ax = Math.abs(x);
      double ay = Math.abs(y);
      double az = Math.abs(z);
      if (ax >= ay && ax >= az) {
         out.x = y;
         out.y = -x;
         out.z = 0.0;
      } else if (ay >= ax && ay >= az) {
         out.x = 0.0;
         out.y = z;
         out.z = -y;
      } else {
         out.x = -z;
         out.y = 0.0;
         out.z = x;
      }

      return out;
   }

   public static boolean inViewSector(double xViewer, double zViewer, float heading, float coneAngle, double xObject, double zObject) {
      if (coneAngle > (float) Math.PI) {
         return !inViewSector(xViewer, zViewer, heading + (float) Math.PI, (float) (Math.PI * 2) - coneAngle, xObject, zObject);
      } else {
         xObject -= xViewer;
         zObject -= zViewer;
         double l = xObject * xObject + zObject * zObject;
         if (l <= 1.0E-6) {
            return true;
         } else {
            float angle = PhysicsMath.headingFromDirection(xObject, zObject);
            angle -= heading;

            while (angle < (float) -Math.PI) {
               angle += (float) (Math.PI * 2);
            }

            while (angle > (float) Math.PI) {
               angle -= (float) (Math.PI * 2);
            }

            angle *= 2.0F;
            return -coneAngle <= angle && angle <= coneAngle;
         }
      }
   }

   public static boolean isInViewCone(
      double xViewer,
      double yViewer,
      double zViewer,
      double xViewDirection,
      double yViewDirection,
      double zViewDirection,
      float cosConeHalfAngle,
      double xObject,
      double yObject,
      double zObject
   ) {
      return isInViewCone(xViewDirection, yViewDirection, zViewDirection, cosConeHalfAngle, xObject - xViewer, yObject - yViewer, zObject - zViewer);
   }

   public static boolean isInViewCone(
      double xViewDirection, double yViewDirection, double zViewDirection, float cosConeHalfAngle, double xObject, double yObject, double zObject
   ) {
      if (cosConeHalfAngle >= 1.0F) {
         return true;
      } else {
         double len = length(xObject, yObject, zObject);
         if (len < 1.0E-6) {
            return true;
         } else {
            double dot = dotProduct(xViewDirection, yViewDirection, zViewDirection, xObject, yObject, zObject);
            return dot > cosConeHalfAngle * len;
         }
      }
   }

   public static boolean isInViewCone(@Nonnull Vector3d viewer, @Nonnull Vector3d viewDirection, float cosConeHalfAngle, @Nonnull Vector3d object) {
      return isInViewCone(viewer.x, viewer.y, viewer.z, viewDirection.x, viewDirection.y, viewDirection.z, cosConeHalfAngle, object.x, object.y, object.z);
   }

   public static boolean isInViewCone(
      @Nonnull Vector3d viewer, @Nonnull Vector3d viewDirection, float cosConeHalfAngle, @Nonnull Vector3d object, @Nonnull Vector3d componentSelector
   ) {
      double cx = componentSelector.x;
      double cy = componentSelector.y;
      double cz = componentSelector.z;
      return isInViewCone(
         viewer.x * cx,
         viewer.y * cy,
         viewer.z * cz,
         viewDirection.x * cx,
         viewDirection.y * cy,
         viewDirection.z * cz,
         cosConeHalfAngle,
         object.x * cx,
         object.y * cy,
         object.z * cz
      );
   }

   public static float turnAngle(float from, float to) {
      float delta = PhysicsMath.normalizeAngle(to) - PhysicsMath.normalizeAngle(from);
      if (delta < (float) -Math.PI) {
         delta += (float) (Math.PI * 2);
      } else if (delta > (float) Math.PI) {
         delta -= (float) (Math.PI * 2);
      }

      return delta;
   }

   public static float clampRotation(float rotation, float maxAngle) {
      if (rotation >= maxAngle) {
         return maxAngle;
      } else {
         return rotation > -maxAngle ? rotation : -maxAngle;
      }
   }

   public static int intersectLineSphere(
      @Nonnull Vector3d center, double radius, @Nonnull Vector3d p, @Nonnull Vector3d q, @Nonnull Vector3d x1, @Nonnull Vector3d x2, boolean segmentOnly
   ) {
      x1.assign(q).subtract(p);
      x2.assign(p).subtract(center);
      double a = x1.dot(x1);
      double b = 2.0 * x1.dot(x2);
      double c = x2.dot(x2) - radius * radius;
      double k = b * b - 4.0 * a * c;
      if (a == 0.0) {
         if (k != 0.0) {
            return 0;
         } else {
            x1.assign(p);
            return 1;
         }
      } else if (k < 0.0) {
         return 0;
      } else if (k != 0.0) {
         k = Math.sqrt(k) / (2.0 * a);
         double d = -b / (2.0 * a);
         double d1 = d - k;
         double d2 = d + k;
         if (d2 < d1) {
            double t = d1;
            d1 = d2;
            d2 = t;
         }

         if (segmentOnly) {
            if (d1 > 1.0) {
               return 0;
            }

            if (d1 >= 0.0) {
               x1.assign(p).addScaled(x2, d1);
               if (d2 <= 1.0) {
                  x2.scale(d2).add(p);
                  return 2;
               }

               return 1;
            }

            if (d2 >= 0.0 && d2 <= 1.0) {
               x1.assign(p).addScaled(x2, d1);
               return 1;
            }
         }

         x1.assign(p).addScaled(x2, d1);
         x2.scale(d2).add(p);
         return 2;
      } else {
         double dx = -b / (2.0 * a);
         if (!segmentOnly || !(dx < 0.0) && !(dx > 1.0)) {
            x1.assign(p).addScaled(x2, dx);
            return 1;
         } else {
            return 0;
         }
      }
   }

   public static double intersectLineSphereLerp(
      @Nonnull Vector3d center,
      double radius,
      @Nonnull Vector3d p,
      @Nonnull Vector3d q,
      @Nonnull Vector3d t1,
      @Nonnull Vector3d t2,
      @Nonnull Vector3d componentSelector
   ) {
      t1.assign(q).subtract(p).scale(componentSelector);
      t2.assign(p).subtract(center).scale(componentSelector);
      double a = t1.dot(t1);
      double b = 2.0 * t1.dot(t2);
      double c = t2.dot(t2) - radius * radius;
      double k = b * b - 4.0 * a * c;
      if (a == 0.0) {
         return k == 0.0 ? 0.0 : 1.0;
      } else if (k < 0.0) {
         return 1.0;
      } else if (k == 0.0) {
         double d = -b / (2.0 * a);
         return !(d < 0.0) && !(d > 1.0) ? d : 1.0;
      } else {
         k = Math.sqrt(k) / (2.0 * a);
         double d = -b / (2.0 * a);
         double d1 = d - k;
         double d2 = d + k;
         if (d2 < d1) {
            double t = d1;
            d1 = d2;
            d2 = t;
         }

         if (d1 > 1.0) {
            return 1.0;
         } else if (d1 >= 0.0) {
            return d1;
         } else {
            return d2 >= 0.0 && d2 <= 1.0 ? d2 : 1.0;
         }
      }
   }

   public static double intersectLineSphereLerp(
      @Nonnull Vector3d center, double radius, @Nonnull Vector3d p, @Nonnull Vector3d q, @Nonnull Vector3d componentSelector
   ) {
      return intersectLineSphereLerp(center, radius, p, q, new Vector3d(), new Vector3d(), componentSelector);
   }

   public static double dotProduct(@Nonnull Vector3d base, @Nonnull Vector3d p, @Nonnull Vector3d q) {
      double dx = p.x - base.x;
      double dy = p.y - base.y;
      double dz = p.z - base.z;
      double px = q.x - base.x;
      double py = q.y - base.y;
      double pz = q.z - base.z;
      return dx * px + dy * py + dz * pz;
   }

   public static double dotProduct(@Nonnull Vector3d base, @Nonnull Vector3d p, @Nonnull Vector3d q, @Nonnull Vector3d componentSelector) {
      double dx = (p.x - base.x) * componentSelector.x;
      double dy = (p.y - base.y) * componentSelector.y;
      double dz = (p.z - base.z) * componentSelector.z;
      double px = (q.x - base.x) * componentSelector.x;
      double py = (q.y - base.y) * componentSelector.y;
      double pz = (q.z - base.z) * componentSelector.z;
      return dx * px + dy * py + dz * pz;
   }

   public static double dotProduct(double dx, double dy, double dz) {
      return dx * dx + dy * dy + dz * dz;
   }

   public static double dotProduct(double px, double py, double pz, double qx, double qy, double qz) {
      return px * qx + py * qy + pz * qz;
   }

   public static double dotProduct(float dx, float dy, float dz) {
      return dx * dx + dy * dy + dz * dz;
   }

   public static double dotProduct(float px, float py, float pz, float qx, float qy, float qz) {
      return px * qx + py * qy + pz * qz;
   }

   private static double length(double dx, double dy, double dz) {
      return Math.sqrt(dotProduct(dx, dy, dz));
   }

   public static void lerpDistance(@Nonnull Vector3d start, @Nonnull Vector3d end, double distance, @Nonnull Vector3d result) {
      lerp(start, end, distance / start.distanceTo(end), result);
   }

   public static void lerp(@Nonnull Vector3d start, @Nonnull Vector3d end, double lambda, @Nonnull Vector3d result) {
      double dx = end.x - start.x;
      double dy = end.y - start.y;
      double dz = end.z - start.z;
      offsetVector(start, dx, dy, dz, lambda, result);
   }

   public static double lerp(double a, double b, double s) {
      return (1.0 - s) * a + s * b;
   }

   public static void offsetVector(@Nonnull Vector3d start, double dx, double dy, double dz, double lambda, @Nonnull Vector3d result) {
      result.assign(start.x + lambda * dx, start.y + lambda * dy, start.z + lambda * dz);
   }

   public static void offsetVector(double sx, double sy, double sz, double dx, double dy, double dz, double lambda, @Nonnull Vector3d result) {
      result.assign(sx + lambda * dx, sy + lambda * dy, sz + lambda * dz);
   }

   public static void orthoComposition(@Nonnull Vector3d start, @Nonnull Vector3d end, @Nonnull Vector3d ortho, double distance, @Nonnull Vector3d result) {
      double dx = end.x - start.x;
      double dy = end.y - start.y;
      double dz = end.z - start.z;
      double ox = dy * ortho.z - dz * ortho.y;
      double oy = dz * ortho.x - dx * ortho.z;
      double oz = dx * ortho.y - dy * ortho.x;
      offsetVector(end, ox, oy, oz, distance / length(ox, oy, oz), result);
   }

   public static void orthoComposition(
      @Nonnull Vector3d start, @Nonnull Vector3d end, double distanceStart, @Nonnull Vector3d ortho, double distance, @Nonnull Vector3d result
   ) {
      double dx = end.x - start.x;
      double dy = end.y - start.y;
      double dz = end.z - start.z;
      double ox = dy * ortho.z - dz * ortho.y;
      double oy = dz * ortho.x - dx * ortho.z;
      double oz = dx * ortho.y - dy * ortho.x;
      double lambda = distanceStart / length(dx, dy, dz);
      offsetVector(start.x + lambda * dx, start.y + lambda * dy, start.z + lambda * dz, ox, oy, oz, distance / length(ox, oy, oz), result);
   }

   public static float lookatHeading(@Nonnull Vector3d self, @Nonnull Vector3d pointOfInterest, float headingHint) {
      double dx = pointOfInterest.x - self.x;
      double dz = pointOfInterest.z - self.z;
      return dx == 0.0 && dz == 0.0 ? headingHint : PhysicsMath.headingFromDirection(dx, dz);
   }

   public static double blockEmptySpace(@Nonnull BlockType blockType, int rotation, @Nonnull NPCPhysicsMath.Direction direction) {
      if (blockType == null) {
         return 1.0;
      } else if (blockType != BlockType.EMPTY && blockType.getMaterial() == BlockMaterial.Solid) {
         BlockBoundingBoxes blockBoundingBoxes = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
         if (blockBoundingBoxes == null) {
            return 1.0;
         } else {
            Box tempBoundingBox = blockBoundingBoxes.get(rotation).getBoundingBox();
            double d;
            switch (direction) {
               case POS_X:
                  d = tempBoundingBox.min.x;
                  break;
               case NEG_X:
                  d = 1.0 - tempBoundingBox.max.x;
                  break;
               case POS_Y:
                  d = tempBoundingBox.min.y;
                  break;
               case NEG_Y:
                  d = 1.0 - tempBoundingBox.max.y;
                  break;
               case POS_Z:
                  d = tempBoundingBox.min.z;
                  break;
               case NEG_Z:
                  d = 1.0 - tempBoundingBox.max.z;
                  break;
               default:
                  return 0.0;
            }

            return d > 0.0 && d < 1.0 ? d : 0.0;
         }
      } else {
         return 1.0;
      }
   }

   public static double heightOverGround(@Nonnull World world, double x, double y, double z) {
      int ix = MathUtil.floor(x);
      int iy = MathUtil.floor(y);
      int iz = MathUtil.floor(z);
      WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(ix, iz));
      if (chunk == null) {
         return iy + 1;
      } else {
         BlockType blockType = chunk.getBlockType(ix, iy, iz);
         int rotation = chunk.getRotationIndex(ix, iy, iz);
         return iy + blockHeight(blockType, rotation);
      }
   }

   public static double heightOverGround(@Nonnull World world, double x, double z) {
      int ix = MathUtil.floor(x);
      int iz = MathUtil.floor(z);
      WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(ix, iz));
      if (chunk == null) {
         return -1.0;
      } else {
         int iy = chunk.getHeight(ix, iz);
         BlockType blockType = chunk.getBlockType(ix, iy, iz);
         int rotationIndex = chunk.getRotationIndex(ix, iy, iz);
         return iy + blockHeight(blockType, rotationIndex);
      }
   }

   public static double blockHeight(@Nonnull BlockType blockType, int rotation) {
      return 1.0 - blockEmptySpace(blockType, rotation, NPCPhysicsMath.Direction.NEG_Y);
   }

   public static double dotProduct(double x, double y, double z, @Nonnull Vector3d componentSelector) {
      return x * x * componentSelector.x + y * y * componentSelector.y + z * z * componentSelector.z;
   }

   public static double dotProduct(double px, double py, double pz, double qx, double qy, double qz, @Nonnull Vector3d componentSelector) {
      return px * qx * componentSelector.x + py * qy * componentSelector.y + pz * qz * componentSelector.z;
   }

   public static double projectedLengthSquared(@Nonnull Vector3d v, @Nonnull Vector3d componentSelector) {
      return dotProduct(v.x, v.y, v.z, componentSelector);
   }

   public static double projectedLength(@Nonnull Vector3d v, @Nonnull Vector3d componentSelector) {
      return Math.sqrt(projectedLengthSquared(v, componentSelector));
   }

   public static int intersectSweptSpheres(
      @Nonnull Vector3d p1,
      @Nonnull Vector3d velocity1,
      @Nonnull Vector3d p2,
      @Nonnull Vector3d velocity2,
      double radius,
      @Nonnull Vector3d componentSelector,
      double[] results
   ) {
      return intersectSweptSpheres(
         p1.x, p1.y, p1.z, velocity1.x, velocity1.y, velocity1.z, p2.x, p2.y, p2.z, velocity2.x, velocity2.y, velocity2.z, radius, componentSelector, results
      );
   }

   public static int intersectSweptSpheresFootpoint(
      @Nonnull Vector3d p1,
      @Nonnull Vector3d velocity1,
      double radius1,
      @Nonnull Vector3d p2,
      @Nonnull Vector3d velocity2,
      double radius2,
      @Nonnull Vector3d componentSelector,
      double[] results
   ) {
      return intersectSweptSpheres(
         p1.x,
         p1.y + radius1,
         p1.z,
         velocity1.x,
         velocity1.y,
         velocity1.z,
         p2.x,
         p2.y + radius2,
         p2.z,
         velocity2.x,
         velocity2.y,
         velocity2.z,
         radius1 + radius2,
         componentSelector,
         results
      );
   }

   public static int intersectSweptSpheres(
      double p1x,
      double p1y,
      double p1z,
      double velocity1x,
      double velocity1y,
      double velocity1z,
      double p2x,
      double p2y,
      double p2z,
      double velocity2x,
      double velocity2y,
      double velocity2z,
      double radius,
      @Nonnull Vector3d componentSelector,
      double[] results
   ) {
      double px = (p2x - p1x) * componentSelector.x;
      double py = (p2y - p1y) * componentSelector.y;
      double pz = (p2z - p1z) * componentSelector.z;
      double vx = (velocity2x - velocity1x) * componentSelector.x;
      double vy = (velocity2y - velocity1y) * componentSelector.y;
      double vz = (velocity2z - velocity1z) * componentSelector.z;
      double a = dotProduct(vx, vy, vz);
      double b = 2.0 * dotProduct(px, py, pz, vx, vy, vz);
      double c = dotProduct(px, py, pz) - radius * radius;
      double k = b * b - 4.0 * a * c;
      if (k < 0.0) {
         return 0;
      } else {
         results[0] = -b / (2.0 * a);
         results[1] = results[0];
         if (k == 0.0) {
            return 1;
         } else {
            k = Math.sqrt(k) / (2.0 * a);
            results[0] -= k;
            results[1] += k;
            if (results[0] >= results[1]) {
               throw new IllegalArgumentException("IntersectSweptSpheres: Near result larger far result");
            } else {
               return 2;
            }
         }
      }
   }

   public static double collisionSphereRadius(@Nonnull Box boundingBox) {
      return collisionSphereRadius(boundingBox.max.x - boundingBox.min.x, boundingBox.max.z - boundingBox.min.z, boundingBox.max.z - boundingBox.min.z);
   }

   public static double collisionSphereRadius(double boxWidth, double boxDepth, double boxHeight) {
      return Math.pow(boxWidth * boxHeight * boxDepth * 3.0 / (float) (Math.PI * 4), 0.3333333333333333);
   }

   public static double collisionSphereRadius(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());
      if (npcComponent != null) {
         Role role = npcComponent.getRole();
         double radius = role != null ? role.getCollisionRadius() : -1.0;
         if (radius >= 0.0) {
            return radius;
         }
      }

      BoundingBox boundingBoxComponent = componentAccessor.getComponent(ref, BoundingBox.getComponentType());
      Box boundingBox = boundingBoxComponent != null ? boundingBoxComponent.getBoundingBox() : null;
      return boundingBox != null ? collisionSphereRadius(boundingBox.width(), boundingBox.depth(), boundingBox.height()) : 0.75;
   }

   public static double rayCircleIntersect(double sx, double sy, double dx, double dy, double radius) {
      double a = dx * dx + dy * dy;
      double b = 2.0 * (sx * dx + sy * dy);
      double c = sx * sx + sy * sy - radius * radius;
      double k = b * b - 4.0 * a * c;
      if (k < 0.0) {
         return -1.0;
      } else {
         k = Math.sqrt(k);
         a *= 2.0;
         double r1 = (-b - k) / a;
         double r2 = (-b + k) / a;
         if (r1 < 0.0) {
            return r2 < 0.0 ? -1.0 : r2;
         } else if (r2 < 0.0) {
            return r1;
         } else {
            return r1 < r2 ? r1 : r2;
         }
      }
   }

   public static double rayCircleIntersect(@Nonnull Vector3d start, @Nonnull Vector3d end, @Nonnull Vector3d center, double radius, @Nonnull Vector3d normal) {
      if (normal.x == 0.0) {
         return rayCircleIntersect(start.y - center.y, start.z - center.z, end.y - start.y, end.z - start.z, radius);
      } else {
         return normal.y == 0.0
            ? rayCircleIntersect(start.x - center.x, start.z - center.z, end.x - start.x, end.z - start.z, radius)
            : rayCircleIntersect(start.x - center.x, start.y - center.y, end.x - start.x, end.y - start.y, radius);
      }
   }

   @Nonnull
   public static Vector3d projection(@Nonnull Vector3d v, @Nonnull Vector3d p, @Nonnull Vector3d result) {
      result.assign(v).scale(p.dot(v) / v.dot(v));
      return result;
   }

   @Nonnull
   public static Vector3d rejection(@Nonnull Vector3d v, @Nonnull Vector3d p, @Nonnull Vector3d result) {
      projection(v, p, result);
      result.negate().add(p);
      return result;
   }

   @Nonnull
   public static Vector3d subtractVector(@Nonnull Vector3d p, @Nonnull Vector3d q, @Nonnull Vector3d result) {
      return result.assign(p.x - q.x, p.y - q.y, p.z - q.z);
   }

   @Nonnull
   public static Vector3d addDifference(@Nonnull Vector3d result, @Nonnull Vector3d p, @Nonnull Vector3d q) {
      return result.add(p.x - q.x, p.y - q.y, p.z - q.z);
   }

   @Nonnull
   public static Vector3d projection(@Nonnull Vector3d base, @Nonnull Vector3d v, @Nonnull Vector3d p, @Nonnull Vector3d result) {
      subtractVector(v, base, result).scale(dotProduct(base, p, v) / dotProduct(base, v, v));
      return result;
   }

   @Nonnull
   public static Vector3d rejection(@Nonnull Vector3d base, @Nonnull Vector3d v, @Nonnull Vector3d p, @Nonnull Vector3d result) {
      projection(base, v, p, result).negate();
      addDifference(result, p, base);
      return result;
   }

   @Nonnull
   public static Vector3d multiply(@Nonnull Vector3d v, @Nonnull Vector3d w) {
      v.x = v.x * w.x;
      v.y = v.y * w.y;
      v.z = v.z * w.z;
      return v;
   }

   public static double squaredDistProjected(double px, double py, double pz, @Nonnull Vector3d q, @Nonnull Vector3d normal) {
      return squaredDistProjected(px, py, pz, q.x, q.y, q.z, normal);
   }

   public static double squaredDistProjected(double px, double py, double pz, double qx, double qy, double qz, @Nonnull Vector3d normal) {
      double d2 = 0.0;
      if (normal.x == 0.0) {
         double d = qx - px;
         d2 += d * d;
      }

      if (normal.y == 0.0) {
         double d = qy - py;
         d2 += d * d;
      }

      if (normal.z == 0.0) {
         double d = qz - pz;
         d2 += d * d;
      }

      return d2;
   }

   public static double getProjectedDifference(@Nonnull Vector3d p, @Nonnull Vector3d q, @Nonnull Vector3d componentSelector) {
      return (p.x - q.x) * (1.0 - componentSelector.x) + (p.y - q.y) * (1.0 - componentSelector.y) + (p.z - q.z) * (1.0 - componentSelector.z);
   }

   public static boolean isInvalid(double v) {
      return Double.isNaN(v) || Double.isInfinite(v);
   }

   public static boolean isInvalid(@Nonnull Vector3d v) {
      return isInvalid(v.x) || isInvalid(v.y) || isInvalid(v.z);
   }

   public static boolean isValid(double v) {
      return Double.isFinite(v);
   }

   public static boolean isValid(@Nonnull Vector3d v) {
      return isValid(v.x) && isValid(v.y) && isValid(v.z);
   }

   public static double jumpParameters(@Nonnull Vector3d position, @Nonnull Vector3d targetPosition, double gravity, @Nonnull Vector3d velocity) {
      double dx = targetPosition.x - position.x;
      double dz = targetPosition.z - position.z;
      double x = Math.sqrt(dx * dx + dz * dz);
      double y = targetPosition.y - position.y;
      double d = Math.sqrt(x * x + y * y);
      double s1 = y + d;
      double s2 = y - d;
      double v = Math.sqrt(s1 / gravity);
      float phi = TrigMathUtil.atan(-v * v / (gravity * x));
      velocity.assign(dx, TrigMathUtil.sin(phi) * x, dz).setLength(v);
      return v;
   }

   public static double accelerate(double v, double a, double t, double limitSpeed) {
      v += a * t;
      if (v > limitSpeed) {
         v = limitSpeed;
      }

      return v;
   }

   public static double deccelerateToStop(double v, double a, double t) {
      if (v < 0.0) {
         v += a * t;
         if (v > 0.0) {
            v = 0.0;
         }
      } else {
         v -= a * t;
         if (v < 0.0) {
            v = 0.0;
         }
      }

      return v;
   }

   @Nonnull
   public static Vector3d deccelerateToStop(@Nonnull Vector3d v, double a, double t) {
      v.x = deccelerateToStop(v.x, a, t);
      v.y = deccelerateToStop(v.y, a, t);
      v.z = deccelerateToStop(v.z, a, t);
      return v;
   }

   public static double accelerateDrag(double v, double a, double t, double terminalVelocity, double p) {
      return v + t * a * (1.0 - Math.pow(Math.abs(v / terminalVelocity), p));
   }

   public static double accelerateDragCapped(double v, double a, double t, double terminalVelocity, double p) {
      v = accelerateDrag(v, a, t, terminalVelocity, p);
      return v <= terminalVelocity ? v : terminalVelocity;
   }

   public static double accelerateDrag(double v, double a, double t, double terminalVelocity) {
      return accelerateDrag(v, a, t, terminalVelocity, 3.0);
   }

   public static double accelerateDragCapped(double v, double a, double t, double terminalVelocity) {
      return accelerateDragCapped(v, a, t, terminalVelocity, 3.0);
   }

   public static double accelerateToTargetSpeed(double vCurrent, double vTarget, double dt, double accel, double decel, double vMin, double vMax) {
      vTarget = MathUtil.clamp(vTarget, vMin, vMax);
      if (vCurrent == vTarget) {
         return vTarget;
      } else {
         double accelDrag;
         if (vCurrent == 0.0) {
            accelDrag = 0.0;
         } else if (vCurrent > 0.0) {
            accelDrag = -accel * Math.pow(Math.abs(vCurrent / vMax), 3.0);
         } else if (vMin < 0.0) {
            accelDrag = decel * Math.pow(Math.abs(vCurrent / vMin), 3.0);
         } else {
            accelDrag = accel * Math.pow(Math.abs(vCurrent / vMax), 3.0);
         }

         if (vCurrent < vTarget) {
            double v = vCurrent + dt * (accelDrag + accel);
            return v > vTarget ? vTarget : v;
         } else {
            double v = vCurrent + dt * (accelDrag - decel);
            return v < vTarget ? vTarget : v;
         }
      }
   }

   public static double accelerateToTargetSpeed(double vCurrent, double vTarget, double dt, double accel, double decel, double vMax) {
      return accelerateToTargetSpeed(vCurrent, vTarget, dt, accel, decel, 0.0, vMax);
   }

   public static double accelerateToTargetSpeed(double vCurrent, double vTarget, double dt, double accel, double vMax) {
      return accelerateToTargetSpeed(vCurrent, vTarget, dt, accel, accel, 0.0, vMax);
   }

   public static double gravityDrag(double v, double a, double t, double terminalVelocity, double p) {
      double ratio = Math.abs(v / terminalVelocity);
      double pow = Math.pow(ratio, p);
      double dragAccel = a * pow;
      if (v < 0.0) {
         double newV = v - t * (a - dragAccel);
         return v < -terminalVelocity && newV > -terminalVelocity ? -terminalVelocity : newV;
      } else {
         double newV = v - t * (a + dragAccel);
         return v > terminalVelocity && newV < terminalVelocity ? terminalVelocity : newV;
      }
   }

   public static double gravityDrag(double v, double a, double t, double terminalVelocity) {
      return gravityDrag(v, a, t, terminalVelocity, 3.0);
   }

   public static enum Direction {
      POS_X,
      NEG_X,
      POS_Y,
      NEG_Y,
      POS_Z,
      NEG_Z;
   }
}
