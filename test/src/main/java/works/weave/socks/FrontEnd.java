package works.weave.socks;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class FrontEnd {

    public static final String USER_ALREADY_EXISTS_ERROR = "E11000";
    public static final String ERROR_KEY = "error";

    public static final String ENDPOINT_CART = "/cart";
    public static final String ENDPOINT_CUSTOMER = "/customers";
    public static final String ENDPOINT_ORDERS = "/orders";
    public static final String ENDPOINT_REGISTER = "/register";
    public static final String ENDPOINT_LOGIN = "/login";
    public static final String CONTENT_TYPE_JSON = "application/json";

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
            HttpResponse<JsonNode> response = Unirest.post(url + ENDPOINT_REGISTER)
                    .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .body(new Customer(username, password, firstName, lastName))
                    .asJson();
            JSONObject respObject = response.getBody().getObject();
            if (response.getStatus() == HttpStatus.SC_OK && respObject.has(ERROR_KEY) &&
                    respObject.getString(ERROR_KEY).contains(USER_ALREADY_EXISTS_ERROR)) {
                log.info("User {} already registered, ignoring", username);
            } else if (response.getStatus() != HttpStatus.SC_OK || respObject.has(ERROR_KEY)) {
                throw new AssertionError("Registering user '" + username + "' not successful. Received status " +
                        response.getStatus() + ", body '" + response.getBody() + "'");
            } else {
                log.info("User {} registered", username);
            }
        }));
        return this;
    }

    public FrontEnd verifyCustomer(String username, VerifyObject<Customer> verifyObject) {
        Awaitility.await("verifying customer '" + username + "'").atMost(1, TimeUnit.MINUTES).until(Waiter.wait(() -> {
            if (username.equals(state.getLoggedInUsername())) {
                Customer customer = Unirest.get(url + ENDPOINT_CUSTOMER + "/" + state.getLoggedInUserId()).asObject(Customer.class).getBody();
                verifyObject.verify(customer);
            } else {
                throw new IllegalStateException("Only verification of logged in user is supported");
                //TODO implement lookup of user id from username
            }
        }));
        return this;
    }

    public SocksShop endFrontEnd() {
        return parent;
    }

    public FrontEnd login(String username, String password) {
        log.info("Waiting for successful login");
        Awaitility.await("logging in to front-end").atMost(4, TimeUnit.MINUTES).until(Waiter.wait(() -> {
            HttpResponse<String> response = Unirest.get(url + ENDPOINT_LOGIN)
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

    public FrontEnd deleteUser(String username) {
        Awaitility.await("Deleting user '" + username + "'").atMost(1, TimeUnit.MINUTES).until(Waiter.wait(() -> {
            if (username.equals(state.getLoggedInUsername())) {
                HttpResponse<String> response = Unirest.delete(url + ENDPOINT_CUSTOMER + "/" + state.getLoggedInUserId()).asString();
                if (response.getStatus() != HttpStatus.SC_OK || response.getBody().contains(ERROR_KEY)) {
                    throw new AssertionError("Deleting user '" + username + "' not successful. Received status " +
                            response.getStatus() + ", body '" + response.getBody() + "'");
                } else {
                    log.info("User {} deleted", username);
                }
            } else {
                throw new IllegalStateException("Only deleting of logged in user is supported");
                //TODO implement lookup of user id from username
            }
        }));
        return this;
    }

    public FrontEnd verifyCartItems(VerifyObject<Item[]> verifyObject) {
        Awaitility.await("verifying cart items").atMost(1, TimeUnit.MINUTES).until(Waiter.wait(() -> {
                    HttpResponse<Item[]> response = Unirest.get(url + ENDPOINT_CART).asObject(Item[].class);
                    if (response.getStatus() != HttpStatus.SC_OK) {
                        throw new AssertionError("Couldn't get shopping cart content" +
                                response.getStatus() + ", body '" + response.getBody() + "'");
                    }
                    verifyObject.verify(response.getBody());
                }
        ));
        return this;
    }

    public FrontEnd emptyShoppingCart() {
        Awaitility.await("emptying shopping cart").atMost(1, TimeUnit.MINUTES).until(Waiter.wait(() -> {
            HttpResponse<String> response = Unirest.delete(url + ENDPOINT_CART).asString();
            if (response.getStatus() != HttpStatus.SC_ACCEPTED || response.getBody().contains(ERROR_KEY)) {
                throw new AssertionError("Emptying cart not successful. Received status " +
                        response.getStatus() + ", body '" + response.getBody() + "'");
            } else {
                log.info("Emptied shopping cart");
            }
        }));
        return this;
    }

    public FrontEnd addItemToShoppingCart(String id) {
        Awaitility.await("verifying item '" + id + "'").atMost(1, TimeUnit.MINUTES).until(Waiter.wait(() -> {
            HttpResponse<String> response = Unirest.post(url + ENDPOINT_CART).
                    header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).
                    body(getAddCartItemJson(id)).asString();
            if (response.getStatus() != HttpStatus.SC_CREATED || response.getBody().contains(ERROR_KEY)) {
                throw new AssertionError("Adding to cart '" + id + "' not successful. Received status " +
                        response.getStatus() + ", body '" + response.getBody() + "'");
            } else {
                log.info("Item '" + id + "'added to shopping cart");
            }
        }));
        return this;
    }

    public FrontEnd orderCartContent() {
        Awaitility.await("Order cart content").atMost(1, TimeUnit.MINUTES).until(Waiter.wait(() -> {
            if (state.getLoggedInUserId() != null) {
                HttpResponse<String> response = Unirest.post(url + ENDPOINT_ORDERS).asString();
                if (response.getStatus() != HttpStatus.SC_ACCEPTED || response.getBody().contains(ERROR_KEY)) {
                    throw new AssertionError("Order cart content not successful. Received status " +
                            response.getStatus() + ", body '" + response.getBody() + "'");
                } else {
                    log.info("Ordered cart content");
                }
            }
        }));
        return this;
    }


    public FrontEnd verifyOrders(VerifyObject<Order[]> verifyObject) {
        Awaitility.await("verifying cart items").atMost(1, TimeUnit.MINUTES).until(Waiter.wait(() -> {
                    HttpResponse<Order[]> response = Unirest.get(url + ENDPOINT_ORDERS).asObject(Order[].class);
                    if (response.getStatus() != HttpStatus.SC_OK) {
                        throw new AssertionError("Couldn't get shopping cart content" +
                                response.getStatus() + ", body '" + response.getBody() + "'");
                    }
                    verifyObject.verify(response.getBody());
                }
        ));
        return this;
    }

    // The rest API instead of ItemId uses Id,
    // which is incorrect becuase the Item entity already contains an Id property, itemId should be used instead.
    private static String getAddCartItemJson(String id) {
        return "{\"id\" : \"" + id + "\"}";
    }
}
