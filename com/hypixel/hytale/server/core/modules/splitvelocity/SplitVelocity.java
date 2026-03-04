package com.hypixel.hytale.server.core.modules.splitvelocity;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.protocol.packets.setup.ClientFeature;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;

public class SplitVelocity extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(SplitVelocity.class).build();
   public static boolean SHOULD_MODIFY_VELOCITY = true;

   public SplitVelocity(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      this.getClientFeatureRegistry().register(ClientFeature.SplitVelocity);
      SHOULD_MODIFY_VELOCITY = false;
   }

   @Override
   protected void shutdown() {
      SHOULD_MODIFY_VELOCITY = true;
   }
}
