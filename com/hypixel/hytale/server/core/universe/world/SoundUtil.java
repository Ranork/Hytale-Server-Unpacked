package com.hypixel.hytale.server.core.universe.world;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.ItemSoundEvent;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.world.PlaySoundEvent2D;
import com.hypixel.hytale.protocol.packets.world.PlaySoundEvent3D;
import com.hypixel.hytale.protocol.packets.world.PlaySoundEventEntity;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.itemsound.config.ItemSoundSet;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoundUtil {
   public static void playItemSoundEvent(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Item item, @Nonnull ItemSoundEvent itemSoundEvent
   ) {
      ItemSoundSet soundSet = ItemSoundSet.getAssetMap().getAsset(item.getItemSoundSetIndex());
      if (soundSet != null) {
         String soundEventId = soundSet.getSoundEventIds().get(itemSoundEvent);
         if (soundEventId != null) {
            int soundEventIndex = SoundEvent.getAssetMap().getIndex(soundEventId);
            if (soundEventIndex != 0) {
               playSoundEvent2d(ref, soundEventIndex, SoundCategory.UI, store);
            }
         }
      }
   }

   public static void playSoundEventEntity(int soundEventIndex, int networkId, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      playSoundEventEntity(soundEventIndex, networkId, 1.0F, 1.0F, componentAccessor);
   }

   public static void playSoundEventEntity(
      int soundEventIndex, int networkId, float volumeModifier, float pitchModifier, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (soundEventIndex != 0) {
         PlayerUtil.broadcastPacketToPlayers(componentAccessor, new PlaySoundEventEntity(soundEventIndex, networkId, volumeModifier, pitchModifier));
      }
   }

   public static void playSoundEvent2dToPlayer(@Nonnull PlayerRef playerRefComponent, int soundEventIndex, @Nonnull SoundCategory soundCategory) {
      playSoundEvent2dToPlayer(playerRefComponent, soundEventIndex, soundCategory, 1.0F, 1.0F);
   }

   public static void playSoundEvent2dToPlayer(
      @Nonnull PlayerRef playerRefComponent, int soundEventIndex, @Nonnull SoundCategory soundCategory, float volumeModifier, float pitchModifier
   ) {
      if (soundEventIndex != 0) {
         playerRefComponent.getPacketHandler().write(new PlaySoundEvent2D(soundEventIndex, soundCategory, volumeModifier, pitchModifier));
      }
   }

   public static void playSoundEvent2d(int soundEventIndex, @Nonnull SoundCategory soundCategory, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      playSoundEvent2d(soundEventIndex, soundCategory, 1.0F, 1.0F, componentAccessor);
   }

   public static void playSoundEvent2d(
      int soundEventIndex,
      @Nonnull SoundCategory soundCategory,
      float volumeModifier,
      float pitchModifier,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (soundEventIndex != 0) {
         PlayerUtil.broadcastPacketToPlayers(componentAccessor, new PlaySoundEvent2D(soundEventIndex, soundCategory, volumeModifier, pitchModifier));
      }
   }

   public static void playSoundEvent2d(
      @Nonnull Ref<EntityStore> ref, int soundEventIndex, @Nonnull SoundCategory soundCategory, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      playSoundEvent2d(ref, soundEventIndex, soundCategory, 1.0F, 1.0F, componentAccessor);
   }

   public static void playSoundEvent2d(
      @Nonnull Ref<EntityStore> ref,
      int soundEventIndex,
      @Nonnull SoundCategory soundCategory,
      float volumeModifier,
      float pitchModifier,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (soundEventIndex != 0) {
         PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());
         if (playerRefComponent != null) {
            playerRefComponent.getPacketHandler().write(new PlaySoundEvent2D(soundEventIndex, soundCategory, volumeModifier, pitchModifier));
         }
      }
   }

   public static void playSoundEvent3d(
      int soundEventIndex, @Nonnull SoundCategory soundCategory, double x, double y, double z, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      playSoundEvent3d(soundEventIndex, soundCategory, x, y, z, 1.0F, 1.0F, componentAccessor);
   }

   public static void playSoundEvent3d(
      int soundEventIndex,
      @Nonnull SoundCategory soundCategory,
      double x,
      double y,
      double z,
      float volumeModifier,
      float pitchModifier,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (soundEventIndex != 0) {
         SoundEvent soundEvent = SoundEvent.getAssetMap().getAsset(soundEventIndex);
         if (soundEvent != null) {
            PlaySoundEvent3D packet = new PlaySoundEvent3D(soundEventIndex, soundCategory, new Position(x, y, z), volumeModifier, pitchModifier);
            Vector3d position = new Vector3d(x, y, z);
            SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = componentAccessor.getResource(
               EntityModule.get().getPlayerSpatialResourceType()
            );
            ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
            playerSpatialResource.getSpatialStructure().collect(position, soundEvent.getMaxDistance(), results);
            ObjectListIterator var16 = results.iterator();

            while (var16.hasNext()) {
               Ref<EntityStore> playerRef = (Ref<EntityStore>)var16.next();
               if (playerRef != null && playerRef.isValid()) {
                  PlayerRef playerRefComponent = componentAccessor.getComponent(playerRef, PlayerRef.getComponentType());

                  assert playerRefComponent != null;

                  playerRefComponent.getPacketHandler().write(packet);
               }
            }
         }
      }
   }

   public static void playSoundEvent3d(
      int soundEventIndex, @Nonnull SoundCategory soundCategory, @Nonnull Vector3d position, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      playSoundEvent3d(soundEventIndex, soundCategory, position.getX(), position.getY(), position.getZ(), componentAccessor);
   }

   public static void playSoundEvent3d(
      int soundEventIndex,
      @Nonnull SoundCategory soundCategory,
      double x,
      double y,
      double z,
      @Nonnull Predicate<Ref<EntityStore>> shouldHear,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      playSoundEvent3d(soundEventIndex, soundCategory, x, y, z, 1.0F, 1.0F, shouldHear, componentAccessor);
   }

   public static void playSoundEvent3d(
      int soundEventIndex,
      @Nonnull SoundCategory soundCategory,
      double x,
      double y,
      double z,
      float volumeModifier,
      float pitchModifier,
      @Nonnull Predicate<Ref<EntityStore>> shouldHear,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (soundEventIndex != 0) {
         SoundEvent soundEvent = SoundEvent.getAssetMap().getAsset(soundEventIndex);
         if (soundEvent != null) {
            PlaySoundEvent3D packet = new PlaySoundEvent3D(soundEventIndex, soundCategory, new Position(x, y, z), volumeModifier, pitchModifier);
            SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = componentAccessor.getResource(
               EntityModule.get().getPlayerSpatialResourceType()
            );
            ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
            playerSpatialResource.getSpatialStructure().collect(new Vector3d(x, y, z), soundEvent.getMaxDistance(), results);
            ObjectListIterator var16 = results.iterator();

            while (var16.hasNext()) {
               Ref<EntityStore> playerRef = (Ref<EntityStore>)var16.next();
               if (playerRef != null && playerRef.isValid() && shouldHear.test(playerRef)) {
                  PlayerRef playerRefComponent = componentAccessor.getComponent(playerRef, PlayerRef.getComponentType());

                  assert playerRefComponent != null;

                  playerRefComponent.getPacketHandler().write(packet);
               }
            }
         }
      }
   }

   public static void playSoundEvent3d(
      @Nullable Ref<EntityStore> sourceRef, int soundEventIndex, @Nonnull Vector3d pos, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      playSoundEvent3d(sourceRef, soundEventIndex, pos.getX(), pos.getY(), pos.getZ(), componentAccessor);
   }

   public static void playSoundEvent3d(
      @Nullable Ref<EntityStore> sourceRef, int soundEventIndex, double x, double y, double z, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      playSoundEvent3d(sourceRef, soundEventIndex, x, y, z, false, componentAccessor);
   }

   public static void playSoundEvent3d(
      @Nullable Ref<EntityStore> sourceRef,
      int soundEventIndex,
      @Nonnull Vector3d position,
      boolean ignoreSource,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      playSoundEvent3d(sourceRef, soundEventIndex, position.getX(), position.getY(), position.getZ(), ignoreSource, componentAccessor);
   }

   public static void playSoundEvent3d(
      @Nullable Ref<EntityStore> sourceRef,
      int soundEventIndex,
      double x,
      double y,
      double z,
      boolean ignoreSource,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Entity sourceEntity = sourceRef != null ? EntityUtils.getEntity(sourceRef, componentAccessor) : null;
      playSoundEvent3d(soundEventIndex, x, y, z, playerRef -> {
         if (sourceEntity == null) {
            return true;
         } else {
            return ignoreSource && sourceRef.equals(playerRef) ? false : !sourceEntity.isHiddenFromLivingEntity(sourceRef, playerRef, componentAccessor);
         }
      }, componentAccessor);
   }

   public static void playSoundEvent3d(
      int soundEventIndex,
      double x,
      double y,
      double z,
      @Nonnull Predicate<Ref<EntityStore>> shouldHear,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      playSoundEvent3d(soundEventIndex, SoundCategory.SFX, x, y, z, shouldHear, componentAccessor);
   }

   public static void playSoundEvent3dToPlayer(
      @Nullable Ref<EntityStore> playerRef,
      int soundEventIndex,
      @Nonnull SoundCategory soundCategory,
      double x,
      double y,
      double z,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      playSoundEvent3dToPlayer(playerRef, soundEventIndex, soundCategory, x, y, z, 1.0F, 1.0F, componentAccessor);
   }

   public static void playSoundEvent3dToPlayer(
      @Nullable Ref<EntityStore> playerRef,
      int soundEventIndex,
      @Nonnull SoundCategory soundCategory,
      double x,
      double y,
      double z,
      float volumeModifier,
      float pitchModifier,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (playerRef != null && soundEventIndex != 0) {
         SoundEvent soundEventAsset = SoundEvent.getAssetMap().getAsset(soundEventIndex);
         if (soundEventAsset != null) {
            float maxDistance = soundEventAsset.getMaxDistance();
            TransformComponent transformComponent = componentAccessor.getComponent(playerRef, TransformComponent.getComponentType());

            assert transformComponent != null;

            if (transformComponent.getPosition().distanceSquaredTo(x, y, z) <= maxDistance * maxDistance) {
               PlayerRef playerRefComponent = componentAccessor.getComponent(playerRef, PlayerRef.getComponentType());

               assert playerRefComponent != null;

               playerRefComponent.getPacketHandler()
                  .write(new PlaySoundEvent3D(soundEventIndex, soundCategory, new Position(x, y, z), volumeModifier, pitchModifier));
            }
         }
      }
   }

   public static void playSoundEvent3dToPlayer(
      @Nullable Ref<EntityStore> playerRef,
      int soundEventIndex,
      @Nonnull SoundCategory soundCategory,
      @Nonnull Vector3d position,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      playSoundEvent3dToPlayer(playerRef, soundEventIndex, soundCategory, position.x, position.y, position.z, componentAccessor);
   }
}
