package saymobile.company.saytechbuyer

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.StorageReference
import saymobile.company.saytechbuyer.model.GlideApp
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun getProgressDrawable(context: Context): CircularProgressDrawable {
        return CircularProgressDrawable(context).apply {
            strokeWidth = 10f
            centerRadius = 50f
            start()
        }
    }

    fun ImageView.loadImage(
        imageRef: StorageReference,
        progressDrawable: CircularProgressDrawable
    ) {
        // place holder not working with circular imageView
        val options = RequestOptions()
            .placeholder(progressDrawable)
            .error(R.mipmap.ic_launcher)
            .centerCrop()
        GlideApp.with(this)
            .setDefaultRequestOptions(options)
            .load(imageRef)
            .into(this)
    }

    fun formatCurrency(price: Double) : String{
        /**
         * Use currency format later
         */
        val decForm = DecimalFormat("Q#,###.00")

        return decForm.format(price)
//        val guateLocale = Locale("es", "GT")
//        val currency = Currency.getInstance(guateLocale)
        //        val formatCurrency = NumberFormat.getCurrencyInstance(guateLocale)
//        formatCurrency.currency = currency
//        formatCurrency.maximumFractionDigits = 2
//        val formCurr = formatCurrency.format(price)
        //        decForm.currency = currency
//        val poo = decForm.format(formCurr)
//        val decimalFormat = String.format("%.2f", price)
//        return "Q$decimalFormat"
    }

    fun formatFloatDecimal(value: Double): Float {
        val bigDecimal = BigDecimal(value)
        bigDecimal.setScale(2, RoundingMode.HALF_UP)
        return bigDecimal.toFloat()
    }


