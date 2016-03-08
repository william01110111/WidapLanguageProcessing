package net.widap.widaplanguageprocessing;

import net.widap.nlp.*;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.support.v7.app.*;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    WidapMind nlp=null;

    //@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nlp=new WidapMind();
        //Button button = (Button) findViewById(R.id.goButton);
    }

    public void buttonCallback(View view) {

        EditText inputTextUI=(EditText) findViewById(R.id.inputText);
        TextView outputTextUI=(TextView) findViewById(R.id.outputText);

        String inputText=inputTextUI.getText().toString();
        String outputText;

        if (nlp!=null)
            outputText=nlp.parse(inputText);
        else
            outputText="Error: nlp was not initialised";

        outputTextUI.setText(outputText);
    }
}



