package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.server.core.Message;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class WrappedArgumentType<DataType> extends SingleArgumentType<DataType> {
   protected final ArgumentType<DataType> wrappedArgumentType;

   public WrappedArgumentType(Message name, ArgumentType<DataType> wrappedArgumentType, @Nonnull String argumentUsage, @Nullable String... examples) {
      super(name, argumentUsage, examples);
      this.wrappedArgumentType = wrappedArgumentType;
   }

   @Nonnull
   @Override
   public String[] getExamples() {
      return Arrays.equals((Object[])this.examples, (Object[])EMPTY_EXAMPLES) ? this.wrappedArgumentType.getExamples() : this.examples;
   }

   @Nullable
   public DataType get(@Nonnull MultiArgumentContext context) {
      return context.get(this);
   }
}
