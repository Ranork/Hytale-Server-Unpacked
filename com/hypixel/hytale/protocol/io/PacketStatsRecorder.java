package com.hypixel.hytale.protocol.io;

import io.netty.util.AttributeKey;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface PacketStatsRecorder {
   AttributeKey<PacketStatsRecorder> CHANNEL_KEY = AttributeKey.valueOf("PacketStatsRecorder");
   PacketStatsRecorder NOOP = new NoopPacketStatsRecorder();

   void recordSend(int var1, int var2, int var3);

   void recordReceive(int var1, int var2, int var3);

   @Nonnull
   PacketStatsRecorder.PacketStatsEntry getEntry(int var1);

   public interface PacketStatsEntry {
      int RECENT_SECONDS = 30;

      int getPacketId();

      @Nullable
      String getName();

      boolean hasData();

      int getSentCount();

      long getSentUncompressedTotal();

      long getSentCompressedTotal();

      long getSentUncompressedMin();

      long getSentUncompressedMax();

      long getSentCompressedMin();

      long getSentCompressedMax();

      double getSentUncompressedAvg();

      double getSentCompressedAvg();

      @Nonnull
      PacketStatsRecorder.RecentStats getSentRecently();

      int getReceivedCount();

      long getReceivedUncompressedTotal();

      long getReceivedCompressedTotal();

      long getReceivedUncompressedMin();

      long getReceivedUncompressedMax();

      long getReceivedCompressedMin();

      long getReceivedCompressedMax();

      double getReceivedUncompressedAvg();

      double getReceivedCompressedAvg();

      @Nonnull
      PacketStatsRecorder.RecentStats getReceivedRecently();
   }

   public record RecentStats(
      int count, long uncompressedTotal, long compressedTotal, int uncompressedMin, int uncompressedMax, int compressedMin, int compressedMax
   ) {
      public static final PacketStatsRecorder.RecentStats EMPTY = new PacketStatsRecorder.RecentStats(0, 0L, 0L, 0, 0, 0, 0);
   }
}
