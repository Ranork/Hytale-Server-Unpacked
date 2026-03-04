package com.hypixel.hytale.server.core.asset.type.gameplay;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementConfig;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.modules.entity.repulsion.RepulsionConfig;
import javax.annotation.Nonnull;

public class PlayerConfig {
   @Nonnull
   public static final BuilderCodec<PlayerConfig> CODEC = BuilderCodec.builder(PlayerConfig.class, PlayerConfig::new)
      .appendInherited(
         new KeyedCodec<>("HitboxCollisionConfig", Codec.STRING),
         (playerConfig, s) -> playerConfig.hitboxCollisionConfigId = s,
         playerConfig -> playerConfig.hitboxCollisionConfigId,
         (playerConfig, parent) -> playerConfig.hitboxCollisionConfigId = parent.hitboxCollisionConfigId
      )
      .documentation("The HitboxCollision config to apply to all players.")
      .addValidator(HitboxCollisionConfig.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("RepulsionConfig", Codec.STRING),
         (playerConfig, s) -> playerConfig.repulsionConfigId = s,
         playerConfig -> playerConfig.repulsionConfigId,
         (playerConfig, parent) -> playerConfig.repulsionConfigId = parent.repulsionConfigId
      )
      .documentation("The Repulsion to apply to all players.")
      .addValidator(RepulsionConfig.VALIDATOR_CACHE.getValidator())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("MovementConfig", Codec.STRING),
         (playerConfig, s) -> playerConfig.movementConfigId = s,
         playerConfig -> playerConfig.movementConfigId,
         (playerConfig, parent) -> playerConfig.movementConfigId = parent.movementConfigId
      )
      .addValidator(MovementConfig.VALIDATOR_CACHE.getValidator())
      .documentation("The maximum number of simultaneous deployable entities players are allowed to own.")
      .add()
      .appendInherited(
         new KeyedCodec<>("MaxDeployableEntities", Codec.INTEGER),
         (playerConfig, s) -> playerConfig.maxDeployableEntities = s,
         playerConfig -> playerConfig.maxDeployableEntities,
         (playerConfig, parent) -> playerConfig.maxDeployableEntities = parent.maxDeployableEntities
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ArmorVisibilityOption", new EnumCodec<>(PlayerConfig.ArmorVisibilityOption.class)),
         (playerConfig, armorVisibilityOption1) -> playerConfig.armorVisibilityOption = armorVisibilityOption1,
         playerConfig -> playerConfig.armorVisibilityOption,
         (playerConfig, parent) -> playerConfig.armorVisibilityOption = parent.armorVisibilityOption
      )
      .add()
      .afterDecode(playerConfig -> {
         if (playerConfig.hitboxCollisionConfigId != null) {
            playerConfig.hitboxCollisionConfigIndex = HitboxCollisionConfig.getAssetMap().getIndexOrDefault(playerConfig.hitboxCollisionConfigId, -1);
         }

         if (playerConfig.repulsionConfigId != null) {
            playerConfig.repulsionConfigIndex = RepulsionConfig.getAssetMap().getIndexOrDefault(playerConfig.repulsionConfigId, -1);
         }

         if (playerConfig.movementConfigId != null) {
            playerConfig.movementConfigIndex = MovementConfig.getAssetMap().getIndexOrDefault(playerConfig.movementConfigId, 0);
         }
      })
      .build();
   protected String hitboxCollisionConfigId;
   protected String repulsionConfigId;
   protected String movementConfigId = "BuiltinDefault";
   protected int hitboxCollisionConfigIndex = -1;
   protected int repulsionConfigIndex = -1;
   protected int movementConfigIndex = 0;
   protected int maxDeployableEntities = -1;
   protected PlayerConfig.ArmorVisibilityOption armorVisibilityOption = PlayerConfig.ArmorVisibilityOption.ALL;

   public int getHitboxCollisionConfigIndex() {
      return this.hitboxCollisionConfigIndex;
   }

   public int getRepulsionConfigIndex() {
      return this.repulsionConfigIndex;
   }

   public int getMovementConfigIndex() {
      return this.movementConfigIndex;
   }

   public String getMovementConfigId() {
      return this.movementConfigId;
   }

   public int getMaxDeployableEntities() {
      return this.maxDeployableEntities;
   }

   public PlayerConfig.ArmorVisibilityOption getArmorVisibilityOption() {
      return this.armorVisibilityOption;
   }

   public static enum ArmorVisibilityOption {
      ALL(true, true, true, true),
      HELMET_ONLY(true, false, false, false),
      NONE(false, false, false, false);

      private final boolean canHideHelmet;
      private final boolean canHideCuirass;
      private final boolean canHideGauntlets;
      private final boolean canHidePants;

      private ArmorVisibilityOption(boolean canHideHelmet, boolean canHideCuirass, boolean canHideGauntlets, boolean canHidePants) {
         this.canHideHelmet = canHideHelmet;
         this.canHideCuirass = canHideCuirass;
         this.canHideGauntlets = canHideGauntlets;
         this.canHidePants = canHidePants;
      }

      public boolean canHideHelmet() {
         return this.canHideHelmet;
      }

      public boolean canHideCuirass() {
         return this.canHideCuirass;
      }

      public boolean canHideGauntlets() {
         return this.canHideGauntlets;
      }

      public boolean canHidePants() {
         return this.canHidePants;
      }
   }
}
