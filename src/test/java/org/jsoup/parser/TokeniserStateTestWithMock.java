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

    @Test
    public void testTagOpen() {
        Tokeniser tokeniser = setup("<div>", TokeniserState.Data);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.TagOpen);
        inOrder.verify(tokeniser).transition(TokeniserState.TagName);
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void testEndTagOpen() {
        Tokeniser tokeniser = setup("</>", TokeniserState.Data);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.TagOpen);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.EndTagOpen);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.Data);

    }

    @Test
    public void testSelfClosingStartTag() {
        Tokeniser tokeniser = setup("<input/>", TokeniserState.Data);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.TagOpen);
        inOrder.verify(tokeniser).transition(TokeniserState.TagName);
        inOrder.verify(tokeniser).transition(TokeniserState.SelfClosingStartTag);
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void testRcDataTagOpen() {
        Tokeniser tokeniser = setup("<title>", TokeniserState.Rcdata);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.RcdataLessthanSign);
        inOrder.verify(tokeniser).transition(TokeniserState.Rcdata);
    }

    @Test
    public void testRcDataEndTagOpen() {
        Tokeniser tokeniser = setup("</title>", TokeniserState.Rcdata);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.RcdataLessthanSign);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.RCDATAEndTagOpen);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.RCDATAEndTagName);
        inOrder.verify(tokeniser).isAppropriateEndTagToken();
        inOrder.verify(tokeniser).transition(TokeniserState.Rcdata);
    }

    @Test
    public void testRawText() {
        Tokeniser tokeniser = setup("<Hello", TokeniserState.RawtextLessthanSign);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.Rawtext);
    }

    @Test
    public void testRawTextEndTagOpen() {
        Tokeniser tokeniser = setup("/Hello", TokeniserState.RawtextLessthanSign);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.RawtextEndTagOpen);
        inOrder.verify(tokeniser).transition(TokeniserState.RawtextEndTagName);
        inOrder.verify(tokeniser).transition(TokeniserState.Rawtext);
    }


    @Test
    public void testComment() {
        Tokeniser tokeniser = setup("--Comment-->", TokeniserState.CommentStart);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser, times(2)).transition(TokeniserState.CommentStartDash);
        inOrder.verify(tokeniser).transition(TokeniserState.Comment);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.CommentEndDash);
        inOrder.verify(tokeniser).transition(TokeniserState.CommentEnd);
        inOrder.verify(tokeniser).emitCommentPending();
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void testCommentEndBang() {
        Tokeniser tokeniser = setup("Comment--!>", TokeniserState.Comment);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.CommentEndDash);
        inOrder.verify(tokeniser).transition(TokeniserState.CommentEnd);
        inOrder.verify(tokeniser).transition(TokeniserState.CommentEndBang);
        inOrder.verify(tokeniser).emitCommentPending();
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void testCharacterReferenceInData() {
        Tokeniser tokeniser = setup("&Data", TokeniserState.Data);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).consumeCharacterReference(null, false);
        inOrder.verify(tokeniser).emit('&');
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void testCharacterReferenceInRcData() {
        Tokeniser tokeniser = setup("&RcData", TokeniserState.Rcdata);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).consumeCharacterReference(null, false);
        inOrder.verify(tokeniser).emit('&');
        inOrder.verify(tokeniser).transition(TokeniserState.Rcdata);

    }

    @Test
    public void testBogusComment() {
        Tokeniser tokeniser = setup("<?some comment>", TokeniserState.Data);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.TagOpen);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.BogusComment);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.Data);
    }

    @Test
    public void testMarkUpDeclarationOpenToCommentStart() {
        Tokeniser tokeniser = setup("-- Comment-->", TokeniserState.MarkupDeclarationOpen);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.CommentStart);
    }

    @Test
    public void testMarkUpDeclarationOpenToDocType() {
        Tokeniser tokeniser = setup("<!DOCTYPE>", TokeniserState.Data);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.TagOpen);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.MarkupDeclarationOpen);
        inOrder.verify(tokeniser).transition(TokeniserState.Doctype);
    }

    @Test
    public void testMarkUpDeclarationOpenToCDataSection() {
        Tokeniser tokeniser = setup("<![CDATA[]]>", TokeniserState.Data);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.TagOpen);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.MarkupDeclarationOpen);
        inOrder.verify(tokeniser).transition(TokeniserState.CdataSection);
    }

    @Test
    public void testMarkUpDeclarationOpenToBogusComment() {
        Tokeniser tokeniser = setup("<! bogus>", TokeniserState.Data);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.TagOpen);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.MarkupDeclarationOpen);
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.BogusComment);
    }

    @Test
    public void testDocTypeToBeforeDocTypeName() {
        Tokeniser tokeniser = setup(" html>", TokeniserState.Doctype);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.BeforeDoctypeName);
    }

    @Test
    public void testBeforeDoctypeNameToDocTypeName() {
        Tokeniser tokeniser = setup(" html>", TokeniserState.BeforeDoctypeName);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.DoctypeName);
    }

    @Test
    public void DoctypeNameToData() {
        Tokeniser tokeniser = setup(">", TokeniserState.DoctypeName);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void DoctypeNameToAfterDocTypeName() {
        Tokeniser tokeniser = setup(" >", TokeniserState.DoctypeName);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.AfterDoctypeName);
    }

    @Test
    public void AfterDocTypeNameToData() {
        Tokeniser tokeniser = setup(">", TokeniserState.AfterDoctypeName);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.Data);
    }

    @Test
    public void AfterDocTypeNameToAfterDoctypePublicKeyword() {
        Tokeniser tokeniser = setup(" PUBLIC ''>", TokeniserState.AfterDoctypeName);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.AfterDoctypePublicKeyword);
    }

    @Test
    public void AfterDocTypeNameToAfterDoctypeSystemKeyword() {
        Tokeniser tokeniser = setup(" SYSTEM ''>", TokeniserState.AfterDoctypeName);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.AfterDoctypeSystemKeyword);
    }

    @Test
    public void AfterDocTypeNameToBogusDocType() {
        Tokeniser tokeniser = setup("Bogus>", TokeniserState.AfterDoctypeName);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).advanceTransition(TokeniserState.BogusDoctype);
    }

    @Test
    public void AfterDoctypePublicKeywordToData() {
        Tokeniser tokeniser = setup(">", TokeniserState.AfterDoctypePublicKeyword);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void AfterDoctypePublicKeywordToDoctypeSystemIdentifier_doubleQuoted() {
        Tokeniser tokeniser = setup("\">", TokeniserState.AfterDoctypePublicKeyword);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.DoctypePublicIdentifier_doubleQuoted);
    }

    @Test
    public void AfterDoctypePublicKeywordToDoctypePublicIdentifier_singleQuoted() {
        Tokeniser tokeniser = setup("'>", TokeniserState.AfterDoctypePublicKeyword);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.DoctypePublicIdentifier_singleQuoted);
    }

    @Test
    public void AfterDoctypePublicKeywordToBeforeDoctypePublicIdentifier() {
        Tokeniser tokeniser = setup(" >", TokeniserState.AfterDoctypePublicKeyword);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.BeforeDoctypePublicIdentifier);
    }

    @Test
    public void BeforeDoctypePublicIdentifierToData() {
        Tokeniser tokeniser = setup(">", TokeniserState.BeforeDoctypePublicIdentifier);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void BeforeDoctypePublicIdentifierToDoctypePublicIdentifier_doubleQuoted() {
        Tokeniser tokeniser = setup("\"\">", TokeniserState.BeforeDoctypePublicIdentifier);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.DoctypePublicIdentifier_doubleQuoted);
    }

    @Test
    public void BeforeDoctypePublicIdentifierToDoctypePublicIdentifier_singleQuoted() {
        Tokeniser tokeniser = setup("\'\'>", TokeniserState.BeforeDoctypePublicIdentifier);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.DoctypePublicIdentifier_singleQuoted);
    }

    @Test
    public void DoctypePublicIdentifier_doubleQuotedToData() {
        Tokeniser tokeniser = setup(">", TokeniserState.DoctypePublicIdentifier_doubleQuoted);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void DoctypePublicIdentifier_doubleQuotedToAfterDoctypePublicIdentifier() {
        Tokeniser tokeniser = setup("\">", TokeniserState.DoctypePublicIdentifier_doubleQuoted);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.AfterDoctypePublicIdentifier);
    }

    @Test
    public void DoctypePublicIdentifier_singleQuotedToData() {
        Tokeniser tokeniser = setup(">", TokeniserState.DoctypePublicIdentifier_singleQuoted);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void DoctypePublicIdentifier_singleQuotedToAfterDoctypePublicIdentifier() {
        Tokeniser tokeniser = setup("'>", TokeniserState.DoctypePublicIdentifier_singleQuoted);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.AfterDoctypePublicIdentifier);
    }

    @Test
    public void AfterDoctypePublicIdentifierToData() {
        Tokeniser tokeniser = setup(">", TokeniserState.AfterDoctypePublicIdentifier);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void AfterDoctypePublicIdentifierToDoctypeSystemIdentifier_doubleQuoted() {
        Tokeniser tokeniser = setup("\">", TokeniserState.AfterDoctypePublicIdentifier);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.DoctypeSystemIdentifier_doubleQuoted);
    }

    @Test
    public void AfterDoctypePublicIdentifierToDoctypeSystemIdentifier_singleQuoted() {
        Tokeniser tokeniser = setup("'>", TokeniserState.AfterDoctypePublicIdentifier);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.DoctypeSystemIdentifier_singleQuoted);
    }

    @Test
    public void AfterDoctypePublicIdentifierToBetweenDoctypePublicAndSystemIdentifiers() {
        Tokeniser tokeniser = setup(" >", TokeniserState.AfterDoctypePublicIdentifier);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.BetweenDoctypePublicAndSystemIdentifiers);
    }

    @Test
    public void BetweenDoctypePublicAndSystemIdentifiersToData() {
        Tokeniser tokeniser = setup(">", TokeniserState.BetweenDoctypePublicAndSystemIdentifiers);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void BetweenDoctypePublicAndSystemIdentifiersToDoctypeSystemIdentifier_doubleQuoted() {
        Tokeniser tokeniser = setup("\">", TokeniserState.BetweenDoctypePublicAndSystemIdentifiers);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.DoctypeSystemIdentifier_doubleQuoted);
    }

    @Test
    public void BetweenDoctypePublicAndSystemIdentifiersToDoctypeSystemIdentifier_singleQuoted() {
        Tokeniser tokeniser = setup("'>", TokeniserState.BetweenDoctypePublicAndSystemIdentifiers);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.DoctypeSystemIdentifier_singleQuoted);
    }

    @Test
    public void AfterDoctypeSystemKeywordToData() {
        Tokeniser tokeniser = setup(">", TokeniserState.AfterDoctypeSystemKeyword);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void AfterDoctypeSystemKeywordToDoctypeSystemIdentifier_doubleQuoted() {
        Tokeniser tokeniser = setup("\">", TokeniserState.AfterDoctypeSystemKeyword);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.DoctypeSystemIdentifier_doubleQuoted);
    }

    @Test
    public void AfterDoctypeSystemKeywordToDoctypeSystemIdentifier_singleQuoted() {
        Tokeniser tokeniser = setup("\'>", TokeniserState.AfterDoctypeSystemKeyword);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.DoctypeSystemIdentifier_singleQuoted);
    }

    @Test
    public void AfterDoctypeSystemKeywordToBeforeDoctypeSystemIdentifier() {
        Tokeniser tokeniser = setup(" >", TokeniserState.AfterDoctypeSystemKeyword);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.BeforeDoctypeSystemIdentifier);
    }

    @Test
    public void BeforeDoctypeSystemIdentifierToData() {
        Tokeniser tokeniser = setup(">", TokeniserState.BeforeDoctypeSystemIdentifier);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void BeforeDoctypeSystemIdentifierToDoctypeSystemIdentifier_doubleQuoted() {
        Tokeniser tokeniser = setup("\">", TokeniserState.BeforeDoctypeSystemIdentifier);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.DoctypeSystemIdentifier_doubleQuoted);
    }

    @Test
    public void BeforeDoctypeSystemIdentifierToDoctypeSystemIdentifier_singleQuoted() {
        Tokeniser tokeniser = setup("\'>", TokeniserState.BeforeDoctypeSystemIdentifier);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.DoctypeSystemIdentifier_singleQuoted);
    }

    @Test
    public void DoctypeSystemIdentifier_doubleQuotedToData() {
        Tokeniser tokeniser = setup(">", TokeniserState.DoctypeSystemIdentifier_doubleQuoted);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void DoctypeSystemIdentifier_doubleQuotedToAfterDoctypeSystemIdentifier() {
        Tokeniser tokeniser = setup("\">", TokeniserState.DoctypeSystemIdentifier_doubleQuoted);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.AfterDoctypeSystemIdentifier);
    }

    @Test
    public void DoctypeSystemIdentifier_singleQuotedToData() {
        Tokeniser tokeniser = setup(">", TokeniserState.DoctypeSystemIdentifier_singleQuoted);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void DoctypeSystemIdentifier_singleQuotedToAfterDoctypeSystemIdentifier() {
        Tokeniser tokeniser = setup("\'>", TokeniserState.DoctypeSystemIdentifier_singleQuoted);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.AfterDoctypeSystemIdentifier);
    }

    @Test
    public void AfterDoctypeSystemIdentifierToData() {
        Tokeniser tokeniser = setup(">", TokeniserState.AfterDoctypeSystemIdentifier);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }

    @Test
    public void BogusDoctypeToData() {
        Tokeniser tokeniser = setup(">", TokeniserState.BogusDoctype);
        InOrder inOrder = Mockito.inOrder(tokeniser);
        tokeniser.read();
        inOrder.verify(tokeniser).transition(TokeniserState.Data);
    }
}
