package com.encardio.android.escl10vt_r5.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.constant.Constants;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.TimeChart;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.Date;


public class LineChart extends AppCompatActivity {
    public static final String TYPE = "type";
    private final XYMultipleSeriesRenderer mRenderer = getDemoRenderer();
    TimeSeries time_series;
    String[] graphdateA;
    String[] graphbattV;
    String[] graphparaA;
    String[] graphtempA;
    String file_Text;
    String y_axis;
    double maxLimit, minLimit;
    int graph_length;
    double[] x;
    private final XYMultipleSeriesDataset mDataset = getDemoDataset();
    private GraphicalView mChartView;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph);
        Intent intent = getIntent();
        file_Text = intent.getStringExtra("FILETEXT");
        y_axis = intent.getStringExtra("YAXIS");
        graphdateA = intent.getStringArrayExtra("DATE");
        graphparaA = intent.getStringArrayExtra("PARA");
        graphtempA = intent.getStringArrayExtra("TEMP");
        graphbattV = intent.getStringArrayExtra("BATT");
        graph_length = intent.getIntExtra("LENGTH", 1);
        setRendererStyling();

        x = new double[graph_length];
        if (y_axis.contains("Batt")) {
            //{ 1,2,3,4,5,6,7,8 };
            maxLimit = 2 * (Double.parseDouble(graphbattV[1]));
            minLimit = -2 * (Double.parseDouble(graphbattV[1]));
            for (int j = 1; j <= graph_length; j++) {
                x[j - 1] = Float.parseFloat(Constants.setDecimalDigits(graphbattV[j - 1]));

            }

        } else if (y_axis.contains("Temp")) {

            maxLimit = 3 * (Double.parseDouble(graphtempA[1]));
            minLimit = -3 * (Double.parseDouble(graphtempA[1]));
            for (int j = 1; j <= graph_length; j++) {
                x[j - 1] = Float.parseFloat(Constants.setDecimalDigits(graphtempA[j - 1]));
            }

        } else {

            maxLimit = 5 * (Double.parseDouble(graphparaA[1]));
            minLimit = -5 * (Double.parseDouble(graphparaA[1]));
            for (int j = 1; j <= graph_length; j++) {
                x[j - 1] = Float.parseFloat(Constants.setDecimalDigits(graphparaA[j - 1]));
            }


        }
        if (mChartView == null) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
            fillData();
            mChartView = ChartFactory.getTimeChartView(getBaseContext(), mDataset, mRenderer, "yyyy/m/dd hh:mm:ss");
            mRenderer.setSelectableBuffer(10);
            layout.addView(mChartView, new LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        } else
            mChartView.repaint();
    }

    private void setRendererStyling() {
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
        mRenderer.setAxisTitleTextSize(16);
        mRenderer.setChartTitleTextSize(20);
        mRenderer.setLabelsTextSize(15);
        mRenderer.setShowGrid(true);
        //  mRenderer.setGridColor(Color.RED);
        mRenderer.setLegendTextSize(15);
        mRenderer.setMargins(new int[]{20, 30, 15, 0});
        mRenderer.setZoomButtonsVisible(true);
        // mRenderer.setPointSize(10);
    }

    private XYMultipleSeriesDataset getDemoDataset() {
        double[] seriesFirstY = x;
        // double[] seriesSecondY = {10,80,-40,-20,135,24,199,-34,80};
        time_series = new TimeSeries("Date/Time");


        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

        XYSeries firstSeries = new XYSeries("Sample series One");
        for (int i = 0; i < graph_length; i++)
            firstSeries.add(i, seriesFirstY[i]);
        dataset.addSeries(time_series);
 
       /* XYSeries secondSeries = new XYSeries("Sample series Two");
        for (int j = 0; j < 9; j++)
            secondSeries.add(j, seriesSecondY[j]);
        dataset.addSeries(secondSeries);*/
        return dataset;
    }

    private XYMultipleSeriesRenderer getDemoRenderer() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setMargins(new int[]{20, 30, 15, 0});
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(Color.BLUE);
        //   r.setPointStyle(PointStyle.SQUARE);
        //  r.setFillBelowLine(true);
        //   r.setFillBelowLineColor(Color.WHITE);
        // r.setFillPoints(true);
        renderer.addSeriesRenderer(r);
        // r = new XYSeriesRenderer();
        // r.setPointStyle(PointStyle.CIRCLE);
        r.setColor(Color.GREEN);
        // r.setFillPoints(true);
        //  renderer.addSeriesRenderer(r);
        renderer.setAxesColor(Color.DKGRAY);
        renderer.setLabelsColor(Color.LTGRAY);
        return renderer;
    }

    private void fillData() {
        long value = new Date().getTime() - 3 * TimeChart.DAY;
        for (int i = 0; i < graph_length; i++) {

            time_series.add(new Date(value + i * TimeChart.DAY / 4), i);
        }
    }
}
