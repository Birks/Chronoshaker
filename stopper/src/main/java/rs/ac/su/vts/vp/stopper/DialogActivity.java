package rs.ac.su.vts.vp.stopper;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class DialogActivity extends Activity {
    String[] value = new String[255];
    String[] value2 = new String[255];
    String tempString = "";
    TextView debugtext;
    TextView title;
    int timemode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);
        debugtext = (TextView) findViewById(R.id.szartext);
        title = (TextView) findViewById(R.id.activity_title);
        Bundle b = this.getIntent().getExtras();

        if (b != null) {
            value = b.getStringArray("key");
            value2 = b.getStringArray("key2");
            timemode = b.getInt("tm");
        }


        int counter = Integer.parseInt(value[0]);
        Log.d("stopper_verbose", "dialogban:    " + counter);
        Log.d("stopper_verbose", "timemode dialogban " + timemode);
        //tempString+=value2[0];// beíruk az első sorba a first splitet


        if (timemode == 1) {
            title.setText(getResources().getString(R.string.activity_title_lap));
            for (int j = 1; j <= counter; j++) {
                if (j != 1) tempString += "\n";
                Log.d("stopper_verbose", "dialogban:    " + value[j] + "  " + value2[j]);
                if (value2[j].length() <= 8)
                    tempString += value2[j] + "                              " + value[j];
                else
                    tempString += value2[j] + "                         " + value[j];

            }
        } else {
            title.setText(getResources().getString(R.string.activity_title_split));

            for (int j = 1; j <= counter; j++) {
                if (j != 1) tempString += "\n";
                Log.d("stopper_verbose", "dialogban:    " + value[j] + "  " + value2[j]);
                if (value2[j].length() <= 8)
                    tempString += value2[j] + "                             +" + value[j];
                else
                    tempString += value2[j] + "                        +" + value[j];
            }

        }

        Log.d("stopper_verbose", "dialogban:    " + tempString);

        debugtext.setText(tempString);
        debugtext.setTextColor(Color.BLACK);

    }

    public void finishDialog(View v) {
        DialogActivity.this.finish();
    }
}
