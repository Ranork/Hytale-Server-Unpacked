package com.hypixel.hytale.server.core.modules.prefabspawner.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.modules.prefabspawner.PrefabSpawnerState;
import com.hypixel.hytale.server.core.prefab.PrefabWeights;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;

public class PrefabSpawnerSetCommand extends TargetPrefabSpawnerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_PREFAB_SPAWNER_SET = Message.translation("server.commands.prefabspawner.set");
   @Nonnull
   protected final RequiredArg<String> prefabPathArg = this.withRequiredArg("prefab", "server.commands.prefabspawner.set.prefab.desc", ArgTypes.STRING);
   @Nonnull
   protected final OptionalArg<Boolean> fitHeightmapArg = this.withOptionalArg(
      "fitHeightmap", "server.commands.prefabspawner.set.fitHeightmap.desc", ArgTypes.BOOLEAN
   );
   @Nonnull
   protected final OptionalArg<Boolean> inheritSeedArg = this.withOptionalArg(
      "inheritSeed", "server.commands.prefabspawner.set.inheritSeed.desc", ArgTypes.BOOLEAN
   );
   @Nonnull
   protected final OptionalArg<Boolean> inheritHeightCheckArg = this.withOptionalArg(
      "inheritHeightCheck", "server.commands.prefabspawner.set.inheritHeightCheck.desc", ArgTypes.BOOLEAN
   );
   @Nonnull
   protected final OptionalArg<Double> defaultWeightArg = this.withOptionalArg(
      "defaultWeight", "server.commands.prefabspawner.set.defaultWeight.desc", ArgTypes.DOUBLE
   );

   public PrefabSpawnerSetCommand() {
      super("set", "server.commands.prefabspawner.set.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull WorldChunk chunk, @Nonnull PrefabSpawnerState prefabSpawner) {
      String prefabPath = this.prefabPathArg.get(context);
      prefabSpawner.setPrefabPath(prefabPath);
      if (this.fitHeightmapArg.provided(context)) {
         boolean fitHeightmap = getOrDefault(this.fitHeightmapArg, context, true);
         prefabSpawner.setFitHeightmap(fitHeightmap);
      }

      if (this.inheritSeedArg.provided(context)) {
         boolean inheritSeed = getOrDefault(this.inheritSeedArg, context, true);
         prefabSpawner.setInheritSeed(inheritSeed);
      }

      if (this.inheritHeightCheckArg.provided(context)) {
         boolean inheritHeightCheck = getOrDefault(this.inheritHeightCheckArg, context, true);
         prefabSpawner.setInheritHeightCondition(inheritHeightCheck);
      }

      if (this.defaultWeightArg.provided(context)) {
         double weight = this.defaultWeightArg.get(context);
         PrefabWeights prefabWeights = prefabSpawner.getPrefabWeights();
         if (prefabWeights == PrefabWeights.NONE) {
            prefabWeights = new PrefabWeights();
         }

         prefabWeights.setDefaultWeight(weight);
         prefabSpawner.setPrefabWeights(prefabWeights);
      }

      chunk.markNeedsSaving();
      context.sendMessage(MESSAGE_COMMANDS_PREFAB_SPAWNER_SET);
   }

   protected static boolean getOrDefault(@Nonnull OptionalArg<Boolean> arg, @Nonnull CommandContext context, boolean defaultValue) {
      if (!arg.provided(context)) {
         return defaultValue;
      } else {
         Boolean value = arg.get(context);
         return value != null ? value : defaultValue;
      }
   }
}
