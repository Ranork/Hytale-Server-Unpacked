package com.hypixel.hytale.component.spatial;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Resource;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import javax.annotation.Nonnull;

public class SpatialResource<T, ECS_TYPE> implements Resource<ECS_TYPE> {
   @Nonnull
   private static final ThreadLocal<ObjectList<Ref<?>>> THREAD_LOCAL_REFERENCE_LIST = ThreadLocal.withInitial(ObjectArrayList::new);
   @Nonnull
   private final SpatialData<Ref<ECS_TYPE>> spatialData = new SpatialData<>();
   @Nonnull
   private final SpatialStructure<T> spatialStructure;

   @Nonnull
   public static <ECS_TYPE> ObjectList<Ref<ECS_TYPE>> getThreadLocalReferenceList() {
      ObjectList list = THREAD_LOCAL_REFERENCE_LIST.get();
      list.clear();
      return list;
   }

   public SpatialResource(@Nonnull SpatialStructure<T> spatialStructure) {
      this.spatialStructure = spatialStructure;
   }

   @Nonnull
   public SpatialData<Ref<ECS_TYPE>> getSpatialData() {
      return this.spatialData;
   }

   @Nonnull
   public SpatialStructure<T> getSpatialStructure() {
      return this.spatialStructure;
   }

   @Override
   public Resource<ECS_TYPE> clone() {
      throw new UnsupportedOperationException("Not supported yet.");
   }
}
