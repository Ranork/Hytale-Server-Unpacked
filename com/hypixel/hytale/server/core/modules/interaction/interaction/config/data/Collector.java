package com.hypixel.hytale.server.core.modules.interaction.interaction.config.data;

import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Collector {
   void start();

   void into(@Nonnull InteractionContext var1, @Nullable Interaction var2);

   boolean collect(@Nonnull CollectorTag var1, @Nonnull InteractionContext var2, @Nonnull Interaction var3);

   void outof();

   void finished();
}
