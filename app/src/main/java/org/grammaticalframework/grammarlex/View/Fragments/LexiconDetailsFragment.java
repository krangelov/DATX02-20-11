package org.grammaticalframework.grammarlex.View.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;

import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import org.grammaticalframework.grammarlex.Grammarlex;
import org.grammaticalframework.grammarlex.R;
import org.grammaticalframework.grammarlex.TTS;
import org.grammaticalframework.grammarlex.ViewModel.LexiconViewModel;
import org.grammaticalframework.grammarlex.ViewModel.LexiconWord;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.grammaticalframework.grammarlex.ViewModel.SenseSchema;
import com.larvalabs.svgandroid.*;


public class LexiconDetailsFragment extends BaseFragment {

    private ImageView backButton;
    private String lemma;
    private NavController navController;
    private LexiconViewModel model;
    private WebView webView;
    private TextView wordView;
    private TextView explanationTextView;
    private TextView synonymTextView;
    private TextView explanationHeader;
    private TextView synonymsHeader;
    private TextView inflectionsHeader;
    private TableRow synonymsRow;
    private LinearLayout layout;
    private static final String TAG = LexiconDetailsFragment.class.getSimpleName();

    private TTS mTts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTts        = new TTS(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_lexicon_details, container, false);

        wordView = fragmentView.findViewById(R.id.translated_word);
        explanationTextView = fragmentView.findViewById(R.id.explanationTextView);
        synonymTextView = fragmentView.findViewById(R.id.synonymTextView);
        webView = (WebView) fragmentView.findViewById(R.id.web_view);
        explanationHeader = fragmentView.findViewById(R.id.explanationHeader);
        synonymsHeader = fragmentView.findViewById(R.id.synonymsHeader);
        inflectionsHeader = fragmentView.findViewById(R.id.inflectionsHeader);
        synonymsRow = fragmentView.findViewById(R.id.synonymsRow);
        layout = fragmentView.findViewById(R.id.mainView);

        navController = Navigation.findNavController(getActivity().findViewById(R.id.nav_host_fragment));
        backButton = fragmentView.findViewById(R.id.lexicon_details_back);

        backButton.setOnClickListener((v) -> {
            navController.popBackStack();
        });

        ImageView button = (ImageView) fragmentView.findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTts.speak(Grammarlex.get().getTargetLanguage().getLangCode(), (String) wordView.getText());
            }
        });

        model = new ViewModelProvider(getActivity()).get(LexiconViewModel.class);
        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide synonyms elements if no synonyms are found
        synonymTextView.setVisibility(View.GONE);
        synonymsHeader.setVisibility(View.GONE);
        synonymsRow.setVisibility(View.GONE);

        if(getArguments() != null){
            LexiconDetailsFragmentArgs args = LexiconDetailsFragmentArgs.fromBundle(getArguments());
            LexiconWord word = args.getMessage();
            lemma = word.getLemma();
            wordView.setText(word.getWord());

            // show The Image in a ImageView
            new DownloadImagesTask().execute(word.getImages());

            explanationTextView.setText(word.toDescription());

            if(word.getSynonymWords().size() != 0){
                synonymTextView.setText(String.join(", ", word.getSynonymWords()));
                synonymTextView.setVisibility(View.VISIBLE);
                synonymsHeader.setVisibility(View.VISIBLE);
                synonymsRow.setVisibility(View.VISIBLE);
            }

            // Set colors for each header in this view
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                explanationHeader.setForegroundTintList(ContextCompat.getColorStateList(getContext(), R.color.explanation_colorstate));
                synonymsHeader.setForegroundTintList(ContextCompat.getColorStateList(getContext(), R.color.synonyms_colorstate));
                inflectionsHeader.setForegroundTintList(ContextCompat.getColorStateList(getContext(), R.color.inflections_colorstate));
            }

            // TODO: maybe perhaps not write html like this?
            String html = model.inflect(lemma);
            webView.loadData(html, "text/html", "UTF-8");

            // Web view background and padding has to be set programmatically
            webView.setBackgroundResource(R.drawable.content_background);
            webView.setBackgroundColor(Color.TRANSPARENT);

            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageFinished(WebView web, String url) {
                    web.loadUrl("javascript:(function(){ document.body.style.padding = '10px';})();");
                    webView.getSettings().setJavaScriptEnabled(false);
                }
            });
        }
    }

    private class DownloadImagesTask extends AsyncTask<SenseSchema.ImageInfo[], Void, Pair<Drawable,Uri>[]> {

        @RequiresApi(api = Build.VERSION_CODES.O)
        protected Pair<Drawable,Uri>[] doInBackground(SenseSchema.ImageInfo[]... args) {
            SenseSchema.ImageInfo[] imageInfos = args[0];
            Pair<Drawable,Uri>[] drawables = new Pair[imageInfos.length];
            for (int i = 0; i < imageInfos.length; i++) {
                SenseSchema.ImageInfo imageInfo = imageInfos[i];
                Uri uri = Uri.parse("https://en.wikipedia.org/wiki/"+imageInfo.page_url);

                try {
                    ArrayList<String> path = new ArrayList<String>();
                    path.add("https://upload.wikimedia.org/wikipedia");
                    for (String s : imageInfo.img_url.split("/"))
                        path.add(s);
                    String name = path.get(path.size()-1);
                    if (name.endsWith(".svg")) {
                        InputStream in = new java.net.URL(String.join("/",path)).openStream();
                        SVG svg = SVGParser.getSVGFromInputStream(in);
                        drawables[i] = new Pair<Drawable,Uri>(svg.createPictureDrawable(),uri);
                    } else {
                        path.add(2,"thumb");
                        path.add("300px-"+name);
                        InputStream in = new java.net.URL(String.join("/",path)).openStream();
                        drawables[i] = new Pair<Drawable,Uri>(new BitmapDrawable(BitmapFactory.decodeStream(in)),uri);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return drawables;
        }

        protected void onPostExecute(Pair<Drawable,Uri>[] drawables) {
            for (Pair<Drawable,Uri> pair : drawables) {
                if (pair == null)
                    continue;

                final Uri uri = pair.second;

                ImageView imageView = new ImageView(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 20, 0, 20);
                params.height = 300;
                params.gravity = Gravity.CENTER;
                imageView.setLayoutParams(params);
                imageView.setImageDrawable(pair.first);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(browserIntent);
                    }
                });
                layout.addView(imageView, 0);
            }
        }
    }
}
