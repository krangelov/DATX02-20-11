package org.grammaticalframework.grammarlex.View.Fragments;

import android.os.Bundle;

import java.util.*;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.grammaticalframework.grammarlex.Grammarlex;
import org.grammaticalframework.grammarlex.R;
import org.grammaticalframework.grammarlex.phrasebook.Model;
import org.grammaticalframework.grammarlex.phrasebook.syntax.SyntaxTree;
import org.grammaticalframework.pgf.Expr;


/**
 * Created by Björn on 2016-04-25.
 */
public class PhraseListFragment extends Fragment {

    protected Model model;
    private String title;
    private String id;

	public static PhraseListFragment newInstance(String title) {
        PhraseListFragment fragment = new PhraseListFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    public static PhraseListFragment newInstance(String title, String id) {
        PhraseListFragment fragment = new PhraseListFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("id",    id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        model = Model.getInstance();
        if (getArguments() != null) {
            title = getArguments().getString("title");
            id    = getArguments().getString("id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_phrase_list, container, false);

		final List<SyntaxTree> sentences = (id == null) ? model.getSentences() : model.getGroup(id);
        ArrayAdapter<SyntaxTree> adapter = new ArrayAdapter<SyntaxTree>(getActivity(), R.layout.phrase_list_item, sentences) {
            public View	getView(int position, View convertView, ViewGroup parent) {
                Expr e = Expr.readExpr(sentences.get(position).getDesc());
                String label = Grammarlex.get().getSourceConcr().linearize(e);
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setText(label);
                return view;
            }
        };

        final ListView phraseListView = (ListView) view.findViewById(R.id.phrase_listView);
        phraseListView.setAdapter(adapter);

        phraseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long item_id) {
                SyntaxTree phrase = sentences.get(position);
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                bundle.putString("title", (String) ((TextView) view).getText());
                bundle.putString("id", id);
                NavHostFragment.findNavController(PhraseListFragment.this).navigate(R.id.action_phrasebookFragment_to_translatorFragment, bundle);
            }
        });

        return view;
    }
}
