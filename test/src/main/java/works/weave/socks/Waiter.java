package works.weave.socks;

public class Waiter {

    public static Runnable wait(Waitable waitable) {
        return () -> {
            try {
                waitable.run();
            } catch (Exception ex) {
                throw new AssertionError(ex);
            }
        };
    }

    @FunctionalInterface
    interface Waitable {

        void run() throws Exception;
    }
}
