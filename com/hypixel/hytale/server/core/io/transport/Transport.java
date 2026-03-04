package com.hypixel.hytale.server.core.io.transport;

import io.netty.channel.ChannelFuture;
import java.net.InetSocketAddress;

public interface Transport {
   TransportType getType();

   ChannelFuture bind(InetSocketAddress var1) throws InterruptedException;

   void shutdown();
}
