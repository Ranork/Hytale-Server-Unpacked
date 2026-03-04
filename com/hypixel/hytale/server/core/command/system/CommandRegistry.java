package com.hypixel.hytale.server.core.command.system;

import com.hypixel.hytale.function.consumer.BooleanConsumer;
import com.hypixel.hytale.registry.Registry;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;

public class CommandRegistry extends Registry<CommandRegistration> {
   private final PluginBase plugin;

   public CommandRegistry(@Nonnull List<BooleanConsumer> registrations, BooleanSupplier precondition, String preconditionMessage, PluginBase plugin) {
      super(registrations, precondition, preconditionMessage, CommandRegistration::new);
      this.plugin = plugin;
   }

   public CommandRegistration registerCommand(@Nonnull AbstractCommand command) {
      this.checkPrecondition();
      if (this.plugin != null) {
         command.setOwner(this.plugin);
      }

      return this.register(CommandManager.get().register(command));
   }
}
