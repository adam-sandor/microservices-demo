package works.weave.socks;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SocksShopTestConfig.class)
public class ShoppingTest {

    @Autowired
    private SocksShop socksShop;

//    @BeforeClass
//
//
//    @Test
//    public void testBuySomeSocks() {
//        socksShop.frontEnd()
//                .register("testcustomer", "pwd", "Test", "Customer")
//                .login("testcustomer", "pwd")
//                .verifyCustomer(c -> {
//                    assertThat(c.getFirstName(), is("Test"));
//                    assertThat(c.getLastName(), is("Customer"));
//                })
//                .endFrontEnd();
//    }
}

