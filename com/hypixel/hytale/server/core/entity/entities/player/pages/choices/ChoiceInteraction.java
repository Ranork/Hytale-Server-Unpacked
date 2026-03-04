package com.hypixel.hytale.server.core.entity.entities.player.pages.choices;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public abstract class ChoiceInteraction {
   public static final CodecMapCodec<ChoiceInteraction> CODEC = new CodecMapCodec<>("Type");
   public static final BuilderCodec<ChoiceInteraction> BASE_CODEC = BuilderCodec.abstractBuilder(ChoiceInteraction.class).build();

   protected ChoiceInteraction() {
   }

   public abstract void run(@Nonnull Store<EntityStore> var1, @Nonnull Ref<EntityStore> var2, @Nonnull PlayerRef var3);

   @Nonnull
   @Override
   public String toString() {
      return "ChoiceInteraction{}";
   }
}
