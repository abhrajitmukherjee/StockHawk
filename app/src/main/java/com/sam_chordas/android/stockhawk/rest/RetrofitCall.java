package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
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

public class RetrofitCall {

    FragmentActivity mParentActivity;
    FragmentLineGraph mFragment;
    LineChart mChart;

    public RetrofitCall(FragmentActivity inpContext, FragmentLineGraph fg) {
        mParentActivity = inpContext;
        mFragment = fg;
    }


    public void getStats(String id, String type) {


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://query.yahooapis.com/")
                            .addConverterFactory(GsonConverterFactory.create())
                .build();


        StatsService service = retrofit.create(StatsService.class);
        Call<StockStats> repos = service.listStats("select * from yahoo.finance.quotes where symbol in (\""+type+"\")");

        repos.enqueue(new Callback<StockStats>() {
            @Override
            public void onResponse(Call<StockStats> call, Response<StockStats> response) {
          //      Log.d("Test", "onResponse - Status : " + response.code() + " " + call.request().url().toString());


                if (response.isSuccessful()) {
                    StockStats rv = response.body();
                    Quote qt=rv.query.results.quote;
                    TextView tvOpen=(TextView) mParentActivity.findViewById(R.id.text_open);
                    TextView tvVolume=(TextView) mParentActivity.findViewById(R.id.text_volume);
                    TextView tvTodayHigh=(TextView) mParentActivity.findViewById(R.id.text_high);
                    TextView tvAvgVol=(TextView) mParentActivity.findViewById(R.id.text_avg_volume);
                    TextView tvTodayLow=(TextView) mParentActivity.findViewById(R.id.text_low);
                    TextView tvMarket=(TextView) mParentActivity.findViewById(R.id.text_mkt_cap);
                    TextView tvPe=(TextView) mParentActivity.findViewById(R.id.text_pe_ratio);
                    TextView tvFiftyHigh=(TextView) mParentActivity.findViewById(R.id.text_52High);
                    TextView tvFiftyLow=(TextView) mParentActivity.findViewById(R.id.text_52Low);
                    TextView tvDiv=(TextView) mParentActivity.findViewById(R.id.text_div);
                    if (tvOpen!=null){


                        tvOpen.setText("$"+qt.open);
                        tvVolume.setText(qt.volume);
                        tvTodayHigh.setText("$"+qt.daysHigh);
                        tvAvgVol.setText(qt.averageDailyVolume);
                        tvTodayLow.setText("$"+qt.daysLow);
                        tvMarket.setText("$"+qt.marketCapitalization);
                        tvFiftyHigh.setText("$"+qt.yearHigh);
                        if (qt.pERatio==null){
                            tvPe.setText(mParentActivity.getString(R.string.text_NA));

                        }else{
                            tvPe.setText(qt.pERatio);
                        }


                        tvFiftyLow.setText("$"+qt.yearLow);
                        if (qt.dividendYield == null){
                            tvDiv.setText("0");
                        }else
                        {
                            tvDiv.setText(qt.dividendYield);
                        }
                    }


                }}

            @Override
            public void onFailure(Call<StockStats> call, Throwable t) {
                Log.d("Error:", t.getMessage());
            }
        });}










    public void getHistory(final String percent, String type) {


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://chartapi.finance.yahoo.com/")
    //            .addConverterFactory(GsonConverterFactory.create(gson))
                .build();


        StockService service = retrofit.create(StockService.class);
        Call<okhttp3.ResponseBody> repos = service.listHistory(type,"chartdata;type=quote;range=5y");

        repos.enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
        //        Log.d("Test", "onResponse - Status : " + response.code() + " " + call.request().url().toString());


                if (response.isSuccessful()) {
                    okhttp3.ResponseBody rv = response.body();
                    String rawJson="";
                    try{
                        String bodyText=rv.string();
                        rawJson=bodyText.substring(bodyText.indexOf("{"),bodyText.lastIndexOf("}")+1);

                    }catch (IOException e)
                    {

                    }
                    StockJson dataSet = new Gson().fromJson(rawJson, StockJson.class);
                    int size=dataSet.series.size();

                    if ( size> 0) {
                        String stockName=dataSet.meta.companyName;
                        mParentActivity.setTitle(stockName);
                        mChart = (LineChart) mParentActivity.findViewById(R.id.chart);
                        ArrayList<Entry> values = new ArrayList<Entry>();
                        Float price=0f;
                        int offset=261-size;
                        float closePrice=dataSet.series.get(0).close;
                        for (int i = 0; i < 261; i++) {
                            if (i<=offset){
                                price=closePrice;
                            }else{
                                price = dataSet.series.get(i-offset).close;


                            }
                            values.add(new Entry(i, price));

                        }

                        LineDataSet set1;
                        TextView today=(TextView) mParentActivity.findViewById(R.id.todaysChange);

                        if (mChart.getData() != null &&
                                mChart.getData().getDataSetCount() > 0) {
                            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
                            set1.setValues(values);
                            mChart.getData().notifyDataChanged();
                            mChart.notifyDataSetChanged();
                        } else {
                            set1 = new LineDataSet(values, "");
                            set1.enableDashedHighlightLine(10f, 5f, 0f);
                            if(percent.charAt(0)=='-'){
                                set1.setColor(mParentActivity.getResources().getColor(R.color.graph_red));
                                today.setTextColor(mParentActivity.getResources().getColor(R.color.graph_red));
                            }else{
                                set1.setColor(mParentActivity.getResources().getColor(R.color.graph_green));
                                today.setTextColor(mParentActivity.getResources().getColor(R.color.graph_green));
                            }

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


                            mChart.setDescription("5 Year History");


                            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                            dataSets.add(set1); // add the datasets

                            // create a data object with the datasets
                            LineData data = new LineData(dataSets);

                            // set data
                            mChart.setData(data);
                            mChart.invalidate();
                            mChart.notifyDataSetChanged();


                        }

                        TextView tv=(TextView) mParentActivity.findViewById(R.id.stockPrice);
                        tv.setText("$"+Float.toString(price));
                        today.setText(percent);
                        TextView label=(TextView) mParentActivity.findViewById(R.id.todayLabel);
                        label.setText(R.string.last_open);


                        ProgressBar spinner;
                        spinner = (ProgressBar)mParentActivity.findViewById(R.id.progressBar1);
                        spinner.setVisibility(View.GONE);

                    } else {
                        ProgressBar spinner;
                        spinner = (ProgressBar)mParentActivity.findViewById(R.id.progressBar1);
                        spinner.setVisibility(View.GONE);

                        return;

                    }


                    //
                    // tasks available
                } else {

                    try {
                        Log.d("Test", "Error - Status : " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e("Error:", e.toString());
                    }

                }
            }


            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                Log.d("Error:", t.getMessage());

                ProgressBar spinner;
                spinner = (ProgressBar)mParentActivity.findViewById(R.id.progressBar1);
                spinner.setVisibility(View.GONE);

                Context context = mParentActivity;
                CharSequence text = mParentActivity.getString(R.string.msg_data_fetch);
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });


    }

    public interface StockService {
        @GET("/instrument/1.0/{stock}/{path}/json")
        Call<okhttp3.ResponseBody> listHistory(@Path("stock") String stock,
                                               @Path("path") String path);

    }

    public interface StatsService {
        @GET("v1/public/yql?format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=")
        Call<StockStats> listStats(@retrofit2.http.Query("q") String q);

    }


    public class Close {

        @SerializedName("min")
        @Expose
        public Float min;
        @SerializedName("max")
        @Expose
        public Float max;

    }

    public class Date {

        @SerializedName("min")
        @Expose
        public Integer min;
        @SerializedName("max")
        @Expose
        public Integer max;

    }

    public class StockJson {

        @SerializedName("meta")
        @Expose
        public Meta meta;
        @SerializedName("Date")
        @Expose
        public Date date;
        @SerializedName("labels")
        @Expose
        public List<Integer> labels = new ArrayList<Integer>();
        @SerializedName("ranges")
        @Expose
        public Ranges ranges;
        @SerializedName("series")
        @Expose
        public List<Series> series = new ArrayList<Series>();

    }

    public class High {

        @SerializedName("min")
        @Expose
        public Float min;
        @SerializedName("max")
        @Expose
        public Float max;

    }

    public class Low {

        @SerializedName("min")
        @Expose
        public Float min;
        @SerializedName("max")
        @Expose
        public Float max;

    }

    public class Meta {

        @SerializedName("uri")
        @Expose
        public String uri;
        @SerializedName("ticker")
        @Expose
        public String ticker;
        @SerializedName("Company-Name")
        @Expose
        public String companyName;
        @SerializedName("Exchange-Name")
        @Expose
        public String exchangeName;
        @SerializedName("unit")
        @Expose
        public String unit;
        @SerializedName("timestamp")
        @Expose
        public String timestamp;
        @SerializedName("first-trade")
        @Expose
        public String firstTrade;
        @SerializedName("last-trade")
        @Expose
        public String lastTrade;
        @SerializedName("currency")
        @Expose
        public String currency;
        @SerializedName("previous_close_price")
        @Expose
        public Float previousClosePrice;

    }

    public class Open {

        @SerializedName("min")
        @Expose
        public Float min;
        @SerializedName("max")
        @Expose
        public Float max;

    }

    public class Ranges {

        @SerializedName("close")
        @Expose
        public Close close;
        @SerializedName("high")
        @Expose
        public High high;
        @SerializedName("low")
        @Expose
        public Low low;
        @SerializedName("open")
        @Expose
        public Open open;
        @SerializedName("volume")
        @Expose
        public Volume volume;

    }

    public class Series {

        @SerializedName("Date")
        @Expose
        public Integer date;
        @SerializedName("close")
        @Expose
        public Float close;
        @SerializedName("high")
        @Expose
        public Float high;
        @SerializedName("low")
        @Expose
        public Float low;
        @SerializedName("open")
        @Expose
        public Float open;
        @SerializedName("volume")
        @Expose
        public Integer volume;

    }

    public class Volume {

        @SerializedName("min")
        @Expose
        public Integer min;
        @SerializedName("max")
        @Expose
        public Integer max;

    }

    //Class for Stats begins


    public class Diagnostics {

        @SerializedName("url")
        @Expose
        public List<Url> url = new ArrayList<Url>();
        @SerializedName("publiclyCallable")
        @Expose
        public String publiclyCallable;
        @SerializedName("query")
        @Expose
        public Query_ query;
        @SerializedName("javascript")
        @Expose
        public Javascript javascript;
        @SerializedName("user-time")
        @Expose
        public String userTime;
        @SerializedName("service-time")
        @Expose
        public String serviceTime;
        @SerializedName("build-version")
        @Expose
        public String buildVersion;

    }

    public class StockStats {

        @SerializedName("query")
        @Expose
        public Query query;

    }

    public class Javascript {

        @SerializedName("execution-start-time")
        @Expose
        public String executionStartTime;
        @SerializedName("execution-stop-time")
        @Expose
        public String executionStopTime;
        @SerializedName("execution-time")
        @Expose
        public String executionTime;
        @SerializedName("instructions-used")
        @Expose
        public String instructionsUsed;
        @SerializedName("table-name")
        @Expose
        public String tableName;

    }

    public class Query {

        @SerializedName("count")
        @Expose
        public Integer count;
        @SerializedName("created")
        @Expose
        public String created;
        @SerializedName("lang")
        @Expose
        public String lang;
        @SerializedName("diagnostics")
        @Expose
        public Diagnostics diagnostics;
        @SerializedName("results")
        @Expose
        public Results results;

    }

    public class Query_ {

        @SerializedName("execution-start-time")
        @Expose
        public String executionStartTime;
        @SerializedName("execution-stop-time")
        @Expose
        public String executionStopTime;
        @SerializedName("execution-time")
        @Expose
        public String executionTime;
        @SerializedName("params")
        @Expose
        public String params;
        @SerializedName("content")
        @Expose
        public String content;

    }

    public class Quote {

        @SerializedName("symbol")
        @Expose
        public String symbol;
        @SerializedName("Ask")
        @Expose
        public String ask;
        @SerializedName("AverageDailyVolume")
        @Expose
        public String averageDailyVolume;
        @SerializedName("Bid")
        @Expose
        public String bid;
        @SerializedName("AskRealtime")
        @Expose
        public Object askRealtime;
        @SerializedName("BidRealtime")
        @Expose
        public Object bidRealtime;
        @SerializedName("BookValue")
        @Expose
        public String bookValue;
        @SerializedName("Change_PercentChange")
        @Expose
        public String changePercentChange;
        @SerializedName("Change")
        @Expose
        public String change;
        @SerializedName("Commission")
        @Expose
        public Object commission;
        @SerializedName("Currency")
        @Expose
        public String currency;
        @SerializedName("ChangeRealtime")
        @Expose
        public Object changeRealtime;
        @SerializedName("AfterHoursChangeRealtime")
        @Expose
        public Object afterHoursChangeRealtime;
        @SerializedName("DividendShare")
        @Expose
        public Object dividendShare;
        @SerializedName("LastTradeDate")
        @Expose
        public String lastTradeDate;
        @SerializedName("TradeDate")
        @Expose
        public Object tradeDate;
        @SerializedName("EarningsShare")
        @Expose
        public String earningsShare;
        @SerializedName("ErrorIndicationreturnedforsymbolchangedinvalid")
        @Expose
        public Object errorIndicationreturnedforsymbolchangedinvalid;
        @SerializedName("EPSEstimateCurrentYear")
        @Expose
        public String ePSEstimateCurrentYear;
        @SerializedName("EPSEstimateNextYear")
        @Expose
        public String ePSEstimateNextYear;
        @SerializedName("EPSEstimateNextQuarter")
        @Expose
        public String ePSEstimateNextQuarter;
        @SerializedName("DaysLow")
        @Expose
        public String daysLow;
        @SerializedName("DaysHigh")
        @Expose
        public String daysHigh;
        @SerializedName("YearLow")
        @Expose
        public String yearLow;
        @SerializedName("YearHigh")
        @Expose
        public String yearHigh;
        @SerializedName("HoldingsGainPercent")
        @Expose
        public Object holdingsGainPercent;
        @SerializedName("AnnualizedGain")
        @Expose
        public Object annualizedGain;
        @SerializedName("HoldingsGain")
        @Expose
        public Object holdingsGain;
        @SerializedName("HoldingsGainPercentRealtime")
        @Expose
        public Object holdingsGainPercentRealtime;
        @SerializedName("HoldingsGainRealtime")
        @Expose
        public Object holdingsGainRealtime;
        @SerializedName("MoreInfo")
        @Expose
        public Object moreInfo;
        @SerializedName("OrderBookRealtime")
        @Expose
        public Object orderBookRealtime;
        @SerializedName("MarketCapitalization")
        @Expose
        public String marketCapitalization;
        @SerializedName("MarketCapRealtime")
        @Expose
        public Object marketCapRealtime;
        @SerializedName("EBITDA")
        @Expose
        public String eBITDA;
        @SerializedName("ChangeFromYearLow")
        @Expose
        public String changeFromYearLow;
        @SerializedName("PercentChangeFromYearLow")
        @Expose
        public String percentChangeFromYearLow;
        @SerializedName("LastTradeRealtimeWithTime")
        @Expose
        public Object lastTradeRealtimeWithTime;
        @SerializedName("ChangePercentRealtime")
        @Expose
        public Object changePercentRealtime;
        @SerializedName("ChangeFromYearHigh")
        @Expose
        public String changeFromYearHigh;
        @SerializedName("PercebtChangeFromYearHigh")
        @Expose
        public String percebtChangeFromYearHigh;
        @SerializedName("LastTradeWithTime")
        @Expose
        public String lastTradeWithTime;
        @SerializedName("LastTradePriceOnly")
        @Expose
        public String lastTradePriceOnly;
        @SerializedName("HighLimit")
        @Expose
        public Object highLimit;
        @SerializedName("LowLimit")
        @Expose
        public Object lowLimit;
        @SerializedName("DaysRange")
        @Expose
        public String daysRange;
        @SerializedName("DaysRangeRealtime")
        @Expose
        public Object daysRangeRealtime;
        @SerializedName("FiftydayMovingAverage")
        @Expose
        public String fiftydayMovingAverage;
        @SerializedName("TwoHundreddayMovingAverage")
        @Expose
        public String twoHundreddayMovingAverage;
        @SerializedName("ChangeFromTwoHundreddayMovingAverage")
        @Expose
        public String changeFromTwoHundreddayMovingAverage;
        @SerializedName("PercentChangeFromTwoHundreddayMovingAverage")
        @Expose
        public String percentChangeFromTwoHundreddayMovingAverage;
        @SerializedName("ChangeFromFiftydayMovingAverage")
        @Expose
        public String changeFromFiftydayMovingAverage;
        @SerializedName("PercentChangeFromFiftydayMovingAverage")
        @Expose
        public String percentChangeFromFiftydayMovingAverage;
        @SerializedName("Name")
        @Expose
        public String name;
        @SerializedName("Notes")
        @Expose
        public Object notes;
        @SerializedName("Open")
        @Expose
        public String open;
        @SerializedName("PreviousClose")
        @Expose
        public String previousClose;
        @SerializedName("PricePaid")
        @Expose
        public Object pricePaid;
        @SerializedName("ChangeinPercent")
        @Expose
        public String changeinPercent;
        @SerializedName("PriceSales")
        @Expose
        public String priceSales;
        @SerializedName("PriceBook")
        @Expose
        public String priceBook;
        @SerializedName("ExDividendDate")
        @Expose
        public Object exDividendDate;
        @SerializedName("PERatio")
        @Expose
        public String pERatio;
        @SerializedName("DividendPayDate")
        @Expose
        public Object dividendPayDate;
        @SerializedName("PERatioRealtime")
        @Expose
        public Object pERatioRealtime;
        @SerializedName("PEGRatio")
        @Expose
        public String pEGRatio;
        @SerializedName("PriceEPSEstimateCurrentYear")
        @Expose
        public String priceEPSEstimateCurrentYear;
        @SerializedName("PriceEPSEstimateNextYear")
        @Expose
        public String priceEPSEstimateNextYear;
        @SerializedName("Symbol")
        @Expose
        public String symbol1;
        @SerializedName("SharesOwned")
        @Expose
        public Object sharesOwned;
        @SerializedName("ShortRatio")
        @Expose
        public String shortRatio;
        @SerializedName("LastTradeTime")
        @Expose
        public String lastTradeTime;
        @SerializedName("TickerTrend")
        @Expose
        public Object tickerTrend;
        @SerializedName("OneyrTargetPrice")
        @Expose
        public String oneyrTargetPrice;
        @SerializedName("Volume")
        @Expose
        public String volume;
        @SerializedName("HoldingsValue")
        @Expose
        public Object holdingsValue;
        @SerializedName("HoldingsValueRealtime")
        @Expose
        public Object holdingsValueRealtime;
        @SerializedName("YearRange")
        @Expose
        public String yearRange;
        @SerializedName("DaysValueChange")
        @Expose
        public Object daysValueChange;
        @SerializedName("DaysValueChangeRealtime")
        @Expose
        public Object daysValueChangeRealtime;
        @SerializedName("StockExchange")
        @Expose
        public String stockExchange;
        @SerializedName("DividendYield")
        @Expose
        public String dividendYield;
        @SerializedName("PercentChange")
        @Expose
        public String percentChange;

    }

    public class Results {

        @SerializedName("quote")
        @Expose
        public Quote quote;

    }

    public class Url {

        @SerializedName("execution-start-time")
        @Expose
        public String executionStartTime;
        @SerializedName("execution-stop-time")
        @Expose
        public String executionStopTime;
        @SerializedName("execution-time")
        @Expose
        public String executionTime;
        @SerializedName("content")
        @Expose
        public String content;

    }

}
