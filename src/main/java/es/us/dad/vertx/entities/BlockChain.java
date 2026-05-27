package es.us.dad.vertx.entities;

import java.util.ArrayList;
import java.util.List;

public class BlockChain {

    private List<Block> chain;

    private int currentDifficulty = 4;

    public static final int VERSION = 1;


    public BlockChain() {
        this.chain = new ArrayList<>();
        chain.add(createGenesisBlock());
    }

    public Block getLatestBlock() {
        if (chain.isEmpty()) return null;
        return chain.get(chain.size() - 1);
    }

    public void addBlock(Block newBlock) {
        Block previousBlock = getLatestBlock();

        if (previousBlock != null) {
            if (newBlock.getHeader().getIndex() > 0 && !newBlock.getHeader().getPreviousHash().equals(previousBlock.getHash())) {
                throw new RuntimeException("❌ Rechazado: El bloque no apunta al último bloque de la cadena");
            }

            if (newBlock.getHeader().getIndex() != previousBlock.getHeader().getIndex() + 1) {
                throw new RuntimeException("❌ Rechazado: El índice del bloque es incorrecto");
            }
        }

        String calculatedHash = newBlock.calculateHash();
        if (!calculatedHash.equals(newBlock.getHash())) {
            throw new RuntimeException("❌ Rechazado: El hash del bloque no es consistente (datos modificados)");
        }

        String target = new String(new char[this.currentDifficulty]).replace('\0', '0');

        if (newBlock.getHeader().getDifficulty() < this.currentDifficulty) {
            throw new RuntimeException("❌ Rechazado: Dificultad inferior a la requerida");
        }

        if (!newBlock.getHash().startsWith(target)) {
            throw new RuntimeException("❌ Rechazado: El hash no cumple la prueba de trabajo (No minado)");
        }

        System.out.println("✅ Bloque #" + newBlock.getHeader().getIndex() + " añadido a la cadena.");
        this.chain.add(newBlock);
    }

    public Block createNextBlock(Body body) {
        Block previousBlock = getLatestBlock();

        Header nextHeader = new Header();
        nextHeader.setIndex(previousBlock.getHeader().getIndex() + 1);
        nextHeader.setPreviousHash(previousBlock.getHash());
        nextHeader.setTimestamp(System.currentTimeMillis());
        nextHeader.setVersion(VERSION);
        nextHeader.setDifficulty(this.currentDifficulty);
        nextHeader.setNonce(0);

        nextHeader.setMerkleRoot(body.calculateMerkleRoot());

        return new Block(nextHeader, body);
    }

    public Block createGenesisBlock() {
        long index = 0L;
        String previousHash = "0";
        long fixedTimestamp = 1700000000000L;
        long nonce = 0L;
        int difficulty = 1;

        CoinbaseTransaction coinbaseTransaction = new CoinbaseTransaction("admin", "");
        coinbaseTransaction.setSignature("GENESIS_FIXED_SIGNATURE");
        coinbaseTransaction.setTimestamp(fixedTimestamp);
        coinbaseTransaction.setTransactionId(coinbaseTransaction.calculateHash());

        System.out.println("⛏️ Coinbase values: " + coinbaseTransaction.toString());

        List<Transaction> txs = new ArrayList<>();
        txs.add(coinbaseTransaction);
        Body body = new Body(txs);

        Header header = new Header(VERSION, index, previousHash, fixedTimestamp, nonce, difficulty);

        Block genesis = new Block(header, body);

        System.out.println("⛏️ Genesis generado con hash: " + genesis.getHash() + " y merkle root: " + genesis.getHeader().getMerkleRoot());
        System.out.println("⛏️ Coinbase TX ID: " + coinbaseTransaction.getTransactionId());
        return genesis;
    }

    private static void mineGenesis(Block block, int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        while(!block.calculateHash().startsWith(target)) {
            block.getHeader().setNonce(block.getHeader().getNonce() + 1);
        }
    }

    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block current = chain.get(i);
            Block previous = chain.get(i - 1);

            if (!current.getHash().equals(current.calculateHash())) {
                System.out.println("❌ El bloque " + i + " ha sido modificado.");
                return false;
            }

            if (!current.getHeader().getPreviousHash().equals(previous.getHash())) {
                System.out.println("❌ El bloque " + i + " no apunta al bloque anterior.");
                return false;
            }

            int difficulty = current.getHeader().getDifficulty();

            String target = new String(new char[difficulty]).replace('\0', '0');

            if (!current.getHash().startsWith(target)) {
                System.out.println("❌ El bloque " + i + " no ha sido minado correctamente.");
                System.out.println("   Requerido: " + target);
                System.out.println("   Obtenido:  " + current.getHash());
                return false;
            }
        }
        return true;
    }

    public List<Block> getChain() {
        return chain;
    }
}
