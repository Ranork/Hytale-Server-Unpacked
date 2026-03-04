package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.server.core.command.system.ParseResult;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MultiArgumentContext {
   @Nonnull
   private final Map<ArgumentType<?>, Object> parsedArguments = new Object2ObjectOpenHashMap();

   public void registerArgumentValues(@Nonnull ArgumentType<?> argumentType, @Nonnull String[] values, @Nonnull ParseResult parseResult) {
      this.parsedArguments.put(argumentType, argumentType.parse(values, parseResult));
   }

   @Nullable
   public <DataType> DataType get(@Nonnull ArgumentType<DataType> argumentType) {
      return (DataType)this.parsedArguments.get(argumentType);
   }
}
