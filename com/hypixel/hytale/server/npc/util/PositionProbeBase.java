package com.hypixel.hytale.server.npc.util;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.collision.BoxBlockIntersectionEvaluator;
import com.hypixel.hytale.server.core.modules.collision.CollisionFilter;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.collision.WorldUtil;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PositionProbeBase {
   private static final int lastWaterCheckDistanceMinSquared = 25;
   protected boolean touchCeil;
   protected boolean onGround;
   protected boolean inWater;
   protected boolean validPosition = true;
   protected int groundLevel = -1;
   protected int waterLevel = -1;
   protected int heightOverGround = -1;
   protected int heightOverWater = -1;
   protected int heightOverSurface = -1;
   protected int depthBelowSurface = -1;
   private int lastWaterCheckX = Integer.MAX_VALUE;
   private int lastWaterCheckZ = Integer.MAX_VALUE;
   private int lastWaterCheckLevel = -2;

   protected <T> boolean probePosition(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Box boundingBox,
      @Nonnull Vector3d position,
      @Nonnull CollisionResult collisionResult,
      @Nonnull T t,
      @Nonnull CollisionFilter<BoxBlockIntersectionEvaluator, T> blockTest,
      int materialSet,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      World world = componentAccessor.getExternalData().getWorld();
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      ChunkStore chunkStore = world.getChunkStore();
      long chunkIndex = ChunkUtil.indexChunkFromBlock(position.x, position.z);
      Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
      this.reset();
      if (chunkRef != null && chunkRef.isValid()) {
         int x = MathUtil.floor(position.x);
         int y = MathUtil.floor(position.y);
         int z = MathUtil.floor(position.z);
         this.validPosition = CollisionModule.get().validatePosition(world, boundingBox, position, materialSet, t, blockTest, collisionResult) != -1;
         Store<ChunkStore> chunkStoreAccessor = chunkStore.getStore();
         ChunkColumn chunkColumnComponent = chunkStoreAccessor.getComponent(chunkRef, ChunkColumn.getComponentType());
         BlockChunk blockChunkComponent = chunkStoreAccessor.getComponent(chunkRef, BlockChunk.getComponentType());
         if (chunkColumnComponent != null && blockChunkComponent != null) {
            this.groundLevel = blockChunkComponent.getHeight(x, z);
            this.waterLevel = this.updateWaterLevel(chunkStoreAccessor, chunkColumnComponent, blockChunkComponent, x, z);
         } else {
            this.groundLevel = -1;
            this.waterLevel = -1;
            this.lastWaterCheckLevel = -2;
         }

         this.heightOverGround = y - this.groundLevel;
         if (this.waterLevel < this.groundLevel) {
            this.heightOverSurface = this.heightOverGround;
         } else if (y > this.waterLevel) {
            this.heightOverSurface = y - this.waterLevel;
         } else {
            this.depthBelowSurface = this.waterLevel - y;
         }

         return this.validPosition;
      } else {
         this.waterLevel = -1;
         this.groundLevel = -1;
         this.lastWaterCheckLevel = -2;
         return false;
      }
   }

   protected int updateWaterLevel(
      @Nonnull ComponentAccessor<ChunkStore> chunkStore, @Nonnull ChunkColumn chunkColumn, @Nonnull BlockChunk blockChunk, int x, int z
   ) {
      if (this.lastWaterCheckLevel < -1 || this.movedFarEnough(x, z)) {
         this.lastWaterCheckX = x;
         this.lastWaterCheckZ = z;
         this.lastWaterCheckLevel = WorldUtil.getWaterLevel(chunkStore, chunkColumn, blockChunk, x, z, this.groundLevel);
      }

      return this.lastWaterCheckLevel;
   }

   private boolean movedFarEnough(int x, int z) {
      x -= this.lastWaterCheckX;
      z -= this.lastWaterCheckZ;
      return x * x + z * z > 25;
   }

   protected void reset() {
      this.touchCeil = false;
      this.onGround = false;
      this.inWater = false;
      this.validPosition = true;
      this.heightOverGround = -1;
      this.heightOverSurface = -1;
      this.heightOverWater = -1;
      this.depthBelowSurface = -1;
   }

   public boolean isValidPosition() {
      return this.validPosition;
   }

   public boolean isTouchCeil() {
      return this.touchCeil;
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public boolean isInWater() {
      return this.inWater;
   }

   public int getGroundLevel() {
      return this.groundLevel;
   }

   public int getWaterLevel() {
      return this.waterLevel;
   }

   public int getHeightOverGround() {
      return this.heightOverGround;
   }

   public int getHeightOverSurface() {
      return this.heightOverSurface;
   }

   public int getDepthBelowSurface() {
      return this.depthBelowSurface;
   }

   public int getHeightOverWater() {
      return this.heightOverWater;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PositionProbeBase{touchCeil="
         + this.touchCeil
         + ", onGround="
         + this.onGround
         + ", inWater="
         + this.inWater
         + ", validPosition="
         + this.validPosition
         + ", groundLevel="
         + this.groundLevel
         + ", waterLevel="
         + this.waterLevel
         + ", heightOverGround="
         + this.heightOverGround
         + ", heightOverSurface="
         + this.heightOverSurface
         + ", depthBelowSurface="
         + this.depthBelowSurface
         + ", heightOverWater="
         + this.heightOverWater
         + ", lastWaterCheckX="
         + this.lastWaterCheckX
         + ", lastWaterCheckZ="
         + this.lastWaterCheckZ
         + ", lastWaterCheckLevel="
         + this.lastWaterCheckLevel
         + "}";
   }
}
