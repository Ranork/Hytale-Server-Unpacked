package com.hypixel.hytale.builtin.hytalegenerator.material;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SolidMaterial {
   @Nonnull
   private final MaterialCache materialCache;
   public final int blockId;
   public final int support;
   public final int rotation;
   public final int filler;
   @Nullable
   public final Holder<ChunkStore> holder;

   SolidMaterial(@Nonnull MaterialCache materialCache, int blockId, int support, int rotation, int filler, @Nullable Holder<ChunkStore> holder) {
      this.materialCache = materialCache;
      this.blockId = blockId;
      this.support = support;
      this.rotation = rotation;
      this.filler = filler;
      this.holder = holder;
   }

   @Override
   public boolean equals(Object o) {
      return !(o instanceof SolidMaterial that)
         ? false
         : this.blockId == that.blockId
            && this.support == that.support
            && this.rotation == that.rotation
            && this.filler == that.filler
            && Objects.equals(this.materialCache, that.materialCache)
            && Objects.equals(this.holder, that.holder);
   }

   @Override
   public int hashCode() {
      return contentHash(this.blockId, this.support, this.rotation, this.filler, this.holder);
   }

   public static int contentHash(int blockId, int support, int rotation, int filler, @Nullable Holder<ChunkStore> holder) {
      return Objects.hash(blockId, support, rotation, filler, holder);
   }

   @Nonnull
   @Override
   public String toString() {
      return "SolidMaterial{materialCache="
         + this.materialCache
         + ", blockId="
         + this.blockId
         + ", support="
         + this.support
         + ", rotation="
         + this.rotation
         + ", filler="
         + this.filler
         + ", holder="
         + this.holder
         + "}";
   }
}
