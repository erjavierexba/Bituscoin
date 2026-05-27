package es.us.dad.vertx.entities;

public class CoinbaseTransaction extends Transaction {

    private static final long BLOCK_REWARD = 5000000000L;

    public CoinbaseTransaction() {
        super();
        this.setSender("COINBASE_SYSTEM");
    }

    public CoinbaseTransaction(String minerAddress, String extraData) {
        super("COINBASE_SYSTEM", minerAddress, BLOCK_REWARD);

        this.setSignature(extraData + " | Nonce:" + System.nanoTime());

        this.setTransactionId(calculateHash());
    }

    @Override
    public String calculateHash() {
        String baseData = getSender() + getReceiver() + Long.toString(getAmount()) + Long.toString(getFee()) + Long.toString(getTimestamp());
        String extra = (getSignature() != null) ? getSignature() : "";
        return applySha256(baseData + extra);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
