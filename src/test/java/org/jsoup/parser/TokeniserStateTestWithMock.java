package org.jsoup.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TokeniserStateTestWithMock {


    public Tokeniser setup(String input) {
        Tokeniser tokeniser = new Tokeniser(new CharacterReader(input), ParseErrorList.noTracking());
        return spy(tokeniser);
    }

    public Tokeniser setup(String input, TokeniserState state) {
        Tokeniser tokeniser = new Tokeniser(new CharacterReader(input), ParseErrorList.noTracking());
        tokeniser.transition(state);
        return spy(tokeniser);
    }

    @Test
    public void testTokeniser() {
        Tokeniser tokeniser = setup("<div>");
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser, times(1)).advanceTransition(TokeniserState.TagOpen);
        inOrder.verify(tokeniser, times(1)).transition(TokeniserState.TagName);
    }

    @Test
    public void testTokeniserTagOpen() {
        Tokeniser tokeniser = setup("div>", TokeniserState.TagOpen);
        tokeniser.read();
        verify(tokeniser, times(1)).transition(TokeniserState.TagName);
    }

    @Test
    public void testScriptDataEndTag() {
        Tokeniser tokeniser = setup("</div>", TokeniserState.ScriptData);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.ScriptDataLessthanSign);
        inOrder.verify(tokeniser).transition(TokeniserState.ScriptDataEndTagOpen);
        inOrder.verify(tokeniser).transition(TokeniserState.ScriptDataEndTagName);
        inOrder.verify(tokeniser).transition(TokeniserState.ScriptData);
    }

    @Test
    public void testScriptDataEscapeStart() {
        Tokeniser tokeniser = setup("--<abc>", TokeniserState.ScriptDataEscapeStart);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.ScriptDataEscapeStartDash);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.ScriptDataEscapedDashDash);
        inOrder.verify(tokeniser).transition(TokeniserState.ScriptDataEscapedLessthanSign);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.ScriptDataDoubleEscapeStart);
        inOrder.verify(tokeniser).transition(TokeniserState.ScriptDataEscaped);
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void testScriptDataEscaped() {
        Tokeniser tokeniser = setup("-</div>", TokeniserState.ScriptDataEscaped);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.ScriptDataEscapedDash);
        inOrder.verify(tokeniser).transition(TokeniserState.ScriptDataEscapedLessthanSign);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.ScriptDataEscapedEndTagOpen);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.ScriptDataEscapedEndTagName);
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void testScriptDataDoubleEscaped() {
        Tokeniser tokeniser = setup("--</", TokeniserState.ScriptDataDoubleEscaped);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.ScriptDataDoubleEscapedDash);
        inOrder.verify(tokeniser).transition(TokeniserState.ScriptDataDoubleEscapedDashDash);
        inOrder.verify(tokeniser).transition(TokeniserState.ScriptDataDoubleEscapedLessthanSign);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.ScriptDataDoubleEscapeEnd);
        inOrder.verify(tokeniser).transition(TokeniserState.ScriptDataDoubleEscaped);
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void testScriptDataDoubleEscapedDash() {
        Tokeniser tokeniser = setup("-<a", TokeniserState.ScriptDataDoubleEscaped);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.ScriptDataDoubleEscapedDash);
        inOrder.verify(tokeniser).transition(TokeniserState.ScriptDataDoubleEscapedLessthanSign);
        inOrder.verify(tokeniser).transition(TokeniserState.ScriptDataDoubleEscaped);
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void testScriptDataEscapedDashDash() {
        Tokeniser tokeniser = setup(">", TokeniserState.ScriptDataEscapedDashDash);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.ScriptData);
    }

    @Test
    public void testAttributeName() {
        Tokeniser tokeniser = setup("id = \"hello\" /", TokeniserState.BeforeAttributeName);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.createTagPending(true);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.AttributeName);
        inOrder.verify(tokeniser).transition(TokeniserState.AfterAttributeName);
        inOrder.verify(tokeniser).transition(TokeniserState.BeforeAttributeValue);
        inOrder.verify(tokeniser).transition(TokeniserState.AttributeValue_doubleQuoted);
        inOrder.verify(tokeniser).transition(TokeniserState.AfterAttributeValue_quoted);
        inOrder.verify(tokeniser).transition(TokeniserState.SelfClosingStartTag);
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void testAttributeValueUnquotedSingleQuoted() {
        Tokeniser tokeniser = setup("hello color=\'blue\'>", TokeniserState.BeforeAttributeValue);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.createTagPending(true);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.AttributeValue_unquoted);
        inOrder.verify(tokeniser).transition(TokeniserState.BeforeAttributeName);
        /* Some untested transitions here */
        inOrder.verify(tokeniser).transition(TokeniserState.BeforeAttributeValue);
        inOrder.verify(tokeniser).transition(TokeniserState.AttributeValue_singleQuoted);
        inOrder.verify(tokeniser).transition(TokeniserState.AfterAttributeValue_quoted);
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
        System.out.println(tokeniser.getState());
    }

    @Test
    public void testBeforeAttributeName() {
        Tokeniser tokeniser = setup("color /", TokeniserState.AfterAttributeValue_quoted);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.createTagPending(true);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.BeforeAttributeName);
        inOrder.verify(tokeniser).transition(TokeniserState.SelfClosingStartTag);
    }

    @Test
    public void testAfterAttributeName() {
        Tokeniser tokeniser = setup(" color", TokeniserState.AfterAttributeName);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.createTagPending(true);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.AttributeName);
    }
}
