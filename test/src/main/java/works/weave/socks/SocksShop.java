package works.weave.socks;

public class SocksShop {

    private String frontEndUrl;

    private State state = new State();

    public SocksShop(String frontEndUrl) {
        this.frontEndUrl = frontEndUrl;
    }

    public FrontEnd frontEnd() {
        return new FrontEnd(frontEndUrl, this, state);
    }

}
