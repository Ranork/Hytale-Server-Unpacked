package com.hypixel.hytale.builtin.hytalegenerator;

import com.hypixel.hytale.builtin.hytalegenerator.engine.performanceinstruments.MemInstrument;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import org.joml.Vector3i;

public class EntityPlacementData implements MemInstrument {
   private final Vector3i offset;
   private final PrefabRotation rotation;
   private final Holder<EntityStore> entityHolder;
   private final int prefabInstanceId;

   public EntityPlacementData(Vector3i offset, PrefabRotation rotation, Holder<EntityStore> entityHolder, int prefabInstanceId) {
      this.offset = offset;
      this.rotation = rotation;
      this.entityHolder = entityHolder;
      this.prefabInstanceId = prefabInstanceId;
   }

   public Vector3i getOffset() {
      return this.offset;
   }

   public PrefabRotation getRotation() {
      return this.rotation;
   }

   public Holder<EntityStore> getEntityHolder() {
      return this.entityHolder;
   }

   public int getPrefabInstanceId() {
      return this.prefabInstanceId;
   }

   @Nonnull
   @Override
   public MemInstrument.Report getMemoryUsage() {
      long size_bytes = 48L;
      return new MemInstrument.Report(48L);
   }
}
