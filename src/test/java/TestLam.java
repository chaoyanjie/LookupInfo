/**
 * Created by javaccy on 2017/2/13.
 */
public class TestLam {

    public static void main(String[] args) {
        String name = "aaaaaaaa";
        new Thread(() -> {
            System.out.println(name);
        }).start();
    }
}
