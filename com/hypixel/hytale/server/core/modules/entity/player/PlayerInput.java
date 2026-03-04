package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class PlayerInput implements Component<EntityStore> {
   @Nonnull
   private final List<PlayerInput.InputUpdate> inputUpdateQueue = new ObjectArrayList();
   private int mountId;

   public static ComponentType<EntityStore, PlayerInput> getComponentType() {
      return EntityModule.get().getPlayerInputComponentType();
   }

   public void queue(PlayerInput.InputUpdate inputUpdate) {
      this.inputUpdateQueue.add(inputUpdate);
   }

   @Nonnull
   public List<PlayerInput.InputUpdate> getMovementUpdateQueue() {
      return this.inputUpdateQueue;
   }

   public int getMountId() {
      return this.mountId;
   }

   public void setMountId(int mountId) {
      this.mountId = mountId;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      PlayerInput playerInput = new PlayerInput();
      playerInput.inputUpdateQueue.addAll(this.inputUpdateQueue);
      return playerInput;
   }

   public static class AbsoluteMovement implements PlayerInput.InputUpdate {
      private double x;
      private double y;
      private double z;

      public AbsoluteMovement(double x, double y, double z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }

      public double getX() {
         return this.x;
      }

      public void setX(double x) {
         this.x = x;
      }

      public double getY() {
         return this.y;
      }

      public void setY(double y) {
         this.y = y;
      }

      public double getZ() {
         return this.z;
      }

      public void setZ(double z) {
         this.z = z;
      }

      @Override
      public void apply(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, int index) {
         Ref<EntityStore> playerRef = archetypeChunk.getReferenceTo(index);
         Player playerComponent = archetypeChunk.getComponent(index, Player.getComponentType());

         assert playerComponent != null;

         playerComponent.moveTo(playerRef, this.x, this.y, this.z, commandBuffer);
      }
   }

   public interface InputUpdate {
      void apply(CommandBuffer<EntityStore> var1, ArchetypeChunk<EntityStore> var2, int var3);
   }

   public static class RelativeMovement implements PlayerInput.InputUpdate {
      private double x;
      private double y;
      private double z;

      public RelativeMovement(double x, double y, double z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }

      public double getX() {
         return this.x;
      }

      public void setX(double x) {
         this.x = x;
      }

      public double getY() {
         return this.y;
      }

      public void setY(double y) {
         this.y = y;
      }

      public double getZ() {
         return this.z;
      }

      public void setZ(double z) {
         this.z = z;
      }

      @Override
      public void apply(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, int index) {
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         Player playerComponent = archetypeChunk.getComponent(index, Player.getComponentType());

         assert playerComponent != null;

         TransformComponent transformComponent = archetypeChunk.getComponent(index, TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         playerComponent.moveTo(ref, position.x + this.x, position.y + this.y, position.z + this.z, commandBuffer);
      }
   }

   public record SetBody(Direction direction) implements PlayerInput.InputUpdate {
      @Override
      public void apply(CommandBuffer<EntityStore> commandBuffer, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, int index) {
         TransformComponent transformComponent = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
         if (transformComponent != null) {
            transformComponent.getRotation().assign(this.direction.pitch, this.direction.yaw, this.direction.roll);
         }
      }
   }

   public static class SetClientVelocity implements PlayerInput.InputUpdate {
      private final Vector3d velocity;

      public SetClientVelocity(com.hypixel.hytale.protocol.Vector3d velocity) {
         this.velocity = new Vector3d(velocity.x, velocity.y, velocity.z);
      }

      public Vector3d getVelocity() {
         return this.velocity;
      }

      @Override
      public void apply(CommandBuffer<EntityStore> commandBuffer, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, int index) {
         Velocity velocityComponent = archetypeChunk.getComponent(index, Velocity.getComponentType());
         if (velocityComponent != null) {
            velocityComponent.setClient(this.velocity);
         }
      }
   }

   public record SetHead(Direction direction) implements PlayerInput.InputUpdate {
      @Override
      public void apply(CommandBuffer<EntityStore> commandBuffer, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, int index) {
         HeadRotation headRotationComponent = archetypeChunk.getComponent(index, HeadRotation.getComponentType());
         if (headRotationComponent != null) {
            headRotationComponent.getRotation().assign(this.direction.pitch, this.direction.yaw, this.direction.roll);
         }
      }
   }

   public record SetMovementStates(MovementStates movementStates) implements PlayerInput.InputUpdate {
      @Override
      public void apply(CommandBuffer<EntityStore> commandBuffer, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, int index) {
         MovementStatesComponent movementStatesComponent = archetypeChunk.getComponent(index, MovementStatesComponent.getComponentType());
         if (movementStatesComponent != null) {
            movementStatesComponent.setMovementStates(this.movementStates);
         }
      }
   }

   public record SetRiderMovementStates(MovementStates movementStates) implements PlayerInput.InputUpdate {
      @Override
      public void apply(CommandBuffer<EntityStore> commandBuffer, ArchetypeChunk<EntityStore> archetypeChunk, int index) {
      }
   }

   public static class WishMovement implements PlayerInput.InputUpdate {
      private double x;
      private double y;
      private double z;

      public WishMovement(double x, double y, double z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }

      public double getX() {
         return this.x;
      }

      public void setX(double x) {
         this.x = x;
      }

      public double getY() {
         return this.y;
      }

      public void setY(double y) {
         this.y = y;
      }

      public double getZ() {
         return this.z;
      }

      public void setZ(double z) {
         this.z = z;
      }

      @Override
      public void apply(CommandBuffer<EntityStore> commandBuffer, ArchetypeChunk<EntityStore> archetypeChunk, int index) {
      }
   }
}
