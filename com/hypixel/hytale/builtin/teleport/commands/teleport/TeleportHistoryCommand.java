package com.hypixel.hytale.builtin.teleport.commands.teleport;

import com.hypixel.hytale.builtin.teleport.TeleportPlugin;
import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class TeleportHistoryCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_TELEPORT_HISTORY_EMPTY = Message.translation("server.commands.teleport.history.empty");

   public TeleportHistoryCommand() {
      super("history", "server.commands.teleport.dump.desc");
      this.requirePermission(HytalePermissions.fromCommand("teleport.history"));
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      TeleportHistory history = store.ensureAndGetComponent(ref, TeleportHistory.getComponentType());
      TeleportPlugin.get().getLogger().at(Level.INFO).log("Got history for player %s: %s", playerRefComponent.getUsername(), history);
      int backSize = history.getBackSize();
      int forwardSize = history.getForwardSize();
      if (backSize == 0 && forwardSize == 0) {
         context.sendMessage(MESSAGE_COMMANDS_TELEPORT_HISTORY_EMPTY);
      } else {
         context.sendMessage(Message.translation("server.commands.teleport.history.info").param("backCount", backSize).param("forwardCount", forwardSize));
      }
   }
}
