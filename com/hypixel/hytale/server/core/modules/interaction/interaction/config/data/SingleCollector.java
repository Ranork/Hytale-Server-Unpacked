package com.hypixel.hytale.server.core.modules.interaction.interaction.config.data;

import com.hypixel.hytale.function.function.TriFunction;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SingleCollector<T> implements Collector {
   private final TriFunction<CollectorTag, InteractionContext, Interaction, T> function;
   @Nullable
   private T result;

   public SingleCollector(TriFunction<CollectorTag, InteractionContext, Interaction, T> function) {
      this.function = function;
   }

   @Nullable
   public T getResult() {
      return this.result;
   }

   @Override
   public void start() {
      this.result = null;
   }

   @Override
   public void into(@Nonnull InteractionContext context, Interaction interaction) {
   }

   @Override
   public boolean collect(@Nonnull CollectorTag tag, @Nonnull InteractionContext context, @Nonnull Interaction interaction) {
      this.result = this.function.apply(tag, context, interaction);
      return this.result != null;
   }

   @Override
   public void outof() {
   }

   @Override
   public void finished() {
   }
}
