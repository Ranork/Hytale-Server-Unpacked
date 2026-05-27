package com.hypixel.hytale.server.npc.sensorinfo;

import com.hypixel.hytale.server.npc.movement.controllers.BlockHit;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockCollisionProvider extends InfoProviderBase {
   @Nullable
   private List<BlockHit> hits;
   private int hitCount;

   public boolean populate(@Nonnull List<BlockHit> hits, int hitCount) {
      this.hits = hits;
      this.hitCount = hitCount;
      return this.hitCount > 0;
   }

   public void clear() {
      this.hits = null;
      this.hitCount = 0;
   }

   public int getHitCount() {
      return this.hits == null ? 0 : this.hitCount;
   }

   @Nonnull
   public BlockHit getHit(int i) {
      if (this.hits != null && i >= 0 && i < this.hitCount) {
         return this.hits.get(i);
      } else {
         throw new IndexOutOfBoundsException("block hit index " + i + " out of bounds for size " + this.getHitCount());
      }
   }

   @Nullable
   @Override
   public IPositionProvider getPositionProvider() {
      return null;
   }

   @Override
   public boolean hasPosition() {
      return false;
   }
}
