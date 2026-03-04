package com.hypixel.hytale.builtin.hytalegenerator.newsystem.performanceinstruments;

import javax.annotation.Nonnull;

public interface MemInstrument {
   long BYTES_IN_MEGABYTES = 1000000L;
   long INT_SIZE = 4L;
   long DOUBLE_SIZE = 8L;
   long BOOLEAN_SIZE = 1L;
   long OBJECT_REFERENCE_SIZE = 8L;
   long OBJECT_HEADER_SIZE = 16L;
   long ARRAY_HEADER_SIZE = 16L;
   long CLASS_OBJECT_SIZE = 128L;
   long ARRAYLIST_OBJECT_SIZE = 24L;
   long VECTOR3I_SIZE = 28L;
   long VECTOR3D_SIZE = 40L;
   long HASHMAP_ENTRY_SIZE = 32L;

   @Nonnull
   MemInstrument.Report getMemoryUsage();

   public record Report(long size_bytes) {
      public Report(long size_bytes) {
         assert size_bytes >= 0L;

         this.size_bytes = size_bytes;
      }
   }
}
