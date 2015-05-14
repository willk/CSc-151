/*
 * Cody Jackson, William Kinderman
 * 7331, 9997
 */


import java.util.*;

import static java.lang.Character.isUpperCase;

public class Main {

    private static Scanner in;
    private static ArrayList<Node> nodes;
    private static ArrayList<String> live;
    private static Node root, end;

    private static Map<String, LinkedList<Node>> labelMap;

    public static void main(String[] args) {
        in = new Scanner(System.in);
        nodes = new ArrayList<Node>();
        live = new ArrayList<String>();
        root = new Node();
        end = new Node();

        labelMap = new HashMap<String, LinkedList<Node>>();

        getNodes();
        pass2();
        liveness(end);

        for (Node node : nodes) {
            System.out.println(node);
        }
    }

    public static void addToLabelMap(String s, Node n) {
        LinkedList<Node> nodeList = labelMap.get(s);

        if (nodeList == null) {
            nodeList = new LinkedList<Node>();
            labelMap.put(s, nodeList);
        }

        nodeList.add(n);
    }

    public static void getNodes() {
        String p_label = null;
        Node p_node = root;

        while (in.hasNextLine()) {

            Node n = new Node();
            String line = in.nextLine().trim();

            if (line.trim().equals("")) break;

            String parts[] = line.split(" ");
            String termporaliy[] = line.substring(1).split(" ");

            if (p_label != null) {
                n.setLabel(p_label);
                p_label = null;
            }

            if (parts[0].equals("#")) {
                end = p_node;
                for (String s : termporaliy) {
                    end.addAfter(s);
                }
                break;
            } else if (isUpperCase(parts[0].charAt(0)) && parts[0].length() > 1) {
                // We're at a label. We've assumed that we should get two labels in a row.
                p_label = parts[0].substring(0, 6);
                continue;
            } else if (parts[0].equals("jump") || parts[0].equals("goto")) {
                // We're at a jump statement.
                n.setStatement(line);
                n.setDestination(parts[1]);
                n.setType("jump");
                n.addPrevious(p_node);
                p_node.addNext(n);
                p_node = n;
                addToLabelMap(n.getDestination(), n);
//                continue;
            } else if (parts[0].equals("if")) {
                // We're at an if statement.
                n.setStatement(line);
                n.setDestination(parts[5]);
                n.setType("if");
                //System.out.println("Previous node is " + p_node);
                n.addPrevious(p_node);
                p_node.addNext(n);
                p_node = n;
                addToLabelMap(n.getDestination(), n);
            } else if (parts[0].equals("push")) {
                // We've seen a push.
                n.setStatement(line);
                n.setType("push");
            } else if (parts[0].length() == 1) {
                // We've seen a var/ident/something not up there.
                n.setStatement(line);
                n.addPrevious(p_node);
                p_node.addNext(n);
                p_node = n;
                n.setType("statement");
            }

            nodes.add(n);
        }
    }


    public static boolean parseIf(Node n) {
        boolean bob = true;
        String before = n.getBefore().toString();
        String statement[] = n.getStatement().split(" ");

        String lhs = statement[1];
        String rhs1 = statement[3];
        String rhs2 = statement[5];

        // rule #3:
//        if (!rhs1.contains(lhs) && Character.isLetter(rhs1.charAt(0))) {
//            n.removeBefore(rhs1);
//            if (rhs2 != null && Character.isLetter(rhs2.charAt(0))) {
//                n.removeBefore(rhs2);
//            }
//        }

        // Rule 1
        for (Node node : n.getNext()) {
            for (String s : node.getBefore()) {
                n.addAfter(s);
            }
        }

        // rule 4
        // death and taxes something something, confuscious
        for (String s : n.getAfter()) {
            n.addBefore(s);
        }

        // rule #2:
        if (lhs != null && Character.isLetter(lhs.charAt(0))) {
            n.addBefore(lhs);
        }

        if (rhs1 != null && Character.isLetter(rhs1.charAt(0))) {
            n.addBefore(rhs1);
        }


        String afterBefore = n.getBefore().toString();

        if (afterBefore.equals(before)) bob = false;
        return bob;
    }


    public static boolean parseStatement(Node n) {
        boolean bob = true;
        String before = n.getBefore().toString();
        String statement[] = n.getStatement().split(" ");

        String lhs = statement[0];
        String rhs1 = statement[2];
        String rhs2 = null;
        if (statement.length > 3) {
            rhs2 = statement[4];
        }

        // Rule 1
        for (Node node : n.getNext()) {
            for (String s : node.getBefore()) {
                n.addAfter(s);
            }
        }

        // rule 4
        // death and taxes something something, confuscious
        for (String s : n.getAfter()) {
            n.addBefore(s);
        }

        // rule #3:
        if (!rhs1.contains(lhs)) {
            n.removeBefore(lhs);
            if (rhs2 != null && !rhs2.contains(lhs)) {
                n.removeBefore(lhs);
            }
        }

        // rule #2:
        if (rhs1 != null && Character.isLetter(rhs1.charAt(0))) {
            n.addBefore(rhs1);
        }

        if (rhs2 != null && Character.isLetter(rhs2.charAt(0))) {
            n.addBefore(rhs2);
        }

        String afterBefore = n.getBefore().toString();

        if (afterBefore.equals(before)) bob = false;

        return bob;
    }

    private static boolean parseJump(Node n) {
        boolean bob = true;
        String before = n.getBefore().toString();

        // Rule 1
        for (Node node : n.getNext()) {
            for (String s : node.getBefore()) {
                n.addAfter(s);
            }
        }

        // rule 4
        // death and taxes something something, confuscious
        for (String s : n.getAfter()) {
            n.addBefore(s);
        }

        String afterBefore = n.getBefore().toString();
        if (afterBefore.equals(before)) bob = false;

        return bob;
    }

    public static void pass2() {
        for (Node node : nodes) {
            if (node.getLabel() != null) {
                // find the nodes that referenced this node
                LinkedList<Node> nodes = labelMap.get(node.getLabel());
                //System.out.println("fouuunddddd " + nodes);

                for (Node otherNode : nodes) {
                    node.addPrevious(otherNode);
                    otherNode.addNext(node);

                    //System.out.println("Added link from " + node + " to " + otherNode);
                    //System.out.println(otherNode.getNext() + "FDSFDSFSDFSDFSD");

                }
            }
        }
        //System.out.println(labelMap);
    }

    public static void liveness(Node n) {
        boolean bob = true;
        n.incVisitCount();
//        if (n != end && n.getVisitCount() != n.getNext().size()) return;

        if (n.getType().equals("statement")) {
            bob = parseStatement(n);
        } else if (n.getType().equals("if")) {
            bob = parseIf(n);
        } else if (n.getType().equals("jump")) {
            bob = parseJump(n);
        }

        for (Node node : n.getPrevious()) {
            if (node != root && (bob || n.getVisitCount() <= 1)) liveness(node);
        }
    }
}
