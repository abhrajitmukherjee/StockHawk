package com.sam_chordas.android.stockhawk.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.github.mikephil.charting.charts.LineChart;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.RetrofitCall;


public class FragmentLineGraph extends Fragment {
    LineChart mChart;
    String symbol;

    public FragmentLineGraph() {
        // Required empty public constructor
    }

    public static FragmentLineGraph newInstance(String param1, String param2) {
        FragmentLineGraph fragment = new FragmentLineGraph();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
        Intent intent = getActivity().getIntent();
        symbol = intent.getStringExtra("symbol");
        Log.v("Symbol Received",symbol);



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
        rc.getHistory("20",symbol);
        rc.getStats("20",symbol);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_line_graph, container, false);

        Log.v("createview","called");
        return rootView;
    }



}
