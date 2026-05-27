package es.us.dad.vertx.wallet;

import dev.javi.bituscoin.TransactionBuilder;
import es.us.dad.vertx.entities.Transaction;
import es.us.dad.vertx.network.BusAddresses;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import java.util.UUID;

public class WalletVerticle extends AbstractVerticle {

    private String identity;
    private Wallet myWallet;

    @Override
    public void start() {
        this.identity = "Wallet-" + UUID.randomUUID().toString().substring(0, 4);

        this.myWallet = new Wallet();

        System.out.println("💰 " + this.identity + " iniciada.");
        System.out.println("🔑 Mi dirección pública: " + myWallet.getAddress().substring(0, 20) + "...");

        vertx.setPeriodic(5000, id -> generateAndBroadcastTransaction());
    }

    private void generateAndBroadcastTransaction() {
        Transaction tx = new TransactionBuilder()
                .from(myWallet.getAddress())
                .to("Bob")
                .amount(10)
                .fee(0L)
                .signWith(myWallet.getPrivateKey())
                .build();
        System.out.println("💸 " + this.identity + " generando TX firmada: " + tx.getTransactionId().substring(0,8) + "...");

        JsonObject transactionData = tx.toNetworkJson();
        vertx.eventBus().publish(BusAddresses.INTERNAL_NEW_TRANSACTION, transactionData);
    }
}
