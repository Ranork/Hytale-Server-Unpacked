package com.hypixel.hytale.server.worldgen.climate;

import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.server.worldgen.util.LogUtil;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UniqueClimateGenerator {
   public static final UniqueClimateGenerator EMPTY = new UniqueClimateGenerator(
      UniqueClimateGenerator.Entry.EMPTY_ARRAY, UniqueClimateGenerator.Unique.EMPTY_ARRAY
   );
   private static final int[] EMPTY_PARENTS = new int[0];
   private static final int MAX_PARENT_DEPTH = 10;
   private static final Vector2i DEFAULT_ORIGIN = new Vector2i(0, 0);
   private static final Vector2i[] EMPTY_POSITIONS = new Vector2i[0];
   protected final UniqueClimateGenerator.Entry[] entries;
   protected final UniqueClimateGenerator.Unique[] zones;

   public UniqueClimateGenerator(@Nonnull UniqueClimateGenerator.Entry[] entries) {
      this(entries, UniqueClimateGenerator.Unique.EMPTY_ARRAY);
   }

   public UniqueClimateGenerator(@Nonnull UniqueClimateGenerator.Entry[] entries, @Nonnull UniqueClimateGenerator.Unique[] zones) {
      this.entries = entries;
      this.zones = zones;
   }

   public UniqueClimateGenerator.Entry[] entries() {
      return this.entries;
   }

   public UniqueClimateGenerator.Unique[] zones() {
      return this.zones;
   }

   public int generate(int x, int y) {
      for (int i = 0; i < this.zones.length; i++) {
         if (this.zones[i].contains(x, y)) {
            return this.zones[i].color;
         }
      }

      return -1;
   }

   public Zone.UniqueCandidate[] getCandidates(Map<String, Zone> zoneLookup) {
      if (this.entries.length == 0) {
         return Zone.UniqueCandidate.EMPTY_ARRAY;
      } else {
         Zone.UniqueCandidate[] candidates = new Zone.UniqueCandidate[this.entries.length];

         for (int i = 0; i < this.entries.length; i++) {
            UniqueClimateGenerator.Entry entry = this.entries[i];
            Zone zone = zoneLookup.get(entry.zone);
            if (zone == null) {
               throw new Error("Could not find zone: " + entry.zone);
            }

            candidates[i] = new Zone.UniqueCandidate(new Zone.UniqueEntry(zone, entry.color, EMPTY_PARENTS, entry.radius, 0), EMPTY_POSITIONS);
         }

         return candidates;
      }
   }

   public UniqueClimateGenerator apply(
      int seed, @Nonnull Zone.UniqueCandidate[] candidates, @Nonnull ClimateNoise noise, @Nonnull ClimateGraph graph, @Nonnull List<Zone.Unique> collector
   ) {
      if (candidates.length != this.entries.length) {
         LogUtil.getLogger()
            .at(Level.WARNING)
            .log("Mismatched unique climate generator candidates: expected %d, got %d", this.entries.length, candidates.length);
         return this;
      } else {
         UniqueClimateGenerator.Unique[] unique = new UniqueClimateGenerator.Unique[candidates.length];
         Object2ObjectOpenHashMap<String, UniqueClimateGenerator.Unique> lookup = new Object2ObjectOpenHashMap();

         for (int it = 0; it < 10 && lookup.size() < unique.length; it++) {
            for (int i = 0; i < this.entries.length; i++) {
               if (unique[i] == null) {
                  UniqueClimateGenerator.Entry entry = this.entries[i];
                  UniqueClimateGenerator.Unique parent = (UniqueClimateGenerator.Unique)lookup.get(entry.parent);
                  if (entry.parent.isEmpty() || parent != null) {
                     CompletableFuture<Vector2i> position = findZonePosition(seed, DEFAULT_ORIGIN, entry, parent, noise, graph);
                     unique[i] = new UniqueClimateGenerator.Unique(entry.color, entry.radius, position);
                     collector.add(new Zone.Unique(candidates[i].zone().zone(), position));
                     lookup.put(entry.zone, unique[i]);
                  }
               }
            }
         }

         return new UniqueClimateGenerator(this.entries, unique);
      }
   }

   public UniqueClimateGenerator apply(int seed, @Nonnull ClimateNoise noise, @Nonnull ClimateGraph graph) {
      UniqueClimateGenerator.Unique[] unique = new UniqueClimateGenerator.Unique[this.entries.length];
      Object2ObjectOpenHashMap<String, UniqueClimateGenerator.Unique> lookup = new Object2ObjectOpenHashMap();

      for (int it = 0; it < 10 && lookup.size() < unique.length; it++) {
         for (int i = 0; i < this.entries.length; i++) {
            if (unique[i] == null) {
               UniqueClimateGenerator.Entry entry = this.entries[i];
               UniqueClimateGenerator.Unique parent = (UniqueClimateGenerator.Unique)lookup.get(entry.parent);
               if (entry.parent.isEmpty() || parent != null) {
                  CompletableFuture<Vector2i> position = findZonePosition(seed, DEFAULT_ORIGIN, entry, parent, noise, graph);
                  unique[i] = new UniqueClimateGenerator.Unique(entry.color, entry.radius, position);
                  lookup.put(entry.zone, unique[i]);
               }
            }
         }
      }

      if (lookup.size() < unique.length) {
         LogUtil.getLogger().at(Level.WARNING).log("Could not resolve all unique climate zones, resolved %d out of %d", lookup.size(), unique.length);
         UniqueClimateGenerator.Entry[] newEntries = new UniqueClimateGenerator.Entry[lookup.size()];
         UniqueClimateGenerator.Unique[] newUnique = new UniqueClimateGenerator.Unique[lookup.size()];
         int index = 0;

         for (int ix = 0; ix < unique.length; ix++) {
            if (unique[ix] != null) {
               newEntries[index] = this.entries[ix];
               newUnique[index] = unique[ix];
               index++;
            }
         }

         return new UniqueClimateGenerator(newEntries, newUnique);
      } else {
         return new UniqueClimateGenerator(this.entries, unique);
      }
   }

   protected static CompletableFuture<Vector2i> findZonePosition(
      int seed,
      Vector2i origin,
      @Nonnull UniqueClimateGenerator.Entry entry,
      @Nullable UniqueClimateGenerator.Unique parent,
      @Nonnull ClimateNoise noise,
      @Nonnull ClimateGraph graph
   ) {
      return parent != null
         ? parent.position.thenCompose(pos -> findZonePosition(seed, pos, entry, null, noise, graph))
         : ClimateSearch.search(seed, origin.x + entry.origin.x, origin.y + entry.origin.y, entry.minDistance, entry.maxDistance, entry.rule, noise, graph)
            .thenApply(result -> {
               LogUtil.getLogger().at(Level.INFO).log("Found location for unique zone '%s' -> %s", entry.zone, result.pretty());
               return result.position();
            });
   }

   public record Entry(
      @Nonnull String zone,
      @Nonnull String parent,
      int color,
      int radius,
      @Nonnull Vector2i origin,
      int minDistance,
      int maxDistance,
      @Nonnull ClimateSearch.Rule rule
   ) {
      public static final UniqueClimateGenerator.Entry[] EMPTY_ARRAY = new UniqueClimateGenerator.Entry[0];
      public static final String DEFAULT_PARENT = "";
   }

   public record Unique(int color, int radius, int radius2, @Nonnull CompletableFuture<Vector2i> position) {
      public static final UniqueClimateGenerator.Unique[] EMPTY_ARRAY = new UniqueClimateGenerator.Unique[0];

      public Unique(int color, int radius, @Nonnull CompletableFuture<Vector2i> position) {
         this(color, radius, radius * radius, position);
      }

      public boolean contains(int x, int y) {
         Vector2i pos = this.position.join();
         int dx = x - pos.x;
         int dy = y - pos.y;
         return dx >= -this.radius && dy >= -this.radius && dx <= this.radius && dy <= this.radius && dx * dx + dy * dy <= this.radius2;
      }
   }
}
