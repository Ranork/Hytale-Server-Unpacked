package com.hypixel.hytale.builtin.landiscovery;

import com.hypixel.hytale.protocol.packets.serveraccess.Access;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerRequestAccessEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LANDiscoveryPlugin extends JavaPlugin {
   @Nullable
   private LANDiscoveryThread lanDiscoveryThread;
   private static LANDiscoveryPlugin instance;

   public static LANDiscoveryPlugin get() {
      return instance;
   }

   public LANDiscoveryPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      instance = this;
      this.getCommandRegistry().registerCommand(new LANDiscoveryCommand());
      this.getEventRegistry().registerGlobal(SingleplayerRequestAccessEvent.class, event -> this.setLANDiscoveryEnabled(event.getAccess() != Access.Private));
   }

   @Override
   protected void start() {
      if (this.lanDiscoveryThread != null) {
         throw new IllegalArgumentException("Listener thread already exists!");
      }
   }

   @Override
   protected void shutdown() {
      if (this.lanDiscoveryThread != null) {
         this.setLANDiscoveryEnabled(false);
      }
   }

   public void setLANDiscoveryEnabled(boolean enabled) {
      if (!enabled && this.lanDiscoveryThread != null) {
         this.lanDiscoveryThread.interrupt();
         this.lanDiscoveryThread.getSocket().close();
         this.lanDiscoveryThread = null;
      } else if (enabled && this.lanDiscoveryThread == null) {
         this.lanDiscoveryThread = new LANDiscoveryThread();
         this.lanDiscoveryThread.start();
      }
   }

   public boolean isLANDiscoveryEnabled() {
      return this.lanDiscoveryThread != null;
   }

   @Nullable
   public LANDiscoveryThread getLanDiscoveryThread() {
      return this.lanDiscoveryThread;
   }
}
