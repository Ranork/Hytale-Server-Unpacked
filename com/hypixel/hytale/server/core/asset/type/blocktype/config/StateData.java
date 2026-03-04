package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.lookup.Priority;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StateData {
   public static final String NULL_STATE_ID = "default";
   public static final BuilderCodec.Builder<StateData> DEFAULT_CODEC_BUILDER = BuilderCodec.builder(StateData.class, StateData::new)
      .appendInherited(new KeyedCodec<>("Id", Codec.STRING), (stateData, s) -> stateData.id = s, stateData -> stateData.id, (o, p) -> o.id = p.id)
      .add()
      .afterDecode((stateData, extraInfo) -> {
         if (stateData.stateToBlock != null) {
            Map<String, String> map = new Object2ObjectOpenHashMap();

            for (Entry<String, String> entry : stateData.stateToBlock.entrySet()) {
               map.put(entry.getValue(), entry.getKey());
            }

            stateData.blockToState = Collections.unmodifiableMap(map);
         }
      });
   public static final BuilderCodec<StateData> DEFAULT_CODEC = DEFAULT_CODEC_BUILDER.build();
   public static final CodecMapCodec<StateData> CODEC = new CodecMapCodec<StateData>(true)
      .register(Priority.DEFAULT, "StateData", StateData.class, DEFAULT_CODEC);
   private String id;
   private Map<String, String> stateToBlock;
   private Map<String, String> blockToState;

   protected StateData() {
   }

   public StateData(String id) {
      this.id = id;
   }

   @Nullable
   public String getId() {
      return this.id;
   }

   @Nullable
   public String getBlockForState(String state) {
      return this.stateToBlock == null ? null : this.stateToBlock.get(state);
   }

   @Nullable
   public String getStateForBlock(String blockTypeKey) {
      return this.blockToState == null ? null : this.blockToState.get(blockTypeKey);
   }

   @Nullable
   public Map<String, Integer> toPacket(@Nonnull BlockType current) {
      if (this.stateToBlock == null) {
         return null;
      } else {
         Map<String, Integer> data = new Object2IntOpenHashMap();

         for (String state : this.stateToBlock.keySet()) {
            String key = current.getBlockKeyForState(state);
            int index = BlockType.getAssetMap().getIndex(key);
            if (index != Integer.MIN_VALUE) {
               data.put(state, index);
            }
         }

         return data;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "StateData{id='" + this.id + "', stateToBlock='" + this.stateToBlock + "'}";
   }

   public void copyFrom(@Nullable StateData state) {
      if (state != null && this.stateToBlock == null) {
         this.stateToBlock = state.stateToBlock;
         this.blockToState = state.blockToState;
      }
   }

   static void addDefinitions() {
      DEFAULT_CODEC_BUILDER.addField(
         new KeyedCodec<>(
            "Definitions",
            new MapCodec(
               new ContainedAssetCodec<>(BlockType.class, BlockType.CODEC, ContainedAssetCodec.Mode.INJECT_PARENT, StateData::generateBlockKey), HashMap::new
            )
         ),
         (stateData, m) -> stateData.stateToBlock = m,
         stateData -> stateData.stateToBlock
      );
   }

   @Nonnull
   private static String generateBlockKey(@Nonnull AssetExtraInfo<String> extraInfo) {
      String key = extraInfo.getKey();
      return "*" + key + "_" + extraInfo.peekKey('_');
   }
}
