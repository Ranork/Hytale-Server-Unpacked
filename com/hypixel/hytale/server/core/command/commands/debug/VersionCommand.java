package com.hypixel.hytale.server.core.command.commands.debug;

import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import javax.annotation.Nonnull;

public class VersionCommand extends CommandBase {
   public VersionCommand() {
      super("version", "server.commands.version.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      String version = ManifestUtil.getImplementationVersion();
      String patchline = ManifestUtil.getPatchline();
      if ("release".equals(patchline)) {
         context.sendMessage(Message.translation("server.commands.version.response").param("version", version).param("patchline", patchline));
      } else {
         context.sendMessage(
            Message.translation("server.commands.version.response.withEnvironment")
               .param("version", version)
               .param("patchline", patchline)
               .param("environment", "release")
         );
      }
   }
}
