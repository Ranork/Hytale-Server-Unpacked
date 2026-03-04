package com.hypixel.hytale.server.npc.role.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderCodecObjectHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderCombatConfig;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderFactory;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectListHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectReferenceHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.BuilderTemplateInteractionVars;
import com.hypixel.hytale.server.npc.asset.builder.BuilderValidationHelper;
import com.hypixel.hytale.server.npc.asset.builder.FeatureEvaluatorHelper;
import com.hypixel.hytale.server.npc.asset.builder.InstructionContextHelper;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.asset.builder.SpawnableWithModelBuilder;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.FloatHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.IntHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.IntRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.IntSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringArrayNoEmptyStringsValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNullOrNotEmptyValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.TagSetExistsValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.AttitudeGroupExistsValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.BlockSetExistsValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ItemAttitudeGroupExistsValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ItemDropListExistsValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ItemExistsValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ModelExistsValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.RootInteractionValidator;
import com.hypixel.hytale.server.npc.config.AttitudeGroup;
import com.hypixel.hytale.server.npc.config.ItemAttitudeGroup;
import com.hypixel.hytale.server.npc.config.balancing.BalanceAsset;
import com.hypixel.hytale.server.npc.decisionmaker.stateevaluator.StateEvaluator;
import com.hypixel.hytale.server.npc.instructions.Instruction;
import com.hypixel.hytale.server.npc.movement.controllers.BuilderMotionControllerMapUtil;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.movement.controllers.builders.BuilderMotionControllerBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.RoleDebugFlags;
import com.hypixel.hytale.server.npc.role.SpawnEffect;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import com.hypixel.hytale.server.npc.statetransition.StateTransitionController;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import com.hypixel.hytale.server.spawning.ISpawnable;
import com.hypixel.hytale.server.spawning.SpawnTestResult;
import com.hypixel.hytale.server.spawning.SpawningContext;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderRole extends SpawnableWithModelBuilder<Role> implements SpawnEffect {
   protected static final double[] DEFAULT_HEAD_PITCH_RANGE = new double[]{-89.0, 89.0};
   protected String[] displayNames;
   protected final AssetHolder appearance = new AssetHolder();
   protected final AssetHolder dropListId = new AssetHolder();
   protected final IntHolder maxHealth = new IntHolder();
   protected String startState;
   protected String defaultSubState;
   protected int startStateIndex;
   protected int startSubStateIndex;
   protected final EnumSet<RoleDebugFlags> parsedDebugFlags = EnumSet.noneOf(RoleDebugFlags.class);
   protected String debugFlags;
   protected double inertia;
   protected final DoubleHolder knockbackScale = new DoubleHolder();
   protected String opaqueBlockSet;
   protected boolean applyAvoidance;
   protected double entityAvoidanceStrength;
   protected double collisionDistance;
   protected double collisionForceFalloff;
   protected double collisionRadius;
   protected float collisionViewAngle;
   protected final DoubleHolder separationDistance = new DoubleHolder();
   protected final DoubleHolder separationWeight = new DoubleHolder();
   protected final DoubleHolder separationDistanceTarget = new DoubleHolder();
   protected final DoubleHolder separationNearRadiusTarget = new DoubleHolder();
   protected final DoubleHolder separationFarRadiusTarget = new DoubleHolder();
   protected final BooleanHolder applySeparation = new BooleanHolder();
   protected boolean stayInEnvironment;
   protected String allowedEnvironments;
   protected final StringArrayHolder flockSpawnTypes = new StringArrayHolder();
   protected final BooleanHolder flockSpawnTypeRandom = new BooleanHolder();
   protected final StringArrayHolder flockAllowedRoles = new StringArrayHolder();
   protected final BooleanHolder canLeadFlock = new BooleanHolder();
   protected final FloatHolder spawnLockTime = new FloatHolder();
   protected double flockWeightAlignment;
   protected double flockWeightSeparation;
   protected double flockWeightCohesion;
   protected double flockInfluenceRange;
   protected boolean corpseStaysInFlock;
   protected final BooleanHolder invulnerable = new BooleanHolder();
   protected final BooleanHolder breathesInAir = new BooleanHolder();
   protected final BooleanHolder breathesInWater = new BooleanHolder();
   protected final AssetArrayHolder hotbarItems = new AssetArrayHolder();
   protected final AssetArrayHolder offHandItems = new AssetArrayHolder();
   protected final AssetHolder inventoryItemsDropList = new AssetHolder();
   protected final IntHolder defaultOffHandSlot = new IntHolder();
   protected boolean pickupDropOnDeath;
   protected String[] armor;
   protected double deathAnimationTime;
   protected float despawnAnimationTime;
   @Nonnull
   protected AssetHolder deathInteraction = new AssetHolder();
   protected Role.AvoidanceMode avoidanceMode;
   protected boolean disableDamageFlock;
   protected final AssetArrayHolder disableDamageGroups = new AssetArrayHolder();
   protected String spawnParticles;
   protected double[] spawnParticleOffset;
   protected double spawnViewDistance;
   protected int inventorySlots;
   protected int hotbarSlots;
   protected int offHandSlots;
   protected final EnumHolder<Attitude> defaultPlayerAttitude = new EnumHolder<>();
   protected final EnumHolder<Attitude> defaultNPCAttitude = new EnumHolder<>();
   protected final AssetHolder attitudeGroup = new AssetHolder();
   protected final AssetHolder itemAttitudeGroup = new AssetHolder();
   protected Int2ObjectMap<IntSet> busyStates;
   protected final BuilderObjectReferenceHelper<Map<String, MotionController>> motionControllers = new BuilderObjectReferenceHelper<>(
      BuilderMotionControllerMapUtil.CLASS_REFERENCE, this
   );
   protected final BuilderObjectListHelper<Instruction> instructionList = new BuilderObjectListHelper<>(Instruction.class, this);
   protected final BuilderObjectReferenceHelper<Instruction> interactionInstruction = new BuilderObjectReferenceHelper<>(Instruction.class, this);
   protected final BuilderObjectReferenceHelper<Instruction> deathInstruction = new BuilderObjectReferenceHelper<>(Instruction.class, this);
   protected final BuilderObjectReferenceHelper<StateTransitionController> stateTransitionController = new BuilderObjectReferenceHelper<>(
      StateTransitionController.class, this
   );
   protected final StringHolder initialMotionController = new StringHolder();
   protected final BuilderCodecObjectHelper<StateEvaluator> stateEvaluator = new BuilderCodecObjectHelper<>(StateEvaluator.class, StateEvaluator.CODEC, null);
   protected final BuilderCombatConfig combatConfig = new BuilderCombatConfig(BalanceAsset.CHILD_ASSET_CODEC, BalanceAsset.VALIDATOR_CACHE.getValidator());
   protected final BuilderTemplateInteractionVars interactionVars = new BuilderTemplateInteractionVars();
   protected final BooleanHolder isMemory = new BooleanHolder();
   protected final StringHolder memoriesCategory = new StringHolder();
   protected final StringHolder memoriesNameOverride = new StringHolder();
   protected final StringHolder nameTranslationKey = new StringHolder();
   protected final NumberArrayHolder headPitchAngleRange = new NumberArrayHolder();
   protected final BooleanHolder overrideHeadPitchAngle = new BooleanHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Generic role for NPC";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Generic role for NPC with a core planner and list of Motion controllers.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public Role build(@Nonnull BuilderSupport builderSupport) {
      return new Role(this, builderSupport);
   }

   @Override
   public boolean validate(
      String configName,
      @Nonnull NPCLoadTimeValidationHelper validationHelper,
      @Nonnull ExecutionContext context,
      Scope globalScope,
      @Nonnull List<String> errors
   ) {
      validationHelper.setInventorySizes(this.inventorySlots, this.hotbarSlots, this.offHandSlots);

      boolean hotbarValid;
      try {
         String[] hotbar = this.hotbarItems.get(context);
         hotbarValid = hotbar == null || hotbar.length == 0 || validationHelper.validateHotbarHasSlot(hotbar.length - 1, "HotbarItems", errors);
      } catch (IllegalStateException var14) {
         errors.add(String.format("%s: %s", configName, var14.getMessage()));
         hotbarValid = false;
      }

      boolean offHandValid;
      try {
         String[] offHand = this.offHandItems.get(context);
         offHandValid = (offHand == null || offHand.length == 0 || validationHelper.validateOffHandHasSlot(offHand.length - 1, "OffHandItems", errors))
            & validationHelper.validateOffHandHasSlot(this.defaultOffHandSlot.get(context), "DefaultOffHandSlot", errors);
      } catch (IllegalStateException var13) {
         errors.add(String.format("%s: %s", configName, var13.getMessage()));
         offHandValid = false;
      }

      boolean validInitialMotionController = true;
      String mc = this.initialMotionController.get(context);
      if (mc != null) {
         BuilderFactory<MotionController> factory = this.builderManager.getFactory(MotionController.class);

         try {
            BuilderMotionControllerBase builder = (BuilderMotionControllerBase)factory.createBuilder(mc);
            validationHelper.requireMotionControllerType(builder.getClassType());
         } catch (IllegalArgumentException var12) {
            errors.add(String.format("%s: Unable to set InitialMotionController to %s as no such MotionController exists", configName, mc));
            validInitialMotionController = false;
         }
      }

      return super.validate(configName, validationHelper, context, globalScope, errors)
         & hotbarValid
         & offHandValid
         & validInitialMotionController
         & this.instructionList.validate(configName, validationHelper, this.builderManager, context, globalScope, errors)
         & this.interactionInstruction.validate(configName, validationHelper, this.builderManager, context, globalScope, errors)
         & this.deathInstruction.validate(configName, validationHelper, this.builderManager, context, globalScope, errors)
         & this.stateTransitionController.validate(configName, validationHelper, this.builderManager, context, globalScope, errors)
         & this.motionControllers.validate(configName, validationHelper, this.builderManager, context, globalScope, errors)
         & this.combatConfig.validate(configName, validationHelper, context, errors);
   }

   @Nonnull
   public BuilderRole readConfig(@Nonnull JsonElement data) {
      super.readCommonConfig(data);
      this.setNotComponent();
      this.requireInt(data, "MaxHealth", this.maxHealth, IntSingleValidator.greater0(), BuilderDescriptorState.Stable, "Max health", null);
      this.getString(data, "Debug", e -> this.debugFlags = e, "", null, BuilderDescriptorState.WorkInProgress, "Debugging flags", null);
      this.requireAsset(data, "Appearance", this.appearance, ModelExistsValidator.required(), BuilderDescriptorState.Stable, "Model to use for rendering", null);
      this.getStringArray(
         data,
         "DisplayNames",
         a -> this.displayNames = a,
         null,
         null,
         StringArrayNoEmptyStringsValidator.get(),
         BuilderDescriptorState.Stable,
         "List of possible display names to choose from",
         null
      );
      this.requireString(
         data,
         "NameTranslationKey",
         this.nameTranslationKey,
         StringNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "The translation key for this NPC's name",
         null
      );
      this.getAsset(
         data,
         "OpaqueBlockSet",
         v -> this.opaqueBlockSet = v,
         "Opaque",
         BlockSetExistsValidator.withConfig(AssetValidator.CanBeEmpty),
         BuilderDescriptorState.Stable,
         "Blocks blocking line of sight",
         null
      );
      this.getDouble(data, "Inertia", d -> this.inertia = d, 1.0, DoubleSingleValidator.greater(0.1), BuilderDescriptorState.Experimental, "Inertia", null);
      this.getDouble(
         data,
         "KnockbackScale",
         this.knockbackScale,
         1.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Stable,
         "Scale factor for knockback",
         "Scale factor for knockback. Values greater 1 increase knockback. Smaller values decrease it."
      );
      this.getInt(
         data,
         "InventorySize",
         v -> this.inventorySlots = v,
         0,
         IntRangeValidator.between(0, 36),
         BuilderDescriptorState.Stable,
         "Number of available inventory slots",
         null
      );
      this.getInt(
         data,
         "HotbarSize",
         v -> this.hotbarSlots = v,
         3,
         IntRangeValidator.between(3, 8),
         BuilderDescriptorState.Stable,
         "Number of available hotbar slots",
         null
      );
      this.getInt(
         data,
         "OffHandSlots",
         v -> this.offHandSlots = v,
         0,
         IntRangeValidator.between(0, 4),
         BuilderDescriptorState.Stable,
         "The number of slots for off-hand items",
         null
      );
      this.getAssetArray(
         data,
         "HotbarItems",
         this.hotbarItems,
         null,
         0,
         8,
         ItemExistsValidator.orDroplistWithConfig(AssetValidator.ListCanBeEmpty),
         BuilderDescriptorState.Stable,
         "Hotbar items (e.g. primary weapon, secondary weapon, etc)",
         null
      );
      this.getAssetArray(
         data,
         "OffHandItems",
         this.offHandItems,
         null,
         0,
         8,
         ItemExistsValidator.withConfig(AssetValidator.ListCanBeEmpty),
         BuilderDescriptorState.Stable,
         "Off-hand items (e.g. shields, torches, etc)",
         null
      );
      this.getAsset(
         data,
         "PossibleInventoryItems",
         this.inventoryItemsDropList,
         null,
         ItemDropListExistsValidator.withConfig(AssetValidator.CanBeEmpty),
         BuilderDescriptorState.Stable,
         "A droplist defining the possible items the NPCs inventory could contain",
         null
      );
      this.getInt(
         data,
         "DefaultOffHandSlot",
         this.defaultOffHandSlot,
         -1,
         IntRangeValidator.between(-1, 4),
         BuilderDescriptorState.Stable,
         "The default off-hand item slot (-1 is empty)",
         null
      );
      this.getAssetArray(
         data,
         "Armor",
         a -> this.armor = a,
         null,
         null,
         ItemExistsValidator.withConfig(AssetValidator.ListCanBeEmpty),
         BuilderDescriptorState.WorkInProgress,
         "Armor items",
         null
      );
      this.getAsset(
         data,
         "DropList",
         this.dropListId,
         null,
         ItemDropListExistsValidator.withConfig(AssetValidator.CanBeEmpty),
         BuilderDescriptorState.Stable,
         "Drop list to spawn when killed",
         null
      );
      this.getString(data, "StartState", s -> this.startState = s, "start", StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "Initial state", null);
      this.getDefaultSubState(
         data,
         "DefaultSubState",
         v -> this.defaultSubState = v,
         StringNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "The default sub state to reference when transitioning to a main state without a specified sub state",
         null
      );
      this.getDouble(
         data,
         "CollisionDistance",
         d -> this.collisionDistance = d,
         5.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Collision lookahead",
         null
      );
      this.getDouble(
         data,
         "CollisionForceFalloff",
         d -> this.collisionForceFalloff = d,
         2.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "Falloff rate for collision force",
         null
      );
      this.getDouble(data, "CollisionRadius", d -> this.collisionRadius = d, -1.0, null, BuilderDescriptorState.Experimental, "Collision radius override", null);
      this.getFloat(
         data,
         "CollisionViewAngle",
         d -> this.collisionViewAngle = d,
         320.0F,
         DoubleRangeValidator.between(0.0, 360.0),
         BuilderDescriptorState.Experimental,
         "Collision detection view cone",
         null
      );
      this.getDouble(
         data,
         "SeparationDistance",
         this.separationDistance,
         3.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "Desired separation distance",
         null
      );
      this.getDouble(
         data,
         "SeparationWeight",
         this.separationWeight,
         1.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Experimental,
         "Blend factor separation",
         null
      );
      this.getDouble(
         data,
         "SeparationDistanceTarget",
         this.separationDistanceTarget,
         1.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Experimental,
         "Desired separation distance when close to target",
         null
      );
      this.getDouble(
         data,
         "SeparationNearRadiusTarget",
         this.separationNearRadiusTarget,
         1.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "Distance when using SeparationDistanceTarget",
         null
      );
      this.getDouble(
         data,
         "SeparationFarRadiusTarget",
         this.separationFarRadiusTarget,
         5.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "Use normal separation distance from further than this distance",
         null
      );
      this.getBoolean(data, "ApplyAvoidance", b -> this.applyAvoidance = b, false, BuilderDescriptorState.Experimental, "Apply avoidance steering force", null);
      this.getBoolean(data, "ApplySeparation", this.applySeparation, false, BuilderDescriptorState.Experimental, "Apply separation steering force", null);
      this.getEnum(
         data,
         "AvoidanceMode",
         e -> this.avoidanceMode = e,
         Role.AvoidanceMode.class,
         Role.AvoidanceMode.Any,
         BuilderDescriptorState.Experimental,
         "Abilities to use for avoidance",
         null
      );
      this.getDouble(
         data,
         "EntityAvoidanceStrength",
         d -> this.entityAvoidanceStrength = d,
         1.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Experimental,
         "Blending factor avoidance",
         null
      );
      this.getBoolean(
         data, "StayInEnvironment", b -> this.stayInEnvironment = b, false, BuilderDescriptorState.Experimental, "Stay in spawning environment", null
      );
      this.getString(
         data,
         "AllowedEnvironments",
         s -> this.allowedEnvironments = s,
         null,
         StringNullOrNotEmptyValidator.get(),
         BuilderDescriptorState.Experimental,
         "Allowed environment to walk in",
         null
      );
      this.getStringArray(
         data,
         "FlockSpawnTypes",
         this.flockSpawnTypes,
         null,
         0,
         Integer.MAX_VALUE,
         StringArrayNoEmptyStringsValidator.get(),
         BuilderDescriptorState.WorkInProgress,
         "Types of NPC this flock should consist off",
         null
      );
      this.getBoolean(
         data,
         "FlockSpawnTypesRandom",
         this.flockSpawnTypeRandom,
         false,
         BuilderDescriptorState.WorkInProgress,
         "Create a randomized flock if true else spawn in order of FlockSpawnTypes",
         null
      );
      this.getStringArray(
         data,
         "FlockAllowedNPC",
         this.flockAllowedRoles,
         null,
         0,
         Integer.MAX_VALUE,
         StringArrayNoEmptyStringsValidator.get(),
         BuilderDescriptorState.Experimental,
         "List of NPCs allowed in flock",
         null
      );
      this.getBoolean(data, "FlockCanLead", this.canLeadFlock, false, BuilderDescriptorState.Experimental, "This NPC can be flock leader", null);
      this.getDouble(
         data,
         "FlockWeightAlignment",
         v -> this.flockWeightAlignment = v,
         1.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "Blending flock alignment",
         null
      );
      this.getDouble(
         data,
         "FlockWeightSeparation",
         v -> this.flockWeightSeparation = v,
         1.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "Blending flock separation",
         null
      );
      this.getDouble(
         data,
         "FlockWeightCohesion",
         v -> this.flockWeightCohesion = v,
         1.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "Blending flock cohesion",
         null
      );
      this.getDouble(
         data,
         "FlockInfluenceRange",
         v -> this.flockInfluenceRange = v,
         10.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "Influence radius flock forces",
         null
      );
      this.getBoolean(
         data,
         "DisableDamageFlock",
         b -> this.disableDamageFlock = b,
         true,
         BuilderDescriptorState.WorkInProgress,
         "If true disables combat damage from flock members",
         null
      );
      this.getAssetArray(
         data,
         "DisableDamageGroups",
         this.disableDamageGroups,
         null,
         0,
         Integer.MAX_VALUE,
         TagSetExistsValidator.withConfig(AssetValidator.ListCanBeEmpty),
         BuilderDescriptorState.WorkInProgress,
         "Members in this list of group won't cause damage",
         null
      );
      this.getExistentStateSet(
         data,
         "BusyStates",
         s -> this.busyStates = s,
         this.stateHelper,
         BuilderDescriptorState.Stable,
         "States during which this NPC is busy and can't be interacted with",
         null
      );
      this.getCodecObject(
         data, "CombatConfig", this.combatConfig, BuilderDescriptorState.Stable, "The combat configuration providing optional combat action evaluator", null
      );
      this.getBoolean(data, "Invulnerable", this.invulnerable, false, BuilderDescriptorState.Stable, "Makes NPC ignore damage", null);
      this.getBoolean(data, "BreathesInAir", this.breathesInAir, true, BuilderDescriptorState.WorkInProgress, "Can breath in air", null);
      this.getBoolean(data, "BreathesInWater", this.breathesInWater, false, BuilderDescriptorState.WorkInProgress, "Can breath in fluid/water", null);
      this.getBoolean(data, "PickupDropOnDeath", t -> this.pickupDropOnDeath = t, false, BuilderDescriptorState.Stable, "Drop last picked item on death", null);
      this.getDouble(
         data,
         "DeathAnimationTime",
         d -> this.deathAnimationTime = d,
         5.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Experimental,
         "How long to let the death animation play before removing",
         null
      );
      this.getAsset(
         data,
         "DeathInteraction",
         this.deathInteraction,
         null,
         RootInteractionValidator.withConfig(AssetValidator.CanBeEmpty),
         BuilderDescriptorState.Experimental,
         "Interaction to run on death",
         null
      );
      this.getFloat(
         data,
         "DespawnAnimationTime",
         d -> this.despawnAnimationTime = d,
         0.8F,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Experimental,
         "How long to let the despawn animation play before removing",
         null
      );
      this.getString(
         data, "SpawnParticles", v -> this.spawnParticles = v, null, null, BuilderDescriptorState.Experimental, "Particle system when spawning", null
      );
      this.getVector3d(
         data,
         "SpawnParticlesOffset",
         v -> this.spawnParticleOffset = v,
         null,
         null,
         BuilderDescriptorState.Experimental,
         "Displacement from foot point to spawn relative to NPC heading",
         null
      );
      this.getDouble(
         data,
         "SpawnViewDistance",
         v -> this.spawnViewDistance = v,
         75.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "View distance for spawn particle",
         null
      );
      this.getEnum(
         data,
         "DefaultPlayerAttitude",
         this.defaultPlayerAttitude,
         Attitude.class,
         Attitude.HOSTILE,
         BuilderDescriptorState.Stable,
         "The default attitude of this NPC towards players",
         null
      );
      this.getEnum(
         data,
         "DefaultNPCAttitude",
         this.defaultNPCAttitude,
         Attitude.class,
         Attitude.NEUTRAL,
         BuilderDescriptorState.Stable,
         "The default attitude of this NPC towards other NPCs",
         null
      );
      this.getAsset(
         data,
         "AttitudeGroup",
         this.attitudeGroup,
         null,
         AttitudeGroupExistsValidator.withConfig(EnumSet.of(AssetValidator.Config.NULLABLE)),
         BuilderDescriptorState.Stable,
         "The attitude group towards other NPCs this NPC belongs to (often species related)",
         null
      );
      this.getAsset(
         data,
         "ItemAttitudeGroup",
         this.itemAttitudeGroup,
         null,
         ItemAttitudeGroupExistsValidator.withConfig(EnumSet.of(AssetValidator.Config.NULLABLE)),
         BuilderDescriptorState.Stable,
         "This NPC's item attitudes",
         null
      );
      this.getBoolean(
         data,
         "CorpseStaysInFlock",
         b -> this.corpseStaysInFlock = b,
         false,
         BuilderDescriptorState.Stable,
         "Whether the NPC should stay in the flock until corpse removal or be removed at the moment of death",
         null
      );
      this.getBoolean(
         data,
         "OverrideHeadPitchAngle",
         this.overrideHeadPitchAngle,
         true,
         BuilderDescriptorState.Experimental,
         "Whether to override the head pitch angle range",
         null
      );
      this.getDoubleRange(
         data,
         "HeadPitchAngleRange",
         this.headPitchAngleRange,
         DEFAULT_HEAD_PITCH_RANGE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(-90.0, 90.0),
         BuilderDescriptorState.Experimental,
         "Head rotation pitch range to be used instead of model camera settings",
         null
      );
      this.validateAny(this.breathesInAir, this.breathesInWater);
      this.registerStateSetter(this.startState, this.defaultSubState, (m, s) -> {
         this.startStateIndex = m;
         this.startSubStateIndex = s;
      });
      if (this.debugFlags != null && !this.debugFlags.isEmpty()) {
         this.parsedDebugFlags.addAll(this.toDebugFlagSet("RoleDebugFlags", this.debugFlags));
      }

      this.requireObject(
         data,
         "MotionControllerList",
         this.motionControllers,
         BuilderDescriptorState.Stable,
         "Motion controllers",
         null,
         new BuilderValidationHelper(this.fileName, null, this.internalReferenceResolver, null, null, this.extraInfo, this.evaluators, this.readErrors)
      );
      this.getArray(
         data,
         "Instructions",
         this.instructionList,
         null,
         BuilderDescriptorState.WorkInProgress,
         "List of instructions",
         null,
         new BuilderValidationHelper(
            this.fileName,
            null,
            this.internalReferenceResolver,
            this.stateHelper,
            new InstructionContextHelper(InstructionType.Default),
            this.extraInfo,
            this.evaluators,
            this.readErrors
         )
      );
      this.getObject(
         data,
         "InteractionInstruction",
         this.interactionInstruction,
         BuilderDescriptorState.Stable,
         "Interaction instruction",
         "An instruction designed to evaluate and set which players can interact with an NPC, along with setting correct states upon interaction",
         new BuilderValidationHelper(
            this.fileName,
            null,
            this.internalReferenceResolver,
            this.stateHelper,
            new InstructionContextHelper(InstructionType.Interaction),
            this.extraInfo,
            this.evaluators,
            this.readErrors
         )
      );
      this.getObject(
         data,
         "DeathInstruction",
         this.deathInstruction,
         BuilderDescriptorState.Stable,
         "Death instruction",
         "An instruction which will run only when the NPC is dead until they are removed",
         new BuilderValidationHelper(
            this.fileName,
            null,
            this.internalReferenceResolver,
            this.stateHelper,
            new InstructionContextHelper(InstructionType.Death),
            this.extraInfo,
            this.evaluators,
            this.readErrors
         )
      );
      this.getObject(
         data,
         "StateTransitions",
         this.stateTransitionController,
         BuilderDescriptorState.Stable,
         "State transition actions",
         "A set of state transitions and the actions that will be executed during them",
         new BuilderValidationHelper(
            this.fileName,
            new FeatureEvaluatorHelper().lock(),
            this.internalReferenceResolver,
            this.stateHelper,
            new InstructionContextHelper(InstructionType.StateTransitions),
            this.extraInfo,
            this.evaluators,
            this.readErrors
         )
      );
      this.getCodecObject(data, "StateEvaluator", this.stateEvaluator, BuilderDescriptorState.Stable, "A state evaluator", null);
      this.getString(
         data,
         "InitialMotionController",
         this.initialMotionController,
         null,
         StringNullOrNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "The initial motion controller to set",
         "The initial motion controller to set. If omitted and there are multiple, one will be chosen at random."
      );
      this.getCodecObject(data, "InteractionVars", this.interactionVars, BuilderDescriptorState.Stable, "Interaction vars to be used in interactions.", null);
      this.getBoolean(data, "IsMemory", this.isMemory, false, BuilderDescriptorState.Stable, "Used to define if the NPC has a Memory to record.", null);
      this.getString(
         data,
         "MemoriesCategory",
         this.memoriesCategory,
         "Other",
         StringNullOrNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "Category to put the NPC in, as part of the Memories Plugin",
         null
      );
      this.getString(
         data, "MemoriesNameOverride", this.memoriesNameOverride, "", null, BuilderDescriptorState.Stable, "Overrides the Memory name when set.", null
      );
      this.getFloat(
         data,
         "SpawnLockTime",
         this.spawnLockTime,
         1.5,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Stable,
         "How long the NPC should be locked and unable to perform behavior when first spawned",
         null
      );
      StateEvaluator stateEvaluator = this.stateEvaluator.build();
      if (stateEvaluator != null) {
         this.evaluators.add(stateEvaluator);
         stateEvaluator.prepareOptions(this.stateHelper);
         this.stateHelper.setHasStateEvaluator();
      }

      return this;
   }

   @Nonnull
   @Override
   public String getIdentifier() {
      BuilderInfo builderInfo = NPCPlugin.get().getBuilderInfo(this);
      Objects.requireNonNull(builderInfo, "Have builder but can't get builderInfo for it");
      return builderInfo.getKeyName();
   }

   @Nonnull
   @Override
   public SpawnTestResult canSpawn(@Nonnull SpawningContext context) {
      Builder<Map<String, MotionController>> builder = this.motionControllers.getBuilder(this.builderManager, context.getExecutionContext(), this);
      if (builder instanceof ISpawnable) {
         SpawnTestResult result = ((ISpawnable)builder).canSpawn(context);
         if (result != SpawnTestResult.TEST_OK) {
            return result;
         }

         if (!context.canBreathe(this.breathesInAir.get(context.getExecutionContext()), this.breathesInWater.get(context.getExecutionContext()))) {
            return SpawnTestResult.FAIL_NOT_BREATHABLE;
         }
      }

      return SpawnTestResult.TEST_OK;
   }

   @Nonnull
   @Override
   public Class<Role> category() {
      return Role.class;
   }

   @Override
   public String getSpawnModelName(ExecutionContext context, Scope modifierScope) {
      return this.appearance.get(context);
   }

   @Nullable
   @Override
   public Scope createModifierScope(ExecutionContext executionContext) {
      return null;
   }

   @Nonnull
   @Override
   public Scope createExecutionScope() {
      return this.getBuilderParameters().createScope();
   }

   @Override
   public void markNeedsReload() {
      NPCPlugin.get().setRoleBuilderNeedsReload(this);
   }

   @Override
   public String getSpawnParticles() {
      return this.spawnParticles;
   }

   @Override
   public Vector3d getSpawnParticleOffset() {
      return createVector3d(this.spawnParticleOffset, Vector3d.ZERO::clone);
   }

   @Override
   public double getSpawnViewDistance() {
      return this.spawnViewDistance;
   }

   @Override
   public final boolean isEnabled(ExecutionContext context) {
      return true;
   }

   public int getMaxHealth(@Nonnull BuilderSupport builderSupport) {
      return this.maxHealth.get(builderSupport.getExecutionContext());
   }

   @Nullable
   public String[] getDisplayNames() {
      return this.displayNames;
   }

   public String getNameTranslationKey(BuilderSupport support) {
      return this.nameTranslationKey.get(support.getExecutionContext());
   }

   public String getAppearance(@Nonnull BuilderSupport builderSupport) {
      return this.appearance.get(builderSupport.getExecutionContext());
   }

   public boolean isBreathesInAir(BuilderSupport support) {
      return this.breathesInAir.get(support.getExecutionContext());
   }

   public boolean isBreathesInWater(BuilderSupport support) {
      return this.breathesInWater.get(support.getExecutionContext());
   }

   public int getOpaqueBlockSet() {
      int index = BlockSet.getAssetMap().getIndex(this.opaqueBlockSet);
      if (index == Integer.MIN_VALUE) {
         throw new IllegalArgumentException("Unknown key! " + this.opaqueBlockSet);
      } else {
         return index;
      }
   }

   public double getInertia() {
      return this.inertia;
   }

   public double getKnockbackScale(@Nonnull BuilderSupport support) {
      return this.knockbackScale.get(support.getExecutionContext());
   }

   @Nullable
   public String[] getHotbarItems(@Nonnull BuilderSupport support) {
      return this.hotbarItems.get(support.getExecutionContext());
   }

   @Nullable
   public String[] getOffHandItems(@Nonnull BuilderSupport support) {
      return this.offHandItems.get(support.getExecutionContext());
   }

   public String getInventoryItemsDropList(@Nonnull BuilderSupport support) {
      return this.inventoryItemsDropList.get(support.getExecutionContext());
   }

   public String[] getArmor() {
      return this.armor;
   }

   public boolean isPickupDropOnDeath() {
      return this.pickupDropOnDeath;
   }

   public String getDropListId(@Nonnull BuilderSupport builderSupport) {
      return this.dropListId.get(builderSupport.getExecutionContext());
   }

   public String getStartState() {
      return this.startState;
   }

   public int getStartStateIndex() {
      return this.startStateIndex;
   }

   public int getStartSubStateIndex() {
      return this.startSubStateIndex;
   }

   public double getCollisionDistance() {
      return this.collisionDistance;
   }

   public double getCollisionForceFalloff() {
      return this.collisionForceFalloff;
   }

   public boolean isAvoidingEntities() {
      return this.applyAvoidance;
   }

   public Role.AvoidanceMode getAvoidanceMode() {
      return this.avoidanceMode;
   }

   public double getCollisionRadius() {
      return this.collisionRadius;
   }

   public double getSeparationDistance(BuilderSupport support) {
      return this.separationDistance.get(support.getExecutionContext());
   }

   public double getSeparationWeight(BuilderSupport support) {
      return this.separationWeight.get(support.getExecutionContext());
   }

   public double getSeparationDistanceTarget(BuilderSupport support) {
      return this.separationDistanceTarget.get(support.getExecutionContext());
   }

   public double getSeparationNearRadiusTarget(BuilderSupport support) {
      return this.separationNearRadiusTarget.get(support.getExecutionContext());
   }

   public double getSeparationFarRadiusTarget(BuilderSupport support) {
      return this.separationFarRadiusTarget.get(support.getExecutionContext());
   }

   public boolean isApplySeparation(BuilderSupport support) {
      return this.applySeparation.get(support.getExecutionContext());
   }

   public boolean isStayingInEnvironment() {
      return this.stayInEnvironment;
   }

   public String getAllowedEnvironments() {
      return this.allowedEnvironments;
   }

   public double getEntityAvoidanceStrength() {
      return this.entityAvoidanceStrength;
   }

   public boolean isOverridingHeadPitchAngle(@Nonnull BuilderSupport support) {
      return this.overrideHeadPitchAngle.get(support.getExecutionContext());
   }

   public float[] getHeadPitchAngleRange(@Nonnull BuilderSupport support) {
      double[] range = this.headPitchAngleRange.get(support.getExecutionContext());
      return new float[]{(float)(range[0] * (float) (Math.PI / 180.0)), (float)(range[1] * (float) (Math.PI / 180.0))};
   }

   @Nullable
   public String[] getFlockSpawnTypes(@Nonnull BuilderSupport support) {
      return this.flockSpawnTypes.get(support.getExecutionContext());
   }

   public boolean isFlockSpawnTypeRandom(@Nonnull BuilderSupport support) {
      return this.flockSpawnTypeRandom.get(support.getExecutionContext());
   }

   @Nonnull
   public String[] getFlockAllowedRoles(@Nonnull BuilderSupport support) {
      return this.nonNull(this.flockAllowedRoles.get(support.getExecutionContext()));
   }

   public boolean isCanLeadFlock(@Nonnull BuilderSupport support) {
      return this.canLeadFlock.get(support.getExecutionContext());
   }

   public double getFlockWeightAlignment() {
      return this.flockWeightAlignment;
   }

   public double getFlockWeightSeparation() {
      return this.flockWeightSeparation;
   }

   public double getFlockWeightCohesion() {
      return this.flockWeightCohesion;
   }

   public double getFlockInfluenceRange() {
      return this.flockInfluenceRange;
   }

   @Nonnull
   public EnumSet<RoleDebugFlags> getDebugFlags() {
      return this.parsedDebugFlags;
   }

   public float getCollisionViewAngle() {
      return (float) (Math.PI / 180.0) * this.collisionViewAngle;
   }

   @Nullable
   public String getBalanceAsset(@Nonnull BuilderSupport support) {
      return this.combatConfig.build(support.getExecutionContext());
   }

   public double getDeathAnimationTime() {
      return this.deathAnimationTime;
   }

   @Nullable
   public String getDeathInteraction(@Nonnull BuilderSupport builderSupport) {
      String val = this.deathInteraction.get(builderSupport.getExecutionContext());
      return val != null && !val.isEmpty() ? val : null;
   }

   public float getDespawnAnimationTime() {
      return this.despawnAnimationTime;
   }

   public boolean isDisableDamageFlock() {
      return this.disableDamageFlock;
   }

   public int[] getDisableDamageGroups(@Nonnull BuilderSupport support) {
      return WorldSupport.createTagSetIndexArray(this.disableDamageGroups.get(support.getExecutionContext()));
   }

   public boolean isInvulnerable(BuilderSupport support) {
      return this.invulnerable.get(support.getExecutionContext());
   }

   public int getInventorySlots() {
      return this.inventorySlots;
   }

   public int getHotbarSlots() {
      return this.hotbarSlots;
   }

   public int getOffHandSlots() {
      return this.offHandSlots;
   }

   public byte getDefaultOffHandSlot(@Nonnull BuilderSupport support) {
      return (byte)this.defaultOffHandSlot.get(support.getExecutionContext());
   }

   public Int2ObjectMap<IntSet> getBusyStates() {
      return this.busyStates;
   }

   public Attitude getDefaultPlayerAttitude(@Nonnull BuilderSupport support) {
      return this.defaultPlayerAttitude.get(support.getExecutionContext());
   }

   public Attitude getDefaultNPCAttitude(@Nonnull BuilderSupport support) {
      return this.defaultNPCAttitude.get(support.getExecutionContext());
   }

   public int getAttitudeGroup(@Nonnull BuilderSupport support) {
      String groupName = this.attitudeGroup.get(support.getExecutionContext());
      return AttitudeGroup.getAssetMap().getIndex(groupName);
   }

   public int getItemAttitudeGroup(@Nonnull BuilderSupport support) {
      String groupName = this.itemAttitudeGroup.get(support.getExecutionContext());
      return ItemAttitudeGroup.getAssetMap().getIndex(groupName);
   }

   public boolean isCorpseStaysInFlock() {
      return this.corpseStaysInFlock;
   }

   @Nullable
   public Map<String, MotionController> getMotionControllerMap(@Nonnull BuilderSupport support) {
      return this.motionControllers.build(support);
   }

   public String getInitialMotionController(@Nonnull BuilderSupport support) {
      return this.initialMotionController.get(support.getExecutionContext());
   }

   @Nullable
   public List<Instruction> getInstructionList(@Nonnull BuilderSupport support) {
      support.setCurrentInstructionContext(InstructionType.Default);
      return this.instructionList.build(support);
   }

   @Nullable
   public Instruction getInteractionInstruction(@Nonnull BuilderSupport support) {
      support.setCurrentInstructionContext(InstructionType.Interaction);
      return this.interactionInstruction.build(support);
   }

   @Nullable
   public Instruction getDeathInstruction(@Nonnull BuilderSupport support) {
      support.setCurrentInstructionContext(InstructionType.Death);
      return this.deathInstruction.build(support);
   }

   @Nullable
   public StateTransitionController getStateTransitionController(@Nonnull BuilderSupport support) {
      support.setCurrentInstructionContext(InstructionType.Default);
      return this.stateTransitionController.build(support);
   }

   public void registerStateEvaluator(@Nonnull BuilderSupport support) {
      support.setStateEvaluator(this.stateEvaluator.build());
   }

   @Nullable
   public Map<String, String> getInteractionVars(@Nonnull BuilderSupport support) {
      return this.interactionVars.build(support.getExecutionContext());
   }

   public boolean isMemory(ExecutionContext context) {
      return this.isMemory.get(context);
   }

   @Override
   public boolean isMemory(ExecutionContext context, Scope modifierScope) {
      return this.isMemory.get(context);
   }

   @Override
   public String getMemoriesCategory(ExecutionContext context, Scope modifierScope) {
      return this.memoriesCategory.get(context);
   }

   public String getMemoriesNameOverride(ExecutionContext context) {
      return this.memoriesNameOverride.get(context);
   }

   @Override
   public String getMemoriesNameOverride(ExecutionContext context, @Nullable Scope modifierScope) {
      return this.memoriesNameOverride.get(context);
   }

   @Nonnull
   @Override
   public String getNameTranslationKey(ExecutionContext context, @Nullable Scope modifierScope) {
      return this.nameTranslationKey.get(context);
   }

   public float getSpawnLockTime(BuilderSupport support) {
      return this.spawnLockTime.get(support.getExecutionContext());
   }

   @Override
   protected void runLoadTimeValidationHelper0(
      String configName, NPCLoadTimeValidationHelper loadTimeValidationHelper, ExecutionContext context, @Nonnull List<String> errors
   ) {
      NPCPlugin npcModule = NPCPlugin.get();
      String[] spawnTypes = this.flockSpawnTypes.get(context);
      boolean success = true;
      if (spawnTypes != null) {
         for (String type : spawnTypes) {
            if (!npcModule.hasRoleName(type)) {
               success = false;
               errors.add(String.format("%s: No such role '%s' for %s", configName, type, this.flockSpawnTypes.getName()));
            }
         }
      }

      String[] allowedTypes = this.flockAllowedRoles.get(context);
      if (allowedTypes != null) {
         for (String typex : allowedTypes) {
            if (!npcModule.hasRoleName(typex)) {
               success = false;
               errors.add(String.format("%s: No such role '%s' for %s", configName, typex, this.flockAllowedRoles.getName()));
            }
         }
      }

      if (!success) {
         throw new IllegalStateException(
            String.format("Invalid roles referenced for %s or %s", this.flockSpawnTypes.getName(), this.flockAllowedRoles.getName())
         );
      }
   }
}
