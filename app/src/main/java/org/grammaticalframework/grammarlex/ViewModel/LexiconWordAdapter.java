package org.grammaticalframework.grammarlex.ViewModel;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import org.grammaticalframework.grammarlex.R;
import org.grammaticalframework.grammarlex.View.Fragments.BaseFragment;
import org.grammaticalframework.grammarlex.View.Fragments.MainLexiconFragmentDirections;

import java.util.ArrayList;
import java.util.List;

public class LexiconWordAdapter extends RecyclerView.Adapter<LexiconWordAdapter.WordItemViewHolder> {

    private List<LexiconWord> lexiconWordList;
    private BaseFragment ldFragment;
    private NavController navController;
    private static final String TAG = LexiconWordAdapter.class.getSimpleName();

    public LexiconWordAdapter(BaseFragment ldFragment, NavController navController) {
        this.ldFragment = ldFragment;
        this.navController = navController;
        lexiconWordList = new ArrayList<>();
    }

    @Override
    public LexiconWordAdapter.WordItemViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        final View wordItemView = inflater.inflate(R.layout.lexicon_recycleritem, parent, false);
        WordItemViewHolder viewHolder = new WordItemViewHolder(wordItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(LexiconWordAdapter.WordItemViewHolder viewHolder, int position) {

        LexiconWord lexiconWord = lexiconWordList.get(position);

        // Skickar med ordet (lemma + translatedWord) till LexiconDetailsFragment
        MainLexiconFragmentDirections.ActionLexiconFragmentToLexiconDetailsFragment action = MainLexiconFragmentDirections.actionLexiconFragmentToLexiconDetailsFragment(lexiconWord);
        action.setMessage(lexiconWord);
        action.setMessage2(lexiconWord.getLemma());

        viewHolder.itemView.setOnClickListener((v) -> {
            navController.navigate(action);
        });

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(lexiconWord.getTag());
        builder.append(". ");
        builder.append(lexiconWord.getWord());
        builder.setSpan(
                new StyleSpan(Typeface.BOLD),
                0, builder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(" ");
        if (lexiconWord.getStatus() == null || !lexiconWord.getStatus().equals("checked")) {
            builder.append("(?) ");
            builder.setSpan(
                    new ForegroundColorSpan(Color.RED),
                    builder.length()-4, builder.length()-1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        builder.append(lexiconWord.getExplanation());
        viewHolder.wordTextView.setText(builder);
    }

    @Override
    public int getItemCount() {
        return lexiconWordList.size();
    }

    public void setLexiconWordList(List<LexiconWord> lexiconWordList) {
        this.lexiconWordList = lexiconWordList;
        notifyDataSetChanged(); //TODO: change this, not optimal
    }

    public class WordItemViewHolder extends RecyclerView.ViewHolder {

        public TextView wordTextView;

        public WordItemViewHolder(View itemView) {
            super(itemView);

            wordTextView = (TextView) itemView;
        }
    }
}
