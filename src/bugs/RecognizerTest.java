package bugs;

import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for Bugs recognizer.
 * 
 * @author David Matuszek and James Park
 */
public class RecognizerTest {

	Recognizer r0, r1, r2, r3, r4, r5, r6, r7, r8;

	/**
	 * Constructor for RecognizerTest.
	 */
	public RecognizerTest() {
		r0 = new Recognizer("2 + 2");
		r1 = new Recognizer("");
	}

	@Before
	public void setUp() throws Exception {
		r0 = new Recognizer("");
		r1 = new Recognizer("250");
		r2 = new Recognizer("hello");
		r3 = new Recognizer("(xyz + 3)");
		r4 = new Recognizer("12 * 5 - 3 * 4 / 6 + 8");
		r5 = new Recognizer("12 * ((5 - 3) * 4) / 6 + (8)");
		r6 = new Recognizer("17 +");
		r7 = new Recognizer("22 *");
		r8 = new Recognizer("#");
	}

	@Test
	public void testRecognizer() {
		r0 = new Recognizer("");
		r1 = new Recognizer("2 + 2");
	}

	@Test
	public void testIsArithmeticExpression() {
		assertTrue(r1.isArithmeticExpression());
		assertTrue(r2.isArithmeticExpression());
		assertTrue(r3.isArithmeticExpression());
		assertTrue(r4.isArithmeticExpression());
		assertTrue(r5.isArithmeticExpression());

		assertFalse(r0.isArithmeticExpression());
		assertFalse(r8.isArithmeticExpression());

		try {
			assertFalse(r6.isArithmeticExpression());
			fail();
		} catch (SyntaxException e) {
		}
		try {
			assertFalse(r7.isArithmeticExpression());
			fail();
		} catch (SyntaxException e) {
		}
	}

	@Test
	public void testIsArithmeticExpressionWithUnaryMinus() {
		assertTrue(new Recognizer("-5").isArithmeticExpression());
		assertTrue(new Recognizer("12+(-5*10)").isArithmeticExpression());
		assertTrue(new Recognizer("+5").isArithmeticExpression());
		assertTrue(new Recognizer("12+(+5*10)").isArithmeticExpression());
	}

	@Test
	public void testIsTerm() {
		assertFalse(r0.isTerm()); // ""

		assertTrue(r1.isTerm()); // "250"

		assertTrue(r2.isTerm()); // "hello"

		assertTrue(r3.isTerm()); // "(xyz + 3)"
		followedBy(r3, "");

		assertTrue(r4.isTerm()); // "12 * 5 - 3 * 4 / 6 + 8"
		assertEquals(new Token(Token.Type.SYMBOL, "-"), r4.nextToken());
		assertTrue(r4.isTerm());
		followedBy(r4, "+ 8");

		assertTrue(r5.isTerm()); // "12 * ((5 - 3) * 4) / 6 + (8)"
		assertEquals(new Token(Token.Type.SYMBOL, "+"), r5.nextToken());
		assertTrue(r5.isTerm());
		followedBy(r5, "");
	}

	@Test
	public void testIsFactor() {
		assertTrue(r1.isFactor());
		assertTrue(r2.isFactor());
		assertTrue(r3.isFactor());
		assertTrue(r4.isFactor());
		followedBy(r4, "* 5 - 3 * 4 / 6 + 8");
		assertTrue(r5.isFactor());
		followedBy(r5, "* ((5");
		assertTrue(r6.isFactor());
		followedBy(r6, "+");
		assertTrue(r7.isFactor());
		followedBy(r7, "*");

		assertFalse(r0.isFactor());
		assertFalse(r8.isFactor());
		followedBy(r8, "#");

		Recognizer r = new Recognizer("foo()");
		assertTrue(r.isFactor());
		r = new Recognizer("bar(5, abc, 2+3)+");
		assertTrue(r.isFactor());
		followedBy(r, "+");

		r = new Recognizer("foo.bar$");
		assertTrue(r.isFactor());
		followedBy(r, "$");

		r = new Recognizer("123.123");
		assertEquals(new Token(Token.Type.NUMBER, "123.123"), r.nextToken());

		r = new Recognizer("5");
		assertEquals(new Token(Token.Type.NUMBER, "5.0"), r.nextToken());
	}

	@Test
	public void testIsParameterList() {
		Recognizer r = new Recognizer("() $");
		assertTrue(r.isParameterList());
		followedBy(r, "$");
		r = new Recognizer("(5) $");
		assertTrue(r.isParameterList());
		followedBy(r, "$");
		r = new Recognizer("(bar, x+3) $");
		assertTrue(r.isParameterList());
		followedBy(r, "$");
	}

	@Test
	public void testIsAddOperator() {
		Recognizer r = new Recognizer("+ - $");
		assertTrue(r.isAddOperator());
		assertTrue(r.isAddOperator());
		assertFalse(r.isAddOperator());
		followedBy(r, "$");
	}

	@Test
	public void testIsMultiplyOperator() {
		Recognizer r = new Recognizer("* / $");
		assertTrue(r.isMultiplyOperator());
		assertTrue(r.isMultiplyOperator());
		assertFalse(r.isMultiplyOperator());
		followedBy(r, "$");
	}

	@Test
	public void testIsVariable() {
		Recognizer r = new Recognizer("foo 23 bar +");
		assertTrue(r.isVariable());

		assertFalse(r.isVariable());
		assertTrue(r.isFactor());

		assertTrue(r.isVariable());

		assertFalse(r.isVariable());
		assertTrue(r.isAddOperator());
	}

	@Test
	public void testSymbol() {
		Recognizer r = new Recognizer("++");
		assertEquals(new Token(Token.Type.SYMBOL, "+"), r.nextToken());
	}

	@Test
	public void testNextTokenMatchesType() {
		Recognizer r = new Recognizer("++abc");
		assertTrue(r.nextTokenMatches(Token.Type.SYMBOL));
		assertFalse(r.nextTokenMatches(Token.Type.NAME));
		assertTrue(r.nextTokenMatches(Token.Type.SYMBOL));
		assertTrue(r.nextTokenMatches(Token.Type.NAME));
	}

	@Test
	public void testNextTokenMatchesTypeString() {
		Recognizer r = new Recognizer("+abc+");
		assertTrue(r.nextTokenMatches(Token.Type.SYMBOL, "+"));
		assertTrue(r.nextTokenMatches(Token.Type.NAME, "abc"));
		assertFalse(r.nextTokenMatches(Token.Type.SYMBOL, "*"));
		assertTrue(r.nextTokenMatches(Token.Type.SYMBOL, "+"));
	}

	@Test
	public void testNextToken() {
		// NAME, KEYWORD, NUMBER, SYMBOL, EOL, EOF };
		Recognizer r = new Recognizer("abc move 25 *\n");
		assertEquals(new Token(Token.Type.NAME, "abc"), r.nextToken());
		assertEquals(new Token(Token.Type.KEYWORD, "move"), r.nextToken());
		assertEquals(new Token(Token.Type.NUMBER, "25.0"), r.nextToken());
		assertEquals(new Token(Token.Type.SYMBOL, "*"), r.nextToken());
		assertEquals(new Token(Token.Type.EOL, "\n"), r.nextToken());
		assertEquals(new Token(Token.Type.EOF, "EOF"), r.nextToken());

		r = new Recognizer("foo.bar 123.456");
		assertEquals(new Token(Token.Type.NAME, "foo"), r.nextToken());
		assertEquals(new Token(Token.Type.SYMBOL, "."), r.nextToken());
		assertEquals(new Token(Token.Type.NAME, "bar"), r.nextToken());
		assertEquals(new Token(Token.Type.NUMBER, "123.456"), r.nextToken());
	}

	@Test
	public void testPushBack() {
		Recognizer r = new Recognizer("abc 25");
		assertEquals(new Token(Token.Type.NAME, "abc"), r.nextToken());
		r.pushBack();
		assertEquals(new Token(Token.Type.NAME, "abc"), r.nextToken());
		assertEquals(new Token(Token.Type.NUMBER, "25.0"), r.nextToken());
	}

	// ----- "Helper" methods

	/**
	 * This method is given a String containing some or all of the tokens that
	 * should yet be returned by the Tokenizer, and tests whether the Tokenizer
	 * in fact has those Tokens. To succeed, everything in the given String must
	 * still be in the Tokenizer, but there may be additional (untested) Tokens
	 * to be returned. This method is primarily to test whether rejected Tokens
	 * are pushed back appropriately.
	 * 
	 * @param recognizer
	 *            The Recognizer whose Tokenizer is to be tested.
	 * @param expectedTokens
	 *            The Tokens we expect to get from the Tokenizer.
	 */
	private void followedBy(Recognizer recognizer, String expectedTokens) {
		int expectedType;
		int actualType;
		StreamTokenizer actual = recognizer.tokenizer;

		Reader reader = new StringReader(expectedTokens);
		StreamTokenizer expected = new StreamTokenizer(reader);
		expected.ordinaryChar('-');
		expected.ordinaryChar('/');

		try {
			while (true) {
				expectedType = expected.nextToken();
				if (expectedType == StreamTokenizer.TT_EOF)
					break;
				actualType = actual.nextToken();
				assertEquals(expectedType, actualType);
				if (actualType == StreamTokenizer.TT_WORD) {
					assertEquals(expected.sval, actual.sval);
				} else if (actualType == StreamTokenizer.TT_NUMBER) {
					assertEquals(expected.nval, actual.nval, 0.001);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// --- continuation of test methods

	@Test
	public void testIsAction() {
		Recognizer r1 = new Recognizer("move 5 + 1 = 6 \n");
		assertTrue(r1.isAction());
		r2 = new Recognizer("move 17 - 8 \n");
		assertTrue(r2.isAction());
		r3 = new Recognizer("don't 28 \n");
		assertFalse(r3.isAction());

		r1 = new Recognizer("moveto (5 + 1 = 6) , (48) \n");
		assertTrue(r1.isAction());
		r2 = new Recognizer("moveto 17 - 8, 3 + 38 > 900 \n");
		assertTrue(r2.isAction());
		r3 = new Recognizer("move 28 , 328 \n");
		try {
			assertFalse(r3.isAction());
			fail();
		} catch (SyntaxException e) {
		}

		r1 = new Recognizer("turn (5 + 1 = 6) \n");
		assertTrue(r1.isAction());
		r2 = new Recognizer("turn 3 + 38 > 900 \n");
		assertTrue(r2.isAction());
		r3 = new Recognizer("turn \n");
		try {
			assertFalse(r3.isAction());
			fail();
		} catch (SyntaxException e) {
		}

		r1 = new Recognizer("turnto (5 + 1 = 6) \n");
		assertTrue(r1.isAction());
		r2 = new Recognizer("turnto 3 + 38 > 900 \n");
		assertTrue(r2.isAction());
		r3 = new Recognizer("turnto 28 > 2 , 328, 890 \n");
		try {
			assertFalse(r3.isAction());
			fail();
		} catch (SyntaxException e) {
		}

		r1 = new Recognizer(
				"line (9) , (900 - 900) , (5 + 1 = 6) , (48 >= 9) \n");
		assertTrue(r1.isAction());
		r2 = new Recognizer("line 17 - 8, 3 + 38 > 900, 39, 0 \n");
		assertTrue(r2.isAction());
		r3 = new Recognizer("line 28 , 328 \n");
		try {
			assertFalse(r3.isAction());
			fail();
		} catch (SyntaxException e) {
		}
		r4 = new Recognizer("line 28 ,  \n");
		try {
			assertFalse(r4.isAction());
			fail();
		} catch (SyntaxException e) {
		}
		r5 = new Recognizer("line , , , , \n");
		try {
			assertFalse(r5.isAction());
			fail();
		} catch (SyntaxException e) {
		}
		r6 = new Recognizer("line 78, 39, 29 = 9 \n");
		try {
			assertFalse(r6.isAction());
			fail();
		} catch (SyntaxException e) {
		}
	}

	@Test
	public void testIsAllbugsCode() {
		/*    	
    	<allbugs code> ::= "Allbugs"  "{" <eol>
        { <var declaration> }
        { <function definition> }
    	"}" <eol>
		 */
		
		r1 = new Recognizer("Allbugs { \n } \n");
		r2 = new Recognizer("Allbugs { \n var foo, bar\n } \n");
		r3 = new Recognizer("Allbugs { \n define bar using rue, dew {\n exit if 3 + 38 > 900 \n } \n} \n");
		r4 = new Recognizer("Allbugs { \n var foo, bar\n define bar using rue, dew {\n exit if 3 + 38 > 900 \n } \n} \n");
		assertTrue(r1.isAllbugsCode());
		assertTrue(r2.isAllbugsCode());
		assertTrue(r3.isAllbugsCode());
		assertTrue(r4.isAllbugsCode());
	}

	@Test
	public void testIsAssignmentStatement() {
		r1 = new Recognizer("foo = (5 + 1 = 6)\n");
		r2 = new Recognizer("foo = 9 > 2 \n");
		r3 = new Recognizer("bar = (5) \n");
		r4 = new Recognizer("rue = (x+3)\n");
		assertTrue(r1.isAssignmentStatement());
		assertTrue(r2.isAssignmentStatement());
		assertTrue(r3.isAssignmentStatement());
		assertTrue(r4.isAssignmentStatement());
		r5 = new Recognizer("rue == (5), (bar, x+3)\n");
		try {
			assertFalse(r5.isAssignmentStatement());
			fail();
		} catch (SyntaxException e) {
		}
		r6 = new Recognizer("boo = \n");
		try {
			assertFalse(r6.isAssignmentStatement());
			fail();
		} catch (SyntaxException e) {
		}
	}

	@Test
	public void testIsBlock() {
		//<block> ::= "{" <eol> { <command> }  "}" <eol>
		r1 = new Recognizer("{\n}\n");
		r2 = new Recognizer("{\n line 17 - 8, 3 + 38 > 900, 39, 0\n }\n");
		r3 = new Recognizer("{\n exit if 3 + 38 > 900 \n } \n");
	    assertTrue(r1.isBlock());
	    assertTrue(r2.isBlock());
	    assertTrue(r3.isBlock());
    }

	@Test
	public void testBugDefinition() {
		/*
		 * <bug definition> ::= "Bug" <name> "{" <eol>
                         { <var declaration> }
                         [ <initialization block> ]
                         <command>
                         { <command> }
                         { <function definition> }
                    "}" <eol>
		 */
		r1 = new Recognizer("Bug wooboo { \n move 5 + 1 = 6 \n } \n");
		r2 = new Recognizer("Bug booboo { \n var foo, bar \n move 5 + 1 = 6 \n } \n");
		r3 = new Recognizer("Bug bobo { \n var foo, bar \n initially {\n line 17 - 8, 3 + 38 > 900, 39, 0\n }\n move 5 + 1 = 6 \n } \n");
		r4 = new Recognizer("Bug brobro { \n var foo, bar \n initially {\n line 17 - 8, 3 + 38 > 900, 39, 0\n }\n move 5 + 1 = 6 \n "
				+ "do rue (bar, x+3)\n } \n");
		r5 = new Recognizer("Bug brobro { \n var foo, bar \n initially {\n line 17 - 8, 3 + 38 > 900, 39, 0\n }\n move 5 + 1 = 6 \n "
				+ "do rue (bar, x+3)\n define whoa using rue, dew {\n exit if 3 + 38 > 900 \n } \n} \n");
		r6 = new Recognizer("Bug badboo { > 900, 39, 0\n }\n move 5 + 1 = 6 \n "
				+ "do rue (bar, x+3ine whoa , dew {\n exit if 3 + 38 > 900 \n } \n} \n");
		assertTrue(r1.isBugDefinition());
		assertTrue(r2.isBugDefinition());
		assertTrue(r3.isBugDefinition());
		assertTrue(r4.isBugDefinition());
		assertTrue(r5.isBugDefinition());
		try{
			assertFalse(r6.isBugDefinition());
			fail();
		}
		catch (SyntaxException e) {
		}
	}

	@Test
	public void testIsColorStatement() {
		Recognizer r1 = new Recognizer("color blue \n");
		assertTrue(r1.isColorStatement());
		Recognizer r2 = new Recognizer(" color magic broomstick");
		try {
			assertFalse(r2.isColorStatement());
			fail();
		} catch (SyntaxException e) {
		}
	}

	@Test
	public void testIsCommand() {
		testIsAction();
		testIsStatement();
	}

	@Test
	public void testIsComparator() {
		// <comparator> ::= "<" | "<=" | "=" | "!=" | ">=" | ">"
		Recognizer r = new Recognizer("< $ <= 2 = != >= + >");
		assertTrue(r.isComparator()); // <
		assertFalse(r.isComparator()); // $
		r.nextToken();
		assertTrue(r.isComparator()); // <=
		assertFalse(r.isComparator()); // 2
		r.nextToken();
		assertTrue(r.isComparator()); // =
		assertTrue(r.isComparator()); // !=
		assertTrue(r.isComparator()); // >=
		assertFalse(r.isComparator()); // +
		r.nextToken();
		assertTrue(r.isComparator()); // >
	}

	@Test
	public void testIsDoStatement() {
		Recognizer r1 = new Recognizer("do foo \n");
		r2 = new Recognizer("do bar \n");
		r3 = new Recognizer("do rue \n");
		r4 = new Recognizer("do foo () \n");
		r5 = new Recognizer("do bar (5) \n");
		r6 = new Recognizer("do rue (bar, x+3)\n");
		assertTrue(r1.isDoStatement());
		assertTrue(r2.isDoStatement());
		assertTrue(r3.isDoStatement());
		assertTrue(r4.isDoStatement());
		assertTrue(r5.isDoStatement());
		assertTrue(r6.isDoStatement());
		r7 = new Recognizer("do rue (5), (bar, x+3)\n");
		try {
			assertFalse(r7.isDoStatement());
			fail();
		} catch (SyntaxException e) {
		}
		r8 = new Recognizer("do \n");
		try {
			assertFalse(r8.isDoStatement());
			fail();
		} catch (SyntaxException e) {
		}
	}

	@Test
	public void testIsEol() {
		r0 = new Recognizer("\n");
		r1 = new Recognizer("\n\n");
		r2 = new Recognizer("\n\n\n");
		r3 = new Recognizer("(xyz + 3)");
		assertTrue(r0.isEol());
		assertTrue(r1.isEol());
		assertTrue(r2.isEol());
		assertFalse(r3.isEol());
	}

	@Test
	public void testIsExitIfStatement() {
		Recognizer r1 = new Recognizer("exit if (5 + 1 = 6) \n");
		assertTrue(r1.isExitIfStatement());
		r2 = new Recognizer("exit if 3 + 38 > 900 \n");
		assertTrue(r2.isExitIfStatement());
		r3 = new Recognizer("exit if 28 > 2 , 328, 890 \n");
		try {
			assertFalse(r3.isExitIfStatement());
			fail();
		} catch (SyntaxException e) {
		}
	}

	@Test
	public void testIsExpression() {
		// <expression> ::= <arithmetic expression> { <comparator> <arithmetic
		// expression> }
		r1 = new Recognizer("(20 - 4) >= (17 - 1)");
		assertTrue(r1.isExpression());
		r2 = new Recognizer("5 = 7 - 2");
		assertTrue(r2.isExpression());
		r3 = new Recognizer("78 + 2 = (20 * 3) + (5 + 15)");
		assertTrue(r3.isExpression());
		r4 = new Recognizer("(78 + 2) = (20 * 3) + (5 + 15)");
		assertTrue(r4.isExpression());
	}

	@Test
	public void testIsFunctionCall() {
		// <function call> ::= <NAME> <parameter list>
		Recognizer r1 = new Recognizer("foo() $ bar new) blue(");
		assertTrue(r1.isFunctionCall());
		followedBy(r1, "$");
		try {
			assertFalse(r1.isFunctionCall());
			fail();
		} catch (SyntaxException e) {
		}
		r1.nextToken();
		assertFalse(r1.isFunctionCall());
		r1.nextToken();
		assertFalse(r1.isFunctionCall());
		Recognizer r2 = new Recognizer("true(fame, is, hard, to, come, by) $");
		assertTrue(r2.isFunctionCall());
		followedBy(r2, "$");
	}

	@Test
	public void testIsFunctionDefinition() {
		//<function definition> ::= "define" <NAME> [ "using" <variable> { "," <variable> }  ] <block>
		r1 = new Recognizer("define foo {\n line 17 - 8, 3 + 38 > 900, 39, 0\n }\n");
		r2 = new Recognizer("define bar using new {\n exit if 3 + 38 > 900 \n } \n");
		r3 = new Recognizer("define bar using rue, dew {\n exit if 3 + 38 > 900 \n } \n");
		assertTrue(r1.isFunctionDefinition());
		assertTrue(r2.isFunctionDefinition());
		assertTrue(r3.isFunctionDefinition());
	}

	@Test
	public void testIsInitializationBlock() {
		r1 = new Recognizer("initially {\n line 17 - 8, 3 + 38 > 900, 39, 0\n }\n");
		r2 = new Recognizer("initially {\n exit if 3 + 38 > 900 \n } \n");
	    assertTrue(r1.isInitializationBlock());
	    assertTrue(r2.isInitializationBlock());
	}

	@Test
	public void testIsLineAction() {
		Recognizer r1 = new Recognizer(
				"line (9) , (900 - 900) , (5 + 1 = 6) , (48 >= 9) \n");
		assertTrue(r1.isLineAction());
		r2 = new Recognizer("line 17 - 8, 3 + 38 > 900, 39, 0 \n");
		assertTrue(r2.isLineAction());
		r3 = new Recognizer("line 28 , 328 \n");
		try {
			assertFalse(r3.isLineAction());
			fail();
		} catch (SyntaxException e) {
		}
		r4 = new Recognizer("line 28 ,  \n");
		try {
			assertFalse(r4.isLineAction());
			fail();
		} catch (SyntaxException e) {
		}
		r5 = new Recognizer("line , , , , \n");
		try {
			assertFalse(r5.isLineAction());
			fail();
		} catch (SyntaxException e) {
		}
		r6 = new Recognizer("line 78, 39, 29 = 9 \n");
		try {
			assertFalse(r6.isLineAction());
			fail();
		} catch (SyntaxException e) {
		}
	}

	@Test
	public void testIsLoopStatement() {
		r1 = new Recognizer("loop {\n line 17 - 8, 3 + 38 > 900, 39, 0\n }\n");
		r2 = new Recognizer("loop {\n exit if 3 + 38 > 900 \n } \n");
		assertTrue(r1.isLoopStatement());
		assertTrue(r2.isLoopStatement());
	}

	@Test
	public void testIsMoveAction() {
		Recognizer r1 = new Recognizer("move 5 + 1 = 6 \n");
		assertTrue(r1.isMoveAction());
		r2 = new Recognizer("move 17 - 8 \n");
		assertTrue(r2.isMoveAction());
		r3 = new Recognizer("don't 28 \n");
		assertFalse(r3.isMoveAction());
	}

	@Test
	public void testIsMoveToAction() {
		Recognizer r1 = new Recognizer("moveto (5 + 1 = 6) , (48) \n");
		assertTrue(r1.isMoveToAction());
		r2 = new Recognizer("moveto 17 - 8, 3 + 38 > 900 \n");
		assertTrue(r2.isMoveToAction());
		r3 = new Recognizer("move 28 , 328 \n");
		assertFalse(r3.isMoveToAction());
		r4 = new Recognizer("moveto 28 - * & 23 , 328 \n");
		try {
			assertFalse(r4.isMoveToAction());
			fail();
		} catch (SyntaxException e) {
		}
	}

	@Test
	public void testIsProgram() {
		r1 = new Recognizer("Bug brobro { \n var foo, bar \n initially {\n line 17 - 8, 3 + 38 > 900, 39, 0\n }\n move 5 + 1 = 6 \n "
				+ "do rue (bar, x+3)\n define whoa using rue, dew {\n exit if 3 + 38 > 900 \n } \n} \n");
		
		r2 = new Recognizer("Bug brobro { \n var foo, bar \n initially {\n line 17 - 8, 3 + 38 > 900, 39, 0\n }\n move 5 + 1 = 6 \n "
				+ "do rue (bar, x+3)\n define whoa using rue, dew {\n exit if 3 + 38 > 900 \n } \n} \n "
				+ "Bug brobro { \n var foo, bar \n initially {\n line 17 - 8, 3 + 38 > 900, 39, 0\n }\n move 5 + 1 = 6 \n "
				+ "do rue (bar, x+3)\n define whoa using rue, dew {\n exit if 3 + 38 > 900 \n } \n} \n");
		
		r3 = new Recognizer("Allbugs { \n var foo, bar\n define bar using rue, dew {\n exit if 3 + 38 > 900 \n } \n} \n "
				+ "Bug brobro { \n var foo, bar \n initially {\n line 17 - 8, 3 + 38 > 900, 39, 0\n }\n move 5 + 1 = 6 \n "
				+ "do rue (bar, x+3)\n define whoa using rue, dew {\n exit if 3 + 38 > 900 \n } \n} \n "
				+ "Bug brobro { \n var foo, bar \n initially {\n line 17 - 8, 3 + 38 > 900, 39, 0\n }\n move 5 + 1 = 6 \n "
				+ "do rue (bar, x+3)\n define whoa using rue, dew {\n exit if 3 + 38 > 900 \n } \n} \n");
		assertTrue(r1.isProgram());
		assertTrue(r2.isProgram());
		assertTrue(r3.isProgram());
	}

	@Test
	public void testIsReturnStatement() {
		Recognizer r1 = new Recognizer("return (5 + 1 = 6) \n");
		assertTrue(r1.isReturnStatement());
		r2 = new Recognizer("return 3 + 38 > 900 \n");
		assertTrue(r2.isReturnStatement());
		r3 = new Recognizer("return 28 > 2 , 328, 890 \n");
		try {
			assertFalse(r3.isReturnStatement());
			fail();
		} catch (SyntaxException e) {
		}
	}

	@Test
	public void testIsStatement() {
		r1 = new Recognizer("foo = (5 + 1 = 6)\n");
		r2 = new Recognizer("foo = 9 > 2 \n");
		r3 = new Recognizer("bar = (5) \n");
		r4 = new Recognizer("rue = (x+3)\n");
		assertTrue(r1.isStatement());
		assertTrue(r2.isStatement());
		assertTrue(r3.isStatement());
		assertTrue(r4.isStatement());
		r5 = new Recognizer("rue == (5), (bar, x+3)\n");
		try {
			assertFalse(r5.isStatement());
			fail();
		} catch (SyntaxException e) {
		}
		r6 = new Recognizer("boo = \n");
		try {
			assertFalse(r6.isStatement());
			fail();
		} catch (SyntaxException e) {
		}
		r1 = new Recognizer("exit if (5 + 1 = 6) \n");
		assertTrue(r1.isStatement());
		r2 = new Recognizer("exit if 3 + 38 > 900 \n");
		assertTrue(r2.isStatement());
		r3 = new Recognizer("exit if 28 > 2 , 328, 890 \n");
		try {
			assertFalse(r3.isStatement());
			fail();
		} catch (SyntaxException e) {
		}
		r1 = new Recognizer("return (5 + 1 = 6) \n");
		assertTrue(r1.isStatement());
		r2 = new Recognizer("return 3 + 38 > 900 \n");
		assertTrue(r2.isStatement());
		r3 = new Recognizer("return 28 > 2 , 328, 890 \n");
		try {
			assertFalse(r3.isStatement());
			fail();
		} catch (SyntaxException e) {
		}
		Recognizer r1 = new Recognizer("do foo \n");
		r2 = new Recognizer("do bar \n");
		r3 = new Recognizer("do rue \n");
		r4 = new Recognizer("do foo () \n");
		r5 = new Recognizer("do bar (5) \n");
		r6 = new Recognizer("do rue (bar, x+3)\n");
		assertTrue(r1.isStatement());
		assertTrue(r2.isStatement());
		assertTrue(r3.isStatement());
		assertTrue(r4.isStatement());
		assertTrue(r5.isStatement());
		assertTrue(r6.isStatement());
		r7 = new Recognizer("do rue (5), (bar, x+3)\n");
		try {
			assertFalse(r7.isStatement());
			fail();
		} catch (SyntaxException e) {
		}
		r8 = new Recognizer("do \n");
		try {
			assertFalse(r8.isStatement());
			fail();
		} catch (SyntaxException e) {
		}
		r1 = new Recognizer("color blue \n");
		assertTrue(r1.isStatement());
		Recognizer r2 = new Recognizer(" color magic broomstick");
		try {
			assertFalse(r2.isStatement());
			fail();
		} catch (SyntaxException e) {
		}
	}

	@Test
	public void testIsSwitchStatement() {
		//<switch statement> ::= "switch" "{" <eol> { "case" <expression> <eol> { <command> } } "}" <eol>
		r1 = new Recognizer("switch { \n } \n");
		r2 = new Recognizer("switch { \n case (20 - 4) >= (17 - 1) \n } \n");
		r3 = new Recognizer("switch { \n case (20 - 4) >= (17 - 1) line 17 - 8, 3 + 38 > 900, 39, 0\n \n } \n");
		r4 = new Recognizer("switch { \n case (20 - 4) >= (17 - 1) exit if 3 + 38 > 900 \n  \n } \n");
	}
	
	
	@Test
	public void testIsTurnAction() {
		Recognizer r1 = new Recognizer("turn (5 + 1 = 6) \n");
		assertTrue(r1.isTurnAction());
		r2 = new Recognizer("turn 3 + 38 > 900 \n");
		assertTrue(r2.isTurnAction());
		r3 = new Recognizer("turn \n");
		try {
			assertFalse(r3.isTurnAction());
			fail();
		} catch (SyntaxException e) {
		}
	}

	@Test
	public void testIsTurnToAction() {
		Recognizer r1 = new Recognizer("turnto (5 + 1 = 6) \n");
		assertTrue(r1.isTurnToAction());
		r2 = new Recognizer("turnto 3 + 38 > 900 \n");
		assertTrue(r2.isTurnToAction());
		r3 = new Recognizer("turnto 28 > 2 , 328, 890 \n");
		try {
			assertFalse(r3.isTurnToAction());
			fail();
		} catch (SyntaxException e) {
		}
	}

	@Test
	public void testIsVarDeclaration() {
		Recognizer r1 = new Recognizer("var foo, bar\n");
		assertTrue(r1.isVarDeclaration());
		r2 = new Recognizer("var foo \n");
		assertTrue(r2.isVarDeclaration());
		r3 = new Recognizer("var \n");
		try {
			assertFalse(r3.isVarDeclaration());
			fail();
		} catch (SyntaxException e) {
		}

		r4 = new Recognizer("var boo , \n");
		try {
			assertFalse(r4.isVarDeclaration());
			fail();
		} catch (SyntaxException e) {
		}

		r5 = new Recognizer("var boo , moo");
		try {
			assertFalse(r5.isVarDeclaration());
			fail();
		} catch (SyntaxException e) {
		}
	}	
}
