package org.grammaticalframework.grammarlex.View.Fragments;

import java.util.*;

import android.graphics.Typeface;
import android.os.Bundle;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.*;
import android.widget.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import org.daison.IdValue;
import org.daison.ReadTransaction;
import org.grammaticalframework.grammarlex.ViewModel.LexiconWord;
import org.grammaticalframework.grammarlex.ViewModel.SenseSchema;
import org.grammaticalframework.pgf.Expr;
import org.grammaticalframework.grammarlex.*;
import org.grammaticalframework.grammarlex.phrasebook.*;
import org.grammaticalframework.grammarlex.phrasebook.syntax.*;
import org.grammaticalframework.pgf.ExprApplication;

/**
 * Created by matilda on 04/04/16.
 */
public class TranslatorFragment extends Fragment {
    protected Model model;
    private TTS mTts;

	private TextView origin,target;
	private ListView list;
    SyntaxTree phrase;

    ChoiceContext mContext;
    ArrayAdapter<SyntacticChoice> mAdapter;

    public static TranslatorFragment newInstance(SyntaxTree phrase) {
        TranslatorFragment translatorFragment = new TranslatorFragment();
        Bundle args = new Bundle();
        args.putSerializable("phrase", phrase);
        translatorFragment.setArguments(args);
        return translatorFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        model = Model.getInstance();

        String id = getArguments().getString("id");
        int position = getArguments().getInt("position");
        List<SyntaxTree> sentences = (id == null) ? model.getSentences() : model.getGroup(id);
        phrase = sentences.get(position);

        mTts        = new TTS(getActivity());
        mContext    = new ChoiceContext();

		mAdapter =
			new ArrayAdapter<SyntacticChoice>(getActivity(), R.layout.spinner_input_list_item, mContext.getChoices()) {
				LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				@Override
				public View getView (int position, View convertView, ViewGroup parent) {
					SyntacticChoice choice = mContext.getChoices().get(position);
					View view = null;
					if (choice.getNode() instanceof SyntaxNodeBoolean) {
						view = createCheckBoxInputView(inflater, choice, (SyntaxNodeBoolean) choice.getNode(), parent);
					} else if (choice.getNode() instanceof SyntaxNodeOption) {
					    if (((SyntaxNodeOption) choice.getNode()).isLexicon())
						    view = createLexiconInputView(inflater, choice, (SyntaxNodeOption) choice.getNode(), parent);
					    else
                            view = createSpinnerInputView(inflater, choice, (SyntaxNodeOption) choice.getNode(), parent);
					} else if (choice.getNode() instanceof SyntaxNodeNumeral) {
						view = createNumeralInputView(inflater, choice, (SyntaxNodeNumeral) choice.getNode(), parent);
					}
					return view;
				}
			};
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_translator, container, false);
        origin = (TextView) view.findViewById(R.id.origin_phrase);
        target = (TextView) view.findViewById(R.id.target_phrase);

        list   = (ListView) view.findViewById(R.id.input_holder);
        list.setAdapter(mAdapter);

        TextView phrase_title = (TextView) view.findViewById(R.id.phrase_title);
        phrase_title.setText(getArguments().getString("title"));

		ImageView button = (ImageView) view.findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				mTts.speak(Grammarlex.get().getTargetLanguage().getLangCode(), (String) target.getText());
            }
        });

        ImageView backButton = view.findViewById(R.id.lexicon_details_back);
        backButton.setOnClickListener((v) -> {
            NavController navController = Navigation.findNavController(getActivity().findViewById(R.id.nav_host_fragment));
            navController.popBackStack();
        });

        updateSyntax();
        return view;
    }
    
    private View createSpinnerInputView(LayoutInflater inflater, final SyntacticChoice choice, final SyntaxNodeOption options, ViewGroup parent) {
        View view = inflater.inflate(R.layout.spinner_input_list_item, parent, false);
        TextView viewLabel = (TextView) view.findViewById(R.id.text_view_spinner);
        Spinner spinner = (Spinner) view.findViewById(R.id.choice_spinner);

        Grammarlex gl = Grammarlex.get();

        String desc = options.getDesc();
        if (desc == null || desc.isEmpty()) {
            viewLabel.setVisibility(View.GONE);
        } else {
            Expr e = Expr.readExpr(desc);
            viewLabel.setText(gl.getSourceConcr().linearize(e));
        }

        final ArrayAdapter<SyntaxNode> adapter = new ArrayAdapter<SyntaxNode>(getActivity(), android.R.layout.simple_list_item_1, options.getOptions()) {
            public View getView(int position, View convertView, ViewGroup parent) {
                Expr e = Expr.readExpr(getItem(position).getDesc());
                String lin = Grammarlex.get().getSourceConcr().linearize(e);
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setText(lin);
                return view;
            }
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                Expr e = Expr.readExpr(getItem(position).getDesc());
                String lin = Grammarlex.get().getSourceConcr().linearize(e);
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setText(lin);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spinner.setAdapter(adapter);
        
        spinner.setSelection(choice.getChoice());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position != choice.getChoice()) {
					choice.setChoice(position);
					updateSyntax();
				}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
	}

    private View createLexiconInputView(LayoutInflater inflater, final SyntacticChoice choice, final SyntaxNodeOption options, ViewGroup parent) {
        View view = inflater.inflate(R.layout.lexicon_input_list_item, parent, false);
        TextView viewLabel = (TextView) view.findViewById(R.id.text_view_spinner);
        Spinner spinner = (Spinner) view.findViewById(R.id.choice_spinner);

        Grammarlex gl = Grammarlex.get();

        String desc = options.getDesc();
        if (desc == null || desc.isEmpty()) {
            viewLabel.setVisibility(View.GONE);
        } else {
            Expr e = Expr.readExpr(desc);
            viewLabel.setText(gl.getSourceConcr().linearize(e));
        }

        List<LexiconWord> lexiconWords = new ArrayList<>();
        for (SyntaxNode node : options.getOptions()) {
            Expr e = Expr.readExpr(node.getDesc());
            ExprApplication eapp = e.unApp();
            if (eapp == null)
                continue;

            if (eapp.getArguments().length == 0) {
                String form = gl.getTargetConcr().hasLinearization(eapp.getFunction()) ? gl.getTargetConcr().linearize(e) : null;
                String word = gl.getSourceConcr().hasLinearization(eapp.getFunction()) ? gl.getSourceConcr().linearize(e) : null;
                LexiconWord lexiconWord = new LexiconWord(node.getDesc(),null, word, form);
                lexiconWords.add(lexiconWord);

                try (ReadTransaction t = gl.getDatabase().newReadTransaction()) {
                    for (IdValue<SenseSchema.Lexeme> idvalue : t.atIndex(SenseSchema.lexemes_fun, eapp.getFunction())) {
                        SenseSchema.Status status = SenseSchema.Status.Checked;
                        for (SenseSchema.LanguageStatus lang_status : idvalue.getValue().status) {
                            if (lang_status.language.equals(gl.getSourceLanguage().getConcrete()) ||
                                lang_status.language.equals(gl.getTargetLanguage().getConcrete())) {
                                if (status.ordinal() > lang_status.status.ordinal())
                                    status = lang_status.status;
                            }
                        }
                        lexiconWord.setStatus(status);

                        if (idvalue.getValue().synset_id != null) {
                            SenseSchema.Synset synset = t.at(SenseSchema.synsets, idvalue.getValue().synset_id);
                            lexiconWord.setGloss(synset.gloss);
                        }
                    }
                }
            } else {
                String form = gl.getTargetConcr().linearize(e);
                String word = gl.getSourceConcr().linearize(e);
                LexiconWord lexiconWord = new LexiconWord(node.getDesc(),null, word, form);
                lexiconWord.setStatus(SenseSchema.Status.Checked);
                lexiconWords.add(lexiconWord);
            }
        }

        final ArrayAdapter<LexiconWord> adapter = new ArrayAdapter<LexiconWord>(getActivity(), android.R.layout.simple_list_item_1,lexiconWords) {
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setText(getItem(position).getWord());
                return view;
            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setText(getItem(position).toDescription());
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spinner.setAdapter(adapter);
        spinner.setSelection(choice.getChoice());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != choice.getChoice()) {
                    choice.setChoice(position);
                    updateSyntax();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }

    private class NumericKeyBoardTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return source;
        }
    }
    
    private View createNumeralInputView(LayoutInflater inflater, final SyntacticChoice choice, final SyntaxNodeNumeral numeral, ViewGroup parent) {
        View view = inflater.inflate(R.layout.number_input_list_item, parent, false);
        TextView viewLabel = (TextView) view.findViewById(R.id.textView_number);
        final SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        final EditText editNumber = (EditText) view.findViewById(R.id.editNumber);

        Grammarlex gl = Grammarlex.get();

        String desc = choice.getNode().getDesc();
        if (desc == null || desc.isEmpty()) {
            viewLabel.setVisibility(View.GONE);
        } else {
            Expr e = Expr.readExpr(desc);
            viewLabel.setText(gl.getSourceConcr().linearize(e));
        }

        seekBar.setProgress(choice.getChoice());
        editNumber.setText(Integer.toString(choice.getChoice()));

        editNumber.setTransformationMethod(new NumericKeyBoardTransformationMethod());
        editNumber.requestFocus();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editNumber.setText(Integer.toString(progress+1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        editNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
				int number;
				boolean update = false;
				try {
					if(editNumber.getText().toString().equals("")) {
						number = numeral.getMin();
					} else {
						number = Integer.parseInt(editNumber.getText().toString());
						if (number < numeral.getMin()) {
							number = numeral.getMin();
							update = true;
						}
						if (number > numeral.getMax()) {
							number = numeral.getMax();
							update = true;
						}
						editNumber.setInputType(0);
					}
				} catch (NumberFormatException e) {
					number = choice.getChoice();
					update = true;
				}
                choice.setChoice(number);
                if (update)
					editNumber.setText(Integer.toString(number));
                seekBar.setProgress(number-numeral.getMin());
                updateSyntax();
            }
        });

        return view;
	}

    private View createCheckBoxInputView(LayoutInflater inflater, final SyntacticChoice choice, final SyntaxNodeBoolean options, ViewGroup parent) {
        View view = inflater.inflate(R.layout.checkbox_input_list_item, parent, false);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.choice_checkbox);

		String desc = options.getDesc();
        if (desc != null && !desc.isEmpty()) {
            Expr e = Expr.readExpr(desc);
            checkBox.setText(Grammarlex.get().getSourceConcr().linearize(e));
        }

        checkBox.setChecked(choice.getChoice() == 1);
        checkBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
				int position = checkBox.isChecked() ? 1 : 0;
				if (position != choice.getChoice()) {
					choice.setChoice(position);
					updateSyntax();
				}
            }
        });

        return view;
	}

    public void updateSyntax() {
		mContext.reset();

        Expr expr = phrase.getAbstractSyntax(mContext);
        origin.setText(Grammarlex.get().getSourceConcr().linearize(expr));
        target.setText(Grammarlex.get().getTargetConcr().linearize(expr));
		mAdapter.notifyDataSetChanged();
	}
}
