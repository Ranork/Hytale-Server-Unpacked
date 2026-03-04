package com.hypixel.hytale.component.system;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentRegistration;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceRegistration;
import com.hypixel.hytale.component.ResourceType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class System<ECS_TYPE> implements ISystem<ECS_TYPE> {
   @Nonnull
   private final ObjectList<ComponentRegistration<ECS_TYPE, ?>> componentRegistrations = new ObjectArrayList();
   @Nonnull
   private final ObjectList<ResourceRegistration<ECS_TYPE, ?>> resourceRegistrations = new ObjectArrayList();

   @Nonnull
   protected <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(@Nonnull Class<? super T> tClass, @Nonnull Supplier<T> supplier) {
      return this.registerComponent(tClass, null, null, supplier);
   }

   @Nonnull
   protected <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(
      @Nonnull Class<? super T> tClass, @Nonnull String id, @Nonnull BuilderCodec<T> codec
   ) {
      return this.registerComponent(tClass, id, codec, codec::getDefaultValue);
   }

   @Nonnull
   protected <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(
      @Nonnull Class<? super T> tClass, @Nullable String id, @Nullable BuilderCodec<T> codec, @Nonnull Supplier<T> supplier
   ) {
      ComponentType<ECS_TYPE, T> componentType = new ComponentType<>();
      this.componentRegistrations.add(new ComponentRegistration<>(tClass, id, codec, supplier, componentType));
      return componentType;
   }

   @Nonnull
   public <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> registerResource(@Nonnull Class<? super T> tClass, @Nonnull Supplier<T> supplier) {
      return this.registerResource(tClass, null, null, supplier);
   }

   @Nonnull
   public <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> registerResource(
      @Nonnull Class<? super T> tClass, @Nonnull String id, @Nonnull BuilderCodec<T> codec
   ) {
      return this.registerResource(tClass, id, codec, codec::getDefaultValue);
   }

   @Nonnull
   private <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> registerResource(
      @Nonnull Class<? super T> tClass, @Nullable String id, @Nullable BuilderCodec<T> codec, @Nonnull Supplier<T> supplier
   ) {
      ResourceType<ECS_TYPE, T> componentType = new ResourceType<>();
      this.resourceRegistrations.add(new ResourceRegistration<>(tClass, id, codec, supplier, componentType));
      return componentType;
   }

   @Nonnull
   public List<ComponentRegistration<ECS_TYPE, ?>> getComponentRegistrations() {
      return this.componentRegistrations;
   }

   @Nonnull
   public List<ResourceRegistration<ECS_TYPE, ?>> getResourceRegistrations() {
      return this.resourceRegistrations;
   }
}
