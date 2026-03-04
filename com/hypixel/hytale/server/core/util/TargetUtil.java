package com.hypixel.hytale.server.core.util;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.function.predicate.BiIntPredicate;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.math.iterator.BlockIterator;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.collision.CollisionMath;
import com.hypixel.hytale.server.core.modules.collision.WorldUtil;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.IntPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class TargetUtil {
   private static final float ENTITY_TARGET_RADIUS = 8.0F;

   @Nullable
   public static Vector3i getTargetBlock(
      @Nonnull World world,
      @Nonnull BiIntPredicate blockIdPredicate,
      double originX,
      double originY,
      double originZ,
      double directionX,
      double directionY,
      double directionZ,
      double maxDistance
   ) {
      TargetUtil.TargetBuffer buffer = new TargetUtil.TargetBuffer(world);
      buffer.updateChunk((int)originX, (int)originZ);
      boolean success = BlockIterator.iterate(
         originX, originY, originZ, directionX, directionY, directionZ, maxDistance, (x, y, z, px, py, pz, qx, qy, qz, iBuffer) -> {
            if (y >= 0 && y < 320) {
               iBuffer.updateChunk(x, z);
               if (iBuffer.currentBlockChunk != null && iBuffer.currentChunkColumn != null) {
                  iBuffer.x = x;
                  iBuffer.y = y;
                  iBuffer.z = z;
                  BlockSection blockSection = iBuffer.currentBlockChunk.getSectionAtBlockY(y);
                  int blockId = blockSection.get(x, y, z);
                  int fluidId = WorldUtil.getFluidIdAtPosition(iBuffer.chunkStoreAccessor, iBuffer.currentChunkColumn, x, y, z);
                  return !blockIdPredicate.test(blockId, fluidId);
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }, buffer
      );
      return success ? null : new Vector3i(buffer.x, buffer.y, buffer.z);
   }

   @Nullable
   public static Vector3d getTargetLocation(
      @Nonnull World world,
      @Nonnull IntPredicate blockIdPredicate,
      double originX,
      double originY,
      double originZ,
      double directionX,
      double directionY,
      double directionZ,
      double maxDistance
   ) {
      TargetUtil.TargetBufferLocation buffer = new TargetUtil.TargetBufferLocation(world);
      buffer.updateChunk((int)originX, (int)originZ);
      boolean success = BlockIterator.iterate(
         originX, originY, originZ, directionX, directionY, directionZ, maxDistance, (x, y, z, px, py, pz, qx, qy, qz, iBuffer) -> {
            if (y >= 0 && y < 320) {
               iBuffer.updateChunk(x, z);
               if (iBuffer.currentBlockChunk == null) {
                  return false;
               } else {
                  iBuffer.x = x + px;
                  iBuffer.y = y + py;
                  iBuffer.z = z + pz;
                  BlockSection blockSection = iBuffer.currentBlockChunk.getSectionAtBlockY(y);
                  int blockId = blockSection.get(x, y, z);
                  return !blockIdPredicate.test(blockId);
               }
            } else {
               return false;
            }
         }, buffer
      );
      return success ? null : new Vector3d(buffer.x, buffer.y, buffer.z);
   }

   @Nullable
   public static Vector3i getTargetBlockAvoidLocations(
      @Nonnull World world,
      @Nonnull IntPredicate blockIdPredicate,
      double originX,
      double originY,
      double originZ,
      double directionX,
      double directionY,
      double directionZ,
      double maxDistance,
      @Nonnull LinkedList<LongOpenHashSet> blocksToIgnore
   ) {
      TargetUtil.TargetBuffer buffer = new TargetUtil.TargetBuffer(world);
      buffer.updateChunk((int)originX, (int)originZ);
      boolean success = BlockIterator.iterate(
         originX, originY, originZ, directionX, directionY, directionZ, maxDistance, (x, y, z, px, py, pz, qx, qy, qz, iBuffer) -> {
            if (y >= 0 && y < 320) {
               iBuffer.updateChunk(x, z);
               if (iBuffer.currentBlockChunk == null) {
                  return false;
               } else {
                  iBuffer.x = x;
                  iBuffer.y = y;
                  iBuffer.z = z;
                  BlockSection blockSection = iBuffer.currentBlockChunk.getSectionAtBlockY(y);
                  int blockId = blockSection.get(x, y, z);
                  if (blockId != 0) {
                     long packedBlockLocation = BlockUtil.pack(x, y, z);

                     for (LongOpenHashSet locations : blocksToIgnore) {
                        if (locations.contains(packedBlockLocation)) {
                           return true;
                        }
                     }
                  }

                  return !blockIdPredicate.test(blockId);
               }
            } else {
               return false;
            }
         }, buffer
      );
      return success ? null : new Vector3i(buffer.x, buffer.y, buffer.z);
   }

   @Nullable
   public static Vector3i getTargetBlock(@Nonnull Ref<EntityStore> ref, double maxDistance, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return getTargetBlock(ref, blockId -> blockId != 0, maxDistance, componentAccessor);
   }

   @Nullable
   public static Vector3i getTargetBlock(
      @Nonnull Ref<EntityStore> ref, @Nonnull IntPredicate blockIdPredicate, double maxDistance, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      World world = componentAccessor.getExternalData().getWorld();
      Transform transform = getLook(ref, componentAccessor);
      Vector3d pos = transform.getPosition();
      Vector3d dir = transform.getDirection();
      return getTargetBlock(world, (id, _fluidId) -> blockIdPredicate.test(id), pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, maxDistance);
   }

   @Nullable
   public static Vector3d getTargetLocation(@Nonnull Ref<EntityStore> ref, double maxDistance, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return getTargetLocation(ref, blockId -> blockId != 0, maxDistance, componentAccessor);
   }

   @Nullable
   public static Vector3d getTargetLocation(
      @Nonnull Ref<EntityStore> ref, @Nonnull IntPredicate blockIdPredicate, double maxDistance, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Transform transform = getLook(ref, componentAccessor);
      return getTargetLocation(transform, blockIdPredicate, maxDistance, componentAccessor);
   }

   @Nullable
   public static Vector3d getTargetLocation(
      @Nonnull Transform transform, @Nonnull IntPredicate blockIdPredicate, double maxDistance, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      World world = componentAccessor.getExternalData().getWorld();
      Vector3d pos = transform.getPosition();
      Vector3d dir = transform.getDirection();
      return getTargetLocation(world, blockIdPredicate, pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, maxDistance);
   }

   @Nonnull
   public static List<Ref<EntityStore>> getAllEntitiesInSphere(
      @Nonnull Vector3d position, double radius, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
      SpatialResource<Ref<EntityStore>, EntityStore> entitySpatialResource = componentAccessor.getResource(EntityModule.get().getEntitySpatialResourceType());
      entitySpatialResource.getSpatialStructure().collect(position, (float)radius, results);
      SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = componentAccessor.getResource(EntityModule.get().getPlayerSpatialResourceType());
      playerSpatialResource.getSpatialStructure().collect(position, (float)radius, results);
      return results;
   }

   @Nonnull
   public static List<Ref<EntityStore>> getAllEntitiesInCylinder(
      @Nonnull Vector3d position, double radius, double height, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
      SpatialResource<Ref<EntityStore>, EntityStore> entitySpatialResource = componentAccessor.getResource(EntityModule.get().getEntitySpatialResourceType());
      entitySpatialResource.getSpatialStructure().collectCylinder(position, radius, height, results);
      SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = componentAccessor.getResource(EntityModule.get().getPlayerSpatialResourceType());
      playerSpatialResource.getSpatialStructure().collectCylinder(position, radius, height, results);
      return results;
   }

   @Nonnull
   public static List<Ref<EntityStore>> getAllEntitiesInBox(
      @Nonnull Vector3d min, @Nonnull Vector3d max, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
      SpatialResource<Ref<EntityStore>, EntityStore> entitySpatialResource = componentAccessor.getResource(EntityModule.get().getEntitySpatialResourceType());
      entitySpatialResource.getSpatialStructure().collectBox(min, max, results);
      SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = componentAccessor.getResource(EntityModule.get().getPlayerSpatialResourceType());
      playerSpatialResource.getSpatialStructure().collectBox(min, max, results);
      return results;
   }

   @Nullable
   public static Ref<EntityStore> getTargetEntity(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return getTargetEntity(ref, 8.0F, componentAccessor);
   }

   @Nullable
   public static Ref<EntityStore> getTargetEntity(@Nonnull Ref<EntityStore> ref, float radius, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d transformPosition = transformComponent.getPosition();
      Transform lookVec = getLook(ref, componentAccessor);
      Vector3d position = lookVec.getPosition();
      Vector3d direction = lookVec.getDirection();
      List<Ref<EntityStore>> targetEntities = getAllEntitiesInSphere(position, radius, componentAccessor);
      targetEntities.removeIf(
         targetRefx -> targetRefx != null && targetRefx.isValid() && !targetRefx.equals(ref)
            ? !isHitByRay(targetRefx, position, direction, componentAccessor)
            : true
      );
      if (targetEntities.isEmpty()) {
         return null;
      } else {
         Ref<EntityStore> closest = null;
         double minDist2 = Double.MAX_VALUE;

         for (Ref<EntityStore> targetRef : targetEntities) {
            if (targetRef != null && targetRef.isValid()) {
               TransformComponent targetTransformComponent = componentAccessor.getComponent(targetRef, TransformComponent.getComponentType());

               assert targetTransformComponent != null;

               double distance = transformPosition.distanceSquaredTo(targetTransformComponent.getPosition());
               if (distance < minDist2) {
                  minDist2 = distance;
                  closest = targetRef;
               }
            }
         }

         return closest;
      }
   }

   @Nonnull
   public static Transform getLook(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      float eyeHeight = 0.0F;
      ModelComponent modelComponent = componentAccessor.getComponent(ref, ModelComponent.getComponentType());
      if (modelComponent != null) {
         eyeHeight = modelComponent.getModel().getEyeHeight(ref, componentAccessor);
      }

      HeadRotation headRotationComponent = componentAccessor.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      Vector3d position = transformComponent.getPosition();
      Vector3f headRotation = headRotationComponent.getRotation();
      return new Transform(
         position.getX(), position.getY() + eyeHeight, position.getZ(), headRotation.getPitch(), headRotation.getYaw(), headRotation.getRoll()
      );
   }

   private static boolean isHitByRay(
      @Nonnull Ref<EntityStore> ref, @Nonnull Vector3d rayStart, @Nonnull Vector3d rayDir, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      BoundingBox boundingBoxComponent = componentAccessor.getComponent(ref, BoundingBox.getComponentType());
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      if (boundingBoxComponent == null) {
         return false;
      } else {
         Box boundingBox = boundingBoxComponent.getBoundingBox();
         Vector3d position = transformComponent.getPosition();
         Vector2d minMax = new Vector2d();
         return CollisionMath.intersectRayAABB(rayStart, rayDir, position.getX(), position.getY(), position.getZ(), boundingBox, minMax);
      }
   }

   private static class TargetBuffer {
      @Nonnull
      private final World world;
      @Nonnull
      private final ComponentAccessor<ChunkStore> chunkStoreAccessor;
      private int x;
      private int y;
      private int z;
      private int currentChunkX;
      private int currentChunkZ;
      @Nullable
      private Ref<ChunkStore> currentChunkRef;
      @Nullable
      private ChunkColumn currentChunkColumn;
      @Nullable
      private BlockChunk currentBlockChunk;

      public TargetBuffer(@Nonnull World world) {
         this.world = world;
         this.chunkStoreAccessor = world.getChunkStore().getStore();
      }

      public void updateChunk(int blockX, int blockZ) {
         int chunkX = ChunkUtil.chunkCoordinate(blockX);
         int chunkZ = ChunkUtil.chunkCoordinate(blockZ);
         if (this.currentChunkRef == null || chunkX != this.currentChunkX || chunkZ != this.currentChunkZ) {
            this.currentChunkX = chunkX;
            this.currentChunkZ = chunkZ;
            long chunkIndex = ChunkUtil.indexChunk(chunkX, chunkZ);
            this.currentChunkRef = this.world.getChunkStore().getChunkReference(chunkIndex);
            if (this.currentChunkRef != null && this.currentChunkRef.isValid()) {
               this.currentChunkColumn = this.chunkStoreAccessor.getComponent(this.currentChunkRef, ChunkColumn.getComponentType());
               this.currentBlockChunk = this.chunkStoreAccessor.getComponent(this.currentChunkRef, BlockChunk.getComponentType());
            } else {
               this.currentChunkColumn = null;
               this.currentBlockChunk = null;
            }
         }
      }
   }

   private static class TargetBufferLocation {
      @Nonnull
      public final World world;
      @Nonnull
      public final ComponentAccessor<ChunkStore> chunkStoreAccessor;
      private double x;
      private double y;
      private double z;
      private int currentChunkX;
      private int currentChunkZ;
      @Nullable
      public Ref<ChunkStore> currentChunkRef;
      @Nullable
      public BlockChunk currentBlockChunk;

      public TargetBufferLocation(@Nonnull World world) {
         this.world = world;
         this.chunkStoreAccessor = world.getChunkStore().getStore();
      }

      public void updateChunk(int blockX, int blockZ) {
         int chunkX = ChunkUtil.chunkCoordinate(blockX);
         int chunkZ = ChunkUtil.chunkCoordinate(blockZ);
         if (this.currentChunkRef == null || chunkX != this.currentChunkX || chunkZ != this.currentChunkZ) {
            this.currentChunkX = chunkX;
            this.currentChunkZ = chunkZ;
            long chunkIndex = ChunkUtil.indexChunk(chunkX, chunkZ);
            this.currentChunkRef = this.world.getChunkStore().getChunkReference(chunkIndex);
            if (this.currentChunkRef != null && this.currentChunkRef.isValid()) {
               this.currentBlockChunk = this.chunkStoreAccessor.getComponent(this.currentChunkRef, BlockChunk.getComponentType());
            } else {
               this.currentBlockChunk = null;
            }
         }
      }
   }
}
