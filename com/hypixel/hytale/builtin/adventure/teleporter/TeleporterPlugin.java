package com.hypixel.hytale.builtin.adventure.teleporter;

import com.hypixel.hytale.builtin.adventure.teleporter.component.Teleporter;
import com.hypixel.hytale.builtin.adventure.teleporter.interaction.server.TeleporterInteraction;
import com.hypixel.hytale.builtin.adventure.teleporter.interaction.server.UsedTeleporter;
import com.hypixel.hytale.builtin.adventure.teleporter.page.TeleporterSettingsPageSupplier;
import com.hypixel.hytale.builtin.adventure.teleporter.system.ClearUsedTeleporterSystem;
import com.hypixel.hytale.builtin.adventure.teleporter.system.CreateWarpWhenTeleporterPlacedSystem;
import com.hypixel.hytale.builtin.adventure.teleporter.system.TurnOffTeleportersSystem;
import com.hypixel.hytale.builtin.teleport.TeleportPlugin;
import com.hypixel.hytale.builtin.teleport.Warp;
import com.hypixel.hytale.builtin.teleport.commands.event.ModifyWarpEvent;
import com.hypixel.hytale.builtin.teleport.commands.event.RemoveWarpEvent;
import com.hypixel.hytale.builtin.teleport.commands.event.ReplaceWarpEvent;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.PendingTeleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.TeleportRecord;
import com.hypixel.hytale.server.core.modules.interaction.components.PlacedByInteractionComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TeleporterPlugin extends JavaPlugin {
   private static TeleporterPlugin instance;
   private static final Message MESSAGE_TELEPORTER_PROTECTION_REASON = Message.translation("server.commands.teleporter.warp.protected");
   private ComponentType<ChunkStore, Teleporter> teleporterComponentType;
   private ComponentType<EntityStore, UsedTeleporter> usedTeleporterComponentType;

   public static TeleporterPlugin get() {
      return instance;
   }

   public TeleporterPlugin(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      ComponentRegistryProxy<ChunkStore> chunkStoreRegistry = this.getChunkStoreRegistry();
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      this.teleporterComponentType = chunkStoreRegistry.registerComponent(Teleporter.class, "Teleporter", Teleporter.CODEC);
      ComponentType<ChunkStore, PlacedByInteractionComponent> placedByInteractionComponentType = PlacedByInteractionComponent.getComponentType();
      ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoComponentType = BlockModule.BlockStateInfo.getComponentType();
      ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();
      ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
      ComponentType<EntityStore, TeleportRecord> teleportRecordComponentType = TeleportRecord.getComponentType();
      ComponentType<EntityStore, Teleport> teleportComponentType = Teleport.getComponentType();
      ComponentType<EntityStore, PendingTeleport> pendingTeleportComponentType = PendingTeleport.getComponentType();
      chunkStoreRegistry.registerSystem(new TeleporterPlugin.TeleporterOwnedWarpRefChangeSystem(this.teleporterComponentType, blockStateInfoComponentType));
      chunkStoreRegistry.registerSystem(new TeleporterPlugin.TeleporterOwnedWarpRefSystem(this.teleporterComponentType, blockStateInfoComponentType));
      chunkStoreRegistry.registerSystem(new TurnOffTeleportersSystem());
      this.getChunkStoreRegistry()
         .registerSystem(
            new CreateWarpWhenTeleporterPlacedSystem(
               placedByInteractionComponentType, this.teleporterComponentType, blockStateInfoComponentType, playerRefComponentType
            )
         );
      this.usedTeleporterComponentType = entityStoreRegistry.registerComponent(UsedTeleporter.class, UsedTeleporter::new);
      entityStoreRegistry.registerSystem(
         new ClearUsedTeleporterSystem(
            this.usedTeleporterComponentType, transformComponentType, teleportRecordComponentType, teleportComponentType, pendingTeleportComponentType
         )
      );
      this.getCodecRegistry(Interaction.CODEC).register("Teleporter", TeleporterInteraction.class, TeleporterInteraction.CODEC);
      this.getCodecRegistry(OpenCustomUIInteraction.PAGE_CODEC)
         .register("Teleporter", TeleporterSettingsPageSupplier.class, TeleporterSettingsPageSupplier.CODEC);
      this.getEventRegistry().registerGlobal(RemoveWarpEvent.class, TeleporterPlugin::handleModifyWarpEvent);
      this.getEventRegistry().registerGlobal(ReplaceWarpEvent.class, TeleporterPlugin::handleModifyWarpEvent);
   }

   public ComponentType<ChunkStore, Teleporter> getTeleporterComponentType() {
      return this.teleporterComponentType;
   }

   public ComponentType<EntityStore, UsedTeleporter> getUsedTeleporterComponentType() {
      return this.usedTeleporterComponentType;
   }

   private static void handleModifyWarpEvent(ModifyWarpEvent event) {
      Warp warp = event.getWarp();
      if (warp.getCreator().equals("*Teleporter")) {
         event.cancel(MESSAGE_TELEPORTER_PROTECTION_REASON.param("name", warp.getId()));
      }
   }

   private static class TeleporterOwnedWarpRefChangeSystem extends RefChangeSystem<ChunkStore, Teleporter> {
      @Nonnull
      private final ComponentType<ChunkStore, Teleporter> teleporterComponentType;
      @Nonnull
      private final ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateComponentType;

      public TeleporterOwnedWarpRefChangeSystem(
         @Nonnull ComponentType<ChunkStore, Teleporter> teleporterComponentType,
         @Nonnull ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateComponentType
      ) {
         this.teleporterComponentType = teleporterComponentType;
         this.blockStateComponentType = blockStateComponentType;
      }

      @Nonnull
      @Override
      public ComponentType<ChunkStore, Teleporter> componentType() {
         return this.teleporterComponentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull Teleporter component, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
      }

      public void onComponentSet(
         @Nonnull Ref<ChunkStore> ref,
         @Nullable Teleporter oldComponent,
         @Nonnull Teleporter newComponent,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         if (oldComponent != null) {
            String ownedWarp = oldComponent.getOwnedWarp();
            if (ownedWarp != null && !ownedWarp.isEmpty() && !ownedWarp.equals(newComponent.getOwnedWarp())) {
               BlockModule.BlockStateInfo blockstate = commandBuffer.getComponent(ref, this.blockStateComponentType);
               if (blockstate != null && Teleporter.validateWarp(store, oldComponent, blockstate) == Teleporter.ValidationResult.VALID) {
                  TeleportPlugin.get().removeWarp(ownedWarp);
               }

               oldComponent.setOwnedWarp(null);
            }
         }
      }

      public void onComponentRemoved(
         @Nonnull Ref<ChunkStore> ref, @Nonnull Teleporter component, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         String ownedWarp = component.getOwnedWarp();
         if (ownedWarp != null && !ownedWarp.isEmpty()) {
            BlockModule.BlockStateInfo blockstate = commandBuffer.getComponent(ref, this.blockStateComponentType);
            if (blockstate != null && Teleporter.validateWarp(store, component, blockstate) == Teleporter.ValidationResult.VALID) {
               TeleportPlugin.get().removeWarp(ownedWarp);
            }

            component.setOwnedWarp(null);
         }
      }

      @Nonnull
      @Override
      public Query<ChunkStore> getQuery() {
         return Query.any();
      }
   }

   private static class TeleporterOwnedWarpRefSystem extends RefSystem<ChunkStore> {
      @Nonnull
      private final ComponentType<ChunkStore, Teleporter> teleporterComponentType;
      @Nonnull
      private final ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateComponentType;
      @Nonnull
      private final Query<ChunkStore> query;

      public TeleporterOwnedWarpRefSystem(
         @Nonnull ComponentType<ChunkStore, Teleporter> teleporterComponentType,
         @Nonnull ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateComponentType
      ) {
         this.teleporterComponentType = teleporterComponentType;
         this.blockStateComponentType = blockStateComponentType;
         this.query = Query.and(teleporterComponentType, blockStateComponentType);
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         if (reason == AddReason.LOAD) {
            Teleporter teleporter = commandBuffer.getComponent(ref, this.teleporterComponentType);
            if (teleporter != null && teleporter.getOwnedWarp() != null) {
               BlockModule.BlockStateInfo blockstate = commandBuffer.getComponent(ref, this.blockStateComponentType);
               if (blockstate != null) {
                  if (Teleporter.validateWarp(store, teleporter, blockstate) != Teleporter.ValidationResult.VALID) {
                     ((HytaleLogger.Api)TeleporterPlugin.get().getLogger().atInfo())
                        .log("Owned warp '%s' is no longer valid for teleporter", teleporter.getOwnedWarp());
                     teleporter.setOwnedWarp(null);
                  }
               }
            }
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         if (reason == RemoveReason.REMOVE) {
            Teleporter teleporter = commandBuffer.getComponent(ref, this.teleporterComponentType);
            if (teleporter != null && teleporter.getOwnedWarp() != null) {
               BlockModule.BlockStateInfo blockstate = commandBuffer.getComponent(ref, this.blockStateComponentType);
               if (blockstate != null) {
                  if (Teleporter.validateWarp(store, teleporter, blockstate) == Teleporter.ValidationResult.VALID) {
                     TeleportPlugin.get().removeWarp(teleporter.getOwnedWarp());
                  }

                  teleporter.setOwnedWarp(null);
               }
            }
         }
      }

      @Nonnull
      @Override
      public Query<ChunkStore> getQuery() {
         return this.query;
      }
   }
}
