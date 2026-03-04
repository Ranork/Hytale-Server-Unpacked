package com.hypixel.hytale.server.core.command.commands.debug.server;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.util.DumpUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class ServerDumpCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_SERVER_DUMP_DUMPING_STATE = Message.translation("server.commands.server.dump.dumpingState");
   @Nonnull
   private final FlagArg jsonFlag = this.withFlagArg("json", "server.commands.server.dump.json.desc");

   public ServerDumpCommand() {
      super("dump", "server.commands.server.dump.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      context.sendMessage(MESSAGE_COMMANDS_SERVER_DUMP_DUMPING_STATE);
      if (this.jsonFlag.provided(context)) {
         try {
            Path path = DumpUtil.dumpToJson();
            context.sendMessage(Message.translation("server.commands.server.dump.finished").param("filepath", path.toAbsolutePath().toString()));
         } catch (IOException var3) {
            context.sendMessage(Message.translation("server.commands.server.dump.error").param("error", var3.getMessage()));
            throw SneakyThrow.sneakyThrow(var3);
         }
      } else {
         Path file = DumpUtil.dump(false, false);
         context.sendMessage(Message.translation("server.commands.server.dump.finished").param("filepath", file.toAbsolutePath().toString()));
      }
   }
}
