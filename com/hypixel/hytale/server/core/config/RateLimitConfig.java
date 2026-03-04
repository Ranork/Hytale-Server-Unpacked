package com.hypixel.hytale.server.core.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RateLimitConfig {
   public static final int DEFAULT_PACKETS_PER_SECOND = 2000;
   public static final int DEFAULT_BURST_CAPACITY = 500;
   @Nonnull
   public static final Codec<RateLimitConfig> CODEC = BuilderCodec.builder(RateLimitConfig.class, RateLimitConfig::new)
      .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN), (o, b) -> o.enabled = b, o -> o.enabled)
      .documentation("Determines whether packet rate limiting is enabled.")
      .add()
      .<Integer>append(new KeyedCodec<>("PacketsPerSecond", Codec.INTEGER), (o, i) -> o.packetsPerSecond = i, o -> o.packetsPerSecond)
      .documentation("The number of packets allowed per second.")
      .add()
      .<Integer>append(new KeyedCodec<>("BurstCapacity", Codec.INTEGER), (o, i) -> o.burstCapacity = i, o -> o.burstCapacity)
      .documentation("The number of packets that can be sent in a burst before rate limiting is applied.")
      .add()
      .build();
   private Boolean enabled;
   private Integer packetsPerSecond;
   private Integer burstCapacity;
   @Nullable
   transient HytaleServerConfig hytaleServerConfig;

   public RateLimitConfig() {
   }

   public RateLimitConfig(@Nonnull HytaleServerConfig hytaleServerConfig) {
      this.hytaleServerConfig = hytaleServerConfig;
   }

   public void setHytaleServerConfig(@Nonnull HytaleServerConfig hytaleServerConfig) {
      this.hytaleServerConfig = hytaleServerConfig;
   }

   public boolean isEnabled() {
      return this.enabled != null ? this.enabled : true;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      if (this.hytaleServerConfig != null) {
         this.hytaleServerConfig.markChanged();
      }
   }

   public int getPacketsPerSecond() {
      return this.packetsPerSecond != null ? this.packetsPerSecond : 2000;
   }

   public void setPacketsPerSecond(int packetsPerSecond) {
      this.packetsPerSecond = packetsPerSecond;
      if (this.hytaleServerConfig != null) {
         this.hytaleServerConfig.markChanged();
      }
   }

   public int getBurstCapacity() {
      return this.burstCapacity != null ? this.burstCapacity : 500;
   }

   public void setBurstCapacity(int burstCapacity) {
      this.burstCapacity = burstCapacity;
      if (this.hytaleServerConfig != null) {
         this.hytaleServerConfig.markChanged();
      }
   }
}
