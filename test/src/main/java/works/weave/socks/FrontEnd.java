package works.weave.socks;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class FrontEnd {

    private String url;

    private SocksShop parent;

    private State state;

    private Logger log = LoggerFactory.getLogger(FrontEnd.class);

    public FrontEnd(String url, SocksShop parent, State state) {
        this.url = url;
        this.parent = parent;
        this.state = state;
    }

    public FrontEnd register(String username, String password, String firstName, String lastName) {
        Awaitility.await("regitering user " + username).atMost(1, TimeUnit.MINUTES).until(Waiter.wait(() -> {
            HttpResponse<JsonNode> response = Unirest.post(url + "/register")
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(new Customer(username, password, firstName, lastName))
                    .asJson();
            if (response.getStatus() != HttpStatus.SC_OK || response.getBody().getObject().has("error")) {
                throw new AssertionError("Registering user '" + username + "' not successful. Received status " +
                        response.getStatus() + ", body '" + response.getBody() + "'");
            } else {
                log.info("User {} registered", username);
            }
        }));
        return this;
    }

    public FrontEnd verifyLoggedInCustomer(VerifyObject<Customer> verifyObject) {
        Awaitility.await("verifying logged in customer").atMost(1, TimeUnit.MINUTES).until(Waiter.wait(() -> {
            Customer customer = Unirest.get(url + "/customers/" + state.getLoggedInUserId()).asObject(Customer.class).getBody();
            verifyObject.verify(customer);
        }));
        return this;
    }

    public SocksShop endFrontEnd() {
        return parent;
    }

    public FrontEnd login(String username, String password) {
        log.info("Waiting for successful login");
        Awaitility.await("logging in to front-end").atMost(4, TimeUnit.MINUTES).until(Waiter.wait(() -> {
            HttpResponse<String> response = Unirest.get(url + "/login")
                    .basicAuth(username, password)
                    .asString();
            if (response.getStatus() != HttpStatus.SC_OK) {
                throw new AssertionError("Login to front-end not successful. Received status " +
                        response.getStatus() + ", body '" + response.getBody() + "'");
            } else {
                String sessionId = SessionIdParser.parseSessionId(response.getHeaders().get("Set-Cookie").get(0));
                log.info("User {} logged in. SessionId='{}'", username, sessionId);
                state.setLoggedInUsername(username);
                state.setLoggedInUserId(sessionId);
                state.setSessionId(sessionId);
            }
        }));
        return this;
    }

}
