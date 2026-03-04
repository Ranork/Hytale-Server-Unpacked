package com.hypixel.hytale.server.worldgen.climate;

import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.math.vector.Vector2i;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class ClimateSearch {
   public static final int STEP_SIZE = 100;
   public static final int DEFAULT_RADIUS = 5000;
   public static final int MAX_RADIUS = 10000;
   public static final double PI2 = Math.PI * 2;
   public static final long SEED_OFFSET = 1659788403585L;
   public static final double TARGET_SCORE = 0.75;

   public static CompletableFuture<ClimateSearch.Result> search(
      int seed, int cx, int cy, int startRadius, int searchRadius, @Nonnull ClimateSearch.Rule rule, @Nonnull ClimateNoise noise, @Nonnull ClimateGraph graph
   ) {
      return CompletableFuture.supplyAsync(() -> {
         double bestScore = 0.0;
         Vector2i bestPosition = new Vector2i(cx, cy);
         int radius = Math.min(searchRadius, 10000);
         FastRandom rng = new FastRandom(HashUtil.hash(seed, 1659788403585L));
         long start_ms = System.currentTimeMillis();

         for (int r = startRadius; r <= radius; r += 100) {
            int steps = (int)Math.floor((Math.PI * 2) * r / 100.0);
            double inc = (Math.PI * 2) / steps;
            double off = (Math.PI * 2) * rng.nextDouble();

            for (int i = 0; i < steps; i++) {
               double t = i * inc + off;
               int x = cx + (int)(Math.cos(t) * r);
               int y = cy + (int)(Math.sin(t) * r);
               double score = collect(seed, x, y, noise, graph, rule);
               if (score > bestScore) {
                  bestPosition.assign(x, y);
                  bestScore = score;
               }
            }

            if (bestScore >= 0.75) {
               break;
            }
         }

         long time_ms = System.currentTimeMillis() - start_ms;
         return new ClimateSearch.Result(bestPosition, bestScore, time_ms);
      });
   }

   private static double collect(int seed, int x, int y, ClimateNoise noise, ClimateGraph graph, ClimateSearch.Rule rule) {
      double continent = noise.continent.get(seed, x, y);
      if (!rule.continent.test(continent)) {
         return 0.0;
      } else {
         double temperature = noise.temperature.get(seed, x, y);
         if (!rule.temperature.test(temperature)) {
            return 0.0;
         } else {
            double intensity = noise.intensity.get(seed, x, y);
            if (!rule.intensity.test(intensity)) {
               return 0.0;
            } else {
               double fade = graph.getFadeRaw(temperature, intensity);
               return !rule.fade.test(fade) ? 0.0 : rule.score(continent, temperature, intensity, fade);
            }
         }
      }
   }

   public static class Range {
      public static final ClimateSearch.Range DEFAULT = new ClimateSearch.Range(0.0, 1.0, 0.0);
      public final double value;
      public final double radius;
      public final double weight;

      public Range(double value, double radius, double weight) {
         this.value = value;
         this.radius = radius;
         this.weight = weight;
      }

      public double score(double value) {
         double dif = Math.min(this.radius, Math.abs(value - this.value));
         return 1.0 - dif / this.radius;
      }

      public boolean test(double value) {
         return Math.abs(value - this.value) <= this.radius;
      }
   }

   public record Result(Vector2i position, double score, long time_ms) {
      public String pretty() {
         double score = this.score * 100.0;
         return String.format("Position: {%d, %d}, Score: %.2f%%, Time: %dms", this.position.x, this.position.y, score, this.time_ms);
      }
   }

   public static class Rule {
      public final ClimateSearch.Range continent;
      public final ClimateSearch.Range temperature;
      public final ClimateSearch.Range intensity;
      public final ClimateSearch.Range fade;
      public final transient double sumWeight;

      public Rule(ClimateSearch.Range continent, ClimateSearch.Range temperature, ClimateSearch.Range intensity, ClimateSearch.Range fade) {
         this.continent = continent;
         this.temperature = temperature;
         this.intensity = intensity;
         this.fade = fade;
         this.sumWeight = continent.weight + temperature.weight + intensity.weight + fade.weight;
      }

      public double score(double continent, double temperature, double intensity, double fade) {
         double sumScore = 0.0;
         sumScore += this.continent.score(continent) * this.continent.weight;
         sumScore += this.temperature.score(temperature) * this.temperature.weight;
         sumScore += this.intensity.score(intensity) * this.intensity.weight;
         sumScore += this.fade.score(fade) * this.fade.weight;
         return sumScore / this.sumWeight;
      }
   }
}
