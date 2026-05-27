package com.hypixel.hytale.server.core.asset.type.blocktype.config.fallingblocks;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import javax.annotation.Nullable;

public class FallingBlockSettings {
   public static final FallingBlockSettings DEFAULT = new FallingBlockSettings();
   public static final BuilderCodec<FallingBlockSettings> CODEC = BuilderCodec.builder(FallingBlockSettings.class, FallingBlockSettings::new)
      .appendInherited(
         new KeyedCodec<>("Impact", FallingBlockImpact.CODEC, true),
         (fallingBlockSettings, impact) -> fallingBlockSettings.impact = impact,
         fallingBlockSettings -> fallingBlockSettings.impact,
         (fallingBlockSettings, parent) -> fallingBlockSettings.impact = parent.impact
      )
      .documentation("The FallingBlockImpact that should be applied to falling block entities when they hit the ground.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("HitboxCollisionConfig", Codec.STRING),
         (fallingBlock, s) -> fallingBlock.hitboxCollisionConfigId = s,
         fallingBlock -> fallingBlock.hitboxCollisionConfigId,
         (playerConfig, parent) -> playerConfig.hitboxCollisionConfigId = parent.hitboxCollisionConfigId
      )
      .documentation("The HitboxCollision config to apply to falling block entities.")
      .addValidator(HitboxCollisionConfig.VALIDATOR_CACHE.getValidator())
      .add()
      .build();
   private FallingBlockImpact impact;
   @Nullable
   private String hitboxCollisionConfigId;

   protected FallingBlockSettings() {
   }

   public FallingBlockImpact getImpact() {
      return this.impact;
   }

   @Nullable
   public String getHitboxCollisionConfigId() {
      return this.hitboxCollisionConfigId;
   }
}
