package works.weave.socks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Istvan Meszaros on 10/22/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {

    private String id;

    private String itemId;

    private Integer quantity;

    private Integer unitPrice;

    public Item(String itemId, Integer quantity, Integer unitPrice) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }


    public Item(String itemId) {
        this.itemId = itemId;
    }

    @JsonCreator
    public Item(@JsonProperty("id") String id,
                @JsonProperty("itemId") String itemId,
                @JsonProperty("quantity") Integer quantity,
                @JsonProperty("unitprice") Integer unitPrice) {
        this.id = id;
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getId() {
        return id;
    }

    public String getItemId() {
        return itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getUnitPrice() {
        return unitPrice;
    }
}
