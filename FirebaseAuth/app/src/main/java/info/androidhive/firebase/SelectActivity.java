package info.androidhive.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class SelectActivity extends AppCompatActivity {

    Button _selectstudent;
    Button _selectdriver;
    Button _selectparent;
    static int count=-1;
    static int signOutCount=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        _selectstudent=(Button)findViewById(R.id.btn_student_login) ;
        _selectdriver=(Button)findViewById(R.id.btn_driver_login) ;
        _selectparent=(Button)findViewById(R.id.btn_parent_login) ;
        if(MainActivity.signedOut==1){
            MainActivity.signedOut=0;
            signOutCount++;
            _selectdriver.setVisibility(View.VISIBLE);
            _selectparent.setVisibility(View.VISIBLE);
            _selectstudent.setVisibility(View.VISIBLE);
        }

        _selectstudent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity for Student

                _selectdriver.setVisibility(View.GONE);
                _selectparent.setVisibility(View.GONE);
                Intent intent = new Intent(SelectActivity.this, LoginActivity.class);
                count++;
                intent.putExtra("previous","select");
                intent.putExtra("caller","StudentLogin");
                startActivity(intent);


            }
        });

        _selectdriver.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity for Driver
                _selectstudent.setVisibility(View.GONE);
                _selectparent.setVisibility(View.GONE);
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                count++;
                intent.putExtra("previous","select");
                intent.putExtra("caller","DriverLogin");
                startActivity(intent);
            }
        });

        _selectparent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity for Student
                _selectdriver.setVisibility(View.GONE);
                _selectstudent.setVisibility(View.GONE);
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                count++;
                intent.putExtra("previous","select");
                intent.putExtra("caller","ParentLogin");
                startActivity(intent);
            }
        });

    }

}
