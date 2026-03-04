package com.hypixel.hytale.server.npc.asset.builder.validators.asset;

import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class RoleExistsValidator extends AssetValidator {
   private static final RoleExistsValidator DEFAULT_INSTANCE = new RoleExistsValidator();

   private RoleExistsValidator() {
   }

   private RoleExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "Role";
   }

   @Override
   public boolean test(String role) {
      return NPCPlugin.get().hasRoleName(role);
   }

   @Nonnull
   @Override
   public String errorMessage(String role, String attributeName) {
      return "The Role with the name \"" + role + "\" does not exist for attribute \"" + attributeName + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return "NPCRole";
   }

   public static RoleExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static RoleExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new RoleExistsValidator(config);
   }
}
