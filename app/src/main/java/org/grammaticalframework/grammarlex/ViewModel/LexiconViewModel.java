package org.grammaticalframework.grammarlex.ViewModel;

import android.app.Application;

import org.grammaticalframework.grammarlex.Language;
import org.grammaticalframework.grammarlex.Repository.WNExplanation;
import org.grammaticalframework.grammarlex.Repository.WNExplanationRepository;
import org.grammaticalframework.grammarlex.Repository.WNExplanationWithCheck;
import org.grammaticalframework.grammarlex.Grammarlex;
import org.grammaticalframework.grammarlex.gf.GF;
import org.grammaticalframework.pgf.Concr;
import org.grammaticalframework.pgf.Expr;
import org.grammaticalframework.pgf.FullFormEntry;
import org.grammaticalframework.pgf.MorphoAnalysis;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

public class LexiconViewModel extends AndroidViewModel {
    private List<String> translatedWords;
    private List<LexiconWord> lexiconWords;

    //static private LiveData<List<WNExplanation>> wnExplanations;
    static private LiveData<List<WNExplanationWithCheck>> wnExplanationsWithChecks;
    static private LiveData<List<WNExplanation>> wnSynonyms;

    //functions both used for finding explanations
    //private MutableLiveData<List<String>> functionsToSearchFor = new MutableLiveData<>();
    private MutableLiveData<Pair<List<String>,String>> functionsToSearchForWithLangcode = new MutableLiveData<>();
    private MutableLiveData<List<String>> synonymsToSearchFor = new MutableLiveData<>();

    private Grammarlex sl;

    private static final String TAG = LexiconViewModel.class.getSimpleName();
    private GF gfClass;

    //The functions that we are going to find in wordnet
    private List<String> functions = new ArrayList<>();
    private List<String> synonyms = new ArrayList<>();

    //The repository for explanataions
    private WNExplanationRepository wnExplanationRepository;

    public LexiconViewModel(@NonNull Application application) {
        super(application);
        translatedWords = new ArrayList<>();
        lexiconWords = new ArrayList<>();
        sl = (Grammarlex) getApplication().getApplicationContext();
        gfClass = new GF(sl);
        wnExplanationRepository = new WNExplanationRepository(application);

        /*wnExplanations = Transformations.switchMap(functionsToSearchFor, functions -> {
           return wnExplanationRepository.getWNExplanations(functions);
        });*/

        wnExplanationsWithChecks = Transformations.switchMap(functionsToSearchForWithLangcode, pair -> {
            return wnExplanationRepository.getWNExplanationsWithCheck(pair.first, pair.second);
        });

        wnSynonyms = Transformations.switchMap(synonymsToSearchFor, synonyms -> {
            return wnExplanationRepository.getSynonyms(synonyms);
        });

    }

    public String wordTranslator(String word) {
        lexiconWords.clear();
        translatedWords.clear();
        functions.clear();
        synonyms.clear();

        String form = null;

        if (word != null && word.length() > 0) {
            for (FullFormEntry entry : sl.getSourceConcr().lookupWordPrefix(word)) {
                form = entry.getForm();
                for (MorphoAnalysis an : entry.getAnalyses()) {
                    if (!functions.contains(an.getLemma()) && sl.getTargetConcr().hasLinearization(an.getLemma())) {
                        String s = sl.getTargetConcr().linearize(Expr.readExpr(an.getLemma()));
                        functions.add(an.getLemma());
                        translatedWords.add(s);
                        lexiconWords.add(new LexiconWord(an.getLemma(), s, "", speechTag(an.getLemma()), "", ""));
                    }
                }
                if (lexiconWords.size() > 0)
                    break;
            }
        }

        //Needs to be called, updates explanation livedata
        // searchForFunctions(functions);
        searchForSynonyms(synonyms);
        searchForFunctionsWithCheck(functions);

        return form;
    }



    /*private void searchForFunctions(List<String> functions){
        functionsToSearchFor.setValue(functions);
    }*/

    private void searchForSynonyms(List<String> synonyms){
        synonymsToSearchFor.setValue(synonyms);
    }

    private void searchForFunctionsWithCheck(List<String> functions){
        functionsToSearchForWithLangcode.setValue(new Pair<>(functions,sl.getTargetLanguage().getLangCode()));
    }

    public List<LexiconWord> getLexiconWords() {
        return lexiconWords;
    }

    public List<String> getSynonyms(){
        return synonyms;
    }

    public void setLexiconWords(List<LexiconWord> lexiconWords){this.lexiconWords = lexiconWords;}

    /*public LiveData<List<WNExplanation>> getWNExplanations() {
        return wnExplanations;
    }*/

    public LiveData<List<WNExplanation>> getWNSynonyms() {
        return wnSynonyms;
    }

    public LiveData<List<WNExplanationWithCheck>> getWnExplanationsWithChecks() {
        return wnExplanationsWithChecks;
    }

    public String speechTag(String lemma){
        Expr e = Expr.readExpr("MkTag (Inflection" + wordClass(lemma) + " " + lemma + ")");
        return sl.getTargetConcr().linearize(e);
    }

    public String inflect(String lemma){
        Expr e = Expr.readExpr("MkDocument (NoDefinition \"\") (Inflection" + wordClass(lemma) + " " + lemma + ") \"\"");
        return sl.getTargetConcr().linearize(e);
    }

    public String wordClass(String lemma){
        return sl.getGrammar().getFunctionType(lemma).getCategory();
    }

    public void switchLanguages() {
        sl.switchLanguages();
    }

    public List<Language> getAvailableLanguages() {
        return sl.getAvailableLanguages();
    }

    public int getLanguageIndex(Language lang) {
        return sl.getLanguageIndex(lang);
    }

    public Language getSourceLanguage() {
        return sl.getSourceLanguage();
    }

    public Language getTargetLanguage(){
        return sl.getTargetLanguage();
    }

    public void setSourceLanguage(Language lang) {
        sl.setSourceLanguage(lang);
    }

    public void setTargetLanguage(Language lang) {
        sl.setTargetLanguage(lang);
    }

    public Concr getTargetConcr() { return sl.getTargetConcr();}

}