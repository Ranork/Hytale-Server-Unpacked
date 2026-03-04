package com.hypixel.hytale.server.worldgen.util.condition;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.condition.IBlockFluidCondition;
import it.unimi.dsi.fastutil.longs.LongSet;
import javax.annotation.Nonnull;

public class HashSetBlockFluidCondition implements IBlockFluidCondition {
   protected final LongSet set;

   public HashSetBlockFluidCondition(LongSet set) {
      this.set = set;
   }

   public LongSet getSet() {
      return this.set;
   }

   @Override
   public boolean eval(int block, int fluid) {
      return this.set.contains(MathUtil.packLong(block, fluid));
   }

   @Nonnull
   @Override
   public String toString() {
      return "HashSetIntCondition{set=" + this.set + "}";
   }
}
