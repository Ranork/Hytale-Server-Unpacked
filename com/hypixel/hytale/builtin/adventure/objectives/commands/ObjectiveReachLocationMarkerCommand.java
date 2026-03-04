package com.hypixel.hytale.builtin.adventure.objectives.commands;

import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.markers.reachlocation.ReachLocationMarker;
import com.hypixel.hytale.builtin.adventure.objectives.markers.reachlocation.ReachLocationMarkerAsset;
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
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ObjectiveReachLocationMarkerCommand extends AbstractCommandCollection {
   public ObjectiveReachLocationMarkerCommand() {
      super("reachlocationmarker", "server.commands.objective.reachLocationMarker");
      this.addSubCommand(new ObjectiveReachLocationMarkerCommand.AddReachLocationMarkerCommand());
   }

   public static class AddReachLocationMarkerCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<String> reachLocationMarkerArg = this.withRequiredArg(
         "reachLocationMarkerId", "server.commands.objective.reachLocationMarker.add.arg.reachLocationMarkerId.desc", ArgTypes.STRING
      );

      public AddReachLocationMarkerCommand() {
         super("add", "server.commands.objective.reachLocationMarker.add");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         String reachLocationMarkerId = this.reachLocationMarkerArg.get(context);
         if (ReachLocationMarkerAsset.getAssetMap().getAsset(reachLocationMarkerId) == null) {
            context.sendMessage(Message.translation("server.commands.objective.reachLocationMarker.notFound").param("id", reachLocationMarkerId));
            context.sendMessage(
               Message.translation("server.general.failed.didYouMean")
                  .param(
                     "choices",
                     StringUtil.sortByFuzzyDistance(
                           reachLocationMarkerId, ReachLocationMarkerAsset.getAssetMap().getAssetMap().keySet(), CommandUtil.RECOMMEND_COUNT
                        )
                        .toString()
                  )
            );
         } else {
            Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
            holder.addComponent(ReachLocationMarker.getComponentType(), new ReachLocationMarker(reachLocationMarkerId));
            Model model = ObjectivePlugin.get().getObjectiveLocationMarkerModel();
            holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
            holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
            holder.addComponent(Nameplate.getComponentType(), new Nameplate(reachLocationMarkerId));
            TransformComponent playerTransformComponent = store.getComponent(ref, TransformComponent.getComponentType());

            assert playerTransformComponent != null;

            TransformComponent transform = new TransformComponent(playerTransformComponent.getPosition(), playerTransformComponent.getRotation());
            holder.addComponent(TransformComponent.getComponentType(), transform);
            holder.ensureComponent(UUIDComponent.getComponentType());
            holder.ensureComponent(Intangible.getComponentType());
            holder.ensureComponent(HiddenFromAdventurePlayers.getComponentType());
            store.addEntity(holder, AddReason.SPAWN);
            context.sendMessage(Message.translation("server.commands.objective.reachLocationMarker.added").param("id", reachLocationMarkerId));
         }
      }
   }
}
