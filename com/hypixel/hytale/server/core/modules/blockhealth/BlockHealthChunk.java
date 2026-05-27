package com.hypixel.hytale.server.core.modules.blockhealth;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.packets.world.UpdateBlockDamage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.io.MemorySegmentUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.joml.Vector3i;

public class BlockHealthChunk implements Component<ChunkStore> {
   private static final byte SERIALIZATION_VERSION = 2;
   public static final BuilderCodec<BlockHealthChunk> CODEC = BuilderCodec.builder(BlockHealthChunk.class, BlockHealthChunk::new)
      .append(new KeyedCodec<>("Data", Codec.BYTE_ARRAY), BlockHealthChunk::deserialize, BlockHealthChunk::serialize)
      .documentation("Binary data representing the state of this BlockHealthChunk")
      .add()
      .<Instant>append(new KeyedCodec<>("LastRepairGameTime", Codec.INSTANT), (o, l) -> o.lastRepairGameTime = l, o -> o.lastRepairGameTime)
      .documentation("The last tick of the world this BlockHealthChunk processed.")
      .add()
      .build();
   private final Map<Vector3i, BlockHealth> blockHealthMap = new Object2ObjectOpenHashMap(0);
   private final Map<Vector3i, FragileBlock> blockFragilityMap = new Object2ObjectOpenHashMap(0);
   private Instant lastRepairGameTime;

   public Instant getLastRepairGameTime() {
      return this.lastRepairGameTime;
   }

   public void setLastRepairGameTime(Instant lastRepairGameTime) {
      this.lastRepairGameTime = lastRepairGameTime;
   }

   @Nonnull
   public Map<Vector3i, BlockHealth> getBlockHealthMap() {
      return this.blockHealthMap;
   }

   @Nonnull
   public Map<Vector3i, FragileBlock> getBlockFragilityMap() {
      return this.blockFragilityMap;
   }

   @Nonnull
   public BlockHealth damageBlock(Instant currentUptime, @Nonnull World world, @Nonnull Vector3i block, float health) {
      BlockHealth blockHealth = this.blockHealthMap.compute(block, (key, value) -> {
         if (value == null) {
            value = new BlockHealth();
         }

         value.setHealth(value.getHealth() - health);
         value.setLastDamageGameTime(currentUptime);
         return (BlockHealth)(value.getHealth() < 1.0 ? value : null);
      });
      if (blockHealth != null && !blockHealth.isDestroyed()) {
         Predicate<PlayerRef> filter = player -> true;
         world.getNotificationHandler().updateBlockDamage(block.x(), block.y(), block.z(), blockHealth.getHealth(), -health, filter);
      }

      return Objects.requireNonNullElse(blockHealth, BlockHealth.NO_DAMAGE_INSTANCE);
   }

   @Nonnull
   public BlockHealth repairBlock(@Nonnull World world, @Nonnull Vector3i block, float progress) {
      BlockHealth blockHealth = Objects.requireNonNullElse(this.blockHealthMap.computeIfPresent(block, (key, value) -> {
         value.setHealth(value.getHealth() + progress);
         return (BlockHealth)(value.getHealth() > 1.0 ? value : null);
      }), BlockHealth.NO_DAMAGE_INSTANCE);
      world.getNotificationHandler().updateBlockDamage(block.x(), block.y(), block.z(), blockHealth.getHealth(), progress);
      return blockHealth;
   }

   public void removeBlock(@Nonnull World world, @Nonnull Vector3i block) {
      if (this.blockHealthMap.remove(block) != null) {
         world.getNotificationHandler().updateBlockDamage(block.x(), block.y(), block.z(), BlockHealth.NO_DAMAGE_INSTANCE.getHealth(), 0.0F);
      }
   }

   public void makeBlockFragile(Vector3i blockLocation, float fragileDuration) {
      this.blockFragilityMap.compute(blockLocation, (key, value) -> {
         if (value == null) {
            value = new FragileBlock(fragileDuration);
         }

         value.setDurationSeconds(fragileDuration);
         return (FragileBlock)(value.getDurationSeconds() <= 0.0 ? null : value);
      });
   }

   public boolean isBlockFragile(Vector3i block) {
      return this.blockFragilityMap.get(block) != null;
   }

   public float getBlockHealth(Vector3i block) {
      return this.blockHealthMap.getOrDefault(block, BlockHealth.NO_DAMAGE_INSTANCE).getHealth();
   }

   public void createBlockDamagePackets(@Nonnull List<ToClientPacket> list) {
      for (Entry<Vector3i, BlockHealth> entry : this.blockHealthMap.entrySet()) {
         Vector3i block = entry.getKey();
         BlockPosition blockPosition = new BlockPosition(block.x(), block.y(), block.z());
         list.add(new UpdateBlockDamage(blockPosition, entry.getValue().getHealth(), 0.0F));
      }
   }

   @Nonnull
   public BlockHealthChunk clone() {
      BlockHealthChunk copy = new BlockHealthChunk();
      copy.lastRepairGameTime = this.lastRepairGameTime;

      for (Entry<Vector3i, BlockHealth> entry : this.blockHealthMap.entrySet()) {
         copy.blockHealthMap.put(entry.getKey(), entry.getValue().clone());
      }

      for (Entry<Vector3i, FragileBlock> entry : this.blockFragilityMap.entrySet()) {
         copy.blockFragilityMap.put(entry.getKey(), entry.getValue().clone());
      }

      return copy;
   }

   public void deserialize(@Nonnull byte[] data) {
      this.blockHealthMap.clear();
      this.blockFragilityMap.clear();
      MemorySegment mem = MemorySegment.ofArray(data);
      byte version = mem.get(ValueLayout.JAVA_BYTE, 0L);
      int healthEntries = mem.get(MemorySegmentUtil.INT_BE, 1L);
      int offset = 5;

      for (int i = 0; i < healthEntries; i++) {
         int x = mem.get(MemorySegmentUtil.INT_BE, (long)offset);
         int y = mem.get(MemorySegmentUtil.INT_BE, (long)(offset + 4));
         int z = mem.get(MemorySegmentUtil.INT_BE, (long)(offset + 8));
         BlockHealth bh = new BlockHealth();
         bh.deserialize(mem, offset + 12, version);
         this.blockHealthMap.put(new Vector3i(x, y, z), bh);
         offset += 24;
      }

      if (version > 1) {
         int fragilityEntries = mem.get(MemorySegmentUtil.INT_BE, (long)offset);
         offset += 4;

         for (int i = 0; i < fragilityEntries; i++) {
            int x = mem.get(MemorySegmentUtil.INT_BE, (long)offset);
            int y = mem.get(MemorySegmentUtil.INT_BE, (long)(offset + 4));
            int z = mem.get(MemorySegmentUtil.INT_BE, (long)(offset + 8));
            FragileBlock fragileBlock = new FragileBlock();
            fragileBlock.deserialize(mem, offset + 12, version);
            this.blockFragilityMap.put(new Vector3i(x, y, z), fragileBlock);
            offset += 16;
         }
      }
   }

   public byte[] serialize() {
      byte[] result = new byte[5 + this.blockHealthMap.size() * 24 + 4 + this.blockFragilityMap.size() * 16];
      MemorySegment data = MemorySegment.ofArray(result);
      data.set(ValueLayout.JAVA_BYTE, 0L, (byte)2);
      data.set(MemorySegmentUtil.INT_BE, 1L, this.blockHealthMap.size());
      int offset = 5;

      for (Entry<Vector3i, BlockHealth> entry : this.blockHealthMap.entrySet()) {
         Vector3i vec = entry.getKey();
         data.set(MemorySegmentUtil.INT_BE, (long)offset, vec.x);
         data.set(MemorySegmentUtil.INT_BE, (long)(offset + 4), vec.y);
         data.set(MemorySegmentUtil.INT_BE, (long)(offset + 8), vec.z);
         BlockHealth bh = entry.getValue();
         bh.serialize(data, offset + 12);
         offset += 24;
      }

      data.set(MemorySegmentUtil.INT_BE, (long)offset, this.blockFragilityMap.size());
      offset += 4;

      for (Entry<Vector3i, FragileBlock> entry : this.blockFragilityMap.entrySet()) {
         Vector3i vec = entry.getKey();
         data.set(MemorySegmentUtil.INT_BE, (long)offset, vec.x);
         data.set(MemorySegmentUtil.INT_BE, (long)(offset + 4), vec.y);
         data.set(MemorySegmentUtil.INT_BE, (long)(offset + 8), vec.z);
         FragileBlock fragileBlock = entry.getValue();
         fragileBlock.serialize(data, offset + 12);
         offset += 16;
      }

      return result;
   }
}
