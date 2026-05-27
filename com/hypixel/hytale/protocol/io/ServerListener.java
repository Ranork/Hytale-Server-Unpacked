package com.hypixel.hytale.protocol.io;

import java.net.SocketAddress;
import java.util.concurrent.Future;

public interface ServerListener {
   Future<Void> close();

   SocketAddress localAddress();
}
