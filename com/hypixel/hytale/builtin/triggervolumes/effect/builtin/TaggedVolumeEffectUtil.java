package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerVolumeCodecs;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public final class TaggedVolumeEffectUtil {
   @Nonnull
   public static final String EMPTY_TAG_VALUE = "Empty";

   private TaggedVolumeEffectUtil() {
   }

   @Nonnull
   public static List<VolumeEntry> collectTargets(
      @Nonnull TriggerContext context, @Nullable String matchTag, double radius, @Nonnull TaggedVolumeEffectUtil.Center center
   ) {
      TriggerVolumeManager manager = context.getStore().getResource(TriggerVolumesPlugin.get().getManagerResourceType());
      if (manager == null) {
         return List.of();
      } else {
         List<VolumeEntry> candidates;
         if (matchTag != null && !matchTag.isBlank()) {
            int tagIndex = AssetRegistry.getTagIndex(matchTag);
            if (tagIndex == Integer.MIN_VALUE) {
               return List.of();
            }

            candidates = manager.getVolumesByTag(tagIndex);
         } else {
            candidates = List.of(context.getVolume());
         }

         if (radius <= 0.0) {
            return candidates;
         } else {
            Vector3d origin = resolveCenter(context, center);
            double radiusSquared = radius * radius;
            ArrayList<VolumeEntry> result = new ArrayList<>();

            for (VolumeEntry volume : candidates) {
               if (volume.getPosition().distanceSquared(origin) <= radiusSquared) {
                  result.add(volume);
               }
            }

            return result;
         }
      }
   }

   @Nullable
   static TriggerVolumeManager manager(@Nonnull TriggerContext context) {
      return context.getStore().getResource(TriggerVolumesPlugin.get().getManagerResourceType());
   }

   @Nonnull
   public static String normalizeTagValue(@Nullable String value) {
      return TriggerVolumeCodecs.TaggedValue.normalize(value);
   }

   @Nullable
   public static String blankToNull(@Nullable String value) {
      return value != null && !value.isBlank() ? value.trim() : null;
   }

   @Nullable
   public static String composeTagFilter(@Nullable String matchKey, @Nullable String matchValue) {
      if (matchKey == null || matchKey.isBlank()) {
         return null;
      } else {
         return matchValue != null && !matchValue.isBlank() ? matchKey.trim() + "=" + matchValue.trim() : matchKey.trim();
      }
   }

   @Nonnull
   private static Vector3d resolveCenter(@Nonnull TriggerContext context, @Nonnull TaggedVolumeEffectUtil.Center center) {
      if (center == TaggedVolumeEffectUtil.Center.ENTITY) {
         TransformComponent transform = context.getStore().getComponent(context.getEntityRef(), TransformComponent.getComponentType());
         if (transform != null) {
            return new Vector3d(transform.getPosition());
         }
      }

      return new Vector3d(context.getVolume().getPosition());
   }

   public static enum Center {
      VOLUME,
      ENTITY;
   }
}
