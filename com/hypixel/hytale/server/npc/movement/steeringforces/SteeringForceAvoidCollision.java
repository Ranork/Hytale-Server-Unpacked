package com.hypixel.hytale.server.npc.movement.steeringforces;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SteeringForceAvoidCollision extends SteeringForceWithGroup {
   private final Vector3d selfVelocity = new Vector3d();
   private double selfRadius;
   private double collisionTime = Double.MAX_VALUE;
   private final Vector3d colliderPosition = new Vector3d();
   private final double[] tempTime = new double[2];
   private final Vector3d tempPos = new Vector3d();
   private final Vector3d tempVel = new Vector3d();
   private double maxDistance;
   private double falloff = 2.0;
   private double strength = 1.0;
   private Role.AvoidanceMode avoidanceMode = Role.AvoidanceMode.Slowdown;
   private Ref<EntityStore> selfReference;
   @Nullable
   private Ref<EntityStore> otherReference;
   private double velocity;
   private double maxTime;
   private boolean canSlowDown;
   private boolean overlap;
   @Nonnull
   protected final Vector3d lastSteeringDirection = new Vector3d();
   private boolean debug;

   public void setDebug(boolean debug) {
      this.debug = debug;
   }

   public Role.AvoidanceMode getAvoidanceMode() {
      return this.avoidanceMode;
   }

   public void setAvoidanceMode(Role.AvoidanceMode avoidanceMode) {
      this.avoidanceMode = avoidanceMode;
   }

   @Override
   public void setSelf(@Nonnull Ref<EntityStore> ref, @Nonnull Vector3d position, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.setSelf(ref, position, null, -1.0, componentAccessor);
   }

   public void setSelf(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Vector3d position,
      @Nullable Vector3d velocity,
      double radius,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      super.setSelf(ref, position, componentAccessor);
      this.selfReference = ref;
      this.otherReference = null;
      if (velocity != null) {
         this.selfVelocity.assign(velocity);
      } else {
         this.setVelocityFromEntity(this.selfReference, componentAccessor);
      }

      if (radius < 0.0) {
         this.setRadiusFromEntity(this.selfReference, componentAccessor);
      } else {
         this.selfRadius = radius;
      }
   }

   @Override
   public void reset() {
      this.collisionTime = Double.MAX_VALUE;
      this.otherReference = null;
      this.canSlowDown = true;
      this.overlap = false;
      double velocitySquared = this.selfVelocity.squaredLength();
      if (velocitySquared > 0.001) {
         this.velocity = Math.sqrt(velocitySquared);
         this.maxTime = this.maxDistance / velocitySquared;
      } else {
         this.velocity = 0.0;
         this.maxTime = 0.0;
      }
   }

   @Override
   public boolean compute(@Nonnull Steering output) {
      this.lastSteeringDirection.assign(Vector3d.ZERO);
      if (this.velocity == 0.0) {
         return false;
      } else {
         if (super.compute(output)) {
            double l = output.getTranslation().length();
            if (l > 1.0E-4) {
               double distance = this.collisionTime * this.selfVelocity.length();
               if (distance <= this.maxDistance) {
                  switch (this.avoidanceMode) {
                     case Slowdown:
                        this.canSlowDown = true;
                        break;
                     case Evade:
                        this.canSlowDown = this.overlap;
                     case Any:
                  }

                  if (this.canSlowDown) {
                     double s = Math.pow(distance / this.maxDistance, 1.0 / this.falloff);
                     output.scaleTranslation(s);
                     if (this.debug) {
                        NPCPlugin.get().getLogger().at(Level.INFO).log("--> Avoidance slowdown=%s dist=%s maxDist=%s", s, distance, this.maxDistance);
                     }
                  } else {
                     this.tempPos.assign(this.colliderPosition).subtract(this.selfPosition);
                     NPCPhysicsMath.rejection(this.selfVelocity, this.tempPos, this.tempVel);
                     this.tempVel.negate();
                     if (this.tempVel.squaredLength() < 0.001) {
                        this.selfVelocity.cross(Vector3d.UP, this.tempVel);
                        if (this.tempVel.squaredLength() < 0.001) {
                           this.selfVelocity.cross(Vector3d.RIGHT, this.tempVel);
                        }
                     }

                     double s = Math.pow(1.0 - distance / this.maxDistance, 1.0 / this.falloff);
                     this.tempVel.setLength(l * s * this.strength).scale(this.componentSelector);
                     this.lastSteeringDirection.assign(this.tempVel);
                     output.scaleTranslation(1.0 - s);
                     output.getTranslation().add(this.tempVel).setLength(l);
                     if (this.debug) {
                        NPCPlugin.get().getLogger().at(Level.INFO).log("--> Avoidance dist=%.2f l=%.2f s=%.2f maxDist=%.2f", distance, l, s, this.maxDistance);
                     }

                     if (!output.getTranslation().isFinite()) {
                        if (this.debug) {
                           NPCPlugin.get().getLogger().at(Level.WARNING).log("Denormalized avoidance steering dist=%s l=%s s=%s", distance, l, s);
                        }

                        output.clearTranslation();
                        return false;
                     }
                  }

                  return true;
               }
            }
         }

         return false;
      }
   }

   @Override
   public void add(@Nonnull Ref<EntityStore> ref, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      if (this.velocity != 0.0 && this.collisionTime != 0.0) {
         TransformComponent transformComponent = commandBuffer.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         UUIDComponent uuidComponent = commandBuffer.getComponent(ref, UUIDComponent.getComponentType());

         assert uuidComponent != null;

         UUID uuid = uuidComponent.getUuid();
         this.tempPos.assign(position);
         boolean departing = this.tempVel.assign(this.tempPos).subtract(this.selfPosition).dot(this.selfVelocity) <= 0.0;
         if (departing) {
            if (this.debug) {
               NPCPlugin.get().getLogger().at(Level.INFO).log("Avoidance add: Entity %s - Moving away, ignoring", uuid);
            }
         } else {
            double entityRadius = NPCPhysicsMath.collisionSphereRadius(ref, commandBuffer);
            double sumRadius = this.selfRadius + entityRadius;
            this.overlap = this.selfPosition.distanceSquaredTo(this.tempPos) <= sumRadius * sumRadius;
            if (this.overlap) {
               this.collisionTime = 0.0;
               this.canSlowDown = true;
               if (this.debug) {
                  NPCPlugin.get().getLogger().at(Level.INFO).log("Avoidance add: Overlap with %s - Stopping", uuid);
               }
            } else {
               Velocity velocityComponent = commandBuffer.getComponent(ref, Velocity.getComponentType());
               velocityComponent.assignVelocityTo(this.tempVel);
               int solutions = NPCPhysicsMath.intersectSweptSpheresFootpoint(
                  this.selfPosition, this.selfVelocity, this.selfRadius, this.tempPos, this.tempVel, entityRadius, Vector3d.ALL_ONES, this.tempTime
               );
               if (this.debug && solutions > 0) {
                  NPCPlugin.get()
                     .getLogger()
                     .at(Level.INFO)
                     .log(
                        "Avoidance add: Solutions with %s=%s, time=[%s, %s], maxTime=%s, collisionTime=%s",
                        uuid,
                        solutions,
                        this.tempTime[0],
                        this.tempTime[1],
                        this.maxTime,
                        this.collisionTime
                     );
               }

               if (solutions != 0 && !(this.tempTime[0] < 0.0) && !(this.tempTime[0] > this.maxTime) && !(this.tempTime[0] >= this.collisionTime)) {
                  double tempVelocity = this.tempVel.length();
                  double dot = tempVelocity > 0.0 ? this.selfVelocity.dot(this.tempVel) / (tempVelocity * this.velocity) : 0.0;
                  boolean antiParallel = dot < -0.8;
                  if (this.debug && solutions > 0) {
                     NPCPlugin.get()
                        .getLogger()
                        .at(Level.INFO)
                        .log(
                           "Avoidance add: New solution with %s, time=[%s, %s], maxTime=%s, antiParallel=%s, dot=%s, departing=%s, collisionTime=%s",
                           uuid,
                           this.tempTime[0],
                           this.tempTime[1],
                           this.maxTime,
                           antiParallel,
                           dot,
                           departing,
                           this.collisionTime
                        );
                  }

                  this.colliderPosition.assign(position);
                  this.collisionTime = this.tempTime[0];
                  this.otherReference = ref;
                  this.canSlowDown = !antiParallel && this.otherReference.getIndex() < this.selfReference.getIndex();
               }
            }
         }
      }
   }

   public void setVelocityFromEntity(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Velocity velocityComponent = componentAccessor.getComponent(ref, Velocity.getComponentType());
      velocityComponent.assignVelocityTo(this.selfVelocity);
   }

   public void setRadiusFromEntity(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.selfRadius = NPCPhysicsMath.collisionSphereRadius(ref, componentAccessor);
   }

   public void setMaxDistance(double distance) {
      this.maxDistance = distance;
   }

   public void setFalloff(double falloff) {
      this.falloff = falloff;
   }

   public void setSelfVelocity(@Nonnull Vector3d selfVelocity) {
      this.selfVelocity.assign(selfVelocity);
   }

   @Nonnull
   public Vector3d getSelfVelocity() {
      return this.selfVelocity;
   }

   public double getSelfRadius() {
      return this.selfRadius;
   }

   public void setSelfRadius(double selfRadius) {
      this.selfRadius = selfRadius;
   }

   public double getStrength() {
      return this.strength;
   }

   public void setStrength(double strength) {
      this.strength = strength;
   }

   @Nonnull
   public Vector3d getLastSteeringDirection() {
      return this.lastSteeringDirection;
   }
}
