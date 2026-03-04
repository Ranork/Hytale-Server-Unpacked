package com.hypixel.hytale.component;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class EmptyResourceStorage implements IResourceStorage {
   private static final EmptyResourceStorage INSTANCE = new EmptyResourceStorage();

   public static EmptyResourceStorage get() {
      return INSTANCE;
   }

   @Nonnull
   @Override
   public <T extends Resource<ECS_TYPE>, ECS_TYPE> CompletableFuture<T> load(
      @Nonnull Store<ECS_TYPE> store, @Nonnull ComponentRegistry.Data<ECS_TYPE> data, @Nonnull ResourceType<ECS_TYPE, T> resourceType
   ) {
      return CompletableFuture.completedFuture(data.createResource(resourceType));
   }

   @Nonnull
   @Override
   public <T extends Resource<ECS_TYPE>, ECS_TYPE> CompletableFuture<Void> save(
      @Nonnull Store<ECS_TYPE> store, @Nonnull ComponentRegistry.Data<ECS_TYPE> data, @Nonnull ResourceType<ECS_TYPE, T> resourceType, T resource
   ) {
      return CompletableFuture.completedFuture(null);
   }

   @Nonnull
   @Override
   public <T extends Resource<ECS_TYPE>, ECS_TYPE> CompletableFuture<Void> remove(
      @Nonnull Store<ECS_TYPE> store, @Nonnull ComponentRegistry.Data<ECS_TYPE> data, @Nonnull ResourceType<ECS_TYPE, T> resourceType
   ) {
      return CompletableFuture.completedFuture(null);
   }
}
