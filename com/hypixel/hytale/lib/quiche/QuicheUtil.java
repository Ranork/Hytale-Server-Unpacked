package com.hypixel.hytale.lib.quiche;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.ValueLayout.OfShort;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import javax.annotation.Nonnull;

public class QuicheUtil {
   private static final OfShort NET_SHORT = ValueLayout.JAVA_SHORT_UNALIGNED.withOrder(ByteOrder.BIG_ENDIAN);
   private static final int AF_INET = 2;
   private static final int AF_INET6;

   public static int toSocketAddrStorage(@Nonnull MemorySegment socketaddr, @Nonnull InetSocketAddress address) {
      byte[] data = address.getAddress().getAddress();
      int len;
      int addrOffset;
      if (address.getAddress() instanceof Inet6Address) {
         QuicheNative.setSockaddrStorageFamily(socketaddr, AF_INET6);
         len = 28;
         addrOffset = 6;
         socketaddr.set(ValueLayout.JAVA_INT_UNALIGNED, QuicheNative.SOCKADDR_STORAGE_DATA_OFFSET + 2L, 0);
      } else {
         QuicheNative.setSockaddrStorageFamily(socketaddr, 2);
         len = 16;
         addrOffset = 2;
      }

      QuicheNative.setSockaddrLen(socketaddr, (byte)len);
      socketaddr.set(NET_SHORT, QuicheNative.SOCKADDR_STORAGE_DATA_OFFSET, (short)address.getPort());
      MemorySegment.copy(data, 0, socketaddr, ValueLayout.JAVA_BYTE, QuicheNative.SOCKADDR_STORAGE_DATA_OFFSET + addrOffset, data.length);
      return len;
   }

   public static InetSocketAddress fromSocketAddr(@Nonnull MemorySegment socketAddr, int socketAddrLen) {
      int family = QuicheNative.getSockaddrStorageFamily(socketAddr);
      int port = Short.toUnsignedInt(socketAddr.get(NET_SHORT, QuicheNative.SOCKADDR_STORAGE_DATA_OFFSET));
      byte[] addrBytes;
      if (family == 2) {
         addrBytes = new byte[4];
         MemorySegment.copy(socketAddr, ValueLayout.JAVA_BYTE, QuicheNative.SOCKADDR_STORAGE_DATA_OFFSET + 2L, addrBytes, 0, 4);
      } else {
         if (family != AF_INET6) {
            throw new IllegalArgumentException("Unknown address family: " + family);
         }

         addrBytes = new byte[16];
         MemorySegment.copy(socketAddr, ValueLayout.JAVA_BYTE, QuicheNative.SOCKADDR_STORAGE_DATA_OFFSET + 6L, addrBytes, 0, 16);
      }

      try {
         InetAddress addr = InetAddress.getByAddress(addrBytes);
         if (addr instanceof Inet6Address && isIPv4Mapped(addrBytes)) {
            addr = InetAddress.getByAddress(new byte[]{addrBytes[12], addrBytes[13], addrBytes[14], addrBytes[15]});
         }

         return new InetSocketAddress(addr, port);
      } catch (UnknownHostException var6) {
         throw new IllegalArgumentException("Invalid address bytes", var6);
      }
   }

   private static boolean isIPv4Mapped(byte[] addr) {
      if (addr.length != 16) {
         return false;
      } else {
         for (int i = 0; i < 10; i++) {
            if (addr[i] != 0) {
               return false;
            }
         }

         return addr[10] == -1 && addr[11] == -1;
      }
   }

   static {
      AF_INET6 = switch (QuicheNative.PLATFORM) {
         case LINUX -> 10;
         case MACOS -> 30;
         case WINDOWS -> 23;
      };
   }
}
