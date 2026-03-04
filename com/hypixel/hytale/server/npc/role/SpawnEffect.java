package com.hypixel.hytale.server.npc.role;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import javax.annotation.Nonnull;

public interface SpawnEffect {
   String getSpawnParticles();

   Vector3d getSpawnParticleOffset();

   double getSpawnViewDistance();

   default void spawnEffect(@Nonnull Vector3d position, @Nonnull Vector3f rotation, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      String particles = this.getSpawnParticles();
      if (particles != null && !particles.isEmpty()) {
         Vector3d spawnPosition = new Vector3d(0.0, 0.0, 0.0);
         if (this.getSpawnParticleOffset() != null) {
            spawnPosition.assign(this.getSpawnParticleOffset());
         }

         spawnPosition.rotateY(rotation.getYaw()).add(position);
         SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = componentAccessor.getResource(EntityModule.get().getPlayerSpatialResourceType());
         ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
         playerSpatialResource.getSpatialStructure().collect(spawnPosition, this.getSpawnViewDistance(), results);
         ParticleUtil.spawnParticleEffect(particles, spawnPosition, results, componentAccessor);
      }
   }
}
