package com.hypixel.hytale.server.core.asset.type.item.config.damageData;

import com.hypixel.hytale.protocol.DamageData;
import com.hypixel.hytale.protocol.DamageEntry;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record DamageBreakdown(@Nonnull List<DamageBreakdown.Entry> entries) implements NetworkSerializable<DamageData> {
   public static final DamageBreakdown EMPTY = new DamageBreakdown(List.of());

   public DamageBreakdown(@Nonnull List<DamageBreakdown.Entry> entries) {
      entries = List.copyOf(entries);
      this.entries = entries;
   }

   @Nonnull
   public DamageData toPacket() {
      DamageEntry[] array = new DamageEntry[this.entries.size()];

      for (int i = 0; i < this.entries.size(); i++) {
         array[i] = this.entries.get(i).toPacket();
      }

      return new DamageData(array);
   }

   public record Entry(@Nullable String labelKey, float min, float max) {
      @Nonnull
      public DamageEntry toPacket() {
         return new DamageEntry(this.labelKey, this.min, this.max);
      }
   }
}
