package com.hypixel.hytale.server.spawning.commands;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.AssetArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.assets.spawnmarker.config.SpawnMarker;
import com.hypixel.hytale.server.spawning.spawnmarkers.SpawnMarkerEntity;
import javax.annotation.Nonnull;

public class SpawnMarkersCommand extends AbstractCommandCollection {
   @Nonnull
   private static final AssetArgumentType<SpawnMarker, ?> SPAWN_MARKER_ASSET_TYPE = new AssetArgumentType(
      "server.commands.spawning.markers.arg.marker.name", SpawnMarker.class, "server.commands.spawning.markers.arg.marker.usage"
   );

   public SpawnMarkersCommand() {
      super("markers", "server.commands.spawning.markers.desc");
      this.addSubCommand(new SpawnMarkersCommand.EnableCommand());
      this.addSubCommand(new SpawnMarkersCommand.DisableCommand());
      this.addSubCommand(new SpawnMarkersCommand.Add());
   }

   private static class Add extends AbstractPlayerCommand {
      @Nonnull
      private static final Message COMMANDS_ERRORS_PLAYER_ONLY = Message.translation("server.commands.errors.playerOnly");
      @Nonnull
      private final RequiredArg<SpawnMarker> markerArg = this.withRequiredArg(
         "marker", "server.commands.spawning.markers.add.arg.marker.desc", SpawnMarkersCommand.SPAWN_MARKER_ASSET_TYPE
      );
      @Nonnull
      private final FlagArg flipArg = this.withFlagArg("flip", "server.commands.spawning.markers.add.flip.desc");

      public Add() {
         super("add", "server.commands.spawning.markers.add.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         if (!context.isPlayer()) {
            throw new GeneralCommandException(COMMANDS_ERRORS_PLAYER_ONLY);
         } else {
            TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

            assert transformComponent != null;

            SpawnMarker marker = this.markerArg.get(context);
            SpawnMarkerEntity spawnMarker = new SpawnMarkerEntity();
            spawnMarker.setSpawnMarker(marker);
            Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
            holder.addComponent(SpawnMarkerEntity.getComponentType(), spawnMarker);
            TransformComponent spawnerTransformComponent = transformComponent.clone();
            if (this.flipArg.get(context)) {
               spawnerTransformComponent.getRotation().flipRotationOnAxis(Axis.Y);
            }

            holder.addComponent(TransformComponent.getComponentType(), spawnerTransformComponent);
            holder.addComponent(Nameplate.getComponentType(), new Nameplate(marker.getId()));
            holder.ensureComponent(UUIDComponent.getComponentType());
            holder.ensureComponent(HiddenFromAdventurePlayers.getComponentType());
            Model model = SpawnMarkerEntity.getModel(marker);
            holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
            holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
            Ref<EntityStore> spawnMarkerRef = store.addEntity(holder, AddReason.SPAWN);
            if (spawnMarkerRef != null && spawnMarkerRef.isValid()) {
               context.sendMessage(Message.translation("server.commands.spawning.markers.add.added").param("markerId", marker.getId()));
            } else {
               context.sendMessage(Message.translation("server.commands.spawning.markers.add.failed").param("markerId", marker.getId()));
            }
         }
      }
   }

   private static class DisableCommand extends AbstractWorldCommand {
      public DisableCommand() {
         super("disable", "server.commands.spawning.markers.disable.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         WorldConfig worldConfig = world.getWorldConfig();
         worldConfig.setIsSpawnMarkersEnabled(false);
         worldConfig.markChanged();
         context.sendMessage(Message.translation("server.commands.spawning.markers.disabled").param("worldName", world.getName()));
      }
   }

   private static class EnableCommand extends AbstractWorldCommand {
      public EnableCommand() {
         super("enable", "server.commands.spawning.markers.enable.desc");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         WorldConfig worldConfig = world.getWorldConfig();
         worldConfig.setIsSpawnMarkersEnabled(true);
         worldConfig.markChanged();
         context.sendMessage(Message.translation("server.commands.spawning.markers.enabled").param("worldName", world.getName()));
      }
   }
}
