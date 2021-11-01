import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        Target t1 = new Target("a");
        Target t2 = new Target("b");
        Target t3 = new Target("c");
        Target t4 = new Target("d");
        t1.DependsOn(t2);
        t2.DependsOn(t3);

        System.out.println("1 = " + t1.MyType());
        System.out.println("2 = " + t2.MyType());
        System.out.println("3 = " + t3.MyType());
        System.out.println("4 = " + t4.MyType());
    }
}
