package cc.towerdefence.minestom.kubernetes;

import cc.towerdefence.minestom.MinestomServer;
import io.kubernetes.client.ProtoClient;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class KubernetesManager {
    private static final boolean ENABLED = !MinestomServer.DEV_ENVIRONMENT || System.getenv("ENABLE_K8S_DEV") != null;

    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesManager.class);

    private final ApiClient apiClient;
    private final ProtoClient protoClient;


    public KubernetesManager() {
        try {
            this.apiClient = Config.defaultClient();
            Configuration.setDefaultApiClient(this.apiClient);

            this.protoClient = new ProtoClient(this.apiClient);
        } catch (IOException e) {
            LOGGER.error("Failed to initialise Kubernetes client", e);
            throw new RuntimeException(e);
        }
    }

    public static boolean isEnabled() {
        return ENABLED;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public ProtoClient getProtoClient() {
        return protoClient;
    }
}
