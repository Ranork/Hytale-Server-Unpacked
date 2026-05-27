package com.hypixel.hytale.builtin.buildertools;

import com.hypixel.hytale.builtin.buildertools.commands.CopyCommand;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSession;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSessionManager;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditingMetadata;
import com.hypixel.hytale.builtin.buildertools.snapshot.EntityFreezeSnapshot;
import com.hypixel.hytale.builtin.buildertools.snapshot.EntitySettingsSnapshot;
import com.hypixel.hytale.builtin.buildertools.tooloperations.ToolOperation;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.ModelTransform;
import com.hypixel.hytale.protocol.packets.buildertools.BrushOrigin;
import com.hypixel.hytale.protocol.packets.buildertools.BrushShape;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolAction;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArgUpdate;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolEntityAction;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolExtrudeAction;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolGMaskPresetLoadResponse;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolGeneralAction;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolLineAction;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolPasteClipboard;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolRandomizeClipboard;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolResetClipboardRotation;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolRotateClipboard;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolSelectionToolAskForClipboard;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolSelectionToolReplyWithClipboard;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolSelectionTransform;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolSelectionUpdate;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolSetEntityCollision;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolSetEntityLight;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolSetEntityPickupEnabled;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolSetEntityScale;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolSetEntityTransform;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolSetNPCDebug;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolSetTransformationModeState;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolStackArea;
import com.hypixel.hytale.protocol.packets.buildertools.ClipboardEntityChange;
import com.hypixel.hytale.protocol.packets.buildertools.PrefabSetAnchor;
import com.hypixel.hytale.protocol.packets.buildertools.PrefabUnselectPrefab;
import com.hypixel.hytale.protocol.packets.interface_.BlockChange;
import com.hypixel.hytale.protocol.packets.interface_.EditorBlocksChange;
import com.hypixel.hytale.protocol.packets.interface_.FluidChange;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.protocol.packets.player.LoadHotbar;
import com.hypixel.hytale.protocol.packets.player.SaveHotbar;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BuilderTool;
import com.hypixel.hytale.server.core.command.commands.world.entity.EntityRemoveCommand;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.handlers.IPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.IWorldPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.SubPacketHandler;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.DynamicLight;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.NPCMarkerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentDynamicLight;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollision;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.prefab.selection.standard.RotateBlockMode;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3i;

public class BuilderToolsPacketHandler implements SubPacketHandler {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private static final String BUILDER_TOOL_ID_EXTRUDE = "Extrude";
   @Nonnull
   private static final String BUILDER_TOOL_ID_SELECTION = "Selection";
   @Nonnull
   private static final String BUILDER_TOOL_ID_LINE = "Line";
   @Nonnull
   private static final Message MESSAGE_BUILDER_TOOLS_USAGE_DENIED = Message.translation("server.builderTools.usageDenied");
   @Nonnull
   private final IPacketHandler packetHandler;

   public BuilderToolsPacketHandler(@Nonnull IPacketHandler packetHandler) {
      this.packetHandler = packetHandler;
   }

   private static boolean hasPermission(@Nonnull PlayerRef playerRef) {
      return hasPermission(playerRef, null);
   }

   private static boolean hasPermission(@Nonnull PlayerRef playerRef, @Nullable String additionalPermission) {
      UUID playerUuid = playerRef.getUuid();
      PermissionsModule permissionsModule = PermissionsModule.get();
      boolean hasBuilderToolsEditor = permissionsModule.hasPermission(playerUuid, HytalePermissions.BUILDER_TOOLS_EDITOR);
      boolean hasAdditionalPerm = additionalPermission != null && permissionsModule.hasPermission(playerUuid, additionalPermission);
      if (!hasBuilderToolsEditor && !hasAdditionalPerm) {
         playerRef.sendMessage(MESSAGE_BUILDER_TOOLS_USAGE_DENIED);
         return false;
      } else {
         return true;
      }
   }

   @Override
   public void registerHandlers() {
      if (BuilderToolsPlugin.get().isDisabled()) {
         this.packetHandler.registerNoOpHandlers(400, 401, 412, 409, 403, 406, 427, 428, 407, 413, 414, 417, 426);
      } else {
         IWorldPacketHandler.registerHandler(this.packetHandler, 106, this::handleLoadHotbar, BuilderToolsPacketHandler::hasPermission);
         IWorldPacketHandler.registerHandler(this.packetHandler, 107, this::handleSaveHotbar, BuilderToolsPacketHandler::hasPermission);
         IWorldPacketHandler.registerHandler(this.packetHandler, 401, this::handleBuilderToolEntityAction, BuilderToolsPacketHandler::hasPermission);
         IWorldPacketHandler.registerHandler(this.packetHandler, 402, this::handleBuilderToolSetEntityTransform, BuilderToolsPacketHandler::hasPermission);
         IWorldPacketHandler.registerHandler(this.packetHandler, 420, this::handleBuilderToolSetEntityScale, BuilderToolsPacketHandler::hasPermission);
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 408, this::handleBuilderToolSetTransformationModeState, BuilderToolsPacketHandler::hasPermission
         );
         IWorldPacketHandler.registerHandler(this.packetHandler, 417, this::handlePrefabUnselectPrefab, BuilderToolsPacketHandler::hasPermission);
         IWorldPacketHandler.registerHandler(this.packetHandler, 426, this::handlePrefabSetAnchor, BuilderToolsPacketHandler::hasPermission);
         IWorldPacketHandler.registerHandler(this.packetHandler, 421, this::handleBuilderToolSetEntityPickupEnabled, BuilderToolsPacketHandler::hasPermission);
         IWorldPacketHandler.registerHandler(this.packetHandler, 422, this::handleBuilderToolSetEntityLight, BuilderToolsPacketHandler::hasPermission);
         IWorldPacketHandler.registerHandler(this.packetHandler, 423, this::handleBuilderToolSetNPCDebug, BuilderToolsPacketHandler::hasPermission);
         IWorldPacketHandler.registerHandler(this.packetHandler, 425, this::handleBuilderToolSetEntityCollision, BuilderToolsPacketHandler::hasPermission);
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 400, this::handleBuilderToolArgUpdate, p -> hasPermission(p, HytalePermissions.EDITOR_BRUSH_CONFIG)
         );
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 409, this::handleBuilderToolSelectionUpdate, p -> hasPermission(p, HytalePermissions.EDITOR_SELECTION_USE)
         );
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 403, this::handleBuilderToolExtrudeAction, p -> hasPermission(p, HytalePermissions.EDITOR_SELECTION_MODIFY)
         );
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 406, this::handleBuilderToolRotateClipboard, p -> hasPermission(p, HytalePermissions.EDITOR_SELECTION_CLIPBOARD)
         );
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 427, this::handleBuilderToolResetClipboardRotation, p -> hasPermission(p, HytalePermissions.EDITOR_SELECTION_CLIPBOARD)
         );
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 428, this::handleBuilderToolRandomizeClipboard, p -> hasPermission(p, HytalePermissions.EDITOR_SELECTION_CLIPBOARD)
         );
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 407, this::handleBuilderToolPasteClipboard, p -> hasPermission(p, HytalePermissions.EDITOR_SELECTION_CLIPBOARD)
         );
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 413, this::handleBuilderToolOnUseInteraction, p -> hasPermission(p, HytalePermissions.EDITOR_BRUSH_USE)
         );
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 410, this::handleBuilderToolSelectionToolAskForClipboard, p -> hasPermission(p, HytalePermissions.EDITOR_SELECTION_CLIPBOARD)
         );
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 414, this::handleBuilderToolLineAction, p -> hasPermission(p, HytalePermissions.EDITOR_BRUSH_USE)
         );
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 405, this::handleBuilderToolSelectionTransform, p -> hasPermission(p, HytalePermissions.EDITOR_SELECTION_CLIPBOARD)
         );
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 404, this::handleBuilderToolStackArea, p -> hasPermission(p, HytalePermissions.EDITOR_SELECTION_CLIPBOARD)
         );
         IWorldPacketHandler.registerHandler(this.packetHandler, 412, this::handleBuilderToolGeneralAction);
         IWorldPacketHandler.registerHandler(this.packetHandler, 431, this::handleGMaskPresetLoadResponse, BuilderToolsPacketHandler::hasPermission);
      }
   }

   public void handleBuilderToolSetTransformationModeState(
      @Nonnull BuilderToolSetTransformationModeState packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
      ToolOperation.getOrCreatePrototypeSettings(playerRef.getUuid()).setInSelectionTransformationMode(packet.enabled);
   }

   public void handleBuilderToolArgUpdate(
      @Nonnull BuilderToolArgUpdate packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      BuilderToolsPlugin.get().onToolArgUpdate(ref, playerRef, packet, store);
   }

   public void handleLoadHotbar(
      @Nonnull LoadHotbar packet, @Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         playerComponent.getHotbarManager().loadHotbar(ref, packet.inventoryRow, store);
      }
   }

   public void handleSaveHotbar(
      @Nonnull SaveHotbar packet, @Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         playerComponent.getHotbarManager().saveHotbar(ref, packet.inventoryRow, store);
      }
   }

   public void handleBuilderToolEntityAction(
      @Nonnull BuilderToolEntityAction packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      int targetId = packet.entityId;
      Ref<EntityStore> targetRef = world.getEntityStore().getRefFromNetworkId(targetId);
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (targetRef == null || !targetRef.isValid()) {
         playerRef.sendMessage(Message.translation("server.general.entityNotFound").param("id", targetId));
      } else if (playerComponent != null) {
         Player targetPlayerComponent = store.getComponent(targetRef, Player.getComponentType());
         if (targetPlayerComponent != null) {
            playerRef.sendMessage(Message.translation("server.builderTools.entityTool.cannotTargetPlayer"));
         } else {
            LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
            switch (packet.action) {
               case Freeze:
                  BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, builderState, componentAccessor) -> {
                     EntityFreezeSnapshot snapshot = new EntityFreezeSnapshot(targetRef, componentAccessor);
                     if (componentAccessor.getArchetype(targetRef).contains(Frozen.getComponentType())) {
                        componentAccessor.tryRemoveComponent(targetRef, Frozen.getComponentType());
                        AnimationUtils.stopAnimation(targetRef, AnimationSlot.Movement, componentAccessor);
                     } else {
                        componentAccessor.addComponent(targetRef, Frozen.getComponentType(), Frozen.get());
                        componentAccessor.tryRemoveComponent(targetRef, DespawnComponent.getComponentType());
                        resetToIdleAnimation(targetRef, componentAccessor);
                     }

                     builderState.pushEntityFreezeHistory(snapshot);
                  });
                  break;
               case Clone:
                  BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, builderState, componentAccessor) -> {
                     Store<EntityStore> entityStore = world.getEntityStore().getStore();
                     Holder<EntityStore> copy = entityStore.copyEntity(targetRef);
                     if (copy.getComponent(UUIDComponent.getComponentType()) != null) {
                        copy.replaceComponent(UUIDComponent.getComponentType(), new UUIDComponent(UUID.randomUUID()));
                     }

                     copy.tryRemoveComponent(DespawnComponent.getComponentType());
                     Ref<EntityStore> clonedRef = entityStore.addEntity(copy, AddReason.SPAWN);
                     if (clonedRef != null) {
                        builderState.pushEntityCloneHistory(clonedRef);
                     }
                  });
                  break;
               case Remove:
                  BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, builderState, componentAccessor) -> {
                     if (targetRef.isValid()) {
                        builderState.pushEntityRemoveHistory(targetRef, componentAccessor);
                        EntityRemoveCommand.removeEntity(ref, targetRef, componentAccessor);
                     }
                  });
            }
         }
      }
   }

   private static void resetToIdleAnimation(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      ModelComponent modelComponent = componentAccessor.getComponent(ref, ModelComponent.getComponentType());
      if (modelComponent != null && modelComponent.getModel() != null) {
         if (modelComponent.getModel().getAnimationSetMap().containsKey("Idle")) {
            AnimationUtils.playAnimation(ref, AnimationSlot.Movement, "Idle", componentAccessor);
            AnimationUtils.stopAnimation(ref, AnimationSlot.Status, componentAccessor);
            AnimationUtils.stopAnimation(ref, AnimationSlot.Action, componentAccessor);
         }
      }
   }

   public void handleBuilderToolGeneralAction(
      @Nonnull BuilderToolGeneralAction packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
         switch (packet.action) {
            case HistoryUndo:
               if (!hasPermission(playerRef, HytalePermissions.EDITOR_HISTORY)) {
                  return;
               }

               BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.undo(r, 1, componentAccessor));
               break;
            case HistoryRedo:
               if (!hasPermission(playerRef, HytalePermissions.EDITOR_HISTORY)) {
                  return;
               }

               BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.redo(r, 1, componentAccessor));
               break;
            case SelectionCopy:
               if (!hasPermission(playerRef, HytalePermissions.EDITOR_SELECTION_CLIPBOARD)) {
                  return;
               }

               CopyCommand.copySelection(ref, store);
               break;
            case SelectionPosition1:
            case SelectionPosition2:
               if (!hasPermission(playerRef, HytalePermissions.EDITOR_SELECTION_USE)) {
                  return;
               }

               TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
               if (transformComponent == null) {
                  return;
               }

               BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.getState(playerComponent, playerRef);
               Vector3d position = transformComponent.getPosition();
               Vector3i intTriple = new Vector3i(MathUtil.floor(position.x()), MathUtil.floor(position.y()), MathUtil.floor(position.z()));
               BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> {
                  if (packet.action == BuilderToolAction.SelectionPosition1) {
                     builderState.pos1(intTriple, componentAccessor);
                  } else {
                     builderState.pos2(intTriple, componentAccessor);
                  }
               });
               break;
            case ActivateToolMode:
               if (!hasPermission(playerRef, HytalePermissions.BUILDER_TOOLS_EDITOR)) {
                  return;
               }

               InventoryComponent.Tool toolComponentx = store.getComponent(ref, InventoryComponent.Tool.getComponentType());
               if (toolComponentx != null) {
                  toolComponentx.setUsingToolsItem(true);
               }
               break;
            case DeactivateToolMode:
               if (!hasPermission(playerRef, HytalePermissions.BUILDER_TOOLS_EDITOR)) {
                  return;
               }

               InventoryComponent.Tool toolComponent = store.getComponent(ref, InventoryComponent.Tool.getComponentType());
               if (toolComponent != null) {
                  toolComponent.setUsingToolsItem(false);
               }

               BiConsumer<PlayerRef, Store<EntityStore>> deactivated = BuilderToolsPlugin.get().getBuilderToolModeDeactivatedCallback();
               if (deactivated != null) {
                  deactivated.accept(playerRef, store);
               }
         }
      }
   }

   public void handleBuilderToolSelectionUpdate(
      @Nonnull BuilderToolSelectionUpdate packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
         if (packet.xMin == Integer.MIN_VALUE
            && packet.xMax == Integer.MIN_VALUE
            && packet.yMin == Integer.MIN_VALUE
            && packet.yMax == Integer.MIN_VALUE
            && packet.zMin == Integer.MIN_VALUE
            && packet.zMax == Integer.MIN_VALUE) {
            BiConsumer<PlayerRef, Store<EntityStore>> clearedCb = BuilderToolsPlugin.get().getSelectionClearedCallback();
            if (clearedCb != null) {
               clearedCb.accept(playerRef, store);
            }
         } else {
            BiConsumer<PlayerRef, Store<EntityStore>> boundsCb = BuilderToolsPlugin.get().getSelectionBoundsUpdatedCallback();
            if (boundsCb != null) {
               boundsCb.accept(playerRef, store);
            }

            BuilderToolsPlugin.addToQueue(
               playerComponent, playerRef, (r, s, componentAccessor) -> s.update(packet.xMin, packet.yMin, packet.zMin, packet.xMax, packet.yMax, packet.zMax)
            );
         }
      }
   }

   public void handleBuilderToolSelectionToolAskForClipboard(
      @Nonnull BuilderToolSelectionToolAskForClipboard packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
         PrototypePlayerBuilderToolSettings prototypeSettings = ToolOperation.getOrCreatePrototypeSettings(playerRef.getUuid());
         BuilderToolsPlugin.addToQueue(
            playerComponent,
            playerRef,
            (r, s, componentAccessor) -> {
               BlockSelection selection = s.getSelection();
               if (selection != null) {
                  EditorBlocksChange editorPacket = selection.toPacket();
                  BlockChange[] blocksChange = editorPacket.blocksChange;
                  prototypeSettings.setBlockChangesForPlaySelectionToolPasteMode(blocksChange);
                  ArrayList<PrototypePlayerBuilderToolSettings.FluidChange> fluidChanges = new ArrayList<>();
                  int anchorX = selection.getAnchorX();
                  int anchorY = selection.getAnchorY();
                  int anchorZ = selection.getAnchorZ();
                  selection.forEachFluid(
                     (x, y, z, fluidId, fluidLevel) -> fluidChanges.add(
                        new PrototypePlayerBuilderToolSettings.FluidChange(x - anchorX, y - anchorY, z - anchorZ, fluidId, fluidLevel)
                     )
                  );
                  PrototypePlayerBuilderToolSettings.FluidChange[] fluidChangesArray = fluidChanges.toArray(
                     PrototypePlayerBuilderToolSettings.FluidChange[]::new
                  );
                  prototypeSettings.setFluidChangesForPlaySelectionToolPasteMode(fluidChangesArray);
                  ArrayList<PrototypePlayerBuilderToolSettings.EntityChange> entityChanges = new ArrayList<>();
                  selection.forEachEntity(holder -> {
                     TransformComponent transform = holder.getComponent(TransformComponent.getComponentType());
                     if (transform != null && transform.getPosition() != null) {
                        Vector3d pos = transform.getPosition();
                        entityChanges.add(new PrototypePlayerBuilderToolSettings.EntityChange(pos.x(), pos.y(), pos.z(), holder.clone()));
                     }
                  });
                  prototypeSettings.setEntityChangesForPlaySelectionToolPasteMode(entityChanges.toArray(PrototypePlayerBuilderToolSettings.EntityChange[]::new));
                  FluidChange[] packetFluids = new FluidChange[fluidChangesArray.length];

                  for (int i = 0; i < fluidChangesArray.length; i++) {
                     PrototypePlayerBuilderToolSettings.FluidChange fc = fluidChangesArray[i];
                     packetFluids[i] = new FluidChange(fc.x(), fc.y(), fc.z(), fc.fluidId(), fc.fluidLevel());
                  }

                  ClipboardEntityChange[] packetEntities = new ClipboardEntityChange[entityChanges.size()];

                  for (int i = 0; i < entityChanges.size(); i++) {
                     PrototypePlayerBuilderToolSettings.EntityChange ec = entityChanges.get(i);
                     packetEntities[i] = BlockSelection.toClipboardEntityChange(ec.entityHolder(), anchorX, anchorY, anchorZ);
                  }

                  if (blocksChange != null && blocksChange.length > 4000000) {
                     NotificationUtil.sendNotification(
                        playerRef.getPacketHandler(),
                        Message.translation("server.builderTools.copycut.tooLarge"),
                        Message.translation("server.builderTools.copycut.tooLarge.detail").param("overCount", blocksChange.length - 4000000),
                        NotificationStyle.Warning
                     );
                     return;
                  }

                  playerRef.getPacketHandler().write(new BuilderToolSelectionToolReplyWithClipboard(blocksChange, packetFluids, packetEntities));
               }
            }
         );
      }
   }

   private void handleBuilderToolSelectionTransform(
      @Nonnull BuilderToolSelectionTransform packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
         boolean keepEmptyBlocks = true;
         BuilderTool builderTool = BuilderTool.getActiveBuilderTool(ref, store);
         if (builderTool != null && builderTool.getId().equals("Selection")) {
            ItemStack itemInHand = InventoryComponent.getItemInHand(store, ref);
            BuilderTool.ArgData args = builderTool.getItemArgData(itemInHand);
            if (args != null && args.tool() != null) {
               keepEmptyBlocks = (Boolean)args.tool().getOrDefault("KeepEmptyBlocks", true);
            }
         }

         boolean finalKeepEmptyBlocks = keepEmptyBlocks;
         Quaterniond rotation = new Quaterniond(packet.rotation);
         Vector3i translationOffset = new Vector3i(packet.translationOffset.x, packet.translationOffset.y, packet.translationOffset.z);
         Vector3i initialSelectionMin = new Vector3i(packet.initialSelectionMin.x, packet.initialSelectionMin.y, packet.initialSelectionMin.z);
         Vector3i initialSelectionMax = new Vector3i(packet.initialSelectionMax.x, packet.initialSelectionMax.y, packet.initialSelectionMax.z);
         Rotation3f rotationOrigin = new Rotation3f(packet.initialRotationOrigin.x(), packet.initialRotationOrigin.y(), packet.initialRotationOrigin.z());
         PrototypePlayerBuilderToolSettings prototypeSettings = ToolOperation.getOrCreatePrototypeSettings(playerRef.getUuid());
         BuilderToolsPlugin.addToQueue(
            playerComponent,
            playerRef,
            (r, s, componentAccessor) -> {
               if (s.getSelection() != null) {
                  int blockCount = s.getSelection().getSelectionVolume();
                  boolean large = blockCount > 20000;
                  if (large) {
                     playerRef.sendMessage(Message.translation("server.builderTools.selection.large.warning"));
                  }

                  try {
                     if (prototypeSettings.getBlockChangesForPlaySelectionToolPasteMode() == null) {
                        s.select(initialSelectionMin, initialSelectionMax, "server.builderTools.selectReasons.selectionTranslatePacket", componentAccessor);
                        List<Ref<EntityStore>> lastTransformRefs = prototypeSettings.getLastTransformEntityRefs();
                        HashSet<Ref<EntityStore>> skipSet = lastTransformRefs != null ? new HashSet<>(lastTransformRefs) : null;
                        if (packet.cutOriginal) {
                           s.copyOrCut(
                              r,
                              initialSelectionMin.x,
                              initialSelectionMin.y,
                              initialSelectionMin.z,
                              initialSelectionMax.x,
                              initialSelectionMax.y,
                              initialSelectionMax.z,
                              154,
                              null,
                              skipSet,
                              store
                           );
                        } else {
                           s.copyOrCut(
                              r,
                              initialSelectionMin.x,
                              initialSelectionMin.y,
                              initialSelectionMin.z,
                              initialSelectionMax.x,
                              initialSelectionMax.y,
                              initialSelectionMax.z,
                              152,
                              store
                           );
                        }

                        BlockSelection selection = s.getSelection();
                        int anchorX = selection.getAnchorX();
                        int anchorY = selection.getAnchorY();
                        int anchorZ = selection.getAnchorZ();
                        ObjectArrayList<BlockChange> blockChangeList = new ObjectArrayList();
                        ObjectArrayList<Holder<ChunkStore>> blockHolderList = new ObjectArrayList();
                        selection.forEachBlock((x, y, z, block) -> {
                           if (block.filler() == 0) {
                              blockChangeList.add(new BlockChange(x - anchorX, y - anchorY, z - anchorZ, block.blockId(), (byte)block.rotation()));
                              blockHolderList.add(block.holder() != null ? block.holder().clone() : null);
                           }
                        });
                        prototypeSettings.setBlockChangesForPlaySelectionToolPasteMode((BlockChange[])blockChangeList.toArray(BlockChange[]::new));
                        prototypeSettings.setBlockHoldersForPasteMode((Holder<ChunkStore>[])blockHolderList.toArray(new Holder[0]));
                        ArrayList<PrototypePlayerBuilderToolSettings.FluidChange> fluidChanges = new ArrayList<>();
                        selection.forEachFluid(
                           (x, y, z, fluidId, fluidLevel) -> fluidChanges.add(
                              new PrototypePlayerBuilderToolSettings.FluidChange(x - anchorX, y - anchorY, z - anchorZ, fluidId, fluidLevel)
                           )
                        );
                        prototypeSettings.setFluidChangesForPlaySelectionToolPasteMode(
                           fluidChanges.toArray(PrototypePlayerBuilderToolSettings.FluidChange[]::new)
                        );
                        ArrayList<PrototypePlayerBuilderToolSettings.EntityChange> entityChanges = new ArrayList<>();
                        selection.forEachEntity(holder -> {
                           TransformComponent transform = holder.getComponent(TransformComponent.getComponentType());
                           if (transform != null && transform.getPosition() != null) {
                              Vector3d pos = transform.getPosition();
                              entityChanges.add(new PrototypePlayerBuilderToolSettings.EntityChange(pos.x(), pos.y(), pos.z(), holder.clone()));
                           }
                        });
                        prototypeSettings.setEntityChangesForPlaySelectionToolPasteMode(
                           entityChanges.toArray(PrototypePlayerBuilderToolSettings.EntityChange[]::new)
                        );
                        prototypeSettings.setBlockChangeOffsetOrigin(new Vector3i(selection.getX(), selection.getY(), selection.getZ()));
                     }

                     BlockChange[] localBlockChanges = prototypeSettings.getBlockChangesForPlaySelectionToolPasteMode();
                     PrototypePlayerBuilderToolSettings.FluidChange[] localFluidChanges = prototypeSettings.getFluidChangesForPlaySelectionToolPasteMode();
                     PrototypePlayerBuilderToolSettings.EntityChange[] localEntityChanges = prototypeSettings.getEntityChangesForPlaySelectionToolPasteMode();
                     Holder<ChunkStore>[] localBlockHolders = prototypeSettings.getBlockHoldersForPasteMode();
                     Vector3i blockChangeOffsetOrigin = prototypeSettings.getBlockChangeOffsetOrigin();
                     if (packet.initialPastePointForClipboardPaste != null) {
                        blockChangeOffsetOrigin = new Vector3i(
                           packet.initialPastePointForClipboardPaste.x,
                           packet.initialPastePointForClipboardPaste.y,
                           packet.initialPastePointForClipboardPaste.z
                        );
                     }

                     if (blockChangeOffsetOrigin != null) {
                        prototypeSettings.setLastTransformEntityRefs(null);
                        s.transformThenPasteClipboard(
                           localBlockChanges,
                           localFluidChanges,
                           localEntityChanges,
                           localBlockHolders,
                           rotation,
                           translationOffset,
                           rotationOrigin,
                           blockChangeOffsetOrigin,
                           finalKeepEmptyBlocks,
                           prototypeSettings,
                           componentAccessor
                        );
                        s.select(initialSelectionMin, initialSelectionMax, "server.builderTools.selectReasons.selectionTranslatePacket", componentAccessor);
                        s.transformSelectionPoints(rotation, translationOffset, rotationOrigin);
                        if (!packet.isExitingTransformMode) {
                           prototypeSettings.setBlockChangeOffsetOrigin(
                              new Vector3i(
                                 blockChangeOffsetOrigin.x + translationOffset.x,
                                 blockChangeOffsetOrigin.y + translationOffset.y,
                                 blockChangeOffsetOrigin.z + translationOffset.z
                              )
                           );
                        }

                        if (large) {
                           playerRef.sendMessage(Message.translation("server.builderTools.selection.large.complete"));
                        }

                        return;
                     }

                     playerRef.sendMessage(Message.translation("server.builderTools.selection.noBlockChangeOffsetOrigin"));
                  } catch (Exception var28) {
                     LOGGER.at(Level.WARNING).log("Error during selection transform", var28);
                     return;
                  } finally {
                     if (packet.isExitingTransformMode) {
                        prototypeSettings.setInSelectionTransformationMode(false);
                        prototypeSettings.setLastTransformEntityRefs(null);
                     }
                  }
               }
            }
         );
      }
   }

   public void handleBuilderToolExtrudeAction(
      @Nonnull BuilderToolExtrudeAction packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         BuilderTool builderTool = BuilderTool.getActiveBuilderTool(ref, store);
         if (builderTool != null && builderTool.getId().equals("Extrude")) {
            ItemStack itemInHand = InventoryComponent.getItemInHand(store, ref);
            BuilderTool.ArgData args = builderTool.getItemArgData(itemInHand);
            if (args != null) {
               Map<String, Object> toolArgs = args.tool();
               if (toolArgs != null) {
                  int extrudeDepth = (Integer)args.tool().get("ExtrudeDepth");
                  int extrudeWidth = (Integer)args.tool().get("ExtrudeWidth");
                  int extrudeLength = (Integer)args.tool().get("ExtrudeLength");
                  String extrudeFilter = (String)args.tool().get("ExtrudeFilter");
                  String extrudeStrategy = (String)args.tool().get("ExtrudeStrategy");
                  long chunkIndex = ChunkUtil.indexChunkFromBlock(packet.x, packet.z);
                  WorldChunk chunk = world.getChunk(chunkIndex);
                  int fillerX = 0;
                  int fillerY = 0;
                  int fillerZ = 0;
                  if (chunk != null) {
                     int filler = chunk.getFiller(packet.x, packet.y, packet.z);
                     if (filler != 0) {
                        fillerX = FillerBlockUtil.unpackX(filler);
                        fillerY = FillerBlockUtil.unpackY(filler);
                        fillerZ = FillerBlockUtil.unpackZ(filler);
                     }
                  }

                  int targetX = fillerX != 0 ? (packet.x -= fillerX) : packet.x;
                  int targetY = fillerY != 0 ? (packet.y -= fillerY) : packet.y;
                  int targetZ = fillerZ != 0 ? (packet.z -= fillerZ) : packet.z;
                  LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
                  switch (packet.mode) {
                     case Extrude:
                        BlockPattern extrudePattern = (BlockPattern)args.tool().get("ExtrudeMaterial");
                        BuilderToolsPlugin.addToQueue(
                           playerComponent,
                           playerRef,
                           (r, s, componentAccessor) -> s.extendOrShrinkFace(
                              targetX,
                              targetY,
                              targetZ,
                              packet.xNormal,
                              packet.yNormal,
                              packet.zNormal,
                              extrudeDepth,
                              extrudeWidth,
                              extrudeLength,
                              false,
                              extrudePattern,
                              extrudeFilter,
                              extrudeStrategy,
                              packet.undoGroupSize,
                              packet.isHoldDownInteraction,
                              componentAccessor
                           )
                        );
                        break;
                     case Shrink:
                        BuilderToolsPlugin.addToQueue(
                           playerComponent,
                           playerRef,
                           (r, s, componentAccessor) -> s.extendOrShrinkFace(
                              targetX,
                              targetY,
                              targetZ,
                              packet.xNormal,
                              packet.yNormal,
                              packet.zNormal,
                              extrudeDepth,
                              extrudeWidth,
                              extrudeLength,
                              true,
                              BlockPattern.EMPTY,
                              extrudeFilter,
                              extrudeStrategy,
                              packet.undoGroupSize,
                              packet.isHoldDownInteraction,
                              componentAccessor
                           )
                        );
                        break;
                     case Fill:
                        BlockPattern fillPattern = (BlockPattern)args.tool().get("FillMaterial");
                        BuilderToolsPlugin.addToQueue(
                           playerComponent,
                           playerRef,
                           (r, s, componentAccessor) -> s.fillVolume(
                              targetX,
                              targetY,
                              targetZ,
                              packet.xNormal,
                              packet.yNormal,
                              packet.zNormal,
                              extrudeDepth,
                              extrudeWidth,
                              extrudeLength,
                              fillPattern,
                              packet.undoGroupSize,
                              packet.isHoldDownInteraction,
                              componentAccessor
                           )
                        );
                  }
               }
            }
         }
      }
   }

   public void handleBuilderToolStackArea(
      @Nonnull BuilderToolStackArea packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
         BuilderToolsPlugin.addToQueue(
            playerComponent,
            playerRef,
            (r, s, componentAccessor) -> {
               s.select(
                  this.fromBlockPosition(packet.selectionMin),
                  this.fromBlockPosition(packet.selectionMax),
                  "server.builderTools.selectReasons.extrude",
                  componentAccessor
               );
               s.stack(r, new Vector3i(packet.xNormal, packet.yNormal, packet.zNormal), packet.numStacks, true, 0, componentAccessor);
            }
         );
      }
   }

   @Nonnull
   public Vector3i fromBlockPosition(@Nonnull BlockPosition position) {
      return new Vector3i(position.x, position.y, position.z);
   }

   public void handleBuilderToolRotateClipboard(
      @Nonnull BuilderToolRotateClipboard packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         Axis axis = packet.axis == com.hypixel.hytale.protocol.packets.buildertools.Axis.X
            ? Axis.X
            : (packet.axis == com.hypixel.hytale.protocol.packets.buildertools.Axis.Y ? Axis.Y : Axis.Z);
         RotateBlockMode rotateBlockMode = RotateBlockMode.ALL;
         BuilderTool builderTool = BuilderTool.getActiveBuilderTool(ref, store);
         if (builderTool != null && builderTool.getId().equals("Paste")) {
            ItemStack itemInHand = InventoryComponent.getItemInHand(store, ref);
            BuilderTool.ArgData args = builderTool.getItemArgData(itemInHand);
            if (args != null && args.tool() != null) {
               rotateBlockMode = RotateBlockMode.fromString((String)args.tool().getOrDefault("RotateBlock", "All"));
            }
         }

         RotateBlockMode finalRotateBlockMode = rotateBlockMode;
         LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
         BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> {
            s.setSkipNextPreviewRebuild(finalRotateBlockMode == RotateBlockMode.ALL);
            s.rotate(r, axis, packet.angle, finalRotateBlockMode, componentAccessor);
         });
      }
   }

   public void handleBuilderToolRandomizeClipboard(
      @Nonnull BuilderToolRandomizeClipboard packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         RotateBlockMode rotateBlockMode = RotateBlockMode.ALL;
         BuilderTool builderTool = BuilderTool.getActiveBuilderTool(ref, store);
         if (builderTool != null && builderTool.getId().equals("Paste")) {
            ItemStack itemInHand = InventoryComponent.getItemInHand(store, ref);
            BuilderTool.ArgData args = builderTool.getItemArgData(itemInHand);
            if (args != null && args.tool() != null) {
               rotateBlockMode = RotateBlockMode.fromString((String)args.tool().getOrDefault("RotateBlock", "All"));
            }
         }

         RotateBlockMode finalRotateBlockMode = rotateBlockMode;
         LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
         BuilderToolsPlugin.addToQueue(
            playerComponent,
            playerRef,
            (r, s, componentAccessor) -> s.applyRandomizeTransforms(
               r, packet.deltaX, packet.deltaY, packet.deltaZ, packet.flipX, packet.flipY, packet.flipZ, finalRotateBlockMode, componentAccessor
            )
         );
      }
   }

   public void handleBuilderToolResetClipboardRotation(
      @Nonnull BuilderToolResetClipboardRotation packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
         BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.resetClipboardRotation(r, componentAccessor));
      }
   }

   public void handleBuilderToolPasteClipboard(
      @Nonnull BuilderToolPasteClipboard packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         boolean pasteAir = true;
         BuilderTool builderTool = BuilderTool.getActiveBuilderTool(ref, store);
         if (builderTool != null && builderTool.getId().equals("Paste")) {
            ItemStack itemInHand = InventoryComponent.getItemInHand(store, ref);
            BuilderTool.ArgData args = builderTool.getItemArgData(itemInHand);
            if (args != null && args.tool() != null) {
               pasteAir = (Boolean)args.tool().getOrDefault("PasteAir", true);
            }
         }

         boolean finalPasteAir = pasteAir;
         LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
         BuilderToolsPlugin.addToQueue(
            playerComponent, playerRef, (r, s, componentAccessor) -> s.paste(r, packet.x, packet.y, packet.z, false, !finalPasteAir, componentAccessor)
         );
      }
   }

   public void handleBuilderToolLineAction(
      @Nonnull BuilderToolLineAction packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         BuilderTool builderTool = BuilderTool.getActiveBuilderTool(ref, store);
         if (builderTool != null && builderTool.getId().equals("Line")) {
            ItemStack itemInHand = InventoryComponent.getItemInHand(store, ref);
            BuilderTool.ArgData args = builderTool.getItemArgData(itemInHand);
            if (args != null) {
               Map<String, Object> toolArgs = args.tool();
               if (toolArgs != null) {
                  int lineWidth = (Integer)toolArgs.get("bLineWidth");
                  int lineHeight = (Integer)toolArgs.get("cLineHeight");
                  BrushShape lineShape = BrushShape.valueOf((String)toolArgs.get("dLineShape"));
                  BrushOrigin lineOrigin = BrushOrigin.valueOf((String)toolArgs.get("eLineOrigin"));
                  int lineWallThickness = (Integer)toolArgs.get("fLineWallThickness");
                  int lineSpacing = (Integer)toolArgs.get("gLineSpacing");
                  int lineDensity = (Integer)toolArgs.get("hLineDensity");
                  BlockPattern lineMaterial = (BlockPattern)toolArgs.get("aLineMaterial");
                  LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
                  BuilderToolsPlugin.addToQueue(
                     playerComponent,
                     playerRef,
                     (r, s, componentAccessor) -> s.editLine(
                        packet.xStart,
                        packet.yStart,
                        packet.zStart,
                        packet.xEnd,
                        packet.yEnd,
                        packet.zEnd,
                        lineMaterial,
                        lineWidth,
                        lineHeight,
                        lineWallThickness,
                        lineShape,
                        lineOrigin,
                        lineSpacing,
                        lineDensity,
                        ToolOperation.combineMasks(args, s.getGlobalMask()),
                        componentAccessor
                     )
                  );
               }
            }
         }
      }
   }

   public void handleBuilderToolOnUseInteraction(
      @Nonnull BuilderToolOnUseInteraction packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
         BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.edit(ref, packet, componentAccessor));
      }
   }

   public void handleBuilderToolSetEntityTransform(
      @Nonnull BuilderToolSetEntityTransform packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         Ref<EntityStore> targetRef = world.getEntityStore().getRefFromNetworkId(packet.entityId);
         if (targetRef != null && targetRef.isValid()) {
            if (store.getComponent(targetRef, Player.getComponentType()) == null) {
               BuilderToolsPlugin.addToQueue(
                  playerComponent,
                  playerRef,
                  (r, builderState, componentAccessor) -> {
                     if (targetRef.isValid()) {
                        TransformComponent transformComponent = componentAccessor.getComponent(targetRef, TransformComponent.getComponentType());
                        if (transformComponent != null) {
                           ModelTransform modelTransform = packet.modelTransform;
                           builderState.handleEntityTransform(targetRef, modelTransform != null, packet.isSessionEnd, componentAccessor);
                           if (modelTransform != null) {
                              componentAccessor.tryRemoveComponent(targetRef, DespawnComponent.getComponentType());
                           }

                           HeadRotation headRotation = componentAccessor.getComponent(targetRef, HeadRotation.getComponentType());
                           if (modelTransform != null) {
                              boolean hasPosition = modelTransform.position != null;
                              boolean hasLookOrientation = modelTransform.lookOrientation != null;
                              boolean hasBodyOrientation = modelTransform.bodyOrientation != null;
                              if (hasPosition) {
                                 transformComponent.getPosition().set(modelTransform.position.x, modelTransform.position.y, modelTransform.position.z);
                              }

                              if (hasLookOrientation && headRotation != null) {
                                 headRotation.getRotation()
                                    .set(modelTransform.lookOrientation.pitch, modelTransform.lookOrientation.yaw, modelTransform.lookOrientation.roll);
                              }

                              if (hasBodyOrientation) {
                                 transformComponent.getRotation()
                                    .set(modelTransform.bodyOrientation.pitch, modelTransform.bodyOrientation.yaw, modelTransform.bodyOrientation.roll);
                              }

                              if (hasPosition || hasLookOrientation || hasBodyOrientation) {
                                 transformComponent.markChunkDirty(componentAccessor);
                              }
                           }

                           if (packet.isSessionEnd) {
                              boolean isItemOrBlock = componentAccessor.getArchetype(targetRef).contains(ItemComponent.getComponentType())
                                 || componentAccessor.getArchetype(targetRef).contains(BlockEntity.getComponentType());
                              if (isItemOrBlock) {
                                 BoundingBox boundingBox = componentAccessor.getComponent(targetRef, BoundingBox.getComponentType());
                                 if (boundingBox != null) {
                                    boolean isBlock = componentAccessor.getArchetype(targetRef).contains(BlockEntity.getComponentType());
                                    Rotation3f rotation;
                                    if (isBlock && headRotation != null) {
                                       rotation = headRotation.getRotation();
                                    } else {
                                       rotation = transformComponent.getRotation();
                                    }

                                    boundingBox.applyRotation(rotation.pitch(), rotation.yaw(), rotation.roll());
                                 }
                              }
                           }
                        }
                     }
                  }
               );
            }
         }
      }
   }

   public void handlePrefabUnselectPrefab(
      @Nonnull PrefabUnselectPrefab packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
      PrefabEditSessionManager prefabEditSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();
      PrefabEditSession prefabEditSession = prefabEditSessionManager.getPrefabEditSession(playerRef.getUuid());
      if (prefabEditSession == null) {
         playerRef.sendMessage(Message.translation("server.commands.editprefab.notInEditSession"));
      } else {
         if (prefabEditSession.clearSelectedPrefab(ref, store)) {
            playerRef.sendMessage(Message.translation("server.commands.editprefab.unselected"));
         } else {
            playerRef.sendMessage(Message.translation("server.commands.editprefab.noPrefabSelected"));
         }
      }
   }

   public void handlePrefabSetAnchor(
      @Nonnull PrefabSetAnchor packet, @Nonnull PlayerRef playerRef, @Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull Store<EntityStore> store
   ) {
      LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
      PrefabEditSessionManager prefabEditSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();
      PrefabEditSession prefabEditSession = prefabEditSessionManager.getPrefabEditSession(playerRef.getUuid());
      if (prefabEditSession == null) {
         playerRef.sendMessage(Message.translation("server.commands.editprefab.notInEditSession"));
      } else {
         PrefabEditingMetadata prefabEditingMetadata = null;
         Vector3i targetBlockPos = new Vector3i(packet.x, packet.y, packet.z);

         for (PrefabEditingMetadata value : prefabEditSession.getLoadedPrefabMetadata().values()) {
            boolean isWithinPrefab = value.isLocationWithinPrefabBoundingBox(new Vector3i(packet.x, packet.y, packet.z));
            if (isWithinPrefab) {
               prefabEditingMetadata = value;
               break;
            }
         }

         if (prefabEditingMetadata == null) {
            playerRef.sendMessage(Message.translation("server.commands.editprefab.select.error.noPrefabFound"));
         } else {
            prefabEditingMetadata.setAnchorPoint(targetBlockPos, world);
            prefabEditingMetadata.sendAnchorHighlightingPacket(playerRef.getPacketHandler());
         }
      }
   }

   public void handleBuilderToolSetEntityScale(
      @Nonnull BuilderToolSetEntityScale packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         Ref<EntityStore> targetRef = world.getEntityStore().getRefFromNetworkId(packet.entityId);
         if (targetRef != null && targetRef.isValid()) {
            if (store.getComponent(targetRef, Player.getComponentType()) == null) {
               BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, builderState, componentAccessor) -> {
                  if (targetRef.isValid()) {
                     builderState.handleEntityScale(targetRef, componentAccessor);
                     EntityScaleComponent scaleComponent = componentAccessor.getComponent(targetRef, EntityScaleComponent.getComponentType());
                     if (scaleComponent == null) {
                        scaleComponent = new EntityScaleComponent(packet.scale);
                        componentAccessor.addComponent(targetRef, EntityScaleComponent.getComponentType(), scaleComponent);
                     } else {
                        scaleComponent.setScale(packet.scale);
                     }
                  }
               });
            }
         }
      }
   }

   public void handleBuilderToolSetEntityPickupEnabled(
      @Nonnull BuilderToolSetEntityPickupEnabled packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         Ref<EntityStore> targetRef = world.getEntityStore().getRefFromNetworkId(packet.entityId);
         if (targetRef != null && targetRef.isValid()) {
            PropComponent propComponent = store.getComponent(targetRef, PropComponent.getComponentType());
            if (propComponent != null) {
               BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, builderState, componentAccessor) -> {
                  if (targetRef.isValid()) {
                     EntitySettingsSnapshot snapshot = new EntitySettingsSnapshot(targetRef, componentAccessor);
                     EntitySettingsSnapshot.applyPickupState(targetRef, packet.enabled, componentAccessor);
                     builderState.pushEntitySettingsHistory(snapshot);
                  }
               });
            }
         }
      }
   }

   public void handleBuilderToolSetEntityLight(
      @Nonnull BuilderToolSetEntityLight packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         Ref<EntityStore> targetRef = world.getEntityStore().getRefFromNetworkId(packet.entityId);
         if (targetRef != null && targetRef.isValid()) {
            BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, builderState, componentAccessor) -> {
               if (targetRef.isValid()) {
                  EntitySettingsSnapshot snapshot = new EntitySettingsSnapshot(targetRef, componentAccessor);
                  if (packet.light == null) {
                     componentAccessor.tryRemoveComponent(targetRef, DynamicLight.getComponentType());
                     componentAccessor.tryRemoveComponent(targetRef, PersistentDynamicLight.getComponentType());
                  } else {
                     ColorLight colorLight = new ColorLight(packet.light.radius, packet.light.red, packet.light.green, packet.light.blue);
                     DynamicLight existingDynamic = componentAccessor.getComponent(targetRef, DynamicLight.getComponentType());
                     if (existingDynamic != null) {
                        existingDynamic.setColorLight(colorLight);
                     } else {
                        componentAccessor.addComponent(targetRef, DynamicLight.getComponentType(), new DynamicLight(colorLight));
                     }

                     PersistentDynamicLight existingPersistent = componentAccessor.getComponent(targetRef, PersistentDynamicLight.getComponentType());
                     if (existingPersistent != null) {
                        existingPersistent.setColorLight(colorLight);
                     } else {
                        componentAccessor.addComponent(targetRef, PersistentDynamicLight.getComponentType(), new PersistentDynamicLight(colorLight));
                     }
                  }

                  builderState.pushEntitySettingsHistory(snapshot);
               }
            });
         }
      }
   }

   public void handleBuilderToolSetNPCDebug(
      @Nonnull BuilderToolSetNPCDebug packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Ref<EntityStore> targetRef = world.getEntityStore().getRefFromNetworkId(packet.entityId);
      if (targetRef != null && targetRef.isValid()) {
         NPCMarkerComponent npcMarkerComponent = store.getComponent(targetRef, NPCMarkerComponent.getComponentType());
         if (npcMarkerComponent != null) {
            UUIDComponent uuidComponent = store.getComponent(targetRef, UUIDComponent.getComponentType());
            if (uuidComponent != null) {
               UUID uuid = uuidComponent.getUuid();
               String command = packet.enabled ? "npc debug set display --entity " + uuid : "npc debug clear --entity " + uuid;
               CommandManager.get().handleCommand(playerRef, command);
            }
         }
      }
   }

   public void handleBuilderToolSetEntityCollision(
      @Nonnull BuilderToolSetEntityCollision packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         Ref<EntityStore> targetRef = world.getEntityStore().getRefFromNetworkId(packet.entityId);
         if (targetRef != null && targetRef.isValid()) {
            PropComponent propComponent = store.getComponent(targetRef, PropComponent.getComponentType());
            NPCMarkerComponent npcMarkerComponent = store.getComponent(targetRef, NPCMarkerComponent.getComponentType());
            if (propComponent != null || npcMarkerComponent != null) {
               BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, builderState, componentAccessor) -> {
                  if (targetRef.isValid()) {
                     EntitySettingsSnapshot snapshot = new EntitySettingsSnapshot(targetRef, componentAccessor);
                     if (packet.collisionType != null && !packet.collisionType.isEmpty()) {
                        HitboxCollisionConfig hitboxCollisionConfig = HitboxCollisionConfig.getAssetMap().getAsset(packet.collisionType);
                        if (hitboxCollisionConfig != null) {
                           HitboxCollision existing = componentAccessor.getComponent(targetRef, HitboxCollision.getComponentType());
                           if (existing != null) {
                              existing.setHitboxCollisionConfigIndex(HitboxCollisionConfig.getAssetMap().getIndexOrDefault(packet.collisionType, -1));
                           } else {
                              componentAccessor.addComponent(targetRef, HitboxCollision.getComponentType(), new HitboxCollision(hitboxCollisionConfig));
                           }
                        }
                     } else {
                        componentAccessor.tryRemoveComponent(targetRef, HitboxCollision.getComponentType());
                     }

                     builderState.pushEntitySettingsHistory(snapshot);
                  }
               });
            }
         }
      }
   }

   public void handleGMaskPresetLoadResponse(
      @Nonnull BuilderToolGMaskPresetLoadResponse packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> {
            if (packet.maskData != null && !packet.maskData.isEmpty()) {
               try {
                  BlockMask mask = BlockMask.parse(packet.maskData);
                  s.setGlobalMask(mask, componentAccessor);
               } catch (Exception var6x) {
                  playerRef.sendMessage(Message.translation("server.builderTools.globalmask.load.failed").param("reason", var6x.getMessage()));
               }
            } else {
               playerRef.sendMessage(Message.translation("server.builderTools.globalmask.load.empty"));
            }
         });
      }
   }
}
