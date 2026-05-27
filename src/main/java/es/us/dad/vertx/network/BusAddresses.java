package es.us.dad.vertx.network;

public class BusAddresses {
    public static final String MINED_BLOCK = "internal.mined.block";

    public static final String INTERNAL_NEW_TRANSACTION = "internal.new.transaction";
    public static final String NEW_TRANSACTION = INTERNAL_NEW_TRANSACTION;

    public static final String INCOMING_BLOCK = "p2p.incoming.block";

    public static final String INCOMING_TRANSACTION = "p2p.incoming.transaction";

    public static final String BROADCAST_REQUEST = "p2p.action.broadcast";
}
