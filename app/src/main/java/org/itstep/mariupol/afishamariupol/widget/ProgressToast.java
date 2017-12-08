package org.itstep.mariupol.afishamariupol.widget;

import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.itstep.mariupol.afishamariupol.R;

public class ProgressToast extends Toast {

	private static TextView mProgressToastText;
	private final Toast mProgressToast;

	public ProgressToast(Context context) {
		super(context);

		LayoutInflater inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        /*
         * Производится инициализация разметки всплывающего сообщения,
         * устанавливается изображение значка информации в всплывающее сообщение
         */
		View rootView = inflater.inflate(R.layout.layout_progress_toast, null);
		//ImageView toastImage = (ImageView) rootView.findViewById(R.id.imageView1);
		//toastImage.setImageResource(R.mipmap.ic_info);
		//mProgressToastText = (TextView) rootView.findViewById(R.id.textView1);

        /*
         * Устанавливается инициализированный внешний вид,
         * местоположение всплывающего сообщения на экране устройства,
         * а также длительность существованая сообщения
         */
		this.setView(rootView);
		this.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
		this.setDuration(Toast.LENGTH_SHORT);
		mProgressToast = this;
	}

	/*
     * Метод вызова сообщения без установки длительности существования
     * с передачей сообщению текстовой информации в качестве последовательности
     * текстовых символов или строки
     */
	public static ProgressToast makeText(Context context, CharSequence text) {
		ProgressToast result = new ProgressToast(context);
		//mProgressToastText.setText(text);

		return result;
	}

	/*
     * Метод вызова сообщения с установкой длительности существования и
     * с передачей сообщению текстовой информации в качестве последовательности
     * текстовых символов или строки
     */
	public static ProgressToast makeText(Context context, CharSequence text, int duration) {
		ProgressToast result = new ProgressToast(context);
		result.setDuration(duration);
		//mProgressToastText.setText(text);

		return result;
	}

	/*
     * Метод вызова сообщения без установки длительности существования
     * с передачей сообщению ID текстового ресурса
     */
	public static Toast makeText(Context context, int resId)
			throws Resources.NotFoundException {
		return makeText(context, context.getResources().getText(resId));
	}

	/*
     * Метод вызова сообщения с установкой длительности существования и
     * с передачей сообщению ID текстового ресурса
     */
	public static Toast makeText(Context context, int resId, int duration)
			throws Resources.NotFoundException {
		return makeText(context, context.getResources().getText(resId), duration);
	}
}