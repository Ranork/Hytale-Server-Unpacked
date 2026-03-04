package com.hypixel.hytale.server.core.universe.world.spawn;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public interface ISpawnProvider {
   @Nonnull
   BuilderCodecMapCodec<ISpawnProvider> CODEC;

   default Transform getSpawnPoint(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      UUIDComponent uuidComponent = componentAccessor.getComponent(ref, UUIDComponent.getComponentType());
      if (!<unrepresentable>.$assertionsDisabled && uuidComponent == null) {
         throw new AssertionError();
      } else {
         World world = componentAccessor.getExternalData().getWorld();
         return this.getSpawnPoint(world, uuidComponent.getUuid());
      }
   }

   @Deprecated(forRemoval = true)
   default Transform getSpawnPoint(@Nonnull Entity entity) {
      return this.getSpawnPoint(entity.getWorld(), entity.getUuid());
   }

   Transform getSpawnPoint(@Nonnull World var1, @Nonnull UUID var2);

   @Deprecated
   Transform[] getSpawnPoints();

   boolean isWithinSpawnDistance(@Nonnull Vector3d var1, double var2);

   static {
      if (<unrepresentable>.$assertionsDisabled) {
      }

      CODEC = new BuilderCodecMapCodec<>(true);
   }
}
