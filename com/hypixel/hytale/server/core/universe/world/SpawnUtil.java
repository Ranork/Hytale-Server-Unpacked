package com.hypixel.hytale.server.core.universe.world;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class SpawnUtil {
   @Nullable
   public static TransformComponent applyFirstSpawnTransform(
      @Nonnull Holder<EntityStore> holder, @Nonnull World world, @Nonnull WorldConfig worldConfig, @Nonnull UUID playerUuid
   ) {
      ISpawnProvider spawnProvider = worldConfig.getSpawnProvider();
      if (spawnProvider == null) {
         return null;
      } else {
         Transform spawnPoint = spawnProvider.getSpawnPoint(world, playerUuid);
         Vector3f bodyRotation = new Vector3f(0.0F, spawnPoint.getRotation().getYaw(), 0.0F);
         TransformComponent transformComponent = new TransformComponent(spawnPoint.getPosition(), bodyRotation);
         holder.addComponent(TransformComponent.getComponentType(), transformComponent);
         HeadRotation headRotationComponent = holder.ensureAndGetComponent(HeadRotation.getComponentType());
         headRotationComponent.teleportRotation(spawnPoint.getRotation());
         return transformComponent;
      }
   }

   public static void applyTransform(@Nonnull Holder<EntityStore> holder, @Nonnull Transform transform) {
      TransformComponent transformComponent = holder.getComponent(TransformComponent.getComponentType());

      assert transformComponent != null;

      transformComponent.setPosition(transform.getPosition());
      transformComponent.getRotation().setYaw(transform.getRotation().getYaw());
      HeadRotation headRotationComponent = holder.ensureAndGetComponent(HeadRotation.getComponentType());
      headRotationComponent.teleportRotation(transform.getRotation());
   }
}
