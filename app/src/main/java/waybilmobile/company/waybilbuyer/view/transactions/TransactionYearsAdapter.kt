package waybilmobile.company.waybilbuyer.view.transactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_year_filter.view.*
import waybilmobile.company.waybilbuyer.R

class TransactionYearsAdapter (private val transactionYearsList: ArrayList<Int>) : RecyclerView.Adapter<TransactionYearsAdapter.TransactionYearsViewHolder>(){

    val selectedYear = MutableLiveData<Int>()

    fun updateTransactionYears(newTransactionYearsList: ArrayList<Int>){
        transactionYearsList.clear()
        transactionYearsList.addAll(newTransactionYearsList)
        notifyDataSetChanged()
    }

    class TransactionYearsViewHolder(var view: View) : RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionYearsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_year_filter, parent, false)
        return TransactionYearsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return transactionYearsList.size
    }

    override fun onBindViewHolder(holder: TransactionYearsViewHolder, position: Int) {
        val item = transactionYearsList[position]
        holder.view.year_filter.text = transactionYearsList[position].toString()
        holder.view.setOnClickListener {selectedYear.value = item
        }
    }
}