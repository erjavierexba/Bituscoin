package es.us.dad.vertx.entities;

import es.us.dad.vertx.utils.SecurityUtils;
import io.vertx.core.json.JsonObject;

import java.security.PublicKey;

public class Transaction {

    private String transactionId;
    private String sender;
    private String receiver;
    private long amount;
    private long fee;
    private long timestamp;
    private String signature;

    public Transaction() {
    }

    public Transaction(JsonObject tx) {
        this.transactionId = tx.getString("transactionId");
        this.sender = tx.getString("sender");
        this.receiver = tx.getString("receiver");
        this.amount = tx.getLong("amount");
        this.fee = tx.getLong("fee", 0L);
        this.timestamp = tx.getLong("timestamp");
        this.signature = tx.getString("signature");
    }

    public Transaction(String sender, String receiver, long amount) {
        this(sender, receiver, amount, 0L);
    }

    public Transaction(String sender, String receiver, long amount, long fee) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.fee = fee;
        this.timestamp = System.currentTimeMillis();
        this.transactionId = calculateHash();
    }

    public String calculateHash() {
        String dataToHash = sender + receiver + Long.toString(amount) + Long.toString(fee) + Long.toString(timestamp);
        return applySha256(dataToHash);
    }

    public boolean verifySignature() {
        if (this.sender.equals("COINBASE_SYSTEM")) {
            return true;
        }

        if (this.signature == null || this.signature.isEmpty()) {
            return false;
        }

        try {
            PublicKey pubKey = SecurityUtils.decodePublicKey(this.sender);
            byte[] sigBytes = java.util.Base64.getDecoder().decode(this.signature);
            return SecurityUtils.verifyECDSASig(pubKey, this.transactionId, sigBytes);
        } catch (Exception e) {
            return false;
        }
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("transactionId", this.transactionId)
                .put("sender", this.sender)
                .put("receiver", this.receiver)
                .put("amount", this.amount)
                .put("fee", this.fee)
                .put("timestamp", this.timestamp);

        if (this.signature != null) {
            json.put("signature", this.signature);
        }

        return json;
    }

    public JsonObject toNetworkJson() {
        return toJson();
    }

    public static String applySha256(String input) {
        try {
             return SHA256.applySha256(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    public long getFee() { return fee; }
    public void setFee(long fee) { this.fee = fee; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    @Override
    public String toString() {
        return String.format("[%s] %s -> %s : %d BTC", transactionId, sender, receiver, amount);
    }
}
