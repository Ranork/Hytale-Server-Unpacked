package com.hypixel.hytale.server.core.universe.world.chunk.section;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.LegacyModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public class ChunkSection implements Component<ChunkStore> {
   public static final BuilderCodec<ChunkSection> CODEC = BuilderCodec.builder(ChunkSection.class, ChunkSection::new).build();
   private Ref<ChunkStore> chunkColumnReference;
   private int x;
   private int y;
   private int z;

   public static ComponentType<ChunkStore, ChunkSection> getComponentType() {
      return LegacyModule.get().getChunkSectionComponentType();
   }

   private ChunkSection() {
   }

   public ChunkSection(Ref<ChunkStore> chunkColumnReference, int x, int y, int z) {
      this.chunkColumnReference = chunkColumnReference;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public void load(Ref<ChunkStore> chunkReference, int x, int y, int z) {
      this.chunkColumnReference = chunkReference;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Ref<ChunkStore> getChunkColumnReference() {
      return this.chunkColumnReference;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getZ() {
      return this.z;
   }

   @Nonnull
   @Override
   public Component<ChunkStore> clone() {
      return this;
   }
}
