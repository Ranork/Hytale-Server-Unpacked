package com.hypixel.hytale.lib.quiche;

import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.annotation.Nonnull;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public record QuicheServerCredentials(@Nonnull X509Certificate certificate, @Nonnull byte[] privateKeyDer) {
   @Nonnull
   public static QuicheServerCredentials generateSelfSigned() {
      try {
         KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("EC");
         keyPairGen.initialize(new ECGenParameterSpec("secp256r1"));
         KeyPair keyPair = keyPairGen.generateKeyPair();
         Instant now = Instant.now();
         Instant notAfter = now.plus(365L, ChronoUnit.DAYS);
         X500Principal issuer = new X500Principal("CN=Hytale Server");
         JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
            issuer, BigInteger.valueOf(System.currentTimeMillis()), Date.from(now), Date.from(notAfter), issuer, keyPair.getPublic()
         );
         ContentSigner signer = new JcaContentSignerBuilder("SHA256withECDSA").build(keyPair.getPrivate());
         X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(certBuilder.build(signer));
         return new QuicheServerCredentials(certificate, keyPair.getPrivate().getEncoded());
      } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | OperatorCreationException | CertificateException var8) {
         throw SneakyThrow.sneakyThrow(var8);
      }
   }

   @Nonnull
   public byte[] certificateDer() {
      try {
         return this.certificate.getEncoded();
      } catch (CertificateEncodingException var2) {
         throw SneakyThrow.sneakyThrow(var2);
      }
   }
}
