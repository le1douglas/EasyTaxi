package douglas.leone.easytaxi.ui.mainActivity;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import douglas.leone.easytaxi.R;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        final BottomNavigationView bottomNavigationView =
                findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.map);


        final NavController navController = Navigation.findNavController(this, R.id.my_nav_host_fragment);

        //binds menu item with the appropriate destination
        NavigationUI.setupWithNavController(bottomNavigationView, navController);


    }

}
