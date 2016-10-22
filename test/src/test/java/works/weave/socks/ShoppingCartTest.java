package works.weave.socks;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Istvan Meszaros on 10/22/16.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SocksShopTestConfig.class)
public class ShoppingCartTest {

    private static final String SOCK_COLOURFUL_ID = "3395a43e-2d88-40de-b95f-e00e1502085b";
    private static final String USER = "testcustomer";
    private static final String PASSWORD = "pwd";

    @Autowired
    private SocksShop socksShop;

    @Before
    public void init() {
        socksShop.frontEnd().emptyShoppingCart().endFrontEnd();
    }


    @Test
    public void startWithEmptyShoppingCart() {
        socksShop.frontEnd().
                verifyCartItems(items -> {
                    assertThat(items.length, is(0));
                }).
                endFrontEnd();
    }


    @Test
    public void addSockToShoppingCart() {
        socksShop.frontEnd().
                addItemToShoppingCart(SOCK_COLOURFUL_ID).
                verifyCartItems(items -> {
                    assertThat(items.length, is(1));
                    assertThat(items[0].getItemId(), is(SOCK_COLOURFUL_ID));
                }).
                endFrontEnd();
    }

    @Test
    public void addSockAndEmptyShoppingCart_shouldResultInEmptyShoppingCart() {
        socksShop.frontEnd().
                addItemToShoppingCart(SOCK_COLOURFUL_ID).
                emptyShoppingCart().
                verifyCartItems(items -> {
                    assertThat(items.length, is(0));
                }).
                endFrontEnd();
    }

    @Test
    public void add2SockToShoppingCart_IncreasedQuantity() {
        socksShop.frontEnd().
                addItemToShoppingCart(SOCK_COLOURFUL_ID).
                addItemToShoppingCart(SOCK_COLOURFUL_ID).
                verifyCartItems(items -> {
                    assertThat(items.length, is(1));
                    Item sock = items[0];
                    assertThat(sock.getItemId(), is(SOCK_COLOURFUL_ID));
                    assertThat(sock.getQuantity(), is(2));
                }).
                endFrontEnd();
    }

    @Test
    public void orderContentWithoutLogin_shouldNotAddOrders() {
        socksShop.frontEnd().
                addItemToShoppingCart(SOCK_COLOURFUL_ID).
                orderCartContent().
                verifyOrders(orders -> {
                            assertThat(orders.length, is(0));
                        }
                ).
                endFrontEnd();
    }

    @Test
    public void orderContentWithLogin_shouldEmptyCartAndSendOrders() {
        socksShop.frontEnd().
                register(USER, PASSWORD, "John", "Doe").
                login(USER, PASSWORD).
                addItemToShoppingCart(SOCK_COLOURFUL_ID).
                orderCartContent().
                verifyOrders(orders -> {
                            assertThat(orders.length, is(1));
                            assertThat(orders[0].getTotal(), is(18));
                            assertThat(orders[0].getStatus(), is("Shipped"));
                        }
                ).
                verifyCartItems(items -> {
                            assertThat(items.length, is(0));
                        }
                ).
                deleteUser(USER).
                endFrontEnd();
    }

}

