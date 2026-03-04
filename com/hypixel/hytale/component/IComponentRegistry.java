package com.hypixel.hytale.component;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.event.EntityEventType;
import com.hypixel.hytale.component.event.WorldEventType;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.spatial.SpatialStructure;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.ISystem;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public interface IComponentRegistry<ECS_TYPE> {
   @Nonnull
   <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(@Nonnull Class<? super T> var1, @Nonnull Supplier<T> var2);

   @Nonnull
   <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(
      @Nonnull Class<? super T> var1, @Nonnull String var2, @Nonnull BuilderCodec<T> var3
   );

   @Nonnull
   <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> registerResource(@Nonnull Class<? super T> var1, @Nonnull Supplier<T> var2);

   @Nonnull
   <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> registerResource(
      @Nonnull Class<? super T> var1, @Nonnull String var2, @Nonnull BuilderCodec<T> var3
   );

   <T extends ISystem<ECS_TYPE>> SystemType<ECS_TYPE, T> registerSystemType(@Nonnull Class<? super T> var1);

   @Nonnull
   <T extends EcsEvent> EntityEventType<ECS_TYPE, T> registerEntityEventType(@Nonnull Class<? super T> var1);

   @Nonnull
   <T extends EcsEvent> WorldEventType<ECS_TYPE, T> registerWorldEventType(@Nonnull Class<? super T> var1);

   @Nonnull
   SystemGroup<ECS_TYPE> registerSystemGroup();

   void registerSystem(@Nonnull ISystem<ECS_TYPE> var1);

   ResourceType<ECS_TYPE, SpatialResource<Ref<ECS_TYPE>, ECS_TYPE>> registerSpatialResource(@Nonnull Supplier<SpatialStructure<Ref<ECS_TYPE>>> var1);
}
