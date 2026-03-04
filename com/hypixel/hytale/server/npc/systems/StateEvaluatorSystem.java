package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.Evaluator;
import com.hypixel.hytale.server.npc.decisionmaker.stateevaluator.StateEvaluator;
import com.hypixel.hytale.server.npc.decisionmaker.stateevaluator.StateOption;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class StateEvaluatorSystem extends EntityTickingSystem<EntityStore> {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private final ComponentType<EntityStore, StateEvaluator> stateEvaluatorComponent;
   @Nonnull
   private final ComponentType<EntityStore, NPCEntity> npcComponentType;
   @Nonnull
   private final ComponentType<EntityStore, UUIDComponent> uuidComponentType = UUIDComponent.getComponentType();
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies;
   @Nonnull
   private final Query<EntityStore> query;

   public StateEvaluatorSystem(
      @Nonnull ComponentType<EntityStore, StateEvaluator> stateEvaluatorComponent, @Nonnull ComponentType<EntityStore, NPCEntity> npcComponentType
   ) {
      this.stateEvaluatorComponent = stateEvaluatorComponent;
      this.npcComponentType = npcComponentType;
      this.dependencies = Set.of(
         new SystemDependency<>(Order.BEFORE, RoleSystems.BehaviourTickSystem.class),
         new SystemDependency<>(Order.AFTER, RoleSystems.PreBehaviourSupportTickSystem.class)
      );
      this.query = Query.and(npcComponentType, stateEvaluatorComponent, this.uuidComponentType);
   }

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return this.dependencies;
   }

   @Override
   public boolean isParallel(int archetypeChunkSize, int taskCount) {
      return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
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

      UUIDComponent uuidComponent = archetypeChunk.getComponent(index, this.uuidComponentType);

      assert uuidComponent != null;

      Role role = npcComponent.getRole();
      if (role != null) {
         StateSupport stateSupport = role.getStateSupport();
         if (!stateSupport.isRunningTransitionActions()) {
            StateEvaluator stateEvaluator = archetypeChunk.getComponent(index, this.stateEvaluatorComponent);

            assert stateEvaluator != null;

            if (stateEvaluator.isActive() && stateEvaluator.shouldExecute(dt)) {
               HytaleLogger.Api logContext = LOGGER.at(Level.FINE);
               if (logContext.isEnabled()) {
                  logContext.log("%s with uuid %s: Beginning state evaluation", npcComponent.getRoleName(), uuidComponent.getUuid());
               }

               EvaluationContext evaluationContext = stateEvaluator.getEvaluationContext();
               stateEvaluator.prepareEvaluationContext(evaluationContext);
               Evaluator<StateOption>.OptionHolder chosenOption = stateEvaluator.evaluate(index, archetypeChunk, commandBuffer, evaluationContext);
               evaluationContext.reset();
               logContext = LOGGER.at(Level.FINE);
               if (logContext.isEnabled()) {
                  logContext.log("%s with uuid %s: Chose state option %s", npcComponent.getRoleName(), uuidComponent.getUuid(), chosenOption);
               }

               if (chosenOption != null) {
                  StateOption action = (StateOption)chosenOption.getOption();
                  int targetState = action.getStateIndex();
                  int targetSubState = action.getSubStateIndex();
                  if (!stateSupport.inState(targetState) || !stateSupport.inSubState(targetSubState)) {
                     stateSupport.setState(action.getStateIndex(), action.getSubStateIndex(), true, false);
                     logContext = LOGGER.at(Level.FINE);
                     if (logContext.isEnabled()) {
                        logContext.log("%s with uuid %s: Setting state", npcComponent.getRoleName(), uuidComponent.getUuid());
                     }

                     stateEvaluator.onStateSwitched();
                  }
               }
            }
         }
      }
   }
}
