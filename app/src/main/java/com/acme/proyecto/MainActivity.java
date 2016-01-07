package com.acme.proyecto;

import android.app.FragmentTransaction;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    FragmentPagerAdapter adapterViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);

        //--- ViewPager handler ----------------

        vpPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {

           /*     //si la pestaña seleccionada es estado, tengo que eliminar la instancia activa de ConfigFragment para que vuelva a loguear
               if (position==1){
                 //  vpPager.getAdapter().destroyItem((ViewGroup)findViewById(R.id.vpPager),vpPager.getAdapter().getItemPosition("ConfigFragment"),"ConfigFragment");
                   FragmentManager fm = getSupportFragmentManager();
                   List <Fragment> fragments = fm.getFragments();
                   Fragment lastFragment = fragments.get(fragments.size() - 1);
                   if ((lastFragment!=null) && (lastFragment instanceof ConfigFragment)) {
                       FragmentTransaction ft = getFragmentManager().beginTransaction();
                       lastFragment.getResources()
                   }
               }*/
            }
        });

        //-------------Fin ViewPager handler-------------------

    }

    //------------------------------------------------------------------

    // PagerAdapter
    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return StateFragment.newInstance();
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return ConfigFragment.newInstance();
                default:
                    return null;
            }
        }

    }
    //---fin PagerAdapter

}


