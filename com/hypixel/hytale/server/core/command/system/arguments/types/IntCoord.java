package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public class IntCoord {
   private final int value;
   private final boolean height;
   private final boolean relative;
   private final boolean chunk;

   public IntCoord(int value, boolean height, boolean relative, boolean chunk) {
      this.value = value;
      this.height = height;
      this.relative = relative;
      this.chunk = chunk;
   }

   public int getValue() {
      return this.value;
   }

   public boolean isNotBase() {
      return !this.height && !this.relative && !this.chunk;
   }

   public boolean isHeight() {
      return this.height;
   }

   public boolean isRelative() {
      return this.relative;
   }

   public boolean isChunk() {
      return this.chunk;
   }

   public int resolveXZ(int base) {
      return this.resolve(base);
   }

   public int resolveYAtWorldCoords(int base, @Nonnull ChunkStore chunkStore, int x, int z) {
      if (this.height) {
         long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
         Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
         if (chunkRef != null && chunkRef.isValid()) {
            WorldChunk worldChunkComponent = chunkStore.getStore().getComponent(chunkRef, WorldChunk.getComponentType());

            assert worldChunkComponent != null;

            return this.resolve(worldChunkComponent.getHeight(x, z) + 1);
         } else {
            return this.resolve(base);
         }
      } else {
         return this.resolve(base);
      }
   }

   protected int resolve(int base) {
      int val = this.chunk ? this.value * 32 : this.value;
      return this.relative ? val + base : val;
   }

   @Nonnull
   public static IntCoord parse(@Nonnull String str) {
      boolean height = false;
      boolean relative = false;
      boolean chunk = false;
      int index = 0;

      while (true) {
         switch (str.charAt(index)) {
            case '_':
               index++;
               height = true;
               if (str.length() == index) {
                  return new IntCoord(0, true, relative, chunk);
               }
               break;
            case 'c':
               index++;
               chunk = true;
               if (str.length() == index) {
                  return new IntCoord(0, height, relative, true);
               }
               break;
            case '~':
               index++;
               relative = true;
               if (str.length() == index) {
                  return new IntCoord(0, height, true, chunk);
               }
               break;
            default:
               String rest = str.substring(index);
               return new IntCoord(Integer.parseInt(rest), height, relative, chunk);
         }
      }
   }
}
