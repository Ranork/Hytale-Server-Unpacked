package com.hypixel.hytale.server.core.command.system.arguments.system;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import javax.annotation.Nonnull;

public abstract class WrappedArg<BasicType> {
   @Nonnull
   protected final Argument<?, BasicType> arg;

   public WrappedArg(@Nonnull Argument<?, BasicType> arg) {
      this.arg = arg;
   }

   public boolean provided(@Nonnull CommandContext context) {
      return this.arg.provided(context);
   }

   @Nonnull
   public String getName() {
      return this.arg.getName();
   }

   @Nonnull
   public String getDescription() {
      return this.arg.getDescription();
   }

   @Nonnull
   public <D extends WrappedArg<BasicType>> D addAliases(@Nonnull String... aliases) {
      if (this.arg instanceof AbstractOptionalArg abstractOptionalArg) {
         abstractOptionalArg.addAliases(aliases);
         return (D)this;
      } else {
         throw new UnsupportedOperationException(
            "You are trying to add aliases to a wrapped arg that is wrapping a RequiredArgument. RequiredArguments do not accept aliases"
         );
      }
   }

   @Nonnull
   public Argument<?, BasicType> getArg() {
      return this.arg;
   }

   protected BasicType get(@Nonnull CommandContext context) {
      return this.arg.get(context);
   }
}
