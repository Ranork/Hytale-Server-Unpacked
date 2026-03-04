package com.hypixel.hytale.server.core.universe.world.connectedblocks;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class ConnectedBlockFaceTags {
   public static final BuilderCodec<ConnectedBlockFaceTags> CODEC = BuilderCodec.builder(ConnectedBlockFaceTags.class, ConnectedBlockFaceTags::new)
      .append(new KeyedCodec<>("North", new ArrayCodec<>(Codec.STRING, String[]::new), false), (o, tags) -> {
         HashSet<String> strings = new HashSet<>(tags.length);
         strings.addAll(Arrays.asList(tags));
         o.blockFaceTags.put(Vector3i.NORTH, strings);
      }, o -> o.blockFaceTags.containsKey(Vector3i.NORTH) ? o.blockFaceTags.get(Vector3i.NORTH).toArray(String[]::new) : new String[0])
      .add()
      .append(new KeyedCodec<>("East", new ArrayCodec<>(Codec.STRING, String[]::new), false), (o, tags) -> {
         HashSet<String> strings = new HashSet<>(tags.length);
         strings.addAll(Arrays.asList(tags));
         o.blockFaceTags.put(Vector3i.EAST, strings);
      }, o -> o.blockFaceTags.containsKey(Vector3i.EAST) ? o.blockFaceTags.get(Vector3i.EAST).toArray(String[]::new) : new String[0])
      .add()
      .append(new KeyedCodec<>("South", new ArrayCodec<>(Codec.STRING, String[]::new), false), (o, tags) -> {
         HashSet<String> strings = new HashSet<>(tags.length);
         strings.addAll(Arrays.asList(tags));
         o.blockFaceTags.put(Vector3i.SOUTH, strings);
      }, o -> o.blockFaceTags.containsKey(Vector3i.SOUTH) ? o.blockFaceTags.get(Vector3i.SOUTH).toArray(String[]::new) : new String[0])
      .add()
      .append(new KeyedCodec<>("West", new ArrayCodec<>(Codec.STRING, String[]::new), false), (o, tags) -> {
         HashSet<String> strings = new HashSet<>(tags.length);
         strings.addAll(Arrays.asList(tags));
         o.blockFaceTags.put(Vector3i.WEST, strings);
      }, o -> o.blockFaceTags.containsKey(Vector3i.WEST) ? o.blockFaceTags.get(Vector3i.WEST).toArray(String[]::new) : new String[0])
      .add()
      .append(new KeyedCodec<>("Up", new ArrayCodec<>(Codec.STRING, String[]::new), false), (o, tags) -> {
         HashSet<String> strings = new HashSet<>(tags.length);
         strings.addAll(Arrays.asList(tags));
         o.blockFaceTags.put(Vector3i.UP, strings);
      }, o -> o.blockFaceTags.containsKey(Vector3i.UP) ? o.blockFaceTags.get(Vector3i.UP).toArray(String[]::new) : new String[0])
      .add()
      .append(new KeyedCodec<>("Down", new ArrayCodec<>(Codec.STRING, String[]::new), false), (o, tags) -> {
         HashSet<String> strings = new HashSet<>(tags.length);
         strings.addAll(Arrays.asList(tags));
         o.blockFaceTags.put(Vector3i.DOWN, strings);
      }, o -> o.blockFaceTags.containsKey(Vector3i.DOWN) ? o.blockFaceTags.get(Vector3i.DOWN).toArray(String[]::new) : new String[0])
      .add()
      .build();
   public static final ConnectedBlockFaceTags EMPTY = new ConnectedBlockFaceTags();
   @Nonnull
   private final Map<Vector3i, HashSet<String>> blockFaceTags = new Object2ObjectOpenHashMap();

   public boolean contains(Vector3i direction, String blockFaceTag) {
      return this.blockFaceTags.containsKey(direction) && this.blockFaceTags.get(direction).contains(blockFaceTag);
   }

   @Nonnull
   public Map<Vector3i, HashSet<String>> getBlockFaceTags() {
      return this.blockFaceTags;
   }

   public Set<String> getBlockFaceTags(Vector3i direction) {
      return (Set<String>)(this.blockFaceTags.containsKey(direction) ? this.blockFaceTags.get(direction) : Collections.emptySet());
   }

   @Nonnull
   public Set<Vector3i> getDirections() {
      return this.blockFaceTags.keySet();
   }
}
