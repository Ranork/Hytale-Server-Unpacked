package com.hypixel.hytale.server.core.universe.world;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil.FillerFetcher;
import java.util.concurrent.CompletableFuture;

class World$1 implements FillerFetcher<BlockSection, ChunkStore> {
    World$1(final World param1, final ChunkSection param2) {
        this.val$sectionInfo = nullx;
        this.this$0 = this$0;
    }

    public int getBlock(BlockSection blockSection, ChunkStore chunkStore, int x, int y, int z) {
        if (x >= 0 && y >= 0 && z >= 0 && x < 32 && y < 32 && z < 32) {
            return blockSection.get(x, y, z);
        } else {
            int nx = this.val$sectionInfo.getX() + ChunkUtil.chunkCoordinate(x);
            int ny = this.val$sectionInfo.getY() + ChunkUtil.chunkCoordinate(y);
            int nz = this.val$sectionInfo.getZ() + ChunkUtil.chunkCoordinate(z);
            CompletableFuture<Ref<ChunkStore>> refFuture = chunkStore.getChunkSectionReferenceAsync(nx, ny, nz);

            while (!refFuture.isDone()) {
                this.this$0.consumeTaskQueue();
            }

            Ref<ChunkStore> ref = (Ref<ChunkStore>)refFuture.join();
            BlockSection blocks = (BlockSection)chunkStore.getStore().getComponent(ref, BlockSection.getComponentType());
            return blocks == null ? Integer.MIN_VALUE : blocks.get(x, y, z);
        }
    }

    public int getFiller(BlockSection blockSection, ChunkStore chunkStore, int x, int y, int z) {
        if (x >= 0 && y >= 0 && z >= 0 && x < 32 && y < 32 && z < 32) {
            return blockSection.getFiller(x, y, z);
        } else {
            int nx = this.val$sectionInfo.getX() + ChunkUtil.chunkCoordinate(x);
            int ny = this.val$sectionInfo.getY() + ChunkUtil.chunkCoordinate(y);
            int nz = this.val$sectionInfo.getZ() + ChunkUtil.chunkCoordinate(z);
            CompletableFuture<Ref<ChunkStore>> refFuture = chunkStore.getChunkSectionReferenceAsync(nx, ny, nz);

            while (!refFuture.isDone()) {
                this.this$0.consumeTaskQueue();
            }

            Ref<ChunkStore> ref = (Ref<ChunkStore>)refFuture.join();
            BlockSection blocks = (BlockSection)chunkStore.getStore().getComponent(ref, BlockSection.getComponentType());
            return blocks == null ? Integer.MIN_VALUE : blocks.getFiller(x, y, z);
        }
    }

    public int getRotationIndex(BlockSection blockSection, ChunkStore chunkStore, int x, int y, int z) {
        if (x >= 0 && y >= 0 && z >= 0 && x < 32 && y < 32 && z < 32) {
            return blockSection.getFiller(x, y, z);
        } else {
            int nx = this.val$sectionInfo.getX() + ChunkUtil.chunkCoordinate(x);
            int ny = this.val$sectionInfo.getY() + ChunkUtil.chunkCoordinate(y);
            int nz = this.val$sectionInfo.getZ() + ChunkUtil.chunkCoordinate(z);
            CompletableFuture<Ref<ChunkStore>> refFuture = chunkStore.getChunkSectionReferenceAsync(nx, ny, nz);

            while (!refFuture.isDone()) {
                this.this$0.consumeTaskQueue();
            }

            Ref<ChunkStore> ref = (Ref<ChunkStore>)refFuture.join();
            BlockSection blocks = (BlockSection)chunkStore.getStore().getComponent(ref, BlockSection.getComponentType());
            return blocks == null ? Integer.MIN_VALUE : blocks.getRotationIndex(x, y, z);
        }
    }
}