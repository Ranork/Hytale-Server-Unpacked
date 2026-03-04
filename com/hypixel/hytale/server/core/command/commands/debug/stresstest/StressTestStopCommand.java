package com.hypixel.hytale.server.core.command.commands.debug.stresstest;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class StressTestStopCommand extends AbstractAsyncCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_STRESS_TEST_NOT_STARTED = Message.translation("server.commands.stresstest.notStarted");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_STRESS_TEST_STOPPED = Message.translation("server.commands.stresstest.stopped");

   public StressTestStopCommand() {
      super("stop", "server.commands.stresstest.stop.desc");
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      if (!StressTestStartCommand.STATE.compareAndSet(StressTestStartCommand.StressTestState.RUNNING, StressTestStartCommand.StressTestState.STOPPING)) {
         context.sendMessage(MESSAGE_COMMANDS_STRESS_TEST_NOT_STARTED);
         return CompletableFuture.completedFuture(null);
      } else {
         StressTestStartCommand.stop();
         context.sendMessage(MESSAGE_COMMANDS_STRESS_TEST_STOPPED);
         return CompletableFuture.completedFuture(null);
      }
   }
}
