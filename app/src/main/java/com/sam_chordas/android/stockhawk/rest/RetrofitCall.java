package com.sam_chordas.android.stockhawk.rest;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sam_chordas.android.stockhawk.BuildConfig;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.FragmentLineGraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class RetrofitCall {

    FragmentActivity mParentActivity;
    FragmentLineGraph mFragment;
    LineChart mChart;

    public RetrofitCall(FragmentActivity inpContext, FragmentLineGraph fg) {
        mParentActivity = inpContext;
        mFragment = fg;
    }

    public void getHistory(String id, String type) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.quandl.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        StockService service = retrofit.create(StockService.class);
        Call<StockJson> repos = service.listHistory(type+".json","4",
                "2015-07-08", "2016-07-08", "daily", "diff", "asc", BuildConfig.STOCK_API_KEY);

        repos.enqueue(new Callback<StockJson>() {
            @Override
            public void onResponse(Call<StockJson> call, Response<StockJson> response) {
                Log.d("Test", "onResponse - Status : " + response.code() + " " + call.request().url().toString());


                if (response.isSuccessful()) {
                    StockJson rv = response.body();
                    Log.d("test", rv.dataSet.id.toString());
                    if (rv.dataSet.data.size() > 0) {
                        String stockName=rv.dataSet.name;
                        mParentActivity.setTitle(stockName.substring(0,stockName.indexOf("Prices")));
                        Log.v("set data", "called" + rv.dataSet.id.toString());

                        mChart = (LineChart) mParentActivity.findViewById(R.id.chart);
                        ArrayList<Entry> values = new ArrayList<Entry>();
                        String price="";
                        for (int i = 0; i < rv.dataSet.data.size(); i++) {


                            price=rv.dataSet.data.get(i).get(1).toString();
                            Float f = Float.parseFloat(price);
                            values.add(new Entry(i, f));
                        }

                        LineDataSet set1;

                        if (mChart.getData() != null &&
                                mChart.getData().getDataSetCount() > 0) {
                            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
                            set1.setValues(values);
                            mChart.getData().notifyDataChanged();
                            mChart.notifyDataSetChanged();
                        } else {
                            set1 = new LineDataSet(values, "DataSet 1");
                            set1.enableDashedHighlightLine(10f, 5f, 0f);
                            set1.setColor(Color.DKGRAY);
                            set1.setCircleColor(Color.BLACK);
                            set1.setLineWidth(3f);
                            set1.setDrawCircleHole(false);
                            set1.setDrawCircles(false);
                            set1.setDrawValues(false);

                            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                            set1.setCubicIntensity(0.1f);

                            set1.setDrawFilled(false);
                            mChart.setDrawBorders(false);
                            mChart.getAxisRight().disableGridDashedLine();
                            mChart.getAxisLeft().setEnabled(false);
                            mChart.getAxisRight().setEnabled(false);
                            mChart.getXAxis().setEnabled(false);
                            mChart.setDoubleTapToZoomEnabled(false);
                            mChart.setPinchZoom(false);
                            mChart.setDescription("");


                            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                            dataSets.add(set1); // add the datasets

                            // create a data object with the datasets
                            LineData data = new LineData(dataSets);

                            // set data
                            mChart.setData(data);
                            mChart.invalidate();
                            mChart.notifyDataSetChanged();


                        }

                        TextView tv=(TextView) mParentActivity.findViewById(R.id.stockPrice);
                        tv.setText("$"+price);

                    } else {

                        return;

                    }


                    //
                    // tasks available
                } else {

                    try {
                        Log.d("Test", "Error - Status : " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e("Test", e.toString());
                    }

                }
            }


            @Override
            public void onFailure(Call<StockJson> call, Throwable t) {
                Log.d("Error----", t.getMessage());
            }
        });


    }

    public interface StockService {
        @GET("api/v3/datasets/WIKI/{jsonType}")
        Call<StockJson> listHistory(@Path("jsonType") String jsonType,
                                    @Query("column_index") String colIndex,
                                    @Query("start_date") String fromDate,
                                    @Query("end_date") String endDate,
                                    @Query("collapse") String frequency,
                                    @Query("transformation") String trans,
                                    @Query("order") String order,
                                    @Query("api_key") String api_key);

    }

    public class StockJson {

        @SerializedName("dataset")
        @Expose
        public Dataset dataSet;
    }

    public class Dataset {

        @SerializedName("id")
        @Expose
        public Integer id;
        @SerializedName("dataset_code")
        @Expose
        public String datasetCode;
        @SerializedName("database_code")
        @Expose
        public String databaseCode;
        @SerializedName("name")
        @Expose
        public String name;
        @SerializedName("description")
        @Expose
        public String description;
        @SerializedName("refreshed_at")
        @Expose
        public String refreshedAt;
        @SerializedName("newest_available_date")
        @Expose
        public String newestAvailableDate;
        @SerializedName("oldest_available_date")
        @Expose
        public String oldestAvailableDate;
        @SerializedName("column_names")
        @Expose
        public List<String> columnNames = new ArrayList<String>();
        @SerializedName("frequency")
        @Expose
        public String frequency;
        @SerializedName("type")
        @Expose
        public String type;
        @SerializedName("premium")
        @Expose
        public Boolean premium;
        @SerializedName("limit")
        @Expose
        public Object limit;
        @SerializedName("transform")
        @Expose
        public Object transform;
        @SerializedName("column_index")
        @Expose
        public Integer columnIndex;
        @SerializedName("start_date")
        @Expose
        public String startDate;
        @SerializedName("end_date")
        @Expose
        public String endDate;
        @SerializedName("data")
        @Expose
        public List<List<String>> data = new ArrayList<List<String>>();
        @SerializedName("collapse")
        @Expose
        public String collapse;
        @SerializedName("order")
        @Expose
        public String order;
        @SerializedName("database_id")
        @Expose
        public Integer databaseId;

    }


}
