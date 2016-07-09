package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sam_chordas.android.stockhawk.R;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        FragmentLineGraph lineFrag = new FragmentLineGraph();
        if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                    .add(R.id.stock_detail_container, lineFrag)
                    .commit();
        }
    }
}
