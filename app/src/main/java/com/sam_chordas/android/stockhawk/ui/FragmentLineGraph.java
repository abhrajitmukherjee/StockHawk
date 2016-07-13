package com.sam_chordas.android.stockhawk.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.RetrofitCall;


public class FragmentLineGraph extends Fragment {
    LineChart mChart;
    String symbol,percent;

    public FragmentLineGraph() {
        // Required empty public constructor
    }

    public static FragmentLineGraph newInstance(String param1, String param2) {
        FragmentLineGraph fragment = new FragmentLineGraph();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        symbol = intent.getStringExtra("symbol");
        percent= intent.getStringExtra("percent");

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();

        setData(20,(float)5.0);

    }

    public void setData(int count, float range) {
        RetrofitCall rc=new RetrofitCall(getActivity(),this);
        rc.getHistory(percent,symbol);
        rc.getStats("20",symbol);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_line_graph, container, false);

       return rootView;
    }



}
