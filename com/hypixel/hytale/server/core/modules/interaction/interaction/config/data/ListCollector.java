package com.hypixel.hytale.server.core.modules.interaction.interaction.config.data;

import com.hypixel.hytale.function.function.TriFunction;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class ListCollector<T> implements Collector {
   private final TriFunction<CollectorTag, InteractionContext, Interaction, T> function;
   private List<T> list;

   public ListCollector(TriFunction<CollectorTag, InteractionContext, Interaction, T> function) {
      this.function = function;
   }

   public List<T> getList() {
      return this.list;
   }

   @Override
   public void start() {
      this.list = new ObjectArrayList();
   }

   @Override
   public void into(@Nonnull InteractionContext context, Interaction interaction) {
   }

   @Override
   public boolean collect(@Nonnull CollectorTag tag, @Nonnull InteractionContext context, @Nonnull Interaction interaction) {
      this.list.add(this.function.apply(tag, context, interaction));
      return false;
   }

   @Override
   public void outof() {
   }

   @Override
   public void finished() {
   }
}
