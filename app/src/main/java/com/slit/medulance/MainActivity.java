package com.slit.medulance;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private WebView webView;
    private EditText location1, location2;
    private Button submit, mSOS;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference locationRef;
    private FusedLocationProviderClient fusedLocationClient;
    private String origin = "";
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    private String latitude;
    private String longitude;
    private LatLng userlocation;
    String UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://zacseed.com/map/map.php?location=Colombo");
        location1=findViewById(R.id.editText);
        location2=findViewById(R.id.editText2);
        getMyLocation();
        submit=findViewById(R.id.button2);

        //Initializing btnSOS
        mSOS = findViewById(R.id.btnSOS);

        progressDialog=new ProgressDialog(this);
        firebaseAuth=FirebaseAuth.getInstance();
        UID=firebaseAuth.getCurrentUser().getUid();
        locationRef= FirebaseDatabase.getInstance().getReference().child("Location").child(UID);



        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://zacseed.com/map/directions.php?origin="+ location1.getText() +"&destination=" + location2.getText();
                if (location1.getText().toString().contains("Your Location")){
                    url = "http://zacseed.com/map/directions.php?origin="+ latitude + "," + longitude +"&destination=" + location2.getText();
                }else {
                    url = "http://zacseed.com/map/directions.php?origin="+ location1.getText() +"&destination=" + location2.getText();
                }

                webView.loadUrl(url);
                saveData();


            }
        });

        mSOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.setMessage("Processing SOS Request...");
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(false);

                HashMap nearHosp =new HashMap();
                HashMap reqDate =new HashMap();


                nearHosp.put("Nearest Hospital", "6.919194,79.869568");

                Long tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();


                DatabaseReference dateRef = FirebaseDatabase.getInstance().getReference().child("Location").child(UID);

                dateRef.child("Date").setValue(ts);

                DatabaseReference locRef = FirebaseDatabase.getInstance().getReference().child("Location").child(UID);
                locRef.child("location1").setValue(location1.getText());



                DatabaseReference nearHosplocationRef = FirebaseDatabase.getInstance().getReference().child("Location").child(UID).child("SOS");

                nearHosplocationRef.updateChildren(nearHosp).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task)
                    {
                        if(task.isSuccessful())
                        {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this,"Location Updated!!", Toast.LENGTH_SHORT).show();



                        }
                        else
                        {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this,"Error!! Please Try Again!!", Toast.LENGTH_SHORT).show();
                        }


                    }

                });

            }
        });
        Button cancelBtn = findViewById(R.id.button3);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMyLocation();
                location2.setText("");
            }
        });

    }

    private void saveData()
    {
        String Location1=location1.getText().toString();
        String Location2=location2.getText().toString();


            progressDialog.setMessage("Updating Location...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);
            HashMap usermap=new HashMap();

            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();

            usermap.put("Date", ts);
            usermap.put("Location1", Location1);
            usermap.put("Location2", Location2);
            usermap.put("SOS", false);

            locationRef.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this,"Location Updated!!", Toast.LENGTH_SHORT).show();
                        location1.setText(" ");
                        location2.setText(" ");


                    }
                    else
                    {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this,"Error!! Please Try Again!!", Toast.LENGTH_SHORT).show();
                    }


                }

            });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private  void getMyLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        longitude = String.valueOf(location.getLongitude());
        latitude = String.valueOf(location.getLatitude());

        webView.loadUrl("http://zacseed.com/map/map.php?location=" + latitude + "," + longitude);
        location1.setText(latitude+","+longitude);

    }


}
