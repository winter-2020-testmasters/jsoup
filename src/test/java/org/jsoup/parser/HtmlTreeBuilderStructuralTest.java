package org.jsoup.parser;

import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;

public class HtmlTreeBuilderStructuralTest {

    private HtmlTreeBuilder createTestTB() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(" "), " ", new Parser(tb));
        tb.parser.setTrackErrors(1);
        return tb;
    }

    @Test
    public void testInColumnGroupWithComment() {
        HtmlTreeBuilder tb = createTestTB();
        tb.process(new Token.Comment(), HtmlTreeBuilderState.InColumnGroup);
        Assert.assertEquals(tb.getDocument().childNode(0).nodeName(), "#comment");
    }

    @Test
    public void testInColumnGroupWithDoctype() {
        HtmlTreeBuilder tb = createTestTB();
        tb.process(new Token.Doctype(), HtmlTreeBuilderState.InColumnGroup);
        Assert.assertEquals(tb.parser.getErrors().size(), 1);
    }

    @Test
    public void testInSelectInTableWithStartTag() {
        HtmlTreeBuilder tb = createTestTB();
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("caption");
        tb.process(startTag, HtmlTreeBuilderState.InSelectInTable);
        Assert.assertEquals(tb.parser.getErrors().size(), 1);
    }

    @Test
    public void testInSelectInTableWithEndTag() {
        HtmlTreeBuilder tb = createTestTB();
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("caption");
        tb.process(endTag, HtmlTreeBuilderState.InSelectInTable);
        Assert.assertEquals(tb.parser.getErrors().size(), 1);
    }

    @Test
    public void testInFramesetWithFramesetStartTag() {
        HtmlTreeBuilder tb = createTestTB();
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("frameset");
        tb.process(startTag, HtmlTreeBuilderState.InFrameset);
        Assert.assertEquals(tb.pop().normalName(), "frameset");
    }

    @Test
    public void testInFramesetWithDefaultStartTag() {
        HtmlTreeBuilder tb = createTestTB();
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("hello");
        tb.process(startTag, HtmlTreeBuilderState.InFrameset);
        Assert.assertEquals(tb.parser.getErrors().size(), 1);
    }

    @Test
    public void testInFramesetWithEOF() {
        HtmlTreeBuilder tb = createTestTB();
        //Process a start tag to set TB's "current element"
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("body");
        tb.process(startTag);
        //Process EOF
        tb.process(new Token.EOF(), HtmlTreeBuilderState.InFrameset);
        Assert.assertEquals(tb.parser.getErrors().size(), 1);
    }
}
