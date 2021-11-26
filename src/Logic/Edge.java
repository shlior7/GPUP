package Logic;

public class Edge {
    String out, in;

    public Edge(String depends, String required) {
        this.out = depends;
        this.in = required;
    }

    public Edge(String in, String out, boolean dependsOrRequired) {
        this(dependsOrRequired ? in : out, dependsOrRequired ? out : in);
    }

    public String getIn(){
        return in;
    }

    public String getOut(){
        return out;
    }
}