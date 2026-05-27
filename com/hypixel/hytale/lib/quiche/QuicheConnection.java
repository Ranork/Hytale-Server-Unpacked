package com.hypixel.hytale.lib.quiche;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.io.ChannelConnection;
import com.hypixel.hytale.protocol.io.ConnectionHandler;
import com.hypixel.hytale.protocol.io.ZstdNative;
import com.hypixel.hytale.protocol.packets.connection.DisconnectType;
import com.hypixel.hytale.protocol.packets.connection.QuicApplicationErrorCode;
import com.hypixel.hytale.protocol.packets.connection.ServerDisconnect;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiFunction;
import java.util.logging.Level;
import javax.annotation.Nullable;

class QuicheConnection {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final int QUEUE_SIZE = 1024;
   private static final int DATA_BUFFER_SIZE = 65535;
   public static final int ASSUMED_CLIENT_MAX_PACKET_SIZE_BYTES = 102400;
   public final QuicheListener listener;
   public final byte[] dcid;
   public final MemorySegment connHandle;
   private final ArrayBlockingQueue<QuicheListener.DatagramBuffer> incomingBuffers = new ArrayBlockingQueue<>(1024);
   private final ArrayBlockingQueue<SocketAddress> incomingSources = new ArrayBlockingQueue<>(1024);
   private final ArrayBlockingQueue<Runnable> eventTasks = new ArrayBlockingQueue<>(1024);
   private final AtomicBoolean active = new AtomicBoolean(true);
   final MemorySegment decompressionContext = ZstdNative.createDCtx();
   final MemorySegment compressionContext = ZstdNative.createCCtx();
   final Thread processingThread;
   @Nullable
   public X509Certificate certificate;
   public SocketAddress address;
   public String serverName;
   private final Long2ObjectOpenHashMap<QuicheChannel> streams = new Long2ObjectOpenHashMap();
   private final LongArrayList writableStreams = new LongArrayList();

   public QuicheConnection(QuicheListener listener, byte[] dcid, MemorySegment connHandle) {
      this.listener = listener;
      this.dcid = dcid;
      this.connHandle = connHandle;
      this.processingThread = Thread.ofVirtual().name("quiche-conn-" + QuicheChannel.formatDcid(dcid)).start(this::process);
   }

   public void process() {
      Long2ObjectFunction<QuicheChannel> newStream = id -> new QuicheChannel(this, id, id == 0L ? NetworkChannel.Default : NetworkChannel.Voice);
      Thread thread = Thread.currentThread();

      try (Arena arena = Arena.ofConfined()) {
         MemorySegment recvInfo = arena.allocate(QuicheNative.RECV_INFO_LAYOUT);
         MemorySegment sendInfo = arena.allocate(QuicheNative.SEND_INFO_LAYOUT);
         MemorySegment peerAddr = arena.allocate(QuicheNative.SOCKADDR_STORAGE_LAYOUT);
         QuicheNative.setRecvInfoTo(recvInfo, this.listener.localAddr);
         QuicheNative.setRecvInfoToLen(recvInfo, this.listener.localAddrLen);
         MemorySegment sendBuffer = arena.allocate(64000L);
         ByteBuffer sendBufferBuf = sendBuffer.asByteBuffer();
         InetSocketAddress target = null;
         MemorySegment lastSocketAddress = arena.allocate(QuicheNative.SOCKADDR_STORAGE_LAYOUT);
         int lastSocketAddressLen = 0;
         MemorySegment dataBuffer = arena.allocate(65535L);
         MemorySegment fin = arena.allocate(QuicheNative.C_BOOL);
         MemorySegment errorCode = arena.allocate(QuicheNative.C_LONG);
         boolean established = false;

         while (!thread.isInterrupted()) {
            boolean handledAnything = false;
            if (QuicheNative.connIsClosed(this.connHandle)) {
               this.logCloseReason(arena);
               break;
            }

            Runnable task;
            while ((task = this.eventTasks.poll()) != null) {
               handledAnything = true;
               task.run();
            }

            long streamId;
            while ((streamId = QuicheNative.connStreamReadableNext(this.connHandle)) != -1L) {
               QuicheChannel stream = (QuicheChannel)this.streams.computeIfAbsent(streamId, newStream);
               if (stream.isNew) {
                  ((HytaleLogger.Api)LOGGER.atInfo()).log("Stream %d created", streamId);
                  this.initChannel(stream);
                  stream.isNew = false;
               }

               while (QuicheNative.connStreamReadable(this.connHandle, streamId)) {
                  while (true) {
                     long read = QuicheNative.connStreamRecv(this.connHandle, streamId, dataBuffer, dataBuffer.byteSize(), fin, errorCode);
                     if (read < 0L) {
                        if (read != -1L) {
                           ((HytaleLogger.Api)LOGGER.atSevere())
                              .log("Error reading stream %d: %s (%d)", streamId, QuicheNative.QuicheError.fromCode((int)read), read);
                        }
                        break;
                     }

                     handledAnything = true;

                     try {
                        stream.consumePacketData(dataBuffer, (int)read);
                     } catch (Exception var94) {
                        ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var94)).log("Exception processing packet on stream %d", streamId);
                        this.gracefulClose(stream);
                        break;
                     }
                  }
               }

               if (QuicheNative.connStreamFinished(this.connHandle, streamId)) {
                  ((HytaleLogger.Api)LOGGER.atInfo()).log("Stream %d finished", streamId);
                  QuicheChannel finished = (QuicheChannel)this.streams.remove(streamId);
                  if (finished != null) {
                     finished.close();
                  }
               }
            }

            while ((streamId = QuicheNative.connStreamWritableNext(this.connHandle)) != -1L) {
               this.writableStreams.add(streamId);
            }

            for (int i = this.writableStreams.size() - 1; i >= 0; i--) {
               QuicheChannel streamx = (QuicheChannel)this.streams.get(this.writableStreams.getLong(i));
               if (streamx == null) {
                  this.writableStreams.removeLong(i);
               } else {
                  handledAnything |= streamx.tryWrite();
                  if (!streamx.isWritable()) {
                     this.writableStreams.removeLong(i);
                  }
               }
            }

            QuicheListener.DatagramBuffer datagramBuffer;
            while ((datagramBuffer = this.incomingBuffers.poll()) != null) {
               handledAnything = true;

               try {
                  SocketAddress source = this.incomingSources.take();
                  int peerAddrLen = QuicheUtil.toSocketAddrStorage(peerAddr, (InetSocketAddress)source);
                  QuicheNative.setRecvInfoFrom(recvInfo, peerAddr);
                  QuicheNative.setRecvInfoFromLen(recvInfo, peerAddrLen);
                  long bytes = QuicheNative.connRecv(this.connHandle, datagramBuffer.memory(), datagramBuffer.buffer().limit(), recvInfo);
                  if (bytes < 0L) {
                     ((HytaleLogger.Api)LOGGER.atWarning()).log("Failed to recv: %s (%d)", QuicheNative.QuicheError.fromCode((int)bytes), bytes);
                  }
               } finally {
                  this.listener.returnBuffer(datagramBuffer);
               }
            }

            while (true) {
               long written = QuicheNative.connSend(this.connHandle, sendBuffer, sendBuffer.byteSize(), sendInfo);
               if (written < 0L) {
                  if (written != -1L) {
                     ((HytaleLogger.Api)LOGGER.atWarning()).log("Failed to send: %s (%d)", QuicheNative.QuicheError.fromCode((int)written), written);
                  }
                  break;
               }

               if (written == 0L) {
                  break;
               }

               handledAnything = true;
               sendBufferBuf.position(0);
               sendBufferBuf.limit((int)written);
               MemorySegment sendInfoTo = QuicheNative.getSendInfoTo(sendInfo);
               int sendInfoToLen = QuicheNative.getSendInfoToLen(sendInfo);
               if (target == null
                  || lastSocketAddressLen != sendInfoToLen
                  || MemorySegment.mismatch(lastSocketAddress, 0L, lastSocketAddressLen, sendInfoTo, 0L, sendInfoToLen) != -1L) {
                  lastSocketAddress.copyFrom(sendInfoTo);
                  lastSocketAddressLen = sendInfoToLen;
                  target = QuicheUtil.fromSocketAddr(sendInfoTo, sendInfoToLen);
               }

               int sent = this.listener.channel.send(sendBufferBuf, target);
               if (sent != written) {
                  throw new IllegalStateException("Failed to send all bytes");
               }
            }

            if (QuicheNative.connIsEstablished(this.connHandle) && !established) {
               established = true;

               try (Arena tempArena = Arena.ofConfined()) {
                  label1057: {
                     MemorySegment certMemLocation = tempArena.allocate(QuicheNative.C_POINTER);
                     MemorySegment certMemSize = tempArena.allocate(QuicheNative.C_SIZE_T);
                     QuicheNative.connPeerCert(this.connHandle, certMemLocation, certMemSize);
                     MemorySegment cert = certMemLocation.get(QuicheNative.C_POINTER, 0L);
                     long size = QuicheNative.getSizeT(certMemSize, 0L);
                     if (size != 0L && !cert.equals(MemorySegment.NULL)) {
                        CertificateFactory factory = CertificateFactory.getInstance("X.509");
                        this.certificate = (X509Certificate)factory.generateCertificate(
                           new ByteArrayInputStream(cert.reinterpret(size).toArray(ValueLayout.JAVA_BYTE))
                        );
                        if (this.certificate != null) {
                           ((HytaleLogger.Api)LOGGER.atInfo()).log("Peer certificate retrieved: %s", this.certificate);
                           MemorySegment sniOut = tempArena.allocate(QuicheNative.C_POINTER);
                           MemorySegment sniLen = tempArena.allocate(QuicheNative.C_SIZE_T);
                           QuicheNative.connServerName(this.connHandle, sniOut, sniLen);
                           long sniSize = QuicheNative.getSizeT(sniLen, 0L);
                           if (sniSize > 0L) {
                              MemorySegment sniPtr = sniOut.get(QuicheNative.C_POINTER, 0L).reinterpret(sniSize);
                              this.serverName = new String(sniPtr.toArray(ValueLayout.JAVA_BYTE), StandardCharsets.UTF_8);
                              ((HytaleLogger.Api)LOGGER.atInfo()).log("SNI hostname: %s", this.serverName);
                           }

                           MemorySegment addr = arena.allocate(QuicheNative.SOCKADDR_STORAGE_LAYOUT);
                           MemorySegment addrLenMem = arena.allocate(QuicheNative.C_SIZE_T);
                           MemorySegment iter = QuicheNative.connPathsIter(this.connHandle, this.listener.localAddr, this.listener.localAddrLen);

                           try {
                              if (QuicheNative.socketAddrIterNext(iter, addr, addrLenMem)) {
                                 this.address = QuicheUtil.fromSocketAddr(addr, (int)QuicheNative.getSizeT(addrLenMem, 0L));
                              }
                              break label1057;
                           } finally {
                              QuicheNative.socketAddrIterFree(iter);
                           }
                        }

                        ((HytaleLogger.Api)LOGGER.atSevere()).log("Missing client cert for %s", QuicheChannel.formatDcid(this.dcid));
                        QuicheNative.connClose(this.connHandle, true, QuicApplicationErrorCode.AuthFailed.getValue(), MemorySegment.NULL, 0L);
                        continue;
                     }

                     ((HytaleLogger.Api)LOGGER.atSevere()).log("Failed to retrieve peer certificate for %s", QuicheChannel.formatDcid(this.dcid));
                     QuicheNative.connClose(this.connHandle, true, QuicApplicationErrorCode.AuthFailed.getValue(), MemorySegment.NULL, 0L);
                     continue;
                  }
               } catch (CertificateException var97) {
                  ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var97)).log("Certificate error for %s", QuicheChannel.formatDcid(this.dcid));
                  if (!QuicheNative.connIsClosed(this.connHandle)) {
                     QuicheNative.connClose(this.connHandle, true, QuicApplicationErrorCode.AuthFailed.getValue(), MemorySegment.NULL, 0L);
                  }
                  continue;
               }

               if (this.address == null) {
                  ((HytaleLogger.Api)LOGGER.atSevere()).log("Failed to find connection path for %s", QuicheChannel.formatDcid(this.dcid));
                  QuicheNative.connClose(this.connHandle, true, QuicApplicationErrorCode.NoError.getValue(), MemorySegment.NULL, 0L);
                  continue;
               }

               ((HytaleLogger.Api)LOGGER.atInfo()).log("Connection established");
            }

            this.active.set(!QuicheNative.connIsClosed(this.connHandle));
            long timeout = QuicheNative.connTimeoutAsNanos(this.connHandle);
            if (timeout > 0L && !handledAnything) {
               LockSupport.parkNanos(timeout);
               QuicheNative.connOnTimeout(this.connHandle);
            } else if (timeout == 0L) {
               QuicheNative.connOnTimeout(this.connHandle);
            } else if (!handledAnything) {
               LockSupport.park();
            }
         }

         if (!QuicheNative.connIsClosed(this.connHandle)) {
            QuicheChannel gameStream = (QuicheChannel)this.streams.get(0L);
            if (gameStream != null) {
               this.gracefulClose(gameStream);
            } else {
               QuicheNative.connClose(this.connHandle, true, QuicApplicationErrorCode.NoError.getValue(), MemorySegment.NULL, 0L);
            }

            this.drainSendQueue();
         }
      } catch (ClosedChannelException | InterruptedException var99) {
         if (var99 instanceof InterruptedException) {
            thread.interrupt();
         }

         ((HytaleLogger.Api)LOGGER.atInfo()).log("Connection %s shutting down", QuicheChannel.formatDcid(this.dcid));
      } catch (Exception var100) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var100))
            .log("Unhandled exception in connection %s processing", QuicheChannel.formatDcid(this.dcid));

         try {
            QuicheChannel gameStream = (QuicheChannel)this.streams.get(0L);
            if (gameStream != null) {
               this.gracefulClose(gameStream);
            } else if (!QuicheNative.connIsClosed(this.connHandle)) {
               QuicheNative.connClose(this.connHandle, false, 1L, MemorySegment.NULL, 0L);
            }

            this.drainSendQueue();
         } catch (Exception var90) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var90))
               .log("Error during connection close cleanup for %s", QuicheChannel.formatDcid(this.dcid));
         }
      } finally {
         ObjectIterator remaining = this.streams.values().iterator();

         while (remaining.hasNext()) {
            QuicheChannel streamx = (QuicheChannel)remaining.next();

            try {
               if (streamx.handler != null) {
                  streamx.handler.logCloseMessage();
               }
            } catch (Exception var89) {
               ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var89)).log("Error logging close message for stream %d", streamx.getStreamId());
            }

            try {
               streamx.close();
            } catch (Exception var88) {
               ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var88)).log("Error closing stream %d", streamx.getStreamId());
            }
         }

         this.active.set(false);

         while ((remaining = this.incomingBuffers.poll()) != null) {
            this.listener.returnBuffer(remaining);
         }

         QuicheNative.connFree(this.connHandle);
         ZstdNative.freeDCtx(this.decompressionContext);
         ZstdNative.freeCCtx(this.compressionContext);
      }
   }

   private void initChannel(QuicheChannel stream) {
      long streamId = stream.getStreamId();
      if (streamId == 0L) {
         ((HytaleLogger.Api)LOGGER.atInfo()).log("Primary stream %d created", streamId);
         int alpnVersion = this.getNegotiatedAlpnVersion();
         if (alpnVersion < 2) {
            ((HytaleLogger.Api)LOGGER.atInfo())
               .log("Rejecting connection %s: ALPN version %d < required %d", QuicheChannel.formatDcid(this.dcid), alpnVersion, 2);
            FormattedMessage msg = new FormattedMessage();
            msg.messageId = "server.general.disconnect.clientOutdated";
            stream.handler = this.listener.handler.apply(stream);
            stream.handler.registered(null);
            stream.writeForClose(new ServerDisconnect(msg, DisconnectType.Disconnect));
            QuicheNative.connClose(this.connHandle, true, QuicApplicationErrorCode.ClientOutdated.getValue(), MemorySegment.NULL, 0L);
         } else {
            QuicheNative.connStreamPriority(this.connHandle, streamId, (byte)0, false);
            stream.handler = this.listener.handler.apply(stream);
            stream.handler.registered(null);
         }
      } else if ((streamId & 3L) == 0L) {
         BiFunction<ConnectionHandler, ChannelConnection, ConnectionHandler> auxHandler = this.listener.auxStreamHandler;
         if (auxHandler == null) {
            ((HytaleLogger.Api)LOGGER.atWarning()).log("No aux stream handler configured, closing stream %d", streamId);
            QuicheNative.connStreamShutdown(this.connHandle, streamId, 0, 0L);
            QuicheNative.connStreamShutdown(this.connHandle, streamId, 1, 0L);
         } else {
            QuicheChannel primaryStream = (QuicheChannel)this.streams.get(0L);
            if (primaryStream != null && primaryStream.handler != null) {
               ConnectionHandler handler = auxHandler.apply(primaryStream.handler, stream);
               if (handler == null) {
                  ((HytaleLogger.Api)LOGGER.atWarning()).log("Aux stream %d rejected by handler", streamId);
                  QuicheNative.connStreamShutdown(this.connHandle, streamId, 0, 0L);
                  QuicheNative.connStreamShutdown(this.connHandle, streamId, 1, 0L);
               } else {
                  stream.handler = handler;
                  handler.registered(null);
               }
            } else {
               ((HytaleLogger.Api)LOGGER.atWarning()).log("Primary stream not ready, closing aux stream %d", streamId);
               QuicheNative.connStreamShutdown(this.connHandle, streamId, 0, 0L);
               QuicheNative.connStreamShutdown(this.connHandle, streamId, 1, 0L);
            }
         }
      } else {
         ((HytaleLogger.Api)LOGGER.atWarning()).log("Unexpected stream type for stream %d (low bits: %d), ignoring", streamId, streamId & 3L);
      }
   }

   public boolean isActive() {
      return this.active.get();
   }

   private int getNegotiatedAlpnVersion() {
      byte e;
      try (Arena tempArena = Arena.ofConfined()) {
         MemorySegment alpnOut = tempArena.allocate(QuicheNative.C_POINTER);
         MemorySegment alpnLen = tempArena.allocate(QuicheNative.C_SIZE_T);
         QuicheNative.connApplicationProto(this.connHandle, alpnOut, alpnLen);
         long size = QuicheNative.getSizeT(alpnLen, 0L);
         if (size == 0L) {
            return 0;
         }

         MemorySegment alpnPtr = alpnOut.get(QuicheNative.C_POINTER, 0L).reinterpret(size);
         String alpn = new String(alpnPtr.toArray(ValueLayout.JAVA_BYTE), StandardCharsets.UTF_8);
         if (alpn.startsWith("hytale/")) {
            try {
               return Integer.parseInt(alpn.substring(7));
            } catch (NumberFormatException var11) {
               return 0;
            }
         }

         e = 0;
      }

      return e;
   }

   public void execute(Runnable task) {
      if (!this.eventTasks.offer(task)) {
         ((HytaleLogger.Api)LOGGER.atWarning()).log("Event task queue full for %s, dropping task", QuicheChannel.formatDcid(this.dcid));
      } else {
         LockSupport.unpark(this.processingThread);
      }
   }

   public void wake() {
      LockSupport.unpark(this.processingThread);
   }

   private void logCloseReason(Arena arena) {
      String dcidHex = QuicheChannel.formatDcid(this.dcid);
      if (QuicheNative.connIsTimedOut(this.connHandle)) {
         ((HytaleLogger.Api)LOGGER.atInfo()).log("Connection %s closed: timed out", dcidHex);
      } else {
         MemorySegment isApp = arena.allocate(QuicheNative.C_BOOL);
         MemorySegment errCode = arena.allocate(QuicheNative.C_LONG);
         MemorySegment reason = arena.allocate(QuicheNative.C_POINTER);
         MemorySegment reasonLen = arena.allocate(QuicheNative.C_SIZE_T);
         if (QuicheNative.connPeerError(this.connHandle, isApp, errCode, reason, reasonLen)) {
            long code = errCode.get(ValueLayout.JAVA_LONG, 0L);
            boolean app = isApp.get(ValueLayout.JAVA_BOOLEAN, 0L);
            long len = QuicheNative.getSizeT(reasonLen, 0L);
            String reasonStr = "";
            if (len > 0L) {
               MemorySegment reasonPtr = reason.get(QuicheNative.C_POINTER, 0L);
               if (reasonPtr.address() != 0L) {
                  MemorySegment reasonMem = reasonPtr.reinterpret(len);
                  reasonStr = " reason=\"" + new String(reasonMem.toArray(ValueLayout.JAVA_BYTE), StandardCharsets.UTF_8) + "\"";
               }
            }

            ((HytaleLogger.Api)LOGGER.atInfo())
               .log("Connection %s closed by peer: %s error code %d%s", dcidHex, app ? "application" : "transport", code, reasonStr);
         } else if (QuicheNative.connLocalError(this.connHandle, isApp, errCode, reason, reasonLen)) {
            long code = errCode.get(ValueLayout.JAVA_LONG, 0L);
            boolean app = isApp.get(ValueLayout.JAVA_BOOLEAN, 0L);
            ((HytaleLogger.Api)LOGGER.atInfo()).log("Connection %s closed locally: %s error code %d", dcidHex, app ? "application" : "transport", code);
         } else {
            ((HytaleLogger.Api)LOGGER.atInfo()).log("Connection %s closed (reason unknown)", dcidHex);
         }
      }
   }

   private void gracefulClose(QuicheChannel gameStream) {
      if (!QuicheNative.connIsClosed(this.connHandle)) {
         FormattedMessage msg = new FormattedMessage();
         msg.messageId = "server.general.disconnect.internalServerError";
         gameStream.writeForClose(new ServerDisconnect(msg, DisconnectType.Crash));
         QuicheNative.connClose(this.connHandle, true, QuicApplicationErrorCode.NoError.getValue(), MemorySegment.NULL, 0L);
      }
   }

   private void drainSendQueue() {
      try (Arena drainArena = Arena.ofConfined()) {
         MemorySegment buf = drainArena.allocate(64000L);
         ByteBuffer bufBuf = buf.asByteBuffer();
         MemorySegment info = drainArena.allocate(QuicheNative.SEND_INFO_LAYOUT);

         for (int i = 0; i < 100; i++) {
            long written = QuicheNative.connSend(this.connHandle, buf, buf.byteSize(), info);
            if (written <= 0L) {
               break;
            }

            bufBuf.position(0);
            bufBuf.limit((int)written);
            InetSocketAddress target = QuicheUtil.fromSocketAddr(QuicheNative.getSendInfoTo(info), QuicheNative.getSendInfoToLen(info));
            this.listener.channel.send(bufBuf, target);
         }
      } catch (IOException var11) {
         ((HytaleLogger.Api)LOGGER.atWarning()).log("Failed to drain send queue for %s: %s", QuicheChannel.formatDcid(this.dcid), var11.getMessage());
      }
   }

   public void submit(SocketAddress source, QuicheListener.DatagramBuffer buf) {
      if (!this.incomingBuffers.offer(buf)) {
         this.listener.returnBuffer(buf);
         ((HytaleLogger.Api)LOGGER.atInfo()).log("%s: isn't processing packets fast enough, discarding", QuicheChannel.formatDcid(this.dcid));
      } else {
         this.incomingSources.add(source);
         LockSupport.unpark(this.processingThread);
      }
   }

   QuicheChannel createChannel(NetworkChannel networkChannel) {
      if (networkChannel != NetworkChannel.Default && networkChannel != NetworkChannel.Voice) {
         int streamId = networkChannel.ordinal() - 1 << 2 | 3;
         QuicheChannel channel = (QuicheChannel)this.streams.computeIfAbsent(streamId, id -> new QuicheChannel(this, id, networkChannel));
         this.writableStreams.add(streamId);
         return channel;
      } else {
         throw new IllegalArgumentException("Cannot create server-initiated uni stream for " + networkChannel);
      }
   }
}
