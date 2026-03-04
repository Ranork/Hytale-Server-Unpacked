package com.hypixel.hytale.server.core.prefab.selection.buffer.impl;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nullable;

public class PrefabBufferBlockEntry {
   public static final PrefabBufferBlockEntry[] EMPTY_ARRAY = new PrefabBufferBlockEntry[0];
   public final int y;
   public String blockTypeKey;
   public int blockId;
   public float chance;
   @Nullable
   public Holder<ChunkStore> state;
   public int fluidId;
   public byte fluidLevel;
   public byte supportValue;
   public int filler;
   public int rotation;

   public PrefabBufferBlockEntry(int y) {
      this(y, 0, "Empty");
   }

   public PrefabBufferBlockEntry(int y, int blockId, String blockTypeKey) {
      this(y, blockId, blockTypeKey, 1.0F);
   }

   public PrefabBufferBlockEntry(int y, int blockId, String blockTypeKey, float chance) {
      this(y, blockId, blockTypeKey, chance, null, 0, (byte)0, (byte)0, 0, 0);
   }

   public PrefabBufferBlockEntry(
      int y,
      int blockId,
      String blockTypeKey,
      float chance,
      Holder<ChunkStore> state,
      int fluidId,
      byte fluidLevel,
      byte supportValue,
      int rotation,
      int filler
   ) {
      this.y = y;
      this.blockId = blockId;
      this.blockTypeKey = blockTypeKey;
      this.chance = chance;
      this.state = state;
      this.fluidId = fluidId;
      this.fluidLevel = fluidLevel;
      this.supportValue = supportValue;
      this.rotation = rotation;
      this.filler = filler;
   }
}
