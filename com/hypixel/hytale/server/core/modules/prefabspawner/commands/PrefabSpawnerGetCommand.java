package com.hypixel.hytale.server.core.modules.prefabspawner.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.modules.prefabspawner.PrefabSpawnerState;
import com.hypixel.hytale.server.core.prefab.PrefabWeights;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import java.util.Objects;
import javax.annotation.Nonnull;

public class PrefabSpawnerGetCommand extends TargetPrefabSpawnerCommand {
   public PrefabSpawnerGetCommand() {
      super("get", "server.commands.prefabspawner.get.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull WorldChunk chunk, @Nonnull PrefabSpawnerState prefabSpawner) {
      String prefab = Objects.requireNonNullElse(prefabSpawner.getPrefabPath(), "<undefined>");
      context.sendMessage(Message.translation("server.commands.prefabspawner.get.path").param("prefab", prefab));
      context.sendMessage(Message.translation("server.commands.prefabspawner.get.fitsHeightmap").param("fitHeightmap", prefabSpawner.isFitHeightmap()));
      context.sendMessage(Message.translation("server.commands.prefabspawner.get.inheritsSeed").param("inheritSeed", prefabSpawner.isInheritSeed()));
      context.sendMessage(
         Message.translation("server.commands.prefabspawner.get.inheritsHeightCheck").param("inheritHeightCheck", prefabSpawner.isInheritHeightCondition())
      );
      PrefabWeights weights = prefabSpawner.getPrefabWeights();
      if (weights.size() != 0) {
         context.sendMessage(Message.translation("server.commands.prefabspawner.get.defaultWeight").param("weight", weights.getDefaultWeight()));
         context.sendMessage(Message.translation("server.commands.prefabspawner.get.weights").param("weights", weights.getMappingString()));
      }
   }
}
