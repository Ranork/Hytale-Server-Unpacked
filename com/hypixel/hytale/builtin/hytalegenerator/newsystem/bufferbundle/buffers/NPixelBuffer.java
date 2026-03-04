package com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers;

import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class NPixelBuffer<T> extends NBuffer {
   public static final int BUFFER_SIZE_BITS = 3;
   @Nonnull
   public static final Vector3i SIZE = new Vector3i(8, 1, 8);

   @Nullable
   public abstract T getPixelContent(@Nonnull Vector3i var1);

   public abstract void setPixelContent(@Nonnull Vector3i var1, @Nullable T var2);

   @Nonnull
   public abstract Class<T> getPixelType();
}
