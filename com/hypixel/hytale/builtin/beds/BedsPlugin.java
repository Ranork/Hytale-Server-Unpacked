package com.hypixel.hytale.builtin.beds;

import com.hypixel.hytale.builtin.beds.interactions.BedInteraction;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSomnolence;
import com.hypixel.hytale.builtin.beds.sleep.components.SleepTracker;
import com.hypixel.hytale.builtin.beds.sleep.resources.WorldSomnolence;
import com.hypixel.hytale.builtin.beds.sleep.systems.player.EnterBedSystem;
import com.hypixel.hytale.builtin.beds.sleep.systems.player.RegisterTrackerSystem;
import com.hypixel.hytale.builtin.beds.sleep.systems.player.SleepNotificationSystem;
import com.hypixel.hytale.builtin.beds.sleep.systems.player.UpdateSleepPacketSystem;
import com.hypixel.hytale.builtin.beds.sleep.systems.player.WakeUpOnDismountSystem;
import com.hypixel.hytale.builtin.beds.sleep.systems.world.StartSlumberSystem;
import com.hypixel.hytale.builtin.beds.sleep.systems.world.UpdateWorldSlumberSystem;
import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class BedsPlugin extends JavaPlugin {
   private static BedsPlugin instance;
   private ComponentType<EntityStore, PlayerSomnolence> playerSomnolenceComponentType;
   private ComponentType<EntityStore, SleepTracker> sleepTrackerComponentType;
   private ResourceType<EntityStore, WorldSomnolence> worldSomnolenceResourceType;

   public static BedsPlugin getInstance() {
      return instance;
   }

   public BedsPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      instance = this;
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      this.playerSomnolenceComponentType = entityStoreRegistry.registerComponent(PlayerSomnolence.class, PlayerSomnolence::new);
      this.sleepTrackerComponentType = entityStoreRegistry.registerComponent(SleepTracker.class, SleepTracker::new);
      this.worldSomnolenceResourceType = entityStoreRegistry.registerResource(WorldSomnolence.class, WorldSomnolence::new);
      ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();
      ComponentType<EntityStore, MountedComponent> mountedComponentType = MountedComponent.getComponentType();
      ResourceType<EntityStore, WorldTimeResource> worldTimeResourceType = WorldTimeResource.getResourceType();
      entityStoreRegistry.registerSystem(new RegisterTrackerSystem(playerRefComponentType, this.sleepTrackerComponentType));
      entityStoreRegistry.registerSystem(new WakeUpOnDismountSystem(mountedComponentType, this.playerSomnolenceComponentType));
      entityStoreRegistry.registerSystem(new EnterBedSystem(mountedComponentType, playerRefComponentType));
      entityStoreRegistry.registerSystem(
         new UpdateSleepPacketSystem(
            playerRefComponentType, this.playerSomnolenceComponentType, this.sleepTrackerComponentType, this.worldSomnolenceResourceType, worldTimeResourceType
         )
      );
      entityStoreRegistry.registerSystem(new StartSlumberSystem(this.playerSomnolenceComponentType, this.worldSomnolenceResourceType, worldTimeResourceType));
      entityStoreRegistry.registerSystem(
         new UpdateWorldSlumberSystem(this.playerSomnolenceComponentType, this.worldSomnolenceResourceType, worldTimeResourceType)
      );
      entityStoreRegistry.registerSystem(new SleepNotificationSystem());
      Interaction.CODEC.register("Bed", BedInteraction.class, BedInteraction.CODEC);
   }

   public ComponentType<EntityStore, PlayerSomnolence> getPlayerSomnolenceComponentType() {
      return this.playerSomnolenceComponentType;
   }

   public ComponentType<EntityStore, SleepTracker> getSleepTrackerComponentType() {
      return this.sleepTrackerComponentType;
   }

   public ResourceType<EntityStore, WorldSomnolence> getWorldSomnolenceResourceType() {
      return this.worldSomnolenceResourceType;
   }
}
