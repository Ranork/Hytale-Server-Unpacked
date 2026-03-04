package com.hypixel.hytale.component;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public interface IResourceStorage {
   @Nonnull
   <T extends Resource<ECS_TYPE>, ECS_TYPE> CompletableFuture<T> load(
      @Nonnull Store<ECS_TYPE> var1, @Nonnull ComponentRegistry.Data<ECS_TYPE> var2, @Nonnull ResourceType<ECS_TYPE, T> var3
   );

   @Nonnull
   <T extends Resource<ECS_TYPE>, ECS_TYPE> CompletableFuture<Void> save(
      @Nonnull Store<ECS_TYPE> var1, @Nonnull ComponentRegistry.Data<ECS_TYPE> var2, @Nonnull ResourceType<ECS_TYPE, T> var3, T var4
   );

   @Nonnull
   <T extends Resource<ECS_TYPE>, ECS_TYPE> CompletableFuture<Void> remove(
      @Nonnull Store<ECS_TYPE> var1, @Nonnull ComponentRegistry.Data<ECS_TYPE> var2, @Nonnull ResourceType<ECS_TYPE, T> var3
   );
}
