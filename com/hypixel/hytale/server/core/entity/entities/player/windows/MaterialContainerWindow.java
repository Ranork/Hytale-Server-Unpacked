package com.hypixel.hytale.server.core.entity.entities.player.windows;

import javax.annotation.Nonnull;

public interface MaterialContainerWindow {
   @Nonnull
   MaterialExtraResourcesSection getExtraResourcesSection();

   void invalidateExtraResources();

   boolean isValid();
}
