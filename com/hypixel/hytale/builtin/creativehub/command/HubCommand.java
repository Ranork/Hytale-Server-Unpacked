package com.hypixel.hytale.builtin.creativehub.command;

import com.hypixel.hytale.builtin.creativehub.CreativeHubPlugin;
import com.hypixel.hytale.builtin.creativehub.config.CreativeHubEntityConfig;
import com.hypixel.hytale.builtin.creativehub.config.CreativeHubWorldConfig;
import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HubCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_NOT_IN_HUB_WORLD = Message.translation("server.commands.hub.notInHubWorld");
   @Nonnull
   private static final Message MESSAGE_ALREADY_IN_HUB = Message.translation("server.commands.hub.alreadyInHub");

   public HubCommand() {
      super("hub", "server.commands.hub.desc");
      this.addAliases("cosmos", "crossroads");
      this.setPermissionGroup(GameMode.Creative);
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      World parentWorld = findParentHubWorld(store, ref);
      if (parentWorld == null) {
         playerRef.sendMessage(MESSAGE_NOT_IN_HUB_WORLD);
      } else {
         CreativeHubWorldConfig hubConfig = CreativeHubWorldConfig.get(parentWorld.getWorldConfig());
         if (hubConfig != null && hubConfig.getStartupInstance() != null) {
            World currentHub = CreativeHubPlugin.get().getActiveHubInstance(parentWorld);
            if (world.equals(currentHub)) {
               playerRef.sendMessage(MESSAGE_ALREADY_IN_HUB);
            } else {
               ISpawnProvider spawnProvider = parentWorld.getWorldConfig().getSpawnProvider();
               Transform returnPoint = spawnProvider != null ? spawnProvider.getSpawnPoint(parentWorld, playerRef.getUuid()) : new Transform();
               World hubInstance = CreativeHubPlugin.get().getOrSpawnHubInstance(parentWorld, hubConfig, returnPoint);
               InstancesPlugin.teleportPlayerToInstance(ref, store, hubInstance, null);
            }
         } else {
            playerRef.sendMessage(MESSAGE_NOT_IN_HUB_WORLD);
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
