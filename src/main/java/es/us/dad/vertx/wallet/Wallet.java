package es.us.dad.vertx.wallet;

import es.us.dad.vertx.entities.Transaction;
import es.us.dad.vertx.utils.SecurityUtils;
import dev.javi.bituscoin.TransactionBuilder;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Wallet {

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public Wallet() {
        KeyPair pair = SecurityUtils.generateECKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public String getAddress() {
        return SecurityUtils.encodeKey(publicKey);
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public Transaction sendFunds(String receiver, long amount) {
        return new TransactionBuilder()
                .from(this.getAddress())
                .to(receiver)
                .amount(amount)
                .fee(0L)
                .signWith(this.privateKey)
                .build();
    }
}
