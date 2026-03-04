package com.hypixel.hytale.server.npc.corecomponents.movement;

import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.BodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionWanderBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.movement.controllers.ProbeMoveData;
import com.hypixel.hytale.server.npc.movement.steeringforces.SteeringForcePursue;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.RoleDebugFlags;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BodyMotionWanderBase extends BodyMotionBase {
   public static final HytaleLogger LOGGER = NPCPlugin.get().getLogger();
   public static final int DIRECTION_COUNT = 32;
   public static final float SEGMENT_ANGLE = (float) (Math.PI / 16);
   public static final double MIN_DISTANCE_SHRINK = 0.3;
   public static final double MIN_DISTANCE_SHRINK_SCALE = -1.4;
   protected final double minWalkTime;
   protected final double maxWalkTime;
   protected final float minHeadingChange;
   protected final float maxHeadingChange;
   protected final byte minDirection;
   protected final byte maxDirection;
   protected final boolean relaxHeadingChange;
   protected final double relativeSpeed;
   protected final double minMoveDistance;
   protected final double stopDistance;
   protected final int testsPerTick;
   protected final boolean isAvoidingBlockDamage;
   protected final boolean isRelaxedMoveConstraints;
   protected final double desiredAltitudeWeight;
   protected final byte[] preOrderedDirections = new byte[32];
   protected final int insideConeCount;
   protected final Vector3d targetPosition = new Vector3d();
   protected final Vector3d probeDirection = new Vector3d();
   protected final Vector3d probePosition = new Vector3d();
   protected final SteeringForcePursue seekTarget = new SteeringForcePursue();
   protected final ProbeMoveData probeMoveData = new ProbeMoveData();
   protected boolean debugSteer;
   protected BodyMotionWanderBase.State state;
   protected float angleOffset;
   protected double probeDY;
   protected double maxDistanceAbove;
   protected double maxDistanceBelow;
   protected double walkTime;
   protected float walkHeading;
   protected double walkDistance;
   protected int directionIndex;
   protected double desiredWalkDistance;
   protected final double[] walkDistances = new double[32];
   protected final byte[] walkDirections = new byte[32];

   public BodyMotionWanderBase(@Nonnull BuilderBodyMotionWanderBase builder, @Nonnull BuilderSupport builderSupport) {
      super(builder);
      this.minWalkTime = builder.getMinWalkTime(builderSupport);
      this.maxWalkTime = builder.getMaxWalkTime(builderSupport);
      this.minHeadingChange = (float) (Math.PI / 180.0) * builder.getMinHeadingChange(builderSupport);
      this.maxHeadingChange = (float) (Math.PI / 180.0) * builder.getMaxHeadingChange(builderSupport);
      this.relaxHeadingChange = builder.isRelaxHeadingChange(builderSupport);
      this.minDirection = (byte)MathUtil.fastFloor(this.minHeadingChange / (float) (Math.PI / 16));
      this.maxDirection = (byte)MathUtil.fastCeil(this.maxHeadingChange / (float) (Math.PI / 16));
      this.relativeSpeed = builder.getRelativeSpeed(builderSupport);
      this.minMoveDistance = builder.getMinMoveDistance(builderSupport);
      this.stopDistance = builder.getStopDistance(builderSupport);
      this.testsPerTick = builder.getTestsPerTick(builderSupport);
      this.desiredAltitudeWeight = builder.getDesiredAltitudeWeight(builderSupport);
      boolean avoidingBlockDamage = builder.isAvoidingBlockDamage(builderSupport);
      this.isAvoidingBlockDamage = avoidingBlockDamage;
      this.probeMoveData.setAvoidingBlockDamage(avoidingBlockDamage);
      boolean relaxedMoveConstraints = builder.isRelaxedMoveConstraints(builderSupport);
      this.isRelaxedMoveConstraints = relaxedMoveConstraints;
      this.probeMoveData.setRelaxedMoveConstraints(relaxedMoveConstraints);
      int count = 0;

      for (int i = this.minDirection; i <= this.maxDirection; i++) {
         count = this.addPreOrderedDirection(i, count);
      }

      this.insideConeCount = count;

      for (int var7 = 0; var7 < this.minDirection; var7++) {
         count = this.addPreOrderedDirection(var7, count);
      }

      for (int var8 = this.maxDirection + 1; var8 <= 16; var8++) {
         count = this.addPreOrderedDirection(var8, count);
      }
   }

   @Override
   public void activate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.debugSteer = role.getDebugSupport().isDebugFlagSet(RoleDebugFlags.MotionControllerSteer);
      NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

      assert npcComponent != null;

      this.restartSearch(ref, npcComponent, role.getActiveMotionController(), componentAccessor);
   }

   @Override
   public void deactivate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.debugSteer) {
         componentAccessor.removeComponent(ref, Nameplate.getComponentType());
      }
   }

   @Override
   public void motionControllerChanged(
      @Nullable Ref<EntityStore> ref,
      @Nonnull NPCEntity npcComponent,
      @Nonnull MotionController motionController,
      @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.restartSearch(ref, npcComponent, motionController, componentAccessor);
   }

   @Override
   public boolean computeSteering(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nullable InfoProvider sensorInfo,
      double dt,
      @Nonnull Steering desiredSteering,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

      assert npcComponent != null;

      MotionController activeMotionController = role.getActiveMotionController();
      if (this.debugSteer) {
         LOGGER.at(Level.INFO)
            .log(
               "Wander compute: state=%s canAct=%s blocked=%s walkTime=%s",
               this.state.toString(),
               activeMotionController.canAct(ref, componentAccessor),
               activeMotionController.isObstructed(),
               this.walkTime
            );
         String headline = this.state.toString();
         componentAccessor.putComponent(ref, Nameplate.getComponentType(), new Nameplate(headline));
      }

      desiredSteering.clear();
      float currentHorizontalSpeedMultiplier = npcComponent.getCurrentHorizontalSpeedMultiplier(ref, componentAccessor);
      if (currentHorizontalSpeedMultiplier == 0.0F) {
         this.state = BodyMotionWanderBase.State.STOPPED;
         return true;
      } else {
         if (this.state == BodyMotionWanderBase.State.STOPPED) {
            this.restartSearch(ref, npcComponent, activeMotionController, componentAccessor);
         }

         if (activeMotionController.isInProgress()) {
            if (this.state == BodyMotionWanderBase.State.WALKING) {
               this.walkTime -= dt;
               activeMotionController.setRelaxedMoveConstraints(this.isRelaxedMoveConstraints);
               activeMotionController.setAvoidingBlockDamage(this.isAvoidingBlockDamage && activeMotionController.isAvoidingBlockDamage());
            }

            return true;
         } else if (!activeMotionController.canAct(ref, componentAccessor)) {
            return true;
         } else {
            TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

            assert transformComponent != null;

            Vector3f bodyRotation = transformComponent.getRotation();
            if (activeMotionController.isObstructed() && this.state == BodyMotionWanderBase.State.WALKING) {
               this.restartSearch(ref, npcComponent, activeMotionController, componentAccessor);
               if (this.debugSteer) {
                  LOGGER.at(Level.INFO)
                     .log(
                        "Wander: Blocked state=%s directionIndex=%s walkTime=%s yaw=%s newYaw=%s",
                        this.state.toString(),
                        this.directionIndex,
                        this.walkTime,
                        (180.0F / (float)Math.PI) * bodyRotation.getYaw(),
                        (180.0F / (float)Math.PI) * this.walkHeading
                     );
               }
            }

            if (this.state == BodyMotionWanderBase.State.SEARCHING) {
               int testCount = 0;

               while (true) {
                  if (this.directionIndex == 32 || !this.relaxHeadingChange && this.directionIndex == this.insideConeCount) {
                     if (this.findBestDirection(ref, componentAccessor)) {
                        break;
                     }

                     this.restartSearch(ref, npcComponent, activeMotionController, componentAccessor);
                  } else {
                     if (this.probeDirection(ref, this.directionIndex, role, componentAccessor)) {
                        break;
                     }

                     this.directionIndex++;
                  }

                  if (++testCount >= this.testsPerTick) {
                     return true;
                  }
               }

               double stopDistance = Math.min(Math.max(this.stopDistance, activeMotionController.getCurrentTurnRadius()), this.walkDistance);
               double slowdownDistance = Math.min(2.0 * stopDistance, this.walkDistance);
               this.seekTarget.setDistances(slowdownDistance, stopDistance);
               this.seekTarget.setComponentSelector(activeMotionController.getComponentSelector());
               this.seekTarget.setTargetPosition(this.targetPosition);
               this.state = BodyMotionWanderBase.State.TURNING;
               if (this.debugSteer) {
                  LOGGER.at(Level.INFO)
                     .log(
                        "Wander: Found move state=%s directionIndex=%s yaw=%s newYaw=%s",
                        this.state.toString(),
                        this.directionIndex,
                        (180.0F / (float)Math.PI) * bodyRotation.getYaw(),
                        (180.0F / (float)Math.PI) * this.walkHeading
                     );
               }
            }

            if (this.state == BodyMotionWanderBase.State.TURNING) {
               float heading = bodyRotation.getYaw();
               double turnAngle = NPCPhysicsMath.turnAngle(this.walkHeading, heading);
               if (!(Math.abs(turnAngle) < 0.05235988F)) {
                  desiredSteering.setYaw(this.walkHeading);
                  if (this.debugSteer) {
                     LOGGER.at(Level.INFO)
                        .log(
                           "Wander: Turn state=%s turnAngle=%s heading=%s walkHeading=%s",
                           this.state.toString(),
                           180.0F / (float)Math.PI * turnAngle,
                           (180.0F / (float)Math.PI) * heading,
                           (180.0F / (float)Math.PI) * this.walkHeading
                        );
                  }

                  return true;
               }

               if (this.debugSteer) {
                  LOGGER.at(Level.INFO)
                     .log(
                        "Wander: Walk state=%s yaw=%s desiredYaw=%s walkTime=%s",
                        this.state.toString(),
                        (180.0F / (float)Math.PI) * bodyRotation.getYaw(),
                        (180.0F / (float)Math.PI) * this.walkHeading,
                        this.walkTime
                     );
               }

               this.state = BodyMotionWanderBase.State.WALKING;
            }

            if (this.state == BodyMotionWanderBase.State.WALKING) {
               this.seekTarget.setSelfPosition(transformComponent.getPosition());
               this.walkTime -= dt;
               if (!this.seekTarget.compute(desiredSteering) || this.walkTime <= 0.0) {
                  this.restartSearch(ref, npcComponent, activeMotionController, componentAccessor);
                  if (this.debugSteer) {
                     LOGGER.at(Level.INFO)
                        .log(
                           "Wander: Walk done state=%s directionIndex=%s yaw=%s desiredYaw=%s",
                           this.state.toString(),
                           this.directionIndex,
                           (180.0F / (float)Math.PI) * bodyRotation.getYaw(),
                           (180.0F / (float)Math.PI) * this.walkHeading
                        );
                  }
               }

               activeMotionController.setRelaxedMoveConstraints(this.isRelaxedMoveConstraints);
               activeMotionController.setAvoidingBlockDamage(this.isAvoidingBlockDamage && activeMotionController.isAvoidingBlockDamage());
               desiredSteering.scaleTranslation(this.relativeSpeed);
            }

            return true;
         }
      }
   }

   protected boolean findBestDirection(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      int index = -1;
      double distance = 0.0;
      double count = 0.0;
      double average = 0.0;

      for (int i = 0; i < this.directionIndex; i++) {
         double d = this.walkDistances[i];
         if (d > 0.0) {
            count++;
            average += d;
         }
      }

      if (count > 0.0) {
         average /= count;
         ThreadLocalRandom random = ThreadLocalRandom.current();

         for (int ix = 0; ix < this.directionIndex; ix++) {
            double d = this.walkDistances[ix];
            if (d > distance) {
               distance = d;
               index = ix;
               if (!(d < average)) {
                  double r = random.nextDouble();
                  if (r <= 0.5) {
                     double scale = r * -1.4 + 1.0;
                     distance = d * scale;
                     break;
                  }
               }
            }
         }
      }

      if (index == -1) {
         return false;
      } else {
         this.walkHeading = this.toAngle(ref, this.walkDirections[index], componentAccessor);
         this.walkDistance = distance;
         this.computeTargetPosition(ref, this.walkHeading, this.walkDistance, componentAccessor);
         return true;
      }
   }

   protected abstract double constrainMove(
      @Nonnull Ref<EntityStore> var1,
      @Nonnull Role var2,
      @Nonnull Vector3d var3,
      @Nonnull Vector3d var4,
      double var5,
      @Nonnull ComponentAccessor<EntityStore> var7
   );

   protected void restartSearch(
      @Nullable Ref<EntityStore> ref,
      @Nonnull NPCEntity npcComponent,
      @Nonnull MotionController activeMotionController,
      @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.state = BodyMotionWanderBase.State.SEARCHING;
      float currentHorizontalSpeedMultiplier = npcComponent.getCurrentHorizontalSpeedMultiplier(ref, componentAccessor);
      this.walkTime = RandomExtra.randomRange(this.minWalkTime, this.maxWalkTime) / currentHorizontalSpeedMultiplier;
      this.desiredWalkDistance = this.relativeSpeed * activeMotionController.getMaximumSpeed() * this.walkTime;
      this.directionIndex = 0;
      this.angleOffset = this.relaxHeadingChange ? ThreadLocalRandom.current().nextFloat() * (float) (Math.PI / 16) : 0.0F;
      if (ref != null && componentAccessor != null) {
         this.computeHeightRange(ref, activeMotionController, componentAccessor);
      }

      this.probeDY = RandomExtra.randomRange(-this.maxDistanceBelow, this.maxDistanceAbove);
      System.arraycopy(this.preOrderedDirections, 0, this.walkDirections, 0, 32);
      ArrayUtil.shuffleArray(this.walkDirections, 0, this.insideConeCount, ThreadLocalRandom.current());
      if (this.insideConeCount < 31) {
         ArrayUtil.shuffleArray(this.walkDirections, this.insideConeCount, 32, ThreadLocalRandom.current());
      }

      Arrays.fill(this.walkDistances, 0.0);
   }

   protected void computeHeightRange(
      @Nonnull Ref<EntityStore> ref, @Nonnull MotionController motionController, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.maxDistanceAbove = 0.0;
      this.maxDistanceBelow = 0.0;
      if (!motionController.is2D()) {
         double wanderVerticalMovementRatio = motionController.getWanderVerticalMovementRatio();
         double maxVerticalDistance = wanderVerticalMovementRatio * this.desiredWalkDistance;
         if (maxVerticalDistance != 0.0) {
            MotionController.VerticalRange verticalRange = motionController.getDesiredVerticalRange(ref, componentAccessor);
            double desiredAltitudeWeight = this.desiredAltitudeWeight >= 0.0 ? this.desiredAltitudeWeight : motionController.getDesiredAltitudeWeight();
            if (desiredAltitudeWeight > 0.0 && !verticalRange.isWithinRange()) {
               maxVerticalDistance = this.desiredWalkDistance * (wanderVerticalMovementRatio + (1.0 - wanderVerticalMovementRatio) * desiredAltitudeWeight);
            }

            double y = verticalRange.current;
            double negativeMaxVerticalDistance = -maxVerticalDistance * desiredAltitudeWeight;
            this.maxDistanceAbove = MathUtil.clamp(verticalRange.max - y, negativeMaxVerticalDistance, maxVerticalDistance);
            this.maxDistanceBelow = MathUtil.clamp(y - verticalRange.min, negativeMaxVerticalDistance, maxVerticalDistance);
         }
      }
   }

   protected boolean probeDirection(@Nonnull Ref<EntityStore> ref, int dirIndex, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      int direction = this.walkDirections[dirIndex];
      MotionController motionController = role.getActiveMotionController();
      float heading = this.toAngle(ref, direction, componentAccessor);
      this.computeTargetPosition(ref, heading, this.desiredWalkDistance, componentAccessor);
      double constrainDistance = this.constrainMove(ref, role, this.probePosition, this.targetPosition, this.desiredWalkDistance, componentAccessor);
      if (constrainDistance < 1.0E-5) {
         return false;
      } else {
         if (constrainDistance < this.desiredWalkDistance) {
            this.probeDirection.scale(constrainDistance / this.desiredWalkDistance);
         }

         this.probeMoveData.setAvoidingBlockDamage(!motionController.willReceiveBlockDamage());
         double moveDistance = motionController.probeMove(ref, this.probePosition, this.probeDirection, this.probeMoveData, componentAccessor);
         if (moveDistance < 1.0E-5) {
            return false;
         } else {
            this.walkDistances[dirIndex] = moveDistance;
            if (moveDistance < this.desiredWalkDistance) {
               return false;
            } else {
               if (moveDistance < constrainDistance) {
                  this.probeDirection.scale(moveDistance / constrainDistance);
               }

               this.walkDistance = moveDistance;
               this.walkHeading = heading;
               this.targetPosition.assign(this.probePosition).add(this.probeDirection);
               return true;
            }
         }
      }
   }

   private void computeTargetPosition(@Nonnull Ref<EntityStore> ref, float heading, double distance, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      this.probePosition.assign(transformComponent.getPosition());
      this.probeDirection.x = PhysicsMath.headingX(heading) * distance;
      this.probeDirection.y = this.probeDY * distance / this.desiredWalkDistance;
      this.probeDirection.z = PhysicsMath.headingZ(heading) * distance;
      this.targetPosition.assign(this.probePosition).add(this.probeDirection);
   }

   protected float toAngle(@Nonnull Ref<EntityStore> ref, int direction, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      return PhysicsMath.normalizeAngle(transformComponent.getRotation().getYaw() + direction * (float) (Math.PI / 16) + this.angleOffset);
   }

   private int addPreOrderedDirection(int direction, int count) {
      this.preOrderedDirections[count++] = (byte)direction;
      if (direction != 0 && direction != 16) {
         this.preOrderedDirections[count++] = (byte)(32 - direction);
      }

      return count;
   }

   public static enum State {
      SEARCHING,
      TURNING,
      WALKING,
      STOPPED;
   }
}
