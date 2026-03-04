package com.hypixel.hytale.server.core.modules.interaction.interaction.util;

import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum InteractionTarget {
   USER,
   OWNER,
   TARGET;

   @Nonnull
   public static final EnumCodec<InteractionTarget> CODEC = new EnumCodec<>(InteractionTarget.class)
      .documentKey(USER, "Causes the interaction to target the entity that triggered/owns the interaction chain.")
      .documentKey(OWNER, "Causes the interaction to target the entity that owns the interaction chain.")
      .documentKey(TARGET, "Causes the interaction to target the entity that is the target of the interaction chain.");

   @Nullable
   public Ref<EntityStore> getEntity(@Nonnull InteractionContext ctx, @Nonnull Ref<EntityStore> ref) {
      return switch (this) {
         case USER -> ctx.getEntity();
         case OWNER -> ctx.getOwningEntity();
         case TARGET -> ctx.getTargetEntity();
      };
   }

   @Nonnull
   public com.hypixel.hytale.protocol.InteractionTarget toProtocol() {
      return switch (this) {
         case USER -> com.hypixel.hytale.protocol.InteractionTarget.User;
         case OWNER -> com.hypixel.hytale.protocol.InteractionTarget.Owner;
         case TARGET -> com.hypixel.hytale.protocol.InteractionTarget.Target;
      };
   }
}
