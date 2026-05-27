package com.hypixel.hytale.protocol.io;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.ValueLayout.OfInt;
import java.lang.foreign.ValueLayout.OfLong;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ZstdNative {
   private static final Linker LINKER = Linker.nativeLinker();
   private static final Arena GLOBAL_ARENA = Arena.global();
   private static final SymbolLookup LOOKUP;
   private static final OfLong C_SIZE_T;
   private static final OfInt C_INT = ValueLayout.JAVA_INT;
   private static final OfLong C_UNSIGNED_LONG_LONG = ValueLayout.JAVA_LONG;
   private static final AddressLayout C_POINTER = ValueLayout.ADDRESS;
   private static final MethodHandle ZSTD_COMPRESS;
   private static final MethodHandle ZSTD_DECOMPRESS;
   private static final MethodHandle ZSTD_COMPRESS_BOUND;
   private static final MethodHandle ZSTD_GET_FRAME_CONTENT_SIZE;
   private static final MethodHandle ZSTD_IS_ERROR;
   private static final MethodHandle ZSTD_GET_ERROR_NAME;
   private static final MethodHandle ZSTD_CREATE_CCTX;
   private static final MethodHandle ZSTD_FREE_CCTX;
   private static final MethodHandle ZSTD_COMPRESS_CCTX;
   private static final MethodHandle ZSTD_CREATE_DCTX;
   private static final MethodHandle ZSTD_FREE_DCTX;
   private static final MethodHandle ZSTD_DECOMPRESS_DCTX;
   private static final MethodHandle ZSTD_DEFAULT_C_LEVEL;
   public static final long CONTENT_SIZE_UNKNOWN = -1L;
   public static final long CONTENT_SIZE_ERROR = -2L;

   private ZstdNative() {
   }

   private static MethodHandle downcall(String name, FunctionDescriptor descriptor) {
      return LINKER.downcallHandle(LOOKUP.find(name).orElseThrow(() -> new UnsatisfiedLinkError("Missing symbol: " + name)), descriptor);
   }

   public static long compress(MemorySegment dst, long dstCapacity, MemorySegment src, long srcSize, int compressionLevel) {
      try {
         return (long)ZSTD_COMPRESS.invokeExact((MemorySegment)dst, (long)dstCapacity, (MemorySegment)src, (long)srcSize, (int)compressionLevel);
      } catch (Throwable var8) {
         throw sneaky(var8);
      }
   }

   public static long decompress(MemorySegment dst, long dstCapacity, MemorySegment src, long compressedSize) {
      try {
         return (long)ZSTD_DECOMPRESS.invokeExact((MemorySegment)dst, (long)dstCapacity, (MemorySegment)src, (long)compressedSize);
      } catch (Throwable var7) {
         throw sneaky(var7);
      }
   }

   public static long compressBound(long srcSize) {
      try {
         return (long)ZSTD_COMPRESS_BOUND.invokeExact((long)srcSize);
      } catch (Throwable var3) {
         throw sneaky(var3);
      }
   }

   public static long getFrameContentSize(MemorySegment src, long srcSize) {
      try {
         return (long)ZSTD_GET_FRAME_CONTENT_SIZE.invokeExact((MemorySegment)src, (long)srcSize);
      } catch (Throwable var4) {
         throw sneaky(var4);
      }
   }

   public static boolean isError(long code) {
      try {
         return (int)ZSTD_IS_ERROR.invokeExact((long)code) != 0;
      } catch (Throwable var3) {
         throw sneaky(var3);
      }
   }

   public static String getErrorName(long code) {
      try {
         MemorySegment ptr = (MemorySegment)ZSTD_GET_ERROR_NAME.invokeExact((long)code);
         return ptr.reinterpret(256L).getString(0L);
      } catch (Throwable var3) {
         throw sneaky(var3);
      }
   }

   public static int defaultCompressionLevel() {
      try {
         return (int)ZSTD_DEFAULT_C_LEVEL.invokeExact();
      } catch (Throwable var1) {
         throw sneaky(var1);
      }
   }

   public static MemorySegment createCCtx() {
      try {
         return (MemorySegment)ZSTD_CREATE_CCTX.invokeExact();
      } catch (Throwable var1) {
         throw sneaky(var1);
      }
   }

   public static long freeCCtx(MemorySegment cctx) {
      try {
         return (long)ZSTD_FREE_CCTX.invokeExact((MemorySegment)cctx);
      } catch (Throwable var2) {
         throw sneaky(var2);
      }
   }

   public static long compressCCtx(MemorySegment cctx, MemorySegment dst, long dstCapacity, MemorySegment src, long srcSize, int compressionLevel) {
      try {
         return (long)ZSTD_COMPRESS_CCTX.invokeExact(
            (MemorySegment)cctx, (MemorySegment)dst, (long)dstCapacity, (MemorySegment)src, (long)srcSize, (int)compressionLevel
         );
      } catch (Throwable var9) {
         throw sneaky(var9);
      }
   }

   public static MemorySegment createDCtx() {
      try {
         return (MemorySegment)ZSTD_CREATE_DCTX.invokeExact();
      } catch (Throwable var1) {
         throw sneaky(var1);
      }
   }

   public static long freeDCtx(MemorySegment dctx) {
      try {
         return (long)ZSTD_FREE_DCTX.invokeExact((MemorySegment)dctx);
      } catch (Throwable var2) {
         throw sneaky(var2);
      }
   }

   public static long decompressDCtx(MemorySegment dctx, MemorySegment dst, long dstCapacity, MemorySegment src, long compressedSize) {
      try {
         return (long)ZSTD_DECOMPRESS_DCTX.invokeExact((MemorySegment)dctx, (MemorySegment)dst, (long)dstCapacity, (MemorySegment)src, (long)compressedSize);
      } catch (Throwable var8) {
         throw sneaky(var8);
      }
   }

   private static <T extends Throwable> RuntimeException sneaky(Throwable t) throws T {
      throw t;
   }

   static {
      MemoryLayout canonicalSizeT = LINKER.canonicalLayouts().get("size_t");
      if (!(canonicalSizeT instanceof OfLong sizeT)) {
         throw new UnsupportedOperationException("ZstdNative requires a 64-bit platform (size_t is " + canonicalSizeT + ")");
      } else {
         C_SIZE_T = sizeT;
         String osName = System.getProperty("os.name").toLowerCase();
         String arch = System.getProperty("os.arch").toLowerCase();
         String resourcePath;
         if (osName.contains("linux")) {
            if (!"amd64".equals(arch) && !"x86_64".equals(arch)) {
               throw new UnsupportedOperationException("Unsupported Linux architecture: " + arch);
            }

            resourcePath = "/native/linux-x64/libzstd.so";
         } else if (!osName.contains("mac") && !osName.contains("darwin")) {
            if (!osName.contains("win")) {
               throw new UnsupportedOperationException("Unsupported OS: " + osName);
            }

            if (!"amd64".equals(arch) && !"x86_64".equals(arch)) {
               throw new UnsupportedOperationException("Unsupported Windows architecture: " + arch);
            }

            resourcePath = "/native/win-x64/zstd.dll";
         } else {
            if (!"aarch64".equals(arch)) {
               throw new UnsupportedOperationException("Unsupported macOS architecture: " + arch);
            }

            resourcePath = "/native/osx-arm64/libzstd.dylib";
         }

         try (InputStream stream = ZstdNative.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
               throw new IllegalStateException("Native library not found on classpath: " + resourcePath);
            }

            Path tmpDir = Files.createTempDirectory("zstd-native");
            Path libPath = tmpDir.resolve(Path.of(resourcePath).getFileName());
            Files.copy(stream, libPath, StandardCopyOption.REPLACE_EXISTING);
            libPath.toFile().deleteOnExit();
            tmpDir.toFile().deleteOnExit();
            LOOKUP = SymbolLookup.libraryLookup(libPath, GLOBAL_ARENA);
         } catch (IOException var10) {
            throw new ExceptionInInitializerError(var10);
         }

         ZSTD_COMPRESS = downcall("ZSTD_compress", FunctionDescriptor.of(C_SIZE_T, C_POINTER, C_SIZE_T, C_POINTER, C_SIZE_T, C_INT));
         ZSTD_DECOMPRESS = downcall("ZSTD_decompress", FunctionDescriptor.of(C_SIZE_T, C_POINTER, C_SIZE_T, C_POINTER, C_SIZE_T));
         ZSTD_COMPRESS_BOUND = downcall("ZSTD_compressBound", FunctionDescriptor.of(C_SIZE_T, C_SIZE_T));
         ZSTD_GET_FRAME_CONTENT_SIZE = downcall("ZSTD_getFrameContentSize", FunctionDescriptor.of(C_UNSIGNED_LONG_LONG, C_POINTER, C_SIZE_T));
         ZSTD_IS_ERROR = downcall("ZSTD_isError", FunctionDescriptor.of(C_INT, C_SIZE_T));
         ZSTD_GET_ERROR_NAME = downcall("ZSTD_getErrorName", FunctionDescriptor.of(C_POINTER, C_SIZE_T));
         ZSTD_CREATE_CCTX = downcall("ZSTD_createCCtx", FunctionDescriptor.of(C_POINTER));
         ZSTD_FREE_CCTX = downcall("ZSTD_freeCCtx", FunctionDescriptor.of(C_SIZE_T, C_POINTER));
         ZSTD_COMPRESS_CCTX = downcall("ZSTD_compressCCtx", FunctionDescriptor.of(C_SIZE_T, C_POINTER, C_POINTER, C_SIZE_T, C_POINTER, C_SIZE_T, C_INT));
         ZSTD_CREATE_DCTX = downcall("ZSTD_createDCtx", FunctionDescriptor.of(C_POINTER));
         ZSTD_FREE_DCTX = downcall("ZSTD_freeDCtx", FunctionDescriptor.of(C_SIZE_T, C_POINTER));
         ZSTD_DECOMPRESS_DCTX = downcall("ZSTD_decompressDCtx", FunctionDescriptor.of(C_SIZE_T, C_POINTER, C_POINTER, C_SIZE_T, C_POINTER, C_SIZE_T));
         ZSTD_DEFAULT_C_LEVEL = downcall("ZSTD_defaultCLevel", FunctionDescriptor.of(C_INT));
      }
   }
}
