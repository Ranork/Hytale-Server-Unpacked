package com.hypixel.hytale.server.core.modules.block;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class BlockReplaceEvent extends EcsEvent {
   private final Ref<ChunkStore> chunkRef;
   private int selfX;
   private int selfY;
   private int selfZ;
   private final Holder<ChunkStore> newEntity;
   private final int baseX;
   private final int baseY;
   private final int baseZ;

   public BlockReplaceEvent(Ref<ChunkStore> chunkRef, int selfX, int selfY, int selfZ, Holder<ChunkStore> newEntity, int baseX, int baseY, int baseZ) {
      this.chunkRef = chunkRef;
      this.selfX = selfX;
      this.selfY = selfY;
      this.selfZ = selfZ;
      this.newEntity = newEntity;
      this.baseX = baseX;
      this.baseY = baseY;
      this.baseZ = baseZ;
   }

   public Ref<ChunkStore> getChunkRef() {
      return this.chunkRef;
   }

   public int getSelfX() {
      return this.selfX;
   }

   public int getSelfY() {
      return this.selfY;
   }

   public int getSelfZ() {
      return this.selfZ;
   }

   public Holder<ChunkStore> getNewEntity() {
      return this.newEntity;
   }

   public void next(int selfX, int selfY, int selfZ) {
      this.selfX = selfX;
      this.selfY = selfY;
      this.selfZ = selfZ;
   }
}
