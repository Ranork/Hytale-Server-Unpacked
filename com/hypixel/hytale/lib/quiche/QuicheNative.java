package com.hypixel.hytale.lib.quiche;

import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.ValueLayout.OfBoolean;
import java.lang.foreign.ValueLayout.OfByte;
import java.lang.foreign.ValueLayout.OfInt;
import java.lang.foreign.ValueLayout.OfLong;
import java.lang.foreign.ValueLayout.OfShort;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class QuicheNative {
   public static final int QUICHE_MAX_CONN_ID_LEN = 20;
   public static final int QUICHE_SHUTDOWN_READ = 0;
   public static final int QUICHE_SHUTDOWN_WRITE = 1;
   private static final Linker LINKER = Linker.nativeLinker();
   private static final Arena GLOBAL_ARENA = Arena.global();
   private static final SymbolLookup LOOKUP;
   public static final QuicheNative.Platform PLATFORM;
   public static final AddressLayout C_POINTER;
   public static final OfInt C_INT;
   public static final OfLong C_LONG;
   public static final OfByte C_BYTE;
   public static final OfBoolean C_BOOL;
   public static final OfShort C_SHORT;
   public static final ValueLayout C_SIZE_T;
   public static final StructLayout SOCKADDR_STORAGE_LAYOUT;
   public static final StructLayout SOCKADDR_LAYOUT;
   public static final StructLayout TIMESPEC_LAYOUT;
   public static final StructLayout RECV_INFO_LAYOUT;
   public static final StructLayout SEND_INFO_LAYOUT;
   public static final VarHandle SOCKADDR_FAMILY;
   public static final VarHandle SOCKADDR_STORAGE_FAMILY;
   public static final VarHandle SOCKADDR_STORAGE_LEN;
   public static final long SOCKADDR_STORAGE_DATA_OFFSET;
   private static final VarHandle TIMESPEC_SEC;
   private static final VarHandle TIMESPEC_NSEC;
   private static final VarHandle RECV_INFO_FROM;
   private static final VarHandle RECV_INFO_FROM_LEN;
   private static final VarHandle RECV_INFO_TO;
   private static final VarHandle RECV_INFO_TO_LEN;
   private static final long SEND_INFO_FROM_OFFSET;
   private static final VarHandle SEND_INFO_FROM_LEN;
   private static final long SEND_INFO_TO_OFFSET;
   private static final VarHandle SEND_INFO_TO_LEN;
   private static final long SEND_INFO_AT_OFFSET;
   public static final MethodHandle QUICHE_VERSION;
   public static final MethodHandle QUICHE_ENABLE_DEBUG_LOGGING;
   public static final MethodHandle QUICHE_PUT_VARINT;
   public static final MethodHandle QUICHE_GET_VARINT;
   public static final MethodHandle QUICHE_CONFIG_NEW;
   public static final MethodHandle QUICHE_CONFIG_LOAD_CERT;
   public static final MethodHandle QUICHE_CONFIG_LOAD_CERT_CHAIN_FROM_PEM_FILE;
   public static final MethodHandle QUICHE_CONFIG_LOAD_PRIV_KEY;
   public static final MethodHandle QUICHE_CONFIG_LOAD_PRIV_KEY_FROM_PEM_FILE;
   public static final MethodHandle QUICHE_CONFIG_LOAD_VERIFY_LOCATIONS_FROM_FILE;
   public static final MethodHandle QUICHE_CONFIG_LOAD_VERIFY_LOCATIONS_FROM_DIRECTORY;
   public static final MethodHandle QUICHE_CONFIG_VERIFY_PEER;
   public static final MethodHandle QUICHE_CONFIG_VERIFY_PEER_OPTIONAL;
   public static final MethodHandle QUICHE_CONFIG_GREASE;
   public static final MethodHandle QUICHE_CONFIG_DISCOVER_PMTU;
   public static final MethodHandle QUICHE_CONFIG_LOG_KEYS;
   public static final MethodHandle QUICHE_CONFIG_ENABLE_EARLY_DATA;
   public static final MethodHandle QUICHE_CONFIG_SET_APPLICATION_PROTOS;
   public static final MethodHandle QUICHE_CONFIG_SET_MAX_AMPLIFICATION_FACTOR;
   public static final MethodHandle QUICHE_CONFIG_SET_MAX_IDLE_TIMEOUT;
   public static final MethodHandle QUICHE_CONFIG_SET_MAX_RECV_UDP_PAYLOAD_SIZE;
   public static final MethodHandle QUICHE_CONFIG_SET_MAX_SEND_UDP_PAYLOAD_SIZE;
   public static final MethodHandle QUICHE_CONFIG_SET_INITIAL_MAX_DATA;
   public static final MethodHandle QUICHE_CONFIG_SET_INITIAL_MAX_STREAM_DATA_BIDI_LOCAL;
   public static final MethodHandle QUICHE_CONFIG_SET_INITIAL_MAX_STREAM_DATA_BIDI_REMOTE;
   public static final MethodHandle QUICHE_CONFIG_SET_INITIAL_MAX_STREAM_DATA_UNI;
   public static final MethodHandle QUICHE_CONFIG_SET_INITIAL_MAX_STREAMS_BIDI;
   public static final MethodHandle QUICHE_CONFIG_SET_INITIAL_MAX_STREAMS_UNI;
   public static final MethodHandle QUICHE_CONFIG_SET_ACK_DELAY_EXPONENT;
   public static final MethodHandle QUICHE_CONFIG_SET_MAX_ACK_DELAY;
   public static final MethodHandle QUICHE_CONFIG_SET_DISABLE_ACTIVE_MIGRATION;
   public static final MethodHandle QUICHE_CONFIG_SET_CC_ALGORITHM_NAME;
   public static final MethodHandle QUICHE_CONFIG_SET_INITIAL_CONGESTION_WINDOW_PACKETS;
   public static final MethodHandle QUICHE_CONFIG_SET_CC_ALGORITHM;
   public static final MethodHandle QUICHE_CONFIG_ENABLE_HYSTART;
   public static final MethodHandle QUICHE_CONFIG_ENABLE_PACING;
   public static final MethodHandle QUICHE_CONFIG_SET_MAX_PACING_RATE;
   public static final MethodHandle QUICHE_CONFIG_ENABLE_DGRAM;
   public static final MethodHandle QUICHE_CONFIG_SET_MAX_CONNECTION_WINDOW;
   public static final MethodHandle QUICHE_CONFIG_SET_MAX_STREAM_WINDOW;
   public static final MethodHandle QUICHE_CONFIG_SET_ACTIVE_CONNECTION_ID_LIMIT;
   public static final MethodHandle QUICHE_CONFIG_SET_STATELESS_RESET_TOKEN;
   public static final MethodHandle QUICHE_CONFIG_SET_DISABLE_DCID_REUSE;
   public static final MethodHandle QUICHE_CONFIG_SET_TICKET_KEY;
   public static final MethodHandle QUICHE_CONFIG_FREE;
   public static final MethodHandle QUICHE_HEADER_INFO;
   public static final MethodHandle QUICHE_ACCEPT;
   public static final MethodHandle QUICHE_CONNECT;
   public static final MethodHandle QUICHE_NEGOTIATE_VERSION;
   public static final MethodHandle QUICHE_RETRY;
   public static final MethodHandle QUICHE_VERSION_IS_SUPPORTED;
   public static final MethodHandle QUICHE_CONN_NEW_WITH_TLS;
   public static final MethodHandle QUICHE_CONN_SET_KEYLOG_PATH;
   public static final MethodHandle QUICHE_CONN_SET_KEYLOG_FD;
   public static final MethodHandle QUICHE_CONN_SET_QLOG_PATH;
   public static final MethodHandle QUICHE_CONN_SET_QLOG_FD;
   public static final MethodHandle QUICHE_CONN_SET_SESSION;
   public static final MethodHandle QUICHE_CONN_SET_MAX_IDLE_TIMEOUT;
   public static final MethodHandle QUICHE_CONN_RECV;
   public static final MethodHandle QUICHE_CONN_SEND;
   public static final MethodHandle QUICHE_CONN_SEND_QUANTUM;
   public static final MethodHandle QUICHE_CONN_SEND_ON_PATH;
   public static final MethodHandle QUICHE_CONN_SEND_QUANTUM_ON_PATH;
   public static final MethodHandle QUICHE_CONN_TIMEOUT_AS_NANOS;
   public static final MethodHandle QUICHE_CONN_TIMEOUT_AS_MILLIS;
   public static final MethodHandle QUICHE_CONN_ON_TIMEOUT;
   public static final MethodHandle QUICHE_CONN_CLOSE;
   public static final MethodHandle QUICHE_CONN_FREE;
   public static final MethodHandle QUICHE_CONN_STREAM_RECV;
   public static final MethodHandle QUICHE_CONN_STREAM_SEND;
   public static final MethodHandle QUICHE_CONN_STREAM_PRIORITY;
   public static final MethodHandle QUICHE_CONN_STREAM_SHUTDOWN;
   public static final MethodHandle QUICHE_CONN_STREAM_CAPACITY;
   public static final MethodHandle QUICHE_CONN_STREAM_READABLE;
   public static final MethodHandle QUICHE_CONN_STREAM_READABLE_NEXT;
   public static final MethodHandle QUICHE_CONN_STREAM_WRITABLE;
   public static final MethodHandle QUICHE_CONN_STREAM_WRITABLE_NEXT;
   public static final MethodHandle QUICHE_CONN_STREAM_FINISHED;
   public static final MethodHandle QUICHE_CONN_READABLE;
   public static final MethodHandle QUICHE_CONN_WRITABLE;
   public static final MethodHandle QUICHE_STREAM_ITER_NEXT;
   public static final MethodHandle QUICHE_STREAM_ITER_FREE;
   public static final MethodHandle QUICHE_CONN_MAX_SEND_UDP_PAYLOAD_SIZE;
   public static final MethodHandle QUICHE_CONN_IS_ESTABLISHED;
   public static final MethodHandle QUICHE_CONN_IS_RESUMED;
   public static final MethodHandle QUICHE_CONN_IS_IN_EARLY_DATA;
   public static final MethodHandle QUICHE_CONN_IS_READABLE;
   public static final MethodHandle QUICHE_CONN_IS_DRAINING;
   public static final MethodHandle QUICHE_CONN_IS_CLOSED;
   public static final MethodHandle QUICHE_CONN_IS_TIMED_OUT;
   public static final MethodHandle QUICHE_CONN_IS_SERVER;
   public static final MethodHandle QUICHE_CONN_PEER_STREAMS_LEFT_BIDI;
   public static final MethodHandle QUICHE_CONN_PEER_STREAMS_LEFT_UNI;
   public static final MethodHandle QUICHE_CONN_TRACE_ID;
   public static final MethodHandle QUICHE_CONN_SOURCE_ID;
   public static final MethodHandle QUICHE_CONN_DESTINATION_ID;
   public static final MethodHandle QUICHE_CONN_APPLICATION_PROTO;
   public static final MethodHandle QUICHE_CONN_PEER_CERT;
   public static final MethodHandle QUICHE_CONN_SESSION;
   public static final MethodHandle QUICHE_CONN_SERVER_NAME;
   public static final MethodHandle QUICHE_CONN_PEER_ERROR;
   public static final MethodHandle QUICHE_CONN_LOCAL_ERROR;
   public static final MethodHandle QUICHE_CONN_STATS;
   public static final MethodHandle QUICHE_CONN_PEER_TRANSPORT_PARAMS;
   public static final MethodHandle QUICHE_CONN_PATH_STATS;
   public static final MethodHandle QUICHE_CONN_SOURCE_IDS;
   public static final MethodHandle QUICHE_CONNECTION_ID_ITER_NEXT;
   public static final MethodHandle QUICHE_CONNECTION_ID_ITER_FREE;
   public static final MethodHandle QUICHE_CONN_RETIRED_SCID_NEXT;
   public static final MethodHandle QUICHE_CONN_RETIRED_SCIDS;
   public static final MethodHandle QUICHE_CONN_AVAILABLE_DCIDS;
   public static final MethodHandle QUICHE_CONN_SCIDS_LEFT;
   public static final MethodHandle QUICHE_CONN_ACTIVE_SCIDS;
   public static final MethodHandle QUICHE_CONN_NEW_SCID;
   public static final MethodHandle QUICHE_CONN_RETIRE_DCID;
   public static final MethodHandle QUICHE_CONN_PROBE_PATH;
   public static final MethodHandle QUICHE_CONN_MIGRATE_SOURCE;
   public static final MethodHandle QUICHE_CONN_MIGRATE;
   public static final MethodHandle QUICHE_CONN_PATH_EVENT_NEXT;
   public static final MethodHandle QUICHE_PATH_EVENT_TYPE;
   public static final MethodHandle QUICHE_PATH_EVENT_NEW;
   public static final MethodHandle QUICHE_PATH_EVENT_VALIDATED;
   public static final MethodHandle QUICHE_PATH_EVENT_FAILED_VALIDATION;
   public static final MethodHandle QUICHE_PATH_EVENT_CLOSED;
   public static final MethodHandle QUICHE_PATH_EVENT_REUSED_SOURCE_CONNECTION_ID;
   public static final MethodHandle QUICHE_PATH_EVENT_PEER_MIGRATED;
   public static final MethodHandle QUICHE_PATH_EVENT_FREE;
   public static final MethodHandle QUICHE_CONN_PATHS_ITER;
   public static final MethodHandle QUICHE_SOCKET_ADDR_ITER_NEXT;
   public static final MethodHandle QUICHE_SOCKET_ADDR_ITER_FREE;
   public static final MethodHandle QUICHE_CONN_IS_PATH_VALIDATED;
   public static final MethodHandle QUICHE_CONN_DGRAM_MAX_WRITABLE_LEN;
   public static final MethodHandle QUICHE_CONN_DGRAM_RECV_FRONT_LEN;
   public static final MethodHandle QUICHE_CONN_DGRAM_RECV_QUEUE_LEN;
   public static final MethodHandle QUICHE_CONN_DGRAM_RECV_QUEUE_BYTE_SIZE;
   public static final MethodHandle QUICHE_CONN_DGRAM_SEND_QUEUE_LEN;
   public static final MethodHandle QUICHE_CONN_DGRAM_SEND_QUEUE_BYTE_SIZE;
   public static final MethodHandle QUICHE_CONN_DGRAM_RECV;
   public static final MethodHandle QUICHE_CONN_DGRAM_SEND;
   public static final MethodHandle QUICHE_CONN_DGRAM_PURGE_OUTGOING;
   public static final MethodHandle QUICHE_CONN_IS_DGRAM_SEND_QUEUE_FULL;
   public static final MethodHandle QUICHE_CONN_IS_DGRAM_RECV_QUEUE_FULL;
   public static final MethodHandle QUICHE_CONN_SEND_ACK_ELICITING;
   public static final MethodHandle QUICHE_CONN_SEND_ACK_ELICITING_ON_PATH;
   public static final MethodHandle QUICHE_H3_CONFIG_NEW;
   public static final MethodHandle QUICHE_H3_CONFIG_SET_MAX_FIELD_SECTION_SIZE;
   public static final MethodHandle QUICHE_H3_CONFIG_SET_QPACK_MAX_TABLE_CAPACITY;
   public static final MethodHandle QUICHE_H3_CONFIG_SET_QPACK_BLOCKED_STREAMS;
   public static final MethodHandle QUICHE_H3_CONFIG_ENABLE_EXTENDED_CONNECT;
   public static final MethodHandle QUICHE_H3_CONFIG_FREE;
   public static final MethodHandle QUICHE_H3_CONN_NEW_WITH_TRANSPORT;
   public static final MethodHandle QUICHE_H3_CONN_POLL;
   public static final MethodHandle QUICHE_H3_EVENT_TYPE;
   public static final MethodHandle QUICHE_H3_EVENT_FOR_EACH_HEADER;
   public static final MethodHandle QUICHE_H3_FOR_EACH_SETTING;
   public static final MethodHandle QUICHE_H3_EVENT_HEADERS_HAS_MORE_FRAMES;
   public static final MethodHandle QUICHE_H3_EXTENDED_CONNECT_ENABLED_BY_PEER;
   public static final MethodHandle QUICHE_H3_EVENT_FREE;
   public static final MethodHandle QUICHE_H3_SEND_REQUEST;
   public static final MethodHandle QUICHE_H3_SEND_RESPONSE;
   public static final MethodHandle QUICHE_H3_SEND_RESPONSE_WITH_PRIORITY;
   public static final MethodHandle QUICHE_H3_SEND_ADDITIONAL_HEADERS;
   public static final MethodHandle QUICHE_H3_SEND_BODY;
   public static final MethodHandle QUICHE_H3_RECV_BODY;
   public static final MethodHandle QUICHE_H3_SEND_GOAWAY;
   public static final MethodHandle QUICHE_H3_PARSE_EXTENSIBLE_PRIORITY;
   public static final MethodHandle QUICHE_H3_SEND_PRIORITY_UPDATE_FOR_REQUEST;
   public static final MethodHandle QUICHE_H3_TAKE_LAST_PRIORITY_UPDATE;
   public static final MethodHandle QUICHE_H3_DGRAM_ENABLED_BY_PEER;
   public static final MethodHandle QUICHE_H3_CONN_STATS;
   public static final MethodHandle QUICHE_H3_CONN_FREE;

   public static void setSizeT(MemorySegment segment, long offset, long value) {
      switch (C_SIZE_T) {
         case OfLong l:
            segment.set(l, offset, value);
            break;
         case OfInt i:
            segment.set(i, offset, (int)value);
            break;
         default:
            throw new UnsupportedOperationException("Unsupported size_t layout: " + C_SIZE_T);
      }
   }

   public static long getSizeT(MemorySegment segment, long offset) {
      return switch (C_SIZE_T) {
         case OfLong l -> segment.get(l, offset);
         case OfInt i -> Integer.toUnsignedLong(segment.get(i, offset));
         default -> throw new UnsupportedOperationException("Unsupported size_t layout: " + C_SIZE_T);
      };
   }

   public static int getSockaddrFamily(MemorySegment sockaddr) {
      return switch (PLATFORM) {
         case LINUX, WINDOWS -> Short.toUnsignedInt((short)SOCKADDR_FAMILY.get((MemorySegment)sockaddr, (long)0L));
         case MACOS -> Byte.toUnsignedInt((byte)SOCKADDR_FAMILY.get((MemorySegment)sockaddr, (long)0L));
      };
   }

   public static void setSockaddrFamily(MemorySegment sockaddr, int family) {
      switch (PLATFORM) {
         case LINUX:
         case WINDOWS:
            SOCKADDR_FAMILY.set((MemorySegment)sockaddr, (long)0L, (short)((short)family));
            break;
         case MACOS:
            SOCKADDR_FAMILY.set((MemorySegment)sockaddr, (long)0L, (byte)((byte)family));
      }
   }

   public static byte getSockaddrLen(MemorySegment sockaddr) {
      return SOCKADDR_STORAGE_LEN == null ? 0 : (byte)SOCKADDR_STORAGE_LEN.get((MemorySegment)sockaddr, (long)0L);
   }

   public static void setSockaddrLen(MemorySegment sockaddr, byte len) {
      if (SOCKADDR_STORAGE_LEN != null) {
         SOCKADDR_STORAGE_LEN.set((MemorySegment)sockaddr, (long)0L, (byte)len);
      }
   }

   public static MemorySegment getSockaddrData(MemorySegment sockaddr) {
      return sockaddr.asSlice(SOCKADDR_STORAGE_DATA_OFFSET, SOCKADDR_LAYOUT.byteSize() - SOCKADDR_STORAGE_DATA_OFFSET);
   }

   public static int getSockaddrStorageFamily(MemorySegment storage) {
      return switch (PLATFORM) {
         case LINUX, WINDOWS -> Short.toUnsignedInt((short)SOCKADDR_STORAGE_FAMILY.get((MemorySegment)storage, (long)0L));
         case MACOS -> Byte.toUnsignedInt((byte)SOCKADDR_STORAGE_FAMILY.get((MemorySegment)storage, (long)0L));
      };
   }

   public static void setSockaddrStorageFamily(MemorySegment storage, int family) {
      switch (PLATFORM) {
         case LINUX:
         case WINDOWS:
            SOCKADDR_STORAGE_FAMILY.set((MemorySegment)storage, (long)0L, (short)((short)family));
            break;
         case MACOS:
            SOCKADDR_STORAGE_FAMILY.set((MemorySegment)storage, (long)0L, (byte)((byte)family));
      }
   }

   public static long getTimespecSec(MemorySegment timespec) {
      return (long)TIMESPEC_SEC.get((MemorySegment)timespec, (long)0L);
   }

   public static void setTimespecSec(MemorySegment timespec, long sec) {
      TIMESPEC_SEC.set((MemorySegment)timespec, (long)0L, (long)sec);
   }

   public static long getTimespecNsec(MemorySegment timespec) {
      return switch (PLATFORM) {
         case LINUX, MACOS -> (long)TIMESPEC_NSEC.get((MemorySegment)timespec, (long)0L);
         case WINDOWS -> Integer.toUnsignedLong((int)TIMESPEC_NSEC.get((MemorySegment)timespec, (long)0L));
      };
   }

   public static void setTimespecNsec(MemorySegment timespec, long nsec) {
      switch (PLATFORM) {
         case LINUX:
         case MACOS:
            TIMESPEC_NSEC.set((MemorySegment)timespec, (long)0L, (long)nsec);
            break;
         case WINDOWS:
            TIMESPEC_NSEC.set((MemorySegment)timespec, (long)0L, (int)((int)nsec));
      }
   }

   public static MemorySegment getRecvInfoFrom(MemorySegment info) {
      return (MemorySegment)RECV_INFO_FROM.get((MemorySegment)info, (long)0L);
   }

   public static void setRecvInfoFrom(MemorySegment info, MemorySegment from) {
      RECV_INFO_FROM.set((MemorySegment)info, (long)0L, (MemorySegment)from);
   }

   public static int getRecvInfoFromLen(MemorySegment info) {
      return (int)RECV_INFO_FROM_LEN.get((MemorySegment)info, (long)0L);
   }

   public static void setRecvInfoFromLen(MemorySegment info, int len) {
      RECV_INFO_FROM_LEN.set((MemorySegment)info, (long)0L, (int)len);
   }

   public static MemorySegment getRecvInfoTo(MemorySegment info) {
      return (MemorySegment)RECV_INFO_TO.get((MemorySegment)info, (long)0L);
   }

   public static void setRecvInfoTo(MemorySegment info, MemorySegment to) {
      RECV_INFO_TO.set((MemorySegment)info, (long)0L, (MemorySegment)to);
   }

   public static int getRecvInfoToLen(MemorySegment info) {
      return (int)RECV_INFO_TO_LEN.get((MemorySegment)info, (long)0L);
   }

   public static void setRecvInfoToLen(MemorySegment info, int len) {
      RECV_INFO_TO_LEN.set((MemorySegment)info, (long)0L, (int)len);
   }

   public static MemorySegment getSendInfoFrom(MemorySegment info) {
      return info.asSlice(SEND_INFO_FROM_OFFSET, SOCKADDR_STORAGE_LAYOUT);
   }

   public static int getSendInfoFromLen(MemorySegment info) {
      return (int)SEND_INFO_FROM_LEN.get((MemorySegment)info, (long)0L);
   }

   public static void setSendInfoFromLen(MemorySegment info, int len) {
      SEND_INFO_FROM_LEN.set((MemorySegment)info, (long)0L, (int)len);
   }

   public static MemorySegment getSendInfoTo(MemorySegment info) {
      return info.asSlice(SEND_INFO_TO_OFFSET, SOCKADDR_STORAGE_LAYOUT);
   }

   public static int getSendInfoToLen(MemorySegment info) {
      return (int)SEND_INFO_TO_LEN.get((MemorySegment)info, (long)0L);
   }

   public static void setSendInfoToLen(MemorySegment info, int len) {
      SEND_INFO_TO_LEN.set((MemorySegment)info, (long)0L, (int)len);
   }

   public static MemorySegment getSendInfoAt(MemorySegment info) {
      return info.asSlice(SEND_INFO_AT_OFFSET, TIMESPEC_LAYOUT);
   }

   private static MethodHandle downcall(String name, FunctionDescriptor descriptor) {
      return LINKER.downcallHandle(LOOKUP.find(name).orElseThrow(), descriptor);
   }

   public static MemorySegment version() {
      try {
         return (MemorySegment)QUICHE_VERSION.invokeExact();
      } catch (Throwable var1) {
         throw SneakyThrow.sneakyThrow(var1);
      }
   }

   public static int enableDebugLogging(MemorySegment callback, MemorySegment argp) {
      try {
         return (int)QUICHE_ENABLE_DEBUG_LOGGING.invokeExact((MemorySegment)callback, (MemorySegment)argp);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static int putVarint(MemorySegment buf, long bufLen, long val) {
      try {
         return (int)QUICHE_PUT_VARINT.invokeExact((MemorySegment)buf, (long)bufLen, (long)val);
      } catch (Throwable var6) {
         throw SneakyThrow.sneakyThrow(var6);
      }
   }

   public static long getVarint(MemorySegment buf, long bufLen, MemorySegment out) {
      try {
         return (long)QUICHE_GET_VARINT.invokeExact((MemorySegment)buf, (long)bufLen, (MemorySegment)out);
      } catch (Throwable var5) {
         throw SneakyThrow.sneakyThrow(var5);
      }
   }

   public static MemorySegment configNew(int version) {
      try {
         return (MemorySegment)QUICHE_CONFIG_NEW.invokeExact((int)version);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static int configLoadCert(MemorySegment config, MemorySegment buf, long bufLen) {
      try {
         return (int)QUICHE_CONFIG_LOAD_CERT.invokeExact((MemorySegment)config, (MemorySegment)buf, (long)bufLen);
      } catch (Throwable var5) {
         throw SneakyThrow.sneakyThrow(var5);
      }
   }

   public static int configLoadCertChainFromPemFile(MemorySegment config, MemorySegment path) {
      try {
         return (int)QUICHE_CONFIG_LOAD_CERT_CHAIN_FROM_PEM_FILE.invokeExact((MemorySegment)config, (MemorySegment)path);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static int configLoadPrivKey(MemorySegment config, MemorySegment buf, long bufLen) {
      try {
         return (int)QUICHE_CONFIG_LOAD_PRIV_KEY.invokeExact((MemorySegment)config, (MemorySegment)buf, (long)bufLen);
      } catch (Throwable var5) {
         throw SneakyThrow.sneakyThrow(var5);
      }
   }

   public static int configLoadPrivKeyFromPemFile(MemorySegment config, MemorySegment path) {
      try {
         return (int)QUICHE_CONFIG_LOAD_PRIV_KEY_FROM_PEM_FILE.invokeExact((MemorySegment)config, (MemorySegment)path);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static int configLoadVerifyLocationsFromFile(MemorySegment config, MemorySegment path) {
      try {
         return (int)QUICHE_CONFIG_LOAD_VERIFY_LOCATIONS_FROM_FILE.invokeExact((MemorySegment)config, (MemorySegment)path);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static int configLoadVerifyLocationsFromDirectory(MemorySegment config, MemorySegment path) {
      try {
         return (int)QUICHE_CONFIG_LOAD_VERIFY_LOCATIONS_FROM_DIRECTORY.invokeExact((MemorySegment)config, (MemorySegment)path);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static void configVerifyPeer(MemorySegment config, boolean v) {
      try {
         QUICHE_CONFIG_VERIFY_PEER.invokeExact((MemorySegment)config, (boolean)v);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static void configVerifyPeerOptional(MemorySegment config) {
      try {
         QUICHE_CONFIG_VERIFY_PEER_OPTIONAL.invokeExact((MemorySegment)config);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static void configGrease(MemorySegment config, boolean v) {
      try {
         QUICHE_CONFIG_GREASE.invokeExact((MemorySegment)config, (boolean)v);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static void configDiscoverPmtu(MemorySegment config, boolean v) {
      try {
         QUICHE_CONFIG_DISCOVER_PMTU.invokeExact((MemorySegment)config, (boolean)v);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static void configLogKeys(MemorySegment config) {
      try {
         QUICHE_CONFIG_LOG_KEYS.invokeExact((MemorySegment)config);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static void configEnableEarlyData(MemorySegment config) {
      try {
         QUICHE_CONFIG_ENABLE_EARLY_DATA.invokeExact((MemorySegment)config);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static int configSetApplicationProtos(MemorySegment config, MemorySegment protos, long protosLen) {
      try {
         return (int)QUICHE_CONFIG_SET_APPLICATION_PROTOS.invokeExact((MemorySegment)config, (MemorySegment)protos, (long)protosLen);
      } catch (Throwable var5) {
         throw SneakyThrow.sneakyThrow(var5);
      }
   }

   public static void configSetMaxAmplificationFactor(MemorySegment config, long v) {
      try {
         QUICHE_CONFIG_SET_MAX_AMPLIFICATION_FACTOR.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void configSetMaxIdleTimeout(MemorySegment config, long v) {
      try {
         QUICHE_CONFIG_SET_MAX_IDLE_TIMEOUT.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void configSetMaxRecvUdpPayloadSize(MemorySegment config, long v) {
      try {
         QUICHE_CONFIG_SET_MAX_RECV_UDP_PAYLOAD_SIZE.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void configSetMaxSendUdpPayloadSize(MemorySegment config, long v) {
      try {
         QUICHE_CONFIG_SET_MAX_SEND_UDP_PAYLOAD_SIZE.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void configSetInitialMaxData(MemorySegment config, long v) {
      try {
         QUICHE_CONFIG_SET_INITIAL_MAX_DATA.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void configSetInitialMaxStreamDataBidiLocal(MemorySegment config, long v) {
      try {
         QUICHE_CONFIG_SET_INITIAL_MAX_STREAM_DATA_BIDI_LOCAL.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void configSetInitialMaxStreamDataBidiRemote(MemorySegment config, long v) {
      try {
         QUICHE_CONFIG_SET_INITIAL_MAX_STREAM_DATA_BIDI_REMOTE.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void configSetInitialMaxStreamDataUni(MemorySegment config, long v) {
      try {
         QUICHE_CONFIG_SET_INITIAL_MAX_STREAM_DATA_UNI.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void configSetInitialMaxStreamsBidi(MemorySegment config, long v) {
      try {
         QUICHE_CONFIG_SET_INITIAL_MAX_STREAMS_BIDI.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void configSetInitialMaxStreamsUni(MemorySegment config, long v) {
      try {
         QUICHE_CONFIG_SET_INITIAL_MAX_STREAMS_UNI.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void configSetAckDelayExponent(MemorySegment config, long v) {
      try {
         QUICHE_CONFIG_SET_ACK_DELAY_EXPONENT.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void configSetMaxAckDelay(MemorySegment config, long v) {
      try {
         QUICHE_CONFIG_SET_MAX_ACK_DELAY.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void configSetDisableActiveMigration(MemorySegment config, boolean v) {
      try {
         QUICHE_CONFIG_SET_DISABLE_ACTIVE_MIGRATION.invokeExact((MemorySegment)config, (boolean)v);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static int configSetCcAlgorithmName(MemorySegment config, MemorySegment name) {
      try {
         return (int)QUICHE_CONFIG_SET_CC_ALGORITHM_NAME.invokeExact((MemorySegment)config, (MemorySegment)name);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static void configSetInitialCongestionWindowPackets(MemorySegment config, long v) {
      try {
         QUICHE_CONFIG_SET_INITIAL_CONGESTION_WINDOW_PACKETS.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void configSetCcAlgorithm(MemorySegment config, int algo) {
      try {
         QUICHE_CONFIG_SET_CC_ALGORITHM.invokeExact((MemorySegment)config, (int)algo);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static void configEnableHystart(MemorySegment config, boolean v) {
      try {
         QUICHE_CONFIG_ENABLE_HYSTART.invokeExact((MemorySegment)config, (boolean)v);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static void configEnablePacing(MemorySegment config, boolean v) {
      try {
         QUICHE_CONFIG_ENABLE_PACING.invokeExact((MemorySegment)config, (boolean)v);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static void configSetMaxPacingRate(MemorySegment config, long v) {
      try {
         QUICHE_CONFIG_SET_MAX_PACING_RATE.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void configEnableDgram(MemorySegment config, boolean v, long recvQueueLen, long sendQueueLen) {
      try {
         QUICHE_CONFIG_ENABLE_DGRAM.invokeExact((MemorySegment)config, (boolean)v, (long)recvQueueLen, (long)sendQueueLen);
      } catch (Throwable var7) {
         throw SneakyThrow.sneakyThrow(var7);
      }
   }

   public static void configSetMaxConnectionWindow(MemorySegment config, long v) {
      try {
         QUICHE_CONFIG_SET_MAX_CONNECTION_WINDOW.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void configSetMaxStreamWindow(MemorySegment config, long v) {
      try {
         QUICHE_CONFIG_SET_MAX_STREAM_WINDOW.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void configSetActiveConnectionIdLimit(MemorySegment config, long v) {
      try {
         QUICHE_CONFIG_SET_ACTIVE_CONNECTION_ID_LIMIT.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void configSetStatelessResetToken(MemorySegment config, MemorySegment token) {
      try {
         QUICHE_CONFIG_SET_STATELESS_RESET_TOKEN.invokeExact((MemorySegment)config, (MemorySegment)token);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static void configSetDisableDcidReuse(MemorySegment config, boolean v) {
      try {
         QUICHE_CONFIG_SET_DISABLE_DCID_REUSE.invokeExact((MemorySegment)config, (boolean)v);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static int configSetTicketKey(MemorySegment config, MemorySegment key, long keyLen) {
      try {
         return (int)QUICHE_CONFIG_SET_TICKET_KEY.invokeExact((MemorySegment)config, (MemorySegment)key, (long)keyLen);
      } catch (Throwable var5) {
         throw SneakyThrow.sneakyThrow(var5);
      }
   }

   public static void configFree(MemorySegment config) {
      try {
         QUICHE_CONFIG_FREE.invokeExact((MemorySegment)config);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static int headerInfo(
      MemorySegment buf,
      long bufLen,
      long dcilLen,
      MemorySegment version,
      MemorySegment type,
      MemorySegment scid,
      MemorySegment scidLen,
      MemorySegment dcid,
      MemorySegment dcidLen,
      MemorySegment token,
      MemorySegment tokenLen
   ) {
      try {
         return (int)QUICHE_HEADER_INFO.invokeExact(
            (MemorySegment)buf,
            (long)bufLen,
            (long)dcilLen,
            (MemorySegment)version,
            (MemorySegment)type,
            (MemorySegment)scid,
            (MemorySegment)scidLen,
            (MemorySegment)dcid,
            (MemorySegment)dcidLen,
            (MemorySegment)token,
            (MemorySegment)tokenLen
         );
      } catch (Throwable var14) {
         throw SneakyThrow.sneakyThrow(var14);
      }
   }

   public static MemorySegment accept(
      MemorySegment scid,
      long scidLen,
      MemorySegment odcid,
      long odcidLen,
      MemorySegment local,
      int localLen,
      MemorySegment peer,
      int peerLen,
      MemorySegment config
   ) {
      try {
         return (MemorySegment)QUICHE_ACCEPT.invokeExact(
            (MemorySegment)scid,
            (long)scidLen,
            (MemorySegment)odcid,
            (long)odcidLen,
            (MemorySegment)local,
            (int)localLen,
            (MemorySegment)peer,
            (int)peerLen,
            (MemorySegment)config
         );
      } catch (Throwable var12) {
         throw SneakyThrow.sneakyThrow(var12);
      }
   }

   public static MemorySegment connect(
      MemorySegment serverName, MemorySegment scid, long scidLen, MemorySegment local, int localLen, MemorySegment peer, int peerLen, MemorySegment config
   ) {
      try {
         return (MemorySegment)QUICHE_CONNECT.invokeExact(
            (MemorySegment)serverName,
            (MemorySegment)scid,
            (long)scidLen,
            (MemorySegment)local,
            (int)localLen,
            (MemorySegment)peer,
            (int)peerLen,
            (MemorySegment)config
         );
      } catch (Throwable var10) {
         throw SneakyThrow.sneakyThrow(var10);
      }
   }

   public static long negotiateVersion(MemorySegment scid, long scidLen, MemorySegment dcid, long dcidLen, MemorySegment out, long outLen) {
      try {
         return (long)QUICHE_NEGOTIATE_VERSION.invokeExact(
            (MemorySegment)scid, (long)scidLen, (MemorySegment)dcid, (long)dcidLen, (MemorySegment)out, (long)outLen
         );
      } catch (Throwable var10) {
         throw SneakyThrow.sneakyThrow(var10);
      }
   }

   public static long retry(
      MemorySegment scid,
      long scidLen,
      MemorySegment dcid,
      long dcidLen,
      MemorySegment newScid,
      long newScidLen,
      MemorySegment token,
      long tokenLen,
      int version,
      MemorySegment out,
      long outLen
   ) {
      try {
         return (long)QUICHE_RETRY.invokeExact(
            (MemorySegment)scid,
            (long)scidLen,
            (MemorySegment)dcid,
            (long)dcidLen,
            (MemorySegment)newScid,
            (long)newScidLen,
            (MemorySegment)token,
            (long)tokenLen,
            (int)version,
            (MemorySegment)out,
            (long)outLen
         );
      } catch (Throwable var17) {
         throw SneakyThrow.sneakyThrow(var17);
      }
   }

   public static boolean versionIsSupported(int version) {
      try {
         return (boolean)QUICHE_VERSION_IS_SUPPORTED.invokeExact((int)version);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static MemorySegment connNewWithTls(
      MemorySegment scid,
      long scidLen,
      MemorySegment odcid,
      long odcidLen,
      MemorySegment local,
      int localLen,
      MemorySegment peer,
      int peerLen,
      MemorySegment config,
      MemorySegment sslCtx,
      boolean isServer
   ) {
      try {
         return (MemorySegment)QUICHE_CONN_NEW_WITH_TLS.invokeExact(
            (MemorySegment)scid,
            (long)scidLen,
            (MemorySegment)odcid,
            (long)odcidLen,
            (MemorySegment)local,
            (int)localLen,
            (MemorySegment)peer,
            (int)peerLen,
            (MemorySegment)config,
            (MemorySegment)sslCtx,
            (boolean)isServer
         );
      } catch (Throwable var14) {
         throw SneakyThrow.sneakyThrow(var14);
      }
   }

   public static boolean connSetKeylogPath(MemorySegment conn, MemorySegment path) {
      try {
         return (boolean)QUICHE_CONN_SET_KEYLOG_PATH.invokeExact((MemorySegment)conn, (MemorySegment)path);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static void connSetKeylogFd(MemorySegment conn, int fd) {
      try {
         QUICHE_CONN_SET_KEYLOG_FD.invokeExact((MemorySegment)conn, (int)fd);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static boolean connSetQlogPath(MemorySegment conn, MemorySegment path, MemorySegment title, MemorySegment desc) {
      try {
         return (boolean)QUICHE_CONN_SET_QLOG_PATH.invokeExact((MemorySegment)conn, (MemorySegment)path, (MemorySegment)title, (MemorySegment)desc);
      } catch (Throwable var5) {
         throw SneakyThrow.sneakyThrow(var5);
      }
   }

   public static void connSetQlogFd(MemorySegment conn, int fd, MemorySegment title, MemorySegment desc) {
      try {
         QUICHE_CONN_SET_QLOG_FD.invokeExact((MemorySegment)conn, (int)fd, (MemorySegment)title, (MemorySegment)desc);
      } catch (Throwable var5) {
         throw SneakyThrow.sneakyThrow(var5);
      }
   }

   public static int connSetSession(MemorySegment conn, MemorySegment buf, long bufLen) {
      try {
         return (int)QUICHE_CONN_SET_SESSION.invokeExact((MemorySegment)conn, (MemorySegment)buf, (long)bufLen);
      } catch (Throwable var5) {
         throw SneakyThrow.sneakyThrow(var5);
      }
   }

   public static int connSetMaxIdleTimeout(MemorySegment conn, long timeout) {
      try {
         return (int)QUICHE_CONN_SET_MAX_IDLE_TIMEOUT.invokeExact((MemorySegment)conn, (long)timeout);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static long connRecv(MemorySegment conn, MemorySegment buf, long bufLen, MemorySegment info) {
      try {
         return (long)QUICHE_CONN_RECV.invokeExact((MemorySegment)conn, (MemorySegment)buf, (long)bufLen, (MemorySegment)info);
      } catch (Throwable var6) {
         throw SneakyThrow.sneakyThrow(var6);
      }
   }

   public static long connSend(MemorySegment conn, MemorySegment buf, long bufLen, MemorySegment info) {
      try {
         return (long)QUICHE_CONN_SEND.invokeExact((MemorySegment)conn, (MemorySegment)buf, (long)bufLen, (MemorySegment)info);
      } catch (Throwable var6) {
         throw SneakyThrow.sneakyThrow(var6);
      }
   }

   public static long connSendQuantum(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_SEND_QUANTUM.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long connSendOnPath(
      MemorySegment conn, MemorySegment buf, long bufLen, MemorySegment local, int localLen, MemorySegment peer, int peerLen, MemorySegment info
   ) {
      try {
         return (long)QUICHE_CONN_SEND_ON_PATH.invokeExact(
            (MemorySegment)conn, (MemorySegment)buf, (long)bufLen, (MemorySegment)local, (int)localLen, (MemorySegment)peer, (int)peerLen, (MemorySegment)info
         );
      } catch (Throwable var10) {
         throw SneakyThrow.sneakyThrow(var10);
      }
   }

   public static long connSendQuantumOnPath(MemorySegment conn, MemorySegment local, int localLen, MemorySegment peer, int peerLen) {
      try {
         return (long)QUICHE_CONN_SEND_QUANTUM_ON_PATH.invokeExact((MemorySegment)conn, (MemorySegment)local, (int)localLen, (MemorySegment)peer, (int)peerLen);
      } catch (Throwable var6) {
         throw SneakyThrow.sneakyThrow(var6);
      }
   }

   public static long connTimeoutAsNanos(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_TIMEOUT_AS_NANOS.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long connTimeoutAsMillis(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_TIMEOUT_AS_MILLIS.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static void connOnTimeout(MemorySegment conn) {
      try {
         QUICHE_CONN_ON_TIMEOUT.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static int connClose(MemorySegment conn, boolean app, long err, MemorySegment reason, long reasonLen) {
      try {
         return (int)QUICHE_CONN_CLOSE.invokeExact((MemorySegment)conn, (boolean)app, (long)err, (MemorySegment)reason, (long)reasonLen);
      } catch (Throwable var8) {
         throw SneakyThrow.sneakyThrow(var8);
      }
   }

   public static void connFree(MemorySegment conn) {
      try {
         QUICHE_CONN_FREE.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long connStreamRecv(MemorySegment conn, long streamId, MemorySegment buf, long bufLen, MemorySegment fin, MemorySegment errorCode) {
      try {
         return (long)QUICHE_CONN_STREAM_RECV.invokeExact(
            (MemorySegment)conn, (long)streamId, (MemorySegment)buf, (long)bufLen, (MemorySegment)fin, (MemorySegment)errorCode
         );
      } catch (Throwable var9) {
         throw SneakyThrow.sneakyThrow(var9);
      }
   }

   public static long connStreamSend(MemorySegment conn, long streamId, MemorySegment buf, long bufLen, boolean fin, MemorySegment errorCode) {
      try {
         return (long)QUICHE_CONN_STREAM_SEND.invokeExact(
            (MemorySegment)conn, (long)streamId, (MemorySegment)buf, (long)bufLen, (boolean)fin, (MemorySegment)errorCode
         );
      } catch (Throwable var9) {
         throw SneakyThrow.sneakyThrow(var9);
      }
   }

   public static int connStreamPriority(MemorySegment conn, long streamId, byte urgency, boolean incremental) {
      try {
         return (int)QUICHE_CONN_STREAM_PRIORITY.invokeExact((MemorySegment)conn, (long)streamId, (byte)urgency, (boolean)incremental);
      } catch (Throwable var6) {
         throw SneakyThrow.sneakyThrow(var6);
      }
   }

   public static int connStreamShutdown(MemorySegment conn, long streamId, int direction, long err) {
      if (direction != 0 && direction != 1) {
         throw new IllegalArgumentException("Invalid shutdown direction: " + direction);
      } else {
         try {
            return (int)QUICHE_CONN_STREAM_SHUTDOWN.invokeExact((MemorySegment)conn, (long)streamId, (int)direction, (long)err);
         } catch (Throwable var7) {
            throw SneakyThrow.sneakyThrow(var7);
         }
      }
   }

   public static long connStreamCapacity(MemorySegment conn, long streamId) {
      try {
         return (long)QUICHE_CONN_STREAM_CAPACITY.invokeExact((MemorySegment)conn, (long)streamId);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static boolean connStreamReadable(MemorySegment conn, long streamId) {
      try {
         return (boolean)QUICHE_CONN_STREAM_READABLE.invokeExact((MemorySegment)conn, (long)streamId);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static long connStreamReadableNext(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_STREAM_READABLE_NEXT.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static int connStreamWritable(MemorySegment conn, long streamId, long len) {
      try {
         return (int)QUICHE_CONN_STREAM_WRITABLE.invokeExact((MemorySegment)conn, (long)streamId, (long)len);
      } catch (Throwable var6) {
         throw SneakyThrow.sneakyThrow(var6);
      }
   }

   public static long connStreamWritableNext(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_STREAM_WRITABLE_NEXT.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static boolean connStreamFinished(MemorySegment conn, long streamId) {
      try {
         return (boolean)QUICHE_CONN_STREAM_FINISHED.invokeExact((MemorySegment)conn, (long)streamId);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static MemorySegment connReadable(MemorySegment conn) {
      try {
         return (MemorySegment)QUICHE_CONN_READABLE.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static MemorySegment connWritable(MemorySegment conn) {
      try {
         return (MemorySegment)QUICHE_CONN_WRITABLE.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static boolean streamIterNext(MemorySegment iter, MemorySegment streamId) {
      try {
         return (boolean)QUICHE_STREAM_ITER_NEXT.invokeExact((MemorySegment)iter, (MemorySegment)streamId);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static void streamIterFree(MemorySegment iter) {
      try {
         QUICHE_STREAM_ITER_FREE.invokeExact((MemorySegment)iter);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long connMaxSendUdpPayloadSize(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_MAX_SEND_UDP_PAYLOAD_SIZE.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static boolean connIsEstablished(MemorySegment conn) {
      try {
         return (boolean)QUICHE_CONN_IS_ESTABLISHED.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static boolean connIsResumed(MemorySegment conn) {
      try {
         return (boolean)QUICHE_CONN_IS_RESUMED.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static boolean connIsInEarlyData(MemorySegment conn) {
      try {
         return (boolean)QUICHE_CONN_IS_IN_EARLY_DATA.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static boolean connIsReadable(MemorySegment conn) {
      try {
         return (boolean)QUICHE_CONN_IS_READABLE.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static boolean connIsDraining(MemorySegment conn) {
      try {
         return (boolean)QUICHE_CONN_IS_DRAINING.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static boolean connIsClosed(MemorySegment conn) {
      try {
         return (boolean)QUICHE_CONN_IS_CLOSED.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static boolean connIsTimedOut(MemorySegment conn) {
      try {
         return (boolean)QUICHE_CONN_IS_TIMED_OUT.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static boolean connIsServer(MemorySegment conn) {
      try {
         return (boolean)QUICHE_CONN_IS_SERVER.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long connPeerStreamsLeftBidi(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_PEER_STREAMS_LEFT_BIDI.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long connPeerStreamsLeftUni(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_PEER_STREAMS_LEFT_UNI.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static void connTraceId(MemorySegment conn, MemorySegment out, MemorySegment outLen) {
      try {
         QUICHE_CONN_TRACE_ID.invokeExact((MemorySegment)conn, (MemorySegment)out, (MemorySegment)outLen);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void connSourceId(MemorySegment conn, MemorySegment out, MemorySegment outLen) {
      try {
         QUICHE_CONN_SOURCE_ID.invokeExact((MemorySegment)conn, (MemorySegment)out, (MemorySegment)outLen);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void connDestinationId(MemorySegment conn, MemorySegment out, MemorySegment outLen) {
      try {
         QUICHE_CONN_DESTINATION_ID.invokeExact((MemorySegment)conn, (MemorySegment)out, (MemorySegment)outLen);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void connApplicationProto(MemorySegment conn, MemorySegment out, MemorySegment outLen) {
      try {
         QUICHE_CONN_APPLICATION_PROTO.invokeExact((MemorySegment)conn, (MemorySegment)out, (MemorySegment)outLen);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void connPeerCert(MemorySegment conn, MemorySegment out, MemorySegment outLen) {
      try {
         QUICHE_CONN_PEER_CERT.invokeExact((MemorySegment)conn, (MemorySegment)out, (MemorySegment)outLen);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void connSession(MemorySegment conn, MemorySegment out, MemorySegment outLen) {
      try {
         QUICHE_CONN_SESSION.invokeExact((MemorySegment)conn, (MemorySegment)out, (MemorySegment)outLen);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void connServerName(MemorySegment conn, MemorySegment out, MemorySegment outLen) {
      try {
         QUICHE_CONN_SERVER_NAME.invokeExact((MemorySegment)conn, (MemorySegment)out, (MemorySegment)outLen);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static boolean connPeerError(MemorySegment conn, MemorySegment isApp, MemorySegment errorCode, MemorySegment reason, MemorySegment reasonLen) {
      try {
         return (boolean)QUICHE_CONN_PEER_ERROR.invokeExact(
            (MemorySegment)conn, (MemorySegment)isApp, (MemorySegment)errorCode, (MemorySegment)reason, (MemorySegment)reasonLen
         );
      } catch (Throwable var6) {
         throw SneakyThrow.sneakyThrow(var6);
      }
   }

   public static boolean connLocalError(MemorySegment conn, MemorySegment isApp, MemorySegment errorCode, MemorySegment reason, MemorySegment reasonLen) {
      try {
         return (boolean)QUICHE_CONN_LOCAL_ERROR.invokeExact(
            (MemorySegment)conn, (MemorySegment)isApp, (MemorySegment)errorCode, (MemorySegment)reason, (MemorySegment)reasonLen
         );
      } catch (Throwable var6) {
         throw SneakyThrow.sneakyThrow(var6);
      }
   }

   public static void connStats(MemorySegment conn, MemorySegment stats) {
      try {
         QUICHE_CONN_STATS.invokeExact((MemorySegment)conn, (MemorySegment)stats);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static boolean connPeerTransportParams(MemorySegment conn, MemorySegment params) {
      try {
         return (boolean)QUICHE_CONN_PEER_TRANSPORT_PARAMS.invokeExact((MemorySegment)conn, (MemorySegment)params);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static int connPathStats(MemorySegment conn, long idx, MemorySegment stats) {
      try {
         return (int)QUICHE_CONN_PATH_STATS.invokeExact((MemorySegment)conn, (long)idx, (MemorySegment)stats);
      } catch (Throwable var5) {
         throw SneakyThrow.sneakyThrow(var5);
      }
   }

   public static MemorySegment connSourceIds(MemorySegment conn) {
      try {
         return (MemorySegment)QUICHE_CONN_SOURCE_IDS.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static boolean connectionIdIterNext(MemorySegment iter, MemorySegment id, MemorySegment idLen) {
      try {
         return (boolean)QUICHE_CONNECTION_ID_ITER_NEXT.invokeExact((MemorySegment)iter, (MemorySegment)id, (MemorySegment)idLen);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void connectionIdIterFree(MemorySegment iter) {
      try {
         QUICHE_CONNECTION_ID_ITER_FREE.invokeExact((MemorySegment)iter);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static boolean connRetiredScidNext(MemorySegment conn, MemorySegment id, MemorySegment idLen) {
      try {
         return (boolean)QUICHE_CONN_RETIRED_SCID_NEXT.invokeExact((MemorySegment)conn, (MemorySegment)id, (MemorySegment)idLen);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static long connRetiredScids(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_RETIRED_SCIDS.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long connAvailableDcids(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_AVAILABLE_DCIDS.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long connScidsLeft(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_SCIDS_LEFT.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long connActiveScids(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_ACTIVE_SCIDS.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static int connNewScid(MemorySegment conn, MemorySegment scid, long scidLen, MemorySegment resetToken, boolean retire, MemorySegment seq) {
      try {
         return (int)QUICHE_CONN_NEW_SCID.invokeExact(
            (MemorySegment)conn, (MemorySegment)scid, (long)scidLen, (MemorySegment)resetToken, (boolean)retire, (MemorySegment)seq
         );
      } catch (Throwable var8) {
         throw SneakyThrow.sneakyThrow(var8);
      }
   }

   public static int connRetireDcid(MemorySegment conn, long seq) {
      try {
         return (int)QUICHE_CONN_RETIRE_DCID.invokeExact((MemorySegment)conn, (long)seq);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static int connProbePath(MemorySegment conn, MemorySegment local, int localLen, MemorySegment peer, int peerLen, MemorySegment seq) {
      try {
         return (int)QUICHE_CONN_PROBE_PATH.invokeExact(
            (MemorySegment)conn, (MemorySegment)local, (int)localLen, (MemorySegment)peer, (int)peerLen, (MemorySegment)seq
         );
      } catch (Throwable var7) {
         throw SneakyThrow.sneakyThrow(var7);
      }
   }

   public static int connMigrateSource(MemorySegment conn, MemorySegment local, int localLen, MemorySegment seq) {
      try {
         return (int)QUICHE_CONN_MIGRATE_SOURCE.invokeExact((MemorySegment)conn, (MemorySegment)local, (int)localLen, (MemorySegment)seq);
      } catch (Throwable var5) {
         throw SneakyThrow.sneakyThrow(var5);
      }
   }

   public static int connMigrate(MemorySegment conn, MemorySegment local, int localLen, MemorySegment peer, int peerLen, MemorySegment seq) {
      try {
         return (int)QUICHE_CONN_MIGRATE.invokeExact(
            (MemorySegment)conn, (MemorySegment)local, (int)localLen, (MemorySegment)peer, (int)peerLen, (MemorySegment)seq
         );
      } catch (Throwable var7) {
         throw SneakyThrow.sneakyThrow(var7);
      }
   }

   public static MemorySegment connPathEventNext(MemorySegment conn) {
      try {
         return (MemorySegment)QUICHE_CONN_PATH_EVENT_NEXT.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static int pathEventType(MemorySegment ev) {
      try {
         return (int)QUICHE_PATH_EVENT_TYPE.invokeExact((MemorySegment)ev);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static void pathEventNew(MemorySegment ev, MemorySegment localAddr, MemorySegment localAddrLen, MemorySegment peerAddr, MemorySegment peerAddrLen) {
      try {
         QUICHE_PATH_EVENT_NEW.invokeExact(
            (MemorySegment)ev, (MemorySegment)localAddr, (MemorySegment)localAddrLen, (MemorySegment)peerAddr, (MemorySegment)peerAddrLen
         );
      } catch (Throwable var6) {
         throw SneakyThrow.sneakyThrow(var6);
      }
   }

   public static void pathEventValidated(
      MemorySegment ev, MemorySegment localAddr, MemorySegment localAddrLen, MemorySegment peerAddr, MemorySegment peerAddrLen
   ) {
      try {
         QUICHE_PATH_EVENT_VALIDATED.invokeExact(
            (MemorySegment)ev, (MemorySegment)localAddr, (MemorySegment)localAddrLen, (MemorySegment)peerAddr, (MemorySegment)peerAddrLen
         );
      } catch (Throwable var6) {
         throw SneakyThrow.sneakyThrow(var6);
      }
   }

   public static void pathEventFailedValidation(
      MemorySegment ev, MemorySegment localAddr, MemorySegment localAddrLen, MemorySegment peerAddr, MemorySegment peerAddrLen
   ) {
      try {
         QUICHE_PATH_EVENT_FAILED_VALIDATION.invokeExact(
            (MemorySegment)ev, (MemorySegment)localAddr, (MemorySegment)localAddrLen, (MemorySegment)peerAddr, (MemorySegment)peerAddrLen
         );
      } catch (Throwable var6) {
         throw SneakyThrow.sneakyThrow(var6);
      }
   }

   public static void pathEventClosed(MemorySegment ev, MemorySegment localAddr, MemorySegment localAddrLen, MemorySegment peerAddr, MemorySegment peerAddrLen) {
      try {
         QUICHE_PATH_EVENT_CLOSED.invokeExact(
            (MemorySegment)ev, (MemorySegment)localAddr, (MemorySegment)localAddrLen, (MemorySegment)peerAddr, (MemorySegment)peerAddrLen
         );
      } catch (Throwable var6) {
         throw SneakyThrow.sneakyThrow(var6);
      }
   }

   public static void pathEventReusedSourceConnectionId(
      MemorySegment ev,
      MemorySegment oldLocalAddr,
      MemorySegment oldLocalAddrLen,
      MemorySegment oldPeerAddr,
      MemorySegment oldPeerAddrLen,
      MemorySegment localAddr,
      MemorySegment localAddrLen,
      MemorySegment peerAddr,
      MemorySegment peerAddrLen,
      MemorySegment seq
   ) {
      try {
         QUICHE_PATH_EVENT_REUSED_SOURCE_CONNECTION_ID.invokeExact(
            (MemorySegment)ev,
            (MemorySegment)oldLocalAddr,
            (MemorySegment)oldLocalAddrLen,
            (MemorySegment)oldPeerAddr,
            (MemorySegment)oldPeerAddrLen,
            (MemorySegment)localAddr,
            (MemorySegment)localAddrLen,
            (MemorySegment)peerAddr,
            (MemorySegment)peerAddrLen,
            (MemorySegment)seq
         );
      } catch (Throwable var11) {
         throw SneakyThrow.sneakyThrow(var11);
      }
   }

   public static void pathEventPeerMigrated(
      MemorySegment ev, MemorySegment localAddr, MemorySegment localAddrLen, MemorySegment peerAddr, MemorySegment peerAddrLen
   ) {
      try {
         QUICHE_PATH_EVENT_PEER_MIGRATED.invokeExact(
            (MemorySegment)ev, (MemorySegment)localAddr, (MemorySegment)localAddrLen, (MemorySegment)peerAddr, (MemorySegment)peerAddrLen
         );
      } catch (Throwable var6) {
         throw SneakyThrow.sneakyThrow(var6);
      }
   }

   public static void pathEventFree(MemorySegment ev) {
      try {
         QUICHE_PATH_EVENT_FREE.invokeExact((MemorySegment)ev);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static MemorySegment connPathsIter(MemorySegment conn, MemorySegment from, long fromLen) {
      try {
         return (MemorySegment)QUICHE_CONN_PATHS_ITER.invokeExact((MemorySegment)conn, (MemorySegment)from, (long)fromLen);
      } catch (Throwable var5) {
         throw SneakyThrow.sneakyThrow(var5);
      }
   }

   public static boolean socketAddrIterNext(MemorySegment iter, MemorySegment addr, MemorySegment addrLen) {
      try {
         return (boolean)QUICHE_SOCKET_ADDR_ITER_NEXT.invokeExact((MemorySegment)iter, (MemorySegment)addr, (MemorySegment)addrLen);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void socketAddrIterFree(MemorySegment iter) {
      try {
         QUICHE_SOCKET_ADDR_ITER_FREE.invokeExact((MemorySegment)iter);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static int connIsPathValidated(MemorySegment conn, MemorySegment local, long localLen, MemorySegment peer, long peerLen) {
      try {
         return (int)QUICHE_CONN_IS_PATH_VALIDATED.invokeExact((MemorySegment)conn, (MemorySegment)local, (long)localLen, (MemorySegment)peer, (long)peerLen);
      } catch (Throwable var8) {
         throw SneakyThrow.sneakyThrow(var8);
      }
   }

   public static long connDgramMaxWritableLen(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_DGRAM_MAX_WRITABLE_LEN.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long connDgramRecvFrontLen(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_DGRAM_RECV_FRONT_LEN.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long connDgramRecvQueueLen(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_DGRAM_RECV_QUEUE_LEN.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long connDgramRecvQueueByteSize(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_DGRAM_RECV_QUEUE_BYTE_SIZE.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long connDgramSendQueueLen(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_DGRAM_SEND_QUEUE_LEN.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long connDgramSendQueueByteSize(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_DGRAM_SEND_QUEUE_BYTE_SIZE.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long connDgramRecv(MemorySegment conn, MemorySegment buf, long bufLen) {
      try {
         return (long)QUICHE_CONN_DGRAM_RECV.invokeExact((MemorySegment)conn, (MemorySegment)buf, (long)bufLen);
      } catch (Throwable var5) {
         throw SneakyThrow.sneakyThrow(var5);
      }
   }

   public static long connDgramSend(MemorySegment conn, MemorySegment buf, long bufLen) {
      try {
         return (long)QUICHE_CONN_DGRAM_SEND.invokeExact((MemorySegment)conn, (MemorySegment)buf, (long)bufLen);
      } catch (Throwable var5) {
         throw SneakyThrow.sneakyThrow(var5);
      }
   }

   public static void connDgramPurgeOutgoing(MemorySegment conn, MemorySegment callback) {
      try {
         QUICHE_CONN_DGRAM_PURGE_OUTGOING.invokeExact((MemorySegment)conn, (MemorySegment)callback);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static boolean connIsDgramSendQueueFull(MemorySegment conn) {
      try {
         return (boolean)QUICHE_CONN_IS_DGRAM_SEND_QUEUE_FULL.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static boolean connIsDgramRecvQueueFull(MemorySegment conn) {
      try {
         return (boolean)QUICHE_CONN_IS_DGRAM_RECV_QUEUE_FULL.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long connSendAckEliciting(MemorySegment conn) {
      try {
         return (long)QUICHE_CONN_SEND_ACK_ELICITING.invokeExact((MemorySegment)conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long connSendAckElicitingOnPath(MemorySegment conn, MemorySegment local, int localLen, MemorySegment peer, int peerLen) {
      try {
         return (long)QUICHE_CONN_SEND_ACK_ELICITING_ON_PATH.invokeExact(
            (MemorySegment)conn, (MemorySegment)local, (int)localLen, (MemorySegment)peer, (int)peerLen
         );
      } catch (Throwable var6) {
         throw SneakyThrow.sneakyThrow(var6);
      }
   }

   public static MemorySegment h3ConfigNew() {
      try {
         return (MemorySegment)QUICHE_H3_CONFIG_NEW.invokeExact();
      } catch (Throwable var1) {
         throw SneakyThrow.sneakyThrow(var1);
      }
   }

   public static void h3ConfigSetMaxFieldSectionSize(MemorySegment config, long v) {
      try {
         QUICHE_H3_CONFIG_SET_MAX_FIELD_SECTION_SIZE.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void h3ConfigSetQpackMaxTableCapacity(MemorySegment config, long v) {
      try {
         QUICHE_H3_CONFIG_SET_QPACK_MAX_TABLE_CAPACITY.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void h3ConfigSetQpackBlockedStreams(MemorySegment config, long v) {
      try {
         QUICHE_H3_CONFIG_SET_QPACK_BLOCKED_STREAMS.invokeExact((MemorySegment)config, (long)v);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static void h3ConfigEnableExtendedConnect(MemorySegment config, boolean v) {
      try {
         QUICHE_H3_CONFIG_ENABLE_EXTENDED_CONNECT.invokeExact((MemorySegment)config, (boolean)v);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static void h3ConfigFree(MemorySegment config) {
      try {
         QUICHE_H3_CONFIG_FREE.invokeExact((MemorySegment)config);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static MemorySegment h3ConnNewWithTransport(MemorySegment conn, MemorySegment config) {
      try {
         return (MemorySegment)QUICHE_H3_CONN_NEW_WITH_TRANSPORT.invokeExact((MemorySegment)conn, (MemorySegment)config);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static long h3ConnPoll(MemorySegment h3Conn, MemorySegment conn, MemorySegment ev) {
      try {
         return (long)QUICHE_H3_CONN_POLL.invokeExact((MemorySegment)h3Conn, (MemorySegment)conn, (MemorySegment)ev);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static int h3EventType(MemorySegment ev) {
      try {
         return (int)QUICHE_H3_EVENT_TYPE.invokeExact((MemorySegment)ev);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static int h3EventForEachHeader(MemorySegment ev, MemorySegment callback, MemorySegment argp) {
      try {
         return (int)QUICHE_H3_EVENT_FOR_EACH_HEADER.invokeExact((MemorySegment)ev, (MemorySegment)callback, (MemorySegment)argp);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static int h3ForEachSetting(MemorySegment h3Conn, MemorySegment callback, MemorySegment argp) {
      try {
         return (int)QUICHE_H3_FOR_EACH_SETTING.invokeExact((MemorySegment)h3Conn, (MemorySegment)callback, (MemorySegment)argp);
      } catch (Throwable var4) {
         throw SneakyThrow.sneakyThrow(var4);
      }
   }

   public static boolean h3EventHeadersHasMoreFrames(MemorySegment ev) {
      try {
         return (boolean)QUICHE_H3_EVENT_HEADERS_HAS_MORE_FRAMES.invokeExact((MemorySegment)ev);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static boolean h3ExtendedConnectEnabledByPeer(MemorySegment h3Conn) {
      try {
         return (boolean)QUICHE_H3_EXTENDED_CONNECT_ENABLED_BY_PEER.invokeExact((MemorySegment)h3Conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static void h3EventFree(MemorySegment ev) {
      try {
         QUICHE_H3_EVENT_FREE.invokeExact((MemorySegment)ev);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   public static long h3SendRequest(MemorySegment h3Conn, MemorySegment conn, MemorySegment headers, long headersLen, boolean fin) {
      try {
         return (long)QUICHE_H3_SEND_REQUEST.invokeExact((MemorySegment)h3Conn, (MemorySegment)conn, (MemorySegment)headers, (long)headersLen, (boolean)fin);
      } catch (Throwable var7) {
         throw SneakyThrow.sneakyThrow(var7);
      }
   }

   public static int h3SendResponse(MemorySegment h3Conn, MemorySegment conn, long streamId, MemorySegment headers, long headersLen, boolean fin) {
      try {
         return (int)QUICHE_H3_SEND_RESPONSE.invokeExact(
            (MemorySegment)h3Conn, (MemorySegment)conn, (long)streamId, (MemorySegment)headers, (long)headersLen, (boolean)fin
         );
      } catch (Throwable var9) {
         throw SneakyThrow.sneakyThrow(var9);
      }
   }

   public static int h3SendResponseWithPriority(
      MemorySegment h3Conn, MemorySegment conn, long streamId, MemorySegment headers, long headersLen, MemorySegment priority, boolean fin
   ) {
      try {
         return (int)QUICHE_H3_SEND_RESPONSE_WITH_PRIORITY.invokeExact(
            (MemorySegment)h3Conn, (MemorySegment)conn, (long)streamId, (MemorySegment)headers, (long)headersLen, (MemorySegment)priority, (boolean)fin
         );
      } catch (Throwable var10) {
         throw SneakyThrow.sneakyThrow(var10);
      }
   }

   public static int h3SendAdditionalHeaders(
      MemorySegment h3Conn, MemorySegment conn, long streamId, MemorySegment headers, long headersLen, boolean hasTrailers, boolean fin
   ) {
      try {
         return (int)QUICHE_H3_SEND_ADDITIONAL_HEADERS.invokeExact(
            (MemorySegment)h3Conn, (MemorySegment)conn, (long)streamId, (MemorySegment)headers, (long)headersLen, (boolean)hasTrailers, (boolean)fin
         );
      } catch (Throwable var10) {
         throw SneakyThrow.sneakyThrow(var10);
      }
   }

   public static long h3SendBody(MemorySegment h3Conn, MemorySegment conn, long streamId, MemorySegment buf, long bufLen, boolean fin) {
      try {
         return (long)QUICHE_H3_SEND_BODY.invokeExact(
            (MemorySegment)h3Conn, (MemorySegment)conn, (long)streamId, (MemorySegment)buf, (long)bufLen, (boolean)fin
         );
      } catch (Throwable var9) {
         throw SneakyThrow.sneakyThrow(var9);
      }
   }

   public static long h3RecvBody(MemorySegment h3Conn, MemorySegment conn, long streamId, MemorySegment buf, long bufLen) {
      try {
         return (long)QUICHE_H3_RECV_BODY.invokeExact((MemorySegment)h3Conn, (MemorySegment)conn, (long)streamId, (MemorySegment)buf, (long)bufLen);
      } catch (Throwable var8) {
         throw SneakyThrow.sneakyThrow(var8);
      }
   }

   public static int h3SendGoaway(MemorySegment h3Conn, MemorySegment conn, long id) {
      try {
         return (int)QUICHE_H3_SEND_GOAWAY.invokeExact((MemorySegment)h3Conn, (MemorySegment)conn, (long)id);
      } catch (Throwable var5) {
         throw SneakyThrow.sneakyThrow(var5);
      }
   }

   public static int h3ParseExtensiblePriority(MemorySegment priority, long priorityLen, MemorySegment parsed) {
      try {
         return (int)QUICHE_H3_PARSE_EXTENSIBLE_PRIORITY.invokeExact((MemorySegment)priority, (long)priorityLen, (MemorySegment)parsed);
      } catch (Throwable var5) {
         throw SneakyThrow.sneakyThrow(var5);
      }
   }

   public static int h3SendPriorityUpdateForRequest(MemorySegment h3Conn, MemorySegment conn, long streamId, MemorySegment priority) {
      try {
         return (int)QUICHE_H3_SEND_PRIORITY_UPDATE_FOR_REQUEST.invokeExact((MemorySegment)h3Conn, (MemorySegment)conn, (long)streamId, (MemorySegment)priority);
      } catch (Throwable var6) {
         throw SneakyThrow.sneakyThrow(var6);
      }
   }

   public static int h3TakeLastPriorityUpdate(MemorySegment h3Conn, long streamId, MemorySegment callback, MemorySegment argp) {
      try {
         return (int)QUICHE_H3_TAKE_LAST_PRIORITY_UPDATE.invokeExact((MemorySegment)h3Conn, (long)streamId, (MemorySegment)callback, (MemorySegment)argp);
      } catch (Throwable var6) {
         throw SneakyThrow.sneakyThrow(var6);
      }
   }

   public static boolean h3DgramEnabledByPeer(MemorySegment h3Conn, MemorySegment conn) {
      try {
         return (boolean)QUICHE_H3_DGRAM_ENABLED_BY_PEER.invokeExact((MemorySegment)h3Conn, (MemorySegment)conn);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static void h3ConnStats(MemorySegment h3Conn, MemorySegment stats) {
      try {
         QUICHE_H3_CONN_STATS.invokeExact((MemorySegment)h3Conn, (MemorySegment)stats);
      } catch (Throwable var3) {
         throw SneakyThrow.sneakyThrow(var3);
      }
   }

   public static void h3ConnFree(MemorySegment h3Conn) {
      try {
         QUICHE_H3_CONN_FREE.invokeExact((MemorySegment)h3Conn);
      } catch (Throwable var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }

   static {
      String osName = System.getProperty("os.name").toLowerCase();
      String arch = System.getProperty("os.arch").toLowerCase();
      if (osName.contains("linux")) {
         PLATFORM = QuicheNative.Platform.LINUX;
      } else if (!osName.contains("mac") && !osName.contains("darwin")) {
         if (!osName.contains("win")) {
            throw new UnsupportedOperationException("Unsupported OS: " + osName);
         }

         PLATFORM = QuicheNative.Platform.WINDOWS;
      } else {
         PLATFORM = QuicheNative.Platform.MACOS;
      }
      String resourcePath = switch (PLATFORM) {
         case LINUX -> {
            if (!"amd64".equals(arch) && !"x86_64".equals(arch)) {
               throw new UnsupportedOperationException("Unsupported Linux architecture: " + arch);
            }

            yield "/native/linux-x64/libquiche.so";
         }
         case MACOS -> {
            if (!"aarch64".equals(arch)) {
               throw new UnsupportedOperationException("Unsupported macOS architecture: " + arch);
            }

            yield "/native/osx-arm64/libquiche.dylib";
         }
         case WINDOWS -> {
            if (!"amd64".equals(arch) && !"x86_64".equals(arch)) {
               throw new UnsupportedOperationException("Unsupported Windows architecture: " + arch);
            }

            yield "/native/win-x64/quiche.dll";
         }
      };

      try (InputStream stream = QuicheNative.class.getResourceAsStream(resourcePath)) {
         if (stream == null) {
            throw new IllegalStateException("Native library not found on classpath: " + resourcePath);
         }

         Path tmpDir = Files.createTempDirectory("quiche-native");
         Path libPath = tmpDir.resolve(Path.of(resourcePath).getFileName());
         Files.copy(stream, libPath, StandardCopyOption.REPLACE_EXISTING);
         libPath.toFile().deleteOnExit();
         tmpDir.toFile().deleteOnExit();
         LOOKUP = SymbolLookup.libraryLookup(libPath, GLOBAL_ARENA);
      } catch (IOException var8) {
         throw new ExceptionInInitializerError(var8);
      }

      C_POINTER = ValueLayout.ADDRESS;
      C_INT = ValueLayout.JAVA_INT;
      C_LONG = ValueLayout.JAVA_LONG;
      C_BYTE = ValueLayout.JAVA_BYTE;
      C_BOOL = ValueLayout.JAVA_BOOLEAN;
      C_SHORT = ValueLayout.JAVA_SHORT;
      C_SIZE_T = (ValueLayout)LINKER.canonicalLayouts().get("size_t");

      SOCKADDR_STORAGE_LAYOUT = switch (PLATFORM) {
         case LINUX -> MemoryLayout.structLayout(
               C_SHORT.withName("ss_family"), MemoryLayout.sequenceLayout(118L, C_BYTE).withName("__ss_padding"), C_LONG.withName("__ss_align")
            )
            .withName("sockaddr_storage");
         case MACOS -> MemoryLayout.structLayout(
               C_BYTE.withName("ss_len"),
               C_BYTE.withName("ss_family"),
               MemoryLayout.sequenceLayout(6L, C_BYTE).withName("__ss_pad1"),
               C_LONG.withName("__ss_align"),
               MemoryLayout.sequenceLayout(112L, C_BYTE).withName("__ss_pad2")
            )
            .withName("sockaddr_storage");
         case WINDOWS -> MemoryLayout.structLayout(
               C_SHORT.withName("ss_family"),
               MemoryLayout.sequenceLayout(6L, C_BYTE).withName("__ss_pad1"),
               C_LONG.withName("__ss_align"),
               MemoryLayout.sequenceLayout(112L, C_BYTE).withName("__ss_pad2")
            )
            .withName("sockaddr_storage");
      };

      SOCKADDR_LAYOUT = switch (PLATFORM) {
         case LINUX, WINDOWS -> MemoryLayout.structLayout(C_SHORT.withName("sa_family"), MemoryLayout.sequenceLayout(14L, C_BYTE).withName("sa_data"))
            .withName("sockaddr");
         case MACOS -> MemoryLayout.structLayout(
               C_BYTE.withName("sa_len"), C_BYTE.withName("sa_family"), MemoryLayout.sequenceLayout(14L, C_BYTE).withName("sa_data")
            )
            .withName("sockaddr");
      };

      TIMESPEC_LAYOUT = switch (PLATFORM) {
         case LINUX, MACOS -> MemoryLayout.structLayout(C_LONG.withName("tv_sec"), C_LONG.withName("tv_nsec")).withName("timespec");
         case WINDOWS -> MemoryLayout.structLayout(C_LONG.withName("tv_sec"), C_INT.withName("tv_nsec"), MemoryLayout.paddingLayout(4L)).withName("timespec");
      };
      RECV_INFO_LAYOUT = MemoryLayout.structLayout(
            C_POINTER.withName("from"),
            C_INT.withName("from_len"),
            MemoryLayout.paddingLayout(4L),
            C_POINTER.withName("to"),
            C_INT.withName("to_len"),
            MemoryLayout.paddingLayout(4L)
         )
         .withName("quiche_recv_info");
      SEND_INFO_LAYOUT = MemoryLayout.structLayout(
            SOCKADDR_STORAGE_LAYOUT.withName("from"),
            C_INT.withName("from_len"),
            MemoryLayout.paddingLayout(4L),
            SOCKADDR_STORAGE_LAYOUT.withName("to"),
            C_INT.withName("to_len"),
            MemoryLayout.paddingLayout(4L),
            TIMESPEC_LAYOUT.withName("at")
         )
         .withName("quiche_send_info");
      SOCKADDR_FAMILY = SOCKADDR_LAYOUT.varHandle(PathElement.groupElement("sa_family"));
      SOCKADDR_STORAGE_FAMILY = SOCKADDR_STORAGE_LAYOUT.varHandle(PathElement.groupElement("ss_family"));
      SOCKADDR_STORAGE_LEN = PLATFORM == QuicheNative.Platform.MACOS ? SOCKADDR_STORAGE_LAYOUT.varHandle(PathElement.groupElement("ss_len")) : null;
      SOCKADDR_STORAGE_DATA_OFFSET = SOCKADDR_STORAGE_LAYOUT.byteOffset(PathElement.groupElement("ss_family"))
         + SOCKADDR_STORAGE_LAYOUT.select(PathElement.groupElement("ss_family")).byteSize();
      TIMESPEC_SEC = TIMESPEC_LAYOUT.varHandle(PathElement.groupElement("tv_sec"));
      TIMESPEC_NSEC = TIMESPEC_LAYOUT.varHandle(PathElement.groupElement("tv_nsec"));
      RECV_INFO_FROM = RECV_INFO_LAYOUT.varHandle(PathElement.groupElement("from"));
      RECV_INFO_FROM_LEN = RECV_INFO_LAYOUT.varHandle(PathElement.groupElement("from_len"));
      RECV_INFO_TO = RECV_INFO_LAYOUT.varHandle(PathElement.groupElement("to"));
      RECV_INFO_TO_LEN = RECV_INFO_LAYOUT.varHandle(PathElement.groupElement("to_len"));
      SEND_INFO_FROM_OFFSET = SEND_INFO_LAYOUT.byteOffset(PathElement.groupElement("from"));
      SEND_INFO_FROM_LEN = SEND_INFO_LAYOUT.varHandle(PathElement.groupElement("from_len"));
      SEND_INFO_TO_OFFSET = SEND_INFO_LAYOUT.byteOffset(PathElement.groupElement("to"));
      SEND_INFO_TO_LEN = SEND_INFO_LAYOUT.varHandle(PathElement.groupElement("to_len"));
      SEND_INFO_AT_OFFSET = SEND_INFO_LAYOUT.byteOffset(PathElement.groupElement("at"));
      QUICHE_VERSION = downcall("quiche_version", FunctionDescriptor.of(C_POINTER));
      QUICHE_ENABLE_DEBUG_LOGGING = downcall("quiche_enable_debug_logging", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER));
      QUICHE_PUT_VARINT = downcall("quiche_put_varint", FunctionDescriptor.of(C_INT, C_POINTER, C_LONG, C_LONG));
      QUICHE_GET_VARINT = downcall("quiche_get_varint", FunctionDescriptor.of(C_LONG, C_POINTER, C_LONG, C_POINTER));
      QUICHE_CONFIG_NEW = downcall("quiche_config_new", FunctionDescriptor.of(C_POINTER, C_INT));
      QUICHE_CONFIG_LOAD_CERT = downcall("quiche_config_load_cert", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_LONG));
      QUICHE_CONFIG_LOAD_CERT_CHAIN_FROM_PEM_FILE = downcall("quiche_config_load_cert_chain_from_pem_file", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER));
      QUICHE_CONFIG_LOAD_PRIV_KEY = downcall("quiche_config_load_priv_key", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_LONG));
      QUICHE_CONFIG_LOAD_PRIV_KEY_FROM_PEM_FILE = downcall("quiche_config_load_priv_key_from_pem_file", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER));
      QUICHE_CONFIG_LOAD_VERIFY_LOCATIONS_FROM_FILE = downcall(
         "quiche_config_load_verify_locations_from_file", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER)
      );
      QUICHE_CONFIG_LOAD_VERIFY_LOCATIONS_FROM_DIRECTORY = downcall(
         "quiche_config_load_verify_locations_from_directory", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER)
      );
      QUICHE_CONFIG_VERIFY_PEER = downcall("quiche_config_verify_peer", FunctionDescriptor.ofVoid(C_POINTER, C_BOOL));
      QUICHE_CONFIG_VERIFY_PEER_OPTIONAL = downcall("quiche_config_verify_peer_optional", FunctionDescriptor.ofVoid(C_POINTER));
      QUICHE_CONFIG_GREASE = downcall("quiche_config_grease", FunctionDescriptor.ofVoid(C_POINTER, C_BOOL));
      QUICHE_CONFIG_DISCOVER_PMTU = downcall("quiche_config_discover_pmtu", FunctionDescriptor.ofVoid(C_POINTER, C_BOOL));
      QUICHE_CONFIG_LOG_KEYS = downcall("quiche_config_log_keys", FunctionDescriptor.ofVoid(C_POINTER));
      QUICHE_CONFIG_ENABLE_EARLY_DATA = downcall("quiche_config_enable_early_data", FunctionDescriptor.ofVoid(C_POINTER));
      QUICHE_CONFIG_SET_APPLICATION_PROTOS = downcall("quiche_config_set_application_protos", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_LONG));
      QUICHE_CONFIG_SET_MAX_AMPLIFICATION_FACTOR = downcall("quiche_config_set_max_amplification_factor", FunctionDescriptor.ofVoid(C_POINTER, C_LONG));
      QUICHE_CONFIG_SET_MAX_IDLE_TIMEOUT = downcall("quiche_config_set_max_idle_timeout", FunctionDescriptor.ofVoid(C_POINTER, C_LONG));
      QUICHE_CONFIG_SET_MAX_RECV_UDP_PAYLOAD_SIZE = downcall("quiche_config_set_max_recv_udp_payload_size", FunctionDescriptor.ofVoid(C_POINTER, C_LONG));
      QUICHE_CONFIG_SET_MAX_SEND_UDP_PAYLOAD_SIZE = downcall("quiche_config_set_max_send_udp_payload_size", FunctionDescriptor.ofVoid(C_POINTER, C_LONG));
      QUICHE_CONFIG_SET_INITIAL_MAX_DATA = downcall("quiche_config_set_initial_max_data", FunctionDescriptor.ofVoid(C_POINTER, C_LONG));
      QUICHE_CONFIG_SET_INITIAL_MAX_STREAM_DATA_BIDI_LOCAL = downcall(
         "quiche_config_set_initial_max_stream_data_bidi_local", FunctionDescriptor.ofVoid(C_POINTER, C_LONG)
      );
      QUICHE_CONFIG_SET_INITIAL_MAX_STREAM_DATA_BIDI_REMOTE = downcall(
         "quiche_config_set_initial_max_stream_data_bidi_remote", FunctionDescriptor.ofVoid(C_POINTER, C_LONG)
      );
      QUICHE_CONFIG_SET_INITIAL_MAX_STREAM_DATA_UNI = downcall("quiche_config_set_initial_max_stream_data_uni", FunctionDescriptor.ofVoid(C_POINTER, C_LONG));
      QUICHE_CONFIG_SET_INITIAL_MAX_STREAMS_BIDI = downcall("quiche_config_set_initial_max_streams_bidi", FunctionDescriptor.ofVoid(C_POINTER, C_LONG));
      QUICHE_CONFIG_SET_INITIAL_MAX_STREAMS_UNI = downcall("quiche_config_set_initial_max_streams_uni", FunctionDescriptor.ofVoid(C_POINTER, C_LONG));
      QUICHE_CONFIG_SET_ACK_DELAY_EXPONENT = downcall("quiche_config_set_ack_delay_exponent", FunctionDescriptor.ofVoid(C_POINTER, C_LONG));
      QUICHE_CONFIG_SET_MAX_ACK_DELAY = downcall("quiche_config_set_max_ack_delay", FunctionDescriptor.ofVoid(C_POINTER, C_LONG));
      QUICHE_CONFIG_SET_DISABLE_ACTIVE_MIGRATION = downcall("quiche_config_set_disable_active_migration", FunctionDescriptor.ofVoid(C_POINTER, C_BOOL));
      QUICHE_CONFIG_SET_CC_ALGORITHM_NAME = downcall("quiche_config_set_cc_algorithm_name", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER));
      QUICHE_CONFIG_SET_INITIAL_CONGESTION_WINDOW_PACKETS = downcall(
         "quiche_config_set_initial_congestion_window_packets", FunctionDescriptor.ofVoid(C_POINTER, C_LONG)
      );
      QUICHE_CONFIG_SET_CC_ALGORITHM = downcall("quiche_config_set_cc_algorithm", FunctionDescriptor.ofVoid(C_POINTER, C_INT));
      QUICHE_CONFIG_ENABLE_HYSTART = downcall("quiche_config_enable_hystart", FunctionDescriptor.ofVoid(C_POINTER, C_BOOL));
      QUICHE_CONFIG_ENABLE_PACING = downcall("quiche_config_enable_pacing", FunctionDescriptor.ofVoid(C_POINTER, C_BOOL));
      QUICHE_CONFIG_SET_MAX_PACING_RATE = downcall("quiche_config_set_max_pacing_rate", FunctionDescriptor.ofVoid(C_POINTER, C_LONG));
      QUICHE_CONFIG_ENABLE_DGRAM = downcall("quiche_config_enable_dgram", FunctionDescriptor.ofVoid(C_POINTER, C_BOOL, C_LONG, C_LONG));
      QUICHE_CONFIG_SET_MAX_CONNECTION_WINDOW = downcall("quiche_config_set_max_connection_window", FunctionDescriptor.ofVoid(C_POINTER, C_LONG));
      QUICHE_CONFIG_SET_MAX_STREAM_WINDOW = downcall("quiche_config_set_max_stream_window", FunctionDescriptor.ofVoid(C_POINTER, C_LONG));
      QUICHE_CONFIG_SET_ACTIVE_CONNECTION_ID_LIMIT = downcall("quiche_config_set_active_connection_id_limit", FunctionDescriptor.ofVoid(C_POINTER, C_LONG));
      QUICHE_CONFIG_SET_STATELESS_RESET_TOKEN = downcall("quiche_config_set_stateless_reset_token", FunctionDescriptor.ofVoid(C_POINTER, C_POINTER));
      QUICHE_CONFIG_SET_DISABLE_DCID_REUSE = downcall("quiche_config_set_disable_dcid_reuse", FunctionDescriptor.ofVoid(C_POINTER, C_BOOL));
      QUICHE_CONFIG_SET_TICKET_KEY = downcall("quiche_config_set_ticket_key", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_LONG));
      QUICHE_CONFIG_FREE = downcall("quiche_config_free", FunctionDescriptor.ofVoid(C_POINTER));
      QUICHE_HEADER_INFO = downcall(
         "quiche_header_info",
         FunctionDescriptor.of(C_INT, C_POINTER, C_LONG, C_LONG, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER)
      );
      QUICHE_ACCEPT = downcall(
         "quiche_accept", FunctionDescriptor.of(C_POINTER, C_POINTER, C_LONG, C_POINTER, C_LONG, C_POINTER, C_INT, C_POINTER, C_INT, C_POINTER)
      );
      QUICHE_CONNECT = downcall("quiche_connect", FunctionDescriptor.of(C_POINTER, C_POINTER, C_POINTER, C_LONG, C_POINTER, C_INT, C_POINTER, C_INT, C_POINTER));
      QUICHE_NEGOTIATE_VERSION = downcall("quiche_negotiate_version", FunctionDescriptor.of(C_LONG, C_POINTER, C_LONG, C_POINTER, C_LONG, C_POINTER, C_LONG));
      QUICHE_RETRY = downcall(
         "quiche_retry", FunctionDescriptor.of(C_LONG, C_POINTER, C_LONG, C_POINTER, C_LONG, C_POINTER, C_LONG, C_POINTER, C_LONG, C_INT, C_POINTER, C_LONG)
      );
      QUICHE_VERSION_IS_SUPPORTED = downcall("quiche_version_is_supported", FunctionDescriptor.of(C_BOOL, C_INT));
      QUICHE_CONN_NEW_WITH_TLS = downcall(
         "quiche_conn_new_with_tls",
         FunctionDescriptor.of(C_POINTER, C_POINTER, C_LONG, C_POINTER, C_LONG, C_POINTER, C_INT, C_POINTER, C_INT, C_POINTER, C_POINTER, C_BOOL)
      );
      QUICHE_CONN_SET_KEYLOG_PATH = downcall("quiche_conn_set_keylog_path", FunctionDescriptor.of(C_BOOL, C_POINTER, C_POINTER));
      QUICHE_CONN_SET_KEYLOG_FD = downcall("quiche_conn_set_keylog_fd", FunctionDescriptor.ofVoid(C_POINTER, C_INT));
      QUICHE_CONN_SET_QLOG_PATH = downcall("quiche_conn_set_qlog_path", FunctionDescriptor.of(C_BOOL, C_POINTER, C_POINTER, C_POINTER, C_POINTER));
      QUICHE_CONN_SET_QLOG_FD = downcall("quiche_conn_set_qlog_fd", FunctionDescriptor.ofVoid(C_POINTER, C_INT, C_POINTER, C_POINTER));
      QUICHE_CONN_SET_SESSION = downcall("quiche_conn_set_session", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_LONG));
      QUICHE_CONN_SET_MAX_IDLE_TIMEOUT = downcall("quiche_conn_set_max_idle_timeout", FunctionDescriptor.of(C_INT, C_POINTER, C_LONG));
      QUICHE_CONN_RECV = downcall("quiche_conn_recv", FunctionDescriptor.of(C_LONG, C_POINTER, C_POINTER, C_LONG, C_POINTER));
      QUICHE_CONN_SEND = downcall("quiche_conn_send", FunctionDescriptor.of(C_LONG, C_POINTER, C_POINTER, C_LONG, C_POINTER));
      QUICHE_CONN_SEND_QUANTUM = downcall("quiche_conn_send_quantum", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_SEND_ON_PATH = downcall(
         "quiche_conn_send_on_path", FunctionDescriptor.of(C_LONG, C_POINTER, C_POINTER, C_LONG, C_POINTER, C_INT, C_POINTER, C_INT, C_POINTER)
      );
      QUICHE_CONN_SEND_QUANTUM_ON_PATH = downcall(
         "quiche_conn_send_quantum_on_path", FunctionDescriptor.of(C_LONG, C_POINTER, C_POINTER, C_INT, C_POINTER, C_INT)
      );
      QUICHE_CONN_TIMEOUT_AS_NANOS = downcall("quiche_conn_timeout_as_nanos", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_TIMEOUT_AS_MILLIS = downcall("quiche_conn_timeout_as_millis", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_ON_TIMEOUT = downcall("quiche_conn_on_timeout", FunctionDescriptor.ofVoid(C_POINTER));
      QUICHE_CONN_CLOSE = downcall("quiche_conn_close", FunctionDescriptor.of(C_INT, C_POINTER, C_BOOL, C_LONG, C_POINTER, C_LONG));
      QUICHE_CONN_FREE = downcall("quiche_conn_free", FunctionDescriptor.ofVoid(C_POINTER));
      QUICHE_CONN_STREAM_RECV = downcall("quiche_conn_stream_recv", FunctionDescriptor.of(C_LONG, C_POINTER, C_LONG, C_POINTER, C_LONG, C_POINTER, C_POINTER));
      QUICHE_CONN_STREAM_SEND = downcall("quiche_conn_stream_send", FunctionDescriptor.of(C_LONG, C_POINTER, C_LONG, C_POINTER, C_LONG, C_BOOL, C_POINTER));
      QUICHE_CONN_STREAM_PRIORITY = downcall("quiche_conn_stream_priority", FunctionDescriptor.of(C_INT, C_POINTER, C_LONG, C_BYTE, C_BOOL));
      QUICHE_CONN_STREAM_SHUTDOWN = downcall("quiche_conn_stream_shutdown", FunctionDescriptor.of(C_INT, C_POINTER, C_LONG, C_INT, C_LONG));
      QUICHE_CONN_STREAM_CAPACITY = downcall("quiche_conn_stream_capacity", FunctionDescriptor.of(C_LONG, C_POINTER, C_LONG));
      QUICHE_CONN_STREAM_READABLE = downcall("quiche_conn_stream_readable", FunctionDescriptor.of(C_BOOL, C_POINTER, C_LONG));
      QUICHE_CONN_STREAM_READABLE_NEXT = downcall("quiche_conn_stream_readable_next", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_STREAM_WRITABLE = downcall("quiche_conn_stream_writable", FunctionDescriptor.of(C_INT, C_POINTER, C_LONG, C_LONG));
      QUICHE_CONN_STREAM_WRITABLE_NEXT = downcall("quiche_conn_stream_writable_next", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_STREAM_FINISHED = downcall("quiche_conn_stream_finished", FunctionDescriptor.of(C_BOOL, C_POINTER, C_LONG));
      QUICHE_CONN_READABLE = downcall("quiche_conn_readable", FunctionDescriptor.of(C_POINTER, C_POINTER));
      QUICHE_CONN_WRITABLE = downcall("quiche_conn_writable", FunctionDescriptor.of(C_POINTER, C_POINTER));
      QUICHE_STREAM_ITER_NEXT = downcall("quiche_stream_iter_next", FunctionDescriptor.of(C_BOOL, C_POINTER, C_POINTER));
      QUICHE_STREAM_ITER_FREE = downcall("quiche_stream_iter_free", FunctionDescriptor.ofVoid(C_POINTER));
      QUICHE_CONN_MAX_SEND_UDP_PAYLOAD_SIZE = downcall("quiche_conn_max_send_udp_payload_size", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_IS_ESTABLISHED = downcall("quiche_conn_is_established", FunctionDescriptor.of(C_BOOL, C_POINTER));
      QUICHE_CONN_IS_RESUMED = downcall("quiche_conn_is_resumed", FunctionDescriptor.of(C_BOOL, C_POINTER));
      QUICHE_CONN_IS_IN_EARLY_DATA = downcall("quiche_conn_is_in_early_data", FunctionDescriptor.of(C_BOOL, C_POINTER));
      QUICHE_CONN_IS_READABLE = downcall("quiche_conn_is_readable", FunctionDescriptor.of(C_BOOL, C_POINTER));
      QUICHE_CONN_IS_DRAINING = downcall("quiche_conn_is_draining", FunctionDescriptor.of(C_BOOL, C_POINTER));
      QUICHE_CONN_IS_CLOSED = downcall("quiche_conn_is_closed", FunctionDescriptor.of(C_BOOL, C_POINTER));
      QUICHE_CONN_IS_TIMED_OUT = downcall("quiche_conn_is_timed_out", FunctionDescriptor.of(C_BOOL, C_POINTER));
      QUICHE_CONN_IS_SERVER = downcall("quiche_conn_is_server", FunctionDescriptor.of(C_BOOL, C_POINTER));
      QUICHE_CONN_PEER_STREAMS_LEFT_BIDI = downcall("quiche_conn_peer_streams_left_bidi", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_PEER_STREAMS_LEFT_UNI = downcall("quiche_conn_peer_streams_left_uni", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_TRACE_ID = downcall("quiche_conn_trace_id", FunctionDescriptor.ofVoid(C_POINTER, C_POINTER, C_POINTER));
      QUICHE_CONN_SOURCE_ID = downcall("quiche_conn_source_id", FunctionDescriptor.ofVoid(C_POINTER, C_POINTER, C_POINTER));
      QUICHE_CONN_DESTINATION_ID = downcall("quiche_conn_destination_id", FunctionDescriptor.ofVoid(C_POINTER, C_POINTER, C_POINTER));
      QUICHE_CONN_APPLICATION_PROTO = downcall("quiche_conn_application_proto", FunctionDescriptor.ofVoid(C_POINTER, C_POINTER, C_POINTER));
      QUICHE_CONN_PEER_CERT = downcall("quiche_conn_peer_cert", FunctionDescriptor.ofVoid(C_POINTER, C_POINTER, C_POINTER));
      QUICHE_CONN_SESSION = downcall("quiche_conn_session", FunctionDescriptor.ofVoid(C_POINTER, C_POINTER, C_POINTER));
      QUICHE_CONN_SERVER_NAME = downcall("quiche_conn_server_name", FunctionDescriptor.ofVoid(C_POINTER, C_POINTER, C_POINTER));
      QUICHE_CONN_PEER_ERROR = downcall("quiche_conn_peer_error", FunctionDescriptor.of(C_BOOL, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER));
      QUICHE_CONN_LOCAL_ERROR = downcall("quiche_conn_local_error", FunctionDescriptor.of(C_BOOL, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER));
      QUICHE_CONN_STATS = downcall("quiche_conn_stats", FunctionDescriptor.ofVoid(C_POINTER, C_POINTER));
      QUICHE_CONN_PEER_TRANSPORT_PARAMS = downcall("quiche_conn_peer_transport_params", FunctionDescriptor.of(C_BOOL, C_POINTER, C_POINTER));
      QUICHE_CONN_PATH_STATS = downcall("quiche_conn_path_stats", FunctionDescriptor.of(C_INT, C_POINTER, C_LONG, C_POINTER));
      QUICHE_CONN_SOURCE_IDS = downcall("quiche_conn_source_ids", FunctionDescriptor.of(C_POINTER, C_POINTER));
      QUICHE_CONNECTION_ID_ITER_NEXT = downcall("quiche_connection_id_iter_next", FunctionDescriptor.of(C_BOOL, C_POINTER, C_POINTER, C_POINTER));
      QUICHE_CONNECTION_ID_ITER_FREE = downcall("quiche_connection_id_iter_free", FunctionDescriptor.ofVoid(C_POINTER));
      QUICHE_CONN_RETIRED_SCID_NEXT = downcall("quiche_conn_retired_scid_next", FunctionDescriptor.of(C_BOOL, C_POINTER, C_POINTER, C_POINTER));
      QUICHE_CONN_RETIRED_SCIDS = downcall("quiche_conn_retired_scids", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_AVAILABLE_DCIDS = downcall("quiche_conn_available_dcids", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_SCIDS_LEFT = downcall("quiche_conn_scids_left", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_ACTIVE_SCIDS = downcall("quiche_conn_active_scids", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_NEW_SCID = downcall("quiche_conn_new_scid", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_LONG, C_POINTER, C_BOOL, C_POINTER));
      QUICHE_CONN_RETIRE_DCID = downcall("quiche_conn_retire_dcid", FunctionDescriptor.of(C_INT, C_POINTER, C_LONG));
      QUICHE_CONN_PROBE_PATH = downcall("quiche_conn_probe_path", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_INT, C_POINTER, C_INT, C_POINTER));
      QUICHE_CONN_MIGRATE_SOURCE = downcall("quiche_conn_migrate_source", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_INT, C_POINTER));
      QUICHE_CONN_MIGRATE = downcall("quiche_conn_migrate", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_INT, C_POINTER, C_INT, C_POINTER));
      QUICHE_CONN_PATH_EVENT_NEXT = downcall("quiche_conn_path_event_next", FunctionDescriptor.of(C_POINTER, C_POINTER));
      QUICHE_PATH_EVENT_TYPE = downcall("quiche_path_event_type", FunctionDescriptor.of(C_INT, C_POINTER));
      QUICHE_PATH_EVENT_NEW = downcall("quiche_path_event_new", FunctionDescriptor.ofVoid(C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER));
      QUICHE_PATH_EVENT_VALIDATED = downcall("quiche_path_event_validated", FunctionDescriptor.ofVoid(C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER));
      QUICHE_PATH_EVENT_FAILED_VALIDATION = downcall(
         "quiche_path_event_failed_validation", FunctionDescriptor.ofVoid(C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER)
      );
      QUICHE_PATH_EVENT_CLOSED = downcall("quiche_path_event_closed", FunctionDescriptor.ofVoid(C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER));
      QUICHE_PATH_EVENT_REUSED_SOURCE_CONNECTION_ID = downcall(
         "quiche_path_event_reused_source_connection_id",
         FunctionDescriptor.ofVoid(C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER)
      );
      QUICHE_PATH_EVENT_PEER_MIGRATED = downcall(
         "quiche_path_event_peer_migrated", FunctionDescriptor.ofVoid(C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER)
      );
      QUICHE_PATH_EVENT_FREE = downcall("quiche_path_event_free", FunctionDescriptor.ofVoid(C_POINTER));
      QUICHE_CONN_PATHS_ITER = downcall("quiche_conn_paths_iter", FunctionDescriptor.of(C_POINTER, C_POINTER, C_POINTER, C_LONG));
      QUICHE_SOCKET_ADDR_ITER_NEXT = downcall("quiche_socket_addr_iter_next", FunctionDescriptor.of(C_BOOL, C_POINTER, C_POINTER, C_POINTER));
      QUICHE_SOCKET_ADDR_ITER_FREE = downcall("quiche_socket_addr_iter_free", FunctionDescriptor.ofVoid(C_POINTER));
      QUICHE_CONN_IS_PATH_VALIDATED = downcall("quiche_conn_is_path_validated", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_LONG, C_POINTER, C_LONG));
      QUICHE_CONN_DGRAM_MAX_WRITABLE_LEN = downcall("quiche_conn_dgram_max_writable_len", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_DGRAM_RECV_FRONT_LEN = downcall("quiche_conn_dgram_recv_front_len", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_DGRAM_RECV_QUEUE_LEN = downcall("quiche_conn_dgram_recv_queue_len", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_DGRAM_RECV_QUEUE_BYTE_SIZE = downcall("quiche_conn_dgram_recv_queue_byte_size", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_DGRAM_SEND_QUEUE_LEN = downcall("quiche_conn_dgram_send_queue_len", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_DGRAM_SEND_QUEUE_BYTE_SIZE = downcall("quiche_conn_dgram_send_queue_byte_size", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_DGRAM_RECV = downcall("quiche_conn_dgram_recv", FunctionDescriptor.of(C_LONG, C_POINTER, C_POINTER, C_LONG));
      QUICHE_CONN_DGRAM_SEND = downcall("quiche_conn_dgram_send", FunctionDescriptor.of(C_LONG, C_POINTER, C_POINTER, C_LONG));
      QUICHE_CONN_DGRAM_PURGE_OUTGOING = downcall("quiche_conn_dgram_purge_outgoing", FunctionDescriptor.ofVoid(C_POINTER, C_POINTER));
      QUICHE_CONN_IS_DGRAM_SEND_QUEUE_FULL = downcall("quiche_conn_is_dgram_send_queue_full", FunctionDescriptor.of(C_BOOL, C_POINTER));
      QUICHE_CONN_IS_DGRAM_RECV_QUEUE_FULL = downcall("quiche_conn_is_dgram_recv_queue_full", FunctionDescriptor.of(C_BOOL, C_POINTER));
      QUICHE_CONN_SEND_ACK_ELICITING = downcall("quiche_conn_send_ack_eliciting", FunctionDescriptor.of(C_LONG, C_POINTER));
      QUICHE_CONN_SEND_ACK_ELICITING_ON_PATH = downcall(
         "quiche_conn_send_ack_eliciting_on_path", FunctionDescriptor.of(C_LONG, C_POINTER, C_POINTER, C_INT, C_POINTER, C_INT)
      );
      QUICHE_H3_CONFIG_NEW = downcall("quiche_h3_config_new", FunctionDescriptor.of(C_POINTER));
      QUICHE_H3_CONFIG_SET_MAX_FIELD_SECTION_SIZE = downcall("quiche_h3_config_set_max_field_section_size", FunctionDescriptor.ofVoid(C_POINTER, C_LONG));
      QUICHE_H3_CONFIG_SET_QPACK_MAX_TABLE_CAPACITY = downcall("quiche_h3_config_set_qpack_max_table_capacity", FunctionDescriptor.ofVoid(C_POINTER, C_LONG));
      QUICHE_H3_CONFIG_SET_QPACK_BLOCKED_STREAMS = downcall("quiche_h3_config_set_qpack_blocked_streams", FunctionDescriptor.ofVoid(C_POINTER, C_LONG));
      QUICHE_H3_CONFIG_ENABLE_EXTENDED_CONNECT = downcall("quiche_h3_config_enable_extended_connect", FunctionDescriptor.ofVoid(C_POINTER, C_BOOL));
      QUICHE_H3_CONFIG_FREE = downcall("quiche_h3_config_free", FunctionDescriptor.ofVoid(C_POINTER));
      QUICHE_H3_CONN_NEW_WITH_TRANSPORT = downcall("quiche_h3_conn_new_with_transport", FunctionDescriptor.of(C_POINTER, C_POINTER, C_POINTER));
      QUICHE_H3_CONN_POLL = downcall("quiche_h3_conn_poll", FunctionDescriptor.of(C_LONG, C_POINTER, C_POINTER, C_POINTER));
      QUICHE_H3_EVENT_TYPE = downcall("quiche_h3_event_type", FunctionDescriptor.of(C_INT, C_POINTER));
      QUICHE_H3_EVENT_FOR_EACH_HEADER = downcall("quiche_h3_event_for_each_header", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_POINTER));
      QUICHE_H3_FOR_EACH_SETTING = downcall("quiche_h3_for_each_setting", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_POINTER));
      QUICHE_H3_EVENT_HEADERS_HAS_MORE_FRAMES = downcall("quiche_h3_event_headers_has_more_frames", FunctionDescriptor.of(C_BOOL, C_POINTER));
      QUICHE_H3_EXTENDED_CONNECT_ENABLED_BY_PEER = downcall("quiche_h3_extended_connect_enabled_by_peer", FunctionDescriptor.of(C_BOOL, C_POINTER));
      QUICHE_H3_EVENT_FREE = downcall("quiche_h3_event_free", FunctionDescriptor.ofVoid(C_POINTER));
      QUICHE_H3_SEND_REQUEST = downcall("quiche_h3_send_request", FunctionDescriptor.of(C_LONG, C_POINTER, C_POINTER, C_POINTER, C_LONG, C_BOOL));
      QUICHE_H3_SEND_RESPONSE = downcall("quiche_h3_send_response", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_LONG, C_POINTER, C_LONG, C_BOOL));
      QUICHE_H3_SEND_RESPONSE_WITH_PRIORITY = downcall(
         "quiche_h3_send_response_with_priority", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_LONG, C_POINTER, C_LONG, C_POINTER, C_BOOL)
      );
      QUICHE_H3_SEND_ADDITIONAL_HEADERS = downcall(
         "quiche_h3_send_additional_headers", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_LONG, C_POINTER, C_LONG, C_BOOL, C_BOOL)
      );
      QUICHE_H3_SEND_BODY = downcall("quiche_h3_send_body", FunctionDescriptor.of(C_LONG, C_POINTER, C_POINTER, C_LONG, C_POINTER, C_LONG, C_BOOL));
      QUICHE_H3_RECV_BODY = downcall("quiche_h3_recv_body", FunctionDescriptor.of(C_LONG, C_POINTER, C_POINTER, C_LONG, C_POINTER, C_LONG));
      QUICHE_H3_SEND_GOAWAY = downcall("quiche_h3_send_goaway", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_LONG));
      QUICHE_H3_PARSE_EXTENSIBLE_PRIORITY = downcall("quiche_h3_parse_extensible_priority", FunctionDescriptor.of(C_INT, C_POINTER, C_LONG, C_POINTER));
      QUICHE_H3_SEND_PRIORITY_UPDATE_FOR_REQUEST = downcall(
         "quiche_h3_send_priority_update_for_request", FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_LONG, C_POINTER)
      );
      QUICHE_H3_TAKE_LAST_PRIORITY_UPDATE = downcall(
         "quiche_h3_take_last_priority_update", FunctionDescriptor.of(C_INT, C_POINTER, C_LONG, C_POINTER, C_POINTER)
      );
      QUICHE_H3_DGRAM_ENABLED_BY_PEER = downcall("quiche_h3_dgram_enabled_by_peer", FunctionDescriptor.of(C_BOOL, C_POINTER, C_POINTER));
      QUICHE_H3_CONN_STATS = downcall("quiche_h3_conn_stats", FunctionDescriptor.ofVoid(C_POINTER, C_POINTER));
      QUICHE_H3_CONN_FREE = downcall("quiche_h3_conn_free", FunctionDescriptor.ofVoid(C_POINTER));
   }

   public static enum Platform {
      LINUX,
      MACOS,
      WINDOWS;
   }

   public static enum QuicheError {
      DONE(-1),
      BUFFER_TOO_SHORT(-2),
      UNKNOWN_VERSION(-3),
      INVALID_FRAME(-4),
      INVALID_PACKET(-5),
      INVALID_STATE(-6),
      INVALID_STREAM_STATE(-7),
      INVALID_TRANSPORT_PARAM(-8),
      CRYPTO_FAIL(-9),
      TLS_FAIL(-10),
      FLOW_CONTROL(-11),
      STREAM_LIMIT(-12),
      FINAL_SIZE(-13),
      CONGESTION_CONTROL(-14),
      STREAM_STOPPED(-15),
      STREAM_RESET(-16),
      ID_LIMIT(-17),
      OUT_OF_IDENTIFIERS(-18),
      KEY_UPDATE(-19),
      CRYPTO_BUFFER_EXCEEDED(-20),
      INVALID_ACK_RANGE(-21),
      OPTIMISTIC_ACK_DETECTED(-22);

      private static final QuicheNative.QuicheError[] BY_CODE;
      private final int code;

      private QuicheError(int code) {
         this.code = code;
      }

      public int code() {
         return this.code;
      }

      public static QuicheNative.QuicheError fromCode(int code) {
         int idx = -code - 1;
         return idx >= 0 && idx < BY_CODE.length ? BY_CODE[idx] : null;
      }

      static {
         QuicheNative.QuicheError[] values = values();
         BY_CODE = new QuicheNative.QuicheError[values.length];

         for (QuicheNative.QuicheError value : values) {
            BY_CODE[-value.code - 1] = value;
         }
      }
   }
}
