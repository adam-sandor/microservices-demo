package works.weave.socks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import io.fabric8.kubernetes.api.model.EndpointSubset;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.io.FileUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
public class SocksShopTestConfig {

    private static Logger log = LoggerFactory.getLogger(SocksShopTestConfig.class);

    static {
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Unirest.setHttpClient(org.apache.http.impl.client.HttpClients.custom()
                .setDefaultCookieStore(new BasicCookieStore())
                .setDefaultCookieSpecRegistry(name -> new RFC6265CookieSpecProvider())
                .build());
    }

    private String ns = "socks";

    private boolean stopApplication = true;

    @Bean
    public KubernetesClient kubernetesClient() {
        Config config = new ConfigBuilder().withMasterUrl("https://192.168.99.100:8443").build();
        return new DefaultKubernetesClient(config);
    }

    @Bean
    public SocksShop socksShop(KubernetesClient client) throws IOException {

        if (client.namespaces().withName(ns).get() == null) {
            log.info("Creating namespace 'socks' and starting application");
            client.namespaces().createNew().withNewMetadata().withName(ns).endMetadata().done();

            String completeDemoYml = FileUtils.readFileToString(new File("/Users/asandor/Projects/cs/socksshop/microservices-demo/deploy/kubernetes/complete-demo.yaml"), "UTF-8");
            String[] components = completeDemoYml.split("---");

            log.info("Starting application");
            for (String component : components) {
                client.load(new ByteInputStream(component.getBytes("UTF-8"), component.length()))
                        .inNamespace(ns)
                        .apply();
            }

            log.info("Waiting for services endpoints to become available");
            Awaitility.await().atMost(4, TimeUnit.MINUTES)
                    .until(() -> {
                        for (String service : Arrays.asList("front-end", "catalogue", "catalogue-db", "user", "user-db", "cart", "cart-db")) {
                            Endpoints endpoints = client.endpoints().inNamespace(ns).withName(service).get();
                            boolean running = !(endpoints == null || endpoints.getSubsets().isEmpty()
                                    || endpoints.getSubsets().get(0).getAddresses().isEmpty()
                                    || endpoints.getSubsets().get(0).getAddresses().get(0).getIp() == null);
                            if (!running) {
                                return false;
                            }
                        }
                        return true;
                    });
        } else {
            log.info("Application already running (namespace 'socks' exists)");
            stopApplication = false; //dont destroy cluster if test didn't start it
        }

        Endpoints endpoints = client.endpoints().inNamespace(ns).withName("front-end").get();
        EndpointSubset subset = endpoints.getSubsets().get(0);
        String address = "http://" + subset.getAddresses().get(0).getIp() + ":" + subset.getPorts().get(0).getPort();

        SocksShop socksShop = new SocksShop(address);
        log.info("SocksShop running. FrontEnd URL is {}", address);
        return socksShop;
    }

    @Autowired
    private KubernetesClient client;

    @PreDestroy
    public void stopApplication() {
        if (stopApplication) {
            log.info("Stopping application");

            client.namespaces().withName(ns).delete();
        }
    }
}
