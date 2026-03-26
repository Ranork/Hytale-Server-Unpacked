package com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.type;

import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.Buffer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class BufferType {
   @Nonnull
   public final Class bufferClass;
   public final int index;
   @Nonnull
   public final Supplier<Buffer> bufferSupplier;
   @Nonnull
   public final String name;

   public BufferType(@Nonnull String name, int index, @Nonnull Class bufferClass, @Nonnull Supplier<Buffer> bufferSupplier) {
      this.name = name;
      this.index = index;
      this.bufferClass = bufferClass;
      this.bufferSupplier = bufferSupplier;
   }

   @Override
   public boolean equals(Object o) {
      return !(o instanceof BufferType that)
         ? false
         : this.index == that.index && this.bufferClass.equals(that.bufferClass) && this.bufferSupplier.equals(that.bufferSupplier);
   }

   public boolean isValidType(@Nonnull Class bufferClass) {
      return this.bufferClass.equals(bufferClass);
   }

   public boolean isValid(@Nonnull Buffer buffer) {
      return this.bufferClass.isInstance(buffer);
   }

   @Override
   public int hashCode() {
      int result = this.bufferClass.hashCode();
      result = 31 * result + this.index;
      return 31 * result + this.bufferSupplier.hashCode();
   }
}
