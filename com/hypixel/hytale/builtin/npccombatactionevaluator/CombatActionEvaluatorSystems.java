package com.hypixel.hytale.builtin.npccombatactionevaluator;

import com.hypixel.hytale.builtin.npccombatactionevaluator.config.CombatBalanceAsset;
import com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.CombatActionEvaluator;
import com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.CombatActionEvaluatorConfig;
import com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.combatactions.CombatActionOption;
import com.hypixel.hytale.builtin.npccombatactionevaluator.memory.DamageMemory;
import com.hypixel.hytale.builtin.npccombatactionevaluator.memory.TargetMemory;
import com.hypixel.hytale.builtin.npccombatactionevaluator.memory.TargetMemorySystems;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.config.balancing.BalanceAsset;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.Evaluator;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.systems.BalancingInitialisationSystem;
import com.hypixel.hytale.server.npc.systems.RoleSystems;
import com.hypixel.hytale.server.npc.valuestore.ValueStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CombatActionEvaluatorSystems {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public static class CombatConstructionData implements Component<EntityStore> {
      protected String combatState;
      protected int markedTargetSlot;
      protected int minRangeSlot;
      protected int maxRangeSlot;
      protected int positioningAngleSlot;

      public static ComponentType<EntityStore, CombatActionEvaluatorSystems.CombatConstructionData> getComponentType() {
         return NPCCombatActionEvaluatorPlugin.get().getCombatConstructionDataComponentType();
      }

      public String getCombatState() {
         return this.combatState;
      }

      public void setCombatState(String state) {
         if (this.combatState != null && !this.combatState.equals(state)) {
            throw new IllegalStateException("Cannot have more than one combat state in an NPC!");
         } else {
            this.combatState = state;
         }
      }

      public int getMarkedTargetSlot() {
         return this.markedTargetSlot;
      }

      public void setMarkedTargetSlot(int markedTargetSlot) {
         this.markedTargetSlot = markedTargetSlot;
      }

      public int getMinRangeSlot() {
         return this.minRangeSlot;
      }

      public void setMinRangeSlot(int minRangeSlot) {
         this.minRangeSlot = minRangeSlot;
      }

      public int getMaxRangeSlot() {
         return this.maxRangeSlot;
      }

      public void setMaxRangeSlot(int maxRangeSlot) {
         this.maxRangeSlot = maxRangeSlot;
      }

      public int getPositioningAngleSlot() {
         return this.positioningAngleSlot;
      }

      public void setPositioningAngleSlot(int positioningAngleSlot) {
         this.positioningAngleSlot = positioningAngleSlot;
      }

      @Nonnull
      @Override
      public Component<EntityStore> clone() {
         CombatActionEvaluatorSystems.CombatConstructionData data = new CombatActionEvaluatorSystems.CombatConstructionData();
         data.combatState = this.combatState;
         data.markedTargetSlot = this.markedTargetSlot;
         data.minRangeSlot = this.minRangeSlot;
         data.maxRangeSlot = this.maxRangeSlot;
         data.positioningAngleSlot = this.positioningAngleSlot;
         return data;
      }
   }

   public static class EvaluatorTick extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, CombatActionEvaluator> componentType;
      private final ComponentType<EntityStore, TargetMemory> targetMemoryComponentType;
      private final ComponentType<EntityStore, DamageMemory> damageMemoryComponentType;
      @Nullable
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;
      @Nonnull
      private final ComponentType<EntityStore, Player> playerComponentType;
      private final ComponentType<EntityStore, ValueStore> valueStoreComponentType;
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      private final Query<EntityStore> query;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;

      public EvaluatorTick(
         ComponentType<EntityStore, CombatActionEvaluator> componentType,
         ComponentType<EntityStore, TargetMemory> targetMemoryComponentType,
         ComponentType<EntityStore, DamageMemory> damageMemoryComponentType
      ) {
         this.componentType = componentType;
         this.targetMemoryComponentType = targetMemoryComponentType;
         this.damageMemoryComponentType = damageMemoryComponentType;
         this.npcComponentType = NPCEntity.getComponentType();
         this.playerComponentType = Player.getComponentType();
         this.valueStoreComponentType = ValueStore.getComponentType();
         this.transformComponentType = TransformComponent.getComponentType();
         this.query = Archetype.of(componentType, targetMemoryComponentType, this.npcComponentType, this.valueStoreComponentType, this.transformComponentType);
         this.dependencies = Set.of(
            new SystemDependency<>(Order.BEFORE, RoleSystems.PreBehaviourSupportTickSystem.class),
            new SystemDependency<>(Order.AFTER, TargetMemorySystems.Ticking.class)
         );
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return false;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         NPCEntity npcComponent = archetypeChunk.getComponent(index, this.npcComponentType);

         assert npcComponent != null;

         Role role = npcComponent.getRole();
         if (!archetypeChunk.getArchetype().contains(DeathComponent.getComponentType())) {
            CombatActionEvaluator evaluatorComponent = archetypeChunk.getComponent(index, this.componentType);

            assert evaluatorComponent != null;

            evaluatorComponent.tickBasicAttackCoolDown(dt);
            StateSupport stateSupport = role.getStateSupport();
            int currentState = stateSupport.getStateIndex();
            if (currentState != evaluatorComponent.getRunInState()) {
               if (evaluatorComponent.getCurrentAction() != null) {
                  evaluatorComponent.completeCurrentAction(true, true);
                  evaluatorComponent.clearPrimaryTarget();
                  role.getMarkedEntitySupport().clearMarkedEntity(evaluatorComponent.getMarkedTargetSlot());
                  HytaleLogger.Api context = CombatActionEvaluatorSystems.LOGGER.at(Level.FINEST);
                  if (context.isEnabled()) {
                     context.log("%s: Leaving combat", archetypeChunk.getReferenceTo(index));
                  }
               }

               DamageMemory damageMemory = archetypeChunk.getComponent(index, this.damageMemoryComponentType);
               if (damageMemory != null) {
                  damageMemory.clearTotalDamage();
               }
            } else if (!role.getCombatSupport().isExecutingAttack()) {
               ValueStore valueStoreComponent = archetypeChunk.getComponent(index, this.valueStoreComponentType);

               assert valueStoreComponent != null;

               double[] postExecutionDistanceRange = evaluatorComponent.consumePostExecutionDistanceRange();
               if (postExecutionDistanceRange != null) {
                  valueStoreComponent.storeDouble(evaluatorComponent.getMinRangeSlot(), postExecutionDistanceRange[0]);
                  valueStoreComponent.storeDouble(evaluatorComponent.getMaxRangeSlot(), postExecutionDistanceRange[1]);
               }

               int currentSubState = stateSupport.getSubStateIndex();
               CombatActionEvaluatorConfig.BasicAttacks basicAttacks = evaluatorComponent.getBasicAttacks(currentSubState);
               if (basicAttacks != null) {
                  evaluatorComponent.setCurrentBasicAttackSet(currentSubState, basicAttacks);
                  String currentBasicAttack = evaluatorComponent.getCurrentBasicAttack();
                  if (currentBasicAttack != null) {
                     if (!evaluatorComponent.tickBasicAttackTimeout(dt)) {
                        role.getMarkedEntitySupport().setMarkedEntity(evaluatorComponent.getMarkedTargetSlot(), evaluatorComponent.getBasicAttackTarget());
                        return;
                     }

                     evaluatorComponent.clearCurrentBasicAttack();
                     HytaleLogger.Api context = CombatActionEvaluatorSystems.LOGGER.at(Level.FINEST);
                     if (context.isEnabled()) {
                        context.log("%s: Basic attack timed out", archetypeChunk.getReferenceTo(index));
                     }
                  }

                  if (evaluatorComponent.canUseBasicAttack(index, archetypeChunk, commandBuffer)) {
                     MotionController activeMotionController = role.getActiveMotionController();
                     TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

                     assert transformComponent != null;

                     Vector3d position = transformComponent.getPosition();
                     Ref<EntityStore> targetRef = null;
                     CombatActionEvaluator.CombatOptionHolder currentAction = evaluatorComponent.getCurrentAction();
                     if (currentAction == null || ((CombatActionOption)currentAction.getOption()).getActionTarget() != CombatActionOption.Target.Friendly) {
                        Ref<EntityStore> primaryTargetRef = evaluatorComponent.getPrimaryTarget();
                        if (primaryTargetRef != null && primaryTargetRef.isValid()) {
                           TransformComponent targetTransformComponent = commandBuffer.getComponent(primaryTargetRef, this.transformComponentType);

                           assert targetTransformComponent != null;

                           Vector3d targetPosition = targetTransformComponent.getPosition();
                           if (activeMotionController.getSquaredDistance(position, targetPosition, basicAttacks.shouldUseProjectedDistance())
                              < basicAttacks.getMaxRangeSquared()) {
                              targetRef = primaryTargetRef;
                           }
                        }
                     }

                     if (targetRef == null) {
                        TargetMemory targetMemoryComponent = archetypeChunk.getComponent(index, this.targetMemoryComponentType);

                        assert targetMemoryComponent != null;

                        targetRef = targetMemoryComponent.getClosestHostile();
                     }

                     if (targetRef != null) {
                        TransformComponent targetTransformComponentx = commandBuffer.getComponent(targetRef, this.transformComponentType);

                        assert targetTransformComponentx != null;

                        Vector3d targetPosition = targetTransformComponentx.getPosition();
                        if (activeMotionController.getSquaredDistance(position, targetPosition, basicAttacks.shouldUseProjectedDistance())
                           < basicAttacks.getMaxRangeSquared()) {
                           evaluatorComponent.setBasicAttackTarget(targetRef);
                           role.getMarkedEntitySupport().setMarkedEntity(evaluatorComponent.getMarkedTargetSlot(), targetRef);
                           String[] basicAttackOptions = basicAttacks.getAttacks();
                           String attack;
                           if (basicAttacks.isRandom()) {
                              attack = basicAttackOptions[RandomExtra.randomRange(basicAttackOptions.length)];
                           } else {
                              int nextAttackIndex = evaluatorComponent.getNextBasicAttackIndex();
                              attack = basicAttackOptions[nextAttackIndex];
                              if (++nextAttackIndex >= basicAttackOptions.length) {
                                 nextAttackIndex = 0;
                              }

                              evaluatorComponent.setNextBasicAttackIndex(nextAttackIndex);
                           }

                           evaluatorComponent.setCurrentBasicAttack(attack, basicAttacks.isDamageFriendlies(), basicAttacks::getInteractionVars);
                           evaluatorComponent.setBasicAttackTimeout(basicAttacks.getTimeout());
                           HytaleLogger.Api context = CombatActionEvaluatorSystems.LOGGER.at(Level.FINEST);
                           if (context.isEnabled()) {
                              context.log("%s: Started basic attack %s", archetypeChunk.getReferenceTo(index), attack);
                           }
                        }
                     }
                  }
               } else {
                  evaluatorComponent.setCurrentBasicAttackSet(currentSubState, null);
               }

               CombatActionEvaluator.CombatOptionHolder currentActionx = evaluatorComponent.getCurrentAction();
               if (currentActionx != null) {
                  if (((CombatActionOption)currentActionx.getOption()).getActionTarget() == CombatActionOption.Target.Self) {
                     return;
                  }

                  if (!evaluatorComponent.hasTimedOut(dt)) {
                     Ref<EntityStore> targetRefx = evaluatorComponent.getPrimaryTarget();
                     if (targetRefx != null && targetRefx.isValid() && !commandBuffer.getArchetype(targetRefx).contains(DeathComponent.getComponentType())) {
                        Player targetPlayerComponent = commandBuffer.getComponent(targetRefx, this.playerComponentType);
                        if (targetPlayerComponent == null || targetPlayerComponent.getGameMode() == GameMode.Adventure) {
                           role.getMarkedEntitySupport().setMarkedEntity(evaluatorComponent.getMarkedTargetSlot(), targetRefx);
                           return;
                        }
                     }
                  }

                  evaluatorComponent.terminateCurrentAction();
                  evaluatorComponent.clearPrimaryTarget();
                  role.getMarkedEntitySupport().clearMarkedEntity(evaluatorComponent.getMarkedTargetSlot());
                  HytaleLogger.Api context = CombatActionEvaluatorSystems.LOGGER.at(Level.FINEST);
                  if (context.isEnabled()) {
                     context.log("%s: Lost current action target or timed out", archetypeChunk.getReferenceTo(index));
                  }
               }

               if (evaluatorComponent.getOptionsBySubState().containsKey(currentSubState)) {
                  EvaluationContext evaluationContext = evaluatorComponent.getEvaluationContext();
                  double minRunUtility = evaluatorComponent.getMinRunUtility();
                  evaluationContext.setMinimumUtility(minRunUtility);
                  evaluationContext.setMinimumWeightCoefficient(0.0);
                  evaluationContext.setLastUsedNanos(evaluatorComponent.getLastRunNanos());
                  CombatActionEvaluator.RunOption runOption = evaluatorComponent.getRunOption();
                  double utility = runOption.calculateUtility(index, archetypeChunk, evaluatorComponent.getPrimaryTarget(), commandBuffer, evaluationContext);
                  evaluationContext.reset();
                  if (!(utility < minRunUtility)) {
                     Int2ObjectMap<List<Evaluator<CombatActionOption>.OptionHolder>> optionLists = evaluatorComponent.getOptionsBySubState();
                     List<Evaluator<CombatActionOption>.OptionHolder> currentStateOptions = (List<Evaluator<CombatActionOption>.OptionHolder>)optionLists.get(
                        currentSubState
                     );
                     evaluatorComponent.setActiveOptions(currentStateOptions);
                     evaluatorComponent.selectNextCombatAction(index, archetypeChunk, commandBuffer, role, valueStoreComponent);
                     evaluatorComponent.setLastRunNanos(System.nanoTime());
                     DamageMemory damageMemory = archetypeChunk.getComponent(index, this.damageMemoryComponentType);
                     if (damageMemory != null) {
                        damageMemory.clearRecentDamage();
                     }

                     HytaleLogger.Api context = CombatActionEvaluatorSystems.LOGGER.at(Level.FINEST);
                     if (context.isEnabled()) {
                        context.log("%s: Has run the combat action evaluator", archetypeChunk.getReferenceTo(index));
                     }
                  }
               }
            }
         }
      }
   }

   public static class OnAdded extends HolderSystem<EntityStore> {
      @Nullable
      private final ComponentType<EntityStore, NPCEntity> npcComponentType = NPCEntity.getComponentType();
      private final ComponentType<EntityStore, CombatActionEvaluatorSystems.CombatConstructionData> combatConstructionDataComponentType;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;
      @Nonnull
      private final Query<EntityStore> query;

      public OnAdded(ComponentType<EntityStore, CombatActionEvaluatorSystems.CombatConstructionData> combatConstructionDataComponentType) {
         this.combatConstructionDataComponentType = combatConstructionDataComponentType;
         this.dependencies = Set.of(new SystemDependency<>(Order.AFTER, BalancingInitialisationSystem.class));
         this.query = Query.and(combatConstructionDataComponentType, combatConstructionDataComponentType);
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         NPCEntity npcComponent = holder.getComponent(this.npcComponentType);

         assert npcComponent != null;

         Role role = npcComponent.getRole();
         if (role != null && role.getBalanceAsset() != null) {
            BalanceAsset balancingAsset = BalanceAsset.getAssetMap().getAsset(role.getBalanceAsset());
            if (balancingAsset instanceof CombatBalanceAsset combatBalance) {
               CombatActionEvaluatorSystems.CombatConstructionData constructionData = holder.getComponent(this.combatConstructionDataComponentType);
               CombatActionEvaluator evaluator = new CombatActionEvaluator(role, combatBalance.getEvaluatorConfig(), constructionData);
               evaluator.setupNPC(holder);
               role.getPositionCache().addExternalPositionCacheRegistration(evaluator::setupNPC);
               holder.putComponent(TargetMemory.getComponentType(), new TargetMemory(combatBalance.getTargetMemoryDuration()));
               holder.putComponent(CombatActionEvaluator.getComponentType(), evaluator);
               holder.ensureComponent(InteractionModule.get().getChainingDataComponent());
               holder.removeComponent(this.combatConstructionDataComponentType);
            }
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }
   }
}
