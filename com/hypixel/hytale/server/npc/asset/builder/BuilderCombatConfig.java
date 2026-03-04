package com.hypixel.hytale.server.npc.asset.builder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.server.npc.config.balancing.BalanceAsset;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderCombatConfig extends BuilderCodecObjectHelper<String> {
   private boolean inline;

   public BuilderCombatConfig(Codec<String> codec, Validator<String> validator) {
      super(String.class, codec, validator);
   }

   public String build() {
      throw new UnsupportedOperationException();
   }

   @Override
   public void readConfig(@Nonnull JsonElement data, @Nonnull ExtraInfo extraInfo) {
      this.inline = data.isJsonObject();
      super.readConfig(data, extraInfo);
   }

   @Nullable
   public String build(@Nonnull ExecutionContext context) {
      String override = context.getCombatConfig();
      return override != null ? override : this.value;
   }

   public boolean validate(
      String configName, NPCLoadTimeValidationHelper loadTimeValidationHelper, @Nonnull ExecutionContext context, @Nonnull List<String> errors
   ) {
      String override = context.getCombatConfig();
      boolean success = true;
      if (override != null && BalanceAsset.getAssetMap().getAsset(override) == null) {
         errors.add(String.format("%s: CombatConfig refers to a non-existent balancing file: %s", configName, override));
         success = false;
      }

      return success;
   }
}
