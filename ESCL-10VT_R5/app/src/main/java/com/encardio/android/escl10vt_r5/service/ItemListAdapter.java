package com.encardio.android.escl10vt_r5.service;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Adapter that allows us to render a list of items.
 */
public class ItemListAdapter extends ArrayAdapter<Item> {
    /**
     * The LayoutInflater li.
     */
    private final LayoutInflater li;

    /**
     * Constructor from a list of items.
     *
     * @param context the context
     * @param items   the items
     */
    public ItemListAdapter(Context context, List<Item> items) {
        super(context, 0, items);
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // This is how you would determine if this particular item is checked
        // when the view gets created
        // --
        // final ListView lv = (ListView) parent;
        // final boolean isChecked = lv.isItemChecked(position);
        // The item we want to get the view for
        // --
        final Item item = getItem(position);
        // Re-use the view if possible
        // --
        View v = convertView;
//        if (v == null) {
//            v = li.inflate(R.layout.item, null);
//        }
        // Set some view properties (We should use the view holder pattern in
        // order to avoid all the findViewById and thus improve performance)
        // --
        // final TextView idView = (TextView) v.findViewById(R.id.itemId);
        // if (idView != null) {
        // idView.setText("#" + item.getId());
        // }
//        final TextView captionView = (TextView) v.findViewById(R.id.itemCaption);
//        if (captionView != null) {
//            captionView.setText(item.getFile());
//        }
        return v;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.ArrayAdapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.BaseAdapter#hasStableIds()
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }
}
