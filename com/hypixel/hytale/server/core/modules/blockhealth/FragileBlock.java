package com.hypixel.hytale.server.core.modules.blockhealth;

import com.hypixel.hytale.server.core.util.io.MemorySegmentUtil;
import java.lang.foreign.MemorySegment;
import javax.annotation.Nonnull;

public class FragileBlock implements Cloneable {
   public static final int BYTE_SIZE = 4;
   private float durationSeconds;

   public FragileBlock(float durationSeconds) {
      this.durationSeconds = durationSeconds;
   }

   public FragileBlock() {
   }

   public float getDurationSeconds() {
      return this.durationSeconds;
   }

   public void setDurationSeconds(float durationSeconds) {
      this.durationSeconds = durationSeconds;
   }

   public void deserialize(@Nonnull MemorySegment data, int offset, byte version) {
      this.durationSeconds = data.get(MemorySegmentUtil.FLOAT_BE, (long)offset);
   }

   public void serialize(@Nonnull MemorySegment data, int offset) {
      data.set(MemorySegmentUtil.FLOAT_BE, (long)offset, this.durationSeconds);
   }

   @Nonnull
   protected FragileBlock clone() {
      return new FragileBlock(this.durationSeconds);
   }

   @Nonnull
   @Override
   public String toString() {
      return "FragileBlock{durationSeconds=" + this.durationSeconds + "}";
   }
}
