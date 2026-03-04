package com.hypixel.hytale.server.core.modules.entity.item;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.NearestBlockUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.collision.CollisionMath;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemPrePhysicsSystem extends EntityTickingSystem<EntityStore> {
   public static final NearestBlockUtil.IterationElement[] SEARCH_ELEMENTS = new NearestBlockUtil.IterationElement[]{
      new NearestBlockUtil.IterationElement(-1, 0, 0, x -> 0.0, y -> y, z -> z),
      new NearestBlockUtil.IterationElement(1, 0, 0, x -> 1.0, y -> y, z -> z),
      new NearestBlockUtil.IterationElement(0, 0, -1, x -> x, y -> y, z -> 0.0),
      new NearestBlockUtil.IterationElement(0, 0, 1, x -> x, y -> y, z -> 1.0)
   };
   public static final double VERTICAL_CLIMB_SCALE = 7.0;
   @Nonnull
   private final ComponentType<EntityStore, BoundingBox> boundingBoxComponentType;
   @Nonnull
   private final ComponentType<EntityStore, Velocity> velocityComponentType;
   @Nonnull
   private final ComponentType<EntityStore, TransformComponent> transformComponentType;
   @Nonnull
   private final ComponentType<EntityStore, PhysicsValues> physicsValuesComponentType;
   @Nonnull
   private final Query<EntityStore> query;

   public ItemPrePhysicsSystem(
      @Nonnull ComponentType<EntityStore, ItemComponent> itemComponentType,
      @Nonnull ComponentType<EntityStore, BoundingBox> boundingBoxComponentType,
      @Nonnull ComponentType<EntityStore, Velocity> velocityComponentType,
      @Nonnull ComponentType<EntityStore, TransformComponent> transformComponentType,
      @Nonnull ComponentType<EntityStore, PhysicsValues> physicsValuesComponentType
   ) {
      this.physicsValuesComponentType = physicsValuesComponentType;
      this.boundingBoxComponentType = boundingBoxComponentType;
      this.transformComponentType = transformComponentType;
      this.velocityComponentType = velocityComponentType;
      this.query = Query.and(
         itemComponentType, TransformComponent.getComponentType(), boundingBoxComponentType, velocityComponentType, physicsValuesComponentType
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
      Velocity velocityComponent = archetypeChunk.getComponent(index, this.velocityComponentType);

      assert velocityComponent != null;

      TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

      assert transformComponent != null;

      PhysicsValues physicsValuesComponent = archetypeChunk.getComponent(index, this.physicsValuesComponentType);

      assert physicsValuesComponent != null;

      BoundingBox boundingBoxComponent = archetypeChunk.getComponent(index, this.boundingBoxComponentType);

      assert boundingBoxComponent != null;

      Box boundingBox = boundingBoxComponent.getBoundingBox();
      World world = commandBuffer.getExternalData().getWorld();
      ChunkStore chunkStore = world.getChunkStore();
      Ref<ChunkStore> chunkRef = transformComponent.getChunkRef();
      WorldChunk worldChunkComponent;
      if (chunkRef != null && chunkRef.isValid()) {
         worldChunkComponent = chunkStore.getStore().getComponent(chunkRef, WorldChunk.getComponentType());
      } else {
         worldChunkComponent = null;
      }

      moveOutOfBlock(worldChunkComponent, transformComponent.getPosition(), velocityComponent, boundingBox);
      applyGravity(dt, boundingBox, physicsValuesComponent, transformComponent.getPosition(), velocityComponent);
   }

   public static void moveOutOfBlock(@Nullable WorldChunk chunk, @Nonnull Vector3d position, @Nonnull Velocity velocityComponent, @Nonnull Box boundingBox) {
      if (chunk != null) {
         int x = MathUtil.floor(position.x);
         int y = MathUtil.floor(position.y);
         int z = MathUtil.floor(position.z);
         BlockType blockType = chunk.getBlockType(x, y, z);

         assert blockType != null;

         if (blockType.getMaterial() == BlockMaterial.Solid) {
            int rotation = chunk.getRotationIndex(x, y, z);
            BlockBoundingBoxes.RotatedVariantBoxes blockBoundingBoxes = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex()).get(rotation);
            boolean overlap = false;

            for (Box detailBox : blockBoundingBoxes.getDetailBoxes()) {
               if (CollisionMath.isOverlapping(CollisionMath.intersectAABBs(x, y, z, detailBox, position.x, position.y, position.z, boundingBox))) {
                  overlap = true;
                  break;
               }
            }

            if (overlap) {
               Vector3i nearestBlock = NearestBlockUtil.findNearestBlock(SEARCH_ELEMENTS, position, (block, _worldChunk) -> {
                  BlockType testBlockType = _worldChunk.getBlockType(block);
                  return testBlockType.getMaterial() != BlockMaterial.Solid;
               }, chunk);
               if (nearestBlock != null) {
                  position.assign(nearestBlock.x + 0.5, nearestBlock.y, nearestBlock.z + 0.5);
               } else {
                  velocityComponent.setY(7.0 * blockBoundingBoxes.getBoundingBox().height());
               }
            }
         }
      }
   }

   public static void applyGravity(float dt, @Nullable Box boundingBox, @Nonnull PhysicsValues values, @Nonnull Vector3d position, @Nonnull Velocity velocity) {
      double area = 1.0;
      if (boundingBox != null) {
         area = Math.abs(boundingBox.width() * boundingBox.depth());
      }

      double density = PhysicsMath.getRelativeDensity(position, boundingBox);
      double terminalVelocity = PhysicsMath.getTerminalVelocity(values.getMass(), density, area, values.getDragCoefficient());
      double gravityStep = PhysicsMath.getAcceleration(velocity.getY(), terminalVelocity) * dt;
      if (!values.isInvertedGravity()) {
         terminalVelocity *= -1.0;
         gravityStep *= -1.0;
      }

      if (velocity.getY() < terminalVelocity && gravityStep > 0.0) {
         velocity.setY(Math.min(velocity.getY() + gravityStep, terminalVelocity));
      } else if (velocity.getY() > terminalVelocity && gravityStep < 0.0) {
         velocity.setY(Math.max(velocity.getY() + gravityStep, terminalVelocity));
      }
   }
}
