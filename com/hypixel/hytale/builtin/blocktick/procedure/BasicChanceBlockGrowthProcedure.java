package com.hypixel.hytale.builtin.blocktick.procedure;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.blocktick.config.TickProcedure;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;

public class BasicChanceBlockGrowthProcedure extends TickProcedure {
   @Nonnull
   public static final BuilderCodec<BasicChanceBlockGrowthProcedure> CODEC = BuilderCodec.builder(
         BasicChanceBlockGrowthProcedure.class, BasicChanceBlockGrowthProcedure::new, TickProcedure.BASE_CODEC
      )
      .addField(new KeyedCodec<>("NextId", Codec.STRING), (proc, v) -> proc.to = v, proc -> proc.to)
      .addField(new KeyedCodec<>("ChanceMin", Codec.INTEGER), (proc, v) -> proc.chanceMin = v, proc -> proc.chanceMin)
      .addField(new KeyedCodec<>("Chance", Codec.INTEGER), (proc, v) -> proc.chance = v, proc -> proc.chance)
      .addField(new KeyedCodec<>("NextTicking", Codec.BOOLEAN), (proc, v) -> proc.nextTicking = v, proc -> proc.nextTicking)
      .build();
   protected int chanceMin;
   protected int chance;
   protected String to;
   protected boolean nextTicking;

   public BasicChanceBlockGrowthProcedure() {
   }

   public BasicChanceBlockGrowthProcedure(int chanceMin, int chance, String to, boolean nextTicking) {
      this.chanceMin = chanceMin;
      this.chance = chance;
      this.to = to;
      this.nextTicking = nextTicking;
   }

   @Nonnull
   @Override
   public BlockTickStrategy onTick(@Nonnull World world, WorldChunk wc, int worldX, int worldY, int worldZ, int blockId) {
      if (!this.runChance()) {
         return BlockTickStrategy.CONTINUE;
      } else {
         return this.executeToBlock(world, worldX, worldY, worldZ, this.to) ? BlockTickStrategy.CONTINUE : BlockTickStrategy.SLEEP;
      }
   }

   protected boolean runChance() {
      return this.getRandom().nextInt(this.chance) < this.chanceMin;
   }

   protected boolean executeToBlock(@Nonnull World world, int worldX, int worldY, int worldZ, String to) {
      world.setBlock(worldX, worldY, worldZ, to);
      return !this.nextTicking;
   }

   @Nonnull
   @Override
   public String toString() {
      return "BasicChanceBlockGrowthProcedure{chanceMin="
         + this.chanceMin
         + ", chance="
         + this.chance
         + ", to="
         + this.to
         + ", nextTicking="
         + this.nextTicking
         + "}";
   }
}
