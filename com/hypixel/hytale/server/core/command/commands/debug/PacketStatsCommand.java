package com.hypixel.hytale.server.core.command.commands.debug;

import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.io.PacketStatsRecorder;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PacketStatsCommand extends AbstractTargetPlayerCommand {
   @Nonnull
   private final RequiredArg<String> packetArg = this.withRequiredArg("packet", "server.commands.packetStats.packet.desc", ArgTypes.STRING);

   public PacketStatsCommand() {
      super("packetstats", "server.commands.packetStats.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context,
      @Nullable Ref<EntityStore> sourceRef,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world,
      @Nonnull Store<EntityStore> store
   ) {
      String packetName = this.packetArg.get(context);
      PacketHandler packetHandler = playerRef.getPacketHandler();
      PacketStatsRecorder recorder = packetHandler.getPacketStatsRecorder();
      if (recorder == null) {
         context.sendMessage(Message.translation("server.commands.packetStats.notAvailable"));
      } else {
         PacketStatsRecorder.PacketStatsEntry entry = findEntry(recorder, packetName);
         if (entry == null) {
            context.sendMessage(Message.translation("server.commands.packetStats.notFound").param("name", packetName));
            context.sendMessage(
               Message.translation("server.general.failed.didYouMean")
                  .param("choices", StringUtil.sortByFuzzyDistance(packetName, getEntryNames(recorder), CommandUtil.RECOMMEND_COUNT).toString())
            );
         } else {
            Message sentTotal = Message.translation("server.commands.packetStats.row")
               .param("count", entry.getSentCount())
               .param(
                  "size",
                  FormatUtil.bytesToString(entry.getSentCompressedTotal())
                     + " ("
                     + FormatUtil.bytesToString(entry.getSentUncompressedTotal())
                     + " uncompressed)"
               )
               .param("avg", FormatUtil.bytesToString((long)entry.getSentCompressedAvg()))
               .param("min", FormatUtil.bytesToString(entry.getSentCompressedMin()))
               .param("max", FormatUtil.bytesToString(entry.getSentCompressedMax()));
            PacketStatsRecorder.RecentStats sentRecently = entry.getSentRecently();
            int sentRecentlyCount = sentRecently.count();
            Message sentRecent = Message.translation("server.commands.packetStats.row")
               .param("count", sentRecentlyCount)
               .param(
                  "size",
                  FormatUtil.bytesToString(sentRecently.compressedTotal())
                     + " ("
                     + FormatUtil.bytesToString(sentRecently.uncompressedTotal())
                     + " uncompressed)"
               )
               .param("avg", FormatUtil.bytesToString(sentRecentlyCount > 0 ? sentRecently.compressedTotal() / sentRecentlyCount : 0L))
               .param("min", FormatUtil.bytesToString(sentRecently.compressedMin()))
               .param("max", FormatUtil.bytesToString(sentRecently.compressedMax()));
            Message receivedTotal = Message.translation("server.commands.packetStats.row")
               .param("count", entry.getReceivedCount())
               .param(
                  "size",
                  FormatUtil.bytesToString(entry.getReceivedCompressedTotal())
                     + " ("
                     + FormatUtil.bytesToString(entry.getReceivedUncompressedTotal())
                     + " uncompressed)"
               )
               .param("avg", FormatUtil.bytesToString((long)entry.getReceivedCompressedAvg()))
               .param("min", FormatUtil.bytesToString(entry.getReceivedCompressedMin()))
               .param("max", FormatUtil.bytesToString(entry.getReceivedCompressedMax()));
            PacketStatsRecorder.RecentStats receivedRecently = entry.getReceivedRecently();
            int receivedRecentlyCount = receivedRecently.count();
            Message receivedRecent = Message.translation("server.commands.packetStats.row")
               .param("count", receivedRecentlyCount)
               .param(
                  "size",
                  FormatUtil.bytesToString(receivedRecently.compressedTotal())
                     + " ("
                     + FormatUtil.bytesToString(receivedRecently.uncompressedTotal())
                     + " uncompressed)"
               )
               .param("avg", FormatUtil.bytesToString(receivedRecentlyCount > 0 ? receivedRecently.compressedTotal() / receivedRecentlyCount : 0L))
               .param("min", FormatUtil.bytesToString(receivedRecently.compressedMin()))
               .param("max", FormatUtil.bytesToString(receivedRecently.compressedMax()));
            context.sendMessage(
               Message.translation("server.commands.packetStats.stats")
                  .param("name", entry.getName())
                  .param("id", entry.getPacketId())
                  .param("sentTotal", sentTotal)
                  .param("sentRecent", sentRecent)
                  .param("receivedTotal", receivedTotal)
                  .param("receivedRecent", receivedRecent)
            );
         }
      }
   }

   @Nullable
   private static PacketStatsRecorder.PacketStatsEntry findEntry(PacketStatsRecorder recorder, String name) {
      for (int i = 0; i < 512; i++) {
         PacketStatsRecorder.PacketStatsEntry entry = recorder.getEntry(i);
         if (entry.hasData()) {
            String entryName = entry.getName();
            if (entryName != null && name.equalsIgnoreCase(entryName)) {
               return entry;
            }
         }
      }

      return null;
   }

   private static List<String> getEntryNames(PacketStatsRecorder recorder) {
      ObjectArrayList<String> list = new ObjectArrayList();

      for (int i = 0; i < 512; i++) {
         PacketStatsRecorder.PacketStatsEntry entry = recorder.getEntry(i);
         if (entry.hasData()) {
            String name = entry.getName();
            if (name != null) {
               list.add(name);
            }
         }
      }

      return list;
   }
}
