package com.hypixel.hytale.builtin.crouchslide;

import com.hypixel.hytale.protocol.packets.setup.ClientFeature;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;

public class CrouchSlidePlugin extends JavaPlugin {
   public CrouchSlidePlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      this.getClientFeatureRegistry().registerClientTag("Allows=Movement");
      this.getClientFeatureRegistry().register(ClientFeature.CrouchSlide);
   }
}
