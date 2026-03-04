package com.hypixel.hytale.component;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record ComponentRegistration<ECS_TYPE, T extends Component<ECS_TYPE>>(
   @Nonnull Class<? super T> typeClass,
   @Nullable String id,
   @Nullable BuilderCodec<T> codec,
   @Nonnull Supplier<T> supplier,
   @Nonnull ComponentType<ECS_TYPE, T> componentType
) {
}
