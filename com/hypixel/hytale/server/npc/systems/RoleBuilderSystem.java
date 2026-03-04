package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.common.thread.ticking.Tickable;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ActiveAnimationComponent;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.FromPrefab;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsSystems;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.modules.physics.systems.PhysicsValuesAddSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.EventSlotMapper;
import com.hypixel.hytale.server.npc.blackboard.view.event.block.BlockEventType;
import com.hypixel.hytale.server.npc.blackboard.view.event.entity.EntityEventType;
import com.hypixel.hytale.server.npc.components.FailedSpawnComponent;
import com.hypixel.hytale.server.npc.components.Timers;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.components.messaging.NPCBlockEventSupport;
import com.hypixel.hytale.server.npc.components.messaging.NPCEntityEventSupport;
import com.hypixel.hytale.server.npc.components.messaging.PlayerBlockEventSupport;
import com.hypixel.hytale.server.npc.components.messaging.PlayerEntityEventSupport;
import com.hypixel.hytale.server.npc.decisionmaker.stateevaluator.StateEvaluator;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.SpawnEffect;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.valuestore.ValueStore;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class RoleBuilderSystem extends HolderSystem<EntityStore> {
   @Nonnull
   private final ComponentType<EntityStore, NPCEntity> npcComponentType;
   @Nonnull
   private final ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
   @Nonnull
   private final ComponentType<EntityStore, ModelComponent> modelComponentType = ModelComponent.getComponentType();
   @Nonnull
   private final ComponentType<EntityStore, PersistentModel> persistentModelComponentType = PersistentModel.getComponentType();
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies;
   @Nonnull
   private final Query<EntityStore> query;

   public RoleBuilderSystem() {
      this.npcComponentType = NPCEntity.getComponentType();
      this.dependencies = Set.of(
         new SystemDependency<>(Order.AFTER, EntityStatsSystems.Setup.class), new SystemDependency<>(Order.AFTER, PhysicsValuesAddSystem.class)
      );
      this.query = Archetype.of(this.npcComponentType, this.transformComponentType);
   }

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return this.dependencies;
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }

   @Override
   public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      NPCEntity npcComponent = holder.getComponent(this.npcComponentType);

      assert npcComponent != null;

      if (npcComponent.getRole() == null) {
         NPCPlugin npcPlugin = NPCPlugin.get();
         int roleIndex = npcComponent.getRoleIndex();
         if (roleIndex == Integer.MIN_VALUE) {
            String roleName = npcComponent.getRoleName();
            roleIndex = npcPlugin.getIndex(roleName);
            if (roleIndex < 0) {
               this.fail(holder);
               npcPlugin.getLogger().at(Level.SEVERE).log("Reloading nonexistent role %s!", roleName);
               return;
            }

            if (npcPlugin.tryGetCachedValidRole(roleIndex) == null) {
               this.fail(holder);
               npcPlugin.getLogger().at(Level.SEVERE).log("Reloading invalid role %s!", roleName);
               return;
            }

            npcComponent.setRoleIndex(roleIndex);
         }

         BuilderInfo builderInfo = npcPlugin.prepareRoleBuilderInfo(roleIndex);
         Builder<Role> roleBuilder = (Builder<Role>)builderInfo.getBuilder();
         if (!roleBuilder.isSpawnable()) {
            this.fail(holder);
            npcPlugin.getLogger().at(Level.SEVERE).log("Attempting to spawn un-spawnable (abstract) role: %s", npcComponent.getRoleName());
         } else {
            BuilderSupport builderSupport = new BuilderSupport(npcPlugin.getBuilderManager(), npcComponent, holder, new ExecutionContext(), roleBuilder, null);

            Role role;
            try {
               role = NPCPlugin.buildRole(roleBuilder, builderInfo, builderSupport, roleIndex);
            } catch (SkipSentryException var24) {
               this.fail(holder);
               npcPlugin.getLogger().at(Level.SEVERE).log("Error: %s for NPC %s", var24.getMessage(), npcComponent.getRole());
               return;
            }

            npcComponent.setRole(role);
            if (role.isInvulnerable()) {
               holder.ensureComponent(Invulnerable.getComponentType());
            }

            Message roleNameMessage = Message.translation(role.getNameTranslationKey());
            holder.putComponent(DisplayNameComponent.getComponentType(), new DisplayNameComponent(roleNameMessage));
            Interactions interactionsComponent = holder.ensureAndGetComponent(Interactions.getComponentType());
            interactionsComponent.setInteractionId(InteractionType.Use, "*UseNPC");
            if (role.getDeathInteraction() != null) {
               interactionsComponent.setInteractionId(InteractionType.Death, role.getDeathInteraction());
            }

            Object2IntMap<String> beaconSlotMappings = builderSupport.getBeaconSlotMappings();
            if (beaconSlotMappings != null) {
               BeaconSupport beaconSupport = new BeaconSupport();
               beaconSupport.initialise(beaconSlotMappings);
               holder.putComponent(BeaconSupport.getComponentType(), beaconSupport);
            }

            if (builderSupport.hasBlockEventSupport()) {
               EventSlotMapper<BlockEventType> playerEventSlotMapper = builderSupport.getPlayerBlockEventSlotMapper();
               if (playerEventSlotMapper != null) {
                  PlayerBlockEventSupport playerBlockEventSupport = new PlayerBlockEventSupport();
                  playerBlockEventSupport.initialise(
                     playerEventSlotMapper.getEventSlotMappings(), playerEventSlotMapper.getEventSlotRanges(), playerEventSlotMapper.getEventSlotCount()
                  );
                  holder.putComponent(PlayerBlockEventSupport.getComponentType(), playerBlockEventSupport);
               }

               EventSlotMapper<BlockEventType> npcEventSlotMapper = builderSupport.getNPCBlockEventSlotMapper();
               if (npcEventSlotMapper != null) {
                  NPCBlockEventSupport npcBlockEventSupport = new NPCBlockEventSupport();
                  npcBlockEventSupport.initialise(
                     npcEventSlotMapper.getEventSlotMappings(), npcEventSlotMapper.getEventSlotRanges(), npcEventSlotMapper.getEventSlotCount()
                  );
                  holder.putComponent(NPCBlockEventSupport.getComponentType(), npcBlockEventSupport);
               }

               for (int i = 0; i < BlockEventType.VALUES.length; i++) {
                  BlockEventType type = BlockEventType.VALUES[i];
                  IntSet sets = builderSupport.getBlockChangeSets(type);
                  if (sets != null) {
                     npcComponent.addBlackboardBlockChangeSets(type, sets);
                  }
               }
            }

            if (builderSupport.hasEntityEventSupport()) {
               EventSlotMapper<EntityEventType> playerEventSlotMapperx = builderSupport.getPlayerEntityEventSlotMapper();
               if (playerEventSlotMapperx != null) {
                  PlayerEntityEventSupport playerEntityEventSupport = new PlayerEntityEventSupport();
                  playerEntityEventSupport.initialise(
                     playerEventSlotMapperx.getEventSlotMappings(), playerEventSlotMapperx.getEventSlotRanges(), playerEventSlotMapperx.getEventSlotCount()
                  );
                  holder.putComponent(PlayerEntityEventSupport.getComponentType(), playerEntityEventSupport);
               }

               EventSlotMapper<EntityEventType> npcEventSlotMapper = builderSupport.getNPCEntityEventSlotMapper();
               if (npcEventSlotMapper != null) {
                  NPCEntityEventSupport npcEntityEventSupport = new NPCEntityEventSupport();
                  npcEntityEventSupport.initialise(
                     npcEventSlotMapper.getEventSlotMappings(), npcEventSlotMapper.getEventSlotRanges(), npcEventSlotMapper.getEventSlotCount()
                  );
                  holder.putComponent(NPCEntityEventSupport.getComponentType(), npcEntityEventSupport);
               }

               for (EntityEventType type : EntityEventType.VALUES) {
                  IntSet sets = builderSupport.getEventNPCGroups(type);
                  if (sets != null) {
                     npcComponent.addBlackboardEntityEventSets(type, sets);
                  }
               }
            }

            Tickable[] timers = builderSupport.allocateTimers();
            if (timers != null) {
               holder.putComponent(Timers.getComponentType(), new Timers(timers));
            }

            StateEvaluator stateEvaluator = builderSupport.getStateEvaluator();
            if (stateEvaluator != null) {
               holder.putComponent(StateEvaluator.getComponentType(), stateEvaluator);
            }

            ValueStore.Builder valueStoreBuilder = builderSupport.getValueStoreBuilder();
            if (valueStoreBuilder != null) {
               holder.putComponent(ValueStore.getComponentType(), valueStoreBuilder.build());
            }

            holder.ensureComponent(EffectControllerComponent.getComponentType());
            holder.ensureComponent(ActiveAnimationComponent.getComponentType());
            boolean fromPrefab = holder.getArchetype().contains(FromPrefab.getComponentType());
            boolean spawnedOrPrefab = reason.equals(AddReason.SPAWN) || fromPrefab;
            if (spawnedOrPrefab) {
               ModelComponent modelComponent = holder.getComponent(this.modelComponentType);
               if (modelComponent == null) {
                  String appearance = role.getAppearanceName();
                  if (appearance == null || appearance.isEmpty()) {
                     this.fail(holder);
                     npcPlugin.getLogger().at(Level.SEVERE).log("Appearance can't be initially empty for role %s", npcComponent.getRoleName());
                     return;
                  }

                  ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(appearance);
                  if (modelAsset == null) {
                     this.fail(holder);
                     npcPlugin.getLogger().at(Level.SEVERE).log("Model asset not found: %s for role %s", appearance, npcComponent.getRoleName());
                     return;
                  }

                  float scale = modelAsset.generateRandomScale();
                  npcComponent.setInitialModelScale(scale);
                  Model scaledModel = Model.createScaledModel(modelAsset, scale);
                  holder.putComponent(this.persistentModelComponentType, new PersistentModel(scaledModel.toReference()));
                  holder.putComponent(this.modelComponentType, new ModelComponent(scaledModel));
               }

               role.spawned(holder, npcComponent);
               if (roleBuilder instanceof SpawnEffect spawnEffect) {
                  TransformComponent transformComponent = holder.getComponent(this.transformComponentType);

                  assert transformComponent != null;

                  spawnEffect.spawnEffect(transformComponent.getPosition(), transformComponent.getRotation(), store);
               }
            }
         }
      }
   }

   @Override
   public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
   }

   private void fail(@Nonnull Holder<EntityStore> holder) {
      Archetype<EntityStore> archetype = holder.getArchetype();
      if (archetype != null) {
         for (int i = archetype.getMinIndex(); i < archetype.length(); i++) {
            ComponentType<EntityStore, ? extends Component<EntityStore>> componentType = (ComponentType<EntityStore, ? extends Component<EntityStore>>)archetype.get(
               i
            );
            if (componentType != null) {
               holder.removeComponent(componentType);
            }
         }

         holder.ensureComponent(FailedSpawnComponent.getComponentType());
      }
   }
}
