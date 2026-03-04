package com.hypixel.hytale.server.core.command.commands.utility;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public class EventTitleCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_EVENT_TITLE_TITLE_REQUIRED = Message.translation("server.commands.eventtitle.titleRequired");
   @Nonnull
   private final FlagArg majorFlag = this.withFlagArg("major", "server.commands.eventtitle.major.desc");
   @Nonnull
   private final DefaultArg<String> secondaryTitleArg = this.withDefaultArg(
      "secondary", "server.commands.eventtitle.secondary.desc", ArgTypes.STRING, "Event", "server.commands.eventtitle.secondary.defaultDesc"
   );
   @Nonnull
   private final OptionalArg<String> primaryTitleArg = this.withOptionalArg("title", "server.commands.eventtitle.title.desc", ArgTypes.STRING);

   public EventTitleCommand() {
      super("eventtitle", "server.commands.eventtitle.desc");
      this.setAllowsExtraArguments(true);
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      String primaryTitleText;
      if (this.primaryTitleArg.provided(context)) {
         primaryTitleText = this.primaryTitleArg.get(context).replace("\"", "");
      } else {
         String inputString = context.getInputString();
         String rawArgs = CommandUtil.stripCommandName(inputString);
         if (rawArgs.trim().isEmpty()) {
            context.sendMessage(MESSAGE_COMMANDS_EVENT_TITLE_TITLE_REQUIRED);
            return;
         }

         primaryTitleText = this.extractTitleFromRawInput(rawArgs, context);
         if (primaryTitleText.trim().isEmpty()) {
            context.sendMessage(MESSAGE_COMMANDS_EVENT_TITLE_TITLE_REQUIRED);
            return;
         }
      }

      Message primaryTitle = Message.raw(primaryTitleText);
      Message secondaryTitle = Message.raw(this.secondaryTitleArg.get(context));
      boolean isMajor = this.majorFlag.get(context);

      for (World world : Universe.get().getWorlds().values()) {
         for (PlayerRef playerRef : world.getPlayerRefs()) {
            EventTitleUtil.showEventTitleToPlayer(playerRef, primaryTitle, secondaryTitle, isMajor);
         }
      }
   }

   @Nonnull
   private String extractTitleFromRawInput(@Nonnull String rawArgs, @Nonnull CommandContext context) {
      String titleText = rawArgs.trim();
      if (this.majorFlag.get(context)) {
         titleText = titleText.replaceAll("--major\\b", "").trim();
      }

      if (this.secondaryTitleArg.provided(context)) {
         String secondaryValue = this.secondaryTitleArg.get(context);
         titleText = titleText.replaceAll("--secondary\\s*=\\s*" + Pattern.quote(secondaryValue), "");
         titleText = titleText.replaceAll("--secondary\\s+" + Pattern.quote(secondaryValue), "");
      } else {
         titleText = titleText.replaceAll("--secondary\\s*=\\s*[^\\s]+", "");
         titleText = titleText.replaceAll("--secondary\\s+[^\\s]+", "");
      }

      return titleText.trim();
   }
}
