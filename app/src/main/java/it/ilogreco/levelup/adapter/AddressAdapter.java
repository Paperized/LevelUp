package it.ilogreco.levelup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import it.ilogreco.levelup.entity.utils.AddressAdapterData;

/**
 * Adapter used with the AutocompleteTextView to display street addresses, no filter is applied in this list (so everything is shown)
 */
public class AddressAdapter extends ArrayAdapter<AddressAdapterData> {
    final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            return null;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

        }
    };

    public AddressAdapter(@NonNull Context context, @NonNull List<AddressAdapterData> objects) {
        super(context, android.R.layout.simple_dropdown_item_1line, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View currentItemView = convertView;

        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        }

        TextView editText = currentItemView.findViewById(android.R.id.text1);
        editText.setText(getItem(position).getAddressName());

        return currentItemView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }
}
