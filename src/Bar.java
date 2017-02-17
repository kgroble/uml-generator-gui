
public class Bar {
    private final boolean this_is_final = true;
    private Integer i;
    
    public void invalidMethod() {
        "abc".length();
        i.getClass().getName();
    }
}
