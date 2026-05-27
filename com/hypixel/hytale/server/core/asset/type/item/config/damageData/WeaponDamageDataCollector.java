package com.hypixel.hytale.server.core.asset.type.item.config.damageData;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChargingInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.CollectorTag;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.StringTag;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.DamageEntityInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.DamageCalculator;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.modules.projectile.interaction.ProjectileInteraction;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class WeaponDamageDataCollector implements Collector {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final String LABEL_CHARGED = "charged";
   private static final String LABEL_BACKSTAB = "backstab";
   private final Deque<WeaponDamageDataCollector.Frame> stack = new ArrayDeque<>();
   private final List<WeaponDamageDataCollector.Leaf> leaves = new ArrayList<>();
   private final float[] damageScratch = new float[2];
   @Nullable
   private CollectorTag pendingTag;

   @Nonnull
   public static DamageBreakdown calculate(@Nonnull Item item, @Nonnull InteractionType type) {
      String rootId = item.getInteractions().get(type);
      if (rootId == null) {
         return DamageBreakdown.EMPTY;
      } else {
         RootInteraction root = RootInteraction.getAssetMap().getAsset(rootId);
         if (root == null) {
            return DamageBreakdown.EMPTY;
         } else {
            try {
               InteractionContext context = InteractionContext.withoutEntity();
               context.setInteractionVarsGetter(c -> item.getInteractionVars());
               WeaponDamageDataCollector calculator = new WeaponDamageDataCollector();
               InteractionManager.walkChain(calculator, type, context, root);
               return calculator.build();
            } catch (Throwable var6) {
               LOGGER.at(Level.WARNING).log("Failed to fetch damage value for item %s: %s", item.getId(), var6);
               return DamageBreakdown.EMPTY;
            }
         }
      }
   }

   private WeaponDamageDataCollector() {
   }

   @Override
   public void start() {
      this.stack.clear();
      this.leaves.clear();
      this.pendingTag = null;
   }

   @Override
   public void into(@Nonnull InteractionContext context, @Nullable Interaction interaction) {
      WeaponDamageDataCollector.Frame parent = this.stack.peek();
      WeaponDamageDataCollector.Frame frame = parent != null ? parent.copy() : new WeaponDamageDataCollector.Frame();
      CollectorTag tag = this.pendingTag;
      this.pendingTag = null;
      if (tag instanceof ChargingInteraction.ChargingTag charging) {
         frame.labelKey = charging.getSeconds() > 0.0 ? "charged" : null;
      } else if (tag instanceof DamageEntityInteraction.AngledDamageTag) {
         frame.labelKey = "backstab";
      } else if (tag instanceof DamageEntityInteraction.TargetedDamageTag targeted) {
         frame.labelKey = "targeted." + targeted.getKey();
      } else if (tag instanceof StringTag stringTag) {
         String name = stringTag.getTag();
         if ("Failed".equals(name) || "Blocked".equals(name)) {
            frame.labelKey = null;
         }
      }

      if (interaction != null) {
         if (!frame.visited.add(interaction.getId())) {
            frame.aborted = true;
         } else if (parent != null && parent.pendingDamage != null) {
            this.adjustDamageForChildEdge(parent.pendingDamage, tag, frame);
         }

         if (interaction instanceof DamageEntityInteraction damage) {
            frame.pendingDamage = damage;
            this.applyDamage(damage.getDamageCalculator(), damage.getRunTime(), 1, frame);
         } else {
            frame.pendingDamage = null;
            if (interaction instanceof ProjectileInteraction projectile) {
               frame.labelKey = null;
               this.applyProjectileHitDamage(projectile, context, frame);
            }
         }
      }

      if (parent != null) {
         parent.descended = true;
      }

      this.stack.push(frame);
   }

   @Override
   public boolean collect(@Nonnull CollectorTag tag, @Nonnull InteractionContext context, @Nonnull Interaction interaction) {
      this.pendingTag = tag;
      return false;
   }

   @Override
   public void outof() {
      WeaponDamageDataCollector.Frame popped = this.stack.pop();
      if (!popped.aborted && !popped.descended) {
         if (!(popped.totalDmgMax <= 0.0F)) {
            this.leaves.add(new WeaponDamageDataCollector.Leaf(popped.labelKey, popped.totalDmgMin, popped.totalDmgMax));
         }
      }
   }

   @Override
   public void finished() {
   }

   private void adjustDamageForChildEdge(@Nonnull DamageEntityInteraction parent, @Nullable CollectorTag tag, @Nonnull WeaponDamageDataCollector.Frame frame) {
      float runTime = parent.getRunTime();
      DamageCalculator baseCalc = parent.getDamageCalculator();
      if (tag instanceof DamageEntityInteraction.AngledDamageTag angled) {
         this.applyDamage(baseCalc, runTime, -1, frame);
         DamageEntityInteraction.AngledDamage[] angledArray = parent.getAngledDamage();
         if (angledArray != null && angled.getIndex() < angledArray.length) {
            DamageEntityInteraction.AngledDamage entry = angledArray[angled.getIndex()];
            DamageCalculator calc = entry.getDamageCalculator() != null ? entry.getDamageCalculator() : baseCalc;
            this.applyDamage(calc, runTime, 1, frame);
         }
      } else if (tag instanceof DamageEntityInteraction.TargetedDamageTag targeted) {
         this.applyDamage(baseCalc, runTime, -1, frame);
         DamageEntityInteraction.TargetedDamage entry = parent.getTargetedDamage().get(targeted.getKey());
         if (entry != null) {
            DamageCalculator calc = entry.getDamageCalculator() != null ? entry.getDamageCalculator() : baseCalc;
            this.applyDamage(calc, runTime, 1, frame);
         }
      } else if (tag instanceof StringTag stringTag) {
         String name = stringTag.getTag();
         if ("Failed".equals(name) || "Blocked".equals(name)) {
            this.applyDamage(baseCalc, runTime, -1, frame);
         }
      }
   }

   private void applyDamage(@Nullable DamageCalculator calc, double duration, int sign, @Nonnull WeaponDamageDataCollector.Frame frame) {
      if (calc != null) {
         calc.computeDamageRange(duration, this.damageScratch);
         frame.totalDmgMin = frame.totalDmgMin + sign * this.damageScratch[0];
         frame.totalDmgMax = frame.totalDmgMax + sign * this.damageScratch[1];
      }
   }

   private void applyProjectileHitDamage(
      @Nonnull ProjectileInteraction projectile, @Nonnull InteractionContext context, @Nonnull WeaponDamageDataCollector.Frame frame
   ) {
      ProjectileConfig config = projectile.getConfig();
      if (config != null) {
         String hitRootId = config.getInteractions().get(InteractionType.ProjectileHit);
         if (hitRootId != null) {
            RootInteraction hitRoot = RootInteraction.getAssetMap().getAsset(hitRootId);
            if (hitRoot != null) {
               WeaponDamageDataCollector.HitDamageCollector collector = new WeaponDamageDataCollector.HitDamageCollector();
               InteractionManager.walkChain(collector, InteractionType.ProjectileHit, context, hitRoot);
               frame.totalDmgMin = frame.totalDmgMin + collector.minDamage;
               frame.totalDmgMax = frame.totalDmgMax + collector.maxDamage;
            }
         }
      }
   }

   @Nonnull
   private DamageBreakdown build() {
      if (this.leaves.isEmpty()) {
         return DamageBreakdown.EMPTY;
      } else {
         Map<String, float[]> aggregates = new LinkedHashMap<>();

         for (WeaponDamageDataCollector.Leaf leaf : this.leaves) {
            String key = leaf.labelKey == null ? "" : leaf.labelKey;
            float[] range = aggregates.computeIfAbsent(key, k -> new float[]{Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY});
            if (leaf.totalDmgMin < range[0]) {
               range[0] = leaf.totalDmgMin;
            }

            if (leaf.totalDmgMax > range[1]) {
               range[1] = leaf.totalDmgMax;
            }
         }

         if (!aggregates.containsKey("") && aggregates.containsKey("charged")) {
            aggregates.put("", aggregates.remove("charged"));
         }

         List<DamageBreakdown.Entry> entries = new ArrayList<>(aggregates.size());
         float[] main = aggregates.remove("");
         if (main != null) {
            entries.add(new DamageBreakdown.Entry(null, main[0], main[1]));
         }

         for (Entry<String, float[]> entry : aggregates.entrySet()) {
            entries.add(new DamageBreakdown.Entry(entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
         }

         return new DamageBreakdown(entries);
      }
   }

   private static final class Frame {
      float totalDmgMin;
      float totalDmgMax;
      @Nullable
      String labelKey;
      @Nullable
      DamageEntityInteraction pendingDamage;
      @Nonnull
      Set<String> visited = new HashSet<>();
      boolean descended;
      boolean aborted;

      @Nonnull
      WeaponDamageDataCollector.Frame copy() {
         WeaponDamageDataCollector.Frame f = new WeaponDamageDataCollector.Frame();
         f.totalDmgMin = this.totalDmgMin;
         f.totalDmgMax = this.totalDmgMax;
         f.labelKey = this.labelKey;
         f.pendingDamage = this.pendingDamage;
         f.visited = new HashSet<>(this.visited);
         f.aborted = this.aborted;
         return f;
      }
   }

   private static final class HitDamageCollector implements Collector {
      private final Deque<WeaponDamageDataCollector.HitFrame> stack = new ArrayDeque<>();
      private final float[] scratch = new float[2];
      private float minDamage;
      private float maxDamage;
      @Nullable
      private CollectorTag pendingTag;

      @Override
      public void start() {
         this.stack.clear();
         this.minDamage = Float.POSITIVE_INFINITY;
         this.maxDamage = Float.NEGATIVE_INFINITY;
         this.pendingTag = null;
      }

      @Override
      public void into(@Nonnull InteractionContext context, @Nullable Interaction interaction) {
         WeaponDamageDataCollector.HitFrame parent = this.stack.peek();
         WeaponDamageDataCollector.HitFrame frame = parent != null ? parent.copy() : new WeaponDamageDataCollector.HitFrame();
         CollectorTag tag = this.pendingTag;
         this.pendingTag = null;
         if (interaction != null) {
            if (!frame.visited.add(interaction.getId())) {
               frame.aborted = true;
            } else if (parent != null && parent.pendingDamage != null) {
               this.adjustDamageForEdge(parent.pendingDamage, tag, frame);
            }

            if (interaction instanceof DamageEntityInteraction damage) {
               frame.pendingDamage = damage;
               this.applyToFrame(damage.getDamageCalculator(), damage.getRunTime(), 1, frame);
            } else {
               frame.pendingDamage = null;
            }
         }

         if (parent != null) {
            parent.descended = true;
         }

         this.stack.push(frame);
      }

      @Override
      public boolean collect(@Nonnull CollectorTag tag, @Nonnull InteractionContext context, @Nonnull Interaction interaction) {
         this.pendingTag = tag;
         return false;
      }

      @Override
      public void outof() {
         WeaponDamageDataCollector.HitFrame popped = this.stack.pop();
         if (!popped.aborted && !popped.descended) {
            if (popped.dmgMax > 0.0F) {
               if (popped.dmgMin < this.minDamage) {
                  this.minDamage = popped.dmgMin;
               }

               if (popped.dmgMax > this.maxDamage) {
                  this.maxDamage = popped.dmgMax;
               }
            }
         }
      }

      @Override
      public void finished() {
         if (this.minDamage == Float.POSITIVE_INFINITY) {
            this.minDamage = 0.0F;
            this.maxDamage = 0.0F;
         }
      }

      private void adjustDamageForEdge(@Nonnull DamageEntityInteraction parent, @Nullable CollectorTag tag, @Nonnull WeaponDamageDataCollector.HitFrame frame) {
         float runTime = parent.getRunTime();
         DamageCalculator base = parent.getDamageCalculator();
         if (tag instanceof DamageEntityInteraction.AngledDamageTag angled) {
            this.applyToFrame(base, runTime, -1, frame);
            DamageEntityInteraction.AngledDamage[] arr = parent.getAngledDamage();
            if (arr != null && angled.getIndex() < arr.length) {
               DamageEntityInteraction.AngledDamage entry = arr[angled.getIndex()];
               this.applyToFrame(entry.getDamageCalculator() != null ? entry.getDamageCalculator() : base, runTime, 1, frame);
            }
         } else if (tag instanceof DamageEntityInteraction.TargetedDamageTag targeted) {
            this.applyToFrame(base, runTime, -1, frame);
            DamageEntityInteraction.TargetedDamage entry = parent.getTargetedDamage().get(targeted.getKey());
            if (entry != null) {
               this.applyToFrame(entry.getDamageCalculator() != null ? entry.getDamageCalculator() : base, runTime, 1, frame);
            }
         } else if (tag instanceof StringTag stringTag) {
            String name = stringTag.getTag();
            if ("Failed".equals(name) || "Blocked".equals(name)) {
               this.applyToFrame(base, runTime, -1, frame);
            }
         }
      }

      private void applyToFrame(@Nullable DamageCalculator calc, double duration, int sign, @Nonnull WeaponDamageDataCollector.HitFrame frame) {
         if (calc != null) {
            calc.computeDamageRange(duration, this.scratch);
            frame.dmgMin = frame.dmgMin + sign * this.scratch[0];
            frame.dmgMax = frame.dmgMax + sign * this.scratch[1];
         }
      }
   }

   private static final class HitFrame {
      float dmgMin;
      float dmgMax;
      @Nullable
      DamageEntityInteraction pendingDamage;
      @Nonnull
      Set<String> visited = new HashSet<>();
      boolean descended;
      boolean aborted;

      @Nonnull
      WeaponDamageDataCollector.HitFrame copy() {
         WeaponDamageDataCollector.HitFrame f = new WeaponDamageDataCollector.HitFrame();
         f.dmgMin = this.dmgMin;
         f.dmgMax = this.dmgMax;
         f.pendingDamage = this.pendingDamage;
         f.visited = new HashSet<>(this.visited);
         f.aborted = this.aborted;
         return f;
      }
   }

   private record Leaf(@Nullable String labelKey, float totalDmgMin, float totalDmgMax) {
   }
}
