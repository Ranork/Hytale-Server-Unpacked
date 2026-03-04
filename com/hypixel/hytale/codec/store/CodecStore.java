package com.hypixel.hytale.codec.store;

import com.hypixel.hytale.codec.Codec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class CodecStore {
   public static final CodecStore STATIC = new CodecStore();
   private final CodecStore parent;
   private final Map<CodecKey<?>, Codec<?>> codecs = new ConcurrentHashMap<>();
   private final Map<CodecKey<?>, Supplier<Codec<?>>> codecSuppliers = new ConcurrentHashMap<>();

   public CodecStore() {
      this.parent = STATIC;
   }

   public CodecStore(CodecStore parent) {
      this.parent = parent;
   }

   @Nullable
   public <T> Codec<T> getCodec(CodecKey<T> key) {
      Codec<T> codec = (Codec<T>)this.codecs.get(key);
      if (codec != null) {
         return codec;
      } else {
         Supplier<Codec<?>> supplier = this.codecSuppliers.get(key);
         if (supplier != null) {
            codec = (Codec<T>)supplier.get();
         }

         if (codec != null) {
            return codec;
         } else {
            return this.parent != null ? this.parent.getCodec(key) : null;
         }
      }
   }

   public <T> void putCodec(CodecKey<T> key, Codec<T> codec) {
      this.codecs.put(key, codec);
   }

   public <T> Codec<?> removeCodec(CodecKey<T> key) {
      return this.codecs.remove(key);
   }

   public <T> void putCodecSupplier(CodecKey<T> key, Supplier<Codec<T>> supplier) {
      this.codecSuppliers.put(key, supplier);
   }

   public <T> Supplier<Codec<?>> removeCodecSupplier(CodecKey<T> key) {
      return this.codecSuppliers.remove(key);
   }
}
