package com.hypixel.hytale.builtin.adventure.teleporter.interaction.server;

import com.hypixel.hytale.builtin.adventure.teleporter.TeleporterPlugin;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class UsedTeleporter implements Component<EntityStore> {
   @Nullable
   private UUID destinationWorldUuid;
   private Vector3d destinationPosition;
   private double clearOutXZ;
   private double clearOutXZSquared;
   private double clearOutY;

   public static ComponentType<EntityStore, UsedTeleporter> getComponentType() {
      return TeleporterPlugin.get().getUsedTeleporterComponentType();
   }

   public UsedTeleporter() {
   }

   public UsedTeleporter(@Nullable UUID destinationWorldUuid, Vector3d destinationPosition, double clearOutXZ, double clearOutY) {
      this.destinationWorldUuid = destinationWorldUuid;
      this.destinationPosition = destinationPosition;
      this.clearOutXZ = clearOutXZ;
      this.clearOutXZSquared = clearOutXZ * clearOutXZ;
      this.clearOutY = clearOutY;
   }

   @Nullable
   public UUID getDestinationWorldUuid() {
      return this.destinationWorldUuid;
   }

   public Vector3d getDestinationPosition() {
      return this.destinationPosition;
   }

   public double getClearOutXZ() {
      return this.clearOutXZ;
   }

   public double getClearOutXZSquared() {
      return this.clearOutXZSquared;
   }

   public double getClearOutY() {
      return this.clearOutY;
   }

   @NullableDecl
   @Override
   public Component<EntityStore> clone() {
      UsedTeleporter clone = new UsedTeleporter();
      clone.destinationWorldUuid = this.destinationWorldUuid;
      clone.destinationPosition = this.destinationPosition.clone();
      clone.clearOutXZ = this.clearOutXZ;
      clone.clearOutXZSquared = this.clearOutXZSquared;
      clone.clearOutY = this.clearOutY;
      return clone;
   }
}
