package com.hypixel.hytale.server.npc.corecomponents.combat;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.SingleCollector;
import com.hypixel.hytale.server.core.modules.projectile.config.BallisticData;
import com.hypixel.hytale.server.core.modules.projectile.config.BallisticDataProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.combat.builders.BuilderActionAttack;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.interactions.NPCInteractionSimulationHandler;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.CombatSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.parameterproviders.ParameterProvider;
import com.hypixel.hytale.server.npc.sensorinfo.parameterproviders.StringParameterProvider;
import com.hypixel.hytale.server.npc.util.AimingData;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionAttack extends ActionBase {
   @Nonnull
   public static final ThreadLocal<SingleCollector<BallisticData>> THREAD_LOCAL_COLLECTOR = ThreadLocal.withInitial(
      () -> new SingleCollector<>(
         (collectorTag, interactionContext, interaction) -> interaction instanceof BallisticDataProvider ballisticDataProvider
               && Interaction.getAssetMap().getKeysForTag(CombatSupport.AIMING_REFERENCE_TAG_INDEX).contains(interaction.getId())
            ? ballisticDataProvider.getBallisticData()
            : null
      )
   );
   protected final int id;
   @Nullable
   protected String attack;
   protected final InteractionType interactionType;
   protected final float chargeFor;
   protected final double[] attackPauseRange;
   protected final double[] aimingTimeRange;
   protected final double meleeConeAngle;
   protected final ActionAttack.BallisticMode ballisticMode;
   protected final boolean checkLineOfSight;
   protected final boolean avoidFriendlyFire;
   protected final boolean damageFriendlies;
   protected final boolean skipAiming;
   protected final double chargeDistance;
   protected final int attackParameterSlot;
   @Nullable
   protected final Map<String, String> interactionVars;
   protected boolean attackReady;
   @Nullable
   protected String attackInteraction;
   protected boolean ballisticShort;
   protected StringParameterProvider cachedAttackProvider;
   protected boolean initialised;
   protected double aimingTimeRemaining;
   protected Role ownerRole;

   public ActionAttack(@Nonnull BuilderActionAttack builderActionAttack, @Nonnull BuilderSupport builderSupport) {
      super(builderActionAttack);
      this.id = builderSupport.getNextAttackIndex();
      this.attack = builderActionAttack.getAttack(builderSupport);
      this.interactionType = builderActionAttack.getAttackType(builderSupport).getInteractionType();
      this.chargeFor = builderActionAttack.getChargeTime(builderSupport);
      this.attackPauseRange = builderActionAttack.getAttackPauseRange(builderSupport);
      this.aimingTimeRange = builderActionAttack.getAimingTimeRange(builderSupport);
      this.meleeConeAngle = builderActionAttack.getMeleeConeAngle();
      this.ballisticMode = builderActionAttack.getBallisticMode();
      this.checkLineOfSight = builderActionAttack.isCheckLineOfSight();
      this.avoidFriendlyFire = builderActionAttack.isAvoidFriendlyFire();
      this.damageFriendlies = builderActionAttack.isDamageFriendlies();
      this.skipAiming = builderActionAttack.isSkipAiming();
      this.chargeDistance = builderActionAttack.getChargeDistance(builderSupport);
      this.attackParameterSlot = builderActionAttack.getAttackParameterSlot(builderSupport);
      this.interactionVars = builderActionAttack.getInteractionVars();
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return super.canExecute(ref, role, sensorInfo, dt, store) && !role.getCombatSupport().isExecutingAttack();
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      this.ownerRole = role;
      CombatSupport combatSupport = role.getCombatSupport();
      InteractionManager interactionManagerComponent = store.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());

      assert interactionManagerComponent != null;

      NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());

      assert npcComponent != null;

      if (!this.initialised) {
         ParameterProvider parameterProvider = this.attackParameterSlot >= 0 && sensorInfo != null
            ? sensorInfo.getParameterProvider(this.attackParameterSlot)
            : null;
         if (parameterProvider instanceof StringParameterProvider) {
            this.cachedAttackProvider = (StringParameterProvider)parameterProvider;
         }

         this.initialised = true;
      }

      if (this.cachedAttackProvider != null) {
         this.attackInteraction = this.cachedAttackProvider.getStringParameter();
      }

      AimingData aimingDataInfo = sensorInfo != null ? sensorInfo.getPassedExtraInfo(AimingData.class) : null;
      AimingData aimingData = aimingDataInfo != null && aimingDataInfo.isClaimedBy(this.id) ? aimingDataInfo : null;
      if (!this.attackReady) {
         this.attackReady = true;
         this.aimingTimeRemaining = this.newAimingTime();
         String nextOverride = combatSupport.getNextAttackOverride();
         if (nextOverride != null) {
            if (!RootInteraction.getAssetMap().getKeysForTag(CombatSupport.ATTACK_TAG_INDEX).contains(nextOverride)) {
               this.attackReady = false;
               ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.WARNING).atMostEvery(1, TimeUnit.MINUTES))
                  .log("NPC: %s using interaction %s that is not tagged as an 'Attack' usable by NPCs", npcComponent.getRoleName(), nextOverride);
               return true;
            }

            this.attackInteraction = nextOverride;
         } else if (this.attack != null && !this.attack.isEmpty()) {
            this.attackInteraction = this.attack;
         } else {
            ItemStack itemInHand = npcComponent.getInventory().getItemInHand();
            InteractionContext context = InteractionContext.forInteraction(interactionManagerComponent, ref, this.interactionType, store);
            String interaction = context.getRootInteractionId(this.interactionType);
            if (interaction == null) {
               this.attackReady = false;
               ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.WARNING).atMostEvery(1, TimeUnit.MINUTES))
                  .log(
                     "NPC: %s using nonexistent interaction of type %s using weapon %s in Attack action",
                     npcComponent.getRoleName(),
                     this.interactionType,
                     itemInHand == null ? "empty" : itemInHand.getItemId()
                  );
               return true;
            }

            Set<String> validKeys = RootInteraction.getAssetMap().getKeysForTag(CombatSupport.ATTACK_TAG_INDEX);
            if (!validKeys.contains(interaction)) {
               this.attackReady = false;
               ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.WARNING).atMostEvery(1, TimeUnit.MINUTES))
                  .log(
                     "NPC: %s with weapon %s, using interaction of type %s that is not tagged as an 'Attack' usable by NPCs",
                     npcComponent.getRoleName(),
                     itemInHand == null ? "empty" : itemInHand.getItemId(),
                     this.interactionType
                  );
               return true;
            }

            this.attackInteraction = interaction;
         }

         if (!this.skipAiming && aimingData != null) {
            SingleCollector<BallisticData> collector = THREAD_LOCAL_COLLECTOR.get();
            interactionManagerComponent.walkChain(
               ref, collector, InteractionType.Primary, RootInteraction.getAssetMap().getAsset(this.attackInteraction), store
            );
            BallisticData ballisticData = collector.getResult();
            if (ballisticData != null) {
               aimingData.requireBallistic(ballisticData);

               this.ballisticShort = switch (this.ballisticMode) {
                  case Short -> true;
                  case Long -> false;
                  case Alternate -> !this.ballisticShort;
                  case Random -> RandomExtra.randomBoolean();
               };
               aimingData.setUseFlatTrajectory(this.ballisticShort);
            } else {
               if (this.chargeDistance > 0.0) {
                  aimingData.setChargeDistance(this.chargeDistance);
                  aimingData.setDesiredHitAngle(this.meleeConeAngle);
               }

               aimingData.requireCloseCombat();
            }

            return false;
         }
      }

      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      Vector3f rotation = aimingData != null && aimingData.getChargeDistance() > 0.0 ? transformComponent.getRotation() : headRotationComponent.getRotation();
      if (this.hasTimeForAiming(dt) && aimingData != null && !aimingData.isOnTarget(rotation.getYaw(), rotation.getPitch(), this.meleeConeAngle)) {
         aimingData.clearSolution();
         return false;
      } else {
         Ref<EntityStore> target = aimingData != null ? aimingData.getTarget() : null;
         if (!this.checkLineOfSight || target != null && role.getPositionCache().hasLineOfSight(ref, target, store)) {
            if (this.avoidFriendlyFire && target != null && role.getPositionCache().isFriendlyBlockingLineOfSight(ref, target, store)) {
               aimingData.clearSolution();
               return true;
            } else {
               ((NPCInteractionSimulationHandler)interactionManagerComponent.getInteractionSimulationHandler()).requestChargeTime(this.chargeFor);
               InteractionContext contextx = InteractionContext.forInteraction(interactionManagerComponent, ref, this.interactionType, store);
               contextx.setInteractionVarsGetter(this::getInteractionVars);
               InteractionChain chain = interactionManagerComponent.initChain(
                  this.interactionType, contextx, RootInteraction.getRootInteractionOrUnknown(this.attackInteraction), false
               );
               interactionManagerComponent.queueExecuteChain(chain);
               combatSupport.setExecutingAttack(chain, this.damageFriendlies, this.newAttackPause());
               if (aimingData != null) {
                  aimingData.setHaveAttacked(true);
               }

               this.attackReady = false;
               return true;
            }
         } else {
            if (aimingData != null) {
               aimingData.clearSolution();
            }

            return true;
         }
      }
   }

   @Override
   public void activate(Role role, @Nullable InfoProvider infoProvider) {
      super.activate(role, infoProvider);
      if (infoProvider != null) {
         AimingData aimingData = infoProvider.getPassedExtraInfo(AimingData.class);
         if (aimingData != null) {
            aimingData.tryClaim(this.id);
         }
      }
   }

   @Override
   public void deactivate(Role role, @Nullable InfoProvider infoProvider) {
      super.deactivate(role, infoProvider);
      if (infoProvider != null) {
         AimingData aimingData = infoProvider.getPassedExtraInfo(AimingData.class);
         if (aimingData != null) {
            aimingData.release();
         }
      }
   }

   protected boolean hasTimeForAiming(double dt) {
      if (this.aimingTimeRemaining > 0.0) {
         this.aimingTimeRemaining -= dt;
         return true;
      } else {
         return false;
      }
   }

   protected double newAimingTime() {
      return this.aimingTimeRange[1] > 0.0 ? RandomExtra.randomRange(this.aimingTimeRange) : 0.0;
   }

   protected double newAttackPause() {
      return RandomExtra.randomRange(this.attackPauseRange[0], this.attackPauseRange[1]);
   }

   @Nullable
   private Map<String, String> getInteractionVars(InteractionContext c) {
      return this.interactionVars == null ? this.ownerRole.getInteractionVars() : this.interactionVars;
   }

   public static enum AttackType implements Supplier<String> {
      Primary(InteractionType.Primary, "Primary attack"),
      Secondary(InteractionType.Secondary, "Secondary attack"),
      Ability1(InteractionType.Ability1, "Ability 1"),
      Ability2(InteractionType.Ability2, "Ability 2"),
      Ability3(InteractionType.Ability3, "Ability 3");

      private final String description;
      private final InteractionType interactionType;

      private AttackType(InteractionType interactionType, String description) {
         this.interactionType = interactionType;
         this.description = description;
      }

      public String get() {
         return this.description;
      }

      public InteractionType getInteractionType() {
         return this.interactionType;
      }
   }

   public static enum BallisticMode implements Supplier<String> {
      Short("Shorter flight curve"),
      Long("Longer flight curve"),
      Alternate("Alternate between long and short"),
      Random("Random long or short");

      private final String description;

      private BallisticMode(String description) {
         this.description = description;
      }

      public String get() {
         return this.description;
      }
   }
}
