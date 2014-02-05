package com.example.terminator;

import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends ActionBarActivity {
    private EditText Screen;
    private float Buffer;
    private String Operation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //My code
        Screen = (EditText) findViewById(R.id.Screen);
        Screen.setClickable(false);
        int idList[] = {R.id.button0,R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9, R.id.buttonEqual, R.id.buttonMinus, R.id.buttonDivide, R.id.buttonMulti, R.id.buttonPlus, R.id.buttonFloat, R.id.buttonC};

        for (int id:idList){
            View v = (Button) findViewById(id);
            v.setOnClickListener(BtnListener);
            v.setOnTouchListener(TouchListener);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public void mMath(String str){
        try{
            Buffer = Float.parseFloat(Screen.getText().toString());
        }catch (Exception e){
            Screen.setText("Error");
            return;
        }

        Operation = str;
        Screen.setText("0");
    }

    public void mResult(){
        float temp;
        try{
            temp = Float.parseFloat(Screen.getText().toString());
        }catch (Exception e){
            Screen.setText("Error");
            return;
        }

        float result = 0;
        if ( Operation.equals("+")) {
            result = Buffer + temp;
        }else if ( Operation.equals("-")) {
            result = Buffer - temp;
        } else if ( Operation.equals("*")) {
            result = Buffer * temp;
        }else if ( Operation.equals("/")) {
            result = Buffer/temp ;
        }else{
            return;
        }
        Screen.setText(String.valueOf(result));
    }

    public void getKeyboard(String str){
        String temp = Screen.getText().toString();
        if (temp.equals("0")){
            temp= "";
        }
        temp += str;
        Screen.setText(temp);
    }

    final View.OnTouchListener TouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    v.getBackground().setColorFilter(0xe0f47521,android.graphics.PorterDuff.Mode.SRC_ATOP);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    v.getBackground().clearColorFilter();
                    v.invalidate();
                    break;
                }
            }


            return false;
        }
    };

    final View.OnClickListener BtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.buttonC:
                    Screen.setText("0");
                    Buffer = 0;
                    Operation = "";
                    break;
                case R.id.buttonPlus:
                    mMath("+");
                    break;
                case R.id.buttonMulti:
                    mMath("*");
                    break;
                case R.id.buttonMinus:
                    mMath("-");
                    break;
                case R.id.buttonEqual:
                    mResult();
                    break;
                case R.id.buttonDivide:
                    mMath("/");
                    break;
                default:
                    String nombre = ((Button) v).getText().toString();
                    getKeyboard(nombre);
                    break;
            }
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
