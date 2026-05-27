package com.hypixel.hytale.builtin.triggervolumes.event;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.UUID;
import javax.annotation.Nonnull;

public class TriggerVolumeEvent implements IEvent<String> {
   @Nonnull
   private final String worldName;
   @Nonnull
   private final TriggerEventType triggerEventType;
   @Nonnull
   private final String volumeId;
   @Nonnull
   private final Ref<EntityStore> entityRef;
   @Nonnull
   private final UUID entityUuid;
   @Nonnull
   private final IntSet volumeTagIndexes;

   public TriggerVolumeEvent(
      @Nonnull String worldName,
      @Nonnull TriggerEventType triggerEventType,
      @Nonnull VolumeEntry volume,
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull UUID entityUuid
   ) {
      this.worldName = worldName;
      this.triggerEventType = triggerEventType;
      this.volumeId = volume.getId();
      this.entityRef = entityRef;
      this.entityUuid = entityUuid;
      this.volumeTagIndexes = volume.getExpandedTagIndexes();
   }

   @Nonnull
   public String getWorldName() {
      return this.worldName;
   }

   @Nonnull
   public TriggerEventType getTriggerEventType() {
      return this.triggerEventType;
   }

   @Nonnull
   public String getVolumeId() {
      return this.volumeId;
   }

   @Nonnull
   public Ref<EntityStore> getEntityRef() {
      return this.entityRef;
   }

   @Nonnull
   public UUID getEntityUuid() {
      return this.entityUuid;
   }

   @Nonnull
   public IntSet getVolumeTagIndexes() {
      return this.volumeTagIndexes;
   }
}
