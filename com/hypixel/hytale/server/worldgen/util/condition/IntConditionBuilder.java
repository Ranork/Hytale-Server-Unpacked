package com.hypixel.hytale.server.worldgen.util.condition;

import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class IntConditionBuilder implements IntConsumer {
   private final Supplier<IntSet> setSupplier;
   private final int nullValue;
   private int first;
   @Nullable
   private IntSet set = null;

   public IntConditionBuilder(Supplier<IntSet> setSupplier, int nullValue) {
      this.setSupplier = setSupplier;
      this.nullValue = nullValue;
      this.first = nullValue;
   }

   public void accept(int value) {
      this.add(value);
   }

   public boolean add(int value) {
      if (value == this.first || value == this.nullValue) {
         return false;
      } else if (this.first == this.nullValue) {
         this.first = value;
         return true;
      } else {
         if (this.set == null) {
            this.set = this.setSupplier.get();
            this.set.add(this.first);
         }

         return this.set.add(value);
      }
   }

   public IIntCondition buildOrDefault(IIntCondition defaultCondition) {
      if (this.first == this.nullValue) {
         return defaultCondition;
      } else {
         IntSet set = this.set;
         if (set == null) {
            set = IntSets.singleton(this.first);
         }

         return new HashSetIntCondition(set);
      }
   }
}
