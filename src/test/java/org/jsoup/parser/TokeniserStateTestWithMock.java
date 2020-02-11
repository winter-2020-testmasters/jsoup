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

   /* @Test
    public void testTokeniser() {
        Tokeniser tokeniser = setup("<div>");
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser, times(1)).advanceTransition(TokeniserState.TagOpen);
        inOrder.verify(tokeniser, times(1)).transition(TokeniserState.TagName);
    }

    @Test
    public void testTokeniserTagOpen() {
        Tokeniser tokeniser = setup("div>");
        tokeniser.transition(TokeniserState.TagOpen);
        tokeniser.read();
        verify(tokeniser, times(1)).transition(TokeniserState.TagName);
    }*/

    @Test
    public void testTagOpen(){
        Tokeniser tokeniser = setup("<div>",TokeniserState.Data);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.TagOpen);
        inOrder.verify(tokeniser).transition(TokeniserState.TagName);
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void testEndTagOpen(){
        Tokeniser tokeniser = setup("</>",TokeniserState.Data);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.TagOpen);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.EndTagOpen);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.Data);

    }

    @Test
    public void testSelfClosingStartTag(){
        Tokeniser tokeniser = setup("<input/>",TokeniserState.Data);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.TagOpen);
        inOrder.verify(tokeniser).transition(TokeniserState.TagName);
        inOrder.verify(tokeniser).transition(TokeniserState.SelfClosingStartTag);
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void testRcDataTagOpen(){
        Tokeniser tokeniser = setup("<title>",TokeniserState.Rcdata);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.RcdataLessthanSign);
        inOrder.verify(tokeniser).transition(TokeniserState.Rcdata);
    }

    @Test
    public void testRcDataEndTagOpen(){
        Tokeniser tokeniser = setup("</title>",TokeniserState.Rcdata);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.RcdataLessthanSign);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.RCDATAEndTagOpen);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.RCDATAEndTagName);
        inOrder.verify(tokeniser).isAppropriateEndTagToken();
        inOrder.verify(tokeniser).transition(TokeniserState.Rcdata);
    }

    @Test
    public void testRawText(){
        Tokeniser tokeniser = setup("<Hello",TokeniserState.RawtextLessthanSign);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.Rawtext);
    }

    @Test
    public void testRawTextEndTagOpen(){
        Tokeniser tokeniser = setup("/Hello",TokeniserState.RawtextLessthanSign);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.RawtextEndTagOpen);
        inOrder.verify(tokeniser).transition(TokeniserState.RawtextEndTagName);
        inOrder.verify(tokeniser).transition(TokeniserState.Rawtext);
    }


    @Test
    public void testComment(){
        Tokeniser tokeniser = setup("--Comment-->",TokeniserState.CommentStart);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser,times(2)).transition(TokeniserState.CommentStartDash);
        inOrder.verify(tokeniser).transition(TokeniserState.Comment);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.CommentEndDash);
        inOrder.verify(tokeniser).transition(TokeniserState.CommentEnd);
        inOrder.verify(tokeniser).emitCommentPending();
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void testCommentEndBang(){
        Tokeniser tokeniser = setup("Comment--!>",TokeniserState.Comment);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.CommentEndDash);
        inOrder.verify(tokeniser).transition(TokeniserState.CommentEnd);
        inOrder.verify(tokeniser).transition(TokeniserState.CommentEndBang);
        inOrder.verify(tokeniser).emitCommentPending();
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void testCharacterReferenceInData(){
        Tokeniser tokeniser = setup("&Data",TokeniserState.Data);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).consumeCharacterReference(null,false);
        inOrder.verify(tokeniser).emit('&');
        inOrder.verify(tokeniser).transition(TokeniserState.Data);

    }

    @Test
    public void testCharacterReferenceInRcData(){
        Tokeniser tokeniser = setup("&RcData",TokeniserState.Rcdata);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).consumeCharacterReference(null,false);
        inOrder.verify(tokeniser).emit('&');
        inOrder.verify(tokeniser).transition(TokeniserState.Rcdata);

    }



}
