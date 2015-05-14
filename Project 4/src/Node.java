import java.util.*;
/*
 * Cody Jackson, William Kinderman
 * 7331, 9997
 */
public class Node {
    private SortedSet<String> before, after;
    private String statement, label, destination;
    private HashSet<Node> previous, next;
    private int visitCount;
    private String type;



    public Node() {
        this.before = new TreeSet<String>();
        this.after = new TreeSet<String>();
        this.previous = new HashSet<Node>();
        this.next = new HashSet<Node>();
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public SortedSet<String> getBefore() {
        return before;
    }

    public void setBefore(SortedSet<String> before) {
        this.before = before;
    }

    public SortedSet<String> getAfter() {
        return after;
    }

    public void setAfter(SortedSet<String> after) {
        this.after = after;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Set<Node> getPrevious() {
        return previous;
    }

    public void setPrevious(HashSet<Node> previous) {
        this.previous = previous;
    }

    public Set<Node> getNext() {
        return next;
    }

    public void setNext(HashSet<Node> next) {
        this.next = next;
    }

    public void addPrevious(Node n) {
        previous.add(n);
    }

    public void addNext(Node n) {
        next.add(n);
    }

    public void addAfter(String s) {
        this.after.add(s);
    }

    public void addBefore(String s) {
        this.before.add(s);
    }

    @Override
    public String toString() {
        String t = new String();

        t += "#";

        for (String s : before) {
            t += " " + s;
        }

        t += "\n" + statement + "\n#";

        for (String s : after) {
            t += " " + s;
        }

        return t;
    }

    public void removeBefore(String s) {
        before.remove(s);
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void incVisitCount() {
        this.visitCount++;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
