package com.hypixel.hytale.server.core.plugin;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.pages.PluginListPage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;

public class PluginListPageManager {
   public static PluginListPageManager instance;
   @Nonnull
   private final List<PluginListPage> activePages = new CopyOnWriteArrayList<>();

   public PluginListPageManager() {
      instance = this;
   }

   @Nonnull
   public static PluginListPageManager get() {
      return instance;
   }

   public void registerPluginListPage(@Nonnull PluginListPage page) {
      if (!this.activePages.contains(page)) {
         this.activePages.add(page);
      }
   }

   public void deregisterPluginListPage(@Nonnull PluginListPage page) {
      if (this.activePages.contains(page)) {
         this.activePages.remove(page);
      }
   }

   public void notifyPluginChange(@Nonnull Map<PluginIdentifier, PluginBase> plugins, @Nonnull PluginIdentifier pluginIdentifier) {
      PluginBase plugin = plugins.get(pluginIdentifier);
      this.activePages.forEach(page -> page.handlePluginChangeEvent(pluginIdentifier, plugin != null && plugin.isEnabled()));
   }

   public static class SessionSettings implements Component<EntityStore> {
      public boolean descriptiveOnly;

      public SessionSettings() {
         this.descriptiveOnly = true;
      }

      public SessionSettings(boolean descriptiveOnly) {
         this.descriptiveOnly = descriptiveOnly;
      }

      @Nonnull
      public static ComponentType<EntityStore, PluginListPageManager.SessionSettings> getComponentType() {
         return PluginManager.get().getSessionSettingsComponentType();
      }

      @Nonnull
      @Override
      public Component<EntityStore> clone() {
         return new PluginListPageManager.SessionSettings(this.descriptiveOnly);
      }
   }
}
