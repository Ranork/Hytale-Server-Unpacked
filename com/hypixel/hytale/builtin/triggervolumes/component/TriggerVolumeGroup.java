package com.hypixel.hytale.builtin.triggervolumes.component;

import com.hypixel.hytale.builtin.triggervolumes.EntityTargetType;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerVolumeCodecs;
import com.hypixel.hytale.builtin.triggervolumes.manager.GroupEntry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class TriggerVolumeGroup implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<TriggerVolumeGroup> CODEC = BuilderCodec.builder(TriggerVolumeGroup.class, TriggerVolumeGroup::new)
      .append(new KeyedCodec<>("GroupLinkId", Codec.STRING), (c, v) -> c.groupLinkId = v, c -> c.groupLinkId)
      .add()
      .append(
         new KeyedCodec<>("Effects", new ArrayCodec<>(TriggerEffect.CODEC, TriggerEffect[]::new), false),
         (c, v) -> c.effects = v != null ? List.of(v) : null,
         c -> c.effects != null ? c.effects.toArray(TriggerEffect[]::new) : null
      )
      .add()
      .append(new KeyedCodec<>("TargetTypes", new ArrayCodec<>(new EnumCodec<>(EntityTargetType.class), EntityTargetType[]::new), false), (c, arr) -> {
         c.targetTypes.clear();
         Collections.addAll(c.targetTypes, arr);
      }, c -> c.targetTypes.isEmpty() ? null : c.targetTypes.toArray(EntityTargetType[]::new))
      .add()
      .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN, false), (c, v) -> c.enabled = v, c -> c.enabled)
      .add()
      .append(new KeyedCodec<>("Color", Codec.INTEGER, false), (c, v) -> c.color = v, c -> c.color)
      .add()
      .append(new KeyedCodec<>("Tags", TriggerVolumeCodecs.TAGS, false), (c, v) -> c.rawTags = v, c -> c.rawTags.isEmpty() ? null : c.rawTags)
      .add()
      .build();
   @Nullable
   private String groupLinkId;
   @Nullable
   private List<TriggerEffect> effects;
   @Nonnull
   private Set<EntityTargetType> targetTypes = EnumSet.of(EntityTargetType.PLAYER);
   private boolean enabled = true;
   private int color;
   @Nonnull
   private Map<String, String> rawTags = Collections.emptyMap();

   @Nonnull
   public static TriggerVolumeGroup fromGroupEntry(@Nonnull String groupLinkId, @Nonnull GroupEntry entry) {
      TriggerVolumeGroup group = new TriggerVolumeGroup();
      group.groupLinkId = groupLinkId;
      group.effects = entry.getEffects().isEmpty() ? null : TriggerEffect.deepCopyList(entry.getEffects());
      group.targetTypes = EnumSet.copyOf(entry.getTargetTypes());
      group.enabled = entry.isEnabled();
      group.color = entry.getColor();
      group.rawTags = copyTags(entry.getRawTags());
      return group;
   }

   @Nullable
   public String getGroupLinkId() {
      return this.groupLinkId;
   }

   @Nonnull
   public GroupEntry toGroupEntry(@Nonnull String id, @Nonnull String worldName, @Nonnull Vector3d origin) {
      GroupEntry entry = new GroupEntry(
         id,
         worldName,
         origin,
         (List<TriggerEffect>)(this.effects != null ? TriggerEffect.deepCopyList(this.effects) : new ArrayList<>()),
         EnumSet.copyOf(this.targetTypes),
         this.enabled,
         this.color
      );
      if (!this.rawTags.isEmpty()) {
         entry.setTags(copyTags(this.rawTags));
      }

      return entry;
   }

   public void applyTo(@Nonnull GroupEntry entry, @Nonnull Vector3d origin) {
      entry.setOrigin(origin);
      entry.getEffects().clear();
      if (this.effects != null) {
         entry.getEffects().addAll(TriggerEffect.deepCopyList(this.effects));
      }

      entry.getTargetTypes().clear();
      entry.getTargetTypes().addAll(this.targetTypes);
      entry.setEnabled(this.enabled);
      entry.setColor(this.color);
      entry.setTags(copyTags(this.rawTags));
   }

   @Nonnull
   private static Map<String, String> copyTags(@Nonnull Map<String, String> tags) {
      if (tags.isEmpty()) {
         return Collections.emptyMap();
      } else {
         HashMap<String, String> copy = new HashMap<>();

         for (Entry<String, String> entry : tags.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
         }

         return copy;
      }
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      TriggerVolumeGroup clone = new TriggerVolumeGroup();
      clone.groupLinkId = this.groupLinkId;
      clone.effects = this.effects != null ? TriggerEffect.deepCopyList(this.effects) : null;
      clone.targetTypes = EnumSet.copyOf(this.targetTypes);
      clone.enabled = this.enabled;
      clone.color = this.color;
      clone.rawTags = copyTags(this.rawTags);
      return clone;
   }
}
