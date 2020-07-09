package org.grammaticalframework.grammarlex.ViewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.grammaticalframework.grammarlex.Grammarlex;
import org.grammaticalframework.grammarlex.gf.GF;
import org.grammaticalframework.pgf.Expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TranslateExerciseViewModel extends AndroidViewModel {

    private static final String TAG = TranslateExerciseViewModel.class.getSimpleName();

    private Grammarlex mGrammarlex;
    private GF gf;
//    private TranslateExercise translateExercise;

    private String linearizedTranslateWord;
    private List<String> linearizedAlternatives;

//    private ExerciseRepository exerciseRepository;

    private int correctAnswers = 0;
    private int incorrectAnswers = 0;


//    private LiveData<TranslateExercise> unsolvedExercise;


    public TranslateExerciseViewModel(Application application){
        super(application);
        mGrammarlex = (Grammarlex) getApplication().getApplicationContext();
        gf = new GF(mGrammarlex);

        linearizedAlternatives = new ArrayList<>();

    }

/*    public void loadWord(TranslateExercise te) {
        this.translateExercise = te;
        Expr e = Expr.readExpr(te.getTranslateFunction());
        linearizedTranslateWord = mGrammarlex.getSourceConcr().linearize(e);
        linearizedAlternatives.clear();
        for (String s : te.getAlternativeFunctions()) {
            e = Expr.readExpr(s);
            linearizedAlternatives.add(mGrammarlex.getTargetConcr().linearize(e));
        }
        e = Expr.readExpr(te.getTranslateFunction());
        linearizedAlternatives.add(mGrammarlex.getTargetConcr().linearize(e));
    }
*/
    public String getWord() {
        return linearizedTranslateWord;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void setIncorrectAnswers(int incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }

    public List<String> getAlternatives() {
        Collections.shuffle(linearizedAlternatives);
        return linearizedAlternatives;
    }

    /*public boolean checkCorrectAnswer(String answer) {
        Expr e = Expr.readExpr(translateExercise.getTranslateFunction());
        String correct = mGrammarlex.getTargetConcr().linearize(e);
        return correct.equals(answer);
    }

    public void getNewExercise() {
        exerciseRepository.addSolvedTranslateExercise(translateExercise);
    }

    public LiveData<TranslateExercise> getUnsolvedExercise() {
        return unsolvedExercise;
    }*/
}
