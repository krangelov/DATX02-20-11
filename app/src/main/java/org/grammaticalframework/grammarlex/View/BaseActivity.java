package org.grammaticalframework.grammarlex.View;

import org.grammaticalframework.grammarlex.ViewModel.BaseViewModel;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    abstract public BaseViewModel getViewModel();
}
