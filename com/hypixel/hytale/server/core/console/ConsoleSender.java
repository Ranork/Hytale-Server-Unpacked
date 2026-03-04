package com.hypixel.hytale.server.core.console;

import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.util.MessageUtil;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;

public class ConsoleSender implements CommandSender {
   public static final ConsoleSender INSTANCE = new ConsoleSender();
   private final UUID uuid = new UUID(0L, 0L);

   protected ConsoleSender() {
   }

   @Override
   public void sendMessage(@Nonnull Message message) {
      Terminal terminal = ConsoleModule.get().getTerminal();
      AttributedString attributedString = MessageUtil.toAnsiString(message);
      HytaleLoggerBackend.rawLog(attributedString.toAnsi(terminal));
   }

   @Nonnull
   @Override
   public String getDisplayName() {
      return "Console";
   }

   @Nonnull
   @Override
   public UUID getUuid() {
      return this.uuid;
   }

   @Override
   public boolean hasPermission(@Nonnull String id) {
      return true;
   }

   @Override
   public boolean hasPermission(@Nonnull String id, boolean def) {
      return true;
   }
}
