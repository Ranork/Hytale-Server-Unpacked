package com.hypixel.hytale.server.spawning.suppression;

import com.hypixel.hytale.server.spawning.suppression.component.ChunkSuppressionEntry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayDeque;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SuppressionSpanHelper {
   private static final ThreadLocal<ArrayDeque<SuppressionSpanHelper.Span>> SPAN_POOL = ThreadLocal.withInitial(ArrayDeque::new);
   private final List<SuppressionSpanHelper.Span> optimisedSuppressionSpans = new ObjectArrayList();
   private int currentSpanIndex = 0;

   public void optimiseSuppressedSpans(int roleIndex, @Nullable ChunkSuppressionEntry entry) {
      if (entry != null) {
         ArrayDeque<SuppressionSpanHelper.Span> spanPool = SPAN_POOL.get();
         List<ChunkSuppressionEntry.SuppressionSpan> suppressionSpans = entry.getSuppressionSpans();
         SuppressionSpanHelper.Span initialSpan = allocateSpan(spanPool);
         initialSpan.init(0, 0);
         this.optimisedSuppressionSpans.add(initialSpan);
         boolean matchedRole = false;

         for (ChunkSuppressionEntry.SuppressionSpan suppressionSpan : suppressionSpans) {
            if (suppressionSpan.includesRole(roleIndex)) {
               matchedRole = true;
               int minY = suppressionSpan.getMinY();
               int maxY = suppressionSpan.getMaxY();
               SuppressionSpanHelper.Span latestSpan = this.optimisedSuppressionSpans.getLast();
               if (latestSpan.includes(minY)) {
                  if (!latestSpan.includes(maxY)) {
                     latestSpan.expandTo(maxY);
                  }
               } else {
                  SuppressionSpanHelper.Span span = allocateSpan(spanPool);
                  span.init(minY, maxY);
                  this.optimisedSuppressionSpans.add(span);
               }
            }
         }

         if (!matchedRole) {
            SuppressionSpanHelper.Span span = this.optimisedSuppressionSpans.removeFirst();
            span.reset();
            spanPool.push(span);
         }
      }
   }

   public int adjustSpawnRangeMin(int min) {
      if (this.optimisedSuppressionSpans.isEmpty()) {
         return min;
      } else {
         int maxSpanIndex = this.optimisedSuppressionSpans.size() - 1;

         SuppressionSpanHelper.Span currentSpan;
         for (currentSpan = this.optimisedSuppressionSpans.get(this.currentSpanIndex);
            min >= currentSpan.max && this.currentSpanIndex < maxSpanIndex;
            currentSpan = this.optimisedSuppressionSpans.get(this.currentSpanIndex)
         ) {
            this.currentSpanIndex++;
         }

         if (currentSpan.includes(min)) {
            if (this.currentSpanIndex < maxSpanIndex) {
               this.currentSpanIndex++;
            }

            return currentSpan.max;
         } else {
            return min;
         }
      }
   }

   public int adjustSpawnRangeMax(int min, int max) {
      if (this.optimisedSuppressionSpans.isEmpty()) {
         return max;
      } else {
         SuppressionSpanHelper.Span currentSpan = this.optimisedSuppressionSpans.get(this.currentSpanIndex);
         if (max < currentSpan.min) {
            return max;
         } else if (currentSpan.includes(max)) {
            return currentSpan.min;
         } else {
            return min < currentSpan.min && max >= currentSpan.max ? currentSpan.min : max;
         }
      }
   }

   public void reset() {
      ArrayDeque<SuppressionSpanHelper.Span> spanPool = SPAN_POOL.get();

      for (int i = this.optimisedSuppressionSpans.size() - 1; i >= 0; i--) {
         SuppressionSpanHelper.Span span = this.optimisedSuppressionSpans.remove(i);
         span.reset();
         spanPool.push(span);
      }

      this.currentSpanIndex = 0;
   }

   @Nonnull
   private static SuppressionSpanHelper.Span allocateSpan(@Nonnull ArrayDeque<SuppressionSpanHelper.Span> spanPool) {
      return spanPool.isEmpty() ? new SuppressionSpanHelper.Span() : spanPool.pop();
   }

   private static class Span {
      private int min = -1;
      private int max = -1;

      public void init(int min, int max) {
         this.min = min;
         this.max = max;
      }

      public void expandTo(int max) {
         this.max = max;
      }

      public boolean includes(int value) {
         return value >= this.min && value <= this.max;
      }

      public void reset() {
         this.min = this.max = -1;
      }
   }
}
