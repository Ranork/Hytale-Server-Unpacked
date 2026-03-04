package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.BuilderToolsUserData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SelectionHistoryCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<Boolean> enabledArg = this.withRequiredArg("enabled", "server.commands.selectionHistory.enabled.desc", ArgTypes.BOOLEAN);

   public SelectionHistoryCommand() {
      super("selectionHistory", "server.commands.selectionHistory.desc");
      this.setPermissionGroup(GameMode.Creative);
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      boolean enabled = this.enabledArg.get(context);
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.getState(playerComponent, playerRef);
      BuilderToolsUserData userData = builderState.getUserData();
      userData.setRecordSelectionHistory(enabled);
      context.sendMessage(Message.translation("server.commands.selectionHistory.set").param("enabled", enabled));
   }
}
