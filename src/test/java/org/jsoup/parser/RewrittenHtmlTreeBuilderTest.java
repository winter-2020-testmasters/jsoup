package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.StringReader;

import static org.mockito.Mockito.*;

public class RewrittenHtmlTreeBuilderTest {

    @Test
    public void HtmlTreeBuilderShouldTransitionCorrectly() {

        //Spy HtmlTreeBuilder
        HtmlTreeBuilder tb = spy(new HtmlTreeBuilder());

        //Mock document
        Document document = mock(Document.class);
        when(document.location()).thenReturn("http://www.example.com");

        //Mock tokeniser
        Tokeniser tokeniser = mock(Tokeniser.class);
        when(tokeniser.read()).thenReturn(new Token.Comment(), new Token.EOF());

        tb.parse(new StringReader("hello"), document, Parser.htmlParser(), tokeniser);

        verify(tb).transition(HtmlTreeBuilderState.BeforeHtml);
        verify(tb).transition(HtmlTreeBuilderState.BeforeHead);

    }

}
