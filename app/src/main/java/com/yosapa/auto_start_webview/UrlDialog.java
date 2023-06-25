package com.yosapa.auto_start_webview;

import android.app.Dialog;
import android.content.Context;
import android.text.InputFilter;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

public class UrlDialog {
    public final EditText editName;
    private final Dialog dialog;
    public final TextView ok;

    public UrlDialog(Context context,String url){
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.new_url_dialog);
        editName = dialog.findViewById(R.id.editName);
        editName.setText(url);
        TextView cancel = dialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        ok = dialog.findViewById(R.id.ok);
    }

    public void show(){
        dialog.show();
    }

    public void cancel(){
        dialog.cancel();
    }
}
