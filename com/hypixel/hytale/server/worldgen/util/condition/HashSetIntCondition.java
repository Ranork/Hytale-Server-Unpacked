package com.hypixel.hytale.server.worldgen.util.condition;

import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import it.unimi.dsi.fastutil.ints.IntSet;
import javax.annotation.Nonnull;

public class HashSetIntCondition implements IIntCondition {
   protected final IntSet set;

   public HashSetIntCondition(IntSet set) {
      this.set = set;
   }

   public IntSet getSet() {
      return this.set;
   }

   @Override
   public boolean eval(int i) {
      return this.set.contains(i);
   }

   @Nonnull
   @Override
   public String toString() {
      return "HashSetIntCondition{set=" + this.set + "}";
   }
}
