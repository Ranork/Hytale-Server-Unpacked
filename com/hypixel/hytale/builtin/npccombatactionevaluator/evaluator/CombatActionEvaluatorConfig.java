package com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator;

import com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.combatactions.CombatActionOption;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.MapUtil;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.Condition;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;

public class CombatActionEvaluatorConfig {
   @Nonnull
   public static final BuilderCodec<CombatActionEvaluatorConfig> CODEC = BuilderCodec.builder(
         CombatActionEvaluatorConfig.class, CombatActionEvaluatorConfig::new
      )
      .appendInherited(
         new KeyedCodec<>("AvailableActions", new MapCodec<>(CombatActionOption.CHILD_ASSET_CODEC, Object2ObjectOpenHashMap::new)),
         (option, o) -> option.availableActions = MapUtil.combineUnmodifiable(option.availableActions, o),
         option -> option.availableActions,
         (option, parent) -> option.availableActions = parent.availableActions
      )
      .addValidator(Validators.nonNull())
      .addValidator(CombatActionOption.VALIDATOR_CACHE.getMapValueValidator())
      .documentation("A map of all available combat actions this NPC can take.")
      .add()
      .<Map>append(
         new KeyedCodec<>("ActionSets", new MapCodec<>(CombatActionEvaluatorConfig.ActionSet.CODEC, Object2ObjectOpenHashMap::new)),
         (option, o) -> option.actionSets = o,
         option -> option.actionSets
      )
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyMap())
      .documentation("A mapping of all combat substate names to the basic attacks and abilities that should be used in them.")
      .add()
      .<String[]>appendInherited(
         new KeyedCodec<>("RunConditions", Condition.CHILD_ASSET_CODEC_ARRAY),
         (option, s) -> option.runConditions = s,
         option -> option.runConditions,
         (option, parent) -> option.runConditions = parent.runConditions
      )
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyArray())
      .addValidator(Condition.VALIDATOR_CACHE.getArrayValidator())
      .documentation("The list of conditions that determine whether or not the combat action evaluator should run.")
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("MinRunUtility", Codec.DOUBLE),
         (option, d) -> option.minRunUtility = d,
         option -> option.minRunUtility,
         (option, parent) -> option.minRunUtility = parent.minRunUtility
      )
      .addValidator(Validators.range(0.5, 1.0))
      .documentation("The minimum utility score required to be returned from the RunConditions to trigger a new run of the combat action evaluator.")
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("MinActionUtility", Codec.DOUBLE),
         (option, d) -> option.minActionUtility = d,
         option -> option.minActionUtility,
         (option, parent) -> option.minActionUtility = parent.minActionUtility
      )
      .addValidator(Validators.range(0.0, 1.0))
      .documentation("The minimum utility score required for any individual combat action to be run.")
      .add()
      .<double[]>appendInherited(
         new KeyedCodec<>("PredictabilityRange", Codec.DOUBLE_ARRAY),
         (option, o) -> option.predictabilityRange = o,
         option -> option.predictabilityRange,
         (option, parent) -> option.predictabilityRange = parent.predictabilityRange
      )
      .addValidator(Validators.doubleArraySize(2))
      .addValidator(Validators.weaklyMonotonicSequentialDoubleArrayValidator())
      .documentation("A random range from which to pick the NPC's predictability factor.")
      .add()
      .build();
   @Nonnull
   private static final double[] DEFAULT_PREDICTABILITY_RANGE = new double[]{1.0, 1.0};
   protected Map<String, String> availableActions = Collections.emptyMap();
   protected Map<String, CombatActionEvaluatorConfig.ActionSet> actionSets;
   protected String[] runConditions;
   protected double minRunUtility = 0.8;
   protected double minActionUtility = 0.1;
   protected double[] predictabilityRange = DEFAULT_PREDICTABILITY_RANGE;

   public Map<String, String> getAvailableActions() {
      return this.availableActions;
   }

   public Map<String, CombatActionEvaluatorConfig.ActionSet> getActionSets() {
      return this.actionSets;
   }

   public String[] getRunConditions() {
      return this.runConditions;
   }

   public double getMinRunUtility() {
      return this.minRunUtility;
   }

   public double getMinActionUtility() {
      return this.minActionUtility;
   }

   public double[] getPredictabilityRange() {
      return this.predictabilityRange;
   }

   @Nonnull
   @Override
   public String toString() {
      return "CombatActionEvaluatorConfig{availableActions="
         + this.availableActions
         + ", actionSets="
         + this.actionSets
         + ", runConditions="
         + Arrays.toString((Object[])this.runConditions)
         + ", minRunUtility="
         + this.minRunUtility
         + ", minActionUtility="
         + this.minActionUtility
         + ", predictabilityRange="
         + Arrays.toString(this.predictabilityRange)
         + "}";
   }

   public static class ActionSet {
      @Nonnull
      public static final BuilderCodec<CombatActionEvaluatorConfig.ActionSet> CODEC = BuilderCodec.builder(
            CombatActionEvaluatorConfig.ActionSet.class, CombatActionEvaluatorConfig.ActionSet::new
         )
         .append(
            new KeyedCodec<>("BasicAttacks", CombatActionEvaluatorConfig.BasicAttacks.CODEC),
            (actionSet, o) -> actionSet.basicAttacks = o,
            actionSet -> actionSet.basicAttacks
         )
         .documentation("The basic attacks to be used in this combat substate.")
         .add()
         .<String[]>append(
            new KeyedCodec<>("Actions", Codec.STRING_ARRAY), (actionSet, o) -> actionSet.combatActions = o, actionsSet -> actionsSet.combatActions
         )
         .addValidator(Validators.nonNull())
         .addValidator(Validators.nonEmptyArray())
         .documentation("A list of available actions that should be used in this combat substate, mapped from AvailableActions.")
         .add()
         .build();
      protected CombatActionEvaluatorConfig.BasicAttacks basicAttacks;
      protected String[] combatActions;

      protected ActionSet() {
      }

      public CombatActionEvaluatorConfig.BasicAttacks getBasicAttacks() {
         return this.basicAttacks;
      }

      public String[] getCombatActions() {
         return this.combatActions;
      }

      @Nonnull
      @Override
      public String toString() {
         return "ActionSet{basicAttacks=" + this.basicAttacks + ", combatActions=" + Arrays.toString((Object[])this.combatActions) + "}";
      }
   }

   public static class BasicAttacks {
      @Nonnull
      public static final BuilderCodec<CombatActionEvaluatorConfig.BasicAttacks> CODEC = BuilderCodec.builder(
            CombatActionEvaluatorConfig.BasicAttacks.class, CombatActionEvaluatorConfig.BasicAttacks::new
         )
         .append(
            new KeyedCodec<>("Attacks", RootInteraction.CHILD_ASSET_CODEC_ARRAY),
            (basicAttacks, o) -> basicAttacks.attacks = o,
            basicAttacks -> basicAttacks.attacks
         )
         .addValidator(Validators.nonNull())
         .addValidator(Validators.nonEmptyArray())
         .addValidator(RootInteraction.VALIDATOR_CACHE.getArrayValidator())
         .documentation("The sequence of basic attacks to be used.")
         .add()
         .<Boolean>append(new KeyedCodec<>("Randomise", Codec.BOOLEAN), (basicAttacks, b) -> basicAttacks.randomise = b, basicAttacks -> basicAttacks.randomise)
         .documentation("Whether or not the basic attacks should be executed randomly, or run in the order they were defined in.")
         .add()
         .<Double>append(new KeyedCodec<>("MaxRange", Codec.DOUBLE), (basicAttacks, d) -> {
            basicAttacks.maxRange = d;
            basicAttacks.maxRangeSquared = d * d;
         }, basicAttacks -> basicAttacks.maxRange)
         .addValidator(Validators.nonNull())
         .addValidator(Validators.greaterThan(0.0))
         .documentation("How close a target needs to be to use a basic attack against them.")
         .add()
         .<Float>append(new KeyedCodec<>("Timeout", Codec.FLOAT), (basicAttacks, f) -> basicAttacks.timeout = f, basicAttacks -> basicAttacks.timeout)
         .addValidator(Validators.greaterThan(0.0F))
         .documentation("How long before giving up if a target moves out of range while preparing to execute a basic attack.")
         .add()
         .<double[]>append(
            new KeyedCodec<>("CooldownRange", Codec.DOUBLE_ARRAY),
            (basicAttacks, o) -> basicAttacks.cooldownRange = o,
            basicAttacks -> basicAttacks.cooldownRange
         )
         .addValidator(Validators.nonNull())
         .addValidator(Validators.doubleArraySize(2))
         .addValidator(Validators.weaklyMonotonicSequentialDoubleArrayValidator())
         .documentation("A random range to pick a cooldown between basic attacks from.")
         .add()
         .<Map>appendInherited(
            new KeyedCodec<>("InteractionVars", new MapCodec<>(RootInteraction.CHILD_ASSET_CODEC, Object2ObjectOpenHashMap::new)),
            (basicAttacks, v) -> basicAttacks.interactionVars = v,
            basicAttacks -> basicAttacks.interactionVars,
            (basicAttacks, parent) -> basicAttacks.interactionVars = parent.interactionVars
         )
         .addValidator(RootInteraction.VALIDATOR_CACHE.getMapValueValidator())
         .documentation("Interaction vars to modify the values in the interaction itself.")
         .add()
         .<Boolean>appendInherited(
            new KeyedCodec<>("DamageFriendlies", Codec.BOOLEAN),
            (basicAttacks, b) -> basicAttacks.damageFriendlies = b,
            basicAttacks -> basicAttacks.damageFriendlies,
            (basicAttacks, parent) -> basicAttacks.damageFriendlies = parent.damageFriendlies
         )
         .documentation("Whether or not basic attacks should be able to damage friendly targets.")
         .add()
         .<Boolean>appendInherited(
            new KeyedCodec<>("UseProjectedDistance", Codec.BOOLEAN),
            (basicAttacks, b) -> basicAttacks.useProjectedDistance = b,
            basicAttacks -> basicAttacks.useProjectedDistance,
            (basicAttacks, parent) -> basicAttacks.useProjectedDistance = parent.useProjectedDistance
         )
         .documentation("Whether to use projected distance instead of 3D distance for checking if in range of basic attacks.")
         .add()
         .build();
      protected String[] attacks;
      protected boolean randomise;
      protected double maxRange;
      protected double maxRangeSquared;
      protected float timeout = 2.0F;
      protected double[] cooldownRange;
      protected Map<String, String> interactionVars = Collections.emptyMap();
      protected boolean damageFriendlies;
      protected boolean useProjectedDistance;

      protected BasicAttacks() {
      }

      public String[] getAttacks() {
         return this.attacks;
      }

      public boolean isRandom() {
         return this.randomise;
      }

      public double getMaxRange() {
         return this.maxRange;
      }

      public double getMaxRangeSquared() {
         return this.maxRangeSquared;
      }

      public float getTimeout() {
         return this.timeout;
      }

      public double[] getCooldownRange() {
         return this.cooldownRange;
      }

      public Map<String, String> getInteractionVars(InteractionContext c) {
         return this.interactionVars;
      }

      public boolean isDamageFriendlies() {
         return this.damageFriendlies;
      }

      public boolean shouldUseProjectedDistance() {
         return this.useProjectedDistance;
      }

      @Nonnull
      @Override
      public String toString() {
         return "BasicAttacks{attacks="
            + Arrays.toString((Object[])this.attacks)
            + ", randomise="
            + this.randomise
            + ", maxRange="
            + this.maxRange
            + ", maxRangeSquared="
            + this.maxRangeSquared
            + ", timeout="
            + this.timeout
            + ", cooldownRange="
            + Arrays.toString(this.cooldownRange)
            + ", interactionVars="
            + this.interactionVars
            + ", damageFriendlies="
            + this.damageFriendlies
            + ", useProjectedDistance="
            + this.useProjectedDistance
            + "}";
      }
   }
}
