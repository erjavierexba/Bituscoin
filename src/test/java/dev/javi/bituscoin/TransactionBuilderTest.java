package dev.javi.bituscoin;

import es.us.dad.vertx.entities.Transaction;
import es.us.dad.vertx.utils.SecurityUtils;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionBuilderTest {

    @Test
    void buildCreatesSignedTransactionWithHashId() {
        KeyPair keyPair = SecurityUtils.generateECKeyPair();
        String sender = SecurityUtils.encodeKey(keyPair.getPublic());

        Transaction transaction = new TransactionBuilder()
                .from(sender)
                .to("receiver")
                .amount(10L)
                .fee(1L)
                .timestamp(123L)
                .signWith(keyPair.getPrivate())
                .build();

        assertEquals(transaction.calculateHash(), transaction.getTransactionId());
        assertTrue(transaction.getTransactionId().matches("[0-9a-f]{64}"));
        assertNotNull(transaction.getSignature());
        assertTrue(transaction.verifySignature());
    }

    @Test
    void amountMustBePositiveBeforeSigning() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                new TransactionBuilder()
                        .from("sender")
                        .to("receiver")
                        .amount(0L)
                        .build());

        assertEquals("amount debe ser mayor que 0", error.getMessage());
    }

    @Test
    void toNetworkJsonIncludesFee() {
        KeyPair keyPair = SecurityUtils.generateECKeyPair();

        Transaction transaction = new TransactionBuilder()
                .from(SecurityUtils.encodeKey(keyPair.getPublic()))
                .to("receiver")
                .amount(10L)
                .fee(2L)
                .signWith(keyPair.getPrivate())
                .build();

        JsonObject networkJson = transaction.toNetworkJson();

        assertEquals(2L, networkJson.getLong("fee"));
        assertEquals(transaction.getTransactionId(), networkJson.getString("transactionId"));
        assertEquals(transaction.getSignature(), networkJson.getString("signature"));
    }
}
