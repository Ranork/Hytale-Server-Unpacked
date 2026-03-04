package com.hypixel.hytale.server.core.modules.prefabspawner.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.modules.prefabspawner.PrefabSpawnerState;
import com.hypixel.hytale.server.core.prefab.PrefabWeights;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;

public class PrefabSpawnerWeightCommand extends TargetPrefabSpawnerCommand {
   @Nonnull
   private final RequiredArg<String> prefabArg = this.withRequiredArg("prefab", "server.commands.prefabspawner.weight.prefab.desc", ArgTypes.STRING);
   @Nonnull
   private final RequiredArg<Float> weightArg = this.withRequiredArg("weight", "server.commands.prefabspawner.weight.weight.desc", ArgTypes.FLOAT);

   public PrefabSpawnerWeightCommand() {
      super("weight", "server.commands.prefabspawner.weight.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull WorldChunk chunk, @Nonnull PrefabSpawnerState prefabSpawner) {
      String prefab = this.prefabArg.get(context);
      Float weight = this.weightArg.get(context);
      PrefabWeights prefabWeights = prefabSpawner.getPrefabWeights();
      if (prefabWeights == PrefabWeights.NONE) {
         prefabWeights = new PrefabWeights();
      }

      if (weight < 0.0F) {
         prefabWeights.removeWeight(prefab);
         context.sendMessage(Message.translation("server.commands.prefabspawner.weight.remove").param("prefab", prefab));
      } else {
         prefabWeights.setWeight(prefab, weight.floatValue());
         context.sendMessage(Message.translation("server.commands.prefabspawner.weight.set").param("prefab", prefab).param("weight", weight));
      }

      prefabSpawner.setPrefabWeights(prefabWeights);
      chunk.markNeedsSaving();
   }
}
