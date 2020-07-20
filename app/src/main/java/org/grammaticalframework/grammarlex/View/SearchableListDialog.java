package org.grammaticalframework.grammarlex.View;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SpinnerAdapter;

import org.grammaticalframework.grammarlex.R;

import java.io.Serializable;
import java.util.Comparator;

public class SearchableListDialog extends DialogFragment implements
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private SpinnerAdapter _adapter;

    private ListView _listViewItems;

    private SearchableItem _searchableItem;

    private SearchView _searchView;

    private String _strPositiveButtonText;

    private DialogInterface.OnClickListener _onClickListener;

    public SearchableListDialog() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_HIDDEN);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Getting the layout inflater to inflate the view in an alert dialog.
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        // Crash on orientation change #7
        // Change Start
        // Description: As the instance was re initializing to null on rotating the device,
        // getting the instance from the saved instance
        if (null != savedInstanceState) {
            _searchableItem = (SearchableItem) savedInstanceState.getSerializable("item");
        }
        // Change End

        View rootView = inflater.inflate(R.layout.searchable_list_dialog, null);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context
                .SEARCH_SERVICE);

        _searchView = (SearchView) rootView.findViewById(R.id.search);
        _searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        _searchView.setIconifiedByDefault(false);
        _searchView.setOnQueryTextListener(this);
        _searchView.setOnCloseListener(this);
        _searchView.requestFocus();
        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context
                .INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(_searchView.getWindowToken(), 0);

        ArrayAdapter<Pair<Integer,Object>> proxyAdapter = new ArrayAdapter<Pair<Integer,Object>>(getActivity(), android.R.layout.simple_list_item_1) {
            public View getView(int position, View convertView, ViewGroup parent) {
                if (_adapter == null)
                    return null;
                position = getItem(position).first;
                return _adapter.getDropDownView(position, convertView, parent);
            }
        };

        _listViewItems = (ListView) rootView.findViewById(R.id.listItems);
        _listViewItems.setAdapter(proxyAdapter);
        _listViewItems.setTextFilterEnabled(true);
        _listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position = ((Pair<Integer,Object>) parent.getAdapter().getItem(position)).first;
                final Object item = _adapter.getItem(position);
                _searchableItem.onSearchableItemClicked(item, position);
                getDialog().dismiss();
            }
        });

        resetProxyAdapter();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setView(rootView);

        String strPositiveButton = _strPositiveButtonText == null ? "CLOSE" : _strPositiveButtonText;
        alertDialog.setPositiveButton(strPositiveButton, _onClickListener);

        final AlertDialog dialog = alertDialog.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_HIDDEN);
        return dialog;
    }

    // Crash on orientation change #7
    // Change Start
    // Description: Saving the instance of searchable item instance.
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("item", _searchableItem);
        super.onSaveInstanceState(outState);
    }
    // Change End

    public void setPositiveButton(String strPositiveButtonText) {
        _strPositiveButtonText = strPositiveButtonText;
    }

    public void setPositiveButton(String strPositiveButtonText, DialogInterface.OnClickListener onClickListener) {
        _strPositiveButtonText = strPositiveButtonText;
        _onClickListener = onClickListener;
    }

    public void setOnSearchableItemClickListener(SearchableItem searchableItem) {
        this._searchableItem = searchableItem;
    }

    public void setAdapter(SpinnerAdapter adapter) {
        this._adapter = adapter;
        resetProxyAdapter();
    }

    public SpinnerAdapter getAdapter() {
        return this._adapter;
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        dismiss();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        _searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (TextUtils.isEmpty(s)) {
            ((Filterable) _listViewItems.getAdapter()).getFilter().filter(null);
        } else {
            ((Filterable) _listViewItems.getAdapter()).getFilter().filter(s);
        }
        return true;
    }

    public interface SearchableItem<T> extends Serializable {
        void onSearchableItemClicked(T item, int position);
    }

    private void resetProxyAdapter() {
        if (_adapter == null || _listViewItems == null)
            return;

        ArrayAdapter<Pair<Integer,Object>> proxyAdapter = (ArrayAdapter<Pair<Integer,Object>>) _listViewItems.getAdapter();
        proxyAdapter.clear();
        for (int i = 0; i < _adapter.getCount(); i++) {
            Object item = _adapter.getItem(i);
            if (item.toString() != null)
                proxyAdapter.add(new Pair<Integer,Object>(i,item));
        }

        proxyAdapter.sort(new Comparator<Pair<Integer, Object>>() {
            @Override
            public int compare(Pair<Integer,Object> p1, Pair<Integer,Object> p2) {
                return p1.second.toString().compareToIgnoreCase(p2.second.toString());
            }
        });
    }
}
