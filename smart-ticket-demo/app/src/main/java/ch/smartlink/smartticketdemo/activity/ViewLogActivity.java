package ch.smartlink.smartticketdemo.activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import ch.smartlink.smartticketdemo.R;
import ch.smartlink.smartticketdemo.cipurse.Logger;
import ch.smartlink.smartticketdemo.control.BaseCardOperationManager;

/**
 * Created by caoky on 11/30/2015.
 */
public class ViewLogActivity extends AppCompatActivity {

    private TextView txtLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        txtLog = (TextView) findViewById(R.id.txtLog);
        loadLog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLog();
    }

    private void loadLog() {
        if (txtLog != null) {
            txtLog.setText(Logger.getLogContent());
        }
    }
}
