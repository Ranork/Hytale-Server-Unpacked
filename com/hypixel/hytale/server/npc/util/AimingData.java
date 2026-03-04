package com.hypixel.hytale.server.npc.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.modules.projectile.config.BallisticData;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.sensorinfo.ExtraInfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AimingData implements ExtraInfoProvider {
   public static final double MIN_MOVE_SPEED_STATIC = 0.01;
   public static final double MIN_MOVE_SPEED_STATIC_2 = 1.0E-4;
   public static final double MIN_AIMING_DISTANCE = 0.01;
   public static final double MIN_AIMING_DISTANCE_2 = 1.0E-4;
   public static final double MIN_AIR_TIME = 0.01;
   public static final double ANGLE_EPSILON = 0.1;
   @Nullable
   private BallisticData ballisticData;
   private boolean useFlatTrajectory = true;
   private double depthOffset;
   private boolean pitchAdjustOffset;
   private boolean haveSolution;
   private boolean haveOrientation;
   private boolean haveAttacked;
   private double chargeDistance;
   private double desiredHitAngle;
   private final float[] pitch = new float[2];
   private final float[] yaw = new float[2];
   @Nullable
   private Ref<EntityStore> target;
   private int owner = Integer.MIN_VALUE;

   public boolean isHaveAttacked() {
      return this.haveAttacked;
   }

   public void setHaveAttacked(boolean haveAttacked) {
      this.haveAttacked = haveAttacked;
   }

   public void requireBallistic(@Nonnull BallisticData ballisticData) {
      this.ballisticData = ballisticData;
      this.haveSolution = false;
      this.haveOrientation = false;
   }

   public void requireCloseCombat() {
      this.ballisticData = null;
      this.haveSolution = false;
      this.haveOrientation = false;
   }

   public float getPitch() {
      return this.getPitch(this.useFlatTrajectory);
   }

   public float getPitch(boolean flatTrajectory) {
      return this.pitch[flatTrajectory ? 0 : 1];
   }

   public float getYaw() {
      return this.getYaw(this.useFlatTrajectory);
   }

   public float getYaw(boolean flatTrajectory) {
      return this.yaw[flatTrajectory ? 0 : 1];
   }

   @Nullable
   public BallisticData getBallisticData() {
      return this.ballisticData;
   }

   public void setUseFlatTrajectory(boolean useFlatTrajectory) {
      this.useFlatTrajectory = useFlatTrajectory;
   }

   public void setChargeDistance(double chargeDistance) {
      this.chargeDistance = chargeDistance;
   }

   public double getChargeDistance() {
      return this.chargeDistance;
   }

   public void setDesiredHitAngle(double desiredHitAngle) {
      this.desiredHitAngle = desiredHitAngle;
   }

   public double getDesiredHitAngle() {
      return this.desiredHitAngle;
   }

   @Nonnull
   @Override
   public Class<AimingData> getType() {
      return AimingData.class;
   }

   public void setDepthOffset(double depthOffset, boolean pitchAdjustOffset) {
      this.depthOffset = depthOffset;
      this.pitchAdjustOffset = depthOffset != 0.0 && pitchAdjustOffset;
   }

   @Nullable
   public Ref<EntityStore> getTarget() {
      if (this.target == null) {
         return null;
      } else if (this.target.isValid() && !this.target.getStore().getArchetype(this.target).contains(DeathComponent.getComponentType())) {
         return this.target;
      } else {
         this.target = null;
         return null;
      }
   }

   public void setTarget(@Nullable Ref<EntityStore> ref) {
      this.target = ref;
   }

   public boolean haveOrientation() {
      return this.haveOrientation || this.haveSolution;
   }

   public void setOrientation(float yaw, float pitch) {
      this.yaw[0] = this.yaw[1] = yaw;
      this.pitch[0] = this.pitch[1] = pitch;
      this.haveOrientation = true;
   }

   public void clearSolution() {
      this.haveOrientation = false;
      this.haveSolution = false;
      this.target = null;
   }

   public boolean computeSolution(double x, double y, double z, double vx, double vy, double vz) {
      double xxzz = x * x + z * z;
      double d2 = xxzz + y * y;
      if (d2 < 0.01) {
         return this.haveSolution = false;
      } else if (this.ballisticData == null) {
         this.yaw[0] = this.yaw[1] = PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(x, z));
         this.pitch[0] = this.pitch[1] = PhysicsMath.pitchFromDirection(x, y, z);
         return this.haveSolution = true;
      } else {
         if (!this.pitchAdjustOffset && xxzz > this.depthOffset * this.depthOffset) {
            double len = Math.sqrt(xxzz);
            double newLen = len - this.depthOffset;
            double scale = newLen / len;
            x *= scale;
            z *= scale;
            xxzz = newLen * newLen;
            d2 = xxzz + y * y;
         }

         double v2 = NPCPhysicsMath.dotProduct(vx, vy, vz);
         if (v2 < 1.0E-4) {
            this.haveSolution = AimingHelper.computePitch(
               Math.sqrt(xxzz), y, this.ballisticData.getMuzzleVelocity(), this.ballisticData.getGravity(), this.pitch
            );
            if (this.haveSolution) {
               this.yaw[0] = this.yaw[1] = PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(x, z));
            }

            return this.haveSolution;
         } else {
            double gravity = this.ballisticData.getGravity();
            double muzzleVelocity = this.ballisticData.getMuzzleVelocity();
            double[] solutions = new double[4];
            double c4 = gravity * gravity / 4.0;
            double c3 = vy * gravity;
            double c2 = v2 + y * gravity - muzzleVelocity * muzzleVelocity;
            double c1 = 2.0 * (x * vx + y * vy + z * vz);
            int numSolutions = RootSolver.solveQuartic(c4, c3, c2, c1, d2, solutions);
            if (numSolutions == 0) {
               return this.haveSolution = false;
            } else {
               int numResults = 0;
               double lastT = Double.MAX_VALUE;

               for (int i = 0; i < numSolutions; i++) {
                  double t = solutions[i];
                  if (!(t <= 0.01)) {
                     double tx = x + t * vx;
                     double tz = z + t * vz;
                     xxzz = tx * tx + tz * tz;
                     if (!(xxzz < 0.01)) {
                        double sine = (y / t + 0.5 * gravity * t) / muzzleVelocity;
                        if (!(sine < -1.0) && !(sine > 1.0)) {
                           float p = TrigMathUtil.asin(sine);
                           float h = PhysicsMath.headingFromDirection(tx, tz);
                           if (numResults < 2) {
                              if (numResults != 0 && !(t > lastT)) {
                                 this.pitch[numResults] = this.pitch[numResults - 1];
                                 this.yaw[numResults] = this.yaw[numResults - 1];
                                 this.pitch[numResults - 1] = p;
                                 this.yaw[numResults - 1] = h;
                              } else {
                                 lastT = t;
                                 this.pitch[numResults] = p;
                                 this.yaw[numResults] = h;
                              }

                              numResults++;
                           }
                        }
                     }
                  }
               }

               if (numResults == 0) {
                  return this.haveSolution = false;
               } else {
                  if (numResults == 1) {
                     this.pitch[1] = this.pitch[0];
                     this.yaw[1] = this.yaw[0];
                  }

                  return this.haveSolution = true;
               }
            }
         }
      }
   }

   public boolean isOnTarget(float yaw, float pitch, double hitAngle) {
      if (!this.haveOrientation()) {
         return false;
      } else {
         double differenceYaw = NPCPhysicsMath.turnAngle(yaw, this.getYaw());
         if (this.ballisticData == null) {
            return -hitAngle <= differenceYaw && differenceYaw <= hitAngle;
         } else {
            double differencePitch = NPCPhysicsMath.turnAngle(pitch, this.getPitch());
            return differencePitch >= -0.1 && differencePitch <= 0.1 && differenceYaw >= -0.1 && differenceYaw <= 0.1;
         }
      }
   }

   public void tryClaim(int id) {
      if (this.owner == Integer.MIN_VALUE) {
         this.owner = id;
         this.clear();
      }
   }

   public boolean isClaimedBy(int id) {
      return this.owner == id;
   }

   public void release() {
      this.owner = Integer.MIN_VALUE;
   }

   public void clear() {
      this.clearSolution();
      this.ballisticData = null;
      this.useFlatTrajectory = true;
      this.depthOffset = 0.0;
      this.pitchAdjustOffset = false;
      this.haveAttacked = false;
   }
}
