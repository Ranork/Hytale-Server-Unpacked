package com.hypixel.hytale.builtin.mounts;

import com.hypixel.hytale.builtin.mounts.commands.MountCommand;
import com.hypixel.hytale.builtin.mounts.interactions.MountInteraction;
import com.hypixel.hytale.builtin.mounts.interactions.SeatingInteraction;
import com.hypixel.hytale.builtin.mounts.interactions.SpawnMinecartInteraction;
import com.hypixel.hytale.builtin.mounts.minecart.MinecartComponent;
import com.hypixel.hytale.builtin.mounts.npc.builders.BuilderActionMount;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.protocol.packets.interaction.DismountNPC;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.io.ServerManager;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerInput;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.systems.RoleChangeSystem;
import javax.annotation.Nonnull;

public class MountPlugin extends JavaPlugin {
   private static MountPlugin instance;
   private ComponentType<ChunkStore, BlockMountComponent> blockMountComponentType;
   private ComponentType<EntityStore, NPCMountComponent> mountComponentType;
   private ComponentType<EntityStore, MountedComponent> mountedComponentType;
   private ComponentType<EntityStore, MountedByComponent> mountedByComponentType;
   private ComponentType<EntityStore, MinecartComponent> minecartComponentType;

   public static MountPlugin getInstance() {
      return instance;
   }

   public MountPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   public ComponentType<EntityStore, NPCMountComponent> getMountComponentType() {
      return this.mountComponentType;
   }

   public ComponentType<EntityStore, MountedComponent> getMountedComponentType() {
      return this.mountedComponentType;
   }

   public ComponentType<EntityStore, MountedByComponent> getMountedByComponentType() {
      return this.mountedByComponentType;
   }

   public ComponentType<EntityStore, MinecartComponent> getMinecartComponentType() {
      return this.minecartComponentType;
   }

   @Override
   protected void setup() {
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      instance = this;
      this.blockMountComponentType = this.getChunkStoreRegistry().registerComponent(BlockMountComponent.class, BlockMountComponent::new);
      NPCPlugin.get().registerCoreComponentType("Mount", BuilderActionMount::new);
      this.mountComponentType = entityStoreRegistry.registerComponent(NPCMountComponent.class, "Mount", NPCMountComponent.CODEC);
      this.mountedComponentType = entityStoreRegistry.registerComponent(MountedComponent.class, () -> {
         throw new UnsupportedOperationException("Mounted component cannot be default constructed");
      });
      this.mountedByComponentType = entityStoreRegistry.registerComponent(MountedByComponent.class, MountedByComponent::new);
      this.minecartComponentType = entityStoreRegistry.registerComponent(MinecartComponent.class, "Minecart", MinecartComponent.CODEC);
      ComponentType<EntityStore, NPCEntity> npcEntityComponentType = NPCEntity.getComponentType();
      ComponentType<EntityStore, NetworkId> networkIdComponentType = NetworkId.getComponentType();
      ComponentType<EntityStore, Player> playerComponentType = Player.getComponentType();
      ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType = EntityTrackerSystems.Visible.getComponentType();
      ComponentType<EntityStore, PlayerInput> playerInputComponentType = PlayerInput.getComponentType();
      ComponentType<EntityStore, MovementStatesComponent> movementStatesComponentType = MovementStatesComponent.getComponentType();
      ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
      ComponentType<EntityStore, Teleport> teleportComponentType = Teleport.getComponentType();
      ComponentType<EntityStore, DeathComponent> deathComponentType = DeathComponent.getComponentType();
      ComponentType<EntityStore, Interactable> interactableComponentType = Interactable.getComponentType();
      ComponentType<EntityStore, PrefabCopyableComponent> prefabCopyableComponentType = PrefabCopyableComponent.getComponentType();
      ResourceType<EntityStore, TimeResource> timeResourceType = TimeResource.getResourceType();
      entityStoreRegistry.registerSystem(new NPCMountSystems.OnAdd(this.mountComponentType, npcEntityComponentType, networkIdComponentType));
      entityStoreRegistry.registerSystem(new NPCMountSystems.DismountOnPlayerDeath(playerComponentType));
      entityStoreRegistry.registerSystem(new NPCMountSystems.DismountOnMountDeath(this.mountComponentType));
      entityStoreRegistry.registerSystem(new NPCMountSystems.OnPlayerRemove(playerComponentType));
      entityStoreRegistry.registerSystem(new MountSystems.TrackerUpdate(visibleComponentType, this.mountedComponentType));
      entityStoreRegistry.registerSystem(new MountSystems.TrackerRemove(this.mountedComponentType, visibleComponentType));
      entityStoreRegistry.registerSystem(new MountSystems.RemoveMountedBy(this.mountedByComponentType, this.mountedComponentType));
      entityStoreRegistry.registerSystem(new MountSystems.RemoveMounted(this.mountedComponentType));
      entityStoreRegistry.registerSystem(new MountSystems.RemoveMountedHolder(this.mountedComponentType));
      entityStoreRegistry.registerSystem(new MountSystems.TeleportMountedEntity(this.mountedComponentType, teleportComponentType));
      entityStoreRegistry.registerSystem(new MountSystems.MountedEntityDeath(this.mountedComponentType, deathComponentType));
      entityStoreRegistry.registerSystem(new MountSystems.PlayerMount(this.mountedComponentType, playerInputComponentType, networkIdComponentType));
      entityStoreRegistry.registerSystem(
         new MountSystems.HandleMountInput(this.mountedComponentType, playerInputComponentType, movementStatesComponentType, transformComponentType)
      );
      entityStoreRegistry.registerSystem(new MountSystems.TrackedMounted(this.mountedComponentType, this.mountedByComponentType));
      entityStoreRegistry.registerSystem(
         new MountSystems.EnsureMinecartComponents(this.minecartComponentType, interactableComponentType, networkIdComponentType, prefabCopyableComponentType)
      );
      entityStoreRegistry.registerSystem(
         new MountSystems.OnMinecartHit(this.minecartComponentType, transformComponentType, playerComponentType, timeResourceType)
      );
      this.getChunkStoreRegistry().registerSystem(new MountSystems.RemoveBlockSeat(this.blockMountComponentType, this.mountedComponentType));
      ServerManager.get().registerSubPacketHandlers(MountGamePacketHandler::new);
      this.getCommandRegistry().registerCommand(new MountCommand());
      Interaction.CODEC.register("SpawnMinecart", SpawnMinecartInteraction.class, SpawnMinecartInteraction.CODEC);
      Interaction.CODEC.register("Mount", MountInteraction.class, MountInteraction.CODEC);
      Interaction.CODEC.register("Seating", SeatingInteraction.class, SeatingInteraction.CODEC);
   }

   public ComponentType<ChunkStore, BlockMountComponent> getBlockMountComponentType() {
      return this.blockMountComponentType;
   }

   public static void checkDismountNpc(@Nonnull ComponentAccessor<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull Player playerComponent) {
      int mountEntityId = playerComponent.getMountEntityId();
      if (mountEntityId != 0) {
         playerComponent.setMountEntityId(0);
         dismountNpc(store, ref, mountEntityId);
      }
   }

   private static void dismountNpc(@Nonnull ComponentAccessor<EntityStore> store, @Nonnull Ref<EntityStore> playerRef, int mountEntityId) {
      Ref<EntityStore> entityReference = store.getExternalData().getRefFromNetworkId(mountEntityId);
      if (entityReference != null && entityReference.isValid()) {
         NPCMountComponent mountComponent = store.getComponent(entityReference, NPCMountComponent.getComponentType());

         assert mountComponent != null;

         resetOriginalMountRole(entityReference, store, mountComponent);
         resetOriginalPlayerMovementSettings(playerRef, store);
      }
   }

   private static void resetOriginalMountRole(
      @Nonnull Ref<EntityStore> entityReference, @Nonnull ComponentAccessor<EntityStore> store, @Nonnull NPCMountComponent mountComponent
   ) {
      NPCEntity npcComponent = store.getComponent(entityReference, NPCEntity.getComponentType());

      assert npcComponent != null;

      RoleChangeSystem.requestRoleChange(entityReference, npcComponent.getRole(), mountComponent.getOriginalRoleIndex(), false, "Idle", null, store);
      store.removeComponent(entityReference, NPCMountComponent.getComponentType());
   }

   public static void resetOriginalPlayerMovementSettings(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> store) {
      PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
      if (playerRef != null) {
         playerRef.getPacketHandler().write(new DismountNPC());
         MovementManager movementManagerComponent = store.getComponent(ref, MovementManager.getComponentType());

         assert movementManagerComponent != null;

         movementManagerComponent.resetDefaultsAndUpdate(ref, store);
      }
   }
}
