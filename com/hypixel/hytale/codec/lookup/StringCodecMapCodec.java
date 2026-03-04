package com.hypixel.hytale.codec.lookup;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.builder.StringTreeMap;
import com.hypixel.hytale.codec.util.RawJsonReader;
import java.io.IOException;
import java.util.concurrent.locks.StampedLock;
import javax.annotation.Nonnull;

public abstract class StringCodecMapCodec<T, C extends Codec<? extends T>> extends ACodecMapCodec<String, T, C> {
   protected final StampedLock stampedLock = new StampedLock();
   protected final StringTreeMap<C> stringTreeMap = new StringTreeMap<>();

   public StringCodecMapCodec() {
      super(Codec.STRING);
   }

   public StringCodecMapCodec(boolean allowDefault) {
      super(Codec.STRING, allowDefault);
   }

   public StringCodecMapCodec(String id) {
      super(id, Codec.STRING);
   }

   public StringCodecMapCodec(String key, boolean allowDefault) {
      super(key, Codec.STRING, allowDefault);
   }

   public StringCodecMapCodec(String key, boolean allowDefault, boolean encodeDefaultKey) {
      super(key, Codec.STRING, allowDefault, encodeDefaultKey);
   }

   public StringCodecMapCodec<T, C> register(@Nonnull Priority priority, @Nonnull String id, Class<? extends T> aClass, C codec) {
      long lock = this.stampedLock.readLock();

      try {
         this.stringTreeMap.put(id, codec);
      } finally {
         this.stampedLock.unlockRead(lock);
      }

      return (StringCodecMapCodec<T, C>)super.register(priority, id, aClass, codec);
   }

   @Override
   public void remove(Class<? extends T> aClass) {
      String id = this.classToId.get(aClass);
      if (id != null) {
         long lock = this.stampedLock.readLock();

         try {
            this.stringTreeMap.remove(id);
         } finally {
            this.stampedLock.unlockRead(lock);
         }

         super.remove(aClass);
      }
   }

   @Override
   public T decodeJson(@Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo) throws IOException {
      reader.mark();
      C codec = null;
      int distance = 0;
      if (RawJsonReader.seekToKey(reader, this.key)) {
         distance = reader.getMarkDistance();
         long lock = this.stampedLock.readLock();

         try {
            StringTreeMap<C> entry = this.stringTreeMap.findEntry(reader);
            codec = entry == null ? null : entry.getValue();
         } finally {
            this.stampedLock.unlockRead(lock);
         }
      }

      extraInfo.ignoreUnusedKey(this.key);

      String id;
      try {
         if (codec != null) {
            reader.reset();
            return (T)codec.decodeJson(reader, extraInfo);
         }

         C defaultCodec = this.getDefaultCodec();
         if (defaultCodec == null) {
            if (distance == 0) {
               throw new ACodecMapCodec.UnknownIdException("No codec registered with for '" + this.key + "': null");
            }

            reader.skip(distance - reader.getMarkDistance());
            id = reader.readString();
            throw new ACodecMapCodec.UnknownIdException("No codec registered with for '" + this.key + "': " + id);
         }

         reader.reset();
         id = defaultCodec.decodeJson(reader, extraInfo);
      } finally {
         extraInfo.popIgnoredUnusedKey();
      }

      return (T)id;
   }
}
