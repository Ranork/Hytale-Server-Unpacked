package com.hypixel.hytale.builtin.hytalegenerator.framework.shaders;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class RelationalShader<T> implements Shader<T> {
   @Nonnull
   private final Map<T, Shader<T>> relations;
   @Nonnull
   private final Shader<T> onMissingKey;

   public RelationalShader(@Nonnull Shader<T> onMissingKey) {
      this.onMissingKey = onMissingKey;
      this.relations = new HashMap<>(1);
   }

   @Nonnull
   public RelationalShader<T> addRelation(@Nonnull T key, @Nonnull Shader<T> value) {
      this.relations.put(key, value);
      return this;
   }

   @Override
   public T shade(T current, long seed) {
      return !this.relations.containsKey(current) ? this.onMissingKey.shade(current, seed) : this.relations.get(current).shade(current, seed);
   }

   @Override
   public T shade(T current, long seedA, long seedB) {
      return !this.relations.containsKey(current) ? this.onMissingKey.shade(current, seedA, seedB) : this.relations.get(current).shade(current, seedA, seedB);
   }

   @Override
   public T shade(T current, long seedA, long seedB, long seedC) {
      return !this.relations.containsKey(current)
         ? this.onMissingKey.shade(current, seedA, seedB, seedC)
         : this.relations.get(current).shade(current, seedA, seedB, seedC);
   }

   @Nonnull
   @Override
   public String toString() {
      return "RelationalShader{relations=" + this.relations + ", onMissingKey=" + this.onMissingKey + "}";
   }
}
