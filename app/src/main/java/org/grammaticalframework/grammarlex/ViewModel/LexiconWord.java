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
    private String gloss;
    private String tag;
    private Long synset_id;
    private SenseSchema.Status status;
    private String synonymWords;

    public LexiconWord(String lemma, String word, String gloss, String tag, Long synset_id, String synonymWords) {
        this.lemma = lemma;
        this.word = word;
        this.gloss = gloss;
        this.tag = tag;
        this.synset_id = synset_id;
        this.status = null;
        this.synonymWords = synonymWords;
    }

    public String getLemma() {return lemma; }

    public String getWord() {
        return word;
    }

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) { this.gloss = gloss; }

    public String getTag(){
        return tag;
    }

    public Long getSynsetId(){
        return synset_id;
    }

    public void setSynsetId(Long synset_id){
        this.synset_id = synset_id;
    }
    
    public void setSynonymWords(String synonymWords){this.synonymWords = synonymWords;}

    public String getSynonymWords() {return synonymWords;}

    public SenseSchema.Status getStatus() {
        return status;
    }

    public void setStatus(SenseSchema.Status status) {
        this.status = status;
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
        if (status == null || !status.equals(SenseSchema.Status.Checked)) {
            builder.append("(?) ");
            builder.setSpan(
                    new ForegroundColorSpan(Color.RED),
                    builder.length()-4, builder.length()-1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        builder.append(gloss);
        return (CharSequence) builder;
    }
}
