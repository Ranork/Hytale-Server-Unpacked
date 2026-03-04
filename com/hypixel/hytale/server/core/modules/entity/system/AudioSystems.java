package com.hypixel.hytale.server.core.modules.entity.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.AudioUpdate;
import com.hypixel.hytale.protocol.BlockSoundEvent;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.blocksound.config.BlockSoundSet;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.AudioComponent;
import com.hypixel.hytale.server.core.modules.entity.component.MovementAudioComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PositionDataComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AudioSystems {
   public static class EntityTrackerUpdate extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType = EntityTrackerSystems.Visible.getComponentType();
      private final ComponentType<EntityStore, AudioComponent> audioComponentType = AudioComponent.getComponentType();
      private final Query<EntityStore> query = Query.and(this.visibleComponentType, this.audioComponentType);

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return EntityTrackerSystems.QUEUE_UPDATE_GROUP;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         EntityTrackerSystems.Visible visibleComponent = archetypeChunk.getComponent(index, this.visibleComponentType);

         assert visibleComponent != null;

         AudioComponent audioComponent = archetypeChunk.getComponent(index, this.audioComponentType);

         assert audioComponent != null;

         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         if (audioComponent.consumeNetworkOutdated()) {
            queueUpdatesFor(ref, audioComponent, visibleComponent.visibleTo);
         } else if (!visibleComponent.newlyVisibleTo.isEmpty()) {
            queueUpdatesFor(ref, audioComponent, visibleComponent.newlyVisibleTo);
         }
      }

      private static void queueUpdatesFor(
         @Nonnull Ref<EntityStore> ref, @Nonnull AudioComponent audioComponent, @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo
      ) {
         AudioUpdate update = new AudioUpdate(audioComponent.getSoundEventIds());

         for (Entry<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> entry : visibleTo.entrySet()) {
            entry.getValue().queueUpdate(ref, update);
         }
      }
   }

   public static class TickMovementAudio extends EntityTickingSystem<EntityStore> {
      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return Query.and(
            TransformComponent.getComponentType(),
            PositionDataComponent.getComponentType(),
            MovementAudioComponent.getComponentType(),
            MovementStatesComponent.getComponentType()
         );
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         TransformComponent transformComponent = archetypeChunk.getComponent(index, TransformComponent.getComponentType());

         assert transformComponent != null;

         PositionDataComponent positionDataComponent = archetypeChunk.getComponent(index, PositionDataComponent.getComponentType());

         assert positionDataComponent != null;

         MovementAudioComponent movementAudioComponent = archetypeChunk.getComponent(index, MovementAudioComponent.getComponentType());

         assert movementAudioComponent != null;

         int insideBlockTypeId = positionDataComponent.getInsideBlockTypeId();
         int lastInsideBlockTypeId = movementAudioComponent.getLastInsideBlockTypeId();
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         Vector3d position = transformComponent.getPosition();
         if (lastInsideBlockTypeId != insideBlockTypeId) {
            movementAudioComponent.setLastInsideBlockTypeId(insideBlockTypeId);
            playMoveInSound(ref, store, movementAudioComponent, position, insideBlockTypeId);
            if (lastInsideBlockTypeId != 0) {
               BlockType blockType = BlockType.getAssetMap().getAsset(lastInsideBlockTypeId);
               int soundSetId = blockType.getBlockSoundSetIndex();
               if (soundSetId != 0) {
                  BlockSoundSet soundSet = BlockSoundSet.getAssetMap().getAsset(soundSetId);
                  int soundEvent = soundSet.getSoundEventIndices().getOrDefault(BlockSoundEvent.MoveOut, 0);
                  if (soundEvent != 0) {
                     SoundUtil.playSoundEvent3d(
                        soundEvent, SoundCategory.SFX, position.x, position.y, position.z, movementAudioComponent.getShouldHearPredicate(ref), commandBuffer
                     );
                  }
               }
            }
         }

         MovementStatesComponent movementStates = archetypeChunk.getComponent(index, MovementStatesComponent.getComponentType());

         assert movementStates != null;

         if (!movementStates.getMovementStates().idle && movementAudioComponent.canMoveInRepeat() && movementAudioComponent.tickMoveInRepeat(dt)) {
            playMoveInSound(ref, commandBuffer, movementAudioComponent, position, insideBlockTypeId);
         }
      }

      private static void playMoveInSound(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull ComponentAccessor<EntityStore> store,
         @Nonnull MovementAudioComponent movementAudioComponent,
         @Nonnull Vector3d position,
         int insideBlockTypeId
      ) {
         movementAudioComponent.setNextMoveInRepeat(MovementAudioComponent.NO_REPEAT);
         if (insideBlockTypeId != 0) {
            BlockType blockType = BlockType.getAssetMap().getAsset(insideBlockTypeId);
            int soundSetId = blockType.getBlockSoundSetIndex();
            if (soundSetId != 0) {
               BlockSoundSet soundSet = BlockSoundSet.getAssetMap().getAsset(soundSetId);
               int soundEvent = soundSet.getSoundEventIndices().getOrDefault(BlockSoundEvent.MoveIn, 0);
               if (soundEvent != 0) {
                  SoundUtil.playSoundEvent3d(
                     soundEvent, SoundCategory.SFX, position.x, position.y, position.z, movementAudioComponent.getShouldHearPredicate(ref), store
                  );
                  movementAudioComponent.setNextMoveInRepeat(
                     RandomExtra.randomRange(soundSet.getMoveInRepeatRange().getInclusiveMin(), soundSet.getMoveInRepeatRange().getInclusiveMax())
                  );
               }
            }
         }
      }
   }
}
