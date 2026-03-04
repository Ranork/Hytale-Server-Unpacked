package com.hypixel.hytale.server.npc.asset.builder.validators.asset;

import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class EntityStatExistsValidator extends AssetValidator {
   private static final EntityStatExistsValidator DEFAULT_INSTANCE = new EntityStatExistsValidator();

   private EntityStatExistsValidator() {
   }

   private EntityStatExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "EntityStat";
   }

   @Override
   public boolean test(String entityStat) {
      return EntityStatType.getAssetMap().getAsset(entityStat) != null;
   }

   @Nonnull
   @Override
   public String errorMessage(String entityStat, String attributeName) {
      return "The entity stat with the name \"" + entityStat + "\" does not exist for attribute \"" + attributeName + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return EntityStatType.class.getSimpleName();
   }

   public static EntityStatExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static EntityStatExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new EntityStatExistsValidator(config);
   }
}
