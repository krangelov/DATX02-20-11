package org.grammaticalframework.grammarlex.View;
import org.grammaticalframework.grammarlex.R;
import org.grammaticalframework.grammarlex.ViewModel.MainViewModel;

import android.content.Intent;
import android.os.Bundle;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity {
    private MainViewModel viewModel;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.viewModel = new MainViewModel();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        CharSequence text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        if (text != null) {
            Bundle bundle = new Bundle();
            bundle.putCharSequence(Intent.EXTRA_PROCESS_TEXT, text);
            navController.popBackStack();
            navController.navigate(R.id.lexiconFragment, bundle);
        }
    }

    public MainViewModel getViewModel() {
        return viewModel;
    }
}
