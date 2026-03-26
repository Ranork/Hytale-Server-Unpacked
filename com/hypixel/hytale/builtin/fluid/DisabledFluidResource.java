package com.hypixel.hytale.builtin.fluid;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DisabledFluidResource implements Resource<ChunkStore> {
   @Nullable
   private Set<String> tags;
   @Nonnull
   private IntSet ids = IntSets.EMPTY_SET;

   @Nonnull
   public static ResourceType<ChunkStore, DisabledFluidResource> getResourceType() {
      return FluidPlugin.get().getDisabledFluidResourceType();
   }

   @Nonnull
   public IntSet getDisabledFluidIds(@Nonnull WorldConfig worldConfig) {
      Set<String> disabledTickers = worldConfig.getDisabledFluidTickers();
      if (disabledTickers.isEmpty()) {
         return IntSets.EMPTY_SET;
      } else if (this.tags == disabledTickers) {
         return this.ids;
      } else {
         IntSet resolved = FluidPlugin.resolveFluidIds(disabledTickers);
         this.tags = disabledTickers;
         this.ids = resolved;
         return resolved;
      }
   }

   public void invalidate() {
      this.tags = null;
      this.ids = IntSets.EMPTY_SET;
   }

   @Nonnull
   @Override
   public Resource<ChunkStore> clone() {
      return new DisabledFluidResource();
   }
}
