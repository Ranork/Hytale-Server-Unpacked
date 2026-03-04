package com.hypixel.hytale.builtin.portals;

import com.hypixel.hytale.builtin.instances.removal.RemovalCondition;
import com.hypixel.hytale.builtin.portals.commands.FragmentCommands;
import com.hypixel.hytale.builtin.portals.commands.player.LeaveCommand;
import com.hypixel.hytale.builtin.portals.commands.utils.CursedHeldItemCommand;
import com.hypixel.hytale.builtin.portals.commands.voidevent.VoidEventCommands;
import com.hypixel.hytale.builtin.portals.components.PortalDevice;
import com.hypixel.hytale.builtin.portals.components.voidevent.VoidEvent;
import com.hypixel.hytale.builtin.portals.components.voidevent.VoidSpawner;
import com.hypixel.hytale.builtin.portals.integrations.PortalGameplayConfig;
import com.hypixel.hytale.builtin.portals.integrations.PortalMarkerProvider;
import com.hypixel.hytale.builtin.portals.integrations.PortalRemovalCondition;
import com.hypixel.hytale.builtin.portals.interactions.EnterPortalInteraction;
import com.hypixel.hytale.builtin.portals.interactions.ReturnPortalInteraction;
import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.builtin.portals.systems.CloseWorldWhenBreakingDeviceSystems;
import com.hypixel.hytale.builtin.portals.systems.PortalInvalidDestinationSystem;
import com.hypixel.hytale.builtin.portals.systems.PortalTrackerSystems;
import com.hypixel.hytale.builtin.portals.systems.curse.CurseItemDropsSystem;
import com.hypixel.hytale.builtin.portals.systems.curse.DeleteCursedItemsOnSpawnSystem;
import com.hypixel.hytale.builtin.portals.systems.curse.DiedInPortalSystem;
import com.hypixel.hytale.builtin.portals.systems.voidevent.StartVoidEventInFragmentSystem;
import com.hypixel.hytale.builtin.portals.systems.voidevent.VoidEventRefSystem;
import com.hypixel.hytale.builtin.portals.systems.voidevent.VoidEventStagesSystem;
import com.hypixel.hytale.builtin.portals.systems.voidevent.VoidInvasionPortalsSpawnSystem;
import com.hypixel.hytale.builtin.portals.systems.voidevent.VoidSpawnerSystems;
import com.hypixel.hytale.builtin.portals.ui.PortalDevicePageSupplier;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import javax.annotation.Nonnull;

public class PortalsPlugin extends JavaPlugin {
   private static PortalsPlugin instance;
   private ResourceType<EntityStore, PortalWorld> portalResourceType;
   private ComponentType<ChunkStore, PortalDevice> portalDeviceComponentType;
   private ComponentType<EntityStore, VoidEvent> voidEventComponentType;
   private ComponentType<EntityStore, VoidSpawner> voidPortalComponentType;
   public static final int MAX_CONCURRENT_FRAGMENTS = 4;

   public static PortalsPlugin getInstance() {
      return instance;
   }

   public PortalsPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      instance = this;
      this.portalResourceType = this.getEntityStoreRegistry().registerResource(PortalWorld.class, PortalWorld::new);
      this.portalDeviceComponentType = this.getChunkStoreRegistry().registerComponent(PortalDevice.class, "Portal", PortalDevice.CODEC);
      this.voidEventComponentType = this.getEntityStoreRegistry().registerComponent(VoidEvent.class, VoidEvent::new);
      this.voidPortalComponentType = this.getEntityStoreRegistry().registerComponent(VoidSpawner.class, VoidSpawner::new);
      this.getCodecRegistry(OpenCustomUIInteraction.PAGE_CODEC).register("PortalDevice", PortalDevicePageSupplier.class, PortalDevicePageSupplier.CODEC);
      this.getCodecRegistry(Interaction.CODEC)
         .register("Portal", EnterPortalInteraction.class, EnterPortalInteraction.CODEC)
         .register("PortalReturn", ReturnPortalInteraction.class, ReturnPortalInteraction.CODEC);
      this.getEventRegistry().registerGlobal(RemoveWorldEvent.class, this::turnOffPortalWhenWorldRemoved);
      this.getEventRegistry()
         .registerGlobal(AddWorldEvent.class, event -> event.getWorld().getWorldMapManager().addMarkerProvider("portals", PortalMarkerProvider.INSTANCE));
      this.getChunkStoreRegistry().registerSystem(new PortalInvalidDestinationSystem());
      this.getChunkStoreRegistry().registerSystem(new CloseWorldWhenBreakingDeviceSystems.ComponentRemoved());
      this.getChunkStoreRegistry().registerSystem(new CloseWorldWhenBreakingDeviceSystems.EntityRemoved());
      this.getEntityStoreRegistry().registerSystem(new PortalTrackerSystems.TrackerSystem());
      this.getEntityStoreRegistry().registerSystem(new PortalTrackerSystems.UiTickingSystem());
      this.getEntityStoreRegistry().registerSystem(new DiedInPortalSystem());
      this.getEntityStoreRegistry().registerSystem(new CurseItemDropsSystem());
      this.getEntityStoreRegistry().registerSystem(new DeleteCursedItemsOnSpawnSystem());
      this.getEntityStoreRegistry().registerSystem(new VoidEventRefSystem());
      this.getEntityStoreRegistry().registerSystem(new VoidInvasionPortalsSpawnSystem());
      this.getEntityStoreRegistry().registerSystem(new VoidSpawnerSystems.Instantiate());
      this.getEntityStoreRegistry().registerSystem(new StartVoidEventInFragmentSystem());
      this.getEntityStoreRegistry().registerSystem(new VoidEventStagesSystem());
      this.getCommandRegistry().registerCommand(new LeaveCommand());
      this.getCommandRegistry().registerCommand(new CursedHeldItemCommand());
      this.getCommandRegistry().registerCommand(new VoidEventCommands());
      this.getCommandRegistry().registerCommand(new FragmentCommands());
      this.getCodecRegistry(RemovalCondition.CODEC).register("Portal", PortalRemovalCondition.class, PortalRemovalCondition.CODEC);
      this.getCodecRegistry(GameplayConfig.PLUGIN_CODEC).register(PortalGameplayConfig.class, "Portal", PortalGameplayConfig.CODEC);
   }

   private void turnOffPortalWhenWorldRemoved(@Nonnull RemoveWorldEvent event) {
      for (World world : Universe.get().getWorlds().values()) {
         if (world != event.getWorld()) {
            world.execute(() -> PortalInvalidDestinationSystem.turnOffPortalsInWorld(world, event.getWorld()));
         }
      }
   }

   public int countActiveFragments() {
      Map<String, World> worlds = Universe.get().getWorlds();
      int count = 0;

      for (World world : worlds.values()) {
         PortalGameplayConfig portalConfig = world.getGameplayConfig().getPluginConfig().get(PortalGameplayConfig.class);
         if (portalConfig != null) {
            count++;
         }
      }

      return count;
   }

   public ResourceType<EntityStore, PortalWorld> getPortalResourceType() {
      return this.portalResourceType;
   }

   public ComponentType<ChunkStore, PortalDevice> getPortalDeviceComponentType() {
      return this.portalDeviceComponentType;
   }

   public ComponentType<EntityStore, VoidEvent> getVoidEventComponentType() {
      return this.voidEventComponentType;
   }

   public ComponentType<EntityStore, VoidSpawner> getVoidPortalComponentType() {
      return this.voidPortalComponentType;
   }
}
