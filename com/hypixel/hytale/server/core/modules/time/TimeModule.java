package com.hypixel.hytale.server.core.modules.time;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.modules.time.commands.TimeCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class TimeModule extends JavaPlugin {
   @Nonnull
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(TimeModule.class).build();
   private static TimeModule instance;
   private ResourceType<EntityStore, WorldTimeResource> worldTimeResourceType;
   private ResourceType<EntityStore, TimeResource> timeResourceType;

   public static TimeModule get() {
      return instance;
   }

   public TimeModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      this.getCommandRegistry().registerCommand(new TimeCommand());
      this.worldTimeResourceType = entityStoreRegistry.registerResource(WorldTimeResource.class, WorldTimeResource::new);
      entityStoreRegistry.registerSystem(new WorldTimeSystems.Init(this.worldTimeResourceType));
      entityStoreRegistry.registerSystem(new WorldTimeSystems.Ticking(this.worldTimeResourceType));
      this.timeResourceType = entityStoreRegistry.registerResource(TimeResource.class, "Time", TimeResource.CODEC);
      entityStoreRegistry.registerSystem(new TimeSystem(this.timeResourceType));
      entityStoreRegistry.registerSystem(new TimePacketSystem(this.worldTimeResourceType));
   }

   @Nonnull
   public ResourceType<EntityStore, WorldTimeResource> getWorldTimeResourceType() {
      return this.worldTimeResourceType;
   }

   @Nonnull
   public ResourceType<EntityStore, TimeResource> getTimeResourceType() {
      return this.timeResourceType;
   }
}
