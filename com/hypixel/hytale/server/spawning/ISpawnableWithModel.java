package com.hypixel.hytale.server.spawning;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ISpawnableWithModel extends ISpawnable {
   @Nullable
   String getSpawnModelName(ExecutionContext var1, Scope var2);

   @Nullable
   default Scope createModifierScope(ExecutionContext executionContext) {
      throw new IllegalStateException("Call to createModifierScope not valid for ISpawnableWithModel");
   }

   Scope createExecutionScope();

   void markNeedsReload();

   boolean isMemory(ExecutionContext var1, @Nullable Scope var2);

   String getMemoriesCategory(ExecutionContext var1, @Nullable Scope var2);

   String getMemoriesNameOverride(ExecutionContext var1, @Nullable Scope var2);

   @Nonnull
   String getNameTranslationKey(ExecutionContext var1, @Nullable Scope var2);
}
