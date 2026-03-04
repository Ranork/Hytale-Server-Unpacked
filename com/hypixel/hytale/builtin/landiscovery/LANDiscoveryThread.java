package com.hypixel.hytale.builtin.landiscovery;

import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.io.ServerManager;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.io.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import javax.annotation.Nonnull;

class LANDiscoveryThread extends Thread {
   private static final byte[] REPLY_HEADER = "HYTALE_DISCOVER_REPLY".getBytes(StandardCharsets.US_ASCII);
   private static final byte[] REQUEST_HEADER = "HYTALE_DISCOVER_REQUEST".getBytes(StandardCharsets.US_ASCII);
   public static final int LAN_DISCOVERY_PORT = 5510;
   @Nonnull
   private final HytaleLogger LOGGER;
   private MulticastSocket socket;

   public LANDiscoveryThread() {
      super("LAN Discovery Listener");
      this.setDaemon(true);
      this.LOGGER = LANDiscoveryPlugin.get().getLogger();
   }

   @Override
   public void run() {
      try {
         this.socket = new MulticastSocket(5510);
         this.socket.setBroadcast(true);
         this.LOGGER.at(Level.INFO).log("Bound to UDP 0.0.0.0:5510 for LAN discovery");
         String name = HytaleServer.get().getServerName();
         if (name.length() > 16377) {
            name = name.substring(0, 16377) + "...";
         }

         byte[] serverName = name.getBytes(StandardCharsets.UTF_8);
         byte[] receiveBuf = new byte[15000];
         DatagramPacket packet = new DatagramPacket(receiveBuf, receiveBuf.length);

         while (!this.isInterrupted()) {
            this.socket.receive(packet);
            if (ArrayUtil.startsWith(packet.getData(), REQUEST_HEADER)) {
               InetSocketAddress publicAddress = ServerManager.get().getNonLoopbackAddress();
               if (publicAddress != null) {
                  ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
                  buf.writeBytes(REPLY_HEADER);
                  buf.writeByte(0);
                  InetAddress address = publicAddress.getAddress();
                  if (address != null && !address.isLoopbackAddress()) {
                     if (address instanceof Inet4Address) {
                        buf.writeByte(4);
                     } else {
                        if (!(address instanceof Inet6Address)) {
                           this.LOGGER.at(Level.WARNING).log("Unrecognized target address class %s: %s", address.getClass(), address);
                           continue;
                        }

                        buf.writeByte(16);
                     }

                     buf.writeBytes(address.getAddress());
                     buf.writeShortLE(publicAddress.getPort());
                     buf.writeShortLE(serverName.length);
                     buf.writeBytes(serverName);
                     buf.writeIntLE(Universe.get().getPlayerCount());
                     int maxPlayers = HytaleServer.get().getConfig().getMaxPlayers();
                     buf.writeIntLE(Math.max(maxPlayers, 0));
                     byte[] sendData = ByteBufUtil.getBytesRelease(buf);
                     DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                     this.socket.send(sendPacket);
                     this.LOGGER.at(Level.FINE).log("Was discovered by %s:%d", packet.getAddress(), packet.getPort());
                  } else {
                     this.LOGGER.at(Level.WARNING).log("No public address to send as response!");
                  }
               }
            }
         }
      } catch (SocketException var14) {
         if (!"Socket closed".equalsIgnoreCase(var14.getMessage()) && !"Socket is closed".equalsIgnoreCase(var14.getMessage())) {
            ((HytaleLogger.Api)this.LOGGER.at(Level.SEVERE).withCause(var14)).log("Exception in lan discovery listener:");
         }
      } catch (Throwable var15) {
         ((HytaleLogger.Api)this.LOGGER.at(Level.SEVERE).withCause(var15)).log("Exception in lan discovery listener:");
      } finally {
         if (this.socket != null) {
            this.socket.close();
         }
      }

      this.LOGGER.at(Level.INFO).log("Stopped listing on UDP 0.0.0.0:5510 for LAN discovery");
   }

   public MulticastSocket getSocket() {
      return this.socket;
   }
}
