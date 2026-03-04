package com.hypixel.hytale.procedurallib.file;

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;

public interface AssetLoader<T> {
   Class<T> type();

   @Nonnull
   T load(@Nonnull InputStream var1) throws IOException;
}
