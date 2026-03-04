package com.hypixel.hytale.server.core.modules.interaction.interaction.config.data;

import com.hypixel.hytale.function.function.TriFunction;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class TreeCollector<T> implements Collector {
   private final TriFunction<CollectorTag, InteractionContext, Interaction, T> function;
   private TreeCollector.Node<T> root;
   private TreeCollector.Node<T> current;

   public TreeCollector(TriFunction<CollectorTag, InteractionContext, Interaction, T> function) {
      this.function = function;
   }

   public TreeCollector.Node<T> getRoot() {
      return this.root;
   }

   @Override
   public void start() {
      this.root = new TreeCollector.Node<>(null);
      this.current = this.root;
   }

   @Override
   public void into(@Nonnull InteractionContext context, Interaction interaction) {
      if (this.current.children != null) {
         this.current.children = Arrays.copyOf(this.current.children, this.current.children.length + 1);
      } else {
         this.current.children = new TreeCollector.Node[1];
      }

      this.current = this.current.children[this.current.children.length - 1] = new TreeCollector.Node<>(this.current);
   }

   @Override
   public boolean collect(@Nonnull CollectorTag tag, @Nonnull InteractionContext context, @Nonnull Interaction interaction) {
      this.current.data = this.function.apply(tag, context, interaction);
      return false;
   }

   @Override
   public void outof() {
      this.current = this.current.parent;
   }

   @Override
   public void finished() {
   }

   public static class Node<T> {
      public static final TreeCollector.Node[] EMPTY_ARRAY = new TreeCollector.Node[0];
      private final TreeCollector.Node<T> parent;
      private TreeCollector.Node<T>[] children = EMPTY_ARRAY;
      private T data;

      Node(TreeCollector.Node<T> parent) {
         this.parent = parent;
      }

      public TreeCollector.Node<T> getParent() {
         return this.parent;
      }

      public TreeCollector.Node<T>[] getChildren() {
         return this.children;
      }

      public T getData() {
         return this.data;
      }
   }
}
