package org.itstep.mariupol.afishamariupol;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.app.NavUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * класс фрагмента для отображения картинки постера на весь экран
 */
public class BigImageFragment extends Fragment {

    private ImageView mBigImageView;
    public static final String EXTRA_IMAGE_URL = "com.itstep.mariupol.afishamariupol.image_url";
    private String mImageUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setHasOptionsMenu(true);
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }

		//при создании активности получаем адрес изображения постера
        mImageUrl = getActivity().getIntent().getStringExtra(EXTRA_IMAGE_URL);
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//при создании представления загружаем изображение с сайта и выводим его в виджет типа ImageView
        View view = inflater.inflate(R.layout.fragment_big_image, container, false);
        mBigImageView = (ImageView) view.findViewById(R.id.bigImageView);
        Picasso.with(getActivity())
                .load(mImageUrl)
                .placeholder(R.drawable.dowloading)
                .error(R.drawable.dowloading_error)
                .into(mBigImageView);

        return view;
    }
}
