package com.hypixel.hytale.builtin.adventure.objectives.markers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.util.PositionUtil;

public class ObjectiveTaskMarker {
   public static final BuilderCodec<ObjectiveTaskMarker> CODEC = BuilderCodec.builder(ObjectiveTaskMarker.class, ObjectiveTaskMarker::new)
      .append(new KeyedCodec<>("Id", Codec.STRING), (marker, o) -> marker.id = o, marker -> marker.id)
      .add()
      .append(new KeyedCodec<>("Transform", Transform.CODEC), (marker, o) -> marker.transform = o, marker -> marker.transform)
      .add()
      .append(new KeyedCodec<>("Icon", Codec.STRING), (marker, o) -> marker.icon = o, marker -> marker.icon)
      .add()
      .append(new KeyedCodec<>("Name", Message.CODEC), (marker, o) -> marker.name = o, marker -> marker.name)
      .add()
      .build();
   public static final ArrayCodec<ObjectiveTaskMarker> ARRAY_CODEC = new ArrayCodec<>(CODEC, ObjectiveTaskMarker[]::new);
   private String id;
   private Transform transform;
   private String icon;
   private Message name;

   public ObjectiveTaskMarker() {
   }

   public ObjectiveTaskMarker(String id, Transform transform, String icon, Message name) {
      this.id = id;
      this.transform = transform;
      this.icon = icon;
      this.name = name;
   }

   public String getId() {
      return this.id;
   }

   public Transform getTransform() {
      return this.transform;
   }

   public String getIcon() {
      return this.icon;
   }

   public Message getName() {
      return this.name;
   }

   public MapMarker toProto() {
      return new MapMarker(this.id, this.name.getFormattedMessage(), null, this.icon, PositionUtil.toTransformPacket(this.transform), null, null);
   }
}
