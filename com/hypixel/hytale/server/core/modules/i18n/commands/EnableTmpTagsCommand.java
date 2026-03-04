package com.hypixel.hytale.server.core.modules.i18n.commands;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import javax.annotation.Nonnull;

public class EnableTmpTagsCommand extends CommandBase {
   public EnableTmpTagsCommand() {
      super("toggletmptags", "server.commands.toggleTmpTags.desc");
      this.addAliases("tmptag", "tmptags", "tmpstring", "tmpstrings", "tmptext");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      HytaleServerConfig config = HytaleServer.get().getConfig();
      if (config != null) {
         boolean displayTmpTags = !config.isDisplayTmpTagsInStrings();
         config.setDisplayTmpTagsInStrings(!displayTmpTags);
         context.sendMessage(Message.translation("server.commands.toggleTmpTags." + (displayTmpTags ? "enabled" : "disabled")));
      }
   }
}
