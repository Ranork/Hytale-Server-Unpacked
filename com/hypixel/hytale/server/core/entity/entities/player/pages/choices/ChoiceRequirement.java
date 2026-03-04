package com.hypixel.hytale.server.core.entity.entities.player.pages.choices;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public abstract class ChoiceRequirement {
   public static final CodecMapCodec<ChoiceRequirement> CODEC = new CodecMapCodec<>("Type");
   public static final BuilderCodec<ChoiceRequirement> BASE_CODEC = BuilderCodec.abstractBuilder(ChoiceRequirement.class).build();

   protected ChoiceRequirement() {
   }

   public abstract boolean canFulfillRequirement(@Nonnull Store<EntityStore> var1, @Nonnull Ref<EntityStore> var2, @Nonnull PlayerRef var3);

   @Nonnull
   @Override
   public String toString() {
      return "ChoiceRequirement{}";
   }
}
