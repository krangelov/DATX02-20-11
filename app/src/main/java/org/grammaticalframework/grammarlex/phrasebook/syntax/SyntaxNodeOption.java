package org.grammaticalframework.grammarlex.phrasebook.syntax;

import java.util.*;

public class SyntaxNodeOption extends SyntaxNode {
    private SyntaxNode[] options;
    private boolean is_lexicon;

    public SyntaxNodeOption(String desc, SyntaxNode[] options, boolean is_lexicon) {
		super(desc);
        this.options = options;
        this.is_lexicon = is_lexicon;
    }

    public SyntaxNode[] getOptions() {
        return options;
    }

    public boolean isLexicon() { return is_lexicon; }

	@Override
    public String getAbstractSyntax(ChoiceContext context) {
		return options[context.choose(this)].getAbstractSyntax(context);
	}
}
