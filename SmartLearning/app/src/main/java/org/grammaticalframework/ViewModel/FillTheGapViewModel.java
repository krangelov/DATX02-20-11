package org.grammaticalframework.ViewModel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import org.grammaticalframework.FillTheGapClass;
import org.grammaticalframework.Repository.FillTheGapExercise;
import org.grammaticalframework.Repository.FillTheGapExerciseRepository;
import org.grammaticalframework.SmartLearning;
import org.grammaticalframework.gf.GF;
import org.grammaticalframework.pgf.Bracket;
import org.grammaticalframework.pgf.Concr;
import org.grammaticalframework.pgf.Expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FillTheGapViewModel extends AndroidViewModel {


    private static final String TAG = FillTheGapViewModel.class.getSimpleName();
    Concr source;
    Concr target;
    private FillTheGapExercise fillthegap;
    private Expr expression;
    private GF gf;

    private FillTheGapExercise ftge;

    private FillTheGapExerciseRepository fillTheGapExerciseRepository;
    private LiveData<List<FillTheGapExercise>> fillTheGapExercises;

    static private LiveData<FillTheGapExercise> fillTheGapExercise;
    private MutableLiveData<String> nextExercise = new MutableLiveData<>();

    private String redactedWord;
    private String linearizedSentence;
    private ArrayList<String> inflections = new ArrayList<>();

    public FillTheGapViewModel(Application application){
        super(application);

        Log.d(TAG, "INSIDE OF FILLTHEGAPVIEWMODEL");

        SmartLearning mSmartLearning = (SmartLearning) getApplication().getApplicationContext();
        gf = new GF(mSmartLearning);
        source = mSmartLearning.getSourceConcr();
        target = mSmartLearning.getTargetConcr();

        fillTheGapExerciseRepository = new FillTheGapExerciseRepository(application);

        //Make sure that the exercise shown depends on what exercise we want to show
        fillTheGapExercise = Transformations.switchMap(nextExercise, function -> {
            return fillTheGapExerciseRepository.getFillTheGapExercise(function);
        });

        nextExercise.setValue("abandon_5_V2");
    }

    public void loadWord(FillTheGapExercise ftge) {
        this.ftge = ftge;
        expression = Expr.readExpr(ftge.getAbstractSyntaxTree());
        linearizedSentence = target.linearize(expression);
        Object[] bs = target.bracketedLinearize(expression);
        findWordToRedact(bs[0]);
        setRedactedWord();
    }

    public String getSentence() {
        return linearizedSentence;
        //return gf.translateWord("apple");
    }

    public List<String> getInflections() {
        List<String> notAllInflections = new ArrayList<>();
        for(int i = 0; i < 5 && i < inflections.size(); i++){
            notAllInflections.add(inflections.get(i));
        }
        Collections.shuffle(notAllInflections);
        return notAllInflections;
    }

    public boolean checkCorrectAnswer(String answer){
        if(answer.equals(redactedWord)){
            return true;
        } else{
            return false;
        }
    }


    public void getNewSentence(){
        nextExercise.setValue("spare_V3");
    }

    /*private void bracketLinearize(){
        fillthegap.setBs(target.bracketedLinearize(fillthegap.getExpr()));
        scanBracket(fillthegap.getBs()[0]);
    }*/

    private void findWordToRedact(Object bs){
            if(bs instanceof Bracket){
                if(((Bracket) bs).fun.equals(ftge.getFunctionToReplace())){
                    redactedWord = ((Bracket) bs).children[0].toString();
                    inflect(((Bracket) bs).fun);
                } else{
                    if(((Bracket) bs).children != null) {
                        for(Object child : ((Bracket) bs).children) {
                            findWordToRedact(child);
                        }
                    }
                }
            }
    }

    private void scanBracket(Object bs) {
        Log.d(TAG, "Bracketedscanned");
        if(bs instanceof Bracket){
            Log.d(TAG, ":DDDD");
            if(((Bracket) bs).cat.equals("V")|| ((Bracket) bs).cat.equals("V2")|| ((Bracket) bs).cat.equals("V3")){
                Log.d(TAG, "___________________________________________");
                String verb = ((Bracket) bs).fun;
                inflect(verb);
                return; //After the first verb has been found, stop
            }
            if (((Bracket) bs).children != null){
                for(Object child : ((Bracket) bs).children){
                    scanBracket(child);
                }
            }
        }else if(bs instanceof String){
        }
    }

    private void inflect(String verb) {
        Log.d(TAG, verb);
        if(!inflections.isEmpty()) {
            inflections.clear();
        }
        Expr e = Expr.readExpr(verb);
        for(Map.Entry<String,String> entry  : target.tabularLinearize(e).entrySet()) {
            if(entry.getKey().contains("Act")){
                inflections.add(entry.getValue()); //Add inflections
            }
        }

    }

    private void setRedactedWord(){
        String targetLinearization = target.linearize(expression);
        ArrayList<String> sentence = new ArrayList<>();
        Collections.addAll(sentence, targetLinearization.split(" "));
        //Prepend the correct inflection to inflections
        if(inflections.size() < 1)
            return;
        String temp = inflections.get(0);
        int indexInflection = inflections.indexOf(redactedWord);
        inflections.set(0, redactedWord);
        inflections.set(indexInflection, temp);

        if(linearizedSentence.contains(redactedWord)) {
            Log.d(TAG, "ooga booga");
            int toRemove = sentence.indexOf(redactedWord);
            String redacted = "";
            for (char c : redactedWord.toCharArray()) {
                redacted += "_";
                sentence.set(toRemove, redacted);
                linearizedSentence = android.text.TextUtils.join(" ", sentence);
            }
        }
    }

    public LiveData<FillTheGapExercise> getFillTheGapExercise() {
        return fillTheGapExercise;
    }
}
