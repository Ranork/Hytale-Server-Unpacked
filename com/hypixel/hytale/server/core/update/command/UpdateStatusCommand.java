package com.hypixel.hytale.server.core.update.command;

import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.update.UpdateModule;
import com.hypixel.hytale.server.core.update.UpdateService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class UpdateStatusCommand extends CommandBase {
   public UpdateStatusCommand() {
      super("status", "server.commands.update.status.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      String currentVersion = ManifestUtil.getImplementationVersion();
      String patchline = UpdateService.getEffectivePatchline();
      String stagedVersion = UpdateService.getStagedVersion();
      UpdateModule updateModule = UpdateModule.get();
      Message msg = Message.translation("server.commands.update.status")
         .param("version", currentVersion != null ? currentVersion : "unknown")
         .param("patchline", patchline)
         .param("staged", stagedVersion != null ? stagedVersion : "none")
         .param("latest", this.getLatestStatus(currentVersion, updateModule))
         .param("downloading", this.getDownloadStatus(updateModule));
      context.sendMessage(msg);
   }

   private String getLatestStatus(String currentVersion, UpdateModule updateModule) {
      if (updateModule == null) {
         return "unknown";
      } else {
         UpdateService.VersionManifest latestKnown = updateModule.getLatestKnownVersion();
         if (latestKnown == null) {
            return "not checked";
         } else {
            return currentVersion != null && currentVersion.equals(latestKnown.version) ? "up to date" : latestKnown.version + " available";
         }
      }
   }

   private String getDownloadStatus(UpdateModule updateModule) {
      if (updateModule != null && updateModule.isDownloadInProgress()) {
         UpdateModule.DownloadProgress progress = updateModule.getDownloadProgress();
         if (progress == null) {
            return "starting...";
         } else {
            StringBuilder sb = new StringBuilder();
            sb.append(progress.percent()).append("% (");
            sb.append(FormatUtil.bytesToString(progress.downloadedBytes()));
            sb.append("/");
            sb.append(FormatUtil.bytesToString(progress.totalBytes()));
            sb.append(")");
            if (progress.etaSeconds() >= 0L) {
               sb.append(" ETA: ").append(FormatUtil.timeUnitToString(progress.etaSeconds(), TimeUnit.SECONDS));
            }

            return sb.toString();
         }
      } else {
         return "no";
      }
   }
}
