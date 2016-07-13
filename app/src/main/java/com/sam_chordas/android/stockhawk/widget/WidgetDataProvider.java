package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.DetailActivity;

import java.util.ArrayList;

/**
 * WidgetDataProvider acts as the adapter for the collection view widget,
 * providing RemoteViews to the widget in the getViewAt method.
 */
public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "WidgetDataProvider";

    ArrayList<ArrayList<String>> mCollection = new ArrayList<>();
    Context mContext = null;

    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mCollection.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
//        RemoteViews view = new RemoteViews(mContext.getPackageName(),
                //android.R.layout.simple_list_item_1);
//        view.setTextViewText(android.R.id.text1, mCollection.get(position));

        RemoteViews view = new RemoteViews(mContext.getPackageName(),
                R.layout.list_item_quote);
        view.setTextViewText(R.id.stock_symbol,mCollection.get(position).get(0));
        view.setTextColor(R.id.stock_symbol,mContext.getResources().getColor(android.R.color.black));
        view.setTextViewText(R.id.bid_price,mCollection.get(position).get(1));
        view.setTextColor(R.id.bid_price,mContext.getResources().getColor(android.R.color.black));
        view.setTextViewText(R.id.change,mCollection.get(position).get(2));
        if (mCollection.get(position).get(2).charAt(0)=='-'){
            view.setTextColor(R.id.change,mContext.getResources().getColor(R.color.graph_red));
        }else{
            view.setTextColor(R.id.change,mContext.getResources().getColor(R.color.graph_green));
        }

        Intent intent = new Intent(mContext, DetailActivity.class);
        intent.putExtra("symbol",mCollection.get(position).get(0));
        intent.putExtra("percent",mCollection.get(position).get(2));

        view.setOnClickFillInIntent(R.id.list_item_quote, intent);
        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private void initData() {
        mCollection.clear();

        Cursor c = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,null,
                QuoteColumns.ISCURRENT+"='1'",null,null);
        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            ArrayList<String> lst=new ArrayList<>();
            lst.add(c.getString(c.getColumnIndex(QuoteColumns.SYMBOL)));
            lst.add(c.getString(c.getColumnIndex(QuoteColumns.BIDPRICE)));
            lst.add(c.getString(c.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
            lst.add(c.getString(c.getColumnIndex(QuoteColumns.ISUP)));
           mCollection.add(lst);
            c.moveToNext();
        }
        c.close();
    }

}