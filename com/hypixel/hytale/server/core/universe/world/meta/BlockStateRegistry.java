package com.hypixel.hytale.server.core.universe.world.meta;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.function.consumer.BooleanConsumer;
import com.hypixel.hytale.registry.Registry;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.StateData;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockStateRegistry extends Registry<BlockStateRegistration> {
   public BlockStateRegistry(@Nonnull List<BooleanConsumer> registrations, BooleanSupplier precondition, String preconditionMessage) {
      super(registrations, precondition, preconditionMessage, BlockStateRegistration::new);
   }

   @Nullable
   public <T extends BlockState> BlockStateRegistration registerBlockState(@Nonnull Class<T> clazz, @Nonnull String key, Codec<T> codec) {
      this.checkPrecondition();
      return this.register(BlockStateModule.get().registerBlockState(clazz, key, codec));
   }

   @Nullable
   public <T extends BlockState, D extends StateData> BlockStateRegistration registerBlockState(
      @Nonnull Class<T> clazz, @Nonnull String key, Codec<T> codec, Class<D> dataClass, Codec<D> dataCodec
   ) {
      this.checkPrecondition();
      return this.register(BlockStateModule.get().registerBlockState(clazz, key, codec, dataClass, dataCodec));
   }
}
