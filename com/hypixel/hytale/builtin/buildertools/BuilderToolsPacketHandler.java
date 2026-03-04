package com.hypixel.hytale.builtin.buildertools;

import com.hypixel.hytale.builtin.buildertools.commands.CopyCommand;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSession;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSessionManager;
import com.hypixel.hytale.builtin.buildertools.tooloperations.ToolOperation;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.ModelTransform;
import com.hypixel.hytale.protocol.packets.buildertools.BrushOrigin;
import com.hypixel.hytale.protocol.packets.buildertools.BrushShape;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolAction;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArgUpdate;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolEntityAction;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolExtrudeAction;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolGeneralAction;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolLineAction;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolPasteClipboard;
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
import com.hypixel.hytale.protocol.packets.buildertools.PrefabUnselectPrefab;
import com.hypixel.hytale.protocol.packets.interface_.BlockChange;
import com.hypixel.hytale.protocol.packets.interface_.EditorBlocksChange;
import com.hypixel.hytale.protocol.packets.interface_.FluidChange;
import com.hypixel.hytale.protocol.packets.player.LoadHotbar;
import com.hypixel.hytale.protocol.packets.player.SaveHotbar;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BrushData;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BuilderTool;
import com.hypixel.hytale.server.core.command.commands.world.entity.EntityCloneCommand;
import com.hypixel.hytale.server.core.command.commands.world.entity.EntityRemoveCommand;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.handlers.IPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.IWorldPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.SubPacketHandler;
import com.hypixel.hytale.server.core.modules.entity.component.DynamicLight;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.NPCMarkerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentDynamicLight;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollision;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.modules.entity.item.PreventPickup;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Quaterniond;

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
      boolean hasBuilderToolsEditor = permissionsModule.hasPermission(playerUuid, "hytale.editor.builderTools");
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
         this.packetHandler.registerNoOpHandlers(400, 401, 412, 409, 403, 406, 407, 413, 414, 417);
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
         IWorldPacketHandler.registerHandler(this.packetHandler, 421, this::handleBuilderToolSetEntityPickupEnabled, BuilderToolsPacketHandler::hasPermission);
         IWorldPacketHandler.registerHandler(this.packetHandler, 422, this::handleBuilderToolSetEntityLight, BuilderToolsPacketHandler::hasPermission);
         IWorldPacketHandler.registerHandler(this.packetHandler, 423, this::handleBuilderToolSetNPCDebug, BuilderToolsPacketHandler::hasPermission);
         IWorldPacketHandler.registerHandler(this.packetHandler, 425, this::handleBuilderToolSetEntityCollision, BuilderToolsPacketHandler::hasPermission);
         IWorldPacketHandler.registerHandler(this.packetHandler, 400, this::handleBuilderToolArgUpdate, p -> hasPermission(p, "hytale.editor.brush.config"));
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 409, this::handleBuilderToolSelectionUpdate, p -> hasPermission(p, "hytale.editor.selection.use")
         );
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 403, this::handleBuilderToolExtrudeAction, p -> hasPermission(p, "hytale.editor.selection.modify")
         );
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 406, this::handleBuilderToolRotateClipboard, p -> hasPermission(p, "hytale.editor.selection.clipboard")
         );
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 407, this::handleBuilderToolPasteClipboard, p -> hasPermission(p, "hytale.editor.selection.clipboard")
         );
         IWorldPacketHandler.registerHandler(this.packetHandler, 413, this::handleBuilderToolOnUseInteraction, p -> hasPermission(p, "hytale.editor.brush.use"));
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 410, this::handleBuilderToolSelectionToolAskForClipboard, p -> hasPermission(p, "hytale.editor.selection.clipboard")
         );
         IWorldPacketHandler.registerHandler(this.packetHandler, 414, this::handleBuilderToolLineAction, p -> hasPermission(p, "hytale.editor.brush.use"));
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 405, this::handleBuilderToolSelectionTransform, p -> hasPermission(p, "hytale.editor.selection.clipboard")
         );
         IWorldPacketHandler.registerHandler(
            this.packetHandler, 404, this::handleBuilderToolStackArea, p -> hasPermission(p, "hytale.editor.selection.clipboard")
         );
         IWorldPacketHandler.registerHandler(this.packetHandler, 412, this::handleBuilderToolGeneralAction);
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
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         BuilderToolsPlugin.get().onToolArgUpdate(playerRef, playerComponent, packet);
      }
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
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         int targetId = packet.entityId;
         Ref<EntityStore> targetRef = world.getEntityStore().getRefFromNetworkId(targetId);
         if (targetRef != null && targetRef.isValid()) {
            Player targetPlayerComponent = store.getComponent(targetRef, Player.getComponentType());
            if (targetPlayerComponent != null) {
               playerComponent.sendMessage(Message.translation("server.builderTools.entityTool.cannotTargetPlayer"));
            } else {
               LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
               switch (packet.action) {
                  case Freeze:
                     UUIDComponent uuidComponent = store.getComponent(targetRef, UUIDComponent.getComponentType());
                     if (uuidComponent != null) {
                        CommandManager.get().handleCommand(playerComponent, "npc freeze --toggle --entity " + uuidComponent.getUuid());
                     }
                     break;
                  case Clone:
                     world.execute(() -> EntityCloneCommand.cloneEntity(playerComponent, targetRef, store));
                     break;
                  case Remove:
                     world.execute(() -> EntityRemoveCommand.removeEntity(ref, targetRef, store));
               }
            }
         } else {
            playerComponent.sendMessage(Message.translation("server.general.entityNotFound").param("id", targetId));
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
               if (!hasPermission(playerRef, "hytale.editor.history")) {
                  return;
               }

               BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.undo(r, 1, componentAccessor));
               break;
            case HistoryRedo:
               if (!hasPermission(playerRef, "hytale.editor.history")) {
                  return;
               }

               BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.redo(r, 1, componentAccessor));
               break;
            case SelectionCopy:
               if (!hasPermission(playerRef, "hytale.editor.selection.clipboard")) {
                  return;
               }

               CopyCommand.copySelection(ref, store);
               break;
            case SelectionPosition1:
            case SelectionPosition2:
               if (!hasPermission(playerRef, "hytale.editor.selection.use")) {
                  return;
               }

               TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
               if (transformComponent == null) {
                  return;
               }

               BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.getState(playerComponent, playerRef);
               Vector3d position = transformComponent.getPosition();
               Vector3i intTriple = new Vector3i(MathUtil.floor(position.getX()), MathUtil.floor(position.getY()), MathUtil.floor(position.getZ()));
               BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> {
                  if (packet.action == BuilderToolAction.SelectionPosition1) {
                     builderState.pos1(intTriple, componentAccessor);
                  } else {
                     builderState.pos2(intTriple, componentAccessor);
                  }
               });
               break;
            case ActivateToolMode:
               if (!hasPermission(playerRef, "hytale.editor.builderTools")) {
                  return;
               }

               playerComponent.getInventory().setUsingToolsItem(true);
               break;
            case DeactivateToolMode:
               if (!hasPermission(playerRef, "hytale.editor.builderTools")) {
                  return;
               }

               playerComponent.getInventory().setUsingToolsItem(false);
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
         BuilderToolsPlugin.addToQueue(
            playerComponent, playerRef, (r, s, componentAccessor) -> s.update(packet.xMin, packet.yMin, packet.zMin, packet.xMax, packet.yMax, packet.zMax)
         );
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
                  int selectionX = selection.getX();
                  int selectionY = selection.getY();
                  int selectionZ = selection.getZ();
                  selection.forEachEntity(
                     holder -> {
                        TransformComponent transform = holder.getComponent(TransformComponent.getComponentType());
                        if (transform != null && transform.getPosition() != null) {
                           Vector3d pos = transform.getPosition();
                           entityChanges.add(
                              new PrototypePlayerBuilderToolSettings.EntityChange(
                                 pos.getX() + selectionX, pos.getY() + selectionY, pos.getZ() + selectionZ, holder.clone()
                              )
                           );
                        }
                     }
                  );
                  prototypeSettings.setEntityChangesForPlaySelectionToolPasteMode(entityChanges.toArray(PrototypePlayerBuilderToolSettings.EntityChange[]::new));
                  FluidChange[] packetFluids = new FluidChange[fluidChangesArray.length];

                  for (int i = 0; i < fluidChangesArray.length; i++) {
                     PrototypePlayerBuilderToolSettings.FluidChange fc = fluidChangesArray[i];
                     packetFluids[i] = new FluidChange(fc.x(), fc.y(), fc.z(), fc.fluidId(), fc.fluidLevel());
                  }

                  playerRef.getPacketHandler().write(new BuilderToolSelectionToolReplyWithClipboard(blocksChange, packetFluids));
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
         BuilderTool builderTool = BuilderTool.getActiveBuilderTool(playerComponent);
         if (builderTool != null && builderTool.getId().equals("Selection")) {
            BuilderTool.ArgData args = builderTool.getItemArgData(playerComponent.getInventory().getItemInHand());
            if (args != null && args.tool() != null) {
               keepEmptyBlocks = (Boolean)args.tool().getOrDefault("KeepEmptyBlocks", true);
            }
         }

         boolean finalKeepEmptyBlocks = keepEmptyBlocks;
         Quaterniond rotation = new Quaterniond(packet.rotation.x, packet.rotation.y, packet.rotation.z, packet.rotation.w);
         Vector3i translationOffset = new Vector3i(packet.translationOffset.x, packet.translationOffset.y, packet.translationOffset.z);
         Vector3i initialSelectionMin = new Vector3i(packet.initialSelectionMin.x, packet.initialSelectionMin.y, packet.initialSelectionMin.z);
         Vector3i initialSelectionMax = new Vector3i(packet.initialSelectionMax.x, packet.initialSelectionMax.y, packet.initialSelectionMax.z);
         Vector3f rotationOrigin = new Vector3f(packet.initialRotationOrigin.x, packet.initialRotationOrigin.y, packet.initialRotationOrigin.z);
         PrototypePlayerBuilderToolSettings prototypeSettings = ToolOperation.getOrCreatePrototypeSettings(playerRef.getUuid());
         BuilderToolsPlugin.addToQueue(
            playerComponent,
            playerRef,
            (r, s, componentAccessor) -> {
               int blockCount = s.getSelection().getSelectionVolume();
               boolean large = blockCount > 20000;
               if (large) {
                  playerComponent.sendMessage(Message.translation("server.builderTools.selection.large.warning"));
               }

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
                  BlockChange[] blocksChange = selection.toPacket().blocksChange;
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
                  prototypeSettings.setFluidChangesForPlaySelectionToolPasteMode(fluidChanges.toArray(PrototypePlayerBuilderToolSettings.FluidChange[]::new));
                  ArrayList<PrototypePlayerBuilderToolSettings.EntityChange> entityChanges = new ArrayList<>();
                  int selectionX = selection.getX();
                  int selectionY = selection.getY();
                  int selectionZ = selection.getZ();
                  selection.forEachEntity(
                     holder -> {
                        TransformComponent transform = holder.getComponent(TransformComponent.getComponentType());
                        if (transform != null && transform.getPosition() != null) {
                           Vector3d pos = transform.getPosition();
                           entityChanges.add(
                              new PrototypePlayerBuilderToolSettings.EntityChange(
                                 pos.getX() + selectionX, pos.getY() + selectionY, pos.getZ() + selectionZ, holder.clone()
                              )
                           );
                        }
                     }
                  );
                  prototypeSettings.setEntityChangesForPlaySelectionToolPasteMode(entityChanges.toArray(PrototypePlayerBuilderToolSettings.EntityChange[]::new));
                  prototypeSettings.setBlockChangeOffsetOrigin(new Vector3i(selection.getX(), selection.getY(), selection.getZ()));
               }

               Vector3i blockChangeOffsetOrigin = prototypeSettings.getBlockChangeOffsetOrigin();
               if (packet.initialPastePointForClipboardPaste != null) {
                  blockChangeOffsetOrigin = new Vector3i(
                     packet.initialPastePointForClipboardPaste.x, packet.initialPastePointForClipboardPaste.y, packet.initialPastePointForClipboardPaste.z
                  );
               }

               if (blockChangeOffsetOrigin == null) {
                  playerComponent.sendMessage(Message.translation("server.builderTools.selection.noBlockChangeOffsetOrigin"));
               } else {
                  s.transformThenPasteClipboard(
                     prototypeSettings.getBlockChangesForPlaySelectionToolPasteMode(),
                     prototypeSettings.getFluidChangesForPlaySelectionToolPasteMode(),
                     prototypeSettings.getEntityChangesForPlaySelectionToolPasteMode(),
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
                  if (large) {
                     playerComponent.sendMessage(Message.translation("server.builderTools.selection.large.complete"));
                  }

                  if (packet.isExitingTransformMode) {
                     prototypeSettings.setInSelectionTransformationMode(false);
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
         BuilderTool builderTool = BuilderTool.getActiveBuilderTool(playerComponent);
         if (builderTool != null && builderTool.getId().equals("Extrude")) {
            ItemStack activeItemStack = playerComponent.getInventory().getItemInHand();
            BuilderTool.ArgData args = builderTool.getItemArgData(activeItemStack);
            int extrudeDepth = (Integer)args.tool().get("ExtrudeDepth");
            int extrudeRadius = (Integer)args.tool().get("ExtrudeRadius");
            int blockId = ((BlockPattern)args.tool().get("ExtrudeMaterial")).firstBlock();
            LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
            BuilderToolsPlugin.addToQueue(
               playerComponent,
               playerRef,
               (r, s, componentAccessor) -> s.extendFace(
                  packet.x,
                  packet.y,
                  packet.z,
                  packet.xNormal,
                  packet.yNormal,
                  packet.zNormal,
                  extrudeDepth,
                  extrudeRadius,
                  blockId,
                  null,
                  null,
                  componentAccessor
               )
            );
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
         LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
         BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.rotate(r, axis, packet.angle, componentAccessor));
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
         LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
         BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.paste(r, packet.x, packet.y, packet.z, componentAccessor));
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
         BuilderTool builderTool = BuilderTool.getActiveBuilderTool(playerComponent);
         if (builderTool != null && builderTool.getId().equals("Line")) {
            BuilderTool.ArgData args = builderTool.getItemArgData(playerComponent.getInventory().getItemInHand());
            BrushData.Values brushData = args.brush();
            Map<String, Object> tool = args.tool();
            if (tool != null) {
               int lineWidth = (Integer)tool.get("bLineWidth");
               int lineHeight = (Integer)tool.get("cLineHeight");
               BrushShape lineShape = BrushShape.valueOf((String)tool.get("dLineShape"));
               BrushOrigin lineOrigin = BrushOrigin.valueOf((String)tool.get("eLineOrigin"));
               int lineWallThickness = (Integer)tool.get("fLineWallThickness");
               int lineSpacing = (Integer)tool.get("gLineSpacing");
               int lineDensity = (Integer)tool.get("hLineDensity");
               BlockPattern lineMaterial = (BlockPattern)tool.get("aLineMaterial");
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
                     ToolOperation.combineMasks(brushData, s.getGlobalMask()),
                     componentAccessor
                  )
               );
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
      Ref<EntityStore> targetRef = world.getEntityStore().getRefFromNetworkId(packet.entityId);
      if (targetRef != null && targetRef.isValid()) {
         TransformComponent transformComponent = store.getComponent(targetRef, TransformComponent.getComponentType());
         if (transformComponent != null) {
            HeadRotation headRotation = store.getComponent(targetRef, HeadRotation.getComponentType());
            ModelTransform modelTransform = packet.modelTransform;
            if (modelTransform != null) {
               boolean hasPosition = modelTransform.position != null;
               boolean hasLookOrientation = modelTransform.lookOrientation != null;
               boolean hasBodyOrientation = modelTransform.bodyOrientation != null;
               if (hasPosition) {
                  transformComponent.getPosition().assign(modelTransform.position.x, modelTransform.position.y, modelTransform.position.z);
               }

               if (hasLookOrientation && headRotation != null) {
                  headRotation.getRotation()
                     .assign(modelTransform.lookOrientation.pitch, modelTransform.lookOrientation.yaw, modelTransform.lookOrientation.roll);
               }

               if (hasBodyOrientation) {
                  transformComponent.getRotation()
                     .assign(modelTransform.bodyOrientation.pitch, modelTransform.bodyOrientation.yaw, modelTransform.bodyOrientation.roll);
               }

               if (hasPosition || hasLookOrientation || hasBodyOrientation) {
                  transformComponent.markChunkDirty(store);
               }
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
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         LOGGER.at(Level.INFO).log("%s: %s", this.packetHandler.getIdentifier(), packet);
         PrefabEditSessionManager prefabEditSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();
         PrefabEditSession prefabEditSession = prefabEditSessionManager.getPrefabEditSession(playerRef.getUuid());
         if (prefabEditSession == null) {
            playerComponent.sendMessage(Message.translation("server.commands.editprefab.notInEditSession"));
         } else {
            if (prefabEditSession.clearSelectedPrefab(ref, store)) {
               playerComponent.sendMessage(Message.translation("server.commands.editprefab.unselected"));
            } else {
               playerComponent.sendMessage(Message.translation("server.commands.editprefab.noPrefabSelected"));
            }
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
      Ref<EntityStore> targetRef = world.getEntityStore().getRefFromNetworkId(packet.entityId);
      if (targetRef != null && targetRef.isValid()) {
         PropComponent propComponent = store.getComponent(targetRef, PropComponent.getComponentType());
         if (propComponent != null) {
            EntityScaleComponent scaleComponent = store.getComponent(targetRef, EntityScaleComponent.getComponentType());
            if (scaleComponent == null) {
               scaleComponent = new EntityScaleComponent(packet.scale);
               store.addComponent(targetRef, EntityScaleComponent.getComponentType(), scaleComponent);
            } else {
               scaleComponent.setScale(packet.scale);
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
      Ref<EntityStore> targetRef = world.getEntityStore().getRefFromNetworkId(packet.entityId);
      if (targetRef != null && targetRef.isValid()) {
         PropComponent propComponent = store.getComponent(targetRef, PropComponent.getComponentType());
         if (propComponent != null) {
            if (packet.enabled) {
               store.ensureComponent(targetRef, Interactable.getComponentType());
               if (store.getComponent(targetRef, PreventPickup.getComponentType()) != null) {
                  store.removeComponent(targetRef, PreventPickup.getComponentType());
               }

               Interactions interactionsComponent = store.getComponent(targetRef, Interactions.getComponentType());
               if (interactionsComponent == null) {
                  interactionsComponent = new Interactions();
                  store.addComponent(targetRef, Interactions.getComponentType(), interactionsComponent);
               }

               interactionsComponent.setInteractionId(InteractionType.Use, "*PickupItem");
               interactionsComponent.setInteractionHint("server.interactionHints.pickup");
            } else {
               if (store.getComponent(targetRef, Interactable.getComponentType()) != null) {
                  store.removeComponent(targetRef, Interactable.getComponentType());
               }

               if (store.getComponent(targetRef, Interactions.getComponentType()) != null) {
                  store.removeComponent(targetRef, Interactions.getComponentType());
               }

               store.ensureComponent(targetRef, PreventPickup.getComponentType());
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
      Ref<EntityStore> targetRef = world.getEntityStore().getRefFromNetworkId(packet.entityId);
      if (targetRef != null && targetRef.isValid()) {
         if (packet.light == null) {
            store.removeComponent(targetRef, DynamicLight.getComponentType());
            store.removeComponent(targetRef, PersistentDynamicLight.getComponentType());
         } else {
            ColorLight colorLight = new ColorLight(packet.light.radius, packet.light.red, packet.light.green, packet.light.blue);
            store.putComponent(targetRef, DynamicLight.getComponentType(), new DynamicLight(colorLight));
            store.putComponent(targetRef, PersistentDynamicLight.getComponentType(), new PersistentDynamicLight(colorLight));
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
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         Ref<EntityStore> targetRef = world.getEntityStore().getRefFromNetworkId(packet.entityId);
         if (targetRef != null && targetRef.isValid()) {
            NPCMarkerComponent npcMarkerComponent = store.getComponent(targetRef, NPCMarkerComponent.getComponentType());
            if (npcMarkerComponent != null) {
               UUIDComponent uuidComponent = store.getComponent(targetRef, UUIDComponent.getComponentType());
               if (uuidComponent != null) {
                  UUID uuid = uuidComponent.getUuid();
                  String command = packet.enabled ? "npc debug set display --entity " + uuid : "npc debug clear --entity " + uuid;
                  CommandManager.get().handleCommand(playerComponent, command);
               }
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
      Ref<EntityStore> targetRef = world.getEntityStore().getRefFromNetworkId(packet.entityId);
      if (targetRef != null && targetRef.isValid()) {
         PropComponent propComponent = store.getComponent(targetRef, PropComponent.getComponentType());
         NPCMarkerComponent npcMarkerComponent = store.getComponent(targetRef, NPCMarkerComponent.getComponentType());
         if (propComponent != null || npcMarkerComponent != null) {
            if (packet.collisionType != null && !packet.collisionType.isEmpty()) {
               HitboxCollisionConfig hitboxCollisionConfig = HitboxCollisionConfig.getAssetMap().getAsset(packet.collisionType);
               if (hitboxCollisionConfig != null) {
                  store.putComponent(targetRef, HitboxCollision.getComponentType(), new HitboxCollision(hitboxCollisionConfig));
               }
            } else {
               store.removeComponent(targetRef, HitboxCollision.getComponentType());
            }
         }
      }
   }
}
