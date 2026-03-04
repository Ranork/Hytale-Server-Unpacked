package com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator;

import com.hypixel.hytale.builtin.npccombatactionevaluator.CombatActionEvaluatorSystems;
import com.hypixel.hytale.builtin.npccombatactionevaluator.NPCCombatActionEvaluatorPlugin;
import com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.combatactions.CombatActionOption;
import com.hypixel.hytale.builtin.npccombatactionevaluator.memory.TargetMemory;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.StateMappingHelper;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.Evaluator;
import com.hypixel.hytale.server.npc.decisionmaker.core.Option;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.valuestore.ValueStore;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CombatActionEvaluator extends Evaluator<CombatActionOption> implements Component<EntityStore> {
   protected static final float NO_TIMEOUT = Float.MAX_VALUE;
   protected CombatActionEvaluator.RunOption runOption;
   protected double minRunUtility;
   protected long lastRunNanos = NOT_USED;
   protected int runInState;
   protected float predictability;
   protected double minActionUtility;
   @Nonnull
   protected final Int2ObjectMap<List<Evaluator<CombatActionOption>.OptionHolder>> optionsBySubState = new Int2ObjectOpenHashMap();
   @Nonnull
   protected final Int2ObjectMap<CombatActionEvaluatorConfig.BasicAttacks> basicAttacksBySubState = new Int2ObjectOpenHashMap();
   protected int currentBasicAttackSubState = Integer.MIN_VALUE;
   protected CombatActionEvaluatorConfig.BasicAttacks currentBasicAttackSet;
   @Nullable
   protected String currentBasicAttack;
   protected Function<InteractionContext, Map<String, String>> currentBasicAttacksInteractionVarsGetter;
   protected boolean currentBasicAttackDamageFriendlies;
   protected int nextBasicAttackIndex;
   protected double basicAttackCooldown;
   @Nullable
   protected Ref<EntityStore> basicAttackTarget;
   protected double basicAttackTimeout;
   @Nullable
   protected Ref<EntityStore> primaryTarget;
   @Nullable
   protected Ref<EntityStore> previousTarget;
   @Nullable
   protected CombatActionEvaluator.CombatOptionHolder currentAction;
   @Nullable
   protected double[] postExecutionDistanceRange;
   protected int markedTargetSlot;
   protected int minRangeSlot;
   protected int maxRangeSlot;
   protected int positioningAngleSlot;
   @Nullable
   protected String currentInteraction;
   protected Function<InteractionContext, Map<String, String>> currentInteractionVarsGetter;
   protected InteractionType currentInteractionType;
   protected float chargeFor;
   protected boolean currentDamageFriendlies;
   protected boolean requireAiming;
   protected boolean positionFirst;
   protected double chargeDistance;
   protected float timeout;
   @Nonnull
   protected final EvaluationContext evaluationContext = new EvaluationContext();

   public static ComponentType<EntityStore, CombatActionEvaluator> getComponentType() {
      return NPCCombatActionEvaluatorPlugin.get().getCombatActionEvaluatorComponentType();
   }

   public CombatActionEvaluator(
      @Nonnull Role role, @Nonnull CombatActionEvaluatorConfig config, @Nonnull CombatActionEvaluatorSystems.CombatConstructionData data
   ) {
      this.runOption = new CombatActionEvaluator.RunOption(config.getRunConditions());
      this.runOption.sortConditions();
      this.minRunUtility = config.getMinRunUtility();
      this.minActionUtility = config.getMinActionUtility();
      this.predictability = (float)RandomExtra.randomRange(config.getPredictabilityRange());
      StateMappingHelper stateHelper = role.getStateSupport().getStateHelper();
      String activeState = data.getCombatState();
      this.runInState = stateHelper.getStateIndex(activeState);
      this.markedTargetSlot = data.getMarkedTargetSlot();
      this.minRangeSlot = data.getMinRangeSlot();
      this.maxRangeSlot = data.getMaxRangeSlot();
      this.positioningAngleSlot = data.getPositioningAngleSlot();
      Map<String, String> availableActions = config.getAvailableActions();
      Map<String, Evaluator<CombatActionOption>.OptionHolder> wrappedAvailableActions = new Object2ObjectOpenHashMap();

      for (Entry<String, String> action : availableActions.entrySet()) {
         CombatActionOption option = CombatActionOption.getAssetMap().getAsset(action.getValue());
         if (option == null) {
            throw new IllegalStateException(String.format("Option %s does not exist!", action.getValue()));
         }

         option.sortConditions();

         CombatActionEvaluator.CombatOptionHolder holder = (CombatActionEvaluator.CombatOptionHolder)(switch (option.getActionTarget()) {
            case Self -> new CombatActionEvaluator.SelfCombatOptionHolder(option);
            case Hostile, Friendly -> new CombatActionEvaluator.MultipleTargetCombatOptionHolder(option);
         });
         wrappedAvailableActions.put(action.getKey(), holder);
      }

      Map<String, CombatActionEvaluatorConfig.ActionSet> actionSets = config.getActionSets();

      for (Entry<String, CombatActionEvaluatorConfig.ActionSet> subState : actionSets.entrySet()) {
         int subStateIndex = stateHelper.getSubStateIndex(this.runInState, subState.getKey());
         if (subStateIndex == Integer.MIN_VALUE) {
            throw new IllegalStateException(String.format("No such state for combat evaluator: %s.%s", activeState, subState.getKey()));
         }

         CombatActionEvaluatorConfig.ActionSet actionSet = subState.getValue();
         this.basicAttacksBySubState.put(subStateIndex, actionSet.getBasicAttacks());
         List<Evaluator<CombatActionOption>.OptionHolder> optionList = (List<Evaluator<CombatActionOption>.OptionHolder>)this.optionsBySubState
            .computeIfAbsent(subStateIndex, k -> new ObjectArrayList());
         String[] combatActions = actionSet.getCombatActions();

         for (String action : combatActions) {
            Evaluator<CombatActionOption>.OptionHolder wrappedAction = wrappedAvailableActions.get(action);
            if (wrappedAction == null) {
               throw new IllegalStateException(String.format("No action with name '%s' defined in AvailableActions!", action));
            }

            optionList.add(wrappedAction);
         }
      }

      ObjectIterator var22 = this.optionsBySubState.values().iterator();

      while (var22.hasNext()) {
         List<Evaluator<CombatActionOption>.OptionHolder> optionList = (List<Evaluator<CombatActionOption>.OptionHolder>)var22.next();
         optionList.sort(Comparator.comparingDouble(Evaluator.OptionHolder::getWeightCoefficient).reversed());
      }
   }

   protected CombatActionEvaluator() {
   }

   public CombatActionEvaluator.RunOption getRunOption() {
      return this.runOption;
   }

   public double getMinRunUtility() {
      return this.minRunUtility;
   }

   @Nonnull
   public EvaluationContext getEvaluationContext() {
      return this.evaluationContext;
   }

   public long getLastRunNanos() {
      return this.lastRunNanos;
   }

   public void setLastRunNanos(long lastRunNanos) {
      this.lastRunNanos = lastRunNanos;
   }

   public int getRunInState() {
      return this.runInState;
   }

   @Nonnull
   public Int2ObjectMap<List<Evaluator<CombatActionOption>.OptionHolder>> getOptionsBySubState() {
      return this.optionsBySubState;
   }

   public CombatActionEvaluatorConfig.BasicAttacks getBasicAttacks(int subState) {
      return (CombatActionEvaluatorConfig.BasicAttacks)this.basicAttacksBySubState.get(subState);
   }

   public void setCurrentBasicAttackSet(int subState, CombatActionEvaluatorConfig.BasicAttacks attacks) {
      if (subState != this.currentBasicAttackSubState) {
         this.nextBasicAttackIndex = 0;
         this.currentBasicAttackSubState = subState;
         this.currentBasicAttackSet = attacks;
      }
   }

   @Nullable
   public String getCurrentBasicAttack() {
      return this.currentBasicAttack;
   }

   public CombatActionEvaluatorConfig.BasicAttacks getCurrentBasicAttackSet() {
      return this.currentBasicAttackSet;
   }

   public void setCurrentBasicAttack(String attack, boolean damageFriendlies, Function<InteractionContext, Map<String, String>> interactionVarsGetter) {
      this.currentBasicAttack = attack;
      this.currentBasicAttacksInteractionVarsGetter = interactionVarsGetter;
      this.currentBasicAttackDamageFriendlies = damageFriendlies;
   }

   public int getNextBasicAttackIndex() {
      return this.nextBasicAttackIndex;
   }

   public void setNextBasicAttackIndex(int next) {
      this.nextBasicAttackIndex = next;
   }

   public boolean canUseBasicAttack(int selfIndex, ArchetypeChunk<EntityStore> archetypeChunk, CommandBuffer<EntityStore> commandBuffer) {
      return this.basicAttackCooldown > 0.0
         ? false
         : this.currentAction == null
            || ((CombatActionOption)this.currentAction.getOption()).isBasicAttackAllowed(selfIndex, archetypeChunk, commandBuffer, this);
   }

   public void tickBasicAttackCoolDown(float dt) {
      if (this.basicAttackCooldown > 0.0) {
         this.basicAttackCooldown -= dt;
      }
   }

   @Nullable
   public Ref<EntityStore> getBasicAttackTarget() {
      return this.basicAttackTarget;
   }

   public void setBasicAttackTarget(Ref<EntityStore> target) {
      this.basicAttackTarget = target;
   }

   public boolean tickBasicAttackTimeout(float dt) {
      return (this.basicAttackTimeout -= dt) <= 0.0;
   }

   public void setBasicAttackTimeout(double timeout) {
      this.basicAttackTimeout = timeout;
   }

   @Nullable
   public Ref<EntityStore> getPrimaryTarget() {
      return this.primaryTarget;
   }

   public void clearPrimaryTarget() {
      this.primaryTarget = null;
   }

   public void setActiveOptions(List<Evaluator<CombatActionOption>.OptionHolder> options) {
      this.options = options;
   }

   public int getMarkedTargetSlot() {
      return this.markedTargetSlot;
   }

   public int getMaxRangeSlot() {
      return this.maxRangeSlot;
   }

   public int getMinRangeSlot() {
      return this.minRangeSlot;
   }

   public int getPositioningAngleSlot() {
      return this.positioningAngleSlot;
   }

   @Nullable
   public String getCurrentAttack() {
      return this.currentBasicAttack != null ? this.currentBasicAttack : this.currentInteraction;
   }

   public float getChargeFor() {
      return this.currentBasicAttack != null ? 0.0F : this.chargeFor;
   }

   public InteractionType getCurrentInteractionType() {
      return this.currentBasicAttack != null ? InteractionType.Primary : this.currentInteractionType;
   }

   public Function<InteractionContext, Map<String, String>> getCurrentInteractionVarsGetter() {
      return this.currentBasicAttack != null ? this.currentBasicAttacksInteractionVarsGetter : this.currentInteractionVarsGetter;
   }

   public boolean shouldDamageFriendlies() {
      return this.currentBasicAttack != null ? this.currentBasicAttackDamageFriendlies : this.currentDamageFriendlies;
   }

   public boolean requiresAiming() {
      return this.currentBasicAttack != null ? true : this.requireAiming;
   }

   public boolean shouldPositionFirst() {
      return this.currentBasicAttack != null ? false : this.positionFirst;
   }

   public double getChargeDistance() {
      return this.currentBasicAttack != null ? 0.0 : this.chargeDistance;
   }

   public void setCurrentInteraction(
      String currentInteraction,
      InteractionType interactionType,
      float chargeFor,
      boolean damageFriendlies,
      boolean requireAiming,
      boolean positionFirst,
      double chargeDistance,
      Function<InteractionContext, Map<String, String>> interactionVarsGetter
   ) {
      this.currentInteraction = currentInteraction;
      this.currentInteractionType = interactionType;
      this.chargeFor = chargeFor;
      this.currentDamageFriendlies = damageFriendlies;
      this.requireAiming = requireAiming;
      this.positionFirst = positionFirst;
      this.currentInteractionVarsGetter = interactionVarsGetter;
      this.chargeDistance = chargeDistance;
   }

   @Nullable
   public CombatActionEvaluator.CombatOptionHolder getCurrentAction() {
      return this.currentAction;
   }

   public double[] consumePostExecutionDistanceRange() {
      double[] distance = this.postExecutionDistanceRange;
      this.postExecutionDistanceRange = null;
      return distance;
   }

   public void setTimeout(float timeout) {
      this.timeout = timeout;
   }

   public void clearTimeout() {
      this.timeout = Float.MAX_VALUE;
   }

   public boolean hasTimedOut(float dt) {
      return this.timeout != Float.MAX_VALUE && (this.timeout -= dt) <= 0.0F;
   }

   public void selectNextCombatAction(
      int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, CommandBuffer<EntityStore> commandBuffer, @Nonnull Role role, ValueStore valueStore
   ) {
      this.evaluationContext.setPredictability(this.predictability);
      this.evaluationContext.setMinimumUtility(this.minActionUtility);
      CombatActionEvaluator.CombatOptionHolder option = (CombatActionEvaluator.CombatOptionHolder)this.evaluate(
         index, archetypeChunk, commandBuffer, this.evaluationContext
      );
      if (option != null) {
         Ref<EntityStore> targetRef = option.getOptionTarget();
         if (targetRef != null && targetRef.isValid()) {
            if (((CombatActionOption)option.getOption()).getActionTarget() == CombatActionOption.Target.Friendly) {
               this.previousTarget = this.primaryTarget;
            }

            this.primaryTarget = targetRef;
            role.getMarkedEntitySupport().setMarkedEntity(this.markedTargetSlot, this.primaryTarget);
         }

         this.currentAction = option;
         ((CombatActionOption)this.currentAction.getOption()).execute(index, archetypeChunk, commandBuffer, role, this, valueStore);
         if (((CombatActionOption)option.getOption()).cancelBasicAttackOnSelect()) {
            this.clearCurrentBasicAttack();
         }
      }
   }

   public void completeCurrentAction(boolean forceClearAbility, boolean clearBasicAttack) {
      if (forceClearAbility || this.currentBasicAttack == null) {
         this.terminateCurrentAction();
         this.lastRunNanos = System.nanoTime();
      }

      if (clearBasicAttack) {
         this.clearCurrentBasicAttack();
      }
   }

   public void terminateCurrentAction() {
      this.currentInteraction = null;
      this.chargeFor = 0.0F;
      if (this.currentAction != null) {
         this.currentAction.setLastUsedNanos(System.nanoTime());
         CombatActionOption option = (CombatActionOption)this.currentAction.getOption();
         if (option.getActionTarget() == CombatActionOption.Target.Friendly) {
            this.primaryTarget = this.previousTarget;
            this.previousTarget = null;
         }

         this.postExecutionDistanceRange = option.getPostExecuteDistanceRange();
         this.currentAction = null;
      }
   }

   public void clearCurrentBasicAttack() {
      if (this.currentBasicAttackSet != null) {
         this.basicAttackCooldown = RandomExtra.randomRange(this.currentBasicAttackSet.getCooldownRange());
      }

      this.currentBasicAttack = null;
      this.basicAttackTarget = null;
   }

   @Override
   public void setupNPC(Role role) {
      ObjectIterator var2 = this.optionsBySubState.values().iterator();

      while (var2.hasNext()) {
         List<Evaluator<CombatActionOption>.OptionHolder> optionList = (List<Evaluator<CombatActionOption>.OptionHolder>)var2.next();

         for (Evaluator<CombatActionOption>.OptionHolder option : optionList) {
            CombatActionOption opt = (CombatActionOption)option.getOption();
            opt.setupNPC(role);
         }
      }
   }

   @Override
   public void setupNPC(Holder<EntityStore> holder) {
      ObjectIterator var2 = this.optionsBySubState.values().iterator();

      while (var2.hasNext()) {
         List<Evaluator<CombatActionOption>.OptionHolder> optionList = (List<Evaluator<CombatActionOption>.OptionHolder>)var2.next();

         for (Evaluator<CombatActionOption>.OptionHolder option : optionList) {
            CombatActionOption opt = (CombatActionOption)option.getOption();
            opt.setupNPC(holder);
         }
      }
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      CombatActionEvaluator evaluator = new CombatActionEvaluator();
      evaluator.options = this.options;
      evaluator.runOption = this.runOption;
      evaluator.minRunUtility = this.minRunUtility;
      evaluator.minActionUtility = this.minActionUtility;
      evaluator.predictability = this.predictability;
      evaluator.runInState = this.runInState;
      evaluator.optionsBySubState.putAll(this.optionsBySubState);
      evaluator.lastRunNanos = this.lastRunNanos;
      evaluator.markedTargetSlot = this.markedTargetSlot;
      evaluator.minRangeSlot = this.minRangeSlot;
      evaluator.maxRangeSlot = this.maxRangeSlot;
      evaluator.positioningAngleSlot = this.positioningAngleSlot;
      evaluator.primaryTarget = this.primaryTarget;
      evaluator.previousTarget = this.previousTarget;
      evaluator.currentAction = this.currentAction;
      evaluator.currentInteraction = this.currentInteraction;
      evaluator.chargeFor = this.chargeFor;
      evaluator.timeout = this.timeout;
      evaluator.basicAttacksBySubState.putAll(this.basicAttacksBySubState);
      evaluator.nextBasicAttackIndex = this.nextBasicAttackIndex;
      evaluator.basicAttackCooldown = this.basicAttackCooldown;
      evaluator.currentBasicAttackSet = this.currentBasicAttackSet;
      evaluator.currentBasicAttack = this.currentBasicAttack;
      evaluator.basicAttackTimeout = this.basicAttackTimeout;
      evaluator.basicAttackTarget = this.basicAttackTarget;
      evaluator.currentBasicAttackSubState = this.currentBasicAttackSubState;
      evaluator.currentInteractionType = this.currentInteractionType;
      evaluator.currentBasicAttackDamageFriendlies = this.currentBasicAttackDamageFriendlies;
      evaluator.currentDamageFriendlies = this.currentDamageFriendlies;
      evaluator.requireAiming = this.requireAiming;
      return evaluator;
   }

   public abstract class CombatOptionHolder extends Evaluator<CombatActionOption>.OptionHolder {
      protected long lastUsedNanos = Evaluator.NOT_USED;

      protected CombatOptionHolder(CombatActionOption option) {
         super(option);
      }

      public void setLastUsedNanos(long lastUsedNanos) {
         this.lastUsedNanos = lastUsedNanos;
      }

      @Nullable
      public Ref<EntityStore> getOptionTarget() {
         return null;
      }
   }

   public class MultipleTargetCombatOptionHolder extends CombatActionEvaluator.CombatOptionHolder {
      protected List<Ref<EntityStore>> targets;
      @Nonnull
      protected final DoubleList targetUtilities = new DoubleArrayList();
      @Nullable
      protected Ref<EntityStore> pickedTarget;

      protected MultipleTargetCombatOptionHolder(CombatActionOption option) {
         super(option);
      }

      @Override
      public double calculateUtility(
         int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, CommandBuffer<EntityStore> commandBuffer, @Nonnull EvaluationContext context
      ) {
         context.setLastUsedNanos(this.lastUsedNanos);
         TargetMemory targetMemory = archetypeChunk.getComponent(index, TargetMemory.getComponentType());

         this.targets = switch (((CombatActionOption)this.option).getActionTarget()) {
            case Self -> throw new IllegalStateException("Self option should not be wrapped in a MultipleTargetCombatOptionHolder!");
            case Hostile -> targetMemory.getKnownHostilesList();
            case Friendly -> targetMemory.getKnownFriendliesList();
         };
         this.targetUtilities.clear();
         this.pickedTarget = null;
         this.utility = 0.0;

         for (int i = 0; i < this.targets.size(); i++) {
            double targetUtility = ((CombatActionOption)this.option).calculateUtility(index, archetypeChunk, this.targets.get(i), commandBuffer, context);
            this.targetUtilities.add(i, targetUtility);
            if (targetUtility > this.utility) {
               this.utility = targetUtility;
               this.pickedTarget = this.targets.get(i);
            }
         }

         return this.utility;
      }

      @Override
      public double getTotalUtility(double threshold) {
         double utility = 0.0;

         for (int i = 0; i < this.targets.size(); i++) {
            double targetUtility = this.targetUtilities.getDouble(i);
            if (targetUtility >= threshold) {
               utility += targetUtility;
            }
         }

         return utility;
      }

      @Override
      public double tryPick(double currentWeight, double threshold) {
         for (int i = 0; i < this.targets.size(); i++) {
            double targetUtility = this.targetUtilities.getDouble(i);
            if (!(targetUtility < threshold)) {
               currentWeight -= targetUtility;
               if (currentWeight <= 0.0) {
                  this.pickedTarget = this.targets.get(i);
                  break;
               }
            }
         }

         return currentWeight;
      }

      @Override
      public Ref<EntityStore> getOptionTarget() {
         return this.pickedTarget;
      }
   }

   public static class RunOption extends Option {
      protected RunOption(String[] conditions) {
         this.conditions = conditions;
      }
   }

   public class SelfCombatOptionHolder extends CombatActionEvaluator.CombatOptionHolder {
      protected SelfCombatOptionHolder(CombatActionOption option) {
         super(option);
      }

      @Override
      public double calculateUtility(
         int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, CommandBuffer<EntityStore> commandBuffer, @Nonnull EvaluationContext context
      ) {
         context.setLastUsedNanos(this.lastUsedNanos);
         return this.utility = ((CombatActionOption)this.option)
            .calculateUtility(index, archetypeChunk, CombatActionEvaluator.this.primaryTarget, commandBuffer, context);
      }
   }
}
