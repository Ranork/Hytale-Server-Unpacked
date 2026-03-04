package com.hypixel.hytale.server.core.modules.anchoraction;

import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import javax.annotation.Nonnull;

@FunctionalInterface
public interface AnchorActionHandler {
   void handle(@Nonnull PlayerRef var1, @Nonnull JsonObject var2);
}
