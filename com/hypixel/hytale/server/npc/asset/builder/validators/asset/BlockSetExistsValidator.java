package com.hypixel.hytale.server.npc.asset.builder.validators.asset;

import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class BlockSetExistsValidator extends AssetValidator {
   private static final BlockSetExistsValidator DEFAULT_INSTANCE = new BlockSetExistsValidator();

   private BlockSetExistsValidator() {
   }

   private BlockSetExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "BlockSet";
   }

   @Override
   public boolean test(String blockSet) {
      return BlockSet.getAssetMap().getAsset(blockSet) != null;
   }

   @Nonnull
   @Override
   public String errorMessage(String blockSet, String attribute) {
      return "The block set with the name \"" + blockSet + "\" does not exist in attribute \"" + attribute + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return BlockSet.class.getSimpleName();
   }

   public static BlockSetExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static BlockSetExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new BlockSetExistsValidator(config);
   }
}
