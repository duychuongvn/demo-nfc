package ch.smartlink.smartticketdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import ch.smartlink.smartticketdemo.activity.CardHistoryActivity;
import ch.smartlink.smartticketdemo.activity.CardOperationActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void activeCardOperation(View view) {
        Intent listViewIntent = new Intent(getApplicationContext(), CardOperationActivity.class);
        startActivity(listViewIntent);
    }

    public void activeCardHistory(View view) {
        Intent listViewIntent = new Intent(getApplicationContext(), CardHistoryActivity.class);
        startActivity(listViewIntent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
