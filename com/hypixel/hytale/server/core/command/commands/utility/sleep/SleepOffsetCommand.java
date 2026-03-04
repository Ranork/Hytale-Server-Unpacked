package com.hypixel.hytale.server.core.command.commands.utility.sleep;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.util.thread.TickingThread;
import javax.annotation.Nonnull;

public class SleepOffsetCommand extends CommandBase {
   @Nonnull
   private final FlagArg percentFlag = this.withFlagArg("percent", "server.commands.sleepoffset.percent.desc");
   @Nonnull
   private final OptionalArg<Integer> offsetArg = this.withOptionalArg("offset", "server.commands.sleepoffset.offset.desc", ArgTypes.INTEGER);

   public SleepOffsetCommand() {
      super("offset", "server.commands.sleepoffset.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      if (this.offsetArg.provided(context)) {
         float oldValue = (float)TickingThread.SLEEP_OFFSET;
         int newValue = this.offsetArg.get(context);
         TickingThread.SLEEP_OFFSET = newValue;
         if (this.percentFlag.get(context)) {
            context.sendMessage(Message.translation("server.commands.sleepoffset.setPercent").param("newValue", newValue).param("oldValue", oldValue));
         } else {
            context.sendMessage(Message.translation("server.commands.sleepoffset.set").param("newValue", newValue).param("oldValue", oldValue));
         }
      } else {
         float value = (float)TickingThread.SLEEP_OFFSET;
         if (this.percentFlag.get(context)) {
            context.sendMessage(Message.translation("server.commands.sleepoffset.getPercent").param("value", value));
         } else {
            context.sendMessage(Message.translation("server.commands.sleepoffset.getOffset").param("value", value));
         }
      }
   }
}
