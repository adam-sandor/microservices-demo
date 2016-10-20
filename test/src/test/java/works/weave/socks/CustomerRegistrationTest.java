package works.weave.socks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SocksShopTestConfig.class)
public class CustomerRegistrationTest {

    @Autowired
    private SocksShop socksShop;

    @Test
    public void testCreateNewUserThenDeleteIt() {
        socksShop.frontEnd()
                .register("testcustomer", "pwd", "Test", "Customer")
                .login("testcustomer", "pwd")
                .verifyCustomer("testcustomer", c -> {
                    assertThat(c.getFirstName(), is("Test"));
                    assertThat(c.getLastName(), is("Customer"));
                })
                .deleteUser("testcustomer")
                .endFrontEnd();
    }
}

