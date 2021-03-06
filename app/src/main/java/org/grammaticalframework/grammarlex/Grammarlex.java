package org.grammaticalframework.grammarlex;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.grammaticalframework.grammarlex.R;
import org.grammaticalframework.pgf.Concr;
import org.grammaticalframework.pgf.PGF;
import org.daison.Database;


public class Grammarlex extends Application {
    private static String TAG = "Grammarlex";

    private GrammarLoader grammarLoader;
    private ConcrLoader sourceLoader;
    private ConcrLoader targetLoader;
    private ConcrLoader otherLoader;
    private Semaphore loaderAccess;

    private PGF pgf;

    private static final String SOURCE_LANG_KEY = "source_lang";
    private static final String TARGET_LANG_KEY = "target_lang";

    private SharedPreferences mSharedPref;

    private static Grammarlex instance;

    private Language[] languages = {
        new Language("bg-BG", "Bulgarian", "ParseBul", R.xml.cyrillic),
        new Language("ca-ES", "Catalan",   "ParseCat", R.xml.qwerty),
        new Language("cmn-Hans-CN", "Chinese", "ParseChi", R.xml.qwerty),
        new Language("nl-NL", "Dutch", "ParseDut", R.xml.qwerty),
        new Language("en-US", "English", "ParseEng", R.xml.qwerty),
        new Language("et-EE", "Estonian","ParseEst", R.xml.nordic),
        new Language("fi-FI", "Finnish", "ParseFin", R.xml.nordic),
        new Language("it-IT", "Italian", "ParseIta", R.xml.qwerty),
        new Language("es-ES", "Spanish", "ParseSpa", R.xml.qwerty),
        new Language("sv-SE", "Swedish", "ParseSwe", R.xml.nordic),
        new Language("pt-PT", "Portuguese", "ParsePor", R.xml.qwerty),
        new Language("th-TH", "Thai",    "ParseTha", R.xml.qwerty),
    };

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Main thread with ID: " + Thread.currentThread().getName() + " is now running.");

        mSharedPref = getApplicationContext().getSharedPreferences(
                getApplicationContext().getString(R.string.global_preferences_key), Context.MODE_PRIVATE);

        loaderAccess = new Semaphore(1);

        grammarLoader = new GrammarLoader();
        grammarLoader.start();

        Language prefSourceLang = getPrefLang(SOURCE_LANG_KEY, 4);
        Language prefTargetLang = getPrefLang(TARGET_LANG_KEY, 9);

        sourceLoader = new ConcrLoader(prefSourceLang);
        sourceLoader.start();

        if (prefSourceLang == prefTargetLang) {
            targetLoader = sourceLoader;
        } else {
            targetLoader = new ConcrLoader(prefTargetLang);
            targetLoader.start();
        }

        otherLoader = null;

        instance = this;
    }

    public PGF getPgf() {
        return pgf;
    }

    public List<Language> getAvailableLanguages() {
        return Arrays.asList(languages);
    }

    public Language getSourceLanguage() {
        try {
            sourceLoader.join();
            return sourceLoader.getLanguage();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public Concr getSourceConcr() {
        try{
            sourceLoader.join();
            return sourceLoader.concr;
        } catch (InterruptedException e) {
            return  null;
        }
    }

    public Language getTargetLanguage() {
        try {
            targetLoader.join();
            return targetLoader.getLanguage();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public Concr getTargetConcr() {
        try{
            targetLoader.join();
            return targetLoader.concr;
        } catch (InterruptedException e) {
            return  null;
        }
    }

    public void setSourceLanguage(Language language) {
        setPrefLang(SOURCE_LANG_KEY, language);

        if (sourceLoader.getLanguage() == language)
            return;
        if (targetLoader.getLanguage() == language) {
            cacheOrUnloadLanguage(sourceLoader);
            sourceLoader = targetLoader;
            return;
        }
        if (otherLoader != null &&
                otherLoader.getLanguage() == language) {
            ConcrLoader tmp = sourceLoader;
            sourceLoader = otherLoader;
            otherLoader  = tmp;
            return;
        }

        try {
            sourceLoader.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "Loading interrupted", e);
        }


        if (sourceLoader.getLanguage() != targetLoader.getLanguage()) {
            cacheOrUnloadLanguage(sourceLoader);
        }

        sourceLoader = new ConcrLoader(language);
        sourceLoader.start();
    }

    public int getLanguageIndex(Language language) {
        for (int i = 0; i < languages.length; i++) {
            if (language.equals(languages[i])) {
                return i;
            }
        }

        return 0;
    }

    public PGF getGrammar() {
        return grammarLoader.getGrammar();
    }

    public void setTargetLanguage(Language language) {
        setPrefLang(TARGET_LANG_KEY, language);

        if (targetLoader.getLanguage() == language)
            return;
        if (sourceLoader.getLanguage() == language) {
            cacheOrUnloadLanguage(targetLoader);
            targetLoader = sourceLoader;
            return;
        }
        if (otherLoader != null &&
                otherLoader.getLanguage() == language) {
            ConcrLoader tmp = targetLoader;
            targetLoader = otherLoader;
            otherLoader  = tmp;
            return;
        }

        try {
            targetLoader.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "Loading interrupted", e);
        }

        if (sourceLoader.getLanguage() != targetLoader.getLanguage()) {
            cacheOrUnloadLanguage(targetLoader);
        }

        targetLoader = new ConcrLoader(language);
        targetLoader.start();
    }

    private void cacheOrUnloadLanguage(ConcrLoader loader) {
        if (otherLoader != null) {
            otherLoader.getConcr().unload();
            Log.d(TAG, otherLoader.getLanguage().getConcrete() + ".pgf_c unloaded");
        }
        otherLoader = loader;
    }

    public void switchLanguages() {
        ConcrLoader tmp = sourceLoader;
        sourceLoader = targetLoader;
        targetLoader = tmp;
    }

    private Language getPrefLang(String key, int def) {
        int index = mSharedPref.getInt(key, def);
        if (index < 0 || index >= languages.length)
            index = def;
        return languages[index];
    }

    private void setPrefLang(String key, Language def) {
        for (int index = 0; index < languages.length; index++) {
            if (def == languages[index]) {
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putInt(key, index);
                editor.commit();
                break;
            }
        }
    }

    private class GrammarLoader extends Thread {
        public GrammarLoader() {
            pgf = null;
        }

        public PGF getGrammar() {
            return pgf;
        }

        public void run() {
            InputStream in = null;
            Log.d(TAG, "Grammar thread with ID: " + getName() + " is now running.");
            try {
                String grammarName = "Parse.pgf";
                in = getApplicationContext().getAssets().open(grammarName);
                Log.d(TAG, "Trying to open " + grammarName);
                long t1 = System.currentTimeMillis();
                pgf = PGF.readPGF(in);
                long t2 = System.currentTimeMillis();
                Log.d(TAG, grammarName + " loaded ("+(t2-t1)+" ms)");
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found", e);
            } catch (IOException e) {
                Log.e(TAG, "Error loading grammar", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing the stream", e);
                    }
                }
            }
        }
    }

    private class ConcrLoader extends Thread {
        private Language language;
        private Concr concr;

        public ConcrLoader(Language language) {
            this.language = language;
            this.concr   = null;
        }

        public Language getLanguage() {
            return language;
        }

        public Concr getConcr() {
            return concr;
        }

        public void run() {
            Log.d(TAG, "Concr thread with ID: " + getName() + " is now running.");
            try {
                grammarLoader.join();
            } catch (InterruptedException e) {
                Log.d(TAG, "interrupted", e);
            }

            InputStream in = null;

            try {
                String name = language.getConcrete()+".pgf_c";
                in = getApplicationContext().getAssets().open(name);
                Log.d(TAG, "Trying to load " + name);
                long t1 = System.currentTimeMillis();
                concr = grammarLoader.getGrammar().getLanguages().get(language.getConcrete());
                long t3 = System.currentTimeMillis();
                Log.d(TAG, "Fetched from grammar: (" + (t3-t1) + " ms)");
                concr.load(in);
                long t2 = System.currentTimeMillis();
                Log.d(TAG, name + " loaded ("+(t2-t1)+" ms)");
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found", e);
            } catch (IOException e) {
                Log.e(TAG, "Error loading concrete", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing the stream", e);
                    }
                }
            }
        }
    }

    private static void deleteDatabases (Context context){
        String[] databaseList = context.databaseList();
        if(databaseList != null){
            for(String database : databaseList){
                if (!context.deleteDatabase(database)){
                    Log.e(TAG, "Not able to delete database " + database + ".");
                }
            }
        }
    }

    public static Grammarlex get() {
        return instance;
    }

    private static Database db = null;
    private static AssetFileDescriptor dbFD = null;

    public Database getDatabase() {
        if (db == null) {
            db = new Database(getAssets(), "semantics.db");
        }
        return db;
    }
}
