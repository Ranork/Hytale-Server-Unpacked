package com.hypixel.hytale.builtin.buildertools;

import com.hypixel.hytale.server.core.Message;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class UndoActionRegistry {
   private static final Map<String, UndoAction> REGISTRY = new ConcurrentHashMap<>();

   private UndoActionRegistry() {
   }

   @Nonnull
   public static UndoAction register(@Nonnull String id, @Nonnull UndoAction action) {
      if (REGISTRY.putIfAbsent(id, action) != null) {
         throw new IllegalArgumentException("UndoAction already registered: " + id);
      } else {
         return action;
      }
   }

   @Nonnull
   public static UndoAction register(@Nonnull String id, @Nonnull String translationKey) {
      UndoActionRegistry.SimpleUndoAction action = new UndoActionRegistry.SimpleUndoAction(id, translationKey, true);
      return register(id, action);
   }

   @Nonnull
   public static UndoAction register(@Nonnull String id, @Nonnull String translationKey, boolean marksPrefabDirty) {
      UndoActionRegistry.SimpleUndoAction action = new UndoActionRegistry.SimpleUndoAction(id, translationKey, marksPrefabDirty);
      return register(id, action);
   }

   @Nullable
   public static UndoAction get(@Nonnull String id) {
      return REGISTRY.get(id);
   }

   static {
      for (BuilderToolsPlugin.Action action : BuilderToolsPlugin.Action.values()) {
         REGISTRY.put(action.id(), action);
      }
   }

   private record SimpleUndoAction(@Nonnull String id, @Nonnull String translationKey, boolean marksPrefabDirty) implements UndoAction {
      @Nonnull
      @Override
      public Message toMessage() {
         return Message.translation(this.translationKey);
      }
   }
}
