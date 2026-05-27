package com.hypixel.hytale.builtin.locate;

import com.hypixel.hytale.builtin.locate.command.LocateCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;

public class LocatePlugin extends JavaPlugin {
   public LocatePlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      this.getCommandRegistry().registerCommand(new LocateCommand());
   }

   @Override
   protected void start() {
   }

   @Override
   protected void shutdown() {
   }
}
