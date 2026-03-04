package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EnvironmentCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<String> environmentArg = this.withRequiredArg("environment", "server.commands.environment.environment.desc", ArgTypes.STRING);

   public EnvironmentCommand() {
      super("environment", "server.commands.environment.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.addAliases("setenv", "setenvironment");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      String envName = this.environmentArg.get(context);
      Environment environment = Environment.getAssetMap().getAsset(envName);
      if (environment == null) {
         context.sendMessage(Message.translation("server.builderTools.environment.envNotFound").param("name", envName));
         context.sendMessage(
            Message.translation("server.general.failed.didYouMean")
               .param(
                  "choices", StringUtil.sortByFuzzyDistance(envName, Environment.getAssetMap().getAssetMap().keySet(), CommandUtil.RECOMMEND_COUNT).toString()
               )
         );
      } else {
         String key = environment.getId();
         int index = Environment.getAssetMap().getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         } else {
            BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.environment(r, index, componentAccessor));
         }
      }
   }
}
