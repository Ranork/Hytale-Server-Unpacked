package com.hypixel.hytale.server.core.prefab.selection.standard;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

@FunctionalInterface
public interface FeedbackConsumer {
   FeedbackConsumer DEFAULT = (key, total, count, target, componentAccessor) -> {};

   void accept(@Nonnull String var1, int var2, int var3, @Nonnull CommandSender var4, @Nonnull ComponentAccessor<EntityStore> var5);
}
