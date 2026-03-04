package com.hypixel.hytale.builtin.commandmacro;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class WaitCommand extends AbstractAsyncCommand {
   private static final long MILLISECONDS_TO_SECONDS_MULTIPLIER = 1000L;
   @Nonnull
   public static final Runnable EMPTY_RUNNABLE = () -> {};
   @Nonnull
   private final RequiredArg<Float> timeArg = this.withRequiredArg("time", "server.commands.wait.arg.time", ArgTypes.FLOAT)
      .addValidator(Validators.greaterThan(0.0F))
      .addValidator(Validators.lessThan(1000.0F));
   @Nonnull
   private final FlagArg printArg = this.withFlagArg("print", "server.commands.wait.arg.print");

   public WaitCommand() {
      super("wait", "server.commands.wait.desc");
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      CommandSender sender = context.sender();
      Runnable runnable = this.printArg.get(context) ? () -> sender.sendMessage(Message.translation("server.commands.wait.complete")) : EMPTY_RUNNABLE;
      return CompletableFuture.runAsync(runnable, CompletableFuture.delayedExecutor((long)(this.timeArg.get(context) * 1000.0F), TimeUnit.MILLISECONDS));
   }
}
