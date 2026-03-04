package com.hypixel.hytale.server.npc.asset.builder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderObjectStaticListHelper<T> extends BuilderObjectListHelper<T> {
   public BuilderObjectStaticListHelper(Class<?> classType, BuilderContext owner) {
      super(classType, owner);
   }

   @Nonnull
   @Override
   protected BuilderObjectReferenceHelper<T> createReferenceHelper() {
      return new BuilderObjectStaticHelper<>(this.classType, this);
   }

   @Nullable
   public List<T> staticBuild(@Nonnull BuilderManager manager) {
      if (this.hasNoElements()) {
         return null;
      } else {
         List<T> objects = new ObjectArrayList();

         for (BuilderObjectReferenceHelper<T> builder : this.builders) {
            T obj = ((BuilderObjectStaticHelper)builder).staticBuild(manager);
            if (obj != null) {
               objects.add(obj);
            }
         }

         return objects;
      }
   }
}
