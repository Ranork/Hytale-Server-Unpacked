package com.hypixel.hytale.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Locale;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NetworkUtil {
   public static Inet6Address ANY_IPV6_ADDRESS;
   public static Inet4Address ANY_IPV4_ADDRESS;
   public static Inet6Address LOOPBACK_IPV6_ADDRESS;
   public static Inet4Address LOOPBACK_IPV4_ADDRESS;

   @Nullable
   public static InetAddress getFirstNonLoopbackAddress() throws SocketException {
      InetAddress firstInet6Address = null;
      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

      while (networkInterfaces.hasMoreElements()) {
         NetworkInterface networkInterface = networkInterfaces.nextElement();
         if (!networkInterface.isLoopback() && networkInterface.isUp()) {
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

            while (inetAddresses.hasMoreElements()) {
               InetAddress inetAddress = inetAddresses.nextElement();
               if (!inetAddress.isLoopbackAddress() && !inetAddress.isAnyLocalAddress() && !inetAddress.isLinkLocalAddress()) {
                  if (inetAddress instanceof Inet4Address) {
                     return inetAddress;
                  }

                  if (inetAddress instanceof Inet6Address && firstInet6Address == null) {
                     firstInet6Address = inetAddress;
                  }
               }
            }
         }
      }

      return firstInet6Address;
   }

   @Nullable
   public static InetAddress getFirstAddressWith(NetworkUtil.AddressType... include) throws SocketException {
      return getFirstAddressWith(include, null);
   }

   @Nullable
   public static InetAddress getFirstAddressWithout(NetworkUtil.AddressType... include) throws SocketException {
      return getFirstAddressWith(null, include);
   }

   @Nullable
   public static InetAddress getFirstAddressWith(@Nullable NetworkUtil.AddressType[] include, @Nullable NetworkUtil.AddressType[] exclude) throws SocketException {
      InetAddress firstInet6Address = null;
      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

      while (networkInterfaces.hasMoreElements()) {
         NetworkInterface networkInterface = networkInterfaces.nextElement();
         if (!networkInterface.isLoopback() && networkInterface.isUp()) {
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

            label70:
            while (inetAddresses.hasMoreElements()) {
               InetAddress inetAddress = inetAddresses.nextElement();
               if (include != null) {
                  for (NetworkUtil.AddressType addressType : include) {
                     if (!addressType.predicate.test(inetAddress)) {
                        continue label70;
                     }
                  }
               }

               if (exclude != null) {
                  for (NetworkUtil.AddressType addressTypex : exclude) {
                     if (addressTypex.predicate.test(inetAddress)) {
                        continue label70;
                     }
                  }
               }

               if (inetAddress instanceof Inet4Address) {
                  return inetAddress;
               }

               if (inetAddress instanceof Inet6Address && firstInet6Address == null) {
                  firstInet6Address = inetAddress;
               }
            }
         }
      }

      return firstInet6Address;
   }

   public static boolean addressMatchesAll(InetAddress address, @Nonnull NetworkUtil.AddressType... types) {
      for (NetworkUtil.AddressType type : types) {
         if (!type.predicate.test(address)) {
            return false;
         }
      }

      return true;
   }

   public static boolean addressMatchesAny(InetAddress address) {
      return addressMatchesAny(address, NetworkUtil.AddressType.values());
   }

   public static boolean addressMatchesAny(InetAddress address, @Nonnull NetworkUtil.AddressType... types) {
      for (NetworkUtil.AddressType type : types) {
         if (type.predicate.test(address)) {
            return true;
         }
      }

      return false;
   }

   @Nonnull
   public static String toSocketString(@Nonnull InetSocketAddress address) {
      String str;
      if (address.getAddress() instanceof Inet6Address) {
         String host = address.getHostString();
         if (host.indexOf(58) >= 0) {
            str = "[" + host + "]";
         } else {
            str = host;
         }

         str = str + ":" + address.getPort();
      } else {
         str = address.getHostString() + ":" + address.getPort();
      }

      return str;
   }

   @Nullable
   public static String getHostName() {
      String localhost = null;

      try {
         InetAddress localHost = InetAddress.getLocalHost();
         localhost = localHost.getHostName();
         if (isAcceptableHostName(localhost)) {
            return localhost;
         }

         String hostName = localHost.getCanonicalHostName();
         if (isAcceptableHostName(hostName)) {
            return hostName;
         }
      } catch (UnknownHostException var10) {
      }

      String hostName = System.getenv("HOSTNAME");
      if (isAcceptableHostName(hostName)) {
         return hostName;
      } else {
         hostName = System.getenv("COMPUTERNAME");
         if (isAcceptableHostName(hostName)) {
            return hostName;
         } else {
            hostName = firstLineIfExists("/etc/hostname");
            if (isAcceptableHostName(hostName)) {
               return hostName;
            } else {
               hostName = firstLineIfExists("/proc/sys/kernel/hostname");
               if (isAcceptableHostName(hostName)) {
                  return hostName;
               } else {
                  try {
                     Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();

                     while (en.hasMoreElements()) {
                        NetworkInterface ni = en.nextElement();
                        if (ni.isUp() && !ni.isLoopback() && !ni.isPointToPoint()) {
                           String name = ni.getName().toLowerCase(Locale.ROOT);
                           if (!name.startsWith("lo")
                              && !name.startsWith("docker")
                              && !name.startsWith("br-")
                              && !name.startsWith("veth")
                              && !name.startsWith("virbr")
                              && !name.startsWith("utun")
                              && !name.startsWith("wg")
                              && !name.startsWith("zt")) {
                              Enumeration<InetAddress> e = ni.getInetAddresses();

                              while (e.hasMoreElements()) {
                                 InetAddress a = e.nextElement();
                                 if (!a.isLoopbackAddress() && !a.isLinkLocalAddress() && !a.isAnyLocalAddress()) {
                                    String hostAddress = a.getHostAddress();
                                    String addressHostName = a.getHostName();
                                    if (addressHostName != null && !addressHostName.equals(hostAddress) && isAcceptableHostName(addressHostName)) {
                                       return addressHostName;
                                    }

                                    String canonicalHostName = a.getCanonicalHostName();
                                    if (canonicalHostName != null && !canonicalHostName.equals(hostAddress) && isAcceptableHostName(canonicalHostName)) {
                                       return canonicalHostName;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  } catch (SocketException var11) {
                  }

                  return null;
               }
            }
         }
      }
   }

   @Nullable
   private static String firstLineIfExists(String path) {
      try {
         Path p = Path.of(path);
         if (!Files.isRegularFile(p)) {
            return null;
         } else {
            String var4;
            try (BufferedReader reader = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
               String line = reader.readLine();
               var4 = line == null ? null : line.trim();
            }

            return var4;
         }
      } catch (IOException var7) {
         return null;
      }
   }

   private static boolean isAcceptableHostName(@Nullable String name) {
      if (name == null) {
         return false;
      } else {
         name = name.trim();
         if (name.isEmpty()) {
            return false;
         } else {
            String lower = name.toLowerCase(Locale.ROOT);
            if (isIPv4Literal(lower) || isLikelyIPv6Literal(lower)) {
               return false;
            } else if ("localhost".equals(lower) || "ip6-localhost".equals(lower) || "ip6-loopback".equals(lower) || "docker-desktop".equals(lower)) {
               return false;
            } else {
               return !lower.contains("docker") && !lower.contains("wsl") && !lower.endsWith(".internal") && !lower.endsWith(".localdomain")
                  ? !lower.endsWith(".local")
                  : false;
            }
         }
      }
   }

   private static boolean isIPv4Literal(@Nonnull String name) {
      int dots = 0;
      int octet = -1;
      int val = 0;

      for (int i = 0; i < name.length(); i++) {
         char ch = name.charAt(i);
         if (ch >= '0' && ch <= '9') {
            if (octet == -1) {
               octet = 0;
            }

            val = val * 10 + (ch - '0');
            if (val > 255) {
               return false;
            }

            if (++octet > 3) {
               return false;
            }
         } else {
            if (ch != '.') {
               return false;
            }

            if (octet <= 0) {
               return false;
            }

            dots++;
            octet = -1;
            val = 0;
            if (dots > 3) {
               return false;
            }
         }
      }

      return dots == 3 && octet > 0;
   }

   private static boolean isLikelyIPv6Literal(@Nonnull String name) {
      boolean colon = false;

      for (int i = 0; i < name.length(); i++) {
         char ch = name.charAt(i);
         if (ch == ':') {
            colon = true;
         } else if ((ch < '0' || ch > '9') && (ch < 'a' || ch > 'f') && (ch < 'A' || ch > 'F')) {
            return false;
         }
      }

      return colon;
   }

   static {
      try {
         ANY_IPV6_ADDRESS = Inet6Address.getByAddress("::", new byte[16], null);
         ANY_IPV4_ADDRESS = (Inet4Address)Inet4Address.getByAddress("0.0.0.0", new byte[4]);
         LOOPBACK_IPV6_ADDRESS = Inet6Address.getByAddress("::1", new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, null);
         LOOPBACK_IPV4_ADDRESS = (Inet4Address)Inet4Address.getByAddress("127.0.0.1", new byte[]{127, 0, 0, 1});
      } catch (UnknownHostException var1) {
         throw new RuntimeException(var1);
      }
   }

   public static enum AddressType {
      MULTICAST(InetAddress::isMulticastAddress),
      ANY_LOCAL(InetAddress::isAnyLocalAddress),
      LOOPBACK(InetAddress::isLoopbackAddress),
      LINK_LOCAL(InetAddress::isLinkLocalAddress),
      SITE_LOCAL(InetAddress::isSiteLocalAddress),
      MC_GLOBAL(InetAddress::isMCGlobal),
      MC_SITE_LOCAL(InetAddress::isMCSiteLocal),
      MC_ORG_LOCAL(InetAddress::isMCOrgLocal);

      private final Predicate<InetAddress> predicate;

      private AddressType(Predicate<InetAddress> predicate) {
         this.predicate = predicate;
      }
   }
}
