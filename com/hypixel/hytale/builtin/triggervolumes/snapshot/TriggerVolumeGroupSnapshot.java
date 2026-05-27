package com.hypixel.hytale.builtin.triggervolumes.snapshot;

import com.hypixel.hytale.builtin.buildertools.snapshot.SelectionSnapshot;
import com.hypixel.hytale.builtin.triggervolumes.EntityTargetType;
import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.manager.ConditionTiming;
import com.hypixel.hytale.builtin.triggervolumes.manager.GroupEntry;
import com.hypixel.hytale.builtin.triggervolumes.manager.RejectionDelayMode;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class TriggerVolumeGroupSnapshot implements SelectionSnapshot<TriggerVolumeGroupSnapshot> {
   @Nonnull
   private final TriggerVolumeGroupSnapshot.SnapshotType type;
   @Nonnull
   private final String groupId;
   @Nonnull
   private final String worldName;
   @Nonnull
   private final Vector3d origin;
   @Nonnull
   private final List<TriggerCondition> conditions;
   @Nonnull
   private final List<TriggerEffect> effects;
   @Nonnull
   private final List<TriggerEffect> rejectionEffects;
   @Nonnull
   private final ConditionTiming conditionTiming;
   @Nonnull
   private final RejectionDelayMode rejectionDelayMode;
   @Nonnull
   private final Set<EntityTargetType> targetTypes;
   private final boolean enabled;
   private final int color;
   @Nonnull
   private final Map<String, String> rawTags;
   @Nonnull
   private final List<String> memberVolumeIds;

   private TriggerVolumeGroupSnapshot(@Nonnull TriggerVolumeGroupSnapshot.SnapshotType type, @Nonnull GroupEntry group) {
      this.type = type;
      this.groupId = group.getId();
      this.worldName = group.getWorldName();
      this.origin = new Vector3d(group.getOrigin());
      this.conditions = TriggerCondition.deepCopyList(group.getConditions());
      this.effects = TriggerEffect.deepCopyList(group.getEffects());
      this.rejectionEffects = TriggerEffect.deepCopyList(group.getRejectionEffects());
      this.conditionTiming = group.getConditionTiming();
      this.rejectionDelayMode = group.getRejectionDelayMode();
      this.targetTypes = EnumSet.copyOf(group.getTargetTypes());
      this.enabled = group.isEnabled();
      this.color = group.getColor();
      this.rawTags = deepCopyTags(group.getRawTags());
      this.memberVolumeIds = List.copyOf(group.getMemberVolumeIds());
   }

   @Nonnull
   public static TriggerVolumeGroupSnapshot ofCreate(@Nonnull GroupEntry group) {
      return new TriggerVolumeGroupSnapshot(TriggerVolumeGroupSnapshot.SnapshotType.CREATE, group);
   }

   @Nonnull
   public static TriggerVolumeGroupSnapshot ofDelete(@Nonnull GroupEntry group) {
      return new TriggerVolumeGroupSnapshot(TriggerVolumeGroupSnapshot.SnapshotType.DELETE, group);
   }

   public TriggerVolumeGroupSnapshot restore(@Nonnull Ref<EntityStore> ref, PlayerRef playerRef, World world, ComponentAccessor<EntityStore> componentAccessor) {
      TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
      TriggerVolumeManager manager = world.getEntityStore().getStore().getResource(plugin.getManagerResourceType());
      if (manager == null) {
         return null;
      } else {
         return switch (this.type) {
            case CREATE -> {
               GroupEntry group = manager.getGroup(this.groupId);
               if (group == null) {
                  yield null;
               } else {
                  TriggerVolumeGroupSnapshot inverse = ofDelete(group);
                  manager.unregisterGroup(this.groupId);
                  manager.notifyViewers();
                  yield inverse;
               }
            }
            case DELETE -> {
               GroupEntry group = new GroupEntry(
                  this.groupId,
                  this.worldName,
                  new Vector3d(this.origin),
                  TriggerEffect.deepCopyList(this.effects),
                  EnumSet.copyOf(this.targetTypes),
                  this.enabled,
                  this.color
               );
               group.getConditions().addAll(TriggerCondition.deepCopyList(this.conditions));
               group.getRejectionEffects().addAll(TriggerEffect.deepCopyList(this.rejectionEffects));
               group.setConditionTiming(this.conditionTiming);
               group.setRejectionDelayMode(this.rejectionDelayMode);
               group.setTags(deepCopyTags(this.rawTags));

               for (String volumeId : this.memberVolumeIds) {
                  group.addMember(volumeId);
               }

               manager.registerGroup(this.groupId, group);
               manager.notifyViewers();
               yield ofCreate(group);
            }
         };
      }
   }

   @Nonnull
   private static Map<String, String> deepCopyTags(@Nonnull Map<String, String> tags) {
      if (tags.isEmpty()) {
         return Collections.emptyMap();
      } else {
         HashMap<String, String> copy = new HashMap<>(tags.size());

         for (Entry<String, String> entry : tags.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
         }

         return copy;
      }
   }

   private static enum SnapshotType {
      CREATE,
      DELETE;
   }
}
