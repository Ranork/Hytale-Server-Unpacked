package com.hypixel.hytale.server.core.command.commands.utility.lighting;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

abstract class LightingSendToggleCommand extends AbstractWorldCommand {
   @Nonnull
   private final String statusTranslationKey;
   @Nonnull
   private final BooleanSupplier getter;
   @Nonnull
   private final Consumer<Boolean> setter;
   @Nonnull
   private final OptionalArg<Boolean> enabledArg;

   protected LightingSendToggleCommand(
      @Nonnull String name,
      @Nonnull String description,
      @Nonnull String enabledDesc,
      @Nonnull String statusTranslationKey,
      @Nonnull BooleanSupplier getter,
      @Nonnull Consumer<Boolean> setter
   ) {
      super(name, description);
      this.statusTranslationKey = statusTranslationKey;
      this.getter = getter;
      this.setter = setter;
      this.enabledArg = this.withOptionalArg("enabled", enabledDesc, ArgTypes.BOOLEAN);
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      Boolean enabled = this.enabledArg.provided(context) ? this.enabledArg.get(context) : null;
      Boolean newValue = Objects.requireNonNullElseGet(enabled, () -> !this.getter.getAsBoolean());
      this.setter.accept(newValue);
      context.sendMessage(Message.translation(this.statusTranslationKey).param("enabled", newValue.toString()));
   }
}
