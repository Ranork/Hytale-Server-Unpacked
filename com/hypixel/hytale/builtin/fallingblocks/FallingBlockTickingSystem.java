package com.hypixel.hytale.builtin.fallingblocks;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.fallingblocks.FallingBlockSettings;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemPrePhysicsSystem;
import com.hypixel.hytale.server.core.modules.physics.SimplePhysicsProvider;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class FallingBlockTickingSystem extends EntityTickingSystem<EntityStore> {
   @Nonnull
   private final ComponentType<EntityStore, FallingBlock> fallingBlockComponentType;
   @Nonnull
   private final ComponentType<EntityStore, TransformComponent> transformComponentType;
   @Nonnull
   private final ComponentType<EntityStore, HeadRotation> headRotationComponentType;
   @Nonnull
   private final ComponentType<EntityStore, Velocity> velocityComponentType;
   @Nonnull
   private final ComponentType<EntityStore, PhysicsValues> physicsValuesComponentType;
   @Nonnull
   private final ComponentType<EntityStore, BoundingBox> boundingBoxComponentType;
   @Nonnull
   private final ComponentType<EntityStore, BlockEntity> blockEntityComponentType;
   @Nonnull
   private final Query<EntityStore> query;

   public FallingBlockTickingSystem(
      @Nonnull ComponentType<EntityStore, FallingBlock> fallingBlockComponentType,
      @Nonnull ComponentType<EntityStore, TransformComponent> transformComponentType,
      @Nonnull ComponentType<EntityStore, HeadRotation> headRotationComponentType,
      @Nonnull ComponentType<EntityStore, Velocity> velocityComponentType,
      @Nonnull ComponentType<EntityStore, PhysicsValues> physicsValuesComponentType,
      @Nonnull ComponentType<EntityStore, BoundingBox> boundingBoxComponentType,
      @Nonnull ComponentType<EntityStore, BlockEntity> blockEntityComponentType
   ) {
      this.fallingBlockComponentType = fallingBlockComponentType;
      this.transformComponentType = transformComponentType;
      this.headRotationComponentType = headRotationComponentType;
      this.velocityComponentType = velocityComponentType;
      this.physicsValuesComponentType = physicsValuesComponentType;
      this.boundingBoxComponentType = boundingBoxComponentType;
      this.blockEntityComponentType = blockEntityComponentType;
      this.query = Query.and(
         fallingBlockComponentType,
         transformComponentType,
         headRotationComponentType,
         velocityComponentType,
         physicsValuesComponentType,
         boundingBoxComponentType,
         blockEntityComponentType
      );
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
      FallingBlock fallingBlockComponent = archetypeChunk.getComponent(index, this.fallingBlockComponentType);

      assert fallingBlockComponent != null;

      Velocity velocityComponent = archetypeChunk.getComponent(index, this.velocityComponentType);

      assert velocityComponent != null;

      TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

      assert transformComponent != null;

      PhysicsValues physicsValuesComponent = archetypeChunk.getComponent(index, this.physicsValuesComponentType);

      assert physicsValuesComponent != null;

      BoundingBox boundingBoxComponent = archetypeChunk.getComponent(index, this.boundingBoxComponentType);

      assert boundingBoxComponent != null;

      BlockEntity blockEntityComponent = archetypeChunk.getComponent(index, this.blockEntityComponentType);

      assert blockEntityComponent != null;

      World world = commandBuffer.getExternalData().getWorld();
      Vector3d position = transformComponent.getPosition();
      Box boundingBox = boundingBoxComponent.getBoundingBox();
      ItemPrePhysicsSystem.applyGravity(dt, boundingBox, physicsValuesComponent, position, velocityComponent);
      SimplePhysicsProvider physicsProvider = blockEntityComponent.getSimplePhysicsProvider();
      ChunkStore chunkStore = world.getChunkStore();
      Ref<ChunkStore> chunkRef = transformComponent.getChunkRef();
      if (chunkRef != null) {
         WorldChunk worldChunk = chunkStore.getStore().getComponent(chunkRef, WorldChunk.getComponentType());
         if (worldChunk != null) {
            if (physicsProvider.isOnGround()) {
               String blockTypeKey = blockEntityComponent.getBlockTypeKey();
               BlockType blockType = BlockType.getAssetMap().getAsset(blockTypeKey);
               HeadRotation headRotationComponent = archetypeChunk.getComponent(index, this.headRotationComponentType);

               assert headRotationComponent != null;

               if (blockType != null && blockType.getMaterial() != BlockMaterial.Empty) {
                  Rotation3f transformRotation = headRotationComponent.getRotation();
                  RotationTuple rotationTuple = RotationTuple.of(
                     Rotation.closestOfDegrees((float)Math.toDegrees(transformRotation.y - (float) Math.PI)),
                     Rotation.closestOfDegrees((float)Math.toDegrees(transformRotation.x)),
                     Rotation.closestOfDegrees((float)Math.toDegrees(transformRotation.z))
                  );
                  FallingBlockSettings fallingBlockSettings = fallingBlockComponent.getFallingBlockSettings();
                  if (fallingBlockSettings != null && fallingBlockSettings.getImpact() != null) {
                     fallingBlockSettings.getImpact().apply(worldChunk, world, blockType, position, rotationTuple, store);
                  } else {
                     PlaceFallingBlockImpact.placeFallingBlock(worldChunk, world, blockType, position, rotationTuple, store);
                  }
               }

               commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
            }
         }
      }
   }
}
