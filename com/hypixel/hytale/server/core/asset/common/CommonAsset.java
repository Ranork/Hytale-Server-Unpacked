package com.hypixel.hytale.server.core.asset.common;

import com.hypixel.hytale.common.util.PatternUtil;
import com.hypixel.hytale.protocol.Asset;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.util.HashUtil;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class CommonAsset implements NetworkSerializable<Asset> {
   public static final int HASH_LENGTH = 64;
   public static final Pattern HASH_PATTERN = Pattern.compile("^[A-Fa-f0-9]{64}$");
   @Nonnull
   private final String name;
   @Nonnull
   private final String hash;
   protected transient WeakReference<CompletableFuture<byte[]>> blob;
   protected transient SoftReference<Asset> cachedPacket;

   public CommonAsset(@Nonnull String name, @Nullable byte[] bytes) {
      this.name = PatternUtil.replaceBackslashWithForwardSlash(name);
      this.hash = hash(bytes);
      this.blob = new WeakReference<>(bytes != null ? CompletableFuture.completedFuture(bytes) : null);
   }

   public CommonAsset(@Nonnull String name, @Nonnull String hash, @Nullable byte[] bytes) {
      this.name = PatternUtil.replaceBackslashWithForwardSlash(name);
      this.hash = hash.toLowerCase();
      this.blob = new WeakReference<>(bytes != null ? CompletableFuture.completedFuture(bytes) : null);
   }

   @Nonnull
   public String getName() {
      return this.name;
   }

   @Nonnull
   public String getHash() {
      return this.hash;
   }

   public CompletableFuture<byte[]> getBlob() {
      CompletableFuture<byte[]> future = this.blob.get();
      if (future == null) {
         future = this.getBlob0();
         this.blob = new WeakReference<>(future);
      }

      return future;
   }

   protected abstract CompletableFuture<byte[]> getBlob0();

   @Nonnull
   public Asset toPacket() {
      Asset cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         Asset packet = new Asset(this.hash, this.name);
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CommonAsset asset = (CommonAsset)o;
         return !this.name.equals(asset.name) ? false : this.hash.equals(asset.hash);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.name.hashCode();
      return 31 * result + this.hash.hashCode();
   }

   @Nonnull
   @Override
   public String toString() {
      return "CommonAsset{name='" + this.name + "', hash='" + this.hash + "'}";
   }

   @Nonnull
   public static String hash(byte[] bytes) {
      return HashUtil.sha256(bytes);
   }
}
