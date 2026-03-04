package com.hypixel.hytale.builtin.creativehub.interactions;

import com.hypixel.hytale.builtin.creativehub.CreativeHubPlugin;
import com.hypixel.hytale.builtin.creativehub.config.CreativeHubEntityConfig;
import com.hypixel.hytale.builtin.creativehub.config.CreativeHubWorldConfig;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.PendingTeleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HubPortalInteraction extends SimpleInstantInteraction {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   public static final BuilderCodec<HubPortalInteraction> CODEC = BuilderCodec.builder(
         HubPortalInteraction.class, HubPortalInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Teleports the **Player** to a permanent world, creating it if required.")
      .<String>appendInherited(new KeyedCodec<>("WorldName", Codec.STRING), (o, i) -> o.worldName = i, o -> o.worldName, (o, p) -> o.worldName = p.worldName)
      .documentation("The name of the permanent world to teleport to.")
      .addValidator(Validators.nonNull())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("WorldGenType", Codec.STRING), (o, i) -> o.worldGenType = i, o -> o.worldGenType, (o, p) -> o.worldGenType = p.worldGenType
      )
      .documentation("The world generator type to use when creating the world (e.g., 'Flat', 'Hytale'). Mutually exclusive with InstanceTemplate.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("InstanceTemplate", Codec.STRING),
         (o, i) -> o.instanceTemplate = i,
         o -> o.instanceTemplate,
         (o, p) -> o.instanceTemplate = p.instanceTemplate
      )
      .documentation("Instance asset to use as template for creating the permanent world. Mutually exclusive with WorldGenType.")
      .add()
      .build();
   private String worldName;
   private String worldGenType;
   @Nullable
   private String instanceTemplate;

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Server;
   }

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      Ref<EntityStore> ref = context.getEntity();
      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      if (playerComponent != null && !playerComponent.isWaitingForClientReady()) {
         Archetype<EntityStore> archetype = commandBuffer.getArchetype(ref);
         if (!archetype.contains(Teleport.getComponentType()) && !archetype.contains(PendingTeleport.getComponentType())) {
            World currentWorld = commandBuffer.getExternalData().getWorld();
            Universe universe = Universe.get();
            World targetWorld = universe.getWorld(this.worldName);
            if (targetWorld != null) {
               teleportToLoadedWorld(ref, commandBuffer, targetWorld, playerComponent);
            } else {
               CompletableFuture<World> worldFuture;
               if (this.instanceTemplate != null) {
                  worldFuture = CreativeHubPlugin.get().spawnPermanentWorldFromTemplate(this.instanceTemplate, this.worldName);
               } else if (universe.isWorldLoadable(this.worldName)) {
                  worldFuture = universe.loadWorld(this.worldName);
               } else {
                  worldFuture = universe.addWorld(this.worldName, this.worldGenType, null);
                  worldFuture.thenAccept(world -> {
                     if (world.getWorldConfig().getDisplayName() == null) {
                        world.getWorldConfig().setDisplayName(WorldConfig.formatDisplayName(this.worldName));
                     }
                  });
               }

               teleportToLoadingWorld(ref, commandBuffer, worldFuture, currentWorld, playerComponent);
            }
         }
      }
   }

   private static void teleportToLoadedWorld(
      @Nonnull Ref<EntityStore> playerRef,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nonnull World targetWorld,
      @Nonnull Player playerComponent
   ) {
      Map<String, PlayerWorldData> perWorldData = playerComponent.getPlayerConfigData().getPerWorldData();
      PlayerWorldData worldData = perWorldData.get(targetWorld.getName());
      Transform spawnPoint;
      if (worldData != null && worldData.getLastPosition() != null) {
         spawnPoint = worldData.getLastPosition();
      } else {
         UUIDComponent uuidComponent = componentAccessor.getComponent(playerRef, UUIDComponent.getComponentType());

         assert uuidComponent != null;

         ISpawnProvider spawnProvider = targetWorld.getWorldConfig().getSpawnProvider();
         spawnPoint = spawnProvider != null ? spawnProvider.getSpawnPoint(targetWorld, uuidComponent.getUuid()) : new Transform();
      }

      Teleport teleportComponent = Teleport.createForPlayer(targetWorld, spawnPoint);
      componentAccessor.addComponent(playerRef, Teleport.getComponentType(), teleportComponent);
   }

   private static void teleportToLoadingWorld(
      @Nonnull Ref<EntityStore> playerRef,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nonnull CompletableFuture<World> worldFuture,
      @Nonnull World originalWorld,
      @Nonnull Player playerComponent
   ) {
      TransformComponent transformComponent = componentAccessor.getComponent(playerRef, TransformComponent.getComponentType());
      if (transformComponent == null) {
         LOGGER.at(Level.SEVERE).log("Cannot teleport player %s to permanent world - missing TransformComponent", playerRef);
      } else {
         Transform originalPosition = transformComponent.getTransform().clone();
         PlayerRef playerRefComponent = componentAccessor.getComponent(playerRef, PlayerRef.getComponentType());
         if (playerRefComponent == null) {
            LOGGER.at(Level.SEVERE).log("Cannot teleport player %s to permanent world - missing PlayerRef component", playerRef);
         } else {
            Map<String, PlayerWorldData> perWorldData = playerComponent.getPlayerConfigData().getPerWorldData();
            UUIDComponent uuidComponent = componentAccessor.getComponent(playerRef, UUIDComponent.getComponentType());
            if (uuidComponent == null) {
               LOGGER.at(Level.SEVERE).log("Cannot teleport player %s to permanent world - missing UUIDComponent", playerRef);
            } else {
               UUID playerUUID = uuidComponent.getUuid();
               CreativeHubEntityConfig hubEntityConfig = componentAccessor.getComponent(playerRef, CreativeHubEntityConfig.getComponentType());
               CompletableFuture.runAsync(playerRefComponent::removeFromStore, originalWorld)
                  .thenCombine(worldFuture.orTimeout(1L, TimeUnit.MINUTES), (v, world) -> (World)world)
                  .thenCompose(world -> {
                     PlayerWorldData worldData = perWorldData.get(world.getName());
                     if (worldData != null && worldData.getLastPosition() != null) {
                        return world.addPlayer(playerRefComponent, worldData.getLastPosition(), Boolean.TRUE, Boolean.FALSE);
                     } else {
                        ISpawnProvider spawnProvider = world.getWorldConfig().getSpawnProvider();
                        Transform spawnPoint = spawnProvider != null ? spawnProvider.getSpawnPoint(world, playerUUID) : null;
                        return world.addPlayer(playerRefComponent, spawnPoint, Boolean.TRUE, Boolean.FALSE);
                     }
                  })
                  .whenComplete(
                     (ret, ex) -> {
                        if (ex != null) {
                           ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(ex))
                              .log("Failed to teleport %s to permanent world", playerRefComponent.getUsername());
                        }

                        if (ret == null) {
                           if (originalWorld.isAlive()) {
                              originalWorld.addPlayer(playerRefComponent, originalPosition, Boolean.TRUE, Boolean.FALSE);
                           } else {
                              if (hubEntityConfig != null && hubEntityConfig.getParentHubWorldUuid() != null) {
                                 World parentWorld = Universe.get().getWorld(hubEntityConfig.getParentHubWorldUuid());
                                 if (parentWorld != null) {
                                    CreativeHubWorldConfig parentHubConfig = CreativeHubWorldConfig.get(parentWorld.getWorldConfig());
                                    if (parentHubConfig != null && parentHubConfig.getStartupInstance() != null) {
                                       World hubInstance = CreativeHubPlugin.get().getOrSpawnHubInstance(parentWorld, parentHubConfig, new Transform());
                                       hubInstance.addPlayer(playerRefComponent, null, Boolean.TRUE, Boolean.FALSE);
                                       return;
                                    }
                                 }
                              }

                              World defaultWorld = Universe.get().getDefaultWorld();
                              if (defaultWorld != null) {
                                 defaultWorld.addPlayer(playerRefComponent, null, Boolean.TRUE, Boolean.FALSE);
                              } else {
                                 LOGGER.at(Level.SEVERE).log("No fallback world available for %s, disconnecting", playerRefComponent.getUsername());
                                 playerRefComponent.getPacketHandler().disconnect("Failed to teleport - no world available");
                              }
                           }
                        }
                     }
                  );
            }
         }
      }
   }
}
