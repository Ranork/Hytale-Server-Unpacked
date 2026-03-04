package com.hypixel.hytale.server.core.modules.blockhealth;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;

public class FragileBlock implements Cloneable {
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

   public void deserialize(@Nonnull ByteBuf buf, byte version) {
      this.durationSeconds = buf.readFloat();
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloat(this.durationSeconds);
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
