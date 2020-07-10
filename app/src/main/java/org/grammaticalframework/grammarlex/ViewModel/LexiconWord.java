package org.grammaticalframework.grammarlex.ViewModel;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LexiconWord implements Serializable {
    private String lemma;
    private String word;
    private String gloss;
    private String tag;
    private Long synset_id;
    private SenseSchema.Status status;
    private SenseSchema.ImageInfo[] images;
    private List<String> synonymWords;

    public LexiconWord(String lemma, String tag, String word) {
        this.lemma = lemma;
        this.word = word;
        this.gloss = "";
        this.tag = tag;
        this.synset_id = null;
        this.status = null;
        this.images = null;
        this.synonymWords = new ArrayList<>();
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

    public List<String> getSynonymWords() { return synonymWords; }

    public void addSynonymWord(String synonymWord) {
        this.synonymWords.add(synonymWord);
    }

    public SenseSchema.Status getStatus() {
        return status;
    }

    public void setStatus(SenseSchema.Status status) {
        this.status = status;
    }

    public SenseSchema.ImageInfo[] getImages() {
        return images;
    }

    public void setImages(SenseSchema.ImageInfo[] images) {
        this.images = images;
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
