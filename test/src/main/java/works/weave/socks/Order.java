package works.weave.socks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by Istvan Meszaros on 10/22/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {

    // TODO improve based on Server Response from /Orders

    private String id;
    private String order;
    private Date date;
    private Integer total;
    private String status;



    public Order(String order, Date date, Integer total, String status) {
        this.order = order;
        this.date = date;
        this.total = total;
        this.status = status;
    }

    @JsonCreator
    public Order(@JsonProperty("id") String id,
                 @JsonProperty("order") String order,
                 @JsonProperty("date") Date date,
                 @JsonProperty("total") Integer total,
                 @JsonProperty("status") String status) {
        this.id = id;
        this.order = order;
        this.date = date;
        this.total = total;
        this.status = status;
    }


    public String getId() {
        return id;
    }

    public String getOrder() {
        return order;
    }

    public Date getDate() {
        return date;
    }

    public Integer getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }
}
