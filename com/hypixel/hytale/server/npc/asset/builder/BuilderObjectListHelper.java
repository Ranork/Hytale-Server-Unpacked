package com.hypixel.hytale.server.npc.asset.builder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderObjectListHelper<T> extends BuilderObjectArrayHelper<List<T>, T> {
   public BuilderObjectListHelper(Class<?> classType, BuilderContext owner) {
      super(classType, owner);
   }

   @Nullable
   public List<T> build(@Nonnull BuilderSupport builderSupport) {
      if (this.hasNoElements()) {
         return null;
      } else {
         List<T> objects = new ObjectArrayList();

         for (BuilderObjectReferenceHelper<T> builder : this.builders) {
            if (!builder.excludeFromRegularBuild()) {
               T obj = builder.build(builderSupport);
               if (obj != null) {
                  objects.add(obj);
               }
            }
         }

         return objects;
      }
   }
}
