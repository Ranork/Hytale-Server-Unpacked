package com.hypixel.hytale.server.npc.corecomponents.combat.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.npc.asset.builder.BuilderCodecObjectHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.FloatHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.CombatInteractionValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.combat.ActionAttack;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionAttack extends BuilderActionBase {
   private static final String ATTACK_PARAMETER = "Attack";
   public static final String[] ANTECEDENT = new String[]{"SkipAiming"};
   public static final String[] SUBSEQUENT = new String[]{"LineOfSight", "AvoidFriendlyFire"};
   public static final double[] DEFAULT_ATTACK_PAUSE_RANGE = new double[]{0.0, 0.0};
   public static final double[] DEFAULT_AIMING_TIME_RANGE = new double[]{0.0, 0.0};
   protected final AssetHolder attack = new AssetHolder();
   protected final EnumHolder<ActionAttack.AttackType> attackType = new EnumHolder<>();
   protected final FloatHolder chargeFor = new FloatHolder();
   protected final NumberArrayHolder attackPauseRange = new NumberArrayHolder();
   protected final NumberArrayHolder aimingTimeRange = new NumberArrayHolder();
   protected double meleeConeAngle;
   protected ActionAttack.BallisticMode ballisticMode = ActionAttack.BallisticMode.Short;
   protected boolean checkLineOfSight;
   protected boolean avoidFriendlyFire;
   protected boolean damageFriendlies;
   protected boolean skipAiming;
   protected DoubleHolder chargeDistance = new DoubleHolder();
   protected final BuilderCodecObjectHelper<Map<String, String>> interactionVars = new BuilderCodecObjectHelper<>(
      RootInteraction.class, RootInteraction.CHILD_ASSET_CODEC_MAP, null
   );
   protected boolean attackProvided;

   @Nonnull
   public ActionAttack build(@Nonnull BuilderSupport builderSupport) {
      return new ActionAttack(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Starts attack";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Let NPC start an attack. When an attack is running no new attack is started.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Experimental;
   }

   @Nonnull
   public BuilderActionAttack readConfig(@Nonnull JsonElement data) {
      this.attackProvided = this.getAsset(
         data,
         "Attack",
         this.attack,
         null,
         CombatInteractionValidator.withConfig(AssetValidator.CanBeEmpty),
         BuilderDescriptorState.Experimental,
         "Attack pattern to use",
         "Attack pattern to use. If omitted, will cancel current attack"
      );
      this.getEnum(
         data,
         "AttackType",
         this.attackType,
         ActionAttack.AttackType.class,
         ActionAttack.AttackType.Primary,
         BuilderDescriptorState.Stable,
         "The interaction type to use",
         null
      );
      this.getFloat(
         data,
         "ChargeFor",
         this.chargeFor,
         0.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Stable,
         "How long to charge for",
         "How long to charge for. 0 indicates no charging. Also doubles as how long to block for"
      );
      this.getDoubleRange(
         data,
         "AttackPauseRange",
         this.attackPauseRange,
         DEFAULT_ATTACK_PAUSE_RANGE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "Range of minimum pause between attacks",
         null
      );
      this.getDoubleRange(
         data,
         "AimingTimeRange",
         this.aimingTimeRange,
         DEFAULT_AIMING_TIME_RANGE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "A range from which to pick a random value denoting the max time the NPC will wait for aiming before launching the attack.",
         null
      );
      this.getBoolean(
         data, "LineOfSight", b -> this.checkLineOfSight = b, false, BuilderDescriptorState.Experimental, "Check Line of Sight before firing", null
      );
      this.getBoolean(
         data, "AvoidFriendlyFire", b -> this.avoidFriendlyFire = b, true, BuilderDescriptorState.Experimental, "Tries to avoid friendly fire if true", null
      );
      this.getEnum(
         data,
         "BallisticMode",
         e -> this.ballisticMode = e,
         ActionAttack.BallisticMode.class,
         ActionAttack.BallisticMode.Short,
         BuilderDescriptorState.WorkInProgress,
         "Trajectory to use",
         null
      );
      this.getDouble(
         data,
         "MeleeConeAngle",
         d -> this.meleeConeAngle = d,
         30.0,
         DoubleRangeValidator.fromExclToIncl(0.0, 360.0),
         BuilderDescriptorState.WorkInProgress,
         "Cone angle considered for on target for melee",
         null
      );
      this.getBoolean(
         data,
         "DamageFriendlies",
         d -> this.damageFriendlies = d,
         false,
         BuilderDescriptorState.Stable,
         "Whether this attack should bypass ignored damage groups and deal damage to the target",
         null
      );
      this.getBoolean(
         data,
         "SkipAiming",
         b -> this.skipAiming = b,
         false,
         BuilderDescriptorState.Stable,
         "Whether aiming should be skipped an the attack just executed immediately.",
         null
      );
      this.getDouble(
         data,
         "ChargeDistance",
         this.chargeDistance,
         0.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Stable,
         "If this is a charge attack, the distance required for the charge",
         null
      );
      this.getCodecObject(
         data, "InteractionVars", this.interactionVars, BuilderDescriptorState.Stable, "Set of interaction vars for modifying the interaction", null
      );
      this.validateBooleanImplicationAnyAntecedent(
         ANTECEDENT, new boolean[]{this.skipAiming}, true, SUBSEQUENT, new boolean[]{this.checkLineOfSight, this.avoidFriendlyFire}, false
      );
      return this;
   }

   @Nullable
   public String getAttack(@Nonnull BuilderSupport builderSupport) {
      String computedAttack = this.attack.get(builderSupport.getExecutionContext());
      return computedAttack != null && !computedAttack.isEmpty() ? computedAttack : null;
   }

   public ActionAttack.AttackType getAttackType(@Nonnull BuilderSupport support) {
      return this.attackType.get(support.getExecutionContext());
   }

   public float getChargeTime(@Nonnull BuilderSupport support) {
      return this.chargeFor.get(support.getExecutionContext());
   }

   public double[] getAttackPauseRange(@Nonnull BuilderSupport support) {
      return this.attackPauseRange.get(support.getExecutionContext());
   }

   public double[] getAimingTimeRange(@Nonnull BuilderSupport support) {
      return this.aimingTimeRange.get(support.getExecutionContext());
   }

   public double getMeleeConeAngle() {
      return this.meleeConeAngle / 2.0 * (float) (Math.PI / 180.0);
   }

   public ActionAttack.BallisticMode getBallisticMode() {
      return this.ballisticMode;
   }

   public boolean isCheckLineOfSight() {
      return this.checkLineOfSight;
   }

   public boolean isAvoidFriendlyFire() {
      return this.avoidFriendlyFire;
   }

   public boolean isDamageFriendlies() {
      return this.damageFriendlies;
   }

   public boolean isSkipAiming() {
      return this.skipAiming;
   }

   public double getChargeDistance(@Nonnull BuilderSupport support) {
      return this.chargeDistance.get(support.getExecutionContext());
   }

   public int getAttackParameterSlot(@Nonnull BuilderSupport support) {
      return this.attackProvided ? Integer.MIN_VALUE : support.getParameterSlot("Attack");
   }

   @Nullable
   public Map<String, String> getInteractionVars() {
      return this.interactionVars.build();
   }
}
