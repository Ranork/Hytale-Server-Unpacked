package com.hypixel.hytale.builtin.triggervolumes.manager;

import com.hypixel.hytale.builtin.triggervolumes.EntityTargetType;
import com.hypixel.hytale.builtin.triggervolumes.component.TriggerVolumeGroup;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.TaggedVolumeEffectUtil;
import com.hypixel.hytale.builtin.triggervolumes.shape.BoxShape;
import com.hypixel.hytale.builtin.triggervolumes.shape.CylinderShape;
import com.hypixel.hytale.builtin.triggervolumes.shape.SphereShape;
import com.hypixel.hytale.builtin.triggervolumes.shape.TriggerVolumeShape;
import com.hypixel.hytale.builtin.triggervolumes.system.DelayedEffectScheduler;
import com.hypixel.hytale.builtin.triggervolumes.system.VolumeSpatialIndex;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.packets.player.AddOrUpdateTriggerVolumeDisplay;
import com.hypixel.hytale.protocol.packets.player.RemoveTriggerVolumeDisplay;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeConditionTiming;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeDisplayEntry;
import com.hypixel.hytale.protocol.packets.player.TriggerVolumeShapeType;
import com.hypixel.hytale.protocol.packets.player.UpdateTriggerVolumeDisplay;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class TriggerVolumeManager implements Resource<EntityStore> {
   @Nonnull
   public static final BuilderCodec<TriggerVolumeManager> CODEC = BuilderCodec.builder(TriggerVolumeManager.class, TriggerVolumeManager::new)
      .append(
         new KeyedCodec<>("Volumes", new MapCodec<>(VolumeEntry.CODEC, ConcurrentHashMap::new, false), false),
         (m, vols) -> m.volumes.putAll(vols),
         m -> m.volumes.isEmpty() ? null : m.volumes
      )
      .add()
      .append(
         new KeyedCodec<>("Groups", new MapCodec<>(GroupEntry.CODEC, ConcurrentHashMap::new, false), false),
         (m, grps) -> m.groups.putAll(grps),
         m -> m.groups.isEmpty() ? null : m.groups
      )
      .add()
      .afterDecode(TriggerVolumeManager::postDecode)
      .build();
   private static final Vector3f COLOR_ENABLED = new Vector3f(0.0F, 0.8F, 0.8F);
   private static final Vector3f COLOR_DISABLED = new Vector3f(0.8F, 0.2F, 0.2F);
   private static final float OPACITY_ENABLED = 0.3F;
   private static final float OPACITY_DISABLED = 0.15F;
   private static final String PASTED_GROUP_ID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
   private static final int PASTED_GROUP_ID_LENGTH = 8;
   private static final int PASTED_VOLUME_ID_LENGTH = 6;
   private final Map<String, VolumeEntry> volumes = new ConcurrentHashMap<>();
   private final Map<String, GroupEntry> groups = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<String, String> prefabGroupLinkRemap = new ConcurrentHashMap<>();
   private final Map<UUID, EnumSet<TriggerVolumeManager.ViewSource>> activeViewers = new ConcurrentHashMap<>();
   private final Map<UUID, String> playerSelections = new ConcurrentHashMap<>();
   private final transient Map<UUID, TriggerVolumeManager.SelectionObserver> selectionObservers = new ConcurrentHashMap<>();
   private final transient Map<UUID, TriggerVolumeManager.VolumeUpdateObserver> volumeUpdateObservers = new ConcurrentHashMap<>();
   private final Set<Long> pendingWorldGenRegenChunks = ConcurrentHashMap.newKeySet();
   @Nonnull
   private final transient Deque<TriggerVolumeManager.PendingTriggerEvent> pendingEvents = new ArrayDeque<>();
   @Nonnull
   private final VolumeSpatialIndex spatialIndex = new VolumeSpatialIndex();
   @Nonnull
   private final transient DelayedEffectScheduler delayedEffectScheduler = new DelayedEffectScheduler();
   @Nullable
   private World world;

   private void postDecode() {
      for (Entry<String, VolumeEntry> entry : this.volumes.entrySet()) {
         entry.getValue().setId(entry.getKey());
         entry.getValue().expandTags();
      }

      for (Entry<String, GroupEntry> entry : this.groups.entrySet()) {
         entry.getValue().setId(entry.getKey());
      }

      for (VolumeEntry vol : this.volumes.values()) {
         if (vol.getGroupId() != null) {
            GroupEntry group = this.groups.get(vol.getGroupId());
            if (group != null) {
               group.addMember(vol.getId());
            }
         }
      }

      this.spatialIndex.markDirty();
   }

   public void register(@Nonnull String id, @Nonnull VolumeEntry entry) {
      this.volumes.put(id, entry);
      this.spatialIndex.markDirty();
   }

   public void unregister(@Nonnull String id) {
      VolumeEntry removed = this.volumes.remove(id);
      if (removed != null) {
         removed.markPendingDestroy();
      }

      this.spatialIndex.markDirty();
   }

   @Nullable
   public VolumeEntry renameVolume(@Nonnull String oldId, @Nonnull String newId) {
      if (oldId.equals(newId)) {
         return this.volumes.get(oldId);
      } else {
         VolumeEntry entry = this.volumes.remove(oldId);
         if (entry == null) {
            return null;
         } else if (this.volumes.putIfAbsent(newId, entry) != null) {
            this.volumes.put(oldId, entry);
            return null;
         } else {
            entry.setId(newId);

            for (GroupEntry group : this.groups.values()) {
               if (group.getMemberVolumeIds().remove(oldId)) {
                  group.addMember(newId);
               }
            }

            for (Entry<UUID, String> selectionEntry : this.playerSelections.entrySet()) {
               if (oldId.equals(selectionEntry.getValue())) {
                  this.playerSelections.put(selectionEntry.getKey(), newId);
               }
            }

            this.spatialIndex.markDirty();
            return entry;
         }
      }
   }

   public void removeWorldGenVolumesInChunk(long chunkIndex) {
      ArrayList<String> emptiedGroups = new ArrayList<>();
      boolean removedAny = false;
      Iterator<VolumeEntry> iter = this.volumes.values().iterator();

      while (iter.hasNext()) {
         VolumeEntry entry = iter.next();
         if (entry.isFromWorldGen()) {
            Vector3d pos = entry.getPosition();
            if (ChunkUtil.indexChunkFromBlock(pos.x(), pos.z()) == chunkIndex) {
               String groupId = entry.getGroupId();
               if (groupId != null) {
                  GroupEntry group = this.groups.get(groupId);
                  if (group != null) {
                     group.removeMember(entry.getId());
                     if (group.getMemberVolumeIds().isEmpty()) {
                        emptiedGroups.add(groupId);
                     }
                  }
               }

               entry.markPendingDestroy();
               iter.remove();
               this.notifyViewersRemove(entry.getId());
               removedAny = true;
            }
         }
      }

      for (String gid : emptiedGroups) {
         this.unregisterGroup(gid);
      }

      if (removedAny) {
         this.spatialIndex.markDirty();
      }
   }

   public void markWorldGenRegenChunk(long chunkIndex) {
      this.pendingWorldGenRegenChunks.add(chunkIndex);
   }

   public boolean consumeWorldGenRegenChunk(long chunkIndex) {
      return this.pendingWorldGenRegenChunks.remove(chunkIndex);
   }

   @Nullable
   public VolumeEntry getVolume(@Nonnull String id) {
      return this.volumes.get(id);
   }

   @Nonnull
   public Collection<VolumeEntry> getVolumes() {
      return Collections.unmodifiableCollection(this.volumes.values());
   }

   @Nonnull
   public Map<String, VolumeEntry> getVolumesMap() {
      return Collections.unmodifiableMap(this.volumes);
   }

   public boolean hasVolume(@Nonnull String id) {
      return this.volumes.containsKey(id);
   }

   public void markSpatialDirty() {
      this.spatialIndex.markDirty();
   }

   @Nonnull
   public VolumeSpatialIndex getSpatialIndex() {
      return this.spatialIndex;
   }

   @Nonnull
   public DelayedEffectScheduler getDelayedEffectScheduler() {
      return this.delayedEffectScheduler;
   }

   public void setWorld(@Nullable World world) {
      this.world = world;
   }

   @Nullable
   public World getWorld() {
      return this.world;
   }

   public void registerGroup(@Nonnull String id, @Nonnull GroupEntry entry) {
      this.groups.put(id, entry);
   }

   public void unregisterGroup(@Nonnull String id) {
      this.groups.remove(id);
   }

   @Nullable
   public GroupEntry getGroup(@Nonnull String id) {
      return this.groups.get(id);
   }

   public boolean hasGroup(@Nonnull String id) {
      return this.groups.containsKey(id);
   }

   public void clearPrefabGroupLinkRemap() {
      this.prefabGroupLinkRemap.clear();
   }

   @Nonnull
   public String ensureGroupForPrefabLink(@Nonnull String groupLinkId, @Nonnull VolumeEntry memberVolume, @Nonnull String worldName) {
      return this.prefabGroupLinkRemap
         .computeIfAbsent(
            groupLinkId,
            link -> {
               String gid = this.generateUniquePastedGroupId();
               int packed = memberVolume.getColor() != null
                  ? packRgb(memberVolume.getColor())
                  : (int)(COLOR_ENABLED.x() * 255.0F) << 16 | (int)(COLOR_ENABLED.y() * 255.0F) << 8 | (int)(COLOR_ENABLED.z() * 255.0F);
               GroupEntry ge = new GroupEntry(
                  gid,
                  worldName,
                  new Vector3d(memberVolume.getPosition()),
                  new ArrayList<>(),
                  EnumSet.copyOf(memberVolume.getTargetTypes()),
                  memberVolume.isEnabled(),
                  packed
               );
               this.registerGroup(gid, ge);
               return gid;
            }
         );
   }

   @Nonnull
   public String upsertGroupForPrefabLink(
      @Nonnull String groupLinkId, @Nonnull TriggerVolumeGroup groupComponent, @Nonnull String worldName, @Nonnull Vector3d origin
   ) {
      String gid = this.prefabGroupLinkRemap.computeIfAbsent(groupLinkId, link -> this.generateUniquePastedGroupId());
      GroupEntry existing = this.groups.get(gid);
      if (existing == null) {
         this.registerGroup(gid, groupComponent.toGroupEntry(gid, worldName, new Vector3d(origin)));
      } else {
         groupComponent.applyTo(existing, new Vector3d(origin));
      }

      return gid;
   }

   @Nullable
   public String ensureWorldGenGroup(int prefabInstanceId, @Nonnull String linkId, @Nonnull VolumeEntry memberVolume, @Nonnull String worldName) {
      String gid = getWorldGenGroupId(prefabInstanceId, linkId);
      if (gid == null) {
         return null;
      } else if (this.groups.containsKey(gid)) {
         return gid;
      } else {
         int packed = memberVolume.getColor() != null
            ? packRgb(memberVolume.getColor())
            : (int)(COLOR_ENABLED.x() * 255.0F) << 16 | (int)(COLOR_ENABLED.y() * 255.0F) << 8 | (int)(COLOR_ENABLED.z() * 255.0F);
         GroupEntry ge = new GroupEntry(
            gid,
            worldName,
            new Vector3d(memberVolume.getPosition()),
            new ArrayList<>(),
            EnumSet.copyOf(memberVolume.getTargetTypes()),
            memberVolume.isEnabled(),
            packed
         );
         this.registerGroup(gid, ge);
         return gid;
      }
   }

   @Nullable
   public String upsertWorldGenGroup(
      int prefabInstanceId, @Nonnull String linkId, @Nonnull TriggerVolumeGroup groupComponent, @Nonnull String worldName, @Nonnull Vector3d origin
   ) {
      String gid = getWorldGenGroupId(prefabInstanceId, linkId);
      if (gid == null) {
         return null;
      } else {
         GroupEntry existing = this.groups.get(gid);
         if (existing == null) {
            this.registerGroup(gid, groupComponent.toGroupEntry(gid, worldName, new Vector3d(origin)));
         } else {
            groupComponent.applyTo(existing, new Vector3d(origin));
         }

         return gid;
      }
   }

   @Nullable
   private static String getWorldGenGroupId(int prefabInstanceId, @Nonnull String linkId) {
      String sanitized = sanitizeWorldGenLinkId(linkId);
      return sanitized.isEmpty() ? null : "tvg_wg_" + Integer.toUnsignedString(prefabInstanceId, 36) + "_" + sanitized;
   }

   @Nonnull
   private static String sanitizeWorldGenLinkId(@Nonnull String linkId) {
      String lower = linkId.toLowerCase(Locale.ROOT);
      StringBuilder builder = new StringBuilder(lower.length());

      for (int i = 0; i < lower.length(); i++) {
         char c = lower.charAt(i);
         if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_') {
            builder.append(c);
         }
      }

      return builder.toString();
   }

   @Nonnull
   public String generateUniqueVolumeId() {
      ThreadLocalRandom rng = ThreadLocalRandom.current();

      String id;
      do {
         StringBuilder builder = new StringBuilder("tv_");

         for (int i = 0; i < 6; i++) {
            builder.append("abcdefghijklmnopqrstuvwxyz0123456789".charAt(rng.nextInt("abcdefghijklmnopqrstuvwxyz0123456789".length())));
         }

         id = builder.toString();
      } while (this.hasVolume(id));

      return id;
   }

   @Nonnull
   private String generateUniquePastedGroupId() {
      ThreadLocalRandom rng = ThreadLocalRandom.current();

      String id;
      do {
         StringBuilder builder = new StringBuilder("tvg_");

         for (int i = 0; i < 8; i++) {
            builder.append("abcdefghijklmnopqrstuvwxyz0123456789".charAt(rng.nextInt("abcdefghijklmnopqrstuvwxyz0123456789".length())));
         }

         id = builder.toString();
      } while (this.hasGroup(id));

      return id;
   }

   private static int packRgb(@Nonnull Vector3f c) {
      int r = Math.min(255, Math.max(0, Math.round(c.x() * 255.0F))) & 0xFF;
      int g = Math.min(255, Math.max(0, Math.round(c.y() * 255.0F))) & 0xFF;
      int b = Math.min(255, Math.max(0, Math.round(c.z() * 255.0F))) & 0xFF;
      return r << 16 | g << 8 | b;
   }

   @Nonnull
   public Map<String, GroupEntry> getGroupsMap() {
      return Collections.unmodifiableMap(this.groups);
   }

   @Nonnull
   public List<VolumeEntry> getGroupMembers(@Nonnull String groupId) {
      ArrayList<VolumeEntry> result = new ArrayList<>();

      for (VolumeEntry entry : this.volumes.values()) {
         if (groupId.equals(entry.getGroupId())) {
            result.add(entry);
         }
      }

      return result;
   }

   @Nonnull
   public List<VolumeEntry> getVolumesByTag(int tagIndex) {
      ArrayList<VolumeEntry> result = new ArrayList<>();

      for (VolumeEntry entry : this.volumes.values()) {
         if (entry.hasTag(tagIndex)) {
            result.add(entry);
         }
      }

      return result;
   }

   public boolean setTag(@Nonnull String volumeId, @Nonnull String key, @Nullable String value, @Nonnull Ref<EntityStore> actorRef, @Nonnull UUID actorUuid) {
      VolumeEntry volume = this.volumes.get(volumeId);
      if (volume == null) {
         return false;
      } else {
         String normalizedKey = key.trim();
         if (normalizedKey.isEmpty()) {
            return false;
         } else {
            String normalizedValue = TaggedVolumeEffectUtil.normalizeTagValue(value);
            String existing = volume.getRawTags().get(normalizedKey);
            if (Objects.equals(existing, normalizedValue)) {
               return false;
            } else {
               LinkedHashMap<String, String> tags = new LinkedHashMap<>(volume.getRawTags());
               tags.put(normalizedKey, normalizedValue);
               volume.setTags(tags);
               this.enqueuePendingEvent(
                  new TriggerVolumeManager.PendingTriggerEvent(
                     TriggerEventType.TAG_ADDED, actorRef, actorUuid, volumeId, null, null, normalizedKey, normalizedValue
                  )
               );
               return true;
            }
         }
      }
   }

   public boolean removeTag(@Nonnull String volumeId, @Nonnull String key, @Nonnull Ref<EntityStore> actorRef, @Nonnull UUID actorUuid) {
      return this.removeTag(volumeId, key, null, actorRef, actorUuid);
   }

   public boolean removeTag(@Nonnull String volumeId, @Nonnull String key, @Nullable String value, @Nonnull Ref<EntityStore> actorRef, @Nonnull UUID actorUuid) {
      VolumeEntry volume = this.volumes.get(volumeId);
      if (volume == null) {
         return false;
      } else {
         String normalizedKey = key.trim();
         if (!normalizedKey.isEmpty() && volume.getRawTags().containsKey(normalizedKey)) {
            String existing = volume.getRawTags().get(normalizedKey);
            String normalizedValue = TaggedVolumeEffectUtil.blankToNull(value);
            if (normalizedValue != null && !Objects.equals(existing, normalizedValue)) {
               return false;
            } else {
               LinkedHashMap<String, String> tags = new LinkedHashMap<>(volume.getRawTags());
               tags.remove(normalizedKey);
               volume.setTags(tags);
               this.enqueuePendingEvent(
                  new TriggerVolumeManager.PendingTriggerEvent(TriggerEventType.TAG_REMOVED, actorRef, actorUuid, volumeId, null, null, normalizedKey, existing)
               );
               return true;
            }
         } else {
            return false;
         }
      }
   }

   public void enqueueBlockEvent(
      @Nonnull TriggerEventType eventType,
      @Nonnull Ref<EntityStore> actorRef,
      @Nonnull UUID actorUuid,
      @Nonnull Vector3d blockPosition,
      @Nonnull String blockId
   ) {
      this.enqueuePendingEvent(
         new TriggerVolumeManager.PendingTriggerEvent(eventType, actorRef, actorUuid, null, new Vector3d(blockPosition), blockId, null, null)
      );
   }

   @Nullable
   public TriggerVolumeManager.PendingTriggerEvent pollPendingEvent() {
      return this.pendingEvents.pollFirst();
   }

   private void enqueuePendingEvent(@Nonnull TriggerVolumeManager.PendingTriggerEvent event) {
      this.pendingEvents.addLast(event);
   }

   public boolean isViewing(@Nonnull UUID playerUuid) {
      EnumSet<TriggerVolumeManager.ViewSource> sources = this.activeViewers.get(playerUuid);
      return sources != null && !sources.isEmpty();
   }

   public boolean isViewing(@Nonnull UUID playerUuid, @Nonnull TriggerVolumeManager.ViewSource source) {
      EnumSet<TriggerVolumeManager.ViewSource> sources = this.activeViewers.get(playerUuid);
      return sources != null && sources.contains(source);
   }

   @Nullable
   public EnumSet<TriggerVolumeManager.ViewSource> peekViewerSources(@Nonnull UUID playerUuid) {
      EnumSet<TriggerVolumeManager.ViewSource> sources = this.activeViewers.get(playerUuid);
      return sources != null ? EnumSet.copyOf(sources) : null;
   }

   public void addViewer(@Nonnull UUID playerUuid, @Nonnull TriggerVolumeManager.ViewSource source) {
      this.activeViewers.computeIfAbsent(playerUuid, k -> EnumSet.noneOf(TriggerVolumeManager.ViewSource.class)).add(source);
   }

   public void removeViewer(@Nonnull UUID playerUuid, @Nonnull TriggerVolumeManager.ViewSource source) {
      this.activeViewers.computeIfPresent(playerUuid, (k, sources) -> {
         sources.remove(source);
         return sources.isEmpty() ? null : sources;
      });
   }

   public void setPlayerSelection(@Nonnull UUID playerUuid, @Nullable String volumeId) {
      if (volumeId == null) {
         this.playerSelections.remove(playerUuid);
      } else {
         this.playerSelections.put(playerUuid, volumeId);
      }

      TriggerVolumeManager.SelectionObserver observer = this.selectionObservers.get(playerUuid);
      if (observer != null) {
         observer.onSelectionChanged(volumeId);
      }
   }

   @Nullable
   public String getPlayerSelection(@Nonnull UUID playerUuid) {
      return this.playerSelections.get(playerUuid);
   }

   public void setSelectionObserver(@Nonnull UUID playerUuid, @Nonnull TriggerVolumeManager.SelectionObserver observer) {
      this.selectionObservers.put(playerUuid, observer);
   }

   public void clearSelectionObserver(@Nonnull UUID playerUuid, @Nonnull TriggerVolumeManager.SelectionObserver observer) {
      this.selectionObservers.remove(playerUuid, observer);
   }

   public void setVolumeUpdateObserver(@Nonnull UUID playerUuid, @Nonnull TriggerVolumeManager.VolumeUpdateObserver observer) {
      this.volumeUpdateObservers.put(playerUuid, observer);
   }

   public void clearVolumeUpdateObserver(@Nonnull UUID playerUuid, @Nonnull TriggerVolumeManager.VolumeUpdateObserver observer) {
      this.volumeUpdateObservers.remove(playerUuid, observer);
   }

   public void notifyViewers() {
      if (!this.activeViewers.isEmpty()) {
         TriggerVolumeDisplayEntry[] entries = this.buildDisplayEntries();
         UpdateTriggerVolumeDisplay packet = new UpdateTriggerVolumeDisplay(entries);

         for (UUID uuid : this.activeViewers.keySet()) {
            PlayerRef playerRef = Universe.get().getPlayer(uuid);
            if (playerRef != null) {
               playerRef.getPacketHandler().write(packet);
            }
         }
      }
   }

   public int getViewerCount() {
      return this.activeViewers.size();
   }

   @Nonnull
   public Set<UUID> getViewerUuids() {
      return new HashSet<>(this.activeViewers.keySet());
   }

   public void clearViewers() {
      this.activeViewers.clear();
      this.playerSelections.clear();
   }

   public void notifyViewersAdd(@Nonnull VolumeEntry vol) {
      if (!this.activeViewers.isEmpty()) {
         AddOrUpdateTriggerVolumeDisplay packet = new AddOrUpdateTriggerVolumeDisplay(vol.getId(), this.buildDisplayEntry(vol));

         for (UUID uuid : this.activeViewers.keySet()) {
            PlayerRef playerRef = Universe.get().getPlayer(uuid);
            if (playerRef != null) {
               playerRef.getPacketHandler().write(packet);
            }
         }
      }

      for (TriggerVolumeManager.VolumeUpdateObserver observer : this.volumeUpdateObservers.values()) {
         observer.onVolumeUpdated(vol);
      }
   }

   public void notifyViewersRemove(@Nonnull String volumeId) {
      if (!this.activeViewers.isEmpty()) {
         RemoveTriggerVolumeDisplay packet = new RemoveTriggerVolumeDisplay(volumeId);

         for (UUID uuid : this.activeViewers.keySet()) {
            PlayerRef playerRef = Universe.get().getPlayer(uuid);
            if (playerRef != null) {
               playerRef.getPacketHandler().write(packet);
            }
         }
      }

      for (TriggerVolumeManager.VolumeUpdateObserver observer : this.volumeUpdateObservers.values()) {
         observer.onVolumeRemoved(volumeId);
      }
   }

   public void notifyViewersLegacyVolumeSkipped() {
      if (!this.activeViewers.isEmpty()) {
         Message message = Message.translation("server.triggervolumes.legacyVolumeSkipped");

         for (UUID uuid : this.activeViewers.keySet()) {
            PlayerRef playerRef = Universe.get().getPlayer(uuid);
            if (playerRef != null) {
               playerRef.sendMessage(message);
            }
         }
      }
   }

   public void sendVolumeDisplay(@Nonnull PlayerRef playerRef) {
      playerRef.getPacketHandler().write(new UpdateTriggerVolumeDisplay(this.buildDisplayEntries()));
   }

   @Nonnull
   private TriggerVolumeDisplayEntry[] buildDisplayEntries() {
      Collection<VolumeEntry> volumeList = this.volumes.values();
      TriggerVolumeDisplayEntry[] entries = new TriggerVolumeDisplayEntry[volumeList.size()];
      int i = 0;

      for (VolumeEntry vol : volumeList) {
         entries[i++] = this.buildDisplayEntry(vol);
      }

      return entries;
   }

   @Nonnull
   public TriggerVolumeDisplayEntry buildDisplayEntry(@Nonnull VolumeEntry vol) {
      TriggerVolumeDisplayEntry entry = new TriggerVolumeDisplayEntry();
      Vector3d pos = vol.getPosition();
      TriggerVolumeShape shape = vol.getShape();
      if (shape instanceof BoxShape box) {
         Vector3d min = box.getMin();
         Vector3d max = box.getMax();
         double cx = pos.x() + (min.x() + max.x()) * 0.5;
         double cy = pos.y() + (min.y() + max.y()) * 0.5;
         double cz = pos.z() + (min.z() + max.z()) * 0.5;
         double hx = (max.x() - min.x()) * 0.5;
         double hy = (max.y() - min.y()) * 0.5;
         double hz = (max.z() - min.z()) * 0.5;
         entry.shapeType = TriggerVolumeShapeType.Box;
         entry.position = new Vector3f((float)cx, (float)cy, (float)cz);
         entry.dimensions = new Vector3f((float)hx, (float)hy, (float)hz);
      } else if (shape instanceof SphereShape sphere) {
         Vector3d c = sphere.getCenter();
         entry.shapeType = TriggerVolumeShapeType.Sphere;
         entry.position = new Vector3f((float)(pos.x() + c.x()), (float)(pos.y() + c.y()), (float)(pos.z() + c.z()));
         entry.dimensions = new Vector3f((float)sphere.getRadius(), 0.0F, 0.0F);
      } else if (shape instanceof CylinderShape cyl) {
         Vector3d c = cyl.getCenter();
         double halfH = cyl.getHeight() * 0.5;
         entry.shapeType = TriggerVolumeShapeType.Cylinder;
         entry.position = new Vector3f((float)(pos.x() + c.x()), (float)(pos.y() + c.y() + halfH), (float)(pos.z() + c.z()));
         entry.dimensions = new Vector3f((float)cyl.getRadius(), (float)cyl.getHeight(), 0.0F);
      } else {
         double r = shape.getBoundingRadius();
         entry.shapeType = TriggerVolumeShapeType.Sphere;
         entry.position = new Vector3f((float)pos.x(), (float)pos.y(), (float)pos.z());
         entry.dimensions = new Vector3f((float)r, 0.0F, 0.0F);
      }

      entry.color = vol.getColor() != null ? vol.getColor() : (vol.isEnabled() ? COLOR_ENABLED : COLOR_DISABLED);
      entry.opacity = vol.isEnabled() ? 0.3F : 0.15F;
      entry.name = vol.getId();
      entry.effectAssetRef = vol.getEffectAssetRef();
      byte targetBits = 0;

      for (EntityTargetType tt : vol.getTargetTypes()) {
         if (tt == EntityTargetType.PLAYER) {
            targetBits = (byte)(targetBits | 1);
         } else if (tt == EntityTargetType.NPC) {
            targetBits = (byte)(targetBits | 2);
         } else if (tt == EntityTargetType.ITEM_DROP) {
            targetBits = (byte)(targetBits | 4);
         } else if (tt == EntityTargetType.PROJECTILE) {
            targetBits = (byte)(targetBits | 8);
         }
      }

      entry.targetTypes = targetBits;
      entry.keepLoaded = vol.isKeepLoaded();
      entry.cancelDelayedOnExit = vol.isCancelDelayedEffectsOnExit();
      entry.cooldown = vol.getCooldown();
      entry.cooldownMode = (byte)vol.getCooldownMode().ordinal();
      entry.activationDelay = vol.getActivationDelay();
      entry.conditionTiming = vol.getConditionTiming() == ConditionTiming.BEFORE_VOLUME_DELAY
         ? TriggerVolumeConditionTiming.BeforeVolumeDelay
         : TriggerVolumeConditionTiming.AfterVolumeDelay;
      if (vol.getGroupId() != null) {
         entry.groupId = vol.getGroupId();
         GroupEntry group = this.groups.get(vol.getGroupId());
         if (group != null) {
            entry.groupColor = group.getColor();
            float r = (group.getColor() >> 16 & 0xFF) / 255.0F;
            float g = (group.getColor() >> 8 & 0xFF) / 255.0F;
            float b = (group.getColor() & 0xFF) / 255.0F;
            entry.color = new Vector3f(r, g, b);
         }
      }

      return entry;
   }

   @Nonnull
   @Override
   public Resource<EntityStore> clone() {
      throw new UnsupportedOperationException("TriggerVolumeManager cannot be cloned");
   }

   public record PendingTriggerEvent(
      @Nonnull TriggerEventType eventType,
      @Nonnull Ref<EntityStore> actorRef,
      @Nonnull UUID actorUuid,
      @Nullable String volumeId,
      @Nullable Vector3d blockPosition,
      @Nullable String blockId,
      @Nullable String tagKey,
      @Nullable String tagValue
   ) {
   }

   public interface SelectionObserver {
      void onSelectionChanged(@Nullable String var1);
   }

   public static enum ViewSource {
      COMMAND,
      TOOL,
      SELECTION_TOOL;
   }

   public interface VolumeUpdateObserver {
      void onVolumeUpdated(@Nonnull VolumeEntry var1);

      void onVolumeRemoved(@Nonnull String var1);
   }
}
