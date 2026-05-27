package com.hypixel.hytale.builtin.buildertools.tooloperations;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.Pair;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.joml.Vector3i;

public class LayersOperation extends ToolOperation {
   private final Vector3i depthDirection = new Vector3i();
   private final int layerOneLength;
   private final int layerTwoLength;
   private final boolean enableLayerTwo;
   private final int layerThreeLength;
   private final boolean enableLayerThree;
   private final String layerOneBlockPattern;
   private final String layerTwoBlockPattern;
   private final String layerThreeBlockPattern;
   private final int brushDensity;
   private final int maxDepthNecessary;
   private boolean failed;
   private final boolean skipLayerOne;
   private final boolean skipLayerTwo;
   private final boolean skipLayerThree;
   private List<Pair<Integer, String>> layers;

   public LayersOperation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player player,
      @Nonnull PlayerRef playerRef,
      @Nonnull BuilderToolOnUseInteraction packet,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      super(ref, packet, componentAccessor);
      HeadRotation headRotationComponent = componentAccessor.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      String var7 = (String)this.args.tool().get("aDirection");
      switch (var7) {
         case "Up":
            this.depthDirection.set(Vector3iUtil.UP);
            break;
         case "Down":
            this.depthDirection.set(Vector3iUtil.DOWN);
            break;
         case "North":
            this.depthDirection.set(Vector3iUtil.NORTH);
            break;
         case "South":
            this.depthDirection.set(Vector3iUtil.SOUTH);
            break;
         case "East":
            this.depthDirection.set(Vector3iUtil.EAST);
            break;
         case "West":
            this.depthDirection.set(Vector3iUtil.WEST);
            break;
         case "Camera":
            this.depthDirection.set(headRotationComponent.getAxisDirection());
            break;
         default:
            this.depthDirection.set(Vector3iUtil.DOWN);
      }

      this.brushDensity = (Integer)this.args.tool().get("jBrushDensity");
      this.layerOneLength = (Integer)this.args.tool().get("bLayerOneLength");
      this.layerTwoLength = (Integer)this.args.tool().get("eLayerTwoLength");
      this.layerThreeLength = (Integer)this.args.tool().get("hLayerThreeLength");
      this.layerOneBlockPattern = this.args.tool().get("cLayerOneMaterial").toString();
      this.layerTwoBlockPattern = this.args.tool().get("fLayerTwoMaterial").toString();
      this.layerThreeBlockPattern = this.args.tool().get("iLayerThreeMaterial").toString();
      this.enableLayerTwo = (Boolean)this.args.tool().get("dEnableLayerTwo");
      this.enableLayerThree = (Boolean)this.args.tool().get("gEnableLayerThree");
      this.skipLayerOne = (Boolean)this.args.tool().getOrDefault("kSkipLayerOne", false);
      this.skipLayerTwo = (Boolean)this.args.tool().getOrDefault("lSkipLayerTwo", false);
      this.skipLayerThree = (Boolean)this.args.tool().getOrDefault("mSkipLayerThree", false);
      this.layers = new ArrayList<>();
      if (!this.skipLayerOne) {
         this.layers.add(Pair.of(this.layerOneLength, this.layerOneBlockPattern));
      }

      if (!this.skipLayerTwo && this.enableLayerTwo) {
         this.layers.add(Pair.of(this.layerTwoLength, this.layerTwoBlockPattern));
      }

      if (!this.skipLayerThree && this.enableLayerThree) {
         this.layers.add(Pair.of(this.layerThreeLength, this.layerThreeBlockPattern));
      }

      this.maxDepthNecessary = this.layerOneLength + (this.enableLayerTwo ? this.layerTwoLength : 0) + (this.enableLayerThree ? this.layerThreeLength : 0);
      if (this.enableLayerThree && !this.enableLayerTwo) {
         playerRef.sendMessage(Message.translation("server.builderTools.layerOperation.layerTwoRequired"));
         this.failed = true;
      }
   }

   @Override
   protected boolean executeBlock(int x, int y, int z) {
      if (this.failed) {
         return false;
      } else if (this.random.nextInt(100) > this.brushDensity) {
         return true;
      } else if (this.edit.getBlock(x, y, z) <= 0) {
         return true;
      } else {
         BlockAccessor chunk = this.edit.getAccessor().getChunk(ChunkUtil.indexChunkFromBlock(x, z));
         this.builderState
            .layer(x, y, z, this.layers, this.maxDepthNecessary, this.depthDirection, (WorldChunk)chunk, this.edit.getBefore(), this.edit.getAfter());
         return true;
      }
   }
}
