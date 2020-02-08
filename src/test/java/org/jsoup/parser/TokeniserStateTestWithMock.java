package org.jsoup.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TokeniserStateTestWithMock {


    public Tokeniser setup(String input) {
        Tokeniser tokeniser = new Tokeniser(new CharacterReader(input), ParseErrorList.noTracking());
        return spy(tokeniser);
    }

    @Test
    public void testTokeniser() {
        Tokeniser tokeniser = setup("<div>");
        tokeniser.read();
        verify(tokeniser, times(1)).advanceTransition(TokeniserState.TagOpen);
        verify(tokeniser, times(1)).transition(TokeniserState.TagName);
    }

    @Test
    public void testTokeniserTagOpen() {
        Tokeniser tokeniser = setup("div>");
        tokeniser.transition(TokeniserState.TagOpen);
        tokeniser.read();
        verify(tokeniser, times(1)).transition(TokeniserState.TagOpen);
    }
}
