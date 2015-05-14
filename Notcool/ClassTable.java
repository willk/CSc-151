//import jdk.nashorn.internal.ir.Symbol;
// worked on by Cody Jackson and William Kinderman
// 7331 and 9997

import java.io.PrintStream;
import java.lang.Object;
import java.lang.System;
import java.util.*;

/** This class may be used to contain the semantic information such as
 * the inheritance graph.  You may use it or not as you like: it is only
 * here to provide a container for the supplied methods.  */
class ClassTable {
    

    public SymbolTable objectTable = new SymbolTable();
    public SymbolTable methodTable = new SymbolTable();

    class Node {
        public ArrayList<Node> children;
        public boolean visited;
		public AbstractSymbol name;
		public SymbolTable objectTableClass;
		public class_c _class;

		public Node() {
			objectTableClass = new SymbolTable();
			children = new ArrayList<Node>();
		}
    }


	private Node root;
	private Node idk; //I don't know

	private List<Node> cantTouchThis;


	public boolean isSubtype(AbstractSymbol a, AbstractSymbol b) {
		while (!a.equals(TreeConstants.No_class)) {
	
			if (a.equals(b)) return true;

			Node bob = search(a, root);
			if (bob == null) return false;

			a = bob._class.getParent();
		}

		return false;
	}

	public AbstractSymbol lub(AbstractSymbol a, AbstractSymbol b) {
		if (isSubtype(b, a)) return a;
		if (b.equals(TreeConstants.SELF_TYPE)) return TreeConstants.SELF_TYPE;

		while (!isSubtype(a, b)) {
			b = search(b, root)._class.getParent();
		}
		return b;
	}

	private void insertHammerTime(class_c c) {
		Node t = insert(c);
		cantTouchThis.add(t); // add to list of things we can't inherit from
	}

	private Node insert(class_c c) {
		//print(root);


		// add to method table?
		for (Enumeration e = c.features.getElements(); e.hasMoreElements(); ) {
			Feature f = (Feature)e.nextElement();
			if (f instanceof method) {
				method m = (method)f;
				c.mt.addId(m.name, m);
				//System.out.println("Added :D :D :D + " + m.name);
			}
		}

		//System.out.println(c.mt);

		// If we see this class in root, we're in trouble :( :( sadface
		Node t = search(c.getName(), root);
		if (t != null) {
			semantError(c).println("Class " + c.getName() + " was previously defined.");
		}

		// If it already exists in IDK, we're okay
		t = search(c.getName(), idk);


		if (t != null) {
			idk.children.remove(t);
//			return;
			//System.out.println("Found in IDK list!");
		} else { // ELSE make a new one.
			//System.out.println("NOT found in IDK list!");
			t = new Node();
			t.name = c.getName();

		}

		t._class = c;

		Node parent = search(c.getParent(), root);
		//System.out.println("Searching for " + c.getParent());
		if (parent == null) {
//			System.out.println("I am Batman! (I can't find my parents.)");
//			System.exit(-1);
			//System.out.println("I didn't find parent! (Batmaaaaaan)");
			parent = new Node();
			parent.name = c.getParent();
			idk.children.add(parent);
		}
		parent.children.add(t);
		return t;
	}

	public class_c getClass(AbstractSymbol key) {
		Node n = search(key);
		if (n != null) {
			return n._class;
		}
		return null;
	}

	public Node search(AbstractSymbol key) {
		return search(key, root);
	}

	private Node search(AbstractSymbol key, Node n) {
		if (n == null) return null;
		if (n.name == key) return n;
		for (Node child : n.children) {
			Node t = search(key, child);
			if (t != null) return t;
		}
		return null;
	}

	private void print(Node n) {
		print(n, 0);
	}

	private void print(Node n, int somethingotherthani) {
		for (int i = 0; i < somethingotherthani; i++) System.out.print("  ");

		System.out.println(n.name);
		for (Node child : n.children) {
			print(child, somethingotherthani + 1);
		}
	}

	private boolean checkHasCycle(Node n) {
		if (n.visited) return true;

		n.visited = true;

		boolean cycle = false;
		for (Node child : n.children) {
			cycle = checkHasCycle(child);
		}

		return cycle;
	}
    
    private int semantErrors;
    private PrintStream errorStream;

    /** Creates data structures representing basic Cool classes (Object,
     * IO, Int, Bool, String).  Please note: as is this method does not
     * do anything useful; you will need to edit it to make if do what
     * you want.
     * */
    private void installBasicClasses() {
    AbstractSymbol filename 
        = AbstractTable.stringtable.addString("<basic class>");
    
    // The following demonstrates how to create dummy parse trees to
    // refer to basic Cool classes.  There's no need for method
    // bodies -- these are already built into the runtime system.

    // IMPORTANT: The results of the following expressions are
    // stored in local variables.  You will want to do something
    // with those variables at the end of this method to make this
    // code meaningful.

    // The Object class has no parent class. Its methods are
    //        cool_abort() : Object    aborts the program
    //        type_name() : Str        returns a string representation 
    //                                 of class name
    //        copy() : SELF_TYPE       returns a copy of the object

    class_c Object_class = 
        new class_c(0, 
               TreeConstants.Object_, 
               TreeConstants.No_class,
               new Features(0)
					   .appendElement(new method(0,
							   TreeConstants.cool_abort,
							   new Formals(0),
							   TreeConstants.Object_,
							   new no_expr(0)))
					   .appendElement(new method(0,
							   TreeConstants.type_name,
							   new Formals(0),
							   TreeConstants.Str,
							   new no_expr(0)))
					   .appendElement(new method(0,
							   TreeConstants.copy,
							   new Formals(0),
							   TreeConstants.SELF_TYPE,
							   new no_expr(0))),
               filename);
    
    // The IO class inherits from Object. Its methods are
    //        out_string(Str) : SELF_TYPE  writes a string to the output
    //        out_int(Int) : SELF_TYPE      "    an int    "  "     "
    //        in_string() : Str            reads a string from the input
    //        in_int() : Int                "   an int     "  "     "

    class_c IO_class = 
        new class_c(0,
               TreeConstants.IO,
               TreeConstants.Object_,
               new Features(0)
					   .appendElement(new method(0,
							   TreeConstants.out_string,
							   new Formals(0)
									   .appendElement(new formalc(0,
											   TreeConstants.arg,
											   TreeConstants.Str)),
							   TreeConstants.SELF_TYPE,
							   new no_expr(0)))
					   .appendElement(new method(0,
							   TreeConstants.out_int,
							   new Formals(0)
									   .appendElement(new formalc(0,
											   TreeConstants.arg,
											   TreeConstants.Int)),
							   TreeConstants.SELF_TYPE,
							   new no_expr(0)))
					   .appendElement(new method(0,
							   TreeConstants.in_string,
							   new Formals(0),
							   TreeConstants.Str,
							   new no_expr(0)))
					   .appendElement(new method(0,
							   TreeConstants.in_int,
							   new Formals(0),
							   TreeConstants.Int,
							   new no_expr(0))),
               filename);

    // The Int class has no methods and only a single attribute, the
    // "val" for the integer.

    class_c Int_class = 
        new class_c(0,
               TreeConstants.Int,
               TreeConstants.Object_,
               new Features(0)
					   .appendElement(new attr(0,
							   TreeConstants.val,
							   TreeConstants.prim_slot,
							   new no_expr(0))),
               filename);

    // Bool also has only the "val" slot.
    class_c Bool_class = 
        new class_c(0,
               TreeConstants.Bool,
               TreeConstants.Object_,
               new Features(0)
					   .appendElement(new attr(0,
							   TreeConstants.val,
							   TreeConstants.prim_slot,
							   new no_expr(0))),
               filename);

    // The class Str has a number of slots and operations:
    //       val                              the length of the string
    //       str_field                        the string itself
    //       length() : Int                   returns length of the string
    //       concat(arg: Str) : Str           performs string concatenation
    //       substr(arg: Int, arg2: Int): Str substring selection

    class_c Str_class =
        new class_c(0,
               TreeConstants.Str,
               TreeConstants.Object_,
               new Features(0)
					   .appendElement(new attr(0,
							   TreeConstants.val,
							   TreeConstants.Int,
							   new no_expr(0)))
					   .appendElement(new attr(0,
							   TreeConstants.str_field,
							   TreeConstants.prim_slot,
							   new no_expr(0)))
					   .appendElement(new method(0,
							   TreeConstants.length,
							   new Formals(0),
							   TreeConstants.Int,
							   new no_expr(0)))
					   .appendElement(new method(0,
							   TreeConstants.concat,
							   new Formals(0)
									   .appendElement(new formalc(0,
											   TreeConstants.arg,
											   TreeConstants.Str)),
							   TreeConstants.Str,
							   new no_expr(0)))
					   .appendElement(new method(0,
							   TreeConstants.substr,
							   new Formals(0)
									   .appendElement(new formalc(0,
											   TreeConstants.arg,
											   TreeConstants.Int))
									   .appendElement(new formalc(0,
											   TreeConstants.arg2,
											   TreeConstants.Int)),
							   TreeConstants.Str,
							   new no_expr(0))),
               filename);

    /* Do somethind with Object_class, IO_class, Int_class,
           Bool_class, and Str_class here */

		root = new Node();
		idk = new Node();
		root.name = Object_class.getName();
		root._class = Object_class;

		// add to method table?
		for (Enumeration e = Object_class.features.getElements(); e.hasMoreElements(); ) {
			Feature f = (Feature)e.nextElement();
			if (f instanceof method) {
				method m = (method)f;
				Object_class.mt.addId(m.name, m);
				//System.out.println("Added :D :D :D + " + m.name);
			}
		}


		cantTouchThis = new ArrayList<Node>();
		insert(IO_class);
		insertHammerTime(Int_class);
		insertHammerTime(Bool_class);
		insertHammerTime(Str_class);

		//System.out.println("Notepad sucks regardless " + isSubtype(Object_class.getName(), Int_class.getName()));

    }

    public ClassTable(Classes cls) {
    	semantErrors = 0;
    	errorStream = System.err;

    	installBasicClasses();

		for (Enumeration e = cls.getElements(); e.hasMoreElements(); ) {
			class_c c = (class_c) e.nextElement();
			insert(c);
		}
		//print(root);


		for (Node n : idk.children) {
			//System.out.println("IDK child: " + n.name);
			for (Node brat : n.children) {
				semantError(brat._class).println("Class " + brat._class.getName() + " inherits from an undefined class " + n.name + ".");
			}
		}

		for (Node n : cantTouchThis) {
			for (Node brat : n.children) {
				if (brat != null) {
					semantError(brat._class).println("Class " + brat._class.getName() + " cannot inherit from " + n._class.getName() + ".");

				}
			}
		}
	}

    /** Prints line number and file name of the given class.
     *
     * Also increments semantic error count.
     *
     * @param c the class
     * @return a print stream to which the rest of the error message is
     * to be printed.
     *
     * */
    public PrintStream semantError(class_c c) {
		//System.out.println(c);
    	return semantError(c.getFilename(), c);
    }

    /** Prints the file name and the line number of the given tree node.
     *
     * Also increments semantic error count.
     *
     * @param filename the file name
     * @param t the tree node
     * @return a print stream to which the rest of the error message is
     * to be printed.
     *
     * */
    public PrintStream semantError(AbstractSymbol filename, TreeNode t) {
    	errorStream.print(filename + ":" + t.getLineNumber() + ": ");
    	return semantError();
    }

    /** Increments semantic error count and returns the print stream for
     * error messages.
     *
     * @return a print stream to which the error message is
     * to be printed.
     *
     * */
    public PrintStream semantError() {
    	semantErrors++;
    	return errorStream;
    }

    /** Returns true if there are any static semantic errors. */
    public boolean errors() {
    	return semantErrors != 0;
    }
}
              
    
