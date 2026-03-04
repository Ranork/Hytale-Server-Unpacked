package com.hypixel.hytale.assetstore.event;

import com.hypixel.hytale.event.IEvent;
import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class AssetMonitorEvent<T> implements IEvent<T> {
   @Nonnull
   private final List<Path> createdOrModifiedFilesToLoad;
   @Nonnull
   private final List<Path> removedFilesToUnload;
   @Nonnull
   private final List<Path> createdOrModifiedDirectories;
   @Nonnull
   private final List<Path> removedFilesAndDirectories;
   @Nonnull
   private final String assetPack;

   public AssetMonitorEvent(
      @Nonnull String assetPack,
      @Nonnull List<Path> createdOrModified,
      @Nonnull List<Path> removed,
      @Nonnull List<Path> createdDirectories,
      @Nonnull List<Path> removedDirectories
   ) {
      this.assetPack = assetPack;
      this.createdOrModifiedFilesToLoad = createdOrModified;
      this.removedFilesToUnload = removed;
      this.createdOrModifiedDirectories = createdDirectories;
      this.removedFilesAndDirectories = removedDirectories;
   }

   @Nonnull
   public String getAssetPack() {
      return this.assetPack;
   }

   @Nonnull
   public List<Path> getCreatedOrModifiedFilesToLoad() {
      return this.createdOrModifiedFilesToLoad;
   }

   @Nonnull
   public List<Path> getRemovedFilesToUnload() {
      return this.removedFilesToUnload;
   }

   @Nonnull
   public List<Path> getRemovedFilesAndDirectories() {
      return this.removedFilesAndDirectories;
   }

   @Nonnull
   public List<Path> getCreatedOrModifiedDirectories() {
      return this.createdOrModifiedDirectories;
   }
}
