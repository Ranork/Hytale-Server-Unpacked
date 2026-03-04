package com.hypixel.hytale.builtin.creativehub.ui;

import com.hypixel.hytale.builtin.creativehub.CreativeHubPlugin;
import com.hypixel.hytale.builtin.creativehub.config.CreativeHubEntityConfig;
import com.hypixel.hytale.builtin.creativehub.config.CreativeHubWorldConfig;
import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.UpdateAnchorUI;
import com.hypixel.hytale.server.core.modules.anchoraction.AnchorActionModule;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ReturnToHubButtonUI {
   public static final String ANCHOR_ID = "MapServerContent";
   public static final String ACTION_RETURN_TO_HUB = "returnToHub";

   private ReturnToHubButtonUI() {
   }

   public static void register() {
      AnchorActionModule.get().register("returnToHub", (playerRef, ref, store, data) -> executeReturnToHub(playerRef, ref, store));
   }

   public static void send(@Nonnull PlayerRef playerRef) {
      send(playerRef, false);
   }

   public static void send(@Nonnull PlayerRef playerRef, boolean disabled) {
      UICommandBuilder commandBuilder = new UICommandBuilder();
      commandBuilder.append("Hud/ReturnToHubButton.ui");
      commandBuilder.set("#ReturnToHubButton.Disabled", disabled);
      UIEventBuilder eventBuilder = new UIEventBuilder();
      if (!disabled) {
         eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ReturnToHubButton", EventData.of("action", "returnToHub"), false);
      }

      playerRef.getPacketHandler().writeNoCache(new UpdateAnchorUI("MapServerContent", true, commandBuilder.getCommands(), eventBuilder.getEvents()));
   }

   public static void clear(@Nonnull PlayerRef playerRef) {
      playerRef.getPacketHandler().writeNoCache(new UpdateAnchorUI("MapServerContent", true, null, null));
   }

   public static void executeReturnToHub(@Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      World parentWorld = findParentHubWorld(store, ref);
      if (parentWorld != null) {
         CreativeHubWorldConfig hubConfig = CreativeHubWorldConfig.get(parentWorld.getWorldConfig());
         if (hubConfig != null && hubConfig.getStartupInstance() != null) {
            World world = store.getExternalData().getWorld();
            World currentHub = CreativeHubPlugin.get().getActiveHubInstance(parentWorld);
            if (!world.equals(currentHub)) {
               ISpawnProvider spawnProvider = parentWorld.getWorldConfig().getSpawnProvider();
               Transform returnPoint = spawnProvider != null ? spawnProvider.getSpawnPoint(parentWorld, playerRef.getUuid()) : new Transform();
               World hubInstance = CreativeHubPlugin.get().getOrSpawnHubInstance(parentWorld, hubConfig, returnPoint);
               InstancesPlugin.teleportPlayerToInstance(ref, store, hubInstance, null);
            }
         }
      }
   }

   @Nullable
   private static World findParentHubWorld(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref) {
      CreativeHubEntityConfig hubEntityConfig = store.getComponent(ref, CreativeHubEntityConfig.getComponentType());
      if (hubEntityConfig != null && hubEntityConfig.getParentHubWorldUuid() != null) {
         World parentWorld = Universe.get().getWorld(hubEntityConfig.getParentHubWorldUuid());
         if (parentWorld != null) {
            CreativeHubWorldConfig hubConfig = CreativeHubWorldConfig.get(parentWorld.getWorldConfig());
            if (hubConfig != null && hubConfig.getStartupInstance() != null) {
               return parentWorld;
            }
         }
      }

      return null;
   }
}
