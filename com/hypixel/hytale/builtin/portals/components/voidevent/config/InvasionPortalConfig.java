package com.hypixel.hytale.builtin.portals.components.voidevent.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InvasionPortalConfig {
   @Nonnull
   public static final BuilderCodec<InvasionPortalConfig> CODEC = BuilderCodec.builder(InvasionPortalConfig.class, InvasionPortalConfig::new)
      .append(new KeyedCodec<>("BlockKey", Codec.STRING), (config, o) -> config.blockKey = o, config -> config.blockKey)
      .documentation("The block used for evil portals that spawn around the world during the event")
      .add()
      .append(new KeyedCodec<>("SpawnBeacons", Codec.STRING_ARRAY), (config, o) -> config.spawnBeacons = o, config -> config.spawnBeacons)
      .add()
      .documentation("An array of SpawnBeacon IDs, which will make mobs spawn around the evil portals")
      .<String>append(new KeyedCodec<>("OnSpawnParticles", Codec.STRING), (config, o) -> config.onSpawnParticles = o, config -> config.onSpawnParticles)
      .documentation("A particle system ID to spawn when the portal spawns, should be a temporary one.")
      .add()
      .build();
   private String blockKey;
   private String[] spawnBeacons;
   private String onSpawnParticles;

   public String getBlockKey() {
      return this.blockKey;
   }

   @Nullable
   public BlockType getBlockType() {
      return BlockType.getAssetMap().getAsset(this.blockKey);
   }

   @Nullable
   public String getOnSpawnParticles() {
      return this.onSpawnParticles;
   }

   @Nullable
   public String[] getSpawnBeacons() {
      return this.spawnBeacons;
   }

   @Nonnull
   public List<String> getSpawnBeaconsList() {
      return this.spawnBeacons == null ? Collections.emptyList() : Arrays.asList(this.spawnBeacons);
   }
}
