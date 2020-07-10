package org.grammaticalframework.grammarlex.ViewModel;

import android.app.Application;

import org.grammaticalframework.grammarlex.Language;
import org.grammaticalframework.grammarlex.Grammarlex;
import org.grammaticalframework.grammarlex.gf.GF;
import org.grammaticalframework.pgf.Concr;
import org.grammaticalframework.pgf.Expr;
import org.grammaticalframework.pgf.FullFormEntry;
import org.grammaticalframework.pgf.MorphoAnalysis;
import org.daison.*;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

public class LexiconViewModel extends AndroidViewModel {
    private List<LexiconWord> lexiconWords;

    private static final String TAG = LexiconViewModel.class.getSimpleName();
    private GF gfClass;

    //The functions that we are going to find in wordnet
    private List<String> synonyms = new ArrayList<>();

    public LexiconViewModel(@NonNull Application application) {
        super(application);
        lexiconWords = new ArrayList<>();
        gfClass = new GF(Grammarlex.get());
    }

    public String wordTranslator(String word) {
        lexiconWords.clear();
        synonyms.clear();

        String form = null;

        Grammarlex gl = Grammarlex.get();
        List<String> functions = new ArrayList<>();

        if (word != null && word.length() > 0) {
            for (FullFormEntry entry : gl.getSourceConcr().lookupWordPrefix(word)) {
                form = entry.getForm();
                for (MorphoAnalysis an : entry.getAnalyses()) {
                    String lemma = an.getLemma();
                    if (!functions.contains(lemma) && gl.getTargetConcr().hasLinearization(lemma)) {
                        functions.add(lemma);

                        String cat = gl.getGrammar().getFunctionType(lemma).getCategory();

                        Expr e_tag = Expr.readExpr("MkTag (Inflection" + cat + " " + lemma + ")");
                        String tag = gl.getTargetConcr().linearize(e_tag);

                        Expr e_lin = Expr.readExpr(lemma);
                        String lin = gl.getTargetConcr().linearize(e_lin);

                        LexiconWord lexiconWord =
                            new LexiconWord(an.getLemma(), tag, lin);

                        try (ReadTransaction t = gl.getDatabase().newReadTransaction()) {
                            for (IdValue<SenseSchema.Lexeme> row : t.atIndex(SenseSchema.lexemes_fun, an.getLemma())) {
                                lexiconWord.setImages(row.getValue().images);

                                for (SenseSchema.LanguageStatus lang_status : row.getValue().status) {
                                    if (lang_status.language.equals(gl.getTargetLanguage().getConcrete())) {
                                        lexiconWord.setStatus(lang_status.status);
                                        break;
                                    }
                                }

                                if (row.getValue().synset_id != null) {
                                    SenseSchema.Synset synset = t.at(SenseSchema.synsets, row.getValue().synset_id.longValue());
                                    if (synset != null) {
                                        lexiconWord.setGloss(synset.gloss);
                                    }
                                }
                            };
                        }

                        lexiconWords.add(lexiconWord);
                    }
                }
                if (lexiconWords.size() > 0)
                    break;
            }
        }

        return form;
    }

    public List<LexiconWord> getLexiconWords() {
        return lexiconWords;
    }

    public List<String> getSynonyms(){
        return synonyms;
    }

    public void setLexiconWords(List<LexiconWord> lexiconWords){this.lexiconWords = lexiconWords;}

    public String inflect(String lemma) {
        Grammarlex gl = Grammarlex.get();
        String cat = gl.getGrammar().getFunctionType(lemma).getCategory();
        Expr e = Expr.readExpr("MkDocument (NoDefinition \"\") (Inflection" + cat + " " + lemma + ") \"\"");
        return gl.getTargetConcr().linearize(e);
    }
}