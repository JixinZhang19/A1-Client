package api;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-02
 */
public class SkierThreadLocal<T> extends ThreadLocal<T> {

    @Override
    protected T initialValue() {
        return (T) new ApiClient();
    }

    @Override
    public void remove() {
        // Remove ThreadLocal once the thread completes its task
        T value = get();
        if (value instanceof AutoCloseable) {
            try {
                ((AutoCloseable) value).close();
            } catch (Exception e) {
                System.out.println("[SEVERE] Error removing ThreadLocal: " + e.getMessage());
            }
        }
        super.remove();
    }

}
