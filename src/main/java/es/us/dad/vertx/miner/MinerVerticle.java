package es.us.dad.vertx.miner;

import es.us.dad.vertx.entities.Block;
import es.us.dad.vertx.entities.BlockChain;
import es.us.dad.vertx.entities.Body;
import es.us.dad.vertx.entities.Transaction;
import es.us.dad.vertx.network.BusAddresses;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class MinerVerticle extends AbstractVerticle {

    private BlockChain blockchain;

    private List<Transaction> transactionPool = new ArrayList<>();

    private static final int BLOCK_SIZE = 3;

    private boolean isMining = false;

    @Override
    public void start() {
        this.blockchain = new BlockChain();

        vertx.eventBus().consumer(BusAddresses.INCOMING_BLOCK, msg -> {
            try {
                JsonObject blockJson = (JsonObject) msg.body();
                Block receivedBlock = new Block(blockJson);
                System.out.println("📦 Bloque recibido de la red: " + receivedBlock.getHash());
                blockchain.addBlock(receivedBlock);
                transactionPool.clear();
                isMining = false;
                System.out.println("✅ Bloque #" + receivedBlock.getHeader().getIndex() + " añadido a la cadena.");
            } catch (Exception e) {
                System.err.println("❌ Error procesando bloque entrante: " + e.getMessage());
            }
        });

        vertx.eventBus().consumer(BusAddresses.INCOMING_TRANSACTION, msg -> {
            addTransactionToPool((JsonObject) msg.body());
        });

        vertx.eventBus().consumer(BusAddresses.INTERNAL_NEW_TRANSACTION, msg -> {
            addTransactionToPool((JsonObject) msg.body());
        });
    }

    private void addTransactionToPool(JsonObject txJson) {
        Transaction tx = new Transaction(txJson);

        if (!tx.verifySignature() || !tx.getTransactionId().equals(tx.calculateHash())) {
            System.err.println("🚨 HACKER DETECTADO: Firma inválida en la TX " + tx.getTransactionId());
            return;
        }

        if (transactionPool.stream().anyMatch(t -> t.getTransactionId().equals(tx.getTransactionId()))) {
            return;
        }

        transactionPool.add(tx);
        System.out.println("📥 TX válida añadida a Mempool. Total: " + transactionPool.size() + "/" + BLOCK_SIZE);

        if (transactionPool.size() >= BLOCK_SIZE && !isMining) {
            mineBlock();
        }
    }

    private void mineBlock() {
        System.out.println("⛏️ ¡Mempool llena! Iniciando minado...");

        isMining = true;

        int limit = Math.min(BLOCK_SIZE, transactionPool.size());
        List<Transaction> transactionsForBlock = new ArrayList<>(transactionPool.subList(0, limit));

        transactionPool.subList(0, limit).clear();

        Body body = new Body(transactionsForBlock);

        Block newBlock = blockchain.createNextBlock(body);

        vertx.executeBlocking(() -> {
            mineBlockPoW(newBlock);
            return newBlock;
        }).onComplete(res -> {
            if (res.succeeded()) {
                Block minedBlock = (Block) res.result();
                System.out.println("✅ ¡BLOQUE MINADO! Hash: " + minedBlock.getHash());

                try {
                    blockchain.addBlock(minedBlock);
                    vertx.eventBus().publish(BusAddresses.MINED_BLOCK, minedBlock.toJson());

                } catch (RuntimeException e) {
                    System.err.println("⚠️ Bloque descartado (Stale Block): Alguien minó más rápido que nosotros.");
                    System.err.println("Motivo: " + e.getMessage());

                } finally {
                    isMining = false;

                    if (transactionPool.size() >= BLOCK_SIZE) {
                        mineBlock();
                    }
                }
            }
        });
    }

    private void mineBlockPoW(Block block) {
        int difficulty = 4;
        String target = new String(new char[difficulty]).replace('\0', '0');
        String hash;
        do {
            block.getHeader().setNonce(block.getHeader().getNonce() + 1);
            hash = block.calculateHash();
        } while (!hash.startsWith(target));

        block.setHash(hash);
    }
}
