package com.hypixel.hytale.builtin.teleport.commands.teleport;

import com.hypixel.hytale.builtin.teleport.commands.teleport.variant.TeleportOtherToPlayerCommand;
import com.hypixel.hytale.builtin.teleport.commands.teleport.variant.TeleportPlayerToCoordinatesCommand;
import com.hypixel.hytale.builtin.teleport.commands.teleport.variant.TeleportToCoordinatesCommand;
import com.hypixel.hytale.builtin.teleport.commands.teleport.variant.TeleportToPlayerCommand;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class TeleportCommand extends AbstractCommandCollection {
   public TeleportCommand() {
      super("tp", "server.commands.tp.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.addAliases("teleport");
      this.addUsageVariant(new TeleportToCoordinatesCommand());
      this.addUsageVariant(new TeleportPlayerToCoordinatesCommand());
      this.addUsageVariant(new TeleportToPlayerCommand());
      this.addUsageVariant(new TeleportOtherToPlayerCommand());
      this.addSubCommand(new TeleportAllCommand());
      this.addSubCommand(new TeleportHomeCommand());
      this.addSubCommand(new TeleportTopCommand());
      this.addSubCommand(new TeleportBackCommand());
      this.addSubCommand(new TeleportForwardCommand());
      this.addSubCommand(new TeleportHistoryCommand());
      this.addSubCommand(new TeleportWorldCommand());
   }
}
