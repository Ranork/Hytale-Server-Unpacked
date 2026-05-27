package com.hypixel.hytale.server.core.asset.common;

import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.util.io.MemorySegmentUtil;
import java.lang.foreign.MemorySegment;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OggVorbisInfoCache {
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final MemorySegment VORBIS_ID_HEADER = MemorySegment.ofArray(new byte[]{1, 118, 111, 114, 98, 105, 115});
   private static final int OGGS_MAGIC = 1332176723;
   private static final Map<String, OggVorbisInfoCache.OggVorbisInfo> vorbisFiles = new ConcurrentHashMap<>();

   @Nonnull
   public static CompletableFuture<OggVorbisInfoCache.OggVorbisInfo> get(String name) {
      OggVorbisInfoCache.OggVorbisInfo info = vorbisFiles.get(name);
      if (info != null) {
         return CompletableFuture.completedFuture(info);
      } else {
         CommonAsset asset = CommonAssetRegistry.getByName(name);
         return asset == null ? CompletableFuture.completedFuture(null) : get0(asset);
      }
   }

   @Nonnull
   public static CompletableFuture<OggVorbisInfoCache.OggVorbisInfo> get(@Nonnull CommonAsset asset) {
      OggVorbisInfoCache.OggVorbisInfo info = vorbisFiles.get(asset.getName());
      return info != null ? CompletableFuture.completedFuture(info) : get0(asset);
   }

   @Nullable
   public static OggVorbisInfoCache.OggVorbisInfo getNow(String name) {
      OggVorbisInfoCache.OggVorbisInfo info = vorbisFiles.get(name);
      if (info != null) {
         return info;
      } else {
         CommonAsset asset = CommonAssetRegistry.getByName(name);
         return asset == null ? null : get0(asset).join();
      }
   }

   public static OggVorbisInfoCache.OggVorbisInfo getNow(@Nonnull CommonAsset asset) {
      OggVorbisInfoCache.OggVorbisInfo info = vorbisFiles.get(asset.getName());
      return info != null ? info : get0(asset).join();
   }

   @Nonnull
   private static CompletableFuture<OggVorbisInfoCache.OggVorbisInfo> get0(@Nonnull CommonAsset asset) {
      String name = asset.getName();
      return CompletableFutureUtil._catch(asset.getBlob().thenApply(bytes -> {
         OggVorbisInfoCache.OggVorbisInfo info = parse(bytes);
         vorbisFiles.put(name, info);
         return info;
      }));
   }

   @Nonnull
   static OggVorbisInfoCache.OggVorbisInfo parse(byte[] bytes) {
      MemorySegment segment = MemorySegment.ofArray(bytes);
      int len = bytes.length;
      int id = -1;
      int i = 0;

      for (int end = len - 7; i <= end; i++) {
         if (bytes[i] == 1 && segment.asSlice(i, 7L).mismatch(VORBIS_ID_HEADER) == -1L) {
            id = i;
            break;
         }
      }

      if (id >= 0 && id + 16 <= len) {
         i = Byte.toUnsignedInt(bytes[id + 11]);
         int sampleRate = segment.get(MemorySegmentUtil.INT_LE, (long)(id + 12));
         double duration = -1.0;
         if (sampleRate > 0) {
            for (int ix = Math.max(0, len - 14); ix >= 0; ix--) {
               if (bytes[ix] == 79 && segment.get(MemorySegmentUtil.INT_BE, (long)ix) == 1332176723) {
                  int headerType = Byte.toUnsignedInt(bytes[ix + 5]);
                  if ((headerType & 4) != 0) {
                     long granule = segment.get(MemorySegmentUtil.LONG_LE, (long)(ix + 6));
                     if (granule >= 0L) {
                        duration = (double)granule / sampleRate;
                     }
                     break;
                  }
               }
            }
         }

         return new OggVorbisInfoCache.OggVorbisInfo(i, sampleRate, duration);
      } else {
         throw new IllegalArgumentException("Vorbis id header not found");
      }
   }

   public static void invalidate(String name) {
      vorbisFiles.remove(name);
   }

   public static class OggVorbisInfo {
      public final int channels;
      public final int sampleRate;
      public final double duration;

      OggVorbisInfo(int channels, int sampleRate, double duration) {
         this.channels = channels;
         this.sampleRate = sampleRate;
         this.duration = duration;
      }

      @Nonnull
      @Override
      public String toString() {
         return "OggVorbisInfo{channels=" + this.channels + ", sampleRate=" + this.sampleRate + ", duration=" + this.duration + "}";
      }
   }
}
