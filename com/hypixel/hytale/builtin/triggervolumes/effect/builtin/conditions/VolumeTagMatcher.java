package com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions;

import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class VolumeTagMatcher {
   private VolumeTagMatcher() {
   }

   static boolean hasTag(@Nonnull VolumeEntry volume, @Nonnull String tagKey, @Nullable String matchValue) {
      String value = volume.getRawTags().get(tagKey);
      if (value == null) {
         return false;
      } else {
         return matchValue != null && !matchValue.isBlank() ? value.equals(matchValue) : true;
      }
   }
}
