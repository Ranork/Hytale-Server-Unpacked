package com.hypixel.hytale.builtin.adventure.objectives.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public class TreasureChestOpeningEvent implements IEvent<String> {
   @Nonnull
   private final UUID objectiveUUID;
   @Nonnull
   private final UUID chestUUID;
   @Nonnull
   private final Ref<EntityStore> playerRef;
   @Nonnull
   private final Store<EntityStore> store;

   public TreasureChestOpeningEvent(
      @Nonnull UUID objectiveUUID, @Nonnull UUID chestUUID, @Nonnull Ref<EntityStore> playerRef, @Nonnull Store<EntityStore> store
   ) {
      this.objectiveUUID = objectiveUUID;
      this.chestUUID = chestUUID;
      this.playerRef = playerRef;
      this.store = store;
   }

   @Nonnull
   public UUID getObjectiveUUID() {
      return this.objectiveUUID;
   }

   @Nonnull
   public UUID getChestUUID() {
      return this.chestUUID;
   }

   @Nonnull
   public Ref<EntityStore> getPlayerRef() {
      return this.playerRef;
   }

   @Nonnull
   public Store<EntityStore> getStore() {
      return this.store;
   }

   @Nonnull
   @Override
   public String toString() {
      return "TreasureChestOpeningEvent{objectiveUUID=" + this.objectiveUUID + ", chestUUID=" + this.chestUUID + ", player=" + this.playerRef + "}";
   }
}
