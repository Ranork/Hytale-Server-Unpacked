package com.hypixel.hytale.builtin.triggervolumes;

import java.util.Collection;
import javax.annotation.Nonnull;

@FunctionalInterface
public interface AssetSourceProvider {
   @Nonnull
   Collection<String> getAssetIds();
}
