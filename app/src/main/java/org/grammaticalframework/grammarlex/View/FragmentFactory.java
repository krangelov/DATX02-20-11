package org.grammaticalframework.grammarlex.View;

import org.grammaticalframework.grammarlex.View.Fragments.BaseFragment;
import org.grammaticalframework.grammarlex.View.Fragments.LexiconDetailsFragment;
import org.grammaticalframework.grammarlex.View.Fragments.MainHomeFragment;
import org.grammaticalframework.grammarlex.View.Fragments.MainLexiconFragment;

public class FragmentFactory {
    public static BaseFragment createMainLexiconFragment(){
        return new MainLexiconFragment();
    }
    public static BaseFragment createMainHomeFragment(){
        return new MainHomeFragment();
    }

    public static BaseFragment createLexiconDetailsFragment() {return new LexiconDetailsFragment(); }
}
