package org.grammaticalframework.grammarlex.ViewModel;

import android.app.Application;
import android.view.inputmethod.CompletionInfo;

import org.grammaticalframework.grammarlex.Language;
import org.grammaticalframework.grammarlex.Grammarlex;
import org.grammaticalframework.pgf.Concr;
import org.grammaticalframework.pgf.Expr;
import org.grammaticalframework.pgf.FullFormEntry;
import org.grammaticalframework.pgf.MorphoAnalysis;
import org.daison.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

public class LexiconViewModel extends AndroidViewModel {
    private List<LexiconWord> lexiconWords;

    private static final String TAG = LexiconViewModel.class.getSimpleName();

    public LexiconViewModel(@NonNull Application application) {
        super(application);
        lexiconWords = new ArrayList<>();
    }

    public String wordTranslator(String word) {
        lexiconWords.clear();

        String form = null;

        Grammarlex gl = Grammarlex.get();
        Set<String> functions = new HashSet<String>();

        if (word != null && word.length() > 1) {
            PriorityQueue<FullFormEntry> queue =
                    new PriorityQueue<FullFormEntry>(500, new Comparator<FullFormEntry>() {
                        @Override
                        public int compare(FullFormEntry lhs, FullFormEntry rhs) {
                            return Double.compare(lhs.getProb(), rhs.getProb());
                        }
                    });
            for (FullFormEntry entry : Grammarlex.get().getSourceConcr().lookupWordPrefix(word)) {
                if (entry.getForm().equals(word)) {
                    queue.clear();
                    queue.add(entry);
                    break;
                }
                boolean is_new = false;
                for (MorphoAnalysis an : entry.getAnalyses()) {
                    if (!functions.contains(an.getLemma())) {
                        functions.add(an.getLemma());
                        is_new = true;
                    }
                }
                if (is_new) {
                    queue.add(entry);
                    if (queue.size() >= 1000)
                        break;
                }
            }

            for (FullFormEntry entry : queue) {
                functions.clear();
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

                                SenseSchema.Status status = SenseSchema.Status.Checked;
                                for (SenseSchema.LanguageStatus lang_status : row.getValue().status) {
                                    if (lang_status.language.equals(gl.getSourceLanguage().getConcrete()) ||
                                        lang_status.language.equals(gl.getTargetLanguage().getConcrete())) {
                                        if (status.ordinal() > lang_status.status.ordinal())
                                            status = lang_status.status;
                                    }
                                }
                                lexiconWord.setStatus(status);

                                if (row.getValue().synset_id != null) {
                                    SenseSchema.Synset synset = t.at(SenseSchema.synsets, row.getValue().synset_id.longValue());
                                    if (synset != null) {
                                        lexiconWord.setGloss(synset.gloss);
                                    }

                                    for (IdValue<SenseSchema.Lexeme> srow : t.atIndex(SenseSchema.lexemes_synset, row.getValue().synset_id)) {
                                        String s_fun = srow.getValue().lex_fun;
                                        if (!gl.getTargetConcr().hasLinearization(s_fun))
                                            continue;
                                        boolean checked = true;
                                        for (SenseSchema.LanguageStatus lang_status : row.getValue().status) {
                                            if ((lang_status.language.equals(gl.getTargetLanguage().getConcrete()) ||
                                                 lang_status.language.equals(gl.getSourceLanguage().getConcrete())) &&
                                                lang_status.status != SenseSchema.Status.Checked) {
                                                checked = false;
                                            }
                                        }
                                        if (!checked)
                                            continue;
                                        String s_lin = gl.getTargetConcr().linearize(Expr.readExpr(s_fun));
                                        if (!lin.equals(s_lin)) {
                                            lexiconWord.addSynonymWord(s_lin);
                                        }
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

            Collections.sort(lexiconWords, new Comparator<LexiconWord>() {
                @Override
                public int compare(LexiconWord lhs, LexiconWord rhs) {
                    // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                    if(lhs.getStatus() != null && rhs.getStatus() == null)
                        return -1;
                    if(lhs.getStatus() == null && rhs.getStatus() != null)
                        return 1;
                    if(lhs.getStatus() == null && rhs.getStatus() == null)
                        return 0;
                    if (lhs.getStatus().equals(rhs.getStatus()))
                        return 0;
                    if(lhs.getStatus().equals(SenseSchema.Status.Checked))
                        return -1;
                    if(rhs.getStatus().equals(SenseSchema.Status.Checked))
                        return 1;
                    return 0;
                }
            });
        }

        return form;
    }

    public List<LexiconWord> getLexiconWords() {
        return lexiconWords;
    }

    public String inflect(String lemma) {
        Grammarlex gl = Grammarlex.get();
        String cat = gl.getGrammar().getFunctionType(lemma).getCategory();
        Expr e = Expr.readExpr("MkDocument (NoDefinition \"\") (Inflection" + cat + " " + lemma + ") \"\"");
        return gl.getTargetConcr().linearize(e);
    }
}