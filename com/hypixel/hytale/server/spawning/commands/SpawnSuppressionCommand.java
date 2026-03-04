package com.hypixel.hytale.server.spawning.commands;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.AssetArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawnsuppression.SpawnSuppression;
import com.hypixel.hytale.server.spawning.suppression.component.ChunkSuppressionEntry;
import com.hypixel.hytale.server.spawning.suppression.component.SpawnSuppressionComponent;
import com.hypixel.hytale.server.spawning.suppression.component.SpawnSuppressionController;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class SpawnSuppressionCommand extends AbstractCommandCollection {
   @Nonnull
   private static final AssetArgumentType<SpawnSuppression, ?> SPAWN_SUPPRESSION_ASSET_TYPE = new AssetArgumentType(
      "server.commands.spawning.suppression.arg.suppression.name", SpawnSuppression.class, "server.commands.spawning.suppression.arg.suppression.usage"
   );

   public SpawnSuppressionCommand() {
      super("suppression", "server.commands.spawning.suppression.desc");
      this.addSubCommand(new SpawnSuppressionCommand.Dump());
      this.addSubCommand(new SpawnSuppressionCommand.DumpAll());
      this.addSubCommand(new SpawnSuppressionCommand.Add());
   }

   @Nonnull
   private static String dumpWorld(@Nonnull World world) {
      SpawnSuppressionController spawnSuppressionController = world.getEntityStore().getStore().getResource(SpawnSuppressionController.getResourceType());
      StringBuilder sb = new StringBuilder("World: ").append(world.getName()).append("\n  Spawn Suppressors:");
      spawnSuppressionController.getSpawnSuppressorMap()
         .values()
         .forEach(entry -> sb.append("\n    ").append(entry.getSuppressionId()).append(": ").append(entry.getPosition()));
      sb.append("\n  Chunk Annotations:");
      spawnSuppressionController.getChunkSuppressionMap().forEach((index, entry) -> {
         sb.append("\n    Chunk ").append(index).append(": ");
         List<ChunkSuppressionEntry.SuppressionSpan> suppressionSpans = entry.getSuppressionSpans();
         suppressionSpans.forEach(span -> {
            sb.append("\n      From y=").append(span.getMinY()).append(" to y=").append(span.getMaxY());
            sb.append("\n      Suppressing Roles: [ ");
            if (span.getSuppressedRoles() != null) {
               sb.append(span.getSuppressedRoles().stream().map(roleIndex -> NPCPlugin.get().getName(roleIndex)).collect(Collectors.joining(", ")));
            }

            sb.append(" ]");
         });
      });
      return sb.toString();
   }

   private static class Add extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<SpawnSuppression> suppressionArg = this.withRequiredArg(
         "suppression", "server.commands.spawning.suppression.add.arg.suppression.desc", SpawnSuppressionCommand.SPAWN_SUPPRESSION_ASSET_TYPE
      );

      public Add() {
         super("add", "server.commands.spawning.suppression.add.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         SpawnSuppression spawnSuppression = this.suppressionArg.get(context);
         Vector3f rotation = transformComponent.getRotation();
         Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
         holder.addComponent(SpawnSuppressionComponent.getComponentType(), new SpawnSuppressionComponent(spawnSuppression.getId()));
         holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(transformComponent.getPosition(), rotation));
         holder.ensureComponent(UUIDComponent.getComponentType());
         holder.ensureComponent(HiddenFromAdventurePlayers.getComponentType());
         Model model = SpawningPlugin.get().getSpawnMarkerModel();
         holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
         holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
         Nameplate nameplate = new Nameplate("SpawnSuppression: " + spawnSuppression);
         holder.addComponent(Nameplate.getComponentType(), nameplate);
         store.addEntity(holder, AddReason.SPAWN);
         context.sendMessage(Message.translation("server.commands.spawning.suppression.add.added").param("suppressionId", spawnSuppression.getId()));
      }
   }

   private static class Dump extends AbstractWorldCommand {
      @Nonnull
      private static final Message MESSAGE_COMMANDS_SPAWNING_SUPPRESSION_DUMP_DUMPED = Message.translation("server.commands.spawning.suppression.dump.dumped");

      public Dump() {
         super("dump", "server.commands.spawning.suppression.dump.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         String dump = SpawnSuppressionCommand.dumpWorld(world);
         context.sendMessage(MESSAGE_COMMANDS_SPAWNING_SUPPRESSION_DUMP_DUMPED);
         ((HytaleLogger.Api)SpawningPlugin.get().getLogger().atInfo()).log(dump);
      }
   }

   private static class DumpAll extends AbstractWorldCommand {
      @Nonnull
      private static final Message MESSAGE_COMMANDS_SPAWNING_SUPPRESSION_DUMP_DUMPED = Message.translation("server.commands.spawning.suppression.dump.dumped");

      public DumpAll() {
         super("dumpall", "server.commands.spawning.suppression.dump.all.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         Universe.get()
            .getWorlds()
            .values()
            .forEach(w -> ((HytaleLogger.Api)SpawningPlugin.get().getLogger().atInfo()).log(SpawnSuppressionCommand.dumpWorld(w)));
         context.sendMessage(MESSAGE_COMMANDS_SPAWNING_SUPPRESSION_DUMP_DUMPED);
      }
   }
}
