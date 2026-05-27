package com.hypixel.hytale.server.core.io.transport;

import com.hypixel.hytale.protocol.io.ServerListener;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;

public interface Transport {
   TransportType getType();

   Future<ServerListener> bind(InetSocketAddress var1) throws InterruptedException;

   void shutdown();
}
