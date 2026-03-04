package com.hypixel.hytale.server.core.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.semver.SemverRange;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModConfig {
   @Nonnull
   public static final BuilderCodec<ModConfig> CODEC = BuilderCodec.builder(ModConfig.class, ModConfig::new)
      .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN), (modConfig, enabled) -> modConfig.enabled = enabled, modConfig -> modConfig.enabled)
      .documentation("Determines whether the mod/plugin is enabled.")
      .add()
      .<SemverRange>append(
         new KeyedCodec<>("RequiredVersion", SemverRange.CODEC),
         (modConfig, semverRange) -> modConfig.requiredVersion = semverRange,
         modConfig -> modConfig.requiredVersion
      )
      .documentation("The required version range for the mod/plugin.")
      .add()
      .build();
   @Nullable
   private Boolean enabled;
   @Nullable
   private SemverRange requiredVersion;

   @Nullable
   public Boolean getEnabled() {
      return this.enabled;
   }

   public void setEnabled(@Nonnull Boolean enabled) {
      this.enabled = enabled;
   }

   @Nullable
   public SemverRange getRequiredVersion() {
      return this.requiredVersion;
   }

   public void setRequiredVersion(@Nonnull SemverRange requiredVersion) {
      this.requiredVersion = requiredVersion;
   }
}
