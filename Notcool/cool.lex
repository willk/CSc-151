/*
 * Cody Jackson
 * William Kinderman
 * CSc 151
 * Team Awesome
 * Assignment 1
 * 10 February 2015
 *
 * Resources:
 *  (Not so) Cool manual: http://theory.stanford.edu/~aiken/software/cool/cool-manual.pdf
 *  Jlex Manual: http://www.cs.princeton.edu/~appel/modern/java/JLex/current/manual.html
 *  JFlex examples:
 *   http://users.csc.calpoly.edu/~gfisher/classes/330/examples/jflex/pascal.jflex
 *   http://alumni.cs.ucr.edu/~vladimir/cs152/A2/MiniJava.jflex
 */

import java_cup.runtime.Symbol;

%%

%{

/*  Stuff enclosed in %{ %} is copied verbatim to the lexer class
 *  definition, all the extra variables/functions you want to use in the
 *  lexer actions should go here.  Don't remove or modify anything that
 *  was there initially.  */

    // Max size of string constants
    static int MAX_STR_CONST = 1025;

    // counter for multi-line comments
    private int open_comment_count = 0;

    // counter for string length
    private int string_length = 0;

    // For assembling string constants
    StringBuffer string_buf = new StringBuffer();

    private int curr_lineno = 1;

    int get_curr_lineno() {
        return curr_lineno;
    }

    private AbstractSymbol filename;

    void set_filename(String fname) {
        filename = AbstractTable.stringtable.addString(fname);
    }

    AbstractSymbol curr_filename() {
        return filename;
    }

%}

CLASS           = [cC][lL][aA][sS][sS]
ELSE            = [eE][lL][sS][eE]
FASLE           = f[aA][lL][sS][eE]
FI              = [fF][iI]
IF              = [iI][fF]
IN              = [iI][nN]
INHERITS        = [iI][nN][hH][eE][rR][iI][tT][sS]
ISVOID          = [iI][sS][vV][oO][iI][dD]
LET             = [lL][eE][tT]
LOOP            = [lL][oO][oO][pP]
POOL            = [pP][oO][oO][lL]
WHILE           = [wW][hH][iI][lL][eE]
CASE            = [cC][aA][sS][eE]
ESAC            = [eE][sS][aA][cC]
NEW             = [nN][eE][wW]
OF              = [oO][fF]
NOT             = [nN][oO][tT]
THEN            = [tT][hH][eE][nN]
TRUE            = t[rR][uU][eE]
DIGIT           = [0-9]
ALPHA           = [a-zA-Z]
INTEGER         = {DIGIT}+

WHITESPACE      = [\t\f\r\x0B ]

TYPE_IDENT      = [A-Z]({ALPHA}|{DIGIT}|_)*
OBJECT_IDENT    = [a-z]({ALPHA}|{DIGIT}|_)*


%init{

/*  Stuff enclosed in %init{ %init} is copied verbatim to the lexer
 *  class constructor, all the extra initialization you want to do should
 *  go here.  Don't remove or modify anything that was there initially. */

    // empty for now
%init}

%eofval{

/*  Stuff enclosed in %eofval{ %eofval} specifies java code that is
 *  executed when end-of-file is reached.  If you use multiple lexical
 *  states and want to do something special if an EOF is encountered in
 *  one of those states, place your code in the switch statement.
 *  Ultimately, you should return the EOF symbol, or your lexer won't
 *  work.  */

    switch(yy_lexical_state) {
    case YYINITIAL:
    /* nothing special to do in the initial state */
    break;
    // If necessary, add code for other states here, e.g:
    case NULL:
    case STRING:
        yybegin(YYINITIAL);
        return new Symbol(TokenConstants.ERROR, new String("EOF in string"));
    case COMMENT:
        yybegin(YYINITIAL);
        return new Symbol(TokenConstants.ERROR, new String("EOF in comment"));
    }
    return new Symbol(TokenConstants.EOF);
%eofval}


%class CoolLexer

%cup

%state COMMENT
%state SLCOMMENT
%state STRING
%state NULL
%%

<STRING>\x00 {
    yybegin(NULL);
    return new Symbol(TokenConstants.ERROR,
                      new String("String contains null character."));
}

<NULL>[\n"\""] {
    yybegin(YYINITIAL);
}

<NULL>. {}

<STRING>"\n" {
    /* Replaces new line with a real new line. */
    string_buf.append("\n");
}

<STRING>"\\"" {
    /* looking for \" and replace it with " */
    string_buf.append("\"");
}

<STRING>\\\n {
    /* Needed, not sure why yet. */
    string_buf.append("\n");
}

<STRING>\r {
    /* Keeping carriage returns */
    string_buf.append("\r");
}

<STRING>"\b" {
    /* Keeping \b */
    string_buf.append("\b");
}

<STRING>"\t" {
    /* Replaces \t with real tab. */
    string_buf.append("\t");
}

<STRING>"\f" {
    /* replaces \f with real form feed */
    string_buf.append("\f");
}

<STRING>"\0" {
    /* Replaces \0 with 0 */
    string_buf.append("0");
}

<STRING>\\[^\0] {
    /* Replace \\<anything> with <anything> */
    string_buf.append(yytext().charAt(1));
}

<STRING>\n {
    yybegin(YYINITIAL);
    return new Symbol(TokenConstants.ERROR,
                      new String("Unterminated string constant"));
}

<STRING>"\"" {
    yybegin(YYINITIAL);
    if (string_buf.length() >= MAX_STR_CONST) {
        return new Symbol(TokenConstants.ERROR,
                          new String("String constant too long"));
    } else {
        AbstractTable.stringtable.addString(string_buf.toString());
        return new Symbol(TokenConstants.STR_CONST,
                          AbstractTable.stringtable.lookup(string_buf.toString()));
    }
}

<STRING>. {
    string_buf.append(yytext());
}

<COMMENT, YYINITIAL>"*)" {
    --open_comment_count;
    if (open_comment_count == 0) yybegin(YYINITIAL);
    if (open_comment_count < 0) {
        ++open_comment_count;
        return new Symbol(TokenConstants.ERROR, new String("Unmatched *)"));
    }
}

<COMMENT>[^\n] {
    /* In comment; don't care until we've reached EOL */
}

<COMMENT>\n {
    /* In comment, reached end of line */
    ++curr_lineno;
}

<COMMENT, YYINITIAL>"(*" {
    /* Comment Start */
    yybegin(COMMENT);
    ++open_comment_count;
}

<SLCOMMENT>[^\n]+ {
    /* In single-line comment nom until we reach EOL */
}
<SLCOMMENT>\n {
    /* End of single-line comment, done noming return to YYINITIAL */
    ++curr_lineno;
    yybegin(YYINITIAL);
}


<YYINITIAL>"\"" {
    /* Starting a string */
    string_buf.setLength(0);
    yybegin(STRING);
}

<YYINITIAL>"--" {
    /* Enter into single-line comment mode */
    yybegin(SLCOMMENT);
}

<YYINITIAL>"=>" { return new Symbol(TokenConstants.DARROW); }
<YYINITIAL>"{"  { return new Symbol(TokenConstants.LBRACE); }
<YYINITIAL>"}"  { return new Symbol(TokenConstants.RBRACE); }
<YYINITIAL>"("  { return new Symbol(TokenConstants.LPAREN); }
<YYINITIAL>")"  { return new Symbol(TokenConstants.RPAREN); }
<YYINITIAL>";"  { return new Symbol(TokenConstants.SEMI); }
<YYINITIAL>":"  { return new Symbol(TokenConstants.COLON); }
<YYINITIAL>","  { return new Symbol(TokenConstants.COMMA); }
<YYINITIAL>"."  { return new Symbol(TokenConstants.DOT); }
<YYINITIAL>"+"  { return new Symbol(TokenConstants.PLUS); }
<YYINITIAL>"-"  { return new Symbol(TokenConstants.MINUS); }
<YYINITIAL>"*"  { return new Symbol(TokenConstants.MULT); }
<YYINITIAL>"/"  { return new Symbol(TokenConstants.DIV); }
<YYINITIAL>"<"  { return new Symbol(TokenConstants.LT); }
<YYINITIAL>"<=" { return new Symbol(TokenConstants.LE); }
<YYINITIAL>"~"  { return new Symbol(TokenConstants.NEG); }
<YYINITIAL>"="  { return new Symbol(TokenConstants.EQ); }
<YYINITIAL>"<-" { return new Symbol(TokenConstants.ASSIGN); }
<YYINITIAL>"@"  { return new Symbol(TokenConstants.AT); }


<YYINITIAL>{CLASS}    { return new Symbol(TokenConstants.CLASS); }
<YYINITIAL>{ELSE}     { return new Symbol(TokenConstants.ELSE); }
<YYINITIAL>{FASLE}    { return new Symbol(TokenConstants.BOOL_CONST, false); }
<YYINITIAL>{FI}       { return new Symbol(TokenConstants.FI); }
<YYINITIAL>{IF}       { return new Symbol(TokenConstants.IF); }
<YYINITIAL>{IN}       { return new Symbol(TokenConstants.IN); }
<YYINITIAL>{INHERITS} { return new Symbol(TokenConstants.INHERITS); }
<YYINITIAL>{ISVOID}   { return new Symbol(TokenConstants.ISVOID); }
<YYINITIAL>{LET}      { return new Symbol(TokenConstants.LET); }
<YYINITIAL>{LOOP}     { return new Symbol(TokenConstants.LOOP); }
<YYINITIAL>{POOL}     { return new Symbol(TokenConstants.POOL); }
<YYINITIAL>{WHILE}    { return new Symbol(TokenConstants.WHILE); }
<YYINITIAL>{CASE}     { return new Symbol(TokenConstants.CASE); }
<YYINITIAL>{ESAC}     { return new Symbol(TokenConstants.ESAC); }
<YYINITIAL>{NEW}      { return new Symbol(TokenConstants.NEW); }
<YYINITIAL>{NOT}      { return new Symbol(TokenConstants.NOT); }
<YYINITIAL>{OF}       { return new Symbol(TokenConstants.OF); }
<YYINITIAL>{THEN}     { return new Symbol(TokenConstants.THEN); }
<YYINITIAL>{TRUE}     { return new Symbol(TokenConstants.BOOL_CONST, true); }

<YYINITIAL>{OBJECT_IDENT} {
    /* Objects belong here. */
    AbstractTable.stringtable.addString(yytext());
    return new Symbol(TokenConstants.OBJECTID,
                      AbstractTable.stringtable.lookup(yytext()));
}

<YYINITIAL>{TYPE_IDENT} {
    /* I'm a type! */
    AbstractTable.stringtable.addString(yytext());
    return new Symbol(TokenConstants.TYPEID,
                      AbstractTable.stringtable.lookup(yytext()));
}

<YYINITIAL>{INTEGER} {
    /* Integers FTW */
    AbstractTable.inttable.addString(yytext());
    return new Symbol(TokenConstants.INT_CONST,
                      AbstractTable.inttable.lookup(yytext()));
}

<YYINITIAL>\n {
    /* Reached a newline */
    ++curr_lineno;
}

<YYINITIAL>{WHITESPACE}+ {
    /* Whitspace, not even once. */
}

[^\n] {
    /* Uhh, bad things happened. */
    return new Symbol(TokenConstants.ERROR, yytext());
}
