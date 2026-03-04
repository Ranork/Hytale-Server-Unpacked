package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.codec.validation.validator.RangeValidator;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import javax.annotation.Nonnull;

public class SetToolHistorySizeCommand extends CommandBase {
   @Nonnull
   private final RequiredArg<Integer> historyLengthArg = this.withRequiredArg(
         "historyLength", "server.commands.settoolhistorysize.historyLength.desc", ArgTypes.INTEGER
      )
      .addValidator(new RangeValidator<>(10, 250, true));

   public SetToolHistorySizeCommand() {
      super("setToolHistorySize", "server.commands.settoolhistorysize.desc");
      this.setPermissionGroup(GameMode.Creative);
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      BuilderToolsPlugin.get().setToolHistorySize(this.historyLengthArg.get(context));
      context.sendMessage(Message.translation("server.commands.settoolhistorysize.set").param("size", this.historyLengthArg.get(context)));
   }
}
