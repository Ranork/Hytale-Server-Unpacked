package com.hypixel.hytale.server.core.asset.type.portalworld;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import javax.annotation.Nullable;

public class PortalSpawnConfig {
   public static final BuilderCodec<PortalSpawnConfig> CODEC = BuilderCodec.builder(PortalSpawnConfig.class, PortalSpawnConfig::new)
      .append(new KeyedCodec<>("SpawnReturnPortal", Codec.BOOLEAN), (config, o) -> config.spawnReturnPortal = o, config -> config.spawnReturnPortal)
      .documentation("Whether to spawn the return (usually a portal block) on the spawn location within the fragment instance.")
      .add()
      .<ISpawnProvider>append(
         new KeyedCodec<>("SpawnProviderOverride", ISpawnProvider.CODEC),
         (config, o) -> config.spawnProviderOverride = o,
         config -> config.spawnProviderOverride
      )
      .documentation(
         "Set a spawn provider which will override the world's spawn provider. The spawn returned from this spawn provider will be the exact spawn location for the fragment."
      )
      .add()
      .<String>append(new KeyedCodec<>("ReturnBlock", Codec.STRING), (config, o) -> config.returnBlockId = o, config -> config.returnBlockId)
      .documentation("Overrides the block to use as a return portal for this portal type.")
      .addValidatorLate(() -> BlockType.VALIDATOR_CACHE.getValidator().late())
      .add()
      .build();
   private boolean spawnReturnPortal = true;
   private ISpawnProvider spawnProviderOverride = null;
   private String returnBlockId;

   public boolean isSpawningReturnPortal() {
      return this.spawnReturnPortal;
   }

   public ISpawnProvider getSpawnProviderOverride() {
      return this.spawnProviderOverride;
   }

   public String getReturnBlockOverrideId() {
      return this.returnBlockId;
   }

   @Nullable
   public BlockType getReturnBlockOverride() {
      return this.returnBlockId == null ? null : BlockType.getAssetMap().getAsset(this.returnBlockId);
   }
}
