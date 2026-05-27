package com.hypixel.hytale.server.core.universe.world.connectedblocks;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public class ConnectedBlockFaceTags {
   public static final BuilderCodec<ConnectedBlockFaceTags> CODEC = BuilderCodec.builder(ConnectedBlockFaceTags.class, ConnectedBlockFaceTags::new)
      .append(new KeyedCodec<>("North", new ArrayCodec<>(Codec.STRING, String[]::new), false), (o, tags) -> {
         HashSet<String> strings = new HashSet<>(tags.length);
         strings.addAll(Arrays.asList(tags));
         o.blockFaceTags.put(Vector3iUtil.NORTH, strings);
      }, o -> o.blockFaceTags.containsKey(Vector3iUtil.NORTH) ? o.blockFaceTags.get(Vector3iUtil.NORTH).toArray(String[]::new) : new String[0])
      .add()
      .append(new KeyedCodec<>("East", new ArrayCodec<>(Codec.STRING, String[]::new), false), (o, tags) -> {
         HashSet<String> strings = new HashSet<>(tags.length);
         strings.addAll(Arrays.asList(tags));
         o.blockFaceTags.put(Vector3iUtil.EAST, strings);
      }, o -> o.blockFaceTags.containsKey(Vector3iUtil.EAST) ? o.blockFaceTags.get(Vector3iUtil.EAST).toArray(String[]::new) : new String[0])
      .add()
      .append(new KeyedCodec<>("South", new ArrayCodec<>(Codec.STRING, String[]::new), false), (o, tags) -> {
         HashSet<String> strings = new HashSet<>(tags.length);
         strings.addAll(Arrays.asList(tags));
         o.blockFaceTags.put(Vector3iUtil.SOUTH, strings);
      }, o -> o.blockFaceTags.containsKey(Vector3iUtil.SOUTH) ? o.blockFaceTags.get(Vector3iUtil.SOUTH).toArray(String[]::new) : new String[0])
      .add()
      .append(new KeyedCodec<>("West", new ArrayCodec<>(Codec.STRING, String[]::new), false), (o, tags) -> {
         HashSet<String> strings = new HashSet<>(tags.length);
         strings.addAll(Arrays.asList(tags));
         o.blockFaceTags.put(Vector3iUtil.WEST, strings);
      }, o -> o.blockFaceTags.containsKey(Vector3iUtil.WEST) ? o.blockFaceTags.get(Vector3iUtil.WEST).toArray(String[]::new) : new String[0])
      .add()
      .append(new KeyedCodec<>("Up", new ArrayCodec<>(Codec.STRING, String[]::new), false), (o, tags) -> {
         HashSet<String> strings = new HashSet<>(tags.length);
         strings.addAll(Arrays.asList(tags));
         o.blockFaceTags.put(Vector3iUtil.UP, strings);
      }, o -> o.blockFaceTags.containsKey(Vector3iUtil.UP) ? o.blockFaceTags.get(Vector3iUtil.UP).toArray(String[]::new) : new String[0])
      .add()
      .append(new KeyedCodec<>("Down", new ArrayCodec<>(Codec.STRING, String[]::new), false), (o, tags) -> {
         HashSet<String> strings = new HashSet<>(tags.length);
         strings.addAll(Arrays.asList(tags));
         o.blockFaceTags.put(Vector3iUtil.DOWN, strings);
      }, o -> o.blockFaceTags.containsKey(Vector3iUtil.DOWN) ? o.blockFaceTags.get(Vector3iUtil.DOWN).toArray(String[]::new) : new String[0])
      .add()
      .build();
   public static final ConnectedBlockFaceTags EMPTY = new ConnectedBlockFaceTags();
   @Nonnull
   private final Map<Vector3ic, HashSet<String>> blockFaceTags = new Object2ObjectOpenHashMap();

   public boolean contains(Vector3i direction, String blockFaceTag) {
      return this.blockFaceTags.containsKey(direction) && this.blockFaceTags.get(direction).contains(blockFaceTag);
   }

   @Nonnull
   public Map<Vector3ic, HashSet<String>> getBlockFaceTags() {
      return this.blockFaceTags;
   }

   public Set<String> getBlockFaceTags(Vector3i direction) {
      return (Set<String>)(this.blockFaceTags.containsKey(direction) ? this.blockFaceTags.get(direction) : Collections.emptySet());
   }

   @Nonnull
   public Set<Vector3ic> getDirections() {
      return this.blockFaceTags.keySet();
   }
}
