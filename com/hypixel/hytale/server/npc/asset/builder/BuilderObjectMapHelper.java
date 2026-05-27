package com.hypixel.hytale.server.npc.asset.builder;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderObjectMapHelper<K, V> extends BuilderObjectArrayHelper<Map<K, V>, V> {
   private Function<V, K> id;

   public BuilderObjectMapHelper(Class classType, Function<V, K> id, BuilderContext owner) {
      super(classType, owner);
      this.id = id;
   }

   @Nullable
   public Map<K, V> build(@Nonnull BuilderSupport builderSupport) {
      if (this.hasNoElements()) {
         return null;
      } else {
         Map<K, V> objects = new Object2ObjectLinkedOpenHashMap();

         for (BuilderObjectReferenceHelper<V> builderObjectReferenceHelper : this.builders) {
            if (!builderObjectReferenceHelper.excludeFromRegularBuild()) {
               V value = builderObjectReferenceHelper.build(builderSupport);
               K key = this.id.apply(value);
               if (objects.containsKey(key)) {
                  throw new IllegalArgumentException("Duplicate key \"" + key + "\" at " + this.getBreadCrumbs() + ": " + this.builderParameters.getFileName());
               }

               objects.put(key, value);
            }
         }

         return objects;
      }
   }

   @Nullable
   public <T, U> T anyMatch(
      @Nonnull BiFunction<Builder<V>, U, T> test,
      @Nonnull BuilderManager builderManager,
      ExecutionContext executionContext,
      U meta,
      T successResult,
      T emptyResult,
      T noMatchResult,
      Builder<?> parentSpawnable
   ) {
      if (this.hasNoElements()) {
         return emptyResult;
      } else {
         assert this.builders != null;

         for (BuilderObjectReferenceHelper<V> builderObjectReferenceHelper : this.builders) {
            T result = test.apply(builderObjectReferenceHelper.getBuilder(builderManager, executionContext, parentSpawnable), meta);
            if (Objects.equals(result, successResult)) {
               return result;
            }
         }

         return noMatchResult;
      }
   }

   @Nullable
   public <T, U> T findFirst(
      @Nonnull BiFunction<Builder<V>, U, T> test,
      @Nonnull BuilderManager builderManager,
      ExecutionContext executionContext,
      U meta,
      @Nullable T emptyResult,
      Builder<?> parentSpawnable
   ) {
      if (this.hasNoElements()) {
         return emptyResult;
      } else {
         assert this.builders != null;

         for (BuilderObjectReferenceHelper<V> ref : this.builders) {
            if (!ref.excludeFromRegularBuild()) {
               T result = test.apply(ref.getBuilder(builderManager, executionContext, parentSpawnable), meta);
               if (result != null) {
                  return result;
               }
            }
         }

         return emptyResult;
      }
   }

   public <A> void forEach(
      @Nonnull BiConsumer<Builder<V>, A> action,
      @Nonnull A argument,
      @Nonnull BuilderManager builderManager,
      @Nonnull ExecutionContext executionContext,
      @Nonnull Builder<?> parent
   ) {
      if (!this.hasNoElements()) {
         assert this.builders != null;

         for (BuilderObjectReferenceHelper<V> builderObjectReferenceHelper : this.builders) {
            if (!builderObjectReferenceHelper.excludeFromRegularBuild()) {
               Builder<V> builder = builderObjectReferenceHelper.getBuilder(builderManager, executionContext, parent);
               action.accept(builder, argument);
            }
         }
      }
   }
}
