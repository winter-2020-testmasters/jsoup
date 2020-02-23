package org.jsoup.parser;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

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

    @Test
    public void testParseFragmentPlainText(){
        String html = "<ol><li>One</li></ol><p>Two</p>";
        Element context =new Element("plaintext");
        List<Node> nodes = Parser.parseFragment(html, context, "http://example.com/");
        Assert.assertEquals(2,nodes.size());
    }

    @Test
    public void testParseFragmentScriptData(){
        String html = "<script>int i=0;</script>";
        Element context =new Element("script");
        List<Node> nodes = Parser.parseFragment(html, context, "http://example.com/");
        Assert.assertEquals(1,nodes.size());
    }

    @Test
    public void testParseFragmentNoScript(){
        String html = "<script>\n" +
                "document.write(\"Hello World!\")\n" +
                "</script>\n" +
                "<noscript>Your browser does not support JavaScript!</noscript>";
        Element context =new Element("noscript");
        List<Node> nodes = Parser.parseFragment(html, context, "http://example.com/");
        Assert.assertEquals(3,nodes.size());
    }

    @Test
    public void testParseFragmentRawText(){
        String html = "<noembed> <h1>Alternative content</h1></noembed>";
        Element context =new Element("noembed");
        List<Node> nodes = Parser.parseFragment(html, context, "http://example.com/");
        Assert.assertEquals(1,nodes.size());
    }

    @Test
    public void testResetInsertionModeSelect(){
        String html = " <select id=\"cars\">\n" +
                "  <option value=\"volvo\">Volvo</option>\n" +
                "  <option value=\"saab\">Saab</option>\n" +
                "  <option value=\"mercedes\">Mercedes</option>\n" +
                "  <option value=\"audi\">Audi</option>\n" +
                "</select>";
        Element context =new Element("select");
        List<Node> nodes = Parser.parseFragment(html, context, "http://example.com/");
        Assert.assertEquals(10,nodes.size());
    }

    @Test
    public void testResetInsertionModeTr(){
        String html = "<tr><td>1</td><td>Software Testing and Debugging</td></tr>";
        Element context =new Element("tr");
        List<Node> nodes = Parser.parseFragment(html, context, "http://example.com/");
        Assert.assertEquals(2,nodes.size());
    }

    @Test
    public void testResetInsertionModeCaption(){
        String html = "<table>\n" +
                "  <caption>Monthly savings</caption>\n" +
                "  <tr>\n" +
                "    <th>Month</th>\n" +
                "    <th>Savings</th>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "    <td>January</td>\n" +
                "    <td>$100</td>\n" +
                "  </tr>\n" +
                "</table>";
        Element context =new Element("caption");
        List<Node> nodes = Parser.parseFragment(html, context, "http://example.com/");
        Assert.assertEquals(1,nodes.size());
    }

    @Test
    public void testResetInsertionModeColGroup(){
        String html = " <table>\n" +
                "  <colgroup>\n" +
                "    <col span=\"2\" style=\"background-color:red\">\n" +
                "    <col style=\"background-color:yellow\">\n" +
                "  </colgroup>\n" +
                "  <tr>\n" +
                "    <th>ISBN</th>\n" +
                "    <th>Title</th>\n" +
                "    <th>Price</th>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "    <td>3476896</td>\n" +
                "    <td>My first HTML</td>\n" +
                "    <td>$53</td>\n" +
                "  </tr>\n" +
                "</table>";
        Element context =new Element("colgroup");
        List<Node> nodes = Parser.parseFragment(html, context, "http://example.com/");
        Assert.assertEquals(18,nodes.size());
    }

    @Test
    public void testResetInsertionModeTable(){
        String html = "<table>\n" +
                "  <caption>Monthly savings</caption>\n" +
                "  <tr>\n" +
                "    <th>Month</th>\n" +
                "    <th>Savings</th>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "    <td>January</td>\n" +
                "    <td>$100</td>\n" +
                "  </tr>\n" +
                "</table>";
        Element context =new Element("table");
        List<Node> nodes = Parser.parseFragment(html, context, "http://example.com/");
        Assert.assertEquals(4,nodes.size());
    }

    @Test
    public void testResetInsertionModeFrameset(){
        String html = "<frameset cols=\"25%,*,25%\">\n" +
                "  <frame src=\"frame_a.htm\">\n" +
                "  <frame src=\"frame_b.htm\">\n" +
                "  <frame src=\"frame_c.htm\">\n" +
                "</frameset>";
        Element context =new Element("frameset");
        List<Node> nodes = Parser.parseFragment(html, context, "http://example.com/");
        Assert.assertEquals(1,nodes.size());
    }

    @Test
    public void testResetInsertionModeHtml(){
        String html = "<html>\n" +
                "<head>\n" +
                "<title>Title of the document</title>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "The content of the document......\n" +
                "</body>\n" +
                "\n" +
                "</html>";
        Element context =new Element("html");
        List<Node> nodes = Parser.parseFragment(html, context, "http://example.com/");
        Assert.assertEquals(3,nodes.size());
    }
}
