package com.hypixel.hytale.server.core.io;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.common.util.NetworkUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.io.commands.BindingsCommand;
import com.hypixel.hytale.server.core.io.handlers.IPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.SubPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.game.GamePacketHandler;
import com.hypixel.hytale.server.core.io.handlers.game.InventoryPacketHandler;
import com.hypixel.hytale.server.core.io.transport.QUICTransport;
import com.hypixel.hytale.server.core.io.transport.TCPTransport;
import com.hypixel.hytale.server.core.io.transport.Transport;
import com.hypixel.hytale.server.core.io.transport.TransportType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ServerManager extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(ServerManager.class).build();
   @Nonnull
   private static final NetworkUtil.AddressType[] NON_PUBLIC_ADDRESS_TYPES = new NetworkUtil.AddressType[]{
      NetworkUtil.AddressType.ANY_LOCAL,
      NetworkUtil.AddressType.LOOPBACK,
      NetworkUtil.AddressType.SITE_LOCAL,
      NetworkUtil.AddressType.LINK_LOCAL,
      NetworkUtil.AddressType.MULTICAST
   };
   private static ServerManager instance;
   @Nonnull
   private final List<Channel> listeners = new CopyOnWriteArrayList<>();
   @Nonnull
   private final List<Function<IPacketHandler, SubPacketHandler>> subPacketHandlers = new ObjectArrayList();
   @Nullable
   private Transport transport;
   @Nullable
   private CompletableFuture<Void> registerFuture;
   @Nullable
   private CompletableFuture<Void> bootFuture;

   public static ServerManager get() {
      return instance;
   }

   public ServerManager(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
      if (!Options.getOptionSet().has(Options.BARE)) {
         this.init();
      }
   }

   public void init() {
      this.registerFuture = CompletableFutureUtil._catch(CompletableFuture.runAsync(SneakyThrow.sneakyRunnable(() -> {
         long start = System.nanoTime();

         this.transport = (Transport)(switch ((TransportType)Options.getOptionSet().valuesOf(Options.TRANSPORT).getFirst()) {
            case TCP -> new TCPTransport();
            case QUIC -> new QUICTransport();
         });
         this.getLogger().at(Level.INFO).log("Took %s to setup transport!", FormatUtil.nanosToString(System.nanoTime() - start));
         this.registerFuture = null;
      })));
   }

   @Override
   protected void setup() {
      this.getEventRegistry().register((short)-40, ShutdownEvent.class, event -> this.unbindAllListeners());
      get().registerSubPacketHandlers(InventoryPacketHandler::new);
      this.getCommandRegistry().registerCommand(new BindingsCommand());
   }

   @Override
   protected void start() {
      this.bootFuture = CompletableFuture.runAsync(() -> {
         CompletableFuture<Void> registerFuture = this.registerFuture;
         if (registerFuture != null) {
            registerFuture.getNow(null);
         }

         if (!HytaleServer.get().isShuttingDown()) {
            label40:
            if (Options.getOptionSet().has(Options.MIGRATIONS) || Options.getOptionSet().has(Options.BARE)) {
               this.bootFuture = null;
            } else if (Constants.SINGLEPLAYER) {
               try {
                  InetAddress[] localhosts = InetAddress.getAllByName("localhost");
                  InetAddress[] arr$ = localhosts;
                  int len$ = localhosts.length;
                  int i$ = 0;

                  while (true) {
                     if (i$ >= len$) {
                        break label40;
                     }

                     InetAddress localhost = arr$[i$];
                     this.bind(new InetSocketAddress(localhost, ((InetSocketAddress)Options.getOptionSet().valueOf(Options.BIND)).getPort()));
                     i$++;
                  }
               } catch (UnknownHostException var7) {
                  throw SneakyThrow.sneakyThrow(var7);
               }
            } else {
               for (InetSocketAddress address : Options.getOptionSet().valuesOf(Options.BIND)) {
                  this.bind(address);
               }

               if (!this.listeners.isEmpty()) {
                  break label40;
               }

               throw new IllegalArgumentException("Listeners is empty after starting ServerManager!!");
            }
         }
      });
   }

   @Override
   protected void shutdown() {
      Universe.get().disconnectAllPLayers();
      this.unbindAllListeners();
      this.transport.shutdown();
      this.transport = null;
      this.getLogger().at(Level.INFO).log("Finished shutting down ServerManager...");
   }

   public void unbindAllListeners() {
      for (Channel channel : this.listeners) {
         this.unbind0(channel);
      }

      this.listeners.clear();
   }

   @Nonnull
   public List<Channel> getListeners() {
      return Collections.unmodifiableList(this.listeners);
   }

   public boolean bind(@Nonnull InetSocketAddress address) {
      if (address.getAddress().isAnyLocalAddress() && this.transport.getType() == TransportType.QUIC) {
         Channel channelIpv6 = this.bind0(new InetSocketAddress(NetworkUtil.ANY_IPV6_ADDRESS, address.getPort()));
         if (channelIpv6 != null) {
            this.listeners.add(channelIpv6);
         }

         Channel channelIpv4 = this.bind0(new InetSocketAddress(NetworkUtil.ANY_IPV4_ADDRESS, address.getPort()));
         if (channelIpv4 != null) {
            this.listeners.add(channelIpv4);
         }

         Channel channelIpv6Localhost = this.bind0(new InetSocketAddress(NetworkUtil.LOOPBACK_IPV6_ADDRESS, address.getPort()));
         if (channelIpv6Localhost != null) {
            this.listeners.add(channelIpv6Localhost);
         }

         return channelIpv4 != null || channelIpv6 != null;
      } else {
         Channel channel = this.bind0(address);
         if (channel != null) {
            this.listeners.add(channel);
         }

         return channel != null;
      }
   }

   public boolean unbind(@Nonnull Channel channel) {
      boolean success = this.unbind0(channel);
      if (success) {
         this.listeners.remove(channel);
      }

      return success;
   }

   @Nullable
   public InetSocketAddress getLocalOrPublicAddress() throws SocketException {
      for (Channel channel : this.listeners) {
         if (channel.localAddress() instanceof InetSocketAddress inetSocketAddress) {
            InetAddress address = inetSocketAddress.getAddress();
            if (address.isLoopbackAddress()) {
               return inetSocketAddress;
            }

            if (address.isAnyLocalAddress()) {
               InetAddress anyNonLoopbackAddress = NetworkUtil.getFirstNonLoopbackAddress();
               if (anyNonLoopbackAddress == null) {
                  return null;
               }

               return new InetSocketAddress(anyNonLoopbackAddress, inetSocketAddress.getPort());
            }

            return inetSocketAddress;
         }
      }

      return null;
   }

   @Nullable
   public InetSocketAddress getNonLoopbackAddress() throws SocketException {
      for (Channel channel : this.listeners) {
         if (channel.localAddress() instanceof InetSocketAddress inetSocketAddress) {
            InetAddress address = inetSocketAddress.getAddress();
            if (!address.isLoopbackAddress()) {
               if (address.isAnyLocalAddress()) {
                  InetAddress anyNonLoopbackAddress = NetworkUtil.getFirstNonLoopbackAddress();
                  if (anyNonLoopbackAddress == null) {
                     return null;
                  }

                  return new InetSocketAddress(anyNonLoopbackAddress, inetSocketAddress.getPort());
               }

               return inetSocketAddress;
            }
         }
      }

      return null;
   }

   @Nullable
   public InetSocketAddress getPublicAddress() throws SocketException {
      for (Channel channel : this.listeners) {
         if (channel.localAddress() instanceof InetSocketAddress inetSocketAddress) {
            InetAddress address = inetSocketAddress.getAddress();
            if (!address.isLoopbackAddress() && !address.isSiteLocalAddress()) {
               if (address.isAnyLocalAddress()) {
                  InetAddress anyPublicAddress = NetworkUtil.getFirstAddressWithout(NON_PUBLIC_ADDRESS_TYPES);
                  if (anyPublicAddress == null) {
                     return null;
                  }

                  return new InetSocketAddress(anyPublicAddress, inetSocketAddress.getPort());
               }

               return inetSocketAddress;
            }
         }
      }

      return null;
   }

   public void waitForBindComplete() {
      CompletableFuture<Void> future = this.bootFuture;
      if (future != null) {
         future.getNow(null);
      }
   }

   public void registerSubPacketHandlers(@Nonnull Function<IPacketHandler, SubPacketHandler> supplier) {
      this.subPacketHandlers.add(supplier);
   }

   public void populateSubPacketHandlers(@Nonnull GamePacketHandler packetHandler) {
      for (Function<IPacketHandler, SubPacketHandler> subPacketHandler : this.subPacketHandlers) {
         packetHandler.registerSubPacketHandler(subPacketHandler.apply(packetHandler));
      }
   }

   @Nullable
   private Channel bind0(@Nonnull InetSocketAddress address) {
      long start = System.nanoTime();
      this.getLogger().at(Level.FINE).log("Binding to %s (%s)", address, this.transport.getType());

      try {
         ChannelFuture f = this.transport.bind(address).sync();
         if (f.isSuccess()) {
            Channel channel = f.channel();
            this.getLogger().at(Level.INFO).log("Listening on %s and took %s", channel.localAddress(), FormatUtil.nanosToString(System.nanoTime() - start));
            return channel;
         }

         ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(new SkipSentryException(f.cause()))).log("Could not bind to host %s", address);
      } catch (InterruptedException var6) {
         Thread.currentThread().interrupt();
         throw new RuntimeException("Interrupted when attempting to bind to host " + address, var6);
      } catch (Throwable var7) {
         ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(new SkipSentryException(var7))).log("Failed to bind to %s", address);
      }

      return null;
   }

   private boolean unbind0(@Nonnull Channel channel) {
      long start = System.nanoTime();
      this.getLogger().at(Level.FINE).log("Closing listener %s", channel);

      try {
         channel.close().await(1L, TimeUnit.SECONDS);
         this.getLogger().at(Level.INFO).log("Closed listener %s and took %s", channel, FormatUtil.nanosToString(System.nanoTime() - start));
         return true;
      } catch (InterruptedException var5) {
         ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(var5)).log("Failed to await for listener to close!");
         Thread.currentThread().interrupt();
         return false;
      }
   }
}
