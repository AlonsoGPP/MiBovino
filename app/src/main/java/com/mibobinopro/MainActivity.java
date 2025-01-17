package com.mibobinopro;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.view.View;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;

import android.view.WindowManager;


import com.mibobinopro.modals.Fertilizer;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener{

    private static final int POS_CLOSE = 0;
    private static final int POS_DASHBOARD = 1;
    private static final int POS_MY_ANIMALS = 2;
    private static final int POS_ANIMAL_EVENTS = 3;
    private static final int POS_FARM_TASKS= 4;
    private static final int POS_MEDICAL_CABINET = 5;
    private static final int POS_FEED_HISTORY = 6;
    private static final int POS_LOGOUT = 7;
    private static final String TAG = "Main Activity";

    private String[] screenTitles;
    private Drawable[] screenIcons;

    private SlidingRootNav slideRootNav;
    private BottomNavigationView bottomNavBar;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
//        Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        setContentView(R.layout.activity_main);
        bottomNavBar = (BottomNavigationView) findViewById(R.id.bottom_nav);
        toolbar = findViewById(R.id.toolbar);
        try{
            setSupportActionBar(toolbar);
        }catch(IllegalStateException e){
            Log.e(TAG, "onCreate: "+ e.getMessage() );
        }

        slideRootNav = new SlidingRootNavBuilder(this)
                .withDragDistance(180)
                .withRootViewScale(0.75f)
                .withRootViewElevation(25)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.drawer_menu)
                .inject();

        screenIcons = loadScreenIcons();
        screenTitles = loadScreenTitles();

        DrawerAdapter adapter = new DrawerAdapter(Arrays.asList(
                createItemFor(POS_CLOSE),
                createItemFor(POS_DASHBOARD),
                createItemFor(POS_MY_ANIMALS),
                createItemFor(POS_ANIMAL_EVENTS),
                createItemFor(POS_FARM_TASKS),
                createItemFor(POS_MEDICAL_CABINET),
                createItemFor(POS_FEED_HISTORY),
                new SpaceItem(150),
                createItemFor(POS_LOGOUT)
        ));

        adapter.setListener(this);

        RecyclerView list = findViewById(R.id.drawer_list);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        adapter.setSelected(POS_DASHBOARD);


        /**
         * bottom navigation bar
         */
       // bottomNavBar.setSelectedItemId(R.id.bottom_home);
        bottomNavBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                switch (item.getItemId()) {
                    case R.id.bottom_home:
                        DashboardFragment dashboardFragment = new DashboardFragment();
                        transaction.replace(R.id.container, dashboardFragment);
                        break;

                    case R.id.bottom_settings:
                        break;

                    case R.id.bottom_user:
                        break;
                }
                transaction.addToBackStack(null);
                transaction.commit();
                return true;
            }
        });
    }

    private DrawerItem createItemFor(int position){
        return new SimpleItem(screenIcons[position],screenTitles[position])
                .withIconTint(color(R.color.green))
                .withTextTint(color(R.color.black))
                .withSelectedIconTint(color(R.color.green))
                .withSelectedTextTint(color(R.color.green));
    }

    @ColorInt
    private int color(@ColorRes int res){
        return ContextCompat.getColor(this,res);
    }

    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.id_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.id_activityScreenIcons);
        Drawable[] icons = new Drawable[ta.length()];

        for(int i=0 ; i<ta.length() ; i++) {
            int id = ta.getResourceId(i,0);
            if (id != 0){
                icons[i] = ContextCompat.getDrawable(this,id);
            }
        }

        ta.recycle();
        return icons;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onItemSelected(int position) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (position == POS_DASHBOARD){
            toolbar.setTitle("Principal");
            DashboardFragment dashboardFragment = new DashboardFragment();
            transaction.replace(R.id.container, dashboardFragment);
        }else if(position == POS_MY_ANIMALS){
            toolbar.setTitle("Mis Animales");
            MyAnimalsFragment animals = new MyAnimalsFragment();
            transaction.replace(R.id.container, animals);
        }else if(position == POS_ANIMAL_EVENTS){
            toolbar.setTitle("Eventos Animales");
            AnimalEventFragment events = new AnimalEventFragment();
            transaction.replace(R.id.container, events);
        }else if(position == POS_FARM_TASKS){
            toolbar.setTitle("Tareas Granja");
            FarmTasksFragment farmTasks = new FarmTasksFragment();
            transaction.replace(R.id.container, farmTasks);
        }else if(position == POS_MEDICAL_CABINET){
            toolbar.setTitle("Medicamento");
            MedicalCabinetFragment medical = new MedicalCabinetFragment();
            transaction.replace(R.id.container, medical);
        }else if(position == POS_FEED_HISTORY){
            toolbar.setTitle("Detalle De Alimentos");
            FeedHistoryFragment feedHistoryFragment = new FeedHistoryFragment();
            transaction.replace(R.id.container, feedHistoryFragment);
        }else{
            finish();
        }

        slideRootNav.closeMenu();
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void InsertMedicineFragmentChange(View view){
        toolbar.setTitle("Nueva Medicina");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        MedicalIsertFragment fragment = new MedicalIsertFragment();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }
    public void MedicalCabinet(View view){
        toolbar.setTitle("Medicamento");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        MedicalCabinetFragment  fragment = new MedicalCabinetFragment();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }
    public void navigateToFeedHostory(View view){
        toolbar.setTitle("Detalle de Alimento");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        FeedHistoryFragment  fragment = new FeedHistoryFragment();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }
    public void navigateToInsertFeed(View view){
        toolbar.setTitle("Nuevo Alimento");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        FeedInsert  fragment = new FeedInsert();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    public void navigateToAnimalEvent(View view){
        toolbar.setTitle("Envento Animal");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        AnimalEventFragment animalEventFragment = new AnimalEventFragment();
        transaction.replace(R.id.container, animalEventFragment);
        transaction.commit();
    }

    public void navigateToAddAnimalEvent(View view){
        toolbar.setTitle("Eventos Animal");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        AddAnimalEvent addAnimalEvent = new AddAnimalEvent();
        transaction.replace(R.id.container, addAnimalEvent);
        transaction.commit();
    }

    public void navigateToAddAnimal(View view){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        AddAnimalFragment addAnimal = new AddAnimalFragment();
        transaction.replace(R.id.container, addAnimal);
        transaction.commit();
    }

    public void navigateToMyAnimal(View view){
        toolbar.setTitle(getString(R.string.my_animals));
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        MyAnimalsFragment myAnimalsFragment = new MyAnimalsFragment();
        transaction.replace(R.id.container, myAnimalsFragment);
        transaction.commit();
    }

  
    public void navigateToAddFarmTask(View view){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        AddNewFarmTask addNewFarmTask = new AddNewFarmTask();
        transaction.replace(R.id.container,addNewFarmTask);
        transaction.commit();
    }

    public void navigateToFarmTask(View view){
        toolbar.setTitle("Tareas Granja");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        FarmTasksFragment myFarmTaskFragment = new FarmTasksFragment();
        transaction.replace(R.id.container, myFarmTaskFragment);
        transaction.commit();
    }

}