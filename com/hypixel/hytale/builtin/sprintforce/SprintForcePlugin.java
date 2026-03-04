package com.hypixel.hytale.builtin.sprintforce;

import com.hypixel.hytale.protocol.packets.setup.ClientFeature;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;

public class SprintForcePlugin extends JavaPlugin {
   public SprintForcePlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      this.getClientFeatureRegistry().register(ClientFeature.SprintForce);
   }
}
