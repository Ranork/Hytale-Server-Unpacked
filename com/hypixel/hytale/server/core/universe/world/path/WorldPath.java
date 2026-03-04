package com.hypixel.hytale.server.core.universe.world.path;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Transform;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;

public class WorldPath implements IPath<SimplePathWaypoint> {
   public static final BuilderCodec<WorldPath> CODEC = BuilderCodec.builder(WorldPath.class, WorldPath::new)
      .addField(new KeyedCodec<>("Id", Codec.UUID_BINARY), (worldPath, uuid) -> worldPath.id = uuid, worldPath -> worldPath.id)
      .addField(new KeyedCodec<>("Name", Codec.STRING), (worldPath, s) -> worldPath.name = s, worldPath -> worldPath.name)
      .addField(
         new KeyedCodec<>("Waypoints", new ArrayCodec<>(Transform.CODEC, Transform[]::new)),
         (worldPath, wayPoints) -> worldPath.waypoints = List.of(wayPoints),
         worldPath -> worldPath.waypoints.toArray(Transform[]::new)
      )
      .build();
   protected UUID id;
   protected String name;
   protected List<Transform> waypoints = Collections.emptyList();
   protected List<SimplePathWaypoint> simpleWaypoints;

   protected WorldPath() {
   }

   public WorldPath(String name, List<Transform> waypoints) {
      this.id = UUID.randomUUID();
      this.name = name;
      this.waypoints = waypoints;
   }

   @Override
   public UUID getId() {
      return this.id;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Nonnull
   @Override
   public List<SimplePathWaypoint> getPathWaypoints() {
      if (this.simpleWaypoints == null || this.simpleWaypoints.size() != this.waypoints.size()) {
         this.simpleWaypoints = new ObjectArrayList();

         for (short i = 0; i < this.waypoints.size(); i++) {
            this.simpleWaypoints.add(new SimplePathWaypoint(i, this.waypoints.get(i)));
         }
      }

      this.simpleWaypoints = Collections.unmodifiableList(this.simpleWaypoints);
      return this.simpleWaypoints;
   }

   @Override
   public int length() {
      return this.waypoints.size();
   }

   public SimplePathWaypoint get(int index) {
      List<SimplePathWaypoint> path = this.getPathWaypoints();
      return path.get(index);
   }

   public List<Transform> getWaypoints() {
      return this.waypoints;
   }

   @Nonnull
   @Override
   public String toString() {
      return "WorldPath{name='" + this.name + "', waypoints=" + this.waypoints + "}";
   }
}
