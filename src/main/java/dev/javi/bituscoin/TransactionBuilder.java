package dev.javi.bituscoin;

import es.us.dad.vertx.entities.Transaction;
import es.us.dad.vertx.utils.SecurityUtils;

import java.security.PrivateKey;
import java.util.Base64;

public class TransactionBuilder {

    private String from;
    private String to;
    private Long amount;
    private long fee;
    private Long timestamp;
    private PrivateKey signingKey;

    public TransactionBuilder from(String from) {
        this.from = from;
        return this;
    }

    public TransactionBuilder to(String to) {
        this.to = to;
        return this;
    }

    public TransactionBuilder amount(long amount) {
        this.amount = amount;
        return this;
    }

    public TransactionBuilder fee(long fee) {
        this.fee = fee;
        return this;
    }

    public TransactionBuilder timestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public TransactionBuilder signWith(PrivateKey signingKey) {
        this.signingKey = signingKey;
        return this;
    }

    public Transaction build() {
        validate();

        Transaction transaction = new Transaction();
        transaction.setSender(from);
        transaction.setReceiver(to);
        transaction.setAmount(amount);
        transaction.setFee(fee);
        transaction.setTimestamp(timestamp != null ? timestamp : System.currentTimeMillis());

        // TODO UTXO: cuando exista un modelo de inputs/outputs, seleccionar inputs suficientes,
        // crear el output al receptor y devolver el cambio (inputTotal - amount - fee) al emisor.
        String transactionId = transaction.calculateHash();
        transaction.setTransactionId(transactionId);

        byte[] signature = SecurityUtils.applyECDSASig(signingKey, transactionId);
        transaction.setSignature(Base64.getEncoder().encodeToString(signature));

        return transaction;
    }

    private void validate() {
        if (from == null || from.isBlank()) {
            throw new IllegalArgumentException("from no puede estar vacio");
        }
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("to no puede estar vacio");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount es obligatorio");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount debe ser mayor que 0");
        }
        if (fee < 0) {
            throw new IllegalArgumentException("fee debe ser mayor o igual que 0");
        }
        if (signingKey == null) {
            throw new IllegalArgumentException("signingKey es obligatoria para firmar la transaccion");
        }
    }
}
