package com.hypixel.hytale.server.npc.corecomponents.utility;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderSensorValueProviderWrapper;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderValueToParameterMapping;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.DebugSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.ValueWrappedInfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.parameterproviders.MultipleParameterProvider;
import com.hypixel.hytale.server.npc.sensorinfo.parameterproviders.SingleDoubleParameterProvider;
import com.hypixel.hytale.server.npc.sensorinfo.parameterproviders.SingleIntParameterProvider;
import com.hypixel.hytale.server.npc.sensorinfo.parameterproviders.SingleStringParameterProvider;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponentCollection;
import com.hypixel.hytale.server.npc.valuestore.ValueStore;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SensorValueProviderWrapper extends SensorBase implements IAnnotatedComponentCollection {
   protected static final IntObjectPair<?>[] EMPTY_ARRAY = new IntObjectPair[0];
   @Nonnull
   protected final Sensor sensor;
   protected final boolean passValues;
   @Nonnull
   protected final IntObjectPair<SingleStringParameterProvider>[] stringParameterProviders;
   @Nonnull
   protected final IntObjectPair<SingleIntParameterProvider>[] intParameterProviders;
   @Nonnull
   protected final IntObjectPair<SingleDoubleParameterProvider>[] doubleParameterProviders;
   @Nonnull
   protected final ValueWrappedInfoProvider infoProvider;
   protected final MultipleParameterProvider multipleParameterProvider = new MultipleParameterProvider();
   protected final ComponentType<EntityStore, ValueStore> valueStoreComponentType;

   public SensorValueProviderWrapper(@Nonnull BuilderSensorValueProviderWrapper builder, @Nonnull BuilderSupport support, @Nonnull Sensor sensor) {
      super(builder);
      this.sensor = sensor;
      this.passValues = builder.isPassValues(support);
      this.infoProvider = new ValueWrappedInfoProvider(sensor.getSensorInfo(), this.multipleParameterProvider);
      ObjectArrayList<IntObjectPair<SingleStringParameterProvider>> stringMappings = new ObjectArrayList();
      ObjectArrayList<IntObjectPair<SingleIntParameterProvider>> intMappings = new ObjectArrayList();
      ObjectArrayList<IntObjectPair<SingleDoubleParameterProvider>> doubleMappings = new ObjectArrayList();
      List<BuilderValueToParameterMapping.ValueToParameterMapping> parameterMappings = builder.getParameterMappings(support);
      if (parameterMappings != null) {
         for (int i = 0; i < parameterMappings.size(); i++) {
            BuilderValueToParameterMapping.ValueToParameterMapping mapping = parameterMappings.get(i);
            int slot = mapping.getToParameterSlot();
            switch (mapping.getType()) {
               case String: {
                  SingleStringParameterProvider provider = new SingleStringParameterProvider(slot);
                  this.multipleParameterProvider.addParameterProvider(slot, provider);
                  stringMappings.add(IntObjectPair.of(mapping.getFromValueSlot(), provider));
                  break;
               }
               case Int: {
                  SingleIntParameterProvider provider = new SingleIntParameterProvider(slot);
                  this.multipleParameterProvider.addParameterProvider(slot, provider);
                  intMappings.add(IntObjectPair.of(mapping.getFromValueSlot(), provider));
                  break;
               }
               case Double: {
                  SingleDoubleParameterProvider provider = new SingleDoubleParameterProvider(slot);
                  this.multipleParameterProvider.addParameterProvider(slot, provider);
                  doubleMappings.add(IntObjectPair.of(mapping.getFromValueSlot(), provider));
               }
            }
         }
      }

      if (stringMappings.isEmpty()) {
         this.stringParameterProviders = (IntObjectPair<SingleStringParameterProvider>[])EMPTY_ARRAY;
      } else {
         this.stringParameterProviders = (IntObjectPair<SingleStringParameterProvider>[])stringMappings.toArray(IntObjectPair[]::new);
      }

      if (intMappings.isEmpty()) {
         this.intParameterProviders = (IntObjectPair<SingleIntParameterProvider>[])EMPTY_ARRAY;
      } else {
         this.intParameterProviders = (IntObjectPair<SingleIntParameterProvider>[])intMappings.toArray(IntObjectPair[]::new);
      }

      if (doubleMappings.isEmpty()) {
         this.doubleParameterProviders = (IntObjectPair<SingleDoubleParameterProvider>[])EMPTY_ARRAY;
      } else {
         this.doubleParameterProviders = (IntObjectPair<SingleDoubleParameterProvider>[])doubleMappings.toArray(IntObjectPair[]::new);
      }

      this.valueStoreComponentType = ValueStore.getComponentType();
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store) || !this.sensor.matches(ref, role, dt, store)) {
         DebugSupport debugSupport = role.getDebugSupport();
         if (debugSupport.isTraceSensorFails()) {
            debugSupport.setLastFailingSensor(this.sensor);
         }

         this.multipleParameterProvider.clear();
         return false;
      } else if (!this.passValues) {
         return true;
      } else {
         ValueStore valueStore = store.getComponent(ref, this.valueStoreComponentType);
         if (valueStore == null) {
            return false;
         } else {
            for (IntObjectPair<SingleStringParameterProvider> provider : this.stringParameterProviders) {
               String value = valueStore.readString(provider.firstInt());
               ((SingleStringParameterProvider)provider.value()).overrideString(value);
            }

            for (IntObjectPair<SingleIntParameterProvider> provider : this.intParameterProviders) {
               int value = valueStore.readInt(provider.firstInt());
               ((SingleIntParameterProvider)provider.value()).overrideInt(value);
            }

            for (IntObjectPair<SingleDoubleParameterProvider> provider : this.doubleParameterProviders) {
               double value = valueStore.readDouble(provider.firstInt());
               ((SingleDoubleParameterProvider)provider.value()).overrideDouble(value);
            }

            return true;
         }
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.infoProvider;
   }

   @Override
   public void registerWithSupport(Role role) {
      this.sensor.registerWithSupport(role);
   }

   @Override
   public void motionControllerChanged(
      @Nullable Ref<EntityStore> ref,
      @Nonnull NPCEntity npcComponent,
      MotionController motionController,
      @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.sensor.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
   }

   @Override
   public void loaded(Role role) {
      this.sensor.loaded(role);
   }

   @Override
   public void spawned(Role role) {
      this.sensor.spawned(role);
   }

   @Override
   public void unloaded(Role role) {
      this.sensor.unloaded(role);
   }

   @Override
   public void removed(Role role) {
      this.sensor.removed(role);
   }

   @Override
   public void teleported(Role role, World from, World to) {
      this.sensor.teleported(role, from, to);
   }

   @Override
   public void done() {
      this.sensor.done();
   }

   @Override
   public int componentCount() {
      return 1;
   }

   @Nonnull
   @Override
   public IAnnotatedComponent getComponent(int index) {
      if (index >= this.componentCount()) {
         throw new IndexOutOfBoundsException();
      } else {
         return this.sensor;
      }
   }

   @Override
   public void setContext(IAnnotatedComponent parent, int index) {
      super.setContext(parent, index);
      this.sensor.setContext(this, index);
   }
}
