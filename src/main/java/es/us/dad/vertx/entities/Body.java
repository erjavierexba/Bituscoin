package es.us.dad.vertx.entities;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Body {

    private List<Transaction> transactions;

    public Body() {
        this.transactions = new ArrayList<>();
    }

    public Body(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public Body(JsonObject json) {
        this.transactions = new ArrayList<>();
        JsonArray txs = json.getJsonArray("transactions");
        if (txs != null) {
            txs.forEach(tx -> this.transactions.add(new Transaction((JsonObject) tx)));
        }
    }

    public JsonObject toJson() {
        JsonArray txArray = new JsonArray();
        transactions.forEach(tx -> txArray.add(tx.toJson()));
        return new JsonObject().put("transactions", txArray);
    }

    public String calculateMerkleRoot() {
        if (transactions == null || transactions.isEmpty()) {
            return Transaction.applySha256("");
        }

        List<String> treeLayer = transactions.stream()
                .map(Transaction::getTransactionId)
                .collect(Collectors.toList());

        int count = treeLayer.size();

        while (count > 1) {
            List<String> newLayer = new ArrayList<>();

            for (int i = 0; i < count; i += 2) {
                String left = treeLayer.get(i);

                String right;
                if (i + 1 < count) {
                    right = treeLayer.get(i + 1);
                } else {
                    right = left;
                }

                String combinedHash = Transaction.applySha256(left + right);
                newLayer.add(combinedHash);
            }

            treeLayer = newLayer;
            count = treeLayer.size();
        }

        return treeLayer.get(0);
    }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
    public void addTransaction(Transaction tx) { this.transactions.add(tx); }
}
