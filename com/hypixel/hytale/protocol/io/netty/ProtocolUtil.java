package com.hypixel.hytale.protocol.io.netty;

import com.hypixel.hytale.protocol.NetworkChannel;
import io.netty.util.AttributeKey;
import java.time.Duration;

public final class ProtocolUtil {
   public static final AttributeKey<NetworkChannel> STREAM_CHANNEL_KEY = AttributeKey.newInstance("STREAM_CHANNEL_ID");
   public static final AttributeKey<Duration> PACKET_TIMEOUT_KEY = AttributeKey.newInstance("PACKET_TIMEOUT");

   private ProtocolUtil() {
   }
}
