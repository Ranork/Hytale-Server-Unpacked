package com.hypixel.hytale.builtin.adventure.objectives.commands;

import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveLocationMarkerAsset;
import com.hypixel.hytale.builtin.adventure.objectives.markers.objectivelocation.ObjectiveLocationMarker;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ObjectiveLocationMarkerCommand extends AbstractCommandCollection {
   public ObjectiveLocationMarkerCommand() {
      super("locationmarker", "server.commands.objective.locationMarker");
      this.addAliases("marker");
      this.addSubCommand(new ObjectiveLocationMarkerCommand.AddLocationMarkerCommand());
      this.addSubCommand(new ObjectiveLocationMarkerCommand.EnableLocationMarkerCommand());
      this.addSubCommand(new ObjectiveLocationMarkerCommand.DisableLocationMarkerCommand());
   }

   public static class AddLocationMarkerCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<String> locationMarkerArg = this.withRequiredArg(
         "locationMarkerId", "server.commands.objective.locationMarker.add.arg.locationMarkerId.desc", ArgTypes.STRING
      );

      public AddLocationMarkerCommand() {
         super("add", "server.commands.objective.locationMarker.add");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         TransformComponent playerTransformComponent = store.getComponent(ref, TransformComponent.getComponentType());
         if (playerTransformComponent != null) {
            String objectiveLocationMarkerId = this.locationMarkerArg.get(context);
            if (ObjectiveLocationMarkerAsset.getAssetMap().getAsset(objectiveLocationMarkerId) == null) {
               context.sendMessage(Message.translation("server.commands.objective.locationMarker.notFound").param("id", objectiveLocationMarkerId));
               context.sendMessage(
                  Message.translation("server.general.failed.didYouMean")
                     .param(
                        "choices",
                        StringUtil.sortByFuzzyDistance(
                              objectiveLocationMarkerId, ObjectiveLocationMarkerAsset.getAssetMap().getAssetMap().keySet(), CommandUtil.RECOMMEND_COUNT
                           )
                           .toString()
                     )
               );
            } else {
               Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
               holder.addComponent(ObjectiveLocationMarker.getComponentType(), new ObjectiveLocationMarker(objectiveLocationMarkerId));
               Model model = ObjectivePlugin.get().getObjectiveLocationMarkerModel();
               holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
               holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
               holder.addComponent(Nameplate.getComponentType(), new Nameplate(objectiveLocationMarkerId));
               TransformComponent transform = new TransformComponent(playerTransformComponent.getPosition(), playerTransformComponent.getRotation());
               holder.addComponent(TransformComponent.getComponentType(), transform);
               holder.ensureComponent(UUIDComponent.getComponentType());
               holder.ensureComponent(Intangible.getComponentType());
               holder.ensureComponent(HiddenFromAdventurePlayers.getComponentType());
               store.addEntity(holder, AddReason.SPAWN);
               context.sendMessage(Message.translation("server.commands.objective.locationMarker.added").param("id", objectiveLocationMarkerId));
            }
         }
      }
   }

   public static class DisableLocationMarkerCommand extends AbstractWorldCommand {
      public DisableLocationMarkerCommand() {
         super("disable", "server.commands.objective.locationMarker.disable");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         WorldConfig worldConfig = world.getWorldConfig();
         worldConfig.setObjectiveMarkersEnabled(false);
         worldConfig.markChanged();
         context.sendMessage(Message.translation("server.commands.objective.locationMarker.disabled").param("worldName", world.getName()));
      }
   }

   public static class EnableLocationMarkerCommand extends AbstractWorldCommand {
      public EnableLocationMarkerCommand() {
         super("enable", "server.commands.objective.locationMarker.enable");
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         WorldConfig worldConfig = world.getWorldConfig();
         worldConfig.setObjectiveMarkersEnabled(true);
         worldConfig.markChanged();
         context.sendMessage(Message.translation("server.commands.objective.locationMarker.enabled").param("worldName", world.getName()));
      }
   }
}
