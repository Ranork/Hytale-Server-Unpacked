package com.hypixel.hytale.server.core.asset.type.musiccontainer.config;

import com.hypixel.hytale.assetstore.map.IndexedAssetMap;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.server.core.asset.type.audiostate.config.AudioStateResolver;
import java.util.HashSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SegmentMusicContainer extends MusicContainer {
   @Nonnull
   public static final BuilderCodec<SegmentMusicContainer> CODEC = BuilderCodec.builder(
         SegmentMusicContainer.class, SegmentMusicContainer::new, MusicContainer.ABSTRACT_CODEC
      )
      .documentation("A music container with parallel layers on a shared timeline. EntryMarker is the downbeat and ExitMarker is the loop/transition point.")
      .<LayerPlacement[]>appendInherited(
         new KeyedCodec<>("Layers", new ArrayCodec<>(LayerPlacement.CODEC, LayerPlacement[]::new)),
         (mc, v) -> mc.layers = v,
         mc -> mc.layers,
         (mc, parent) -> mc.layers = parent.layers
      )
      .addValidator(Validators.nonEmptyArray())
      .add()
      .<BarBeatDuration>appendInherited(
         new KeyedCodec<>("EntryMarker", BarBeatDuration.CODEC),
         (mc, v) -> mc.entryMarker = v,
         mc -> mc.entryMarker,
         (mc, parent) -> mc.entryMarker = parent.entryMarker
      )
      .documentation(
         "Position of the downbeat on the timeline from segment-time 0. All layers that start at segment-time 0 (no ClipStart) get this much pre-entry material before the downbeat."
      )
      .add()
      .<BarBeatDuration>appendInherited(
         new KeyedCodec<>("ExitMarker", BarBeatDuration.CODEC, true),
         (mc, v) -> mc.exitMarker = v,
         mc -> mc.exitMarker,
         (mc, parent) -> mc.exitMarker = parent.exitMarker
      )
      .documentation(
         "Exit position on the timeline from segment-time 0. The active region is [EntryMarker, ExitMarker]. For looping Segments, this is the loop point. For play-once Segments in a Sequence, this is the transition trigger to the next child."
      )
      .add()
      .validator(
         (segment, results) -> {
            float bpm = segment.tempo != null ? segment.tempo.bpm : 0.0F;
            int bpb = segment.tempo != null ? segment.tempo.beatsPerBar : 4;
            boolean usesBarBeat = segment.entryMarker != null && (segment.entryMarker.bars != 0 || segment.entryMarker.beats != 0);
            if (segment.exitMarker != null && (segment.exitMarker.bars != 0 || segment.exitMarker.beats != 0)) {
               usesBarBeat = true;
            }

            if (segment.layers != null) {
               for (LayerPlacement layer : segment.layers) {
                  if (layer.clipStart != null && (layer.clipStart.bars != 0 || layer.clipStart.beats != 0)) {
                     usesBarBeat = true;
                     break;
                  }
               }
            }

            if (usesBarBeat && segment.tempo == null) {
               results.fail("Segment requires a Tempo when EntryMarker, ExitMarker, or ClipStart use bar/beat components");
            } else {
               float entryMs = segment.entryMarker != null ? segment.entryMarker.toMs(bpm, bpb) : 0.0F;
               float exitMs = segment.exitMarker != null ? segment.exitMarker.toMs(bpm, bpb) : 0.0F;
               if (entryMs < 0.0F) {
                  results.fail("EntryMarker resolves to " + entryMs + " ms; must be non-negative");
               }

               if (segment.exitMarker != null) {
                  if (exitMs <= 0.0F) {
                     results.fail("ExitMarker resolves to " + exitMs + " ms; must be positive");
                  } else if (exitMs <= entryMs) {
                     results.fail("ExitMarker (" + exitMs + " ms) must be after EntryMarker (" + entryMs + " ms)");
                  }
               }

               HashSet<String> layerNames = new HashSet<>();
               if (segment.layers != null) {
                  for (LayerPlacement layerx : segment.layers) {
                     if (layerx.name != null && !layerx.name.isBlank()) {
                        if (!layerNames.add(layerx.name)) {
                           results.fail("Duplicate layer name '" + layerx.name + "' in Layers array");
                        }

                        if (layerx.clipStart != null) {
                           float clipStartMs = layerx.clipStart.toMs(bpm, bpb);
                           if (clipStartMs > 0.0F) {
                              results.fail(
                                 "Layer '"
                                    + layerx.name
                                    + "' ClipStart resolves to "
                                    + clipStartMs
                                    + " ms (positive). Layers must start at or before segment-time 0"
                              );
                           }
                        }

                        AudioStateResolver.validateBindings(layerx.stateBindings, "Segment '" + segment.id + "' Layer '" + layerx.name + "'", results);
                     } else {
                        results.fail("Every layer must have a Name");
                     }
                  }
               }
            }
         }
      )
      .afterDecode(segment -> {
         if (segment.layers != null) {
            for (LayerPlacement layer : segment.layers) {
               AudioStateResolver.resolveBindings(layer.stateBindings);
            }
         }
      })
      .build();
   @Nullable
   protected LayerPlacement[] layers;
   @Nullable
   protected BarBeatDuration entryMarker;
   @Nullable
   protected BarBeatDuration exitMarker;

   @Nullable
   public LayerPlacement[] getLayers() {
      return this.layers;
   }

   protected SegmentMusicContainer() {
   }

   public SegmentMusicContainer(@Nonnull String id) {
      super(id);
   }

   @Nonnull
   @Override
   public String[] getChildIds() {
      if (this.layers != null && this.layers.length != 0) {
         String[] ids = new String[this.layers.length];

         for (int i = 0; i < this.layers.length; i++) {
            ids[i] = this.layers[i].container;
         }

         return ids;
      } else {
         return ArrayUtil.EMPTY_STRING_ARRAY;
      }
   }

   @Override
   public void refreshAudioStateResolution() {
      super.refreshAudioStateResolution();
      if (this.layers != null) {
         for (LayerPlacement layer : this.layers) {
            if (layer != null) {
               AudioStateResolver.resolveBindings(layer.stateBindings);
            }
         }
      }
   }

   @Nonnull
   public com.hypixel.hytale.protocol.MusicContainer toPacket() {
      com.hypixel.hytale.protocol.SegmentMusicContainer packet = new com.hypixel.hytale.protocol.SegmentMusicContainer();
      this.fillBasePacketFields(packet);
      packet.entryMarker = this.entryMarker != null ? this.entryMarker.toProtocol() : null;
      packet.exitMarker = this.exitMarker != null ? this.exitMarker.toProtocol() : null;
      if (this.layers != null && this.layers.length > 0) {
         IndexedAssetMap<String, MusicContainer> assetMap = MusicContainer.getAssetMap();
         packet.layers = new com.hypixel.hytale.protocol.LayerPlacement[this.layers.length];

         for (int i = 0; i < this.layers.length; i++) {
            LayerPlacement src = this.layers[i];
            com.hypixel.hytale.protocol.LayerPlacement dst = new com.hypixel.hytale.protocol.LayerPlacement();
            dst.containerIndex = src.container != null ? assetMap.getIndex(src.container) : 0;
            dst.name = src.name;
            dst.clipStart = src.clipStart != null ? src.clipStart.toProtocol() : null;
            dst.stateBindings = AudioStateResolver.toPacketArray(src.stateBindings);
            packet.layers[i] = dst;
         }
      }

      return packet;
   }
}
