package com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.combatactions;

import com.hypixel.hytale.builtin.npccombatactionevaluator.Positioning;
import com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.CombatActionEvaluator;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.interactions.NPCInteractionSimulationHandler;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import com.hypixel.hytale.server.npc.valuestore.ValueStore;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class AbilityCombatAction extends CombatActionOption {
   @Nonnull
   public static final EnumCodec<AbilityCombatAction.AbilityType> MODE_CODEC = new EnumCodec<>(AbilityCombatAction.AbilityType.class)
      .documentKey(AbilityCombatAction.AbilityType.Primary, "Use primary attack.")
      .documentKey(AbilityCombatAction.AbilityType.Secondary, "Use secondary attack.");
   @Nonnull
   public static final EnumCodec<Positioning> POSITIONING_CODEC = new EnumCodec<>(Positioning.class)
      .documentKey(Positioning.Any, "Don't care about positioning.")
      .documentKey(Positioning.Front, "Try to be in front of the target.")
      .documentKey(Positioning.Behind, "Try to be behind the target.")
      .documentKey(Positioning.Flank, "Try to be on the target's flank.");
   @Nonnull
   public static final BuilderCodec<AbilityCombatAction> CODEC = BuilderCodec.builder(
         AbilityCombatAction.class, AbilityCombatAction::new, CombatActionOption.BASE_CODEC
      )
      .documentation("A combat action which executes an attack or ability by triggering an Interaction.")
      .<String>appendInherited(
         new KeyedCodec<>("Ability", RootInteraction.CHILD_ASSET_CODEC),
         (option, s) -> option.ability = s,
         option -> option.ability,
         (option, parent) -> option.ability = parent.ability
      )
      .addValidator(Validators.nonNull())
      .addValidator(RootInteraction.VALIDATOR_CACHE.getValidator())
      .documentation("The interaction (ability) to use.")
      .add()
      .<AbilityCombatAction.AbilityType>appendInherited(
         new KeyedCodec<>("AbilityType", MODE_CODEC),
         (option, e) -> option.abilityType = e,
         option -> option.abilityType,
         (option, parent) -> option.abilityType = parent.abilityType
      )
      .documentation("The ability type.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("ChargeFor", Codec.FLOAT),
         (option, f) -> option.chargeFor = f,
         option -> option.chargeFor,
         (option, parent) -> option.chargeFor = parent.chargeFor
      )
      .addValidator(Validators.greaterThanOrEqual(0.0F))
      .documentation("How long to charge the ability for before using it.")
      .add()
      .<double[]>appendInherited(new KeyedCodec<>("AttackDistanceRange", Codec.DOUBLE_ARRAY), (option, o) -> {
         option.attackRange = o;
         option.maxRangeSquared = o[1] * o[1];
      }, option -> option.attackRange, (option, parent) -> option.attackRange = parent.attackRange)
      .addValidator(Validators.nonNull())
      .addValidator(Validators.doubleArraySize(2))
      .addValidator(Validators.weaklyMonotonicSequentialDoubleArrayValidator())
      .documentation("The range at which the NPC needs to be from the target to execute the attack.")
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("WeaponSlot", Codec.INTEGER),
         (option, i) -> option.weaponSlot = i,
         option -> option.weaponSlot,
         (option, parent) -> option.weaponSlot = parent.weaponSlot
      )
      .documentation("The weapon (hotbar) slot to switch to for this attack.")
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("OffhandSlot", Codec.INTEGER),
         (option, i) -> option.offhandSlot = i,
         option -> option.offhandSlot,
         (option, parent) -> option.offhandSlot = parent.offhandSlot
      )
      .documentation("The off-hand slot to switch to for this attack. -1 set to no off-hand equipped.")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("FailureTimeout", Codec.FLOAT),
         (option, f) -> option.failureTimeout = f,
         option -> option.failureTimeout,
         (option, parent) -> option.failureTimeout = parent.failureTimeout
      )
      .addValidator(Validators.greaterThan(0.0F))
      .documentation("How long to try and run the action before giving up if it can't be completed in time.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("SubState", Codec.STRING),
         (option, s) -> option.subState = s,
         option -> option.subState,
         (option, parent) -> option.subState = parent.subState
      )
      .addValidator(Validators.nonEmptyString())
      .documentation("An optional substate to switch to when selecting this combat action to modify motion or other available actions.")
      .add()
      .<Map>appendInherited(
         new KeyedCodec<>("InteractionVars", new MapCodec<>(RootInteraction.CHILD_ASSET_CODEC, Object2ObjectOpenHashMap::new)),
         (option, v) -> option.interactionVars = v,
         option -> option.interactionVars,
         (option, parent) -> option.interactionVars = parent.interactionVars
      )
      .addValidator(RootInteraction.VALIDATOR_CACHE.getMapValueValidator())
      .documentation("Interaction vars to modify the values in the interaction itself.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("DamageFriendlies", Codec.BOOLEAN),
         (option, b) -> option.damageFriendlies = b,
         option -> option.damageFriendlies,
         (option, parent) -> option.damageFriendlies = parent.damageFriendlies
      )
      .documentation("Whether or not this ability should be able to damage friendly targets.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("RequireAiming", Codec.BOOLEAN),
         (option, b) -> option.requireAiming = b,
         option -> option.requireAiming,
         (option, parent) -> option.requireAiming = parent.requireAiming
      )
      .documentation("Whether or not this ability needs to be aimed at the target.")
      .add()
      .<Positioning>appendInherited(
         new KeyedCodec<>("Positioning", POSITIONING_CODEC),
         (option, e) -> option.positioning = e,
         option -> option.positioning,
         (option, parent) -> option.positioning = parent.positioning
      )
      .documentation("Where the NPC should try to position itself relative to the target's facing direction.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("PositionFirst", Codec.BOOLEAN),
         (option, b) -> option.positionFirst = b,
         option -> option.positionFirst,
         (option, parent) -> option.positionFirst = parent.positionFirst
      )
      .documentation("Whether the NPC should try to reach the correct positioning before executing the ability.")
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("ChargeDistance", Codec.DOUBLE),
         (option, d) -> option.chargeDistance = d,
         option -> option.chargeDistance,
         (option, parent) -> option.chargeDistance = parent.chargeDistance
      )
      .documentation("If this is a charge attack, the distance the charge will cover.")
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .build();
   @Nonnull
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   protected String ability;
   protected AbilityCombatAction.AbilityType abilityType = AbilityCombatAction.AbilityType.Primary;
   protected float chargeFor;
   protected double[] attackRange;
   protected double maxRangeSquared;
   protected int weaponSlot;
   protected int offhandSlot = -1;
   protected float failureTimeout = 10.0F;
   protected String subState;
   protected Map<String, String> interactionVars = Collections.emptyMap();
   protected boolean damageFriendlies;
   protected boolean requireAiming = true;
   protected Positioning positioning = Positioning.Any;
   protected boolean positionFirst;
   protected double chargeDistance;

   public String getAbility() {
      return this.ability;
   }

   public float getChargeFor() {
      return this.chargeFor;
   }

   public double[] getAttackRange() {
      return this.attackRange;
   }

   public int getWeaponSlot() {
      return this.weaponSlot;
   }

   public int getOffhandSlot() {
      return this.offhandSlot;
   }

   public float getFailureTimeout() {
      return this.failureTimeout;
   }

   public boolean isDamageFriendlies() {
      return this.damageFriendlies;
   }

   public boolean isPositionFirst() {
      return this.positionFirst;
   }

   @Override
   public void execute(
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Role role,
      @Nonnull CombatActionEvaluator evaluator,
      @Nonnull ValueStore valueStore
   ) {
      NPCEntity npcComponent = archetypeChunk.getComponent(index, NPCEntity.getComponentType());

      assert npcComponent != null;

      HytaleLogger.Api ctx = CombatActionEvaluator.LOGGER.at(Level.FINEST);
      if (ctx.isEnabled()) {
         ctx.log("%s: Executing option %s", archetypeChunk.getReferenceTo(index), this.getId());
      }

      InventoryHelper.setHotbarSlot(npcComponent.getInventory(), (byte)this.weaponSlot);
      InventoryHelper.setOffHandSlot(npcComponent.getInventory(), (byte)this.offhandSlot);
      if (this.subState != null) {
         role.getStateSupport().setSubState(this.subState);
         ctx = CombatActionEvaluator.LOGGER.at(Level.FINEST);
         if (ctx.isEnabled()) {
            ctx.log("%s: Set substate to %s", archetypeChunk.getReferenceTo(index), this.subState);
         }
      }

      if (this.actionTarget == CombatActionOption.Target.Self) {
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         RootInteraction interaction = RootInteraction.getAssetMap().getAsset(this.ability);
         if (interaction == null) {
            throw new IllegalStateException("No such interaction: " + this.ability);
         } else {
            InteractionManager interactionManagerComponent = archetypeChunk.getComponent(index, InteractionModule.get().getInteractionManagerComponent());

            assert interactionManagerComponent != null;

            if (interactionManagerComponent.getInteractionSimulationHandler() instanceof NPCInteractionSimulationHandler npcInteractionSimulationHandler) {
               npcInteractionSimulationHandler.requestChargeTime(this.chargeFor);
            }

            InteractionContext context = InteractionContext.forInteraction(interactionManagerComponent, ref, this.abilityType.interactionType, commandBuffer);
            context.setInteractionVarsGetter(this::getInteractionVars);
            InteractionChain chain = interactionManagerComponent.initChain(this.abilityType.interactionType, context, interaction, false);
            interactionManagerComponent.queueExecuteChain(chain);
            role.getCombatSupport().setExecutingAttack(chain, false, 2.0);
            ctx = CombatActionEvaluator.LOGGER.at(Level.INFO);
            if (ctx.isEnabled()) {
               ctx.log("%s: Executed self-targeted ability %s", archetypeChunk.getReferenceTo(index), this.ability);
            }

            evaluator.completeCurrentAction(true, true);
         }
      } else {
         valueStore.storeDouble(evaluator.getMinRangeSlot(), this.attackRange[0]);
         valueStore.storeDouble(evaluator.getMaxRangeSlot(), this.attackRange[1]);
         if (this.positioning == Positioning.Any) {
            valueStore.storeDouble(evaluator.getPositioningAngleSlot(), Double.MAX_VALUE);
         } else {
            float randomAngle = RandomExtra.randomRange(0.0F, (float) (Math.PI / 2));

            float chosenAngle = PhysicsMath.normalizeTurnAngle(switch (this.positioning) {
               case Front -> randomAngle + (float) (Math.PI / 2) + (float) (Math.PI / 4);
               case Behind -> randomAngle - (float) (Math.PI / 4);
               case Flank -> randomAngle + (RandomExtra.randomBoolean() ? (float) (Math.PI / 4) : (float) (-Math.PI * 3.0 / 4.0));
               default -> throw new IllegalStateException("Unexpected value: " + this.positioning);
            });
            valueStore.storeDouble(evaluator.getPositioningAngleSlot(), chosenAngle);
         }

         evaluator.setCurrentInteraction(
            this.ability,
            this.abilityType.interactionType,
            this.chargeFor,
            this.damageFriendlies,
            this.requireAiming,
            this.positionFirst,
            this.chargeDistance,
            this::getInteractionVars
         );
         evaluator.setTimeout(this.failureTimeout);
         ctx = CombatActionEvaluator.LOGGER.at(Level.FINEST);
         if (ctx.isEnabled()) {
            ctx.log("%s: Began executing ability %s", archetypeChunk.getReferenceTo(index), this.ability);
         }
      }
   }

   @Override
   public boolean isBasicAttackAllowed(
      int selfIndex,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull CombatActionEvaluator evaluator
   ) {
      Ref<EntityStore> primaryTarget = evaluator.getPrimaryTarget();
      if (primaryTarget != null && primaryTarget.isValid()) {
         TransformComponent primaryTargetTransformComponent = commandBuffer.getComponent(primaryTarget, TRANSFORM_COMPONENT_TYPE);

         assert primaryTargetTransformComponent != null;

         Vector3d targetPos = primaryTargetTransformComponent.getPosition();
         TransformComponent selfTransformComponent = archetypeChunk.getComponent(selfIndex, TRANSFORM_COMPONENT_TYPE);

         assert selfTransformComponent != null;

         Vector3d selfPos = selfTransformComponent.getPosition();
         double distance = selfPos.distanceSquaredTo(targetPos);
         return distance > this.maxRangeSquared;
      } else {
         return true;
      }
   }

   private Map<String, String> getInteractionVars(InteractionContext c) {
      return this.interactionVars;
   }

   @Nonnull
   @Override
   public String toString() {
      return "AbilityCombatAction{ability='"
         + this.ability
         + "', abilityType="
         + this.abilityType
         + ", chargeFor="
         + this.chargeFor
         + ", attackRange="
         + Arrays.toString(this.attackRange)
         + ", maxRangeSquared="
         + this.maxRangeSquared
         + ", weaponSlot="
         + this.weaponSlot
         + ", offhandSlot="
         + this.offhandSlot
         + ", failureTimeout="
         + this.failureTimeout
         + ", subState='"
         + this.subState
         + "', interactionVars="
         + this.interactionVars
         + ", damageFriendlies="
         + this.damageFriendlies
         + ", requireAiming="
         + this.requireAiming
         + "}"
         + super.toString();
   }

   private static enum AbilityType {
      Primary(InteractionType.Primary),
      Secondary(InteractionType.Secondary);

      private final InteractionType interactionType;

      private AbilityType(InteractionType interactionType) {
         this.interactionType = interactionType;
      }
   }
}
