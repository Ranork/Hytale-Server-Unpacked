package com.hypixel.hytale.builtin.landiscovery;

import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.io.ServerManager;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.io.MemorySegmentUtil;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.MemoryLayout.PathElement;
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

   private static LANDiscoveryThread.ReplyPacket createReplyPacket(int addrSize, byte[] serverName) {
      StructLayout layout = MemoryLayout.structLayout(
         MemoryLayout.sequenceLayout(REPLY_HEADER.length, ValueLayout.JAVA_BYTE).withName("header"),
         ValueLayout.JAVA_BYTE.withName("separator"),
         ValueLayout.JAVA_BYTE.withName("addrType"),
         MemoryLayout.sequenceLayout(addrSize, ValueLayout.JAVA_BYTE).withName("address"),
         MemorySegmentUtil.SHORT_LE.withName("port"),
         MemorySegmentUtil.SHORT_LE.withName("nameLength"),
         MemoryLayout.sequenceLayout(serverName.length, ValueLayout.JAVA_BYTE).withName("name"),
         MemorySegmentUtil.INT_LE.withName("playerCount"),
         MemorySegmentUtil.INT_LE.withName("maxPlayers")
      );
      byte[] bytes = new byte[(int)layout.byteSize()];
      MemorySegment segment = MemorySegment.ofArray(bytes);
      MemorySegment.copy(REPLY_HEADER, 0, segment, ValueLayout.JAVA_BYTE, 0L, REPLY_HEADER.length);
      segment.set(ValueLayout.JAVA_BYTE, layout.byteOffset(PathElement.groupElement("addrType")), (byte)addrSize);
      segment.set(MemorySegmentUtil.SHORT_LE, layout.byteOffset(PathElement.groupElement("nameLength")), (short)serverName.length);
      MemorySegment.copy(serverName, 0, segment, ValueLayout.JAVA_BYTE, layout.byteOffset(PathElement.groupElement("name")), serverName.length);
      return new LANDiscoveryThread.ReplyPacket(
         bytes,
         segment,
         layout.byteOffset(PathElement.groupElement("address")),
         layout.byteOffset(PathElement.groupElement("port")),
         layout.byteOffset(PathElement.groupElement("playerCount")),
         layout.byteOffset(PathElement.groupElement("maxPlayers"))
      );
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
         LANDiscoveryThread.ReplyPacket ipv4Reply = createReplyPacket(4, serverName);
         LANDiscoveryThread.ReplyPacket ipv6Reply = createReplyPacket(16, serverName);
         int maxPlayers = HytaleServer.get().getConfig().getMaxPlayers();
         byte[] receiveBuf = new byte[15000];
         DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
         DatagramPacket sendPacket = new DatagramPacket(new byte[0], 0);

         while (!this.isInterrupted()) {
            this.socket.receive(receivePacket);
            if (ArrayUtil.startsWith(receivePacket.getData(), REQUEST_HEADER)) {
               InetSocketAddress publicAddress = ServerManager.get().getNonLoopbackAddress();
               if (publicAddress != null) {
                  InetAddress address = publicAddress.getAddress();
                  if (address != null && !address.isLoopbackAddress()) {
                     LANDiscoveryThread.ReplyPacket reply;
                     if (address instanceof Inet4Address) {
                        reply = ipv4Reply;
                     } else {
                        if (!(address instanceof Inet6Address)) {
                           this.LOGGER.at(Level.WARNING).log("Unrecognized target address class %s: %s", address.getClass(), address);
                           continue;
                        }

                        reply = ipv6Reply;
                     }

                     reply.write(address.getAddress(), publicAddress.getPort(), Universe.get().getPlayerCount(), Math.max(maxPlayers, 0));
                     sendPacket.setData(reply.bytes());
                     sendPacket.setSocketAddress(receivePacket.getSocketAddress());
                     this.socket.send(sendPacket);
                     this.LOGGER.at(Level.FINE).log("Was discovered by %s:%d", receivePacket.getAddress(), receivePacket.getPort());
                  } else {
                     this.LOGGER.at(Level.WARNING).log("No public address to send as response!");
                  }
               }
            }
         }
      } catch (SocketException var16) {
         if (!"Socket closed".equalsIgnoreCase(var16.getMessage()) && !"Socket is closed".equalsIgnoreCase(var16.getMessage())) {
            ((HytaleLogger.Api)this.LOGGER.at(Level.SEVERE).withCause(var16)).log("Exception in lan discovery listener:");
         }
      } catch (Throwable var17) {
         ((HytaleLogger.Api)this.LOGGER.at(Level.SEVERE).withCause(var17)).log("Exception in lan discovery listener:");
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

   private record ReplyPacket(byte[] bytes, MemorySegment segment, long addressOffset, long portOffset, long playerCountOffset, long maxPlayersOffset) {
      void write(byte[] addrBytes, int port, int playerCount, int maxPlayers) {
         MemorySegment.copy(addrBytes, 0, this.segment, ValueLayout.JAVA_BYTE, this.addressOffset, addrBytes.length);
         this.segment.set(MemorySegmentUtil.SHORT_LE, this.portOffset, (short)port);
         this.segment.set(MemorySegmentUtil.INT_LE, this.playerCountOffset, playerCount);
         this.segment.set(MemorySegmentUtil.INT_LE, this.maxPlayersOffset, maxPlayers);
      }
   }
}
