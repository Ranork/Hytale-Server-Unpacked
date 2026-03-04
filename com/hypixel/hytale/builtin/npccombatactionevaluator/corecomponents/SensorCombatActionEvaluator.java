package com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents;

import com.hypixel.hytale.builtin.npccombatactionevaluator.CombatActionEvaluatorSystems;
import com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.builders.BuilderSensorCombatActionEvaluator;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.EntityPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.parameterproviders.MultipleParameterProvider;
import com.hypixel.hytale.server.npc.sensorinfo.parameterproviders.SingleDoubleParameterProvider;
import com.hypixel.hytale.server.npc.valuestore.ValueStore;
import javax.annotation.Nonnull;

public class SensorCombatActionEvaluator extends SensorBase {
   @Nonnull
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   protected final boolean targetInRange;
   protected final double allowableDeviation;
   protected final int minRangeStoreSlot;
   protected final int maxRangeStoreSlot;
   protected final int positioningAngleStoreSlot;
   protected final int targetSlot;
   @Nonnull
   protected final SingleDoubleParameterProvider minRangeParameterProvider;
   @Nonnull
   protected final SingleDoubleParameterProvider maxRangeParameterProvider;
   @Nonnull
   protected final SingleDoubleParameterProvider positioningAngleParameterProvider;
   @Nonnull
   protected final MultipleParameterProvider parameterProvider = new MultipleParameterProvider();
   @Nonnull
   protected final EntityPositionProvider positionProvider = new EntityPositionProvider(this.parameterProvider);
   protected final ComponentType<EntityStore, ValueStore> valueStoreComponentType;

   public SensorCombatActionEvaluator(@Nonnull BuilderSensorCombatActionEvaluator builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.targetInRange = builder.isTargetInRange(support);
      this.allowableDeviation = builder.getAllowableDeviation(support);
      this.targetSlot = builder.getTargetSlot(support);
      int minRangeParameter = support.getParameterSlot("MinRange");
      int maxRangeParameter = support.getParameterSlot("MaxRange");
      int positioningAngleParameter = support.getParameterSlot("PositioningAngle");
      this.minRangeParameterProvider = new SingleDoubleParameterProvider(minRangeParameter);
      this.maxRangeParameterProvider = new SingleDoubleParameterProvider(maxRangeParameter);
      this.positioningAngleParameterProvider = new SingleDoubleParameterProvider(positioningAngleParameter);
      this.parameterProvider.addParameterProvider(minRangeParameter, this.minRangeParameterProvider);
      this.parameterProvider.addParameterProvider(maxRangeParameter, this.maxRangeParameterProvider);
      this.parameterProvider.addParameterProvider(positioningAngleParameter, this.positioningAngleParameterProvider);
      this.minRangeStoreSlot = builder.getMinRangeStoreSlot(support);
      this.maxRangeStoreSlot = builder.getMaxRangeStoreSlot(support);
      this.positioningAngleStoreSlot = builder.getPositioningAngleStoreSlot(support);
      this.valueStoreComponentType = ValueStore.getComponentType();
      Holder<EntityStore> holder = support.getHolder();
      CombatActionEvaluatorSystems.CombatConstructionData constructionData = holder.ensureAndGetComponent(
         CombatActionEvaluatorSystems.CombatConstructionData.getComponentType()
      );
      constructionData.setCombatState(support.getCurrentStateName());
      constructionData.setMarkedTargetSlot(support.getTargetSlot("CAETargetSlot"));
      constructionData.setMinRangeSlot(this.minRangeStoreSlot);
      constructionData.setMaxRangeSlot(this.maxRangeStoreSlot);
      constructionData.setPositioningAngleSlot(this.positioningAngleStoreSlot);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         this.positionProvider.clear();
         this.parameterProvider.clear();
         return false;
      } else {
         Ref<EntityStore> target = role.getMarkedEntitySupport().getMarkedEntityRef(this.targetSlot);
         if (target == null) {
            this.positionProvider.clear();
            this.parameterProvider.clear();
            return false;
         } else {
            this.positionProvider.setTarget(target, store);
            ValueStore valueStore = store.getComponent(ref, this.valueStoreComponentType);
            double minRange = valueStore.readDouble(this.minRangeStoreSlot);
            double maxRange = valueStore.readDouble(this.maxRangeStoreSlot);
            this.minRangeParameterProvider.overrideDouble(minRange);
            this.maxRangeParameterProvider.overrideDouble(maxRange);
            double positioningAngle = valueStore.readDouble(this.positioningAngleStoreSlot);
            this.positioningAngleParameterProvider.overrideDouble(positioningAngle);
            Vector3d selfPosition = store.getComponent(ref, TRANSFORM_COMPONENT_TYPE).getPosition();
            Vector3d targetPosition = store.getComponent(target, TRANSFORM_COMPONENT_TYPE).getPosition();
            double distance = targetPosition.distanceTo(selfPosition);
            return this.targetInRange == distance <= maxRange + this.allowableDeviation;
         }
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.positionProvider;
   }
}
