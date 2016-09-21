package com.androidprojects.kaustav.fpl;

import android.app.ProgressDialog;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidprojects.kaustav.helper.SQLiteHandler;
import com.androidprojects.kaustav.helper.SessionManager;

import java.util.HashMap;

public class Home extends AppCompatActivity {

    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;


    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout;

    private SQLiteHandler db;
    private SessionManager session;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

       /* txtName = (TextView) findViewById(R.id.name);
        txtEmail = (TextView) findViewById(R.id.email);
        btnLogout = (Button) findViewById(R.id.btnLogout);
*/
        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("username");
        String email = user.get("useremail");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.expander);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {

                    case R.id.transfer_market:
                        launchTransfers();
                        break;

                    case R.id.my_wishlist:
                        launchWishlist();
                        break;

                    case R.id.my_team:
                        launchMyTeam();
                        break;

                    case R.id.my_career:
                        launchMyCareer();
                        break;

                    case R.id.change_squad_name:
                        launchSelectTeam();
                        break;

                    case R.id.btn_logout:
                        logoutUser();      // logs out the user
                        break;
                }
                return true;
            }
        });


        // Logout button click event commented out because it is handled above
       /* btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });*/
    }

    private void launchMyCareer() {
    }

    private void launchSelectTeam() {
    }

    private void launchMyTeam() {

    }

    private void launchWishlist() {

    }

    private void launchTransfers() {

    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        pDialog.setMessage("Logging out ...");
        showDialog();

        session.setLogin(false);

        db.deleteUsers();

        hideDialog();
        // Launching the login activity
        Intent intent = new Intent(Home.this, LoginRegister.class);
        startActivity(intent);
        finish();
    }

    private void updateDisplay(Fragment fragment) {

        // Create new fragment and transaction
        //Fragment newFragment = new ExampleFragment();         // For creating the respective fragment
       /* FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}

