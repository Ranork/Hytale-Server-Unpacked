package com.hypixel.hytale.server.npc.movement.controllers;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.movement.MovementState;
import com.hypixel.hytale.server.npc.movement.NavState;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.RoleDebugFlags;
import com.hypixel.hytale.server.npc.role.support.DebugSupport;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface MotionController extends DebugSupport.DebugFlagsChangeListener {
   String getType();

   Role getRole();

   void setRole(Role var1);

   void setInertia(double var1);

   void setKnockbackScale(double var1);

   double getGravity();

   void setHeadPitchAngleRange(@Nullable float[] var1);

   void spawned();

   void activate();

   void deactivate();

   void updateModelParameters(@Nullable Ref<EntityStore> var1, Model var2, Box var3, @Nullable ComponentAccessor<EntityStore> var4);

   double steer(
      @Nonnull Ref<EntityStore> var1,
      @Nonnull Role var2,
      @Nonnull Steering var3,
      @Nonnull Steering var4,
      double var5,
      @Nonnull ComponentAccessor<EntityStore> var7
   );

   double probeMove(@Nonnull Ref<EntityStore> var1, Vector3d var2, Vector3d var3, ProbeMoveData var4, @Nonnull ComponentAccessor<EntityStore> var5);

   double probeMove(@Nonnull Ref<EntityStore> var1, ProbeMoveData var2, @Nonnull ComponentAccessor<EntityStore> var3);

   void constrainRotations(Role var1, TransformComponent var2);

   double getCurrentMaxBodyRotationSpeed();

   void updateMovementState(
      @Nonnull Ref<EntityStore> var1,
      @Nonnull MovementStates var2,
      @Nonnull Steering var3,
      @Nonnull Vector3d var4,
      @Nonnull ComponentAccessor<EntityStore> var5
   );

   boolean isValidPosition(Vector3d var1, ComponentAccessor<EntityStore> var2);

   boolean canAct(@Nonnull Ref<EntityStore> var1, @Nonnull ComponentAccessor<EntityStore> var2);

   boolean isInProgress();

   boolean isObstructed();

   boolean inAir();

   boolean inWater();

   boolean onGround();

   boolean standingOnBlockOfType(int var1);

   double getMaximumSpeed();

   double getCurrentSpeed();

   boolean estimateVelocity(Steering var1, Vector3d var2);

   double getCurrentTurnRadius();

   double waypointDistance(Vector3d var1, Vector3d var2);

   double waypointDistanceSquared(Vector3d var1, Vector3d var2);

   double waypointDistance(@Nonnull Ref<EntityStore> var1, Vector3d var2, @Nonnull ComponentAccessor<EntityStore> var3);

   double waypointDistanceSquared(@Nonnull Ref<EntityStore> var1, Vector3d var2, @Nonnull ComponentAccessor<EntityStore> var3);

   float getMaxClimbAngle();

   float getMaxSinkAngle();

   boolean translateToAccessiblePosition(Vector3d var1, Box var2, double var3, double var5, ComponentAccessor<EntityStore> var7);

   Vector3d getComponentSelector();

   Vector3d getPlanarComponentSelector();

   void setComponentSelector(Vector3d var1);

   boolean is2D();

   Vector3d getWorldNormal();

   Vector3d getWorldAntiNormal();

   void addForce(@Nonnull Vector3d var1, @Nullable VelocityConfig var2);

   Vector3d getForce();

   void forceVelocity(@Nonnull Vector3d var1, @Nullable VelocityConfig var2, boolean var3);

   MotionController.VerticalRange getDesiredVerticalRange(@Nonnull Ref<EntityStore> var1, @Nonnull ComponentAccessor<EntityStore> var2);

   double getWanderVerticalMovementRatio();

   void setAvoidingBlockDamage(boolean var1);

   boolean isAvoidingBlockDamage();

   boolean willReceiveBlockDamage();

   void requirePreciseMovement(Vector3d var1);

   void requireDepthProbing();

   void enableHeadingBlending(double var1, Vector3d var3, double var4);

   void enableHeadingBlending();

   void setRelaxedMoveConstraints(boolean var1);

   boolean isRelaxedMoveConstraints();

   NavState getNavState();

   double getThrottleDuration();

   double getTargetDeltaSquared();

   void setNavState(NavState var1, double var2, double var4);

   void setForceRecomputePath(boolean var1);

   boolean isForceRecomputePath();

   boolean canRestAtPlace();

   void beforeInstructionSensorsAndActions(double var1);

   void beforeInstructionMotion(double var1);

   default boolean matchesType(@Nonnull Class<? extends MotionController> clazz) {
      return clazz.isInstance(this);
   }

   double getDesiredAltitudeWeight();

   double getHeightOverGround();

   default void clearOverrides() {
   }

   default double getSquaredDistance(@Nonnull Vector3d p1, @Nonnull Vector3d p2, boolean useProjectedDistance) {
      return useProjectedDistance ? this.waypointDistanceSquared(p1, p2) : p1.distanceSquaredTo(p2);
   }

   void updatePhysicsValues(PhysicsValues var1);

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   static boolean isInMovementState(@Nonnull Ref<EntityStore> ref, @Nonnull MovementState state, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      MovementStatesComponent movementStatesComponent = componentAccessor.getComponent(ref, MovementStatesComponent.getComponentType());
      if (!<unrepresentable>.$assertionsDisabled && movementStatesComponent == null) {
         throw new AssertionError();
      } else {
         Velocity velocityComponent = componentAccessor.getComponent(ref, Velocity.getComponentType());
         if (!<unrepresentable>.$assertionsDisabled && velocityComponent == null) {
            throw new AssertionError();
         } else {
            MovementStates states = movementStatesComponent.getMovementStates();

            return switch (state) {
               case CLIMBING -> states.climbing;
               case FALLING -> states.falling;
               case CROUCHING -> states.crouching;
               case FLYING -> states.flying;
               case JUMPING -> states.jumping;
               case SPRINTING -> states.sprinting;
               case RUNNING -> states.running;
               case IDLE -> velocityComponent.getVelocity().closeToZero(0.001);
               case WALKING -> !velocityComponent.getVelocity().closeToZero(0.001)
                  && !states.falling
                  && !states.climbing
                  && !states.flying
                  && !states.running
                  && !states.sprinting
                  && !states.jumping
                  && !states.crouching;
               case ANY -> true;
            };
         }
      }
   }

   @Override
   default void onDebugFlagsChanged(EnumSet<RoleDebugFlags> newFlags) {
   }

   static {
      if (<unrepresentable>.$assertionsDisabled) {
      }
   }

   public static class VerticalRange {
      public double current;
      public double min;
      public double max;

      public void assign(double current, double min, double max) {
         this.current = current;
         this.min = min;
         this.max = max;
      }

      public boolean isWithinRange() {
         return this.current >= this.min && this.current <= this.max;
      }
   }
}
