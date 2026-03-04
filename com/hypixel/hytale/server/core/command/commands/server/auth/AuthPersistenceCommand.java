package com.hypixel.hytale.server.core.command.commands.server.auth;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.AuthCredentialStoreProvider;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import java.awt.Color;
import javax.annotation.Nonnull;

public class AuthPersistenceCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_SINGLEPLAYER = Message.translation("server.commands.auth.persistence.singleplayer").color(Color.RED);

   public AuthPersistenceCommand() {
      super("persistence", "server.commands.auth.persistence.desc");
      this.addUsageVariant(new AuthPersistenceCommand.SetPersistenceVariant());
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      if (ServerAuthManager.getInstance().isSingleplayer()) {
         context.sendMessage(MESSAGE_SINGLEPLAYER);
      } else {
         AuthCredentialStoreProvider provider = HytaleServer.get().getConfig().getAuthCredentialStoreProvider();
         String typeName = AuthCredentialStoreProvider.CODEC.getIdFor((Class<? extends AuthCredentialStoreProvider>)provider.getClass());
         context.sendMessage(Message.translation("server.commands.auth.persistence.current").color(Color.YELLOW).param("type", typeName));
         String availableTypes = String.join(", ", AuthCredentialStoreProvider.CODEC.getRegisteredIds());
         context.sendMessage(Message.translation("server.commands.auth.persistence.available").color(Color.GRAY).param("types", availableTypes));
      }
   }

   private static class SetPersistenceVariant extends CommandBase {
      @Nonnull
      private final RequiredArg<String> typeArg = this.withRequiredArg("type", "server.commands.auth.persistence.type.desc", ArgTypes.STRING);

      SetPersistenceVariant() {
         super("server.commands.auth.persistence.variant.desc");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         ServerAuthManager authManager = ServerAuthManager.getInstance();
         if (authManager.isSingleplayer()) {
            context.sendMessage(AuthPersistenceCommand.MESSAGE_SINGLEPLAYER);
         } else {
            String typeName = this.typeArg.get(context);
            BuilderCodec<? extends AuthCredentialStoreProvider> codec = AuthCredentialStoreProvider.CODEC.getCodecFor(typeName);
            if (codec == null) {
               context.sendMessage(Message.translation("server.commands.auth.persistence.unknownType").color(Color.RED).param("type", typeName));
            } else {
               AuthCredentialStoreProvider newProvider = codec.getDefaultValue();
               HytaleServer.get().getConfig().setAuthCredentialStoreProvider(newProvider);
               authManager.swapCredentialStoreProvider(newProvider);
               context.sendMessage(Message.translation("server.commands.auth.persistence.changed").color(Color.GREEN).param("type", typeName));
            }
         }
      }
   }
}
