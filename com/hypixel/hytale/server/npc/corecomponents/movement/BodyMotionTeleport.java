package com.hypixel.hytale.server.npc.corecomponents.movement;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.BodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionTeleport;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BodyMotionTeleport extends BodyMotionBase {
   public static final int MAX_TRIES = 10;
   public static final int MIN_MOVE_CHANGE = 1;
   public static final double TELEPORT_COOLDOWN = 0.5;
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   protected static final ComponentType<EntityStore, BoundingBox> BOUNDING_BOX_COMPONENT_TYPE = BoundingBox.getComponentType();
   protected final double minOffset;
   protected final double maxOffset;
   protected final double maxYOffset;
   protected final float angle;
   protected final BodyMotionTeleport.Orientation orientation;
   protected final Vector3d target = new Vector3d();
   protected final Vector3d offsetVector = new Vector3d();
   protected final Vector3d lastTriedTarget = new Vector3d();
   protected int tries;
   protected double cooldown;

   public BodyMotionTeleport(@Nonnull BuilderBodyMotionTeleport builder) {
      super(builder);
      double[] offset = builder.getOffsetRadius();
      this.minOffset = offset[0];
      this.maxOffset = offset[1];
      this.maxYOffset = builder.getMaxYOffset();
      this.angle = builder.getSectorRadians() / 2.0F;
      this.orientation = builder.getOrientation();
   }

   @Override
   public void activate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.tries = 10;
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
      if (sensorInfo != null && sensorInfo.getPositionProvider().providePosition(this.target)) {
         double dist = this.target.distanceSquaredTo(this.lastTriedTarget);
         if ((this.tries > 0 || !(dist < 1.0)) && !this.tickCooldown(dt)) {
            if (dist > 1.0) {
               this.tries = 10;
            }

            this.lastTriedTarget.assign(this.target);
            TransformComponent transformComponent = componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

            assert transformComponent != null;

            Vector3d selfPosition = transformComponent.getPosition();
            double distance = selfPosition.distanceSquaredTo(this.target);
            double maxOffset2 = this.maxOffset * this.maxOffset;
            if (distance <= maxOffset2 + 1.0E-5) {
               return false;
            } else {
               this.offsetVector.assign(selfPosition).subtract(this.target).setY(0.0);
               this.offsetVector.setLength(RandomExtra.randomRange(this.minOffset, this.maxOffset));
               this.offsetVector.rotateY(RandomExtra.randomRange(-this.angle, this.angle));
               this.target.add(this.offsetVector);
               MotionController motionController = role.getActiveMotionController();
               BoundingBox boundingBoxComponent = componentAccessor.getComponent(ref, BOUNDING_BOX_COMPONENT_TYPE);
               if (motionController.translateToAccessiblePosition(
                     this.target,
                     boundingBoxComponent != null ? boundingBoxComponent.getBoundingBox() : null,
                     this.target.y - this.maxYOffset,
                     this.target.y + this.maxYOffset,
                     componentAccessor
                  )
                  && motionController.isValidPosition(this.target, componentAccessor)) {
                  switch (this.orientation) {
                     case Unchanged: {
                        Vector3f bodyRotation = transformComponent.getRotation();
                        componentAccessor.addComponent(ref, Teleport.getComponentType(), Teleport.createExact(this.target, bodyRotation));
                        break;
                     }
                     case TowardsTarget: {
                        double x = this.lastTriedTarget.getX() - this.target.getX();
                        double y = this.lastTriedTarget.getY() - this.target.getY();
                        double z = this.lastTriedTarget.getZ() - this.target.getZ();
                        Vector3f bodyRotation = transformComponent.getRotation();
                        float yaw;
                        float pitch;
                        if (x * x + z * z < 1.0E-5) {
                           yaw = bodyRotation.getYaw();
                           pitch = bodyRotation.getPitch();
                        } else {
                           yaw = PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(x, z));
                           pitch = PhysicsMath.pitchFromDirection(x, y, z);
                        }

                        componentAccessor.addComponent(
                           ref, Teleport.getComponentType(), Teleport.createExact(this.target, new Vector3f(yaw, pitch, bodyRotation.getRoll()))
                        );
                        break;
                     }
                     case UseTarget: {
                        Ref<EntityStore> targetRef = sensorInfo.hasPosition() ? sensorInfo.getPositionProvider().getTarget() : null;
                        if (targetRef == null) {
                           return false;
                        }

                        TransformComponent targetTransformComponent = componentAccessor.getComponent(targetRef, TRANSFORM_COMPONENT_TYPE);

                        assert targetTransformComponent != null;

                        Vector3f bodyRotation = targetTransformComponent.getRotation();
                        componentAccessor.addComponent(ref, Teleport.getComponentType(), Teleport.createExact(this.target, bodyRotation));
                     }
                  }

                  this.tries = 10;
                  this.cooldown = 0.5;
                  desiredSteering.clear();
                  return false;
               } else {
                  this.tries--;
                  return true;
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   protected boolean tickCooldown(double dt) {
      if (this.cooldown > 0.0) {
         this.cooldown -= dt;
         return true;
      } else {
         return false;
      }
   }

   public static enum Orientation implements Supplier<String> {
      Unchanged("Do not change orientation"),
      TowardsTarget("Face towards the target"),
      UseTarget("Use the target's orientation");

      private final String description;

      private Orientation(String description) {
         this.description = description;
      }

      public String get() {
         return this.description;
      }
   }
}
