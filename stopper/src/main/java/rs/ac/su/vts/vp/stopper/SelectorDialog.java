package rs.ac.su.vts.vp.stopper;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SelectorDialog extends DialogFragment {

    private ArrayList mSelectedItems;
    private int outerWhich = 0;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mSelectedItems = new ArrayList();  // Where we track the selected items
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.timemode)
                .setItems(R.array.timemode_items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        outerWhich=which;
                    }
                });
        return builder.create();
    }

    public int getWhich(){
        return outerWhich;
    }
}

