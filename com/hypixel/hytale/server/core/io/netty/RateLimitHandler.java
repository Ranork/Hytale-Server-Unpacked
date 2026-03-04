package com.hypixel.hytale.server.core.io.netty;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.io.netty.ProtocolUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.logging.Level;

public class RateLimitHandler extends ChannelInboundHandlerAdapter {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final int maxTokens;
   private final int refillRate;
   private int tokens;
   private long lastRefillTime;

   public RateLimitHandler(int maxTokens, int refillRate) {
      this.maxTokens = maxTokens;
      this.refillRate = refillRate;
      this.tokens = maxTokens;
      this.lastRefillTime = System.nanoTime();
   }

   private void refillTokens() {
      long now = System.nanoTime();
      long elapsedNanos = now - this.lastRefillTime;
      long tokensToAdd = elapsedNanos * this.refillRate / 1000000000L;
      if (tokensToAdd > 0L) {
         this.tokens = (int)Math.min((long)this.maxTokens, this.tokens + tokensToAdd);
         this.lastRefillTime = now;
      }
   }

   public void channelRead(ChannelHandlerContext ctx, Object msg) {
      this.refillTokens();
      if (this.tokens > 0) {
         this.tokens--;
         ctx.fireChannelRead(msg);
      } else {
         LOGGER.at(Level.WARNING).log("Rate limit exceeded for %s, disconnecting", NettyUtil.formatRemoteAddress(ctx.channel()));
         ProtocolUtil.closeApplicationConnection(ctx.channel(), 1);
      }
   }
}
