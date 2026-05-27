package es.us.dad.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

import java.nio.file.Files;
import java.nio.file.Paths;

public class BituscoinLauncher {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("❌ Uso: java BituscoinLauncher <ruta_fichero_config.json>");
            System.err.println("   Ejemplo: java BituscoinLauncher conf/node1.json");
            System.exit(1);
        }

        String configFilePath = args[0];

        Vertx vertx = Vertx.vertx();

        try {
            String fileContent = new String(Files.readAllBytes(Paths.get(configFilePath)));
            JsonObject config = new JsonObject(fileContent);

            System.out.println("📂 Cargando configuración desde: " + configFilePath);

            DeploymentOptions options = new DeploymentOptions().setConfig(config);

            vertx.deployVerticle(new MainVerticle(), options).onComplete(res -> {
                if (res.succeeded()) {
                    System.out.println("✅ MainVerticle desplegado ID: " + res.result());
                } else {
                    System.err.println("❌ Fallo en despliegue: " + res.cause());
                    vertx.close();
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Error leyendo el fichero de configuración: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
