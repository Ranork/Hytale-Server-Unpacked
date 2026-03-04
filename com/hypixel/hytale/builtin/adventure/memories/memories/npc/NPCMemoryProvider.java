package com.hypixel.hytale.builtin.adventure.memories.memories.npc;

import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.builtin.adventure.memories.memories.Memory;
import com.hypixel.hytale.builtin.adventure.memories.memories.MemoryProvider;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NPCMemoryProvider extends MemoryProvider<NPCMemory> {
   public static final double DEFAULT_RADIUS = 10.0;

   public NPCMemoryProvider() {
      super("NPC", NPCMemory.CODEC, 10.0);
   }

   @Nonnull
   @Override
   public Map<String, Set<Memory>> getAllMemories() {
      Map<String, Set<Memory>> allMemories = new Object2ObjectOpenHashMap();
      Int2ObjectMap<BuilderInfo> allBuilders = NPCPlugin.get().getBuilderManager().getAllBuilders();
      ObjectIterator var3 = allBuilders.values().iterator();

      while (var3.hasNext()) {
         BuilderInfo builderInfo = (BuilderInfo)var3.next();

         try {
            Builder<?> builder = builderInfo.getBuilder();
            if (builder.isSpawnable() && !builder.isDeprecated() && builderInfo.isValid() && isMemory(builder)) {
               String category = getCategory(builder);
               if (category != null) {
                  String memoriesNameOverride = getMemoriesNameOverride(builder);
                  String translationKey = getNPCNameTranslationKey(builder);
                  NPCMemory memory;
                  if (memoriesNameOverride != null && !memoriesNameOverride.isEmpty()) {
                     memory = new NPCMemory(memoriesNameOverride, translationKey);
                  } else {
                     memory = new NPCMemory(builderInfo.getKeyName(), translationKey);
                  }

                  allMemories.computeIfAbsent(category, s -> new HashSet<>()).add(memory);
               }
            }
         } catch (SkipSentryException var10) {
            MemoriesPlugin.get().getLogger().at(Level.SEVERE).log(var10.getMessage());
         }
      }

      return allMemories;
   }

   @Nullable
   private static String getCategory(@Nonnull Builder<?> builder) {
      if (builder instanceof ISpawnableWithModel spawnableWithModel) {
         ExecutionContext executionContext = new ExecutionContext();
         executionContext.setScope(spawnableWithModel.createExecutionScope());
         Scope modifierScope = spawnableWithModel.createModifierScope(executionContext);
         return spawnableWithModel.getMemoriesCategory(executionContext, modifierScope);
      } else {
         return "Other";
      }
   }

   private static boolean isMemory(@Nonnull Builder<?> builder) {
      if (builder instanceof ISpawnableWithModel spawnableWithModel) {
         ExecutionContext executionContext = new ExecutionContext();
         executionContext.setScope(spawnableWithModel.createExecutionScope());
         Scope modifierScope = spawnableWithModel.createModifierScope(executionContext);
         return spawnableWithModel.isMemory(executionContext, modifierScope);
      } else {
         return false;
      }
   }

   @Nullable
   private static String getMemoriesNameOverride(@Nonnull Builder<?> builder) {
      if (builder instanceof ISpawnableWithModel spawnableWithModel) {
         ExecutionContext executionContext = new ExecutionContext();
         executionContext.setScope(spawnableWithModel.createExecutionScope());
         Scope modifierScope = spawnableWithModel.createModifierScope(executionContext);
         return spawnableWithModel.getMemoriesNameOverride(executionContext, modifierScope);
      } else {
         return null;
      }
   }

   @Nonnull
   private static String getNPCNameTranslationKey(@Nonnull Builder<?> builder) {
      if (builder instanceof ISpawnableWithModel spawnableWithModel) {
         ExecutionContext executionContext = new ExecutionContext();
         executionContext.setScope(spawnableWithModel.createExecutionScope());
         Scope modifierScope = spawnableWithModel.createModifierScope(executionContext);
         return spawnableWithModel.getNameTranslationKey(executionContext, modifierScope);
      } else {
         throw new SkipSentryException(new IllegalStateException("Cannot get translation key for a non spawnable NPC role!"));
      }
   }
}
