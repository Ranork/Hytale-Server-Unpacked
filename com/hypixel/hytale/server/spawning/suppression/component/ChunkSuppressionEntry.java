package com.hypixel.hytale.server.spawning.suppression.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChunkSuppressionEntry implements Component<ChunkStore> {
   @Nonnull
   private final List<ChunkSuppressionEntry.SuppressionSpan> suppressionSpans;

   public static ComponentType<ChunkStore, ChunkSuppressionEntry> getComponentType() {
      return SpawningPlugin.get().getChunkSuppressionEntryComponentType();
   }

   public ChunkSuppressionEntry(@Nonnull List<ChunkSuppressionEntry.SuppressionSpan> suppressionSpans) {
      this.suppressionSpans = Collections.unmodifiableList(suppressionSpans);
   }

   @Nonnull
   public List<ChunkSuppressionEntry.SuppressionSpan> getSuppressionSpans() {
      return this.suppressionSpans;
   }

   public boolean containsOnly(UUID suppressorId) {
      return this.suppressionSpans.size() == 1 && this.suppressionSpans.getFirst().getSuppressorId().equals(suppressorId);
   }

   public boolean isSuppressingRoleAt(int roleIndex, int yPosition) {
      for (int i = 0; i < this.suppressionSpans.size(); i++) {
         ChunkSuppressionEntry.SuppressionSpan span = this.suppressionSpans.get(i);
         if (span.minY > yPosition) {
            return false;
         }

         if (span.maxY >= yPosition && span.includesRole(roleIndex)) {
            return true;
         }
      }

      return false;
   }

   @Nonnull
   @Override
   public Component<ChunkStore> clone() {
      return new ChunkSuppressionEntry(this.suppressionSpans);
   }

   public static class SuppressionSpan {
      private final UUID suppressorId;
      private final int minY;
      private final int maxY;
      @Nullable
      private final IntSet suppressedRoles;

      public SuppressionSpan(UUID suppressorId, int minY, int maxY, @Nullable IntSet suppressedRoles) {
         this.suppressorId = suppressorId;
         this.minY = minY;
         this.maxY = maxY;
         this.suppressedRoles = suppressedRoles != null ? IntSets.unmodifiable(suppressedRoles) : null;
      }

      public UUID getSuppressorId() {
         return this.suppressorId;
      }

      @Nullable
      public IntSet getSuppressedRoles() {
         return this.suppressedRoles;
      }

      public int getMinY() {
         return this.minY;
      }

      public int getMaxY() {
         return this.maxY;
      }

      public boolean includesRole(int roleIndex) {
         return this.suppressedRoles == null || this.suppressedRoles.contains(roleIndex);
      }
   }
}
