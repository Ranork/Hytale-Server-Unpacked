package com.hypixel.hytale.common.collection;

import com.hypixel.hytale.common.util.StringUtil;
import javax.annotation.Nonnull;

public class Flags<T extends Flag> {
   private int flags;

   public Flags(@Nonnull T flag) {
      this.set(flag, true);
   }

   @SafeVarargs
   public Flags(@Nonnull T... flags) {
      for (T flag : flags) {
         this.set(flag, true);
      }
   }

   public Flags(int flags) {
      this.flags = flags;
   }

   public int getFlags() {
      return this.flags;
   }

   public boolean is(@Nonnull T flag) {
      return (this.flags & flag.mask()) != 0;
   }

   public boolean not(@Nonnull T flag) {
      return (this.flags & flag.mask()) == 0;
   }

   public boolean set(@Nonnull T flag, boolean value) {
      return value ? this.flags != (this.flags = this.flags | flag.mask()) : this.flags != (this.flags = this.flags & ~flag.mask());
   }

   public boolean toggle(@Nonnull T flag) {
      return ((this.flags = this.flags ^ flag.mask()) & flag.mask()) != 0;
   }

   @Nonnull
   @Override
   public String toString() {
      return StringUtil.toPaddedBinaryString(this.flags);
   }
}
