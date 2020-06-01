package org.grammaticalframework.grammarlex.ViewModel;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import java.io.Serializable;

public class LexiconWord implements Serializable {
    private String lemma;
    private String word;
    private String explanation;
    private String tag;
    private String synonymCode;
    private String synonymWords;
    //The status of the linearization, i.e. if it has been checked or is guessed
    private String status = null;
    //The language code for the word
    private String langcode = null;

    public LexiconWord(String lemma, String word, String explanation, String tag, String synonymCode, String synonymWords) {
        this.lemma = lemma;
        this.word = word;
        this.explanation = explanation;
        this.tag = tag;
        this.synonymCode = synonymCode;
        this.synonymWords = synonymWords;
    }

    public String getLemma() {return lemma; }

    public String getWord() {
        return word;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getTag(){
        return tag;
    }

    public String getSynonymCode(){
        return synonymCode;
    }

    public void setSynonymCode(String synonyms){
        this.synonymCode = synonyms;
    }
    
    public void setSynonymWords(String synonymWords){this.synonymWords = synonymWords;}

    public String getSynonymWords() {return synonymWords;}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLangcode() {
        return langcode;
    }

    public void setLangcode(String langcode) {
        this.langcode = langcode;
    }

    public CharSequence toDescription() {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(tag);
        builder.append(". ");
        builder.append(word);
        builder.setSpan(
                new StyleSpan(Typeface.BOLD),
                0, builder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(" ");
        if (status == null || !status.equals("checked")) {
            builder.append("(?) ");
            builder.setSpan(
                    new ForegroundColorSpan(Color.RED),
                    builder.length()-4, builder.length()-1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        builder.append(explanation);
        return (CharSequence) builder;
    }
}
