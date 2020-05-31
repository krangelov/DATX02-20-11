package org.grammaticalframework.grammarlex.phrasebook.syntax;

import java.util.*;

public class SyntaxNodeOption extends SyntaxNode {
    private SyntaxNode[] options;

    public SyntaxNodeOption(String desc, SyntaxNode[] options) {
		super(desc);
        this.options = options;
    }

    public SyntaxNode[] getOptions() {
        return options;
    }

	@Override
    public String getAbstractSyntax(ChoiceContext context) {
		return options[context.choose(this)].getAbstractSyntax(context);
	}
}
