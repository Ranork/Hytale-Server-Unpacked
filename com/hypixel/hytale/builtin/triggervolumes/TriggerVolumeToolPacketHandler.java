package com.hypixel.hytale.builtin.triggervolumes;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.snapshot.SelectionSnapshot;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.manager.ConditionTiming;
import com.hypixel.hytale.builtin.triggervolumes.manager.CooldownMode;
import com.hypixel.hytale.builtin.triggervolumes.manager.GroupEntry;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.builtin.triggervolumes.shape.BoxShape;
import com.hypixel.hytale.builtin.triggervolumes.shape.CylinderShape;
import com.hypixel.hytale.builtin.triggervolumes.shape.SphereShape;
import com.hypixel.hytale.builtin.triggervolumes.shape.TriggerVolumeShape;
import com.hypixel.hytale.builtin.triggervolumes.snapshot.TriggerVolumeGroupSnapshot;
import com.hypixel.hytale.builtin.triggervolumes.snapshot.TriggerVolumeSnapshot;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.player.SelectionToolShowTriggerVolumes;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeConditionTiming;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeShapeType;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolCreate;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolCreateResponse;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolDelete;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolDuplicate;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolEquip;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolGroupCreate;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolGroupCreateResponse;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolGroupMove;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolMove;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolMultiMove;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolResize;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolSelect;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolSetActivationDelay;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolSetCancelDelayedOnExit;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolSetColor;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolSetConditionTiming;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolSetCooldown;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolSetKeepLoaded;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolSetTargetTypes;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeToolUngroup;
import com.hypixel.hytale.protocol.packets.player.UpdateTriggerVolumeDisplay;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.io.handlers.IPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.IWorldPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.SubPacketHandler;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class TriggerVolumeToolPacketHandler implements SubPacketHandler {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final String ID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
   private static final int ID_LENGTH = 6;
   private static final int[] GROUP_COLORS = new int[]{10506448, 4239424, 13656208, 14708784, 12599424};
   @Nonnull
   private final IPacketHandler packetHandler;
   private static final Pattern VALID_ID = Pattern.compile("^[a-zA-Z0-9_]{1,64}$");

   public TriggerVolumeToolPacketHandler(@Nonnull IPacketHandler packetHandler) {
      this.packetHandler = packetHandler;
   }

   private static boolean hasPermission(@Nonnull PlayerRef playerRef) {
      return PermissionsModule.get().hasPermission(playerRef.getUuid(), HytalePermissions.BUILDER_TOOLS_EDITOR);
   }

   @Nonnull
   private static String generateId(@Nonnull TriggerVolumeManager manager) {
      ThreadLocalRandom rng = ThreadLocalRandom.current();

      String id;
      do {
         StringBuilder builder = new StringBuilder("tv_");

         for (int charIdx = 0; charIdx < 6; charIdx++) {
            builder.append("abcdefghijklmnopqrstuvwxyz0123456789".charAt(rng.nextInt("abcdefghijklmnopqrstuvwxyz0123456789".length())));
         }

         id = builder.toString();
      } while (manager.hasVolume(id));

      return id;
   }

   private static void ensureViewing(@Nonnull PlayerRef playerRef, @Nonnull TriggerVolumeManager manager) {
      manager.addViewer(playerRef.getUuid(), TriggerVolumeManager.ViewSource.TOOL);
   }

   @Nullable
   private static BuilderToolsPlugin.BuilderState getBuilderState(
      @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef
   ) {
      Player player = store.getComponent(ref, Player.getComponentType());
      return player == null ? null : BuilderToolsPlugin.getState(player, playerRef);
   }

   private static void recordHistory(
      @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull SelectionSnapshot<?> snapshot
   ) {
      BuilderToolsPlugin.BuilderState state = getBuilderState(store, ref, playerRef);
      if (state != null) {
         state.pushTriggerVolumeHistory(snapshot);
      }
   }

   private static void recordHistory(
      @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull List<SelectionSnapshot<?>> snapshots
   ) {
      if (!snapshots.isEmpty()) {
         BuilderToolsPlugin.BuilderState state = getBuilderState(store, ref, playerRef);
         if (state != null) {
            state.pushTriggerVolumeHistory(snapshots);
         }
      }
   }

   @Override
   public void registerHandlers() {
      IWorldPacketHandler.registerHandler(this.packetHandler, 484, this::handleEquip, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 480, this::handleCreate, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 505, this::handleDuplicate, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 481, this::handleMove, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 482, this::handleResize, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 483, this::handleDelete, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 486, this::handleGroupCreate, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 488, this::handleUngroup, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 489, this::handleGroupMove, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 490, this::handleSelect, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 491, this::handleMultiMove, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 492, this::handleSetColor, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 493, this::handleSetTargetTypes, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 500, this::handleSetKeepLoaded, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 501, this::handleSetCooldown, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 502, this::handleSetActivationDelay, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 504, this::handleSetCancelDelayedOnExit, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 506, this::handleSetConditionTiming, TriggerVolumeToolPacketHandler::hasPermission);
      IWorldPacketHandler.registerHandler(this.packetHandler, 503, this::handleSelectionToolShowTriggerVolumes, TriggerVolumeToolPacketHandler::hasPermission);
   }

   private void handleEquip(
      @Nonnull TriggerVolumeToolEquip packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         UUID uuid = playerRef.getUuid();
         if (packet.active) {
            manager.addViewer(uuid, TriggerVolumeManager.ViewSource.TOOL);
            manager.sendVolumeDisplay(playerRef);
         } else {
            manager.removeViewer(uuid, TriggerVolumeManager.ViewSource.TOOL);
            if (!manager.isViewing(uuid)) {
               playerRef.getPacketHandler().write(new UpdateTriggerVolumeDisplay());
            }
         }
      }
   }

   private void handleCreate(
      @Nonnull TriggerVolumeToolCreate packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         ensureViewing(playerRef, manager);
         String worldName = world.getName().toLowerCase(Locale.ROOT);
         Vector3d position = new Vector3d(packet.position.x(), packet.position.y(), packet.position.z());
         TriggerVolumeShape shape = buildShape(
            packet.shapeType,
            packet.param1.x(),
            packet.param1.y(),
            packet.param1.z(),
            packet.param2 != null ? packet.param2.x() : 0.0F,
            packet.param2 != null ? packet.param2.y() : 0.0F,
            packet.param2 != null ? packet.param2.z() : 0.0F,
            packet.param2 != null
         );
         if (shape == null) {
            LOGGER.at(Level.WARNING).log("Invalid shape type from %s", playerRef.getUuid());
         } else {
            String name = packet.name;
            if (name == null || name.isBlank()) {
               name = generateId(manager);
            }

            if (manager.hasVolume(name)) {
               name = generateId(manager);
            }

            VolumeEntry entry = new VolumeEntry(name, worldName, position, shape, new ArrayList<>(), EnumSet.of(EntityTargetType.PLAYER), true);
            manager.register(name, entry);
            manager.notifyViewersAdd(entry);
            recordHistory(store, ref, playerRef, TriggerVolumeSnapshot.ofCreate(entry));
            TriggerVolumeToolCreateResponse response = new TriggerVolumeToolCreateResponse();
            response.volumeId = name;
            response.volumeIds = new String[]{name};
            playerRef.getPacketHandler().write(response);
         }
      }
   }

   private void handleDuplicate(
      @Nonnull TriggerVolumeToolDuplicate packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         ensureViewing(playerRef, manager);
         if (packet.volumeIds != null && packet.volumeIds.length != 0) {
            ArrayList<String> newIds = new ArrayList<>();
            ArrayList<SelectionSnapshot<?>> snapshots = new ArrayList<>();
            String worldName = world.getName().toLowerCase(Locale.ROOT);

            for (String volumeId : packet.volumeIds) {
               VolumeEntry source = manager.getVolume(volumeId);
               if (source != null) {
                  String newId = generateId(manager);
                  VolumeEntry entry = duplicateEntry(source, newId, worldName);
                  String groupId = source.getGroupId();
                  if (groupId != null) {
                     GroupEntry group = manager.getGroup(groupId);
                     if (group != null) {
                        entry.setGroupId(groupId);
                        group.addMember(newId);
                     }
                  }

                  manager.register(newId, entry);
                  manager.notifyViewersAdd(entry);
                  snapshots.add(TriggerVolumeSnapshot.ofCreate(entry));
                  newIds.add(newId);
               }
            }

            if (!newIds.isEmpty()) {
               manager.markSpatialDirty();
               recordHistory(store, ref, playerRef, snapshots);
               TriggerVolumeToolCreateResponse response = new TriggerVolumeToolCreateResponse();
               response.volumeId = newIds.getFirst();
               response.volumeIds = newIds.toArray(String[]::new);
               playerRef.getPacketHandler().write(response);
            }
         }
      }
   }

   @Nonnull
   private static VolumeEntry duplicateEntry(@Nonnull VolumeEntry source, @Nonnull String newId, @Nonnull String worldName) {
      VolumeEntry entry = new VolumeEntry(
         newId,
         worldName,
         new Vector3d(source.getPosition()),
         source.getShape().copy(),
         TriggerEffect.deepCopyList(source.getEffects()),
         EnumSet.copyOf(source.getTargetTypes()),
         source.isEnabled()
      );
      entry.getConditions().addAll(TriggerCondition.deepCopyList(source.getConditions()));
      entry.getRejectionEffects().addAll(TriggerEffect.deepCopyList(source.getRejectionEffects()));
      entry.setConditionTiming(source.getConditionTiming());
      entry.setRejectionDelayMode(source.getRejectionDelayMode());
      entry.setProjectileSource(source.getProjectileSource());
      entry.setEffectAssetRef(source.getEffectAssetRef());
      entry.setKeepLoaded(source.isKeepLoaded());
      entry.setActivationDelay(source.getActivationDelay());
      entry.setCancelDelayedEffectsOnExit(source.isCancelDelayedEffectsOnExit());
      entry.setCooldown(source.getCooldown());
      entry.setCooldownMode(source.getCooldownMode());
      entry.setColor(source.getColor() != null ? new Vector3f(source.getColor()) : null);
      if (!source.getRawTags().isEmpty()) {
         entry.setTags(copyTags(source.getRawTags()));
      }

      return entry;
   }

   private void handleMove(
      @Nonnull TriggerVolumeToolMove packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         ensureViewing(playerRef, manager);
         VolumeEntry entry = manager.getVolume(packet.volumeId);
         if (entry != null) {
            recordHistory(store, ref, playerRef, TriggerVolumeSnapshot.ofMutate(entry));
            Vector3d displayCenter = new Vector3d(packet.newPosition.x(), packet.newPosition.y(), packet.newPosition.z());
            Vector3d offset = computeDisplayOffset(entry.getShape());
            entry.setPosition(new Vector3d(displayCenter).sub(offset));
            manager.markSpatialDirty();
            manager.notifyViewersAdd(entry);
         }
      }
   }

   private void handleResize(
      @Nonnull TriggerVolumeToolResize packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         ensureViewing(playerRef, manager);
         VolumeEntry entry = manager.getVolume(packet.volumeId);
         if (entry != null) {
            TriggerVolumeShape shape = buildShape(
               packet.shapeType,
               packet.param1.x(),
               packet.param1.y(),
               packet.param1.z(),
               packet.param2 != null ? packet.param2.x() : 0.0F,
               packet.param2 != null ? packet.param2.y() : 0.0F,
               packet.param2 != null ? packet.param2.z() : 0.0F,
               packet.param2 != null
            );
            if (shape != null) {
               recordHistory(store, ref, playerRef, TriggerVolumeSnapshot.ofMutate(entry));
               entry.setShape(shape);
               if (packet.newPosition != null) {
                  Vector3d displayCenter = new Vector3d(packet.newPosition.x(), packet.newPosition.y(), packet.newPosition.z());
                  Vector3d offset = computeDisplayOffset(shape);
                  entry.setPosition(new Vector3d(displayCenter).sub(offset));
               }

               manager.markSpatialDirty();
               manager.notifyViewersAdd(entry);
            }
         }
      }
   }

   private void handleDelete(
      @Nonnull TriggerVolumeToolDelete packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         ensureViewing(playerRef, manager);
         VolumeEntry entry = manager.getVolume(packet.volumeId);
         if (entry != null) {
            recordHistory(store, ref, playerRef, TriggerVolumeSnapshot.ofDelete(entry));
            if (entry.getGroupId() != null) {
               GroupEntry group = manager.getGroup(entry.getGroupId());
               if (group != null) {
                  group.removeMember(packet.volumeId);
                  if (group.getMemberVolumeIds().isEmpty()) {
                     manager.unregisterGroup(group.getId());
                  }
               }
            }

            manager.unregister(packet.volumeId);
            manager.notifyViewersRemove(packet.volumeId);
         }
      }
   }

   private void handleGroupCreate(
      @Nonnull TriggerVolumeToolGroupCreate packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         ensureViewing(playerRef, manager);
         if (packet.volumeIds != null && packet.volumeIds.length >= 2) {
            Vector3d centroid = new Vector3d();
            int count = 0;
            int skipped = 0;
            ArrayList<VolumeEntry> validVolumes = new ArrayList<>();

            for (String volumeId : packet.volumeIds) {
               VolumeEntry volume = manager.getVolume(volumeId);
               if (volume != null) {
                  if (volume.getGroupId() != null) {
                     skipped++;
                  } else {
                     validVolumes.add(volume);
                     centroid.add(volume.getPosition());
                     count++;
                  }
               }
            }

            if (count < 2) {
               TriggerVolumeToolGroupCreateResponse failResponse = new TriggerVolumeToolGroupCreateResponse();
               failResponse.groupId = "";
               failResponse.success = false;
               failResponse.skippedCount = skipped;
               playerRef.getPacketHandler().write(failResponse);
            } else {
               List<SelectionSnapshot<?>> snapshots = new ArrayList<>();

               for (VolumeEntry volume : validVolumes) {
                  snapshots.add(TriggerVolumeSnapshot.ofMutate(volume));
               }

               centroid.div(count);
               EnumSet<EntityTargetType> mergedTargets = EnumSet.noneOf(EntityTargetType.class);

               for (VolumeEntry volume : validVolumes) {
                  mergedTargets.addAll(volume.getTargetTypes());
               }

               if (mergedTargets.isEmpty()) {
                  mergedTargets.add(EntityTargetType.PLAYER);
               }

               String groupId = generateGroupId(manager);
               int color = GROUP_COLORS[ThreadLocalRandom.current().nextInt(GROUP_COLORS.length)];
               GroupEntry groupEntry = new GroupEntry(
                  groupId, world.getName().toLowerCase(Locale.ROOT), centroid, new ArrayList<>(), mergedTargets, true, color
               );
               manager.registerGroup(groupId, groupEntry);

               for (VolumeEntry volume : validVolumes) {
                  volume.setGroupId(groupId);
                  groupEntry.addMember(volume.getId());
                  manager.notifyViewersAdd(volume);
               }

               recordHistory(store, ref, playerRef, snapshots);
               TriggerVolumeToolGroupCreateResponse response = new TriggerVolumeToolGroupCreateResponse();
               response.groupId = groupId;
               response.color = color;
               response.success = true;
               response.skippedCount = skipped;
               playerRef.getPacketHandler().write(response);
            }
         }
      }
   }

   private void handleUngroup(
      @Nonnull TriggerVolumeToolUngroup packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         ensureViewing(playerRef, manager);
         GroupEntry group = manager.getGroup(packet.groupId);
         if (group != null) {
            List<SelectionSnapshot<?>> snapshots = new ArrayList<>();
            snapshots.add(TriggerVolumeGroupSnapshot.ofDelete(group));

            for (String volumeId : new ArrayList<>(group.getMemberVolumeIds())) {
               VolumeEntry volume = manager.getVolume(volumeId);
               if (volume != null) {
                  snapshots.add(TriggerVolumeSnapshot.ofMutate(volume));
                  volume.clearGroupActivationState(packet.groupId);
                  volume.setGroupId(null);
                  manager.notifyViewersAdd(volume);
               }
            }

            recordHistory(store, ref, playerRef, snapshots);
            manager.unregisterGroup(packet.groupId);
         }
      }
   }

   private void handleGroupMove(
      @Nonnull TriggerVolumeToolGroupMove packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         ensureViewing(playerRef, manager);
         GroupEntry group = manager.getGroup(packet.groupId);
         if (group != null) {
            List<SelectionSnapshot<?>> snapshots = new ArrayList<>();

            for (String volumeId : group.getMemberVolumeIds()) {
               VolumeEntry volume = manager.getVolume(volumeId);
               if (volume != null) {
                  snapshots.add(TriggerVolumeSnapshot.ofMutate(volume));
               }
            }

            Vector3d delta = new Vector3d(packet.moveDelta.x(), packet.moveDelta.y(), packet.moveDelta.z());
            group.setOrigin(new Vector3d(group.getOrigin()).add(delta));

            for (String volumeIdx : group.getMemberVolumeIds()) {
               VolumeEntry volume = manager.getVolume(volumeIdx);
               if (volume != null) {
                  volume.setPosition(new Vector3d(volume.getPosition()).add(delta));
                  manager.notifyViewersAdd(volume);
               }
            }

            manager.markSpatialDirty();
            recordHistory(store, ref, playerRef, snapshots);
         }
      }
   }

   private void handleSelect(
      @Nonnull TriggerVolumeToolSelect packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         manager.setPlayerSelection(playerRef.getUuid(), packet.volumeId);
      }
   }

   private void handleMultiMove(
      @Nonnull TriggerVolumeToolMultiMove packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         ensureViewing(playerRef, manager);
         if (packet.volumeIds != null && packet.volumeIds.length != 0) {
            List<SelectionSnapshot<?>> snapshots = new ArrayList<>();
            Vector3d delta = new Vector3d(packet.moveDelta.x(), packet.moveDelta.y(), packet.moveDelta.z());

            for (String volumeId : packet.volumeIds) {
               VolumeEntry volume = manager.getVolume(volumeId);
               if (volume != null) {
                  snapshots.add(TriggerVolumeSnapshot.ofMutate(volume));
                  volume.setPosition(new Vector3d(volume.getPosition()).add(delta));
                  manager.notifyViewersAdd(volume);
               }
            }

            manager.markSpatialDirty();
            recordHistory(store, ref, playerRef, snapshots);
         }
      }
   }

   private void handleSetColor(
      @Nonnull TriggerVolumeToolSetColor packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         ensureViewing(playerRef, manager);
         VolumeEntry entry = manager.getVolume(packet.volumeId);
         if (entry != null) {
            recordHistory(store, ref, playerRef, TriggerVolumeSnapshot.ofMutate(entry));
            entry.setColor(new Vector3f(packet.color.x(), packet.color.y(), packet.color.z()));
            if (entry.getGroupId() != null) {
               GroupEntry group = manager.getGroup(entry.getGroupId());
               if (group != null) {
                  int red = (int)(packet.color.x() * 255.0F) & 0xFF;
                  int green = (int)(packet.color.y() * 255.0F) & 0xFF;
                  int blue = (int)(packet.color.z() * 255.0F) & 0xFF;
                  group.setColor(red << 16 | green << 8 | blue);

                  for (String memberId : group.getMemberVolumeIds()) {
                     VolumeEntry member = manager.getVolume(memberId);
                     if (member != null) {
                        manager.notifyViewersAdd(member);
                     }
                  }
               }
            } else {
               manager.notifyViewersAdd(entry);
            }
         }
      }
   }

   private void handleSetTargetTypes(
      @Nonnull TriggerVolumeToolSetTargetTypes packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         ensureViewing(playerRef, manager);
         VolumeEntry entry = manager.getVolume(packet.volumeId);
         if (entry != null) {
            recordHistory(store, ref, playerRef, TriggerVolumeSnapshot.ofMutate(entry));
            Set<EntityTargetType> types = entry.getTargetTypes();
            types.clear();
            if ((packet.targetTypes & 1) != 0) {
               types.add(EntityTargetType.PLAYER);
            }

            if ((packet.targetTypes & 2) != 0) {
               types.add(EntityTargetType.NPC);
            }

            if ((packet.targetTypes & 4) != 0) {
               types.add(EntityTargetType.ITEM_DROP);
            }

            if ((packet.targetTypes & 8) != 0) {
               types.add(EntityTargetType.PROJECTILE);
            }

            if (types.isEmpty()) {
               types.add(EntityTargetType.PLAYER);
            }

            manager.notifyViewersAdd(entry);
         }
      }
   }

   private void handleSetKeepLoaded(
      @Nonnull TriggerVolumeToolSetKeepLoaded packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         ensureViewing(playerRef, manager);
         VolumeEntry entry = manager.getVolume(packet.volumeId);
         if (entry != null) {
            recordHistory(store, ref, playerRef, TriggerVolumeSnapshot.ofMutate(entry));
            entry.setKeepLoaded(packet.keepLoaded);
            manager.notifyViewersAdd(entry);
         }
      }
   }

   private void handleSetCooldown(
      @Nonnull TriggerVolumeToolSetCooldown packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         ensureViewing(playerRef, manager);
         VolumeEntry entry = manager.getVolume(packet.volumeId);
         if (entry != null) {
            recordHistory(store, ref, playerRef, TriggerVolumeSnapshot.ofMutate(entry));
            entry.setCooldown(Math.max(0.0F, packet.cooldown));
            CooldownMode mode = packet.cooldownMode < CooldownMode.values().length ? CooldownMode.values()[packet.cooldownMode] : CooldownMode.PER_ENTITY;
            entry.setCooldownMode(mode);
            manager.notifyViewersAdd(entry);
         }
      }
   }

   private void handleSelectionToolShowTriggerVolumes(
      @Nonnull SelectionToolShowTriggerVolumes packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         UUID uuid = playerRef.getUuid();
         if (packet.active) {
            manager.addViewer(uuid, TriggerVolumeManager.ViewSource.SELECTION_TOOL);
            manager.sendVolumeDisplay(playerRef);
         } else {
            manager.removeViewer(uuid, TriggerVolumeManager.ViewSource.SELECTION_TOOL);
            if (!manager.isViewing(uuid)) {
               playerRef.getPacketHandler().write(new UpdateTriggerVolumeDisplay());
            }
         }
      }
   }

   private void handleSetCancelDelayedOnExit(
      @Nonnull TriggerVolumeToolSetCancelDelayedOnExit packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         ensureViewing(playerRef, manager);
         VolumeEntry entry = manager.getVolume(packet.volumeId);
         if (entry != null) {
            recordHistory(store, ref, playerRef, TriggerVolumeSnapshot.ofMutate(entry));
            entry.setCancelDelayedEffectsOnExit(packet.cancelDelayedOnExit);
            manager.notifyViewersAdd(entry);
         }
      }
   }

   private void handleSetActivationDelay(
      @Nonnull TriggerVolumeToolSetActivationDelay packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         ensureViewing(playerRef, manager);
         VolumeEntry entry = manager.getVolume(packet.volumeId);
         if (entry != null) {
            recordHistory(store, ref, playerRef, TriggerVolumeSnapshot.ofMutate(entry));
            entry.setActivationDelay(Math.max(0.0F, packet.activationDelay));
            manager.notifyViewersAdd(entry);
         }
      }
   }

   private void handleSetConditionTiming(
      @Nonnull TriggerVolumeToolSetConditionTiming packet,
      @Nonnull PlayerRef playerRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
      if (manager != null) {
         ensureViewing(playerRef, manager);
         VolumeEntry entry = manager.getVolume(packet.volumeId);
         if (entry != null) {
            recordHistory(store, ref, playerRef, TriggerVolumeSnapshot.ofMutate(entry));
            entry.setConditionTiming(
               packet.conditionTiming == TriggerVolumeConditionTiming.BeforeVolumeDelay
                  ? ConditionTiming.BEFORE_VOLUME_DELAY
                  : ConditionTiming.AFTER_VOLUME_DELAY
            );
            manager.notifyViewersAdd(entry);
         }
      }
   }

   @Nonnull
   private static String generateGroupId(@Nonnull TriggerVolumeManager manager) {
      ThreadLocalRandom rng = ThreadLocalRandom.current();

      String id;
      do {
         StringBuilder builder = new StringBuilder("tvg_");

         for (int charIdx = 0; charIdx < 6; charIdx++) {
            builder.append("abcdefghijklmnopqrstuvwxyz0123456789".charAt(rng.nextInt("abcdefghijklmnopqrstuvwxyz0123456789".length())));
         }

         id = builder.toString();
      } while (manager.hasGroup(id));

      return id;
   }

   @Nonnull
   private static Vector3d computeDisplayOffset(@Nonnull TriggerVolumeShape shape) {
      if (shape instanceof BoxShape box) {
         return new Vector3d(box.getMin()).add(box.getMax()).mul(0.5);
      } else if (shape instanceof SphereShape sphere) {
         return new Vector3d(sphere.getCenter());
      } else {
         return shape instanceof CylinderShape cyl
            ? new Vector3d(cyl.getCenter().x(), cyl.getCenter().y() + cyl.getHeight() * 0.5, cyl.getCenter().z())
            : new Vector3d();
      }
   }

   @Nonnull
   private static Map<String, String> copyTags(@Nonnull Map<String, String> tags) {
      if (tags.isEmpty()) {
         return Map.of();
      } else {
         HashMap<String, String> copy = new HashMap<>();

         for (Entry<String, String> entry : tags.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
         }

         return copy;
      }
   }

   @Nullable
   private static TriggerVolumeShape buildShape(
      @Nonnull TriggerVolumeShapeType shapeType, float p1x, float p1y, float p1z, float p2x, float p2y, float p2z, boolean hasParam2
   ) {
      return (TriggerVolumeShape)(switch (shapeType) {
         case Box -> !hasParam2 ? null : new BoxShape(new Vector3d(p1x, p1y, p1z), new Vector3d(p2x, p2y, p2z));
         case Sphere -> new SphereShape(new Vector3d(0.0, 0.0, 0.0), p1x);
         case Cylinder -> new CylinderShape(new Vector3d(0.0, 0.0, 0.0), p1x, p1y);
      });
   }
}
