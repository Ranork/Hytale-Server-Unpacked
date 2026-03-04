package com.hypixel.hytale.server.core.universe.world.worldgen;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.entity.component.FromWorldGen;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.universe.world.chunk.EntityChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GeneratedEntityChunk {
   private final List<GeneratedEntityChunk.EntityWrapperEntry> entities;

   public GeneratedEntityChunk() {
      this(new ObjectArrayList());
   }

   protected GeneratedEntityChunk(List<GeneratedEntityChunk.EntityWrapperEntry> entities) {
      this.entities = entities;
   }

   public List<GeneratedEntityChunk.EntityWrapperEntry> getEntities() {
      return this.entities;
   }

   public void forEachEntity(Consumer<GeneratedEntityChunk.EntityWrapperEntry> consumer) {
      this.entities.forEach(consumer);
   }

   public void addEntities(Vector3i offset, PrefabRotation rotation, @Nullable Holder<EntityStore>[] entityHolders, int objectId) {
      if (entityHolders != null && entityHolders.length > 0) {
         this.entities.add(new GeneratedEntityChunk.EntityWrapperEntry(offset, rotation, entityHolders, objectId));
      }
   }

   @Nonnull
   public EntityChunk toEntityChunk() {
      EntityChunk entityChunk = new EntityChunk();

      for (GeneratedEntityChunk.EntityWrapperEntry entry : this.entities) {
         FromWorldGen fromWorldGen = new FromWorldGen(entry.worldgenId());

         for (Holder<EntityStore> entityHolder : entry.entityHolders()) {
            TransformComponent transformComponent = entityHolder.getComponent(TransformComponent.getComponentType());

            assert transformComponent != null;

            entry.rotation().rotate(transformComponent.getPosition().subtract(0.5, 0.0, 0.5));
            transformComponent.getPosition().add(0.5, 0.0, 0.5);
            HeadRotation headRotationComponent = entityHolder.getComponent(HeadRotation.getComponentType());
            if (headRotationComponent != null) {
               headRotationComponent.getRotation().addYaw(-entry.rotation().getYaw());
            }

            transformComponent.getRotation().addYaw(-entry.rotation().getYaw());
            transformComponent.getPosition().add(entry.offset());
            entityHolder.putComponent(FromWorldGen.getComponentType(), fromWorldGen);
            entityChunk.storeEntityHolder(entityHolder);
         }
      }

      return entityChunk;
   }

   public record EntityWrapperEntry(Vector3i offset, PrefabRotation rotation, Holder<EntityStore>[] entityHolders, int worldgenId) {
      @Nonnull
      @Override
      public String toString() {
         return "EntityWrapperEntry{offset="
            + this.offset
            + ", rotation="
            + this.rotation
            + ", entityHolders="
            + Arrays.toString((Object[])this.entityHolders)
            + "}";
      }
   }
}
