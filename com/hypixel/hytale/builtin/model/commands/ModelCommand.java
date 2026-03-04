package com.hypixel.hytale.builtin.model.commands;

import com.hypixel.hytale.builtin.model.pages.ChangeModelPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ModelCommand extends AbstractPlayerCommand {
   public ModelCommand() {
      super("model", "server.commands.model.desc");
      this.addUsageVariant(new ModelCommand.ModelOtherCommand());
      this.addSubCommand(new ModelCommand.ModelSetCommand());
      this.addSubCommand(new ModelCommand.ModelResetCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      playerComponent.getPageManager().openCustomPage(ref, store, new ChangeModelPage(playerRef));
   }

   private static class ModelOtherCommand extends CommandBase {
      @Nonnull
      private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);

      ModelOtherCommand() {
         super("server.commands.model.other.desc");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         PlayerRef targetPlayerRef = this.playerArg.get(context);
         Ref<EntityStore> ref = targetPlayerRef.getReference();
         if (ref != null && ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            world.execute(() -> {
               Player playerComponent = store.getComponent(ref, Player.getComponentType());
               if (playerComponent == null) {
                  context.sendMessage(Message.translation("server.commands.errors.playerNotInWorld"));
               } else {
                  playerComponent.getPageManager().openCustomPage(ref, store, new ChangeModelPage(targetPlayerRef));
               }
            });
         } else {
            context.sendMessage(Message.translation("server.commands.errors.playerNotInWorld"));
         }
      }
   }

   static class ModelResetCommand extends AbstractPlayerCommand {
      @Nonnull
      private final OptionalArg<Float> scaleArg = this.withOptionalArg("scale", "server.commands.model.reset.scale.desc", ArgTypes.FLOAT);

      ModelResetCommand() {
         super("reset", "server.commands.model.reset.desc");
         this.addAliases("clear");
         this.addUsageVariant(new ModelCommand.ModelResetCommand.ModelResetOtherCommand());
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         PlayerSkinComponent skinComponent = store.getComponent(ref, PlayerSkinComponent.getComponentType());
         if (skinComponent == null) {
            context.sendMessage(Message.translation("server.commands.model.noAuthSkinForPlayer").param("model", "Player"));
         } else {
            PlayerSkinComponent playerSkinComponent = store.getComponent(ref, PlayerSkinComponent.getComponentType());

            assert playerSkinComponent != null;

            CosmeticsModule cosmeticsModule = CosmeticsModule.get();
            if (this.scaleArg.provided(context)) {
               Float scale = this.scaleArg.get(context);
               Model newModel = cosmeticsModule.createModel(playerSkinComponent.getPlayerSkin(), scale);
               store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
            } else {
               Model newModel = cosmeticsModule.createModel(playerSkinComponent.getPlayerSkin());
               store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
            }

            playerSkinComponent.setNetworkOutdated();
            context.sendMessage(Message.translation("server.commands.model.modelResetForPlayer"));
         }
      }

      private static class ModelResetOtherCommand extends CommandBase {
         @Nonnull
         private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);
         @Nonnull
         private final OptionalArg<Float> scaleArg = this.withOptionalArg("scale", "server.commands.model.reset.scale.desc", ArgTypes.FLOAT);

         ModelResetOtherCommand() {
            super("server.commands.model.reset.other.desc");
         }

         @Override
         protected void executeSync(@Nonnull CommandContext context) {
            PlayerRef targetPlayerRef = this.playerArg.get(context);
            Ref<EntityStore> ref = targetPlayerRef.getReference();
            if (ref != null && ref.isValid()) {
               Store<EntityStore> store = ref.getStore();
               World world = store.getExternalData().getWorld();
               world.execute(
                  () -> {
                     Player playerComponent = store.getComponent(ref, Player.getComponentType());
                     if (playerComponent == null) {
                        context.sendMessage(Message.translation("server.commands.errors.playerNotInWorld"));
                     } else {
                        PlayerSkinComponent skinComponent = store.getComponent(ref, PlayerSkinComponent.getComponentType());
                        if (skinComponent == null) {
                           context.sendMessage(
                              Message.translation("server.commands.model.noAuthSkin").param("name", targetPlayerRef.getUsername()).param("model", "Player")
                           );
                        } else {
                           PlayerSkinComponent playerSkinComponent = store.getComponent(ref, PlayerSkinComponent.getComponentType());

                           assert playerSkinComponent != null;

                           CosmeticsModule cosmeticsModule = CosmeticsModule.get();
                           if (this.scaleArg.provided(context)) {
                              Float scale = this.scaleArg.get(context);
                              Model newModel = cosmeticsModule.createModel(playerSkinComponent.getPlayerSkin(), scale);
                              store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
                           } else {
                              Model newModel = cosmeticsModule.createModel(playerSkinComponent.getPlayerSkin());
                              store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
                           }

                           playerSkinComponent.setNetworkOutdated();
                           context.sendMessage(Message.translation("server.commands.model.modelReset").param("name", targetPlayerRef.getUsername()));
                        }
                     }
                  }
               );
            } else {
               context.sendMessage(Message.translation("server.commands.errors.playerNotInWorld"));
            }
         }
      }
   }

   static class ModelSetCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<ModelAsset> modelAssetArg = this.withRequiredArg("model", "server.commands.model.set.model.desc", ArgTypes.MODEL_ASSET);
      @Nonnull
      private final OptionalArg<Float> scaleArg = this.withOptionalArg("scale", "server.commands.model.set.scale.desc", ArgTypes.FLOAT);
      @Nonnull
      private final FlagArg bypassScaleLimitsFlag = this.withFlagArg("bypassScaleLimits", "server.commands.model.set.bypassScaleLimits.desc");

      ModelSetCommand() {
         super("set", "server.commands.model.set.desc");
         this.addUsageVariant(new ModelCommand.ModelSetCommand.ModelSetOtherCommand());
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         ModelAsset modelAsset = this.modelAssetArg.get(context);
         float scale = this.scaleArg.provided(context) ? this.scaleArg.get(context) : modelAsset.generateRandomScale();
         if (!this.bypassScaleLimitsFlag.provided(context)) {
            scale = MathUtil.clamp(scale, modelAsset.getMinScale(), modelAsset.getMaxScale());
         }

         Model model = Model.createScaledModel(modelAsset, scale);
         store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
         context.sendMessage(Message.translation("server.commands.model.modelSetForPlayer").param("modelName", modelAsset.getId()));
      }

      private static class ModelSetOtherCommand extends CommandBase {
         @Nonnull
         private final RequiredArg<ModelAsset> modelAssetArg = this.withRequiredArg("model", "server.commands.model.set.model.desc", ArgTypes.MODEL_ASSET);
         @Nonnull
         private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);
         @Nonnull
         private final OptionalArg<Float> scaleArg = this.withOptionalArg("scale", "server.commands.model.set.scale.desc", ArgTypes.FLOAT);
         @Nonnull
         private final FlagArg bypassScaleLimitsFlag = this.withFlagArg("bypassScaleLimits", "server.commands.model.set.bypassScaleLimits.desc");

         ModelSetOtherCommand() {
            super("server.commands.model.set.other.desc");
         }

         @Override
         protected void executeSync(@Nonnull CommandContext context) {
            PlayerRef targetPlayerRef = this.playerArg.get(context);
            Ref<EntityStore> ref = targetPlayerRef.getReference();
            if (ref != null && ref.isValid()) {
               Store<EntityStore> store = ref.getStore();
               World world = store.getExternalData().getWorld();
               world.execute(
                  () -> {
                     Player playerComponent = store.getComponent(ref, Player.getComponentType());
                     if (playerComponent == null) {
                        context.sendMessage(Message.translation("server.commands.errors.playerNotInWorld"));
                     } else {
                        ModelAsset modelAsset = this.modelAssetArg.get(context);
                        float scale = this.scaleArg.provided(context) ? this.scaleArg.get(context) : modelAsset.generateRandomScale();
                        if (!this.bypassScaleLimitsFlag.provided(context)) {
                           scale = MathUtil.clamp(scale, modelAsset.getMinScale(), modelAsset.getMaxScale());
                        }

                        Model model = Model.createScaledModel(modelAsset, scale);
                        store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
                        context.sendMessage(
                           Message.translation("server.commands.model.modelSet")
                              .param("playerName", targetPlayerRef.getUsername())
                              .param("modelName", modelAsset.getId())
                        );
                     }
                  }
               );
            } else {
               context.sendMessage(Message.translation("server.commands.errors.playerNotInWorld"));
            }
         }
      }
   }
}
