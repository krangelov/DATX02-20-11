package org.grammaticalframework.grammarlex.View.Fragments;

import android.os.Bundle;

import java.util.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;

import org.grammaticalframework.grammarlex.R;
import org.grammaticalframework.grammarlex.phrasebook.Model;
import org.grammaticalframework.grammarlex.phrasebook.syntax.SyntaxTree;


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
        ArrayAdapter<SyntaxTree> adapter = new ArrayAdapter<SyntaxTree>(getActivity(), R.layout.phrase_list_item, sentences);

        final ListView phraseListView = (ListView) view.findViewById(R.id.phrase_listView);
        phraseListView.setAdapter(adapter);

        phraseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long item_id) {
                SyntaxTree phrase = sentences.get(position);
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                bundle.putString("id", id);
                NavHostFragment.findNavController(PhraseListFragment.this).navigate(R.id.action_phrasebookFragment_to_translatorFragment, bundle);
            }
        });

        return view;
    }
}
