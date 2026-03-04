package com.hypixel.hytale.server.npc.storage;

import com.hypixel.hytale.server.core.entity.Entity;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public abstract class ParameterStore<Type extends PersistentParameter<?>> {
   protected Map<String, Type> parameters = new HashMap<>();

   protected ParameterStore() {
   }

   public Type get(@Nonnull Entity owner, String name) {
      Type parameter = this.parameters.get(name);
      if (parameter == null) {
         parameter = this.createParameter();
         this.parameters.put(name, parameter);
         owner.markNeedsSave();
      }

      return parameter;
   }

   protected abstract Type createParameter();
}
