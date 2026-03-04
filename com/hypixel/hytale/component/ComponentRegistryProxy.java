package com.hypixel.hytale.component;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.event.EntityEventType;
import com.hypixel.hytale.component.event.WorldEventType;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.spatial.SpatialStructure;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.function.consumer.BooleanConsumer;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class ComponentRegistryProxy<ECS_TYPE> implements IComponentRegistry<ECS_TYPE> {
   private final ComponentRegistry<ECS_TYPE> registry;
   private final List<BooleanConsumer> unregister;

   public ComponentRegistryProxy(List<BooleanConsumer> registrations, ComponentRegistry<ECS_TYPE> registry) {
      this.unregister = registrations;
      this.registry = registry;
   }

   public void shutdown() {
   }

   @Nonnull
   @Override
   public <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(@Nonnull Class<? super T> tClass, @Nonnull Supplier<T> supplier) {
      return this.registerComponentType(this.registry.registerComponent(tClass, supplier));
   }

   @Nonnull
   @Override
   public <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(
      @Nonnull Class<? super T> tClass, @Nonnull String id, @Nonnull BuilderCodec<T> codec
   ) {
      return this.registerComponentType(this.registry.registerComponent(tClass, id, codec));
   }

   @Deprecated(forRemoval = true)
   @Nonnull
   public <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(
      @Nonnull Class<? super T> tClass, @Nonnull String id, @Nonnull BuilderCodec<T> codec, boolean skipValidation
   ) {
      return this.registerComponentType(this.registry.registerComponent(tClass, id, codec, skipValidation));
   }

   @Nonnull
   @Override
   public <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> registerResource(@Nonnull Class<? super T> tClass, @Nonnull Supplier<T> supplier) {
      return this.registerResourceType(this.registry.registerResource(tClass, supplier));
   }

   @Nonnull
   @Override
   public <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> registerResource(
      @Nonnull Class<? super T> tClass, @Nonnull String id, @Nonnull BuilderCodec<T> codec
   ) {
      return this.registerResourceType(this.registry.registerResource(tClass, id, codec));
   }

   @Nonnull
   @Override
   public ResourceType<ECS_TYPE, SpatialResource<Ref<ECS_TYPE>, ECS_TYPE>> registerSpatialResource(@Nonnull Supplier<SpatialStructure<Ref<ECS_TYPE>>> supplier) {
      return this.registerResourceType(this.registry.registerSpatialResource(supplier));
   }

   @Nonnull
   @Override
   public <T extends ISystem<ECS_TYPE>> SystemType<ECS_TYPE, T> registerSystemType(@Nonnull Class<? super T> systemTypeClass) {
      return this.registerSystemType(this.registry.registerSystemType(systemTypeClass));
   }

   @Nonnull
   @Override
   public <T extends EcsEvent> EntityEventType<ECS_TYPE, T> registerEntityEventType(@Nonnull Class<? super T> eventTypeClass) {
      return this.registerEntityEventType(this.registry.registerEntityEventType(eventTypeClass));
   }

   @Nonnull
   @Override
   public <T extends EcsEvent> WorldEventType<ECS_TYPE, T> registerWorldEventType(@Nonnull Class<? super T> eventTypeClass) {
      return this.registerWorldEventType(this.registry.registerWorldEventType(eventTypeClass));
   }

   @Nonnull
   @Override
   public SystemGroup<ECS_TYPE> registerSystemGroup() {
      return this.registerSystemGroup(this.registry.registerSystemGroup());
   }

   @Override
   public void registerSystem(@Nonnull ISystem<ECS_TYPE> system) {
      Class<? extends ISystem<ECS_TYPE>> systemClass = (Class<? extends ISystem<ECS_TYPE>>)system.getClass();
      this.registry.registerSystem(system);
      this.unregister.add(shutdown -> {
         if (!shutdown) {
            if (this.registry.hasSystemClass(systemClass)) {
               this.registry.unregisterSystem(systemClass);
            }
         }
      });
   }

   @Deprecated(forRemoval = true)
   public void registerSystem(@Nonnull ISystem<ECS_TYPE> system, boolean bypassClassCheck) {
      Class<? extends ISystem<ECS_TYPE>> systemClass = (Class<? extends ISystem<ECS_TYPE>>)system.getClass();
      this.registry.registerSystem(system, bypassClassCheck);
      this.unregister.add(shutdown -> {
         if (!shutdown) {
            if (this.registry.hasSystemClass(systemClass)) {
               this.registry.unregisterSystem(systemClass);
            }
         }
      });
   }

   @Nonnull
   private <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponentType(@Nonnull ComponentType<ECS_TYPE, T> componentType) {
      this.unregister.add(shutdown -> {
         if (!shutdown) {
            if (componentType.isValid()) {
               this.registry.unregisterComponent(componentType);
            }
         }
      });
      return componentType;
   }

   @Nonnull
   private <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> registerResourceType(@Nonnull ResourceType<ECS_TYPE, T> componentType) {
      this.unregister.add(shutdown -> {
         if (!shutdown) {
            if (componentType.isValid()) {
               this.registry.unregisterResource(componentType);
            }
         }
      });
      return componentType;
   }

   @Nonnull
   private <T extends ISystem<ECS_TYPE>> SystemType<ECS_TYPE, T> registerSystemType(@Nonnull SystemType<ECS_TYPE, T> systemType) {
      this.unregister.add(shutdown -> {
         if (!shutdown) {
            if (systemType.isValid()) {
               this.registry.unregisterSystemType(systemType);
            }
         }
      });
      return systemType;
   }

   @Nonnull
   private <T extends EcsEvent> EntityEventType<ECS_TYPE, T> registerEntityEventType(@Nonnull EntityEventType<ECS_TYPE, T> eventType) {
      this.unregister.add(shutdown -> {
         if (!shutdown) {
            if (eventType.isValid()) {
               this.registry.unregisterEntityEventType(eventType);
            }
         }
      });
      return eventType;
   }

   @Nonnull
   private <T extends EcsEvent> WorldEventType<ECS_TYPE, T> registerWorldEventType(@Nonnull WorldEventType<ECS_TYPE, T> eventType) {
      this.unregister.add(shutdown -> {
         if (!shutdown) {
            if (eventType.isValid()) {
               this.registry.unregisterWorldEventType(eventType);
            }
         }
      });
      return eventType;
   }

   @Nonnull
   private SystemGroup<ECS_TYPE> registerSystemGroup(@Nonnull SystemGroup<ECS_TYPE> systemGroup) {
      this.unregister.add(shutdown -> {
         if (!shutdown) {
            if (systemGroup.isValid()) {
               this.registry.unregisterSystemGroup(systemGroup);
            }
         }
      });
      return systemGroup;
   }
}
