package com.hypixel.hytale.builtin.landiscovery;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import javax.annotation.Nonnull;

public class LANDiscoveryCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_IO_LAN_DISCOVERY_DISABLED = Message.translation("server.io.landiscovery.disabled");
   @Nonnull
   private static final Message MESSAGE_IO_LAN_DISCOVERY_ENABLED = Message.translation("server.io.landiscovery.enabled");
   @Nonnull
   private final OptionalArg<Boolean> enabledArg = this.withOptionalArg("enabled", "server.commands.landiscovery.enabled.desc", ArgTypes.BOOLEAN);

   public LANDiscoveryCommand() {
      super("landiscovery", "server.commands.landiscovery.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      if (!this.enabledArg.provided(context)) {
         LANDiscoveryPlugin plugin = LANDiscoveryPlugin.get();
         if (plugin.getLanDiscoveryThread() == null) {
            plugin.setLANDiscoveryEnabled(true);
            context.sendMessage(MESSAGE_IO_LAN_DISCOVERY_ENABLED);
         } else {
            plugin.setLANDiscoveryEnabled(false);
            context.sendMessage(MESSAGE_IO_LAN_DISCOVERY_DISABLED);
         }
      } else {
         Boolean enabled = this.enabledArg.get(context);
         LANDiscoveryPlugin plugin = LANDiscoveryPlugin.get();
         if (!enabled && plugin.getLanDiscoveryThread() != null) {
            plugin.setLANDiscoveryEnabled(false);
            context.sendMessage(MESSAGE_IO_LAN_DISCOVERY_DISABLED);
         } else if (enabled && plugin.getLanDiscoveryThread() == null) {
            plugin.setLANDiscoveryEnabled(true);
            context.sendMessage(MESSAGE_IO_LAN_DISCOVERY_ENABLED);
         } else {
            context.sendMessage(Message.translation("server.io.landiscovery.alreadyToggled").param("enabled", enabled.toString()));
         }
      }
   }
}
