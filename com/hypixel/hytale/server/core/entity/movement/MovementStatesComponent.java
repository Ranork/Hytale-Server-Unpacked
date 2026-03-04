package com.hypixel.hytale.server.core.entity.movement;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class MovementStatesComponent implements Component<EntityStore> {
   private MovementStates movementStates = new MovementStates();
   private MovementStates sentMovementStates = new MovementStates();

   public static ComponentType<EntityStore, MovementStatesComponent> getComponentType() {
      return EntityModule.get().getMovementStatesComponentType();
   }

   public MovementStatesComponent() {
   }

   public MovementStatesComponent(@Nonnull MovementStatesComponent other) {
      this.movementStates = new MovementStates(other.movementStates);
      this.sentMovementStates = new MovementStates(other.sentMovementStates);
   }

   public MovementStates getMovementStates() {
      return this.movementStates;
   }

   public void setMovementStates(MovementStates movementStates) {
      this.movementStates = movementStates;
   }

   public MovementStates getSentMovementStates() {
      return this.sentMovementStates;
   }

   public void setSentMovementStates(MovementStates sentMovementStates) {
      this.sentMovementStates = sentMovementStates;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new MovementStatesComponent(this);
   }
}
