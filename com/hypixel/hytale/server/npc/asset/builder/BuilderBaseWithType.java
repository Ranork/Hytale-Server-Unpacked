package com.hypixel.hytale.server.npc.asset.builder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.spawning.ISpawnable;
import javax.annotation.Nonnull;

public abstract class BuilderBaseWithType<T> extends BuilderBase<T> implements ISpawnable {
   private String type;

   @Override
   public Builder<T> readCommonConfig(JsonElement data) {
      return super.readCommonConfig(data);
   }

   protected void readTypeKey(@Nonnull JsonElement data, String typeKey) {
      this.requireString(data, typeKey, s -> this.type = s, StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "Type field", null);
   }

   protected void readTypeKey(@Nonnull JsonElement data) {
      this.readTypeKey(data, "Type");
   }

   public String getType() {
      return this.type;
   }
}
