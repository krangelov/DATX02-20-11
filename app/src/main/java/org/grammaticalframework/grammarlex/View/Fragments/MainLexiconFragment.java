package org.grammaticalframework.grammarlex.View.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import org.grammaticalframework.grammarlex.Grammarlex;
import org.grammaticalframework.grammarlex.Language;
import org.grammaticalframework.grammarlex.R;
import org.grammaticalframework.grammarlex.TranslatorInputMethodService;
import org.grammaticalframework.grammarlex.View.FragmentFactory;
import org.grammaticalframework.grammarlex.ViewModel.LexiconViewModel;
import org.grammaticalframework.grammarlex.ViewModel.LexiconWordAdapter;

import java.util.Timer;
import java.util.TimerTask;

public class MainLexiconFragment extends BaseFragment implements AppBarLayout.OnOffsetChangedListener {

    private LexiconViewModel lexiconVM;
    private final BaseFragment lexiconDetailsFragment = FragmentFactory.createLexiconDetailsFragment();
    private EditText search_bar;
    private TextView search_word;
    private AppBarLayout lexicon_toolbar;
    private Spinner fromLanguageSpinner;
    private Spinner toLanguageSpinner;
    private ImageButton switch_button;
    private ImageButton expand_button;
    private ImageButton search_clear_button;
    private TextView from_lang_short;
    private TextView to_lang_short;
    private ImageView dropDown_icon;
    private RecyclerView rvLexicon;
    private LexiconWordAdapter wordAdapter;

    private int listSize;
    private boolean appBarExpanded = false;
    private boolean appBarCollapsed = true;
    private float fromOffsetX;
    private float fromOffsetY;
    private float toOffsetX;
    private float toOffsetY;
    private float switchOffsetX;
    private float switchOffsetY;
    private boolean updateVM;
    private static final String TAG = MainLexiconFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lexiconVM = new ViewModelProvider(getActivity()).get(LexiconViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_lexicon, container, false);

        search_bar = fragmentView.findViewById(R.id.lexicon_searchbar);
        search_word = fragmentView.findViewById(R.id.lexicon_searchword);
        fromLanguageSpinner = fragmentView.findViewById(R.id.lexicon_from_language);
        toLanguageSpinner = fragmentView.findViewById(R.id.lexicon_to_language);
        lexicon_toolbar = fragmentView.findViewById(R.id.lexicon_appbar);
        switch_button = fragmentView.findViewById(R.id.lexicon_switch_collapsed);
        expand_button = fragmentView.findViewById(R.id.lexicon_expand_button);
        dropDown_icon = fragmentView.findViewById(R.id.lexicon_dropdown_icon);
        search_clear_button = fragmentView.findViewById(R.id.lexicon_search_clear);
        from_lang_short = fragmentView.findViewById(R.id.lexicon_from_short);
        to_lang_short = fragmentView.findViewById(R.id.lexicon_to_short);

        // Lower the dropdown menu so that the currently selected item can be seen
        toLanguageSpinner.setDropDownVerticalOffset(100);
        fromLanguageSpinner.setDropDownVerticalOffset(100);

        search_clear_button.setVisibility(View.GONE);

        Bundle extras = search_bar.getInputExtras(true);
        extras.putBoolean("show_language_toggle", false);

        Grammarlex gl = Grammarlex.get();

        // Adapter for list items to show in recycler view
        ArrayAdapter<Language> spinnerLanguages = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, gl.getAvailableLanguages());
        spinnerLanguages.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        fromLanguageSpinner.setAdapter(spinnerLanguages);
        toLanguageSpinner.setAdapter(spinnerLanguages);


        // Set initial languages in spinners and text
        updateVM = false;
        fromLanguageSpinner.setSelection(gl.getLanguageIndex(gl.getSourceLanguage()));
        toLanguageSpinner.setSelection(gl.getLanguageIndex(gl.getTargetLanguage()));
        updateVM = true;
        from_lang_short.setText(parseLangCode(gl.getSourceLanguage().getLangCode()));
        to_lang_short.setText(parseLangCode(gl.getTargetLanguage().getLangCode()));

        // Listeners for views
        search_clear_button.setOnClickListener((v) -> {
            search_bar.getText().clear();
        });

        switch_button.setOnClickListener((v) -> {
            switch_button.animate().rotationBy(180).setDuration(200).start();
            switchLanguages();
            // Disable for 1 second after pressing to avoid spam-clicking
            switch_button.setEnabled(false);

            Timer buttonTimer = new Timer();
            buttonTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            switch_button.setEnabled(true);
                        }
                    });
                }
            }, 1000);
        });

        expand_button.setOnClickListener((v) -> {
            if (appBarExpanded) {
                lexicon_toolbar.setExpanded(false);
            } else {
                lexicon_toolbar.setExpanded(true);
            }
        });

        search_bar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    search_bar.clearFocus();
                    hideKeyboard(textView);
                }
                return false;
            }
        });

        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 1) {
                    String form = lexiconVM.wordTranslator(editable.toString());
                    if (form == null)
                        search_word.setText(R.string.no_word_matches);
                    else
                        search_word.setText(form);
                    search_clear_button.setVisibility(View.VISIBLE);
                } else {
                    lexiconVM.wordTranslator("");
                    search_word.setText("");
                    search_clear_button.setVisibility(View.GONE);
                }
                wordAdapter.setLexiconWordList(lexiconVM.getLexiconWords());
            }
        });

        fromLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (updateVM) {
                    Language lang = (Language) parent.getSelectedItem();
                    Grammarlex.get().setSourceLanguage(lang);

                    from_lang_short.setText(parseLangCode(lang.getLangCode()));
                }
                if (TranslatorInputMethodService.getInstance() != null)
                    TranslatorInputMethodService.getInstance().handleChangeSourceLanguage(gl.getSourceLanguage());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        toLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Update view model only when the user is manually selecting a language, not in initialization or language switching
                if (updateVM) {
                    Language lang = (Language) parent.getSelectedItem();
                    Grammarlex.get().setTargetLanguage(lang);
                    to_lang_short.setText(parseLangCode(lang.getLangCode()));
                }
                if (TranslatorInputMethodService.getInstance() != null)
                    TranslatorInputMethodService.getInstance().handleChangeTargetLanguage(gl.getTargetLanguage());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Initiate recycler view, set adapter
        NavController navController = Navigation.findNavController(getActivity().findViewById(R.id.nav_host_fragment));
        rvLexicon = (RecyclerView) fragmentView.findViewById(R.id.lexicon_recyclerview);
        wordAdapter = new LexiconWordAdapter(lexiconDetailsFragment, navController);
        rvLexicon.setAdapter(wordAdapter);
        rvLexicon.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvLexicon.addItemDecoration(new DividerItemDecoration((rvLexicon.getContext()), DividerItemDecoration.VERTICAL));

        if (getArguments() != null) {
            CharSequence searchString = getArguments().getCharSequence(Intent.EXTRA_PROCESS_TEXT);
            if (searchString != null) {
                search_bar.setText(searchString);
                wordAdapter.notifyDataSetChanged();
            }
        }

        return fragmentView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Observe livedata from viwemodel
        wordAdapter.setLexiconWordList(lexiconVM.getLexiconWords());
        rvLexicon.addItemDecoration(new DividerItemDecoration((rvLexicon.getContext()), DividerItemDecoration.VERTICAL));
    }

    private void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /*
    * Handles animation functionality, translates the language abbreviations and switch button to new positions when the toolbar expands
    * Sets the expand button to face up or down depending on whether the toolbar is expanded or not
     */
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        appBarExpanded = (verticalOffset == 0);
        appBarCollapsed = Math.abs(verticalOffset) >= lexicon_toolbar.getTotalScrollRange();
        float normalizedOffset = Math.abs(((float)verticalOffset)/lexicon_toolbar.getTotalScrollRange());

        if (appBarExpanded) {
            updateAnimationOffsets();
            translateView(from_lang_short, fromOffsetX, fromOffsetY, 1);
            translateView(to_lang_short, toOffsetX, toOffsetY, 1);
            translateView(switch_button, switchOffsetX, switchOffsetY, 1);

            fromLanguageSpinner.setVisibility(View.VISIBLE);
            toLanguageSpinner.setVisibility(View.VISIBLE);
            from_lang_short.setVisibility(View.INVISIBLE);
            to_lang_short.setVisibility(View.INVISIBLE);

            dropDown_icon.setRotation(180);
        } else if (appBarCollapsed) {
            updateAnimationOffsets();
            translateView(from_lang_short, fromOffsetX, fromOffsetY, 0);
            translateView(to_lang_short, toOffsetX, toOffsetY, 0);
            translateView(switch_button, switchOffsetX, switchOffsetY, 0);

            from_lang_short.setVisibility(View.VISIBLE);
            to_lang_short.setVisibility(View.VISIBLE);

            dropDown_icon.setRotation(0);
        } else {
            translateView(from_lang_short, fromOffsetX, fromOffsetY, 1 - normalizedOffset);
            translateView(to_lang_short, toOffsetX, toOffsetY, 1 - normalizedOffset);
            translateView(switch_button, switchOffsetX, switchOffsetY, 1 - normalizedOffset);

            fromLanguageSpinner.setVisibility(View.INVISIBLE);
            toLanguageSpinner.setVisibility(View.INVISIBLE);
            from_lang_short.setVisibility(View.VISIBLE);
            to_lang_short.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        lexicon_toolbar.addOnOffsetChangedListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        lexicon_toolbar.removeOnOffsetChangedListener(this);
    }

    private void translateView(View v, float offsetX, float offsetY, float progress) {
        v.animate().translationX(offsetX * progress).setDuration(0).start();
        v.animate().translationY(offsetY * progress).setDuration(0).start();
    }

    private void updateAnimationOffsets() {
        fromOffsetX = (fromLanguageSpinner.getX() + (float)fromLanguageSpinner.getWidth()/4 - (from_lang_short.getX() - from_lang_short.getTranslationX()));
        fromOffsetY = (fromLanguageSpinner.getY() + (float)fromLanguageSpinner.getHeight()/4 - (from_lang_short.getY() - from_lang_short.getTranslationY()));

        toOffsetX = (toLanguageSpinner.getX() + (float)toLanguageSpinner.getWidth()/4 - (to_lang_short.getX() - to_lang_short.getTranslationX()));
        toOffsetY = (toLanguageSpinner.getY() + (float)toLanguageSpinner.getHeight()/4 - (to_lang_short.getY() - to_lang_short.getTranslationY()));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        switchOffsetX = ((float)width/2 - (float)switch_button.getWidth()/2 - (switch_button.getX() - switch_button.getTranslationX()));
        switchOffsetY = (fromLanguageSpinner.getY() + (float)fromLanguageSpinner.getHeight()/5 - (switch_button.getY() - switch_button.getTranslationY()));
    }

    /*
     * Returns: the last 2 letters of the lang code which contains the 2 letter abbreviation of each language
     */
    private String parseLangCode(String langCode) {
        String firstLetters = langCode.substring(0, 3);
        if (firstLetters.contains("-"))
            return firstLetters.substring(0, 2).toUpperCase();
        else
            return langCode.substring(langCode.length() - 2).toUpperCase();
    }

    private void switchLanguages() {
        Grammarlex gl = Grammarlex.get();

        gl.switchLanguages();
        updateVM = false;
        fromLanguageSpinner.setSelection(gl.getLanguageIndex(gl.getSourceLanguage()));
        toLanguageSpinner.setSelection(gl.getLanguageIndex(gl.getTargetLanguage()));
        updateVM = true;

        if (TranslatorInputMethodService.getInstance() != null) {
            TranslatorInputMethodService.getInstance().handleSwitchLanguages();
        }
    }
}

