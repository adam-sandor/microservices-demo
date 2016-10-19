package works.weave.socks;

@FunctionalInterface
public interface VerifyObject<T> {

    void verify(T object) throws AssertionError;
}
