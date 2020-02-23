package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HtmlTreeBuilderStructuralTest {

    @Test
    public void newTest() {
        System.out.println("Hello, World");
    }

    @Test
    public void isDocTypeBeforeHtml() { // 45 - 46
        HtmlTreeBuilderState state = HtmlTreeBuilderState.BeforeHtml;
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        String input = "<! DOCTYPE>";
        treeBuilder.initialiseParse(new StringReader(input), "", new Parser(treeBuilder));
        Token token = new Token.Doctype();
        assertFalse(state.process(token, treeBuilder));
    }

    @Test
    public void isNormalTextEndTagBeforeHtml() { //54 - 55
        HtmlTreeBuilderState state = HtmlTreeBuilderState.BeforeHtml;
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        String input = ">";
        treeBuilder.initialiseParse(new StringReader(input), "", new Parser(treeBuilder));
        Token.EndTag token = new Token.EndTag();
        token.normalName = "head";
        assertTrue(state.process(token, treeBuilder));
    }

    @Test
    public void isEndTagBeforeHtml() { // 57 - 58
        HtmlTreeBuilderState state = HtmlTreeBuilderState.BeforeHtml;
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        String input = ">";
        treeBuilder.initialiseParse(new StringReader(input), "", new Parser(treeBuilder));
        Token.EndTag token = new Token.EndTag();
        assertFalse(state.process(token, treeBuilder));
    }

    @Test
    public void isDocTypeInHead() { // 110 - 111
        HtmlTreeBuilderState state = HtmlTreeBuilderState.InHead;
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        String input = "";
        treeBuilder.initialiseParse(new StringReader(input), "", new Parser(treeBuilder));
        Token.Doctype token = new Token.Doctype();
        assertFalse(state.process(token, treeBuilder));
    }

    @Test
    public void isStartTagHTMLInHead() { //115 - 116
        HtmlTreeBuilderState state = HtmlTreeBuilderState.InHead;
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        String input = "";
        treeBuilder.initialiseParse(new StringReader(input), "", new Parser(treeBuilder));
        Token.StartTag token = new Token.StartTag();
        treeBuilder.stack.add(new Element("<html>"));
        token.name("html");
        assertTrue(state.process(token, treeBuilder));
    }

    @Test
    public void isStartTagHeadInHead() { //141 - 142
        HtmlTreeBuilderState state = HtmlTreeBuilderState.InHead;
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        String input = "head";
        treeBuilder.initialiseParse(new StringReader(input), "", new Parser(treeBuilder));
        Token.StartTag token = new Token.StartTag();
        treeBuilder.stack.add(new Element("<head>"));
        token.name("head");
        assertFalse(state.process(token, treeBuilder));
    }

    @Test
    public void isEndTagHeadInHead() { //154
        HtmlTreeBuilderState state = HtmlTreeBuilderState.InHead;
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        String input = "body";
        treeBuilder.initialiseParse(new StringReader(input), "", new Parser(treeBuilder));
        Token.StartTag token = new Token.StartTag();
        treeBuilder.stack.add(new Element("<body>"));
        token.name("body");
        assertTrue(state.process(token, treeBuilder));
    }

    @Test
    public void isNullCharacterInBody() { //258 - 259
        HtmlTreeBuilderState state = HtmlTreeBuilderState.InBody;
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        String input = "";
        treeBuilder.initialiseParse(new StringReader(input), "", new Parser(treeBuilder));
        Token.Character token = new Token.Character();
        token.data(String.valueOf('\u0000'));
        assertFalse(state.process(token, treeBuilder));
    }

    @Test
    public void isStartTagATagInBody() { //290 - 291
        HtmlTreeBuilderState state = HtmlTreeBuilderState.InBody;
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        String input = "a";
        treeBuilder.initialiseParse(new StringReader(input), "", new Parser(treeBuilder));
        Token.StartTag token = new Token.StartTag();
        token.name(input);
        token.attributes.put("a", "a");
        token.attributes.put("img", "img");
        treeBuilder.stack.add(new Element(input));
        treeBuilder.pushActiveFormattingElements(new Element(input));
        assertTrue(state.process(token, treeBuilder));
    }

    @Test
    public void isStartTagHTMLNoAttrInBody() { //331 - 333
        HtmlTreeBuilderState state = HtmlTreeBuilderState.InBody;
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        String input = "html";
        treeBuilder.initialiseParse(new StringReader(input), "", new Parser(treeBuilder));
        Token.StartTag token = new Token.StartTag();
        token.name(input);
        token.attributes.put("a", "a");
        treeBuilder.stack.add(new Element(input));
        treeBuilder.pushActiveFormattingElements(new Element(input));
        assertTrue(state.process(token, treeBuilder));
    }

    @Test
    public void isStartTagFrameSetInBody() { //359 - 366
        HtmlTreeBuilderState state = HtmlTreeBuilderState.InBody;
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        String input = "frameset";
        treeBuilder.initialiseParse(new StringReader(input), "", new Parser(treeBuilder));
        Token.StartTag token = new Token.StartTag();
        token.name(input);

        Element element = new Element("html");
        Element inputElement = new Element(input);

        element.appendChild(inputElement);
        treeBuilder.stack.add(element);
        treeBuilder.stack.add(inputElement);

        treeBuilder.pushActiveFormattingElements(new Element(input));
        assertTrue(state.process(token, treeBuilder));
    }

    @Test
    public void isStartTagFrameSetOKInBody() { //357
        HtmlTreeBuilderState state = HtmlTreeBuilderState.InBody;
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        String input = "frameset";
        treeBuilder.initialiseParse(new StringReader(input), "", new Parser(treeBuilder));
        Token.StartTag token = new Token.StartTag();
        token.name(input);

        Element element = new Element("html");
        Element inputElement = new Element(input);
        treeBuilder.framesetOk(false);
        element.appendChild(inputElement);
        treeBuilder.stack.add(element);
        treeBuilder.stack.add(inputElement);

        treeBuilder.pushActiveFormattingElements(new Element(input));
        assertFalse(state.process(token, treeBuilder));
    }

    @Test
    public void isStartTagFormInBody() { //385 - 386
        HtmlTreeBuilderState state = HtmlTreeBuilderState.InBody;
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        String input = "form";
        treeBuilder.initialiseParse(new StringReader(input), "", new Parser(treeBuilder));
        Token.StartTag token = new Token.StartTag();
        token.name(input);
        Tag tag = Tag.valueOf(token.name(), treeBuilder.settings);
        treeBuilder.setFormElement(new FormElement(tag, "", new Attributes()));

        Element element = new Element("html");
        Element inputElement = new Element(input);
        treeBuilder.framesetOk(false);
        element.appendChild(inputElement);
        treeBuilder.stack.add(element);
        treeBuilder.stack.add(inputElement);

        treeBuilder.pushActiveFormattingElements(new Element(input));
        assertFalse(state.process(token, treeBuilder));
    }

    @Test
    public void isStartTagPlainTextInBody() { //410 - 414
        HtmlTreeBuilderState state = HtmlTreeBuilderState.InBody;
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        String input = "plaintext";
        treeBuilder.initialiseParse(new StringReader(input), "", new Parser(treeBuilder));
        Token.StartTag token = new Token.StartTag();
        token.name(input);
        treeBuilder.stack.add(new Element("p"));
        assertTrue(state.process(token, treeBuilder));
    }

    @Test
    public void isStartTagButtonInBody() { //418 - 420
        HtmlTreeBuilderState state = HtmlTreeBuilderState.InBody;
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        String input = "plaintext";
        treeBuilder.initialiseParse(new StringReader(input), "", new Parser(treeBuilder));
        Token.StartTag token = new Token.StartTag();
        token.name(input);
        treeBuilder.stack.add(new Element("p"));
        assertTrue(state.process(token, treeBuilder));
    }
}
