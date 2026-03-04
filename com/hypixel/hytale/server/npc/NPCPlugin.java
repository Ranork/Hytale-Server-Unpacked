package com.hypixel.hytale.server.npc;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.event.RemovedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.builtin.path.path.TransientPathDefinition;
import com.hypixel.hytale.builtin.path.waypoint.RelativeWaypointDefinition;
import com.hypixel.hytale.builtin.tagset.TagSetPlugin;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.common.benchmark.TimeDistributionRecorder;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.spatial.KDTree;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.function.consumer.TriConsumer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.AssetPackRegisterEvent;
import com.hypixel.hytale.server.core.asset.AssetPackUnregisterEvent;
import com.hypixel.hytale.server.core.asset.GenerateSchemaEvent;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.LoadAssetEvent;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.responsecurve.config.ResponseCurve;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsSystems;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.migrations.MigrationModule;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.path.WorldPathChangedEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.flock.FlockPlugin;
import com.hypixel.hytale.server.flock.config.FlockAsset;
import com.hypixel.hytale.server.migrations.RenameSpawnMarkerMigration;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptor;
import com.hypixel.hytale.server.npc.asset.builder.BuilderFactory;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.asset.builder.BuilderManager;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.attitude.AttitudeMap;
import com.hypixel.hytale.server.npc.blackboard.view.attitude.ItemAttitudeMap;
import com.hypixel.hytale.server.npc.blackboard.view.combat.CombatViewSystems;
import com.hypixel.hytale.server.npc.commands.NPCCommand;
import com.hypixel.hytale.server.npc.commands.NPCRunTestsCommand;
import com.hypixel.hytale.server.npc.components.FailedSpawnComponent;
import com.hypixel.hytale.server.npc.components.SortBufferProviderResource;
import com.hypixel.hytale.server.npc.components.StepComponent;
import com.hypixel.hytale.server.npc.components.Timers;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.components.messaging.NPCBlockEventSupport;
import com.hypixel.hytale.server.npc.components.messaging.NPCEntityEventSupport;
import com.hypixel.hytale.server.npc.components.messaging.PlayerBlockEventSupport;
import com.hypixel.hytale.server.npc.components.messaging.PlayerEntityEventSupport;
import com.hypixel.hytale.server.npc.config.AttitudeGroup;
import com.hypixel.hytale.server.npc.config.ItemAttitudeGroup;
import com.hypixel.hytale.server.npc.config.balancing.BalanceAsset;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.ISensorEntityCollector;
import com.hypixel.hytale.server.npc.corecomponents.ISensorEntityPrioritiser;
import com.hypixel.hytale.server.npc.corecomponents.WeightedAction;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders.BuilderActionAppearance;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders.BuilderActionDisplayName;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders.BuilderActionModelAttachment;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders.BuilderActionPlayAnimation;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders.BuilderActionPlaySound;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders.BuilderActionSpawnParticles;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders.BuilderSensorAnimation;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderWeightedAction;
import com.hypixel.hytale.server.npc.corecomponents.combat.builders.BuilderActionApplyEntityEffect;
import com.hypixel.hytale.server.npc.corecomponents.combat.builders.BuilderActionAttack;
import com.hypixel.hytale.server.npc.corecomponents.combat.builders.BuilderBodyMotionAimCharge;
import com.hypixel.hytale.server.npc.corecomponents.combat.builders.BuilderHeadMotionAim;
import com.hypixel.hytale.server.npc.corecomponents.combat.builders.BuilderSensorDamage;
import com.hypixel.hytale.server.npc.corecomponents.combat.builders.BuilderSensorIsBackingAway;
import com.hypixel.hytale.server.npc.corecomponents.debug.builders.BuilderActionLog;
import com.hypixel.hytale.server.npc.corecomponents.debug.builders.BuilderActionTest;
import com.hypixel.hytale.server.npc.corecomponents.debug.builders.BuilderBodyMotionTestProbe;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderActionBeacon;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderActionIgnoreForAvoidance;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderActionNotify;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderActionOverrideAttitude;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderActionReleaseTarget;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderActionSetMarkedTarget;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderActionSetStat;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderHeadMotionWatch;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderSensorBeacon;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderSensorCount;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderSensorEntity;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderSensorKill;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderSensorPlayer;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderSensorSelf;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderSensorTarget;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterAltitude;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterAnd;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterAttitude;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterCombat;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterHeightDifference;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterInsideBlock;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterInventory;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterItemInHand;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterLineOfSight;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterMovementState;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterNPCGroup;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterNot;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterOr;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterSpotsMe;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterStandingOnBlock;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterStat;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders.BuilderEntityFilterViewSector;
import com.hypixel.hytale.server.npc.corecomponents.entity.prioritisers.builders.BuilderSensorEntityPrioritiserAttitude;
import com.hypixel.hytale.server.npc.corecomponents.interaction.builders.BuilderActionLockOnInteractionTarget;
import com.hypixel.hytale.server.npc.corecomponents.interaction.builders.BuilderActionSetInteractable;
import com.hypixel.hytale.server.npc.corecomponents.interaction.builders.BuilderSensorCanInteract;
import com.hypixel.hytale.server.npc.corecomponents.interaction.builders.BuilderSensorHasInteracted;
import com.hypixel.hytale.server.npc.corecomponents.interaction.builders.BuilderSensorInteractionContext;
import com.hypixel.hytale.server.npc.corecomponents.items.builders.BuilderActionDropItem;
import com.hypixel.hytale.server.npc.corecomponents.items.builders.BuilderActionInventory;
import com.hypixel.hytale.server.npc.corecomponents.items.builders.BuilderActionPickUpItem;
import com.hypixel.hytale.server.npc.corecomponents.items.builders.BuilderSensorDroppedItem;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders.BuilderActionDelayDespawn;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders.BuilderActionDespawn;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders.BuilderActionDie;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders.BuilderActionRemove;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders.BuilderActionRole;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders.BuilderActionSpawn;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders.BuilderSensorAge;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderActionCrouch;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderActionOverrideAltitude;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderActionRecomputePath;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionFind;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionLand;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionLeave;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionMaintainDistance;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionMatchLook;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionMoveAway;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionTakeOff;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionTeleport;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionWander;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionWanderInCircle;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionWanderInRect;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderSensorInAir;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderSensorMotionController;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderSensorNav;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderSensorOnGround;
import com.hypixel.hytale.server.npc.corecomponents.statemachine.builders.BuilderActionParentState;
import com.hypixel.hytale.server.npc.corecomponents.statemachine.builders.BuilderActionState;
import com.hypixel.hytale.server.npc.corecomponents.statemachine.builders.BuilderActionToggleStateEvaluator;
import com.hypixel.hytale.server.npc.corecomponents.statemachine.builders.BuilderSensorIsBusy;
import com.hypixel.hytale.server.npc.corecomponents.statemachine.builders.BuilderSensorState;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderActionSetAlarm;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderActionTimerContinue;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderActionTimerModify;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderActionTimerPause;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderActionTimerRestart;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderActionTimerStart;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderActionTimerStop;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderBodyMotionTimer;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderHeadMotionTimer;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderSensorAlarm;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderSensorTimer;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderActionNothing;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderActionRandom;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderActionResetInstructions;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderActionSequence;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderActionSetFlag;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderActionTimeout;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderBodyMotionNothing;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderBodyMotionSequence;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderHeadMotionNothing;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderHeadMotionSequence;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderSensorAdjustPosition;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderSensorAnd;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderSensorAny;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderSensorEval;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderSensorFlag;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderSensorNot;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderSensorOr;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderSensorRandom;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderSensorSwitch;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderSensorValueProviderWrapper;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderValueToParameterMapping;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderActionMakePath;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderActionPlaceBlock;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderActionResetBlockSensors;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderActionResetPath;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderActionResetSearchRays;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderActionSetBlockToPlace;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderActionSetLeashPosition;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderActionStorePosition;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderActionTriggerSpawners;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderBodyMotionPath;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderHeadMotionObserve;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorBlock;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorBlockChange;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorBlockType;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorCanPlace;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorEntityEvent;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorInWater;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorLeash;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorLight;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorPath;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorReadPosition;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorSearchRay;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorTime;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorWeather;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.Condition;
import com.hypixel.hytale.server.npc.decisionmaker.stateevaluator.StateEvaluator;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.instructions.ActionList;
import com.hypixel.hytale.server.npc.instructions.BodyMotion;
import com.hypixel.hytale.server.npc.instructions.HeadMotion;
import com.hypixel.hytale.server.npc.instructions.Instruction;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.instructions.builders.BuilderActionList;
import com.hypixel.hytale.server.npc.instructions.builders.BuilderInstruction;
import com.hypixel.hytale.server.npc.instructions.builders.BuilderInstructionRandomized;
import com.hypixel.hytale.server.npc.instructions.builders.BuilderInstructionReference;
import com.hypixel.hytale.server.npc.interactions.ContextualUseNPCInteraction;
import com.hypixel.hytale.server.npc.interactions.SpawnNPCInteraction;
import com.hypixel.hytale.server.npc.interactions.UseNPCInteraction;
import com.hypixel.hytale.server.npc.movement.controllers.BuilderMotionControllerMapUtil;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.movement.controllers.builders.BuilderMotionControllerDive;
import com.hypixel.hytale.server.npc.movement.controllers.builders.BuilderMotionControllerFly;
import com.hypixel.hytale.server.npc.movement.controllers.builders.BuilderMotionControllerMap;
import com.hypixel.hytale.server.npc.movement.controllers.builders.BuilderMotionControllerWalk;
import com.hypixel.hytale.server.npc.navigation.AStarNodePoolProviderSimple;
import com.hypixel.hytale.server.npc.path.builders.BuilderRelativeWaypointDefinition;
import com.hypixel.hytale.server.npc.path.builders.BuilderTransientPathDefinition;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.builders.BuilderRole;
import com.hypixel.hytale.server.npc.role.builders.BuilderRoleAbstract;
import com.hypixel.hytale.server.npc.role.builders.BuilderRoleVariant;
import com.hypixel.hytale.server.npc.statetransition.StateTransitionController;
import com.hypixel.hytale.server.npc.systems.AvoidanceSystem;
import com.hypixel.hytale.server.npc.systems.BalancingInitialisationSystem;
import com.hypixel.hytale.server.npc.systems.BlackboardSystems;
import com.hypixel.hytale.server.npc.systems.ComputeVelocitySystem;
import com.hypixel.hytale.server.npc.systems.FailedSpawnSystem;
import com.hypixel.hytale.server.npc.systems.MessageSupportSystem;
import com.hypixel.hytale.server.npc.systems.MovementStatesSystem;
import com.hypixel.hytale.server.npc.systems.NPCDamageSystems;
import com.hypixel.hytale.server.npc.systems.NPCDeathSystems;
import com.hypixel.hytale.server.npc.systems.NPCInteractionSystems;
import com.hypixel.hytale.server.npc.systems.NPCPreTickSystem;
import com.hypixel.hytale.server.npc.systems.NPCSpatialSystem;
import com.hypixel.hytale.server.npc.systems.NPCSystems;
import com.hypixel.hytale.server.npc.systems.NPCVelocityInstructionSystem;
import com.hypixel.hytale.server.npc.systems.NewSpawnStartTickingSystem;
import com.hypixel.hytale.server.npc.systems.PositionCacheSystems;
import com.hypixel.hytale.server.npc.systems.RoleBuilderSystem;
import com.hypixel.hytale.server.npc.systems.RoleChangeSystem;
import com.hypixel.hytale.server.npc.systems.RoleSystems;
import com.hypixel.hytale.server.npc.systems.StateEvaluatorSystem;
import com.hypixel.hytale.server.npc.systems.SteeringSystem;
import com.hypixel.hytale.server.npc.systems.StepCleanupSystem;
import com.hypixel.hytale.server.npc.systems.TimerSystem;
import com.hypixel.hytale.server.npc.util.SensorSupportBenchmark;
import com.hypixel.hytale.server.npc.util.expression.StdScope;
import com.hypixel.hytale.server.npc.valuestore.ValueStore;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NPCPlugin extends JavaPlugin {
   @Nonnull
   public static String FACTORY_CLASS_ROLE = "Role";
   @Nonnull
   public static String FACTORY_CLASS_BODY_MOTION = "BodyMotion";
   @Nonnull
   public static String FACTORY_CLASS_HEAD_MOTION = "HeadMotion";
   @Nonnull
   public static String FACTORY_CLASS_ACTION = "Action";
   @Nonnull
   public static String FACTORY_CLASS_SENSOR = "Sensor";
   @Nonnull
   public static String FACTORY_CLASS_INSTRUCTION = "Instruction";
   @Nonnull
   public static String FACTORY_CLASS_TRANSIENT_PATH = "Path";
   @Nonnull
   public static String FACTORY_CLASS_ACTION_LIST = "ActionList";
   @Nonnull
   public static String ROLE_ASSETS_PATH = "Server/NPC/Roles";
   private static NPCPlugin instance;
   protected List<BuilderDescriptor> builderDescriptors;
   protected final BuilderManager builderManager = new BuilderManager();
   protected boolean validateBuilder;
   protected int maxBlackboardBlockCountPerType = 20;
   protected boolean logFailingTestErrors;
   protected String[] presetCoverageTestNPCs;
   @Nonnull
   protected AtomicInteger pathChangeRevision = new AtomicInteger(0);
   @Nonnull
   protected Lock benchmarkLock = new ReentrantLock();
   @Nullable
   protected Int2ObjectMap<TimeDistributionRecorder> roleTickDistribution;
   @Nullable
   protected Int2ObjectMap<SensorSupportBenchmark> roleSensorSupportDistribution;
   @Nullable
   protected TimeDistributionRecorder roleTickDistributionAll;
   @Nullable
   protected SensorSupportBenchmark roleSensorSupportDistributionAll;
   protected boolean autoReload;
   private AttitudeMap attitudeMap;
   private ItemAttitudeMap itemAttitudeMap;
   private static final Vector3f NULL_ROTATION = new Vector3f(0.0F, 0.0F, 0.0F);
   public static final short PRIORITY_LOAD_NPC = -8;
   public static final short PRIORITY_SPAWN_VALIDATION = -7;
   private final Config<NPCPlugin.NPCConfig> config = this.withConfig("NPCModule", NPCPlugin.NPCConfig.CODEC);
   private ResourceType<EntityStore, Blackboard> blackboardResourceType;
   private ResourceType<EntityStore, CombatViewSystems.CombatDataPool> combatDataPoolResourceType;
   private ResourceType<EntityStore, RoleChangeSystem.RoleChangeQueue> roleChangeQueueResourceType;
   private ResourceType<EntityStore, NewSpawnStartTickingSystem.QueueResource> newSpawnStartTickingQueueResourceType;
   private ResourceType<EntityStore, SortBufferProviderResource> sortBufferProviderResourceResourceType;
   private ResourceType<EntityStore, AStarNodePoolProviderSimple> aStarNodePoolProviderSimpleResourceType;
   private ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> npcSpatialResource;
   private ComponentType<EntityStore, CombatViewSystems.CombatData> combatDataComponentType;
   private ComponentType<EntityStore, NPCRunTestsCommand.NPCTestData> npcTestDataComponentType;
   private ComponentType<EntityStore, BeaconSupport> beaconSupportComponentType;
   private ComponentType<EntityStore, NPCBlockEventSupport> npcBlockEventSupportComponentType;
   private ComponentType<EntityStore, PlayerBlockEventSupport> playerBlockEventSupportComponentType;
   private ComponentType<EntityStore, NPCEntityEventSupport> npcEntityEventSupportComponentType;
   private ComponentType<EntityStore, PlayerEntityEventSupport> playerEntityEventSupportComponentType;
   private ComponentType<EntityStore, StepComponent> stepComponentType;
   private ComponentType<EntityStore, FailedSpawnComponent> failedSpawnComponentType;
   private ComponentType<EntityStore, Timers> timersComponentType;
   private ComponentType<EntityStore, StateEvaluator> stateEvaluatorComponentType;
   private ComponentType<EntityStore, ValueStore> valueStoreComponentType;

   public static NPCPlugin get() {
      return instance;
   }

   public NPCPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      instance = this;
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      EventRegistry eventRegistry = this.getEventRegistry();
      this.getCommandRegistry().registerCommand(new NPCCommand());
      eventRegistry.register(LoadedAssetsEvent.class, ModelAsset.class, this::onModelsChanged);
      eventRegistry.register(LoadedAssetsEvent.class, NPCGroup.class, this::onNPCGroupsLoaded);
      eventRegistry.register(RemovedAssetsEvent.class, NPCGroup.class, this::onNPCGroupsRemoved);
      eventRegistry.register(LoadedAssetsEvent.class, AttitudeGroup.class, this::onAttitudeGroupsLoaded);
      eventRegistry.register(RemovedAssetsEvent.class, AttitudeGroup.class, this::onAttitudeGroupsRemoved);
      eventRegistry.register(LoadedAssetsEvent.class, ItemAttitudeGroup.class, this::onItemAttitudeGroupsLoaded);
      eventRegistry.register(RemovedAssetsEvent.class, ItemAttitudeGroup.class, this::onItemAttitudeGroupsRemoved);
      eventRegistry.register(LoadedAssetsEvent.class, BalanceAsset.class, NPCPlugin::onBalanceAssetsChanged);
      eventRegistry.register(RemovedAssetsEvent.class, BalanceAsset.class, NPCPlugin::onBalanceAssetsRemoved);
      eventRegistry.register(WorldPathChangedEvent.class, this::onPathChange);
      eventRegistry.register(AllNPCsLoadedEvent.class, this::onNPCsLoaded);
      eventRegistry.register(
         (short)-8,
         LoadAssetEvent.class,
         event -> {
            HytaleLogger.getLogger().at(Level.INFO).log("Loading NPC assets phase...");
            long start = System.nanoTime();
            this.builderManager.setAutoReload(this.autoReload);
            boolean validateAssets = Options.getOptionSet().has(Options.VALIDATE_ASSETS);
            List<AssetPack> assetPacks = AssetModule.get().getAssetPacks();

            for (int i = 0; i < assetPacks.size(); i++) {
               boolean includeTests = i == 0;
               boolean loadSucceeded = this.builderManager.loadBuilders(assetPacks.get(i), includeTests);
               if (!loadSucceeded) {
                  event.failed(validateAssets, "failed to validate npc's");
               }
            }

            HytaleLogger.getLogger()
               .at(Level.INFO)
               .log(
                  "Loading NPC assets phase completed! Boot time %s, Took %s",
                  FormatUtil.nanosToString(System.nanoTime() - event.getBootStart()),
                  FormatUtil.nanosToString(System.nanoTime() - start)
               );
         }
      );
      eventRegistry.register(AssetPackRegisterEvent.class, event -> this.builderManager.loadBuilders(event.getAssetPack(), false));
      eventRegistry.register(AssetPackUnregisterEvent.class, event -> this.builderManager.unloadBuilders(event.getAssetPack()));
      eventRegistry.register(GenerateSchemaEvent.class, this::onSchemaGenerate);
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              AttitudeGroup.class, new IndexedLookupTableAssetMap<>(AttitudeGroup[]::new)
                           )
                           .setPath("NPC/Attitude/Roles"))
                        .setCodec(AttitudeGroup.CODEC))
                     .setKeyFunction(AttitudeGroup::getId))
                  .setReplaceOnRemove(AttitudeGroup::new))
               .loadsAfter(NPCGroup.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              ItemAttitudeGroup.class, new IndexedLookupTableAssetMap<>(ItemAttitudeGroup[]::new)
                           )
                           .setPath("NPC/Attitude/Items"))
                        .setCodec(ItemAttitudeGroup.CODEC))
                     .setKeyFunction(ItemAttitudeGroup::getId))
                  .setReplaceOnRemove(ItemAttitudeGroup::new))
               .loadsAfter(Item.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                           BalanceAsset.class, new DefaultAssetMap()
                        )
                        .setPath("NPC/Balancing"))
                     .setCodec(BalanceAsset.CODEC))
                  .setKeyFunction(BalanceAsset::getId))
               .loadsAfter(Condition.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              Condition.class, new IndexedLookupTableAssetMap<>(Condition[]::new)
                           )
                           .setPath("NPC/DecisionMaking/Conditions"))
                        .setCodec(Condition.CODEC))
                     .setKeyFunction(Condition::getId))
                  .setReplaceOnRemove(Condition::getAlwaysTrueFor))
               .loadsAfter(ResponseCurve.class, NPCGroup.class, EntityStatType.class))
            .build()
      );
      this.getEntityRegistry().registerEntity("NPC", NPCEntity.class, NPCEntity::new, NPCEntity.CODEC);
      Interaction.CODEC.register("ContextualUseNPC", ContextualUseNPCInteraction.class, ContextualUseNPCInteraction.CODEC);
      Interaction.CODEC.register("UseNPC", UseNPCInteraction.class, UseNPCInteraction.CODEC);
      Interaction.CODEC.register("SpawnNPC", SpawnNPCInteraction.class, SpawnNPCInteraction.CODEC);
      Interaction.getAssetStore().loadAssets("Hytale:Hytale", List.of(new UseNPCInteraction("*UseNPC")));
      RootInteraction.getAssetStore().loadAssets("Hytale:Hytale", List.of(UseNPCInteraction.DEFAULT_ROOT));
      MigrationModule.get().register("spawnMarkers", RenameSpawnMarkerMigration::new);
      this.setupNPCLoading();
      this.blackboardResourceType = entityStoreRegistry.registerResource(Blackboard.class, Blackboard::new);
      this.combatDataPoolResourceType = entityStoreRegistry.registerResource(CombatViewSystems.CombatDataPool.class, CombatViewSystems.CombatDataPool::new);
      this.roleChangeQueueResourceType = entityStoreRegistry.registerResource(RoleChangeSystem.RoleChangeQueue.class, RoleChangeSystem.RoleChangeQueue::new);
      this.newSpawnStartTickingQueueResourceType = entityStoreRegistry.registerResource(
         NewSpawnStartTickingSystem.QueueResource.class, NewSpawnStartTickingSystem.QueueResource::new
      );
      this.sortBufferProviderResourceResourceType = entityStoreRegistry.registerResource(SortBufferProviderResource.class, SortBufferProviderResource::new);
      this.aStarNodePoolProviderSimpleResourceType = entityStoreRegistry.registerResource(AStarNodePoolProviderSimple.class, AStarNodePoolProviderSimple::new);
      this.npcSpatialResource = entityStoreRegistry.registerSpatialResource(() -> new KDTree<>(Ref::isValid));
      this.combatDataComponentType = entityStoreRegistry.registerComponent(CombatViewSystems.CombatData.class, CombatViewSystems.CombatData::new);
      this.npcTestDataComponentType = entityStoreRegistry.registerComponent(NPCRunTestsCommand.NPCTestData.class, NPCRunTestsCommand.NPCTestData::new);
      this.beaconSupportComponentType = entityStoreRegistry.registerComponent(BeaconSupport.class, BeaconSupport::new);
      this.npcBlockEventSupportComponentType = entityStoreRegistry.registerComponent(NPCBlockEventSupport.class, NPCBlockEventSupport::new);
      this.playerBlockEventSupportComponentType = entityStoreRegistry.registerComponent(PlayerBlockEventSupport.class, PlayerBlockEventSupport::new);
      this.npcEntityEventSupportComponentType = entityStoreRegistry.registerComponent(NPCEntityEventSupport.class, NPCEntityEventSupport::new);
      this.playerEntityEventSupportComponentType = entityStoreRegistry.registerComponent(PlayerEntityEventSupport.class, PlayerEntityEventSupport::new);
      this.stepComponentType = entityStoreRegistry.registerComponent(StepComponent.class, () -> {
         throw new UnsupportedOperationException("Not implemented");
      });
      this.failedSpawnComponentType = entityStoreRegistry.registerComponent(FailedSpawnComponent.class, FailedSpawnComponent::new);
      this.timersComponentType = entityStoreRegistry.registerComponent(Timers.class, () -> {
         throw new UnsupportedOperationException("Not implemented");
      });
      this.stateEvaluatorComponentType = entityStoreRegistry.registerComponent(StateEvaluator.class, () -> {
         throw new UnsupportedOperationException("Not implemented");
      });
      this.valueStoreComponentType = entityStoreRegistry.registerComponent(ValueStore.class, () -> {
         throw new UnsupportedOperationException("Not implemented");
      });
      ComponentType<EntityStore, NPCEntity> npcComponentType = NPCEntity.getComponentType();
      entityStoreRegistry.registerSystem(new BlackboardSystems.InitSystem(this.blackboardResourceType));
      entityStoreRegistry.registerSystem(new BlackboardSystems.TickingSystem(this.blackboardResourceType));
      entityStoreRegistry.registerSystem(new BlackboardSystems.DamageBlockEventSystem());
      entityStoreRegistry.registerSystem(new BlackboardSystems.BreakBlockEventSystem());
      entityStoreRegistry.registerSystem(new CombatViewSystems.Ensure(this.combatDataComponentType));
      entityStoreRegistry.registerSystem(new CombatViewSystems.EntityRemoved(this.combatDataComponentType, this.combatDataPoolResourceType));
      entityStoreRegistry.registerSystem(new CombatViewSystems.Ticking(this.combatDataComponentType, this.combatDataPoolResourceType));
      entityStoreRegistry.registerSystem(new NPCSystems.ModelChangeSystem());
      entityStoreRegistry.registerSystem(new RoleBuilderSystem());
      entityStoreRegistry.registerSystem(new BalancingInitialisationSystem());
      entityStoreRegistry.registerSystem(new RoleSystems.RoleActivateSystem(npcComponentType));
      entityStoreRegistry.registerSystem(new PositionCacheSystems.RoleActivateSystem(npcComponentType, this.stateEvaluatorComponentType));
      entityStoreRegistry.registerSystem(new NPCInteractionSystems.AddSimulationManagerSystem(npcComponentType));
      entityStoreRegistry.registerSystem(new NPCInteractionSystems.TickHeldInteractionsSystem(npcComponentType));
      entityStoreRegistry.registerSystem(new FailedSpawnSystem());
      entityStoreRegistry.registerSystem(new NPCSystems.AddedSystem(npcComponentType));
      entityStoreRegistry.registerSystem(new NPCSystems.AddedFromExternalSystem(npcComponentType));
      entityStoreRegistry.registerSystem(new NPCSystems.AddedFromWorldGenSystem());
      entityStoreRegistry.registerSystem(new NPCSystems.AddSpawnEntityEffectSystem(npcComponentType));
      entityStoreRegistry.registerSystem(new RoleSystems.BehaviourTickSystem(npcComponentType, this.stepComponentType));
      entityStoreRegistry.registerSystem(new RoleSystems.PreBehaviourSupportTickSystem(npcComponentType));
      entityStoreRegistry.registerSystem(new StateEvaluatorSystem(this.stateEvaluatorComponentType, npcComponentType));
      entityStoreRegistry.registerSystem(new PositionCacheSystems.UpdateSystem(npcComponentType, this.npcSpatialResource));
      entityStoreRegistry.registerSystem(new NPCPreTickSystem(npcComponentType));
      Set<Dependency<EntityStore>> postBehaviourDependency = Set.of(new SystemDependency<>(Order.AFTER, RoleSystems.PostBehaviourSupportTickSystem.class));
      entityStoreRegistry.registerSystem(new AvoidanceSystem(npcComponentType));
      entityStoreRegistry.registerSystem(new SteeringSystem(npcComponentType));
      entityStoreRegistry.registerSystem(new RoleSystems.PostBehaviourSupportTickSystem(npcComponentType));
      entityStoreRegistry.registerSystem(new RoleSystems.RoleDebugSystem(npcComponentType, postBehaviourDependency));
      entityStoreRegistry.registerSystem(new TimerSystem(this.timersComponentType, postBehaviourDependency));
      entityStoreRegistry.registerSystem(new ComputeVelocitySystem(npcComponentType, EntityModule.get().getVelocityComponentType(), postBehaviourDependency));
      entityStoreRegistry.registerSystem(
         new MovementStatesSystem(npcComponentType, EntityModule.get().getVelocityComponentType(), EntityModule.get().getMovementStatesComponentType())
      );
      entityStoreRegistry.registerSystem(new MessageSupportSystem.BeaconSystem(this.beaconSupportComponentType, postBehaviourDependency));
      entityStoreRegistry.registerSystem(new MessageSupportSystem.NPCBlockEventSystem(this.npcBlockEventSupportComponentType, postBehaviourDependency));
      entityStoreRegistry.registerSystem(new MessageSupportSystem.PlayerBlockEventSystem(this.playerBlockEventSupportComponentType, postBehaviourDependency));
      entityStoreRegistry.registerSystem(new MessageSupportSystem.NPCEntityEventSystem(this.npcEntityEventSupportComponentType, postBehaviourDependency));
      entityStoreRegistry.registerSystem(new MessageSupportSystem.PlayerEntityEventSystem(this.playerEntityEventSupportComponentType, postBehaviourDependency));
      entityStoreRegistry.registerSystem(new StepCleanupSystem(this.stepComponentType));
      entityStoreRegistry.registerSystem(new NewSpawnStartTickingSystem(this.newSpawnStartTickingQueueResourceType));
      entityStoreRegistry.registerSystem(
         new RoleChangeSystem(
            this.roleChangeQueueResourceType,
            this.beaconSupportComponentType,
            this.playerBlockEventSupportComponentType,
            this.npcBlockEventSupportComponentType,
            this.playerEntityEventSupportComponentType,
            this.npcEntityEventSupportComponentType,
            this.timersComponentType,
            this.stateEvaluatorComponentType,
            this.valueStoreComponentType
         )
      );
      entityStoreRegistry.registerSystem(new NPCSpatialSystem(this.npcSpatialResource));
      entityStoreRegistry.registerSystem(new NPCDeathSystems.NPCKillsEntitySystem());
      entityStoreRegistry.registerSystem(new NPCDeathSystems.EntityViewSystem());
      entityStoreRegistry.registerSystem(new NPCDamageSystems.FilterDamageSystem());
      entityStoreRegistry.registerSystem(new NPCDamageSystems.DamageReceivedSystem());
      entityStoreRegistry.registerSystem(new NPCDamageSystems.DamageDealtSystem());
      entityStoreRegistry.registerSystem(new NPCDamageSystems.DamageReceivedEventViewSystem());
      entityStoreRegistry.registerSystem(new NPCDamageSystems.DropDeathItems());
      entityStoreRegistry.registerSystem(new NPCSystems.OnTeleportSystem());
      entityStoreRegistry.registerSystem(new NPCSystems.OnDeathSystem());
      entityStoreRegistry.registerSystem(new NPCSystems.LegacyWorldGenId());
      entityStoreRegistry.registerSystem(new NPCSystems.KillFeedKillerEventSystem());
      entityStoreRegistry.registerSystem(new NPCSystems.KillFeedDecedentEventSystem());
      entityStoreRegistry.registerSystem(new NPCSystems.PrefabPlaceEntityEventSystem());
      entityStoreRegistry.registerSystem(new NPCVelocityInstructionSystem());
      this.getEntityStoreRegistry().registerSystem(new NPCPlugin.NPCEntityRegenerateStatsSystem());
   }

   public void onSchemaGenerate(@Nonnull GenerateSchemaEvent event) {
      Schema schema = this.builderManager.generateSchema(event.getContext());
      event.addSchema("NPCRole.json", schema);
      event.addSchemaLink("NPCRole", List.of("NPC/Roles/*.json", "NPC/Roles/**/*.json"), null);
      Schema.HytaleMetadata hytale = schema.getHytale();
      hytale.setPath("NPC/Roles");
      hytale.setExtension(".json");
      schema.setId("NPCRole.json");
      schema.setTitle("NPCRole");
   }

   @Override
   protected void start() {
      NPCPlugin.NPCConfig config = this.config.get();
      if (config.isGenerateDescriptors()) {
         this.generateDescriptors();
         if (config.isGenerateDescriptorsFile()) {
            this.saveDescriptors();
         }
      }
   }

   public ResourceType<EntityStore, Blackboard> getBlackboardResourceType() {
      return this.blackboardResourceType;
   }

   public ResourceType<EntityStore, CombatViewSystems.CombatDataPool> getCombatDataPoolResourceType() {
      return this.combatDataPoolResourceType;
   }

   public ResourceType<EntityStore, RoleChangeSystem.RoleChangeQueue> getRoleChangeQueueResourceType() {
      return this.roleChangeQueueResourceType;
   }

   public ResourceType<EntityStore, NewSpawnStartTickingSystem.QueueResource> getNewSpawnStartTickingQueueResourceType() {
      return this.newSpawnStartTickingQueueResourceType;
   }

   public ResourceType<EntityStore, SortBufferProviderResource> getSortBufferProviderResourceResourceType() {
      return this.sortBufferProviderResourceResourceType;
   }

   public ResourceType<EntityStore, AStarNodePoolProviderSimple> getAStarNodePoolProviderSimpleResourceType() {
      return this.aStarNodePoolProviderSimpleResourceType;
   }

   public ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> getNpcSpatialResource() {
      return this.npcSpatialResource;
   }

   public ComponentType<EntityStore, CombatViewSystems.CombatData> getCombatDataComponentType() {
      return this.combatDataComponentType;
   }

   public ComponentType<EntityStore, NPCRunTestsCommand.NPCTestData> getNpcTestDataComponentType() {
      return this.npcTestDataComponentType;
   }

   public ComponentType<EntityStore, BeaconSupport> getBeaconSupportComponentType() {
      return this.beaconSupportComponentType;
   }

   public ComponentType<EntityStore, NPCBlockEventSupport> getNpcBlockEventSupportComponentType() {
      return this.npcBlockEventSupportComponentType;
   }

   public ComponentType<EntityStore, PlayerBlockEventSupport> getPlayerBlockEventSupportComponentType() {
      return this.playerBlockEventSupportComponentType;
   }

   public ComponentType<EntityStore, NPCEntityEventSupport> getNpcEntityEventSupportComponentType() {
      return this.npcEntityEventSupportComponentType;
   }

   public ComponentType<EntityStore, PlayerEntityEventSupport> getPlayerEntityEventSupportComponentType() {
      return this.playerEntityEventSupportComponentType;
   }

   public ComponentType<EntityStore, StepComponent> getStepComponentType() {
      return this.stepComponentType;
   }

   public ComponentType<EntityStore, FailedSpawnComponent> getFailedSpawnComponentType() {
      return this.failedSpawnComponentType;
   }

   public ComponentType<EntityStore, Timers> getTimersComponentType() {
      return this.timersComponentType;
   }

   public ComponentType<EntityStore, StateEvaluator> getStateEvaluatorComponentType() {
      return this.stateEvaluatorComponentType;
   }

   public ComponentType<EntityStore, ValueStore> getValueStoreComponentType() {
      return this.valueStoreComponentType;
   }

   public void setupNPCLoading() {
      this.builderManager.addCategory(FACTORY_CLASS_ROLE, Role.class);
      this.builderManager.addCategory(FACTORY_CLASS_BODY_MOTION, BodyMotion.class);
      this.builderManager.addCategory(FACTORY_CLASS_HEAD_MOTION, HeadMotion.class);
      this.builderManager.addCategory(FACTORY_CLASS_ACTION, Action.class);
      this.builderManager.addCategory(FACTORY_CLASS_SENSOR, Sensor.class);
      this.builderManager.addCategory(FACTORY_CLASS_INSTRUCTION, Instruction.class);
      this.builderManager.addCategory(FACTORY_CLASS_TRANSIENT_PATH, TransientPathDefinition.class);
      this.builderManager.addCategory(FACTORY_CLASS_ACTION_LIST, ActionList.class);
      this.registerCoreFactories();
      this.registerCoreComponentType("Nothing", BuilderBodyMotionNothing::new)
         .registerCoreComponentType("Wander", BuilderBodyMotionWander::new)
         .registerCoreComponentType("WanderInCircle", BuilderBodyMotionWanderInCircle::new)
         .registerCoreComponentType("WanderInRect", BuilderBodyMotionWanderInRect::new)
         .registerCoreComponentType("Timer", BuilderBodyMotionTimer::new)
         .registerCoreComponentType("Sequence", BuilderBodyMotionSequence::new)
         .registerCoreComponentType("Flee", BuilderBodyMotionMoveAway::new)
         .registerCoreComponentType("Seek", BuilderBodyMotionFind::new)
         .registerCoreComponentType("Leave", BuilderBodyMotionLeave::new)
         .registerCoreComponentType("Path", BuilderBodyMotionPath::new)
         .registerCoreComponentType("TakeOff", BuilderBodyMotionTakeOff::new)
         .registerCoreComponentType("TestProbe", BuilderBodyMotionTestProbe::new)
         .registerCoreComponentType("Teleport", BuilderBodyMotionTeleport::new)
         .registerCoreComponentType("Land", BuilderBodyMotionLand::new)
         .registerCoreComponentType("MatchLook", BuilderBodyMotionMatchLook::new)
         .registerCoreComponentType("MaintainDistance", BuilderBodyMotionMaintainDistance::new)
         .registerCoreComponentType("AimCharge", BuilderBodyMotionAimCharge::new);
      this.registerCoreComponentType("Aim", BuilderHeadMotionAim::new)
         .registerCoreComponentType("Watch", BuilderHeadMotionWatch::new)
         .registerCoreComponentType("Observe", BuilderHeadMotionObserve::new)
         .registerCoreComponentType("Sequence", BuilderHeadMotionSequence::new)
         .registerCoreComponentType("Timer", BuilderHeadMotionTimer::new)
         .registerCoreComponentType("Nothing", BuilderHeadMotionNothing::new);
      this.registerCoreComponentType("Appearance", BuilderActionAppearance::new)
         .registerCoreComponentType("Timeout", BuilderActionTimeout::new)
         .registerCoreComponentType("Spawn", BuilderActionSpawn::new)
         .registerCoreComponentType("Nothing", BuilderActionNothing::new)
         .registerCoreComponentType("Attack", BuilderActionAttack::new)
         .registerCoreComponentType("State", BuilderActionState::new)
         .registerCoreComponentType("ReleaseTarget", BuilderActionReleaseTarget::new)
         .registerCoreComponentType("SetMarkedTarget", BuilderActionSetMarkedTarget::new)
         .registerCoreComponentType("Inventory", BuilderActionInventory::new)
         .registerCoreComponentType("DisplayName", BuilderActionDisplayName::new)
         .registerCoreComponentType("Sequence", BuilderActionSequence::new)
         .registerCoreComponentType("Random", BuilderActionRandom::new)
         .registerCoreComponentType("Beacon", BuilderActionBeacon::new)
         .registerCoreComponentType("SetLeashPosition", BuilderActionSetLeashPosition::new)
         .registerCoreComponentType("PlaySound", BuilderActionPlaySound::new)
         .registerCoreComponentType("Despawn", BuilderActionDespawn::new)
         .registerCoreComponentType("PlayAnimation", BuilderActionPlayAnimation::new)
         .registerCoreComponentType("DelayDespawn", BuilderActionDelayDespawn::new)
         .registerCoreComponentType("SpawnParticles", BuilderActionSpawnParticles::new)
         .registerCoreComponentType("Crouch", BuilderActionCrouch::new)
         .registerCoreComponentType("TimerStart", BuilderActionTimerStart::new)
         .registerCoreComponentType("TimerContinue", BuilderActionTimerContinue::new)
         .registerCoreComponentType("TimerPause", BuilderActionTimerPause::new)
         .registerCoreComponentType("TimerModify", BuilderActionTimerModify::new)
         .registerCoreComponentType("TimerStop", BuilderActionTimerStop::new)
         .registerCoreComponentType("TimerRestart", BuilderActionTimerRestart::new)
         .registerCoreComponentType("Test", BuilderActionTest::new)
         .registerCoreComponentType("Log", BuilderActionLog::new)
         .registerCoreComponentType("Role", BuilderActionRole::new)
         .registerCoreComponentType("SetFlag", BuilderActionSetFlag::new)
         .registerCoreComponentType("DropItem", BuilderActionDropItem::new)
         .registerCoreComponentType("PickUpItem", BuilderActionPickUpItem::new)
         .registerCoreComponentType("ResetInstructions", BuilderActionResetInstructions::new)
         .registerCoreComponentType("ParentState", BuilderActionParentState::new)
         .registerCoreComponentType("Notify", BuilderActionNotify::new)
         .registerCoreComponentType("TriggerSpawners", BuilderActionTriggerSpawners::new)
         .registerCoreComponentType("ResetBlockSensors", BuilderActionResetBlockSensors::new)
         .registerCoreComponentType("MakePath", BuilderActionMakePath::new)
         .registerCoreComponentType("OverrideAttitude", BuilderActionOverrideAttitude::new)
         .registerCoreComponentType("SetInteractable", BuilderActionSetInteractable::new)
         .registerCoreComponentType("LockOnInteractionTarget", BuilderActionLockOnInteractionTarget::new)
         .registerCoreComponentType("StorePosition", BuilderActionStorePosition::new)
         .registerCoreComponentType("SetBlockToPlace", BuilderActionSetBlockToPlace::new)
         .registerCoreComponentType("PlaceBlock", BuilderActionPlaceBlock::new)
         .registerCoreComponentType("RecomputePath", BuilderActionRecomputePath::new)
         .registerCoreComponentType("IgnoreForAvoidance", BuilderActionIgnoreForAvoidance::new)
         .registerCoreComponentType("ModelAttachment", BuilderActionModelAttachment::new)
         .registerCoreComponentType("SetAlarm", BuilderActionSetAlarm::new)
         .registerCoreComponentType("ToggleStateEvaluator", BuilderActionToggleStateEvaluator::new)
         .registerCoreComponentType("OverrideAltitude", BuilderActionOverrideAltitude::new)
         .registerCoreComponentType("ResetSearchRays", BuilderActionResetSearchRays::new)
         .registerCoreComponentType("Die", BuilderActionDie::new)
         .registerCoreComponentType("Remove", BuilderActionRemove::new)
         .registerCoreComponentType("ApplyEntityEffect", BuilderActionApplyEntityEffect::new)
         .registerCoreComponentType("ResetPath", BuilderActionResetPath::new)
         .registerCoreComponentType("SetStat", BuilderActionSetStat::new);
      this.registerCoreComponentType("Any", BuilderSensorAny::new)
         .registerCoreComponentType("And", BuilderSensorAnd::new)
         .registerCoreComponentType("Or", BuilderSensorOr::new)
         .registerCoreComponentType("Not", BuilderSensorNot::new)
         .registerCoreComponentType("Player", BuilderSensorPlayer::new)
         .registerCoreComponentType("Mob", BuilderSensorEntity::new)
         .registerCoreComponentType("State", BuilderSensorState::new)
         .registerCoreComponentType("InAir", BuilderSensorInAir::new)
         .registerCoreComponentType("OnGround", BuilderSensorOnGround::new)
         .registerCoreComponentType("Eval", BuilderSensorEval::new)
         .registerCoreComponentType("Damage", BuilderSensorDamage::new)
         .registerCoreComponentType("IsBackingAway", BuilderSensorIsBackingAway::new)
         .registerCoreComponentType("Kill", BuilderSensorKill::new)
         .registerCoreComponentType("Beacon", BuilderSensorBeacon::new)
         .registerCoreComponentType("MotionController", BuilderSensorMotionController::new)
         .registerCoreComponentType("Leash", BuilderSensorLeash::new)
         .registerCoreComponentType("Time", BuilderSensorTime::new)
         .registerCoreComponentType("Count", BuilderSensorCount::new)
         .registerCoreComponentType("Target", BuilderSensorTarget::new)
         .registerCoreComponentType("Timer", BuilderSensorTimer::new)
         .registerCoreComponentType("Switch", BuilderSensorSwitch::new)
         .registerCoreComponentType("Light", BuilderSensorLight::new)
         .registerCoreComponentType("Age", BuilderSensorAge::new)
         .registerCoreComponentType("Flag", BuilderSensorFlag::new)
         .registerCoreComponentType("DroppedItem", BuilderSensorDroppedItem::new)
         .registerCoreComponentType("Path", BuilderSensorPath::new)
         .registerCoreComponentType("Weather", BuilderSensorWeather::new)
         .registerCoreComponentType("Block", BuilderSensorBlock::new)
         .registerCoreComponentType("BlockChange", BuilderSensorBlockChange::new)
         .registerCoreComponentType("EntityEvent", BuilderSensorEntityEvent::new)
         .registerCoreComponentType("Random", BuilderSensorRandom::new)
         .registerCoreComponentType("CanInteract", BuilderSensorCanInteract::new)
         .registerCoreComponentType("HasInteracted", BuilderSensorHasInteracted::new)
         .registerCoreComponentType("ReadPosition", BuilderSensorReadPosition::new)
         .registerCoreComponentType("Animation", BuilderSensorAnimation::new)
         .registerCoreComponentType("CanPlaceBlock", BuilderSensorCanPlace::new)
         .registerCoreComponentType("Nav", BuilderSensorNav::new)
         .registerCoreComponentType("InWater", BuilderSensorInWater::new)
         .registerCoreComponentType("IsBusy", BuilderSensorIsBusy::new)
         .registerCoreComponentType("InteractionContext", BuilderSensorInteractionContext::new)
         .registerCoreComponentType("Alarm", BuilderSensorAlarm::new)
         .registerCoreComponentType("AdjustPosition", BuilderSensorAdjustPosition::new)
         .registerCoreComponentType("SearchRay", BuilderSensorSearchRay::new)
         .registerCoreComponentType("BlockType", BuilderSensorBlockType::new)
         .registerCoreComponentType("Self", BuilderSensorSelf::new)
         .registerCoreComponentType("ValueProviderWrapper", BuilderSensorValueProviderWrapper::new);
      this.registerCoreComponentType("Attitude", BuilderEntityFilterAttitude::new)
         .registerCoreComponentType("LineOfSight", BuilderEntityFilterLineOfSight::new)
         .registerCoreComponentType("HeightDifference", BuilderEntityFilterHeightDifference::new)
         .registerCoreComponentType("ViewSector", BuilderEntityFilterViewSector::new)
         .registerCoreComponentType("Combat", BuilderEntityFilterCombat::new)
         .registerCoreComponentType("ItemInHand", BuilderEntityFilterItemInHand::new)
         .registerCoreComponentType("NPCGroup", BuilderEntityFilterNPCGroup::new)
         .registerCoreComponentType("MovementState", BuilderEntityFilterMovementState::new)
         .registerCoreComponentType("SpotsMe", BuilderEntityFilterSpotsMe::new)
         .registerCoreComponentType("StandingOnBlock", BuilderEntityFilterStandingOnBlock::new)
         .registerCoreComponentType("Stat", BuilderEntityFilterStat::new)
         .registerCoreComponentType("Inventory", BuilderEntityFilterInventory::new)
         .registerCoreComponentType("Not", BuilderEntityFilterNot::new)
         .registerCoreComponentType("And", BuilderEntityFilterAnd::new)
         .registerCoreComponentType("Or", BuilderEntityFilterOr::new)
         .registerCoreComponentType("Altitude", BuilderEntityFilterAltitude::new)
         .registerCoreComponentType("InsideBlock", BuilderEntityFilterInsideBlock::new);
      this.registerCoreComponentType("Attitude", BuilderSensorEntityPrioritiserAttitude::new);
      NPCPlugin.NPCConfig config = this.config.get();
      this.autoReload = config.isAutoReload();
      this.validateBuilder = config.isValidateBuilder();
      this.maxBlackboardBlockCountPerType = config.getMaxBlackboardBlockType();
      this.logFailingTestErrors = config.isLogFailingTestErrors();
      this.presetCoverageTestNPCs = config.getPresetCoverageTestNPCs();
   }

   public String[] getPresetCoverageTestNPCs() {
      return this.presetCoverageTestNPCs;
   }

   @Nullable
   public Pair<Ref<EntityStore>, INonPlayerCharacter> spawnNPC(
      @Nonnull Store<EntityStore> store, @Nonnull String npcType, @Nullable String groupType, @Nonnull Vector3d position, @Nonnull Vector3f rotation
   ) {
      int roleIndex = this.getIndex(npcType);
      if (roleIndex < 0) {
         return null;
      } else {
         Pair<Ref<EntityStore>, NPCEntity> npcPair = this.spawnEntity(store, roleIndex, position, rotation, null, null);
         FlockAsset flockDefinition = groupType != null ? FlockAsset.getAssetMap().getAsset(groupType) : null;
         if (npcPair != null) {
            Ref<EntityStore> npcRef = (Ref<EntityStore>)npcPair.first();
            NPCEntity npcComponent = (NPCEntity)npcPair.second();
            FlockPlugin.trySpawnFlock(npcRef, npcComponent, store, roleIndex, position, rotation, flockDefinition, null);
            return Pair.of((Ref)npcPair.first(), (INonPlayerCharacter)npcPair.second());
         } else {
            return null;
         }
      }
   }

   public static void reloadNPCsWithRole(int roleIndex) {
      Universe.get()
         .getWorlds()
         .forEach(
            (s, world) -> world.execute(() -> world.getEntityStore().getStore().forEachChunk(NPCEntity.getComponentType(), (archetypeChunk, commandBuffer) -> {
               for (int index = 0; index < archetypeChunk.size(); index++) {
                  NPCEntity npc = archetypeChunk.getComponent(index, NPCEntity.getComponentType());
                  if (npc.getRoleIndex() == roleIndex && !npc.getRole().isRoleChangeRequested()) {
                     RoleChangeSystem.requestRoleChange(archetypeChunk.getReferenceTo(index), npc.getRole(), roleIndex, true, world.getEntityStore().getStore());
                  }
               }
            }))
         );
   }

   protected void onNPCGroupsLoaded(LoadedAssetsEvent<String, NPCGroup, AssetMap<String, NPCGroup>> event) {
      this.putNPCGroups();
   }

   protected void onNPCGroupsRemoved(RemovedAssetsEvent<String, NPCGroup, AssetMap<String, NPCGroup>> event) {
      this.putNPCGroups();
   }

   protected void onAttitudeGroupsLoaded(@Nonnull LoadedAssetsEvent<String, AttitudeGroup, AssetMap<String, AttitudeGroup>> event) {
      if (this.attitudeMap == null) {
         this.putAttitudeGroups();
      } else {
         Map<String, AttitudeGroup> loadedAssets = event.getLoadedAssets();
         IndexedLookupTableAssetMap<String, AttitudeGroup> assets = AttitudeGroup.getAssetMap();
         int attitudeGroupCount = this.attitudeMap.getAttitudeGroupCount();

         for (String id : loadedAssets.keySet()) {
            int index = assets.getIndex(id);
            if (index == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown key! " + id);
            }

            if (index >= attitudeGroupCount) {
               this.putAttitudeGroups();
               return;
            }
         }

         loadedAssets.forEach((idx, group) -> {
            int indexx = assets.getIndex(idx);
            if (indexx == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown key! " + idx);
            } else {
               this.attitudeMap.updateAttitudeGroup(indexx, group);
            }
         });
      }
   }

   protected void onAttitudeGroupsRemoved(RemovedAssetsEvent<String, AttitudeGroup, AssetMap<String, AttitudeGroup>> event) {
      this.putAttitudeGroups();
   }

   protected void onItemAttitudeGroupsLoaded(@Nonnull LoadedAssetsEvent<String, ItemAttitudeGroup, AssetMap<String, ItemAttitudeGroup>> event) {
      if (this.itemAttitudeMap == null) {
         this.putItemAttitudeGroups();
      } else {
         Map<String, ItemAttitudeGroup> loadedAssets = event.getLoadedAssets();
         IndexedLookupTableAssetMap<String, ItemAttitudeGroup> assets = ItemAttitudeGroup.getAssetMap();
         int attitudeGroupCount = this.itemAttitudeMap.getAttitudeGroupCount();

         for (String id : loadedAssets.keySet()) {
            int index = assets.getIndex(id);
            if (index == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown key! " + id);
            }

            if (index >= attitudeGroupCount) {
               this.putItemAttitudeGroups();
               return;
            }
         }

         loadedAssets.forEach((idx, group) -> {
            int indexx = assets.getIndex(idx);
            if (indexx == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown key! " + idx);
            } else {
               this.itemAttitudeMap.updateAttitudeGroup(indexx, group);
            }
         });
      }
   }

   protected void onItemAttitudeGroupsRemoved(RemovedAssetsEvent<String, ItemAttitudeGroup, AssetMap<String, ItemAttitudeGroup>> event) {
      this.putItemAttitudeGroups();
   }

   private void putItemAttitudeGroups() {
      ItemAttitudeMap.Builder builder = new ItemAttitudeMap.Builder();
      builder.addAttitudeGroups(ItemAttitudeGroup.getAssetMap().getAssetMap());
      this.itemAttitudeMap = builder.build();
   }

   protected void onPathChange(WorldPathChangedEvent event) {
      this.pathChangeRevision.getAndIncrement();
   }

   public int getPathChangeRevision() {
      return this.pathChangeRevision.get();
   }

   protected void onNPCsLoaded(AllNPCsLoadedEvent event) {
      this.putNPCGroups();
   }

   private void putNPCGroups() {
      IndexedLookupTableAssetMap<String, NPCGroup> indexedAssetMap = NPCGroup.getAssetMap();
      Object2IntOpenHashMap<String> tagSetIndexMap = new Object2IntOpenHashMap();
      indexedAssetMap.getAssetMap().forEach((name, group) -> {
         int index = indexedAssetMap.getIndex(name);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + name);
         } else {
            tagSetIndexMap.put(name, index);
         }
      });
      TagSetPlugin.get(NPCGroup.class).putAssetSets(indexedAssetMap.getAssetMap(), tagSetIndexMap, this.builderManager.getNameToIndexMap());
      this.putAttitudeGroups();
   }

   private void putAttitudeGroups() {
      AttitudeMap.Builder builder = new AttitudeMap.Builder();
      builder.addAttitudeGroups(AttitudeGroup.getAssetMap().getAssetMap());
      this.attitudeMap = builder.build();
   }

   @Nullable
   public String getName(int builderIndex) {
      return this.builderManager.lookupName(builderIndex);
   }

   public int getIndex(String builderName) {
      return this.builderManager.getIndex(builderName);
   }

   @Nullable
   public Builder<Role> tryGetCachedValidRole(int roleIndex) {
      return this.builderManager.tryGetCachedValidRole(roleIndex);
   }

   @Nullable
   public BuilderInfo getBuilderInfo(Builder<?> builder) {
      return this.builderManager.getBuilderInfo(builder);
   }

   public List<String> getRoleTemplateNames(boolean spawnableOnly) {
      return this.builderManager
         .collectMatchingBuilders(
            new ObjectArrayList(),
            entry -> entry.getBuilder().category() == Role.class && (!spawnableOnly || entry.getBuilder().isSpawnable()),
            (builderInfo, objects) -> objects.add(builderInfo.getKeyName())
         );
   }

   public boolean hasRoleName(String roleName) {
      return this.getRoleBuilderInfo(this.getIndex(roleName)) != null;
   }

   public void validateSpawnableRole(String roleName) {
      BuilderInfo builder = this.getRoleBuilderInfo(this.getIndex(roleName));
      if (builder == null) {
         throw new SkipSentryException(new IllegalArgumentException(roleName + " does not exist as a role!"));
      } else if (!builder.getBuilder().isSpawnable()) {
         throw new SkipSentryException(new IllegalArgumentException(roleName + " is an abstract role type and cannot be spawned directly!"));
      }
   }

   @Nullable
   public BuilderInfo getRoleBuilderInfo(int roleIndex) {
      BuilderInfo builderInfo = this.builderManager.tryGetBuilderInfo(roleIndex);
      return builderInfo != null && builderInfo.getBuilder().category() == Role.class ? builderInfo : null;
   }

   public void setBuilderInvalid(int builderIndex) {
      BuilderInfo builderInfo = this.builderManager.tryGetBuilderInfo(builderIndex);
      if (builderInfo != null) {
         builderInfo.setNeedsReload();
      }
   }

   public AttitudeMap getAttitudeMap() {
      return this.attitudeMap;
   }

   public ItemAttitudeMap getItemAttitudeMap() {
      return this.itemAttitudeMap;
   }

   public boolean testAndValidateRole(@Nullable BuilderInfo builderInfo) {
      return builderInfo != null
         && builderInfo.getBuilder() != null
         && builderInfo.getBuilder().category() == Role.class
         && this.builderManager.validateBuilder(builderInfo);
   }

   public void forceValidation(int builderIndex) {
      this.builderManager.forceValidation(builderIndex);
   }

   @Nullable
   public Pair<Ref<EntityStore>, NPCEntity> spawnEntity(
      @Nonnull Store<EntityStore> store,
      int roleIndex,
      @Nonnull Vector3d position,
      @Nullable Vector3f rotation,
      @Nullable Model spawnModel,
      @Nullable TriConsumer<NPCEntity, Ref<EntityStore>, Store<EntityStore>> postSpawn
   ) {
      return this.spawnEntity(store, roleIndex, position, rotation, spawnModel, null, postSpawn);
   }

   @Nullable
   public Pair<Ref<EntityStore>, NPCEntity> spawnEntity(
      @Nonnull Store<EntityStore> store,
      int roleIndex,
      @Nonnull Vector3d position,
      @Nullable Vector3f rotation,
      @Nullable Model spawnModel,
      @Nullable TriConsumer<NPCEntity, Holder<EntityStore>, Store<EntityStore>> preAddToWorld,
      @Nullable TriConsumer<NPCEntity, Ref<EntityStore>, Store<EntityStore>> postSpawn
   ) {
      WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
      NPCEntity npcComponent = new NPCEntity();
      npcComponent.setSpawnInstant(worldTimeResource.getGameTime());
      if (rotation == null) {
         rotation = NULL_ROTATION;
      }

      npcComponent.saveLeashInformation(position, rotation);
      String roleName = this.getName(roleIndex);
      if (roleName == null) {
         get().getLogger().at(Level.WARNING).log("Unable to spawn entity with invalid role index: %s!", roleIndex);
         return null;
      } else {
         npcComponent.setRoleName(roleName);
         npcComponent.setRoleIndex(roleIndex);
         Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
         holder.addComponent(NPCEntity.getComponentType(), npcComponent);
         holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(position, rotation));
         holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));
         DisplayNameComponent displayNameComponent = new DisplayNameComponent(Message.raw(roleName));
         holder.addComponent(DisplayNameComponent.getComponentType(), displayNameComponent);
         holder.ensureComponent(UUIDComponent.getComponentType());
         if (spawnModel != null) {
            npcComponent.setInitialModelScale(spawnModel.getScale());
            holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(spawnModel));
            holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(spawnModel.toReference()));
         }

         if (preAddToWorld != null) {
            preAddToWorld.accept(npcComponent, holder, store);
         }

         Ref<EntityStore> ref = store.addEntity(holder, AddReason.SPAWN);
         if (ref == null) {
            get().getLogger().at(Level.WARNING).log("Unable to handle non-spawned entity: %s!", this.getName(roleIndex));
            return null;
         } else {
            if (postSpawn != null) {
               postSpawn.accept(npcComponent, ref, store);
            }

            return Pair.of(ref, npcComponent);
         }
      }
   }

   @Nonnull
   public BuilderInfo prepareRoleBuilderInfo(int roleIndex) {
      try {
         BuilderInfo builderInfo = this.builderManager.getCachedBuilderInfo(roleIndex, Role.class);
         if (this.validateBuilder) {
            if (!builderInfo.isValidated()) {
               this.builderManager.validateBuilder(builderInfo);
            }

            if (!builderInfo.isValid()) {
               throw new SkipSentryException(new IllegalStateException("Builder " + builderInfo.getKeyName() + " failed validation!"));
            }
         }

         return builderInfo;
      } catch (RuntimeException var4) {
         throw new SkipSentryException(
            new RuntimeException(String.format("Cannot use role template '%s' (%s): %s", this.getName(roleIndex), roleIndex, var4.getMessage()), var4)
         );
      }
   }

   @Nonnull
   public static Role buildRole(@Nonnull Builder<Role> roleBuilder, @Nonnull BuilderInfo builderInfo, @Nonnull BuilderSupport builderSupport, int roleIndex) {
      Role role;
      try {
         StdScope scope = roleBuilder.getBuilderParameters().createScope();
         builderSupport.setScope(scope);
         builderSupport.setGlobalScope(scope);
         role = roleBuilder.build(builderSupport);
         role.postRoleBuilt(builderSupport);
      } catch (Throwable var6) {
         builderInfo.setNeedsReload();
         throw new SkipSentryException(var6);
      }

      role.setRoleIndex(roleIndex, builderInfo.getKeyName());
      return role;
   }

   protected void onModelsChanged(@Nonnull LoadedAssetsEvent<String, ModelAsset, DefaultAssetMap<String, ModelAsset>> event) {
      Map<String, ModelAsset> loadedModelAssets = event.getLoadedAssets();
      Universe.get()
         .getWorlds()
         .values()
         .forEach(
            world -> world.execute(
               () -> {
                  Store<EntityStore> store = world.getEntityStore().getStore();
                  store.forEachEntityParallel(
                     NPCEntity.getComponentType(),
                     (index, archetypeChunk, commandBuffer) -> {
                        ModelComponent entityModelComponent = archetypeChunk.getComponent(index, ModelComponent.getComponentType());
                        if (entityModelComponent != null) {
                           Model oldModel = entityModelComponent.getModel();
                           ModelAsset newModelAsset = loadedModelAssets.get(oldModel.getModelAssetId());
                           if (newModelAsset != null) {
                              Ref<EntityStore> entityReference = archetypeChunk.getReferenceTo(index);
                              commandBuffer.putComponent(
                                 entityReference,
                                 ModelComponent.getComponentType(),
                                 new ModelComponent(Model.createScaledModel(newModelAsset, oldModel.getScale(), oldModel.getRandomAttachmentIds()))
                              );
                           }
                        }
                     }
                  );
               }
            )
         );
   }

   protected void generateDescriptors() {
      this.getLogger().at(Level.INFO).log("===== Generating descriptors for NPC!");
      this.builderDescriptors = this.builderManager.generateDescriptors();
   }

   protected void saveDescriptors() {
      this.getLogger().at(Level.INFO).log("===== Saving descriptors for NPC!");
      Path path = Path.of("npc_descriptors.json");
      BuilderManager.saveDescriptors(this.builderDescriptors, path);
      this.getLogger().at(Level.INFO).log("Saved NPC descriptors to: %s", path);
   }

   public BuilderManager getBuilderManager() {
      return this.builderManager;
   }

   public int getMaxBlackboardBlockCountPerType() {
      return this.maxBlackboardBlockCountPerType;
   }

   public boolean isLogFailingTestErrors() {
      return this.logFailingTestErrors;
   }

   public boolean startRoleBenchmark(double seconds, @Nonnull Consumer<Int2ObjectMap<TimeDistributionRecorder>> onFinished) {
      this.benchmarkLock.lock();

      label37: {
         boolean var4;
         try {
            if (!this.isBenchmarking()) {
               this.roleTickDistribution = new Int2ObjectOpenHashMap();
               this.roleTickDistributionAll = new TimeDistributionRecorder(0.01, 1.0E-5);
               this.roleTickDistribution.put(-1, this.roleTickDistributionAll);
               break label37;
            }

            var4 = false;
         } finally {
            this.benchmarkLock.unlock();
         }

         return var4;
      }

      new CompletableFuture().completeOnTimeout(null, Math.round(seconds * 1000.0), TimeUnit.MILLISECONDS).thenRunAsync(() -> {
         Int2ObjectMap<TimeDistributionRecorder> distribution = this.roleTickDistribution;
         this.benchmarkLock.lock();

         try {
            this.roleTickDistribution = null;
            this.roleTickDistributionAll = null;
         } finally {
            this.benchmarkLock.unlock();
         }

         onFinished.accept(distribution);
      });
      return true;
   }

   public void collectRoleTick(int roleIndex, long nanos) {
      if (this.benchmarkLock.tryLock()) {
         try {
            if (this.roleTickDistribution != null) {
               ((TimeDistributionRecorder)this.roleTickDistribution.computeIfAbsent(roleIndex, i -> new TimeDistributionRecorder(0.01, 1.0E-5)))
                  .recordNanos(nanos);
               this.roleTickDistributionAll.recordNanos(nanos);
            }
         } finally {
            this.benchmarkLock.unlock();
         }
      }
   }

   public boolean isBenchmarkingRole() {
      return this.roleTickDistribution != null;
   }

   public boolean startSensorSupportBenchmark(double seconds, @Nonnull Consumer<Int2ObjectMap<SensorSupportBenchmark>> onFinished) {
      this.benchmarkLock.lock();

      label37: {
         boolean var4;
         try {
            if (!this.isBenchmarking()) {
               this.roleSensorSupportDistribution = new Int2ObjectOpenHashMap();
               this.roleSensorSupportDistributionAll = new SensorSupportBenchmark();
               this.roleSensorSupportDistribution.put(-1, this.roleSensorSupportDistributionAll);
               break label37;
            }

            var4 = false;
         } finally {
            this.benchmarkLock.unlock();
         }

         return var4;
      }

      new CompletableFuture().completeOnTimeout(null, Math.round(seconds * 1000.0), TimeUnit.MILLISECONDS).thenRunAsync(() -> {
         Int2ObjectMap<SensorSupportBenchmark> distribution = this.roleSensorSupportDistribution;
         this.benchmarkLock.lock();

         try {
            this.roleSensorSupportDistribution = null;
            this.roleSensorSupportDistributionAll = null;
         } finally {
            this.benchmarkLock.unlock();
         }

         onFinished.accept(distribution);
      });
      return true;
   }

   public boolean isBenchmarkingSensorSupport() {
      return this.roleSensorSupportDistributionAll != null;
   }

   protected boolean isBenchmarking() {
      return this.isBenchmarkingRole() || this.isBenchmarkingSensorSupport();
   }

   public void collectSensorSupportPlayerList(
      int roleIndex, long getNanos, double maxPlayerDistanceSorted, double maxPlayerDistance, double maxPlayerDistanceAvoidance, int numPlayers
   ) {
      if (this.benchmarkLock.tryLock()) {
         try {
            if (this.roleSensorSupportDistribution != null) {
               ((SensorSupportBenchmark)this.roleSensorSupportDistribution.computeIfAbsent(roleIndex, i -> new SensorSupportBenchmark()))
                  .collectPlayerList(getNanos, maxPlayerDistanceSorted, maxPlayerDistance, maxPlayerDistanceAvoidance, numPlayers);
               this.roleSensorSupportDistributionAll
                  .collectPlayerList(getNanos, maxPlayerDistanceSorted, maxPlayerDistance, maxPlayerDistanceAvoidance, numPlayers);
            }
         } finally {
            this.benchmarkLock.unlock();
         }
      }
   }

   public void collectSensorSupportEntityList(
      int roleIndex, long getNanos, double maxEntityDistanceSorted, double maxEntityDistance, double maxEntityDistanceAvoidance, int numEntities
   ) {
      if (this.benchmarkLock.tryLock()) {
         try {
            if (this.roleSensorSupportDistribution != null) {
               ((SensorSupportBenchmark)this.roleSensorSupportDistribution.computeIfAbsent(roleIndex, i -> new SensorSupportBenchmark()))
                  .collectEntityList(getNanos, maxEntityDistanceSorted, maxEntityDistance, maxEntityDistanceAvoidance, numEntities);
               this.roleSensorSupportDistributionAll
                  .collectEntityList(getNanos, maxEntityDistanceSorted, maxEntityDistance, maxEntityDistanceAvoidance, numEntities);
            }
         } finally {
            this.benchmarkLock.unlock();
         }
      }
   }

   public void collectSensorSupportLosTest(int roleIndex, boolean cacheHit, long time) {
      if (this.isBenchmarkingSensorSupport() && this.benchmarkLock.tryLock()) {
         try {
            if (this.roleSensorSupportDistribution != null) {
               ((SensorSupportBenchmark)this.roleSensorSupportDistribution.computeIfAbsent(roleIndex, i -> new SensorSupportBenchmark()))
                  .collectLosTest(cacheHit, time);
               this.roleSensorSupportDistributionAll.collectLosTest(cacheHit, time);
            }
         } finally {
            this.benchmarkLock.unlock();
         }
      }
   }

   public void collectSensorSupportInverseLosTest(int roleIndex, boolean cacheHit) {
      if (this.isBenchmarkingSensorSupport() && this.benchmarkLock.tryLock()) {
         try {
            if (this.roleSensorSupportDistribution != null) {
               ((SensorSupportBenchmark)this.roleSensorSupportDistribution.computeIfAbsent(roleIndex, i -> new SensorSupportBenchmark()))
                  .collectInverseLosTest(cacheHit);
               this.roleSensorSupportDistributionAll.collectInverseLosTest(cacheHit);
            }
         } finally {
            this.benchmarkLock.unlock();
         }
      }
   }

   public void collectSensorSupportFriendlyBlockingTest(int roleIndex, boolean cacheHit) {
      if (this.isBenchmarkingSensorSupport() && this.benchmarkLock.tryLock()) {
         try {
            if (this.roleSensorSupportDistribution != null) {
               ((SensorSupportBenchmark)this.roleSensorSupportDistribution.computeIfAbsent(roleIndex, i -> new SensorSupportBenchmark()))
                  .collectFriendlyBlockingTest(cacheHit);
               this.roleSensorSupportDistributionAll.collectFriendlyBlockingTest(cacheHit);
            }
         } finally {
            this.benchmarkLock.unlock();
         }
      }
   }

   public void collectSensorSupportTickDone(int roleIndex) {
      if (this.isBenchmarkingSensorSupport() && this.benchmarkLock.tryLock()) {
         try {
            if (this.roleSensorSupportDistribution != null) {
               ((SensorSupportBenchmark)this.roleSensorSupportDistribution.computeIfAbsent(roleIndex, i -> new SensorSupportBenchmark())).tickDone();
               this.roleSensorSupportDistributionAll.tickDone();
            }
         } finally {
            this.benchmarkLock.unlock();
         }
      }
   }

   @Nonnull
   public <T> NPCPlugin registerCoreComponentType(String name, @Nonnull Supplier<Builder<T>> builder) {
      BuilderFactory<T> factory = this.builderManager.getFactory(builder.get().category());
      factory.add(name, builder);
      return this;
   }

   public void setRoleBuilderNeedsReload(Builder<?> builder) {
      BuilderInfo builderInfo = this.getBuilderInfo(builder);
      Objects.requireNonNull(builderInfo, "Have builder but can't get builderInfo for it");
      builderInfo.setNeedsReload();
   }

   protected void registerCoreFactories() {
      BuilderFactory<Role> roleFactory = new BuilderFactory<>(Role.class, "Type");
      roleFactory.add("Generic", BuilderRole::new);
      roleFactory.add("Abstract", BuilderRoleAbstract::new);
      roleFactory.add("Variant", BuilderRoleVariant::new);
      this.builderManager.registerFactory(roleFactory);
      BuilderFactory<MotionController> motionControllerFactory = new BuilderFactory<>(MotionController.class, "Type");
      motionControllerFactory.add("Walk", BuilderMotionControllerWalk::new);
      motionControllerFactory.add("Fly", BuilderMotionControllerFly::new);
      motionControllerFactory.add("Dive", BuilderMotionControllerDive::new);
      this.builderManager.registerFactory(motionControllerFactory);
      BuilderFactory<Map<String, MotionController>> motionControllerMapFactory = new BuilderFactory<>(
         BuilderMotionControllerMapUtil.CLASS_REFERENCE, "Type", BuilderMotionControllerMap::new
      );
      this.builderManager.registerFactory(motionControllerMapFactory);
      BuilderFactory<ActionList> actionListFactory = new BuilderFactory<>(ActionList.class, "Type", BuilderActionList::new);
      this.builderManager.registerFactory(actionListFactory);
      BuilderFactory<Instruction> instructionFactory = new BuilderFactory<>(Instruction.class, "Type", BuilderInstruction::new);
      instructionFactory.add("Random", BuilderInstructionRandomized::new);
      instructionFactory.add("Reference", BuilderInstructionReference::new);
      this.builderManager.registerFactory(instructionFactory);
      BuilderFactory<TransientPathDefinition> transientPathFactory = new BuilderFactory<>(
         TransientPathDefinition.class, "Type", BuilderTransientPathDefinition::new
      );
      this.builderManager.registerFactory(transientPathFactory);
      BuilderFactory<RelativeWaypointDefinition> relativeWaypointFactory = new BuilderFactory<>(
         RelativeWaypointDefinition.class, "Type", BuilderRelativeWaypointDefinition::new
      );
      this.builderManager.registerFactory(relativeWaypointFactory);
      BuilderFactory<WeightedAction> weightedActionFactory = new BuilderFactory<>(WeightedAction.class, "Type", BuilderWeightedAction::new);
      this.builderManager.registerFactory(weightedActionFactory);
      BuilderFactory<BuilderValueToParameterMapping.ValueToParameterMapping> valueToParameterMappingFactory = new BuilderFactory<>(
         BuilderValueToParameterMapping.ValueToParameterMapping.class, "Type", BuilderValueToParameterMapping::new
      );
      this.builderManager.registerFactory(valueToParameterMappingFactory);
      StateTransitionController.registerFactories(this.builderManager);
      this.builderManager.registerFactory(new BuilderFactory<>(BodyMotion.class, "Type"));
      this.builderManager.registerFactory(new BuilderFactory<>(HeadMotion.class, "Type"));
      this.builderManager.registerFactory(new BuilderFactory<>(Action.class, "Type"));
      this.builderManager.registerFactory(new BuilderFactory<>(Sensor.class, "Type"));
      this.builderManager.registerFactory(new BuilderFactory<>(IEntityFilter.class, "Type"));
      this.builderManager.registerFactory(new BuilderFactory<>(ISensorEntityPrioritiser.class, "Type"));
      this.builderManager.registerFactory(new BuilderFactory<>(ISensorEntityCollector.class, "Type"));
   }

   protected static void onBalanceAssetsChanged(@Nonnull LoadedAssetsEvent<String, BalanceAsset, DefaultAssetMap<String, BalanceAsset>> event) {
      Map<String, BalanceAsset> loadedAssets = event.getLoadedAssets();
      Universe.get()
         .getWorlds()
         .forEach(
            (name, world) -> world.execute(
               () -> world.getEntityStore().getStore().forEachChunk(NPCEntity.getComponentType(), (archetypeChunk, commandBuffer) -> {
                  for (int index = 0; index < archetypeChunk.size(); index++) {
                     NPCEntity npcComponent = archetypeChunk.getComponent(index, NPCEntity.getComponentType());

                     assert npcComponent != null;

                     if (loadedAssets.containsKey(npcComponent.getRole().getBalanceAsset())) {
                        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
                        RoleChangeSystem.requestRoleChange(ref, npcComponent.getRole(), npcComponent.getRoleIndex(), false, world.getEntityStore().getStore());
                     }
                  }
               })
            )
         );
   }

   protected static void onBalanceAssetsRemoved(@Nonnull RemovedAssetsEvent<String, BalanceAsset, DefaultAssetMap<String, BalanceAsset>> event) {
      Set<String> removedAssets = event.getRemovedAssets();
      Universe.get()
         .getWorlds()
         .forEach(
            (name, world) -> world.execute(
               () -> world.getEntityStore().getStore().forEachChunk(NPCEntity.getComponentType(), (archetypeChunk, commandBuffer) -> {
                  for (int index = 0; index < archetypeChunk.size(); index++) {
                     NPCEntity npcComponent = archetypeChunk.getComponent(index, NPCEntity.getComponentType());

                     assert npcComponent != null;

                     if (removedAssets.contains(npcComponent.getRole().getBalanceAsset())) {
                        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
                        commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
                     }
                  }
               })
            )
         );
   }

   public static class NPCConfig {
      public static final BuilderCodec<NPCPlugin.NPCConfig> CODEC = BuilderCodec.builder(NPCPlugin.NPCConfig.class, NPCPlugin.NPCConfig::new)
         .append(new KeyedCodec<>("Descriptors", Codec.BOOLEAN), (o, i) -> o.generateDescriptors = i, o -> o.generateDescriptors)
         .add()
         .append(new KeyedCodec<>("DescriptorsFile", Codec.BOOLEAN), (o, i) -> o.generateDescriptorsFile = i, o -> o.generateDescriptorsFile)
         .add()
         .append(new KeyedCodec<>("AutoReload", Codec.BOOLEAN), (o, i) -> o.autoReload = i, o -> o.autoReload)
         .add()
         .append(new KeyedCodec<>("ValidateBuilders", Codec.BOOLEAN), (o, i) -> o.validateBuilder = i, o -> o.validateBuilder)
         .add()
         .append(new KeyedCodec<>("MaxBlackboardBlockType", Codec.INTEGER), (o, i) -> o.maxBlackboardBlockType = i, o -> o.maxBlackboardBlockType)
         .add()
         .append(new KeyedCodec<>("LogFailingTestErrors", Codec.BOOLEAN), (o, i) -> o.logFailingTestErrors = i, o -> o.logFailingTestErrors)
         .add()
         .append(new KeyedCodec<>("PresetCoverageTestNPCs", Codec.STRING_ARRAY), (o, i) -> o.presetCoverageTestNPCs = i, o -> o.presetCoverageTestNPCs)
         .add()
         .build();
      private boolean generateDescriptors;
      private boolean generateDescriptorsFile;
      private boolean autoReload = true;
      private boolean validateBuilder = true;
      private int maxBlackboardBlockType = 20;
      private boolean logFailingTestErrors;
      private String[] presetCoverageTestNPCs = new String[]{
         "Test_Bird",
         "Test_Block_Sensor",
         "Test_Attack_Bow",
         "Test_Combat_Sensor_Sheep",
         "Test_Bow_Charge",
         "Test_OffHand_Swap",
         "Test_Patrol_Path",
         "Test_Flock_Mixed#4",
         "Test_Group_Sheep",
         "Test_Attack_Flying_Ranged",
         "Test_Interaction_Complete_Task",
         "Test_Hotbar_Weapon_Swap",
         "Test_Inventory_Contents",
         "Test_Dive_Flee",
         "Test_Jumping",
         "Test_Walk_Leave",
         "Test_Walk_Seek",
         "Test_Watch",
         "Test_Chained_Path",
         "Test_Spawn_Action",
         "Test_State_Evaluator_Toggle",
         "Test_State_Evaluator_Sleep",
         "Test_Alarm",
         "Test_Timer_Repeating",
         "Test_Action_Model_Attachment",
         "Test_Animation_State_Change",
         "Test_Block_Change",
         "Test_Crouch",
         "Test_Drop_Item",
         "Test_Entity_Damage_Event",
         "Test_Hover_Parrot",
         "Test_In_Water",
         "Test_Light_Sensor",
         "Test_Model_Change",
         "Test_Particle_Emotions",
         "Test_Place_Blocks",
         "Test_Position_Adjustment_Wrapper",
         "Test_Probe",
         "Test_Sensor_Age",
         "Test_Sensor_DroppedItem",
         "Test_Shoot_At_Block",
         "Test_Species_Attitude",
         "Test_Standing_On_Block_Sensor",
         "Test_Teleport",
         "Test_Throw_NPC",
         "Test_Trigger_Spawners",
         "Test_Weather_Sensor",
         "Test_Bird_Land"
      };

      public boolean isGenerateDescriptors() {
         return this.generateDescriptors;
      }

      public boolean isGenerateDescriptorsFile() {
         return this.generateDescriptorsFile;
      }

      public boolean isAutoReload() {
         return this.autoReload;
      }

      public boolean isValidateBuilder() {
         return this.validateBuilder;
      }

      public int getMaxBlackboardBlockType() {
         return this.maxBlackboardBlockType;
      }

      public boolean isLogFailingTestErrors() {
         return this.logFailingTestErrors;
      }

      public String[] getPresetCoverageTestNPCs() {
         return this.presetCoverageTestNPCs;
      }
   }

   public static class NPCEntityRegenerateStatsSystem extends EntityStatsSystems.Regenerate<NPCEntity> {
      public NPCEntityRegenerateStatsSystem() {
         super(EntityStatMap.getComponentType(), NPCEntity.getComponentType());
      }
   }
}
