package com.hypixel.hytale.server.core.asset.common.events;

import com.hypixel.hytale.assetstore.event.AssetMonitorEvent;
import java.nio.file.Path;
import java.util.List;

public class CommonAssetMonitorEvent extends AssetMonitorEvent<Void> {
   public CommonAssetMonitorEvent(
      String assetPack, List<Path> createdOrModified, List<Path> removed, List<Path> createdOrModifiedDirectories, List<Path> removedDirectories
   ) {
      super(assetPack, createdOrModified, removed, createdOrModifiedDirectories, removedDirectories);
   }
}
