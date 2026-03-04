package com.hypixel.hytale.server.core.asset.common;

import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OggVorbisInfoCache {
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
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
      return CompletableFutureUtil._catch(
         asset.getBlob()
            .thenApply(
               bytes -> {
                  ByteBuf b = Unpooled.wrappedBuffer(bytes);

                  OggVorbisInfoCache.OggVorbisInfo var21;
                  try {
                     int len = b.readableBytes();
                     int id = -1;
                     int i = 0;

                     for (int end = len - 7; i <= end; i++) {
                        i = b.indexOf(i, len - 7, (byte)1);
                        if (i == -1) {
                           break;
                        }

                        if (b.getByte(i + 1) == 118
                           && b.getByte(i + 2) == 111
                           && b.getByte(i + 3) == 114
                           && b.getByte(i + 4) == 98
                           && b.getByte(i + 5) == 105
                           && b.getByte(i + 6) == 115) {
                           id = i;
                           break;
                        }
                     }

                     if (id < 0 || id + 16 > len) {
                        throw new IllegalArgumentException("Vorbis id header not found");
                     }

                     i = b.getUnsignedByte(id + 11);
                     int sampleRate = b.getIntLE(id + 12);
                     double duration = -1.0;
                     if (sampleRate > 0) {
                        for (int ix = Math.max(0, len - 14); ix >= 0; ix--) {
                           ix = b.indexOf(ix, 0, (byte)79);
                           if (ix == -1) {
                              break;
                           }

                           if (b.getByte(ix + 1) == 103 && b.getByte(ix + 2) == 103 && b.getByte(ix + 3) == 83) {
                              int headerType = b.getUnsignedByte(ix + 5);
                              if ((headerType & 4) != 0) {
                                 long granule = b.getLongLE(ix + 6);
                                 if (granule >= 0L) {
                                    duration = (double)granule / sampleRate;
                                 }
                                 break;
                              }
                           }
                        }
                     }

                     OggVorbisInfoCache.OggVorbisInfo info = new OggVorbisInfoCache.OggVorbisInfo(i, sampleRate, duration);
                     vorbisFiles.put(name, info);
                     var21 = info;
                  } finally {
                     b.release();
                  }

                  return var21;
               }
            )
      );
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
