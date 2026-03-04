package com.hypixel.hytale.server.core.universe.world;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.packets.world.SpawnParticleSystem;
import com.hypixel.hytale.server.core.asset.type.particle.config.WorldParticle;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ParticleUtil {
   public static final double DEFAULT_PARTICLE_DISTANCE = 75.0;

   public static void spawnParticleEffect(@Nonnull String name, @Nonnull Vector3d position, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = componentAccessor.getResource(EntityModule.get().getPlayerSpatialResourceType());
      ObjectList<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
      playerSpatialResource.getSpatialStructure().collect(position, 75.0, playerRefs);
      spawnParticleEffect(name, position.getX(), position.getY(), position.getZ(), null, playerRefs, componentAccessor);
   }

   public static void spawnParticleEffect(
      @Nonnull String name, @Nonnull Vector3d position, @Nonnull List<Ref<EntityStore>> playerRefs, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      spawnParticleEffect(name, position.getX(), position.getY(), position.getZ(), null, playerRefs, componentAccessor);
   }

   public static void spawnParticleEffect(
      @Nonnull String name,
      @Nonnull Vector3d position,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull List<Ref<EntityStore>> playerRefs,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      spawnParticleEffect(name, position.getX(), position.getY(), position.getZ(), sourceRef, playerRefs, componentAccessor);
   }

   public static void spawnParticleEffect(
      @Nonnull String name,
      @Nonnull Vector3d position,
      @Nonnull Vector3f rotation,
      @Nonnull List<Ref<EntityStore>> playerRefs,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      spawnParticleEffect(
         name,
         position.getX(),
         position.getY(),
         position.getZ(),
         rotation.getYaw(),
         rotation.getPitch(),
         rotation.getRoll(),
         null,
         playerRefs,
         componentAccessor
      );
   }

   public static void spawnParticleEffect(
      @Nonnull String name,
      @Nonnull Vector3d position,
      @Nonnull Vector3f rotation,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull List<Ref<EntityStore>> playerRefs,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      spawnParticleEffect(
         name,
         position.getX(),
         position.getY(),
         position.getZ(),
         rotation.getYaw(),
         rotation.getPitch(),
         rotation.getRoll(),
         sourceRef,
         playerRefs,
         componentAccessor
      );
   }

   public static void spawnParticleEffect(
      String name,
      @Nonnull Vector3d position,
      float yaw,
      float pitch,
      float roll,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull List<Ref<EntityStore>> playerRefs,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      spawnParticleEffect(name, position.getX(), position.getY(), position.getZ(), yaw, pitch, roll, sourceRef, playerRefs, componentAccessor);
   }

   public static void spawnParticleEffect(
      @Nonnull String name,
      @Nonnull Vector3d position,
      float yaw,
      float pitch,
      float roll,
      float scale,
      @Nonnull Color color,
      @Nonnull List<Ref<EntityStore>> playerRefs,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      spawnParticleEffect(name, position.getX(), position.getY(), position.getZ(), yaw, pitch, roll, scale, color, null, playerRefs, componentAccessor);
   }

   public static void spawnParticleEffect(
      @Nonnull WorldParticle particles,
      @Nonnull Vector3d position,
      @Nonnull List<Ref<EntityStore>> playerRefs,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      spawnParticleEffect(particles, position, null, playerRefs, componentAccessor);
   }

   public static void spawnParticleEffect(
      @Nonnull WorldParticle particles,
      @Nonnull Vector3d position,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull List<Ref<EntityStore>> playerRefs,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      spawnParticleEffect(particles, position, 0.0F, 0.0F, 0.0F, sourceRef, playerRefs, componentAccessor);
   }

   public static void spawnParticleEffects(
      @Nonnull WorldParticle[] particles,
      @Nonnull Vector3d position,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull List<Ref<EntityStore>> playerRefs,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      for (WorldParticle particle : particles) {
         spawnParticleEffect(particle, position, sourceRef, playerRefs, componentAccessor);
      }
   }

   public static void spawnParticleEffect(
      @Nonnull WorldParticle particles,
      @Nonnull Vector3d position,
      float yaw,
      float pitch,
      float roll,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull List<Ref<EntityStore>> playerRefs,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      com.hypixel.hytale.protocol.Vector3f positionOffset = particles.getPositionOffset();
      if (positionOffset != null) {
         Vector3d offset = new Vector3d(positionOffset.x, positionOffset.y, positionOffset.z);
         offset.rotateY(yaw);
         offset.rotateX(pitch);
         offset.rotateZ(roll);
         position.x = position.x + offset.x;
         position.y = position.y + offset.y;
         position.z = position.z + offset.z;
      }

      Direction rotationOffset = particles.getRotationOffset();
      if (rotationOffset != null) {
         yaw += (float)Math.toRadians(rotationOffset.yaw);
         pitch += (float)Math.toRadians(rotationOffset.pitch);
         roll += (float)Math.toRadians(rotationOffset.roll);
      }

      String systemId = particles.getSystemId();
      if (systemId != null) {
         spawnParticleEffect(
            systemId,
            position.getX(),
            position.getY(),
            position.getZ(),
            yaw,
            pitch,
            roll,
            particles.getScale(),
            particles.getColor(),
            sourceRef,
            playerRefs,
            componentAccessor
         );
      }
   }

   public static void spawnParticleEffect(
      @Nonnull String name, double x, double y, double z, @Nonnull List<Ref<EntityStore>> playerRefs, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      spawnParticleEffect(name, x, y, z, null, playerRefs, componentAccessor);
   }

   public static void spawnParticleEffect(
      @Nonnull String name,
      double x,
      double y,
      double z,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull List<Ref<EntityStore>> playerRefs,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      spawnParticleEffect(name, x, y, z, 0.0F, 0.0F, 0.0F, 1.0F, null, sourceRef, playerRefs, componentAccessor);
   }

   public static void spawnParticleEffect(
      @Nonnull String name,
      double x,
      double y,
      double z,
      float rotationYaw,
      float rotationPitch,
      float rotationRoll,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull List<Ref<EntityStore>> playerRefs,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      spawnParticleEffect(name, x, y, z, rotationYaw, rotationPitch, rotationRoll, 1.0F, null, sourceRef, playerRefs, componentAccessor);
   }

   public static void spawnParticleEffect(
      @Nonnull String name,
      double x,
      double y,
      double z,
      float rotationYaw,
      float rotationPitch,
      float rotationRoll,
      float scale,
      @Nullable Color color,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull List<Ref<EntityStore>> playerRefs,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Direction rotation = null;
      if (rotationYaw != 0.0F || rotationPitch != 0.0F || rotationRoll != 0.0F) {
         rotation = new Direction(rotationYaw, rotationPitch, rotationRoll);
      }

      SpawnParticleSystem packet = new SpawnParticleSystem(name, new Position(x, y, z), rotation, scale, color);
      ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();

      for (Ref<EntityStore> playerRef : playerRefs) {
         if (playerRef.isValid() && (sourceRef == null || !playerRef.equals(sourceRef))) {
            PlayerRef playerRefComponent = componentAccessor.getComponent(playerRef, playerRefComponentType);

            assert playerRefComponent != null;

            playerRefComponent.getPacketHandler().writeNoCache(packet);
         }
      }
   }
}
