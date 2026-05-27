package com.hypixel.hytale.server.core.io.commands;

import com.hypixel.hytale.protocol.io.ServerListener;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.io.ServerManager;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class BindingsCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_IO_SERVER_MANAGER_BINDINGS = Message.translation("server.io.servermanager.bindings");

   public BindingsCommand() {
      super("bindings", "server.io.servermanager.bindings.description");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      List<ServerListener> listeners = ServerManager.get().getListeners();
      context.sendMessage(
         MessageFormat.list(MESSAGE_IO_SERVER_MANAGER_BINDINGS, listeners.stream().map(Object::toString).map(Message::raw).collect(Collectors.toSet()))
      );
   }
}
