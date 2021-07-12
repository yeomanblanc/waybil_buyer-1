package waybilmobile.company.waybilbuyer.viewModel.transactions

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.formatCurrency
import waybilmobile.company.waybilbuyer.model.orders.Order
import waybilmobile.company.waybilbuyer.model.user.User
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TransactionsViewModel: ViewModel() {

    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid
    private var mFirebaseDatabase = FirebaseFirestore.getInstance()

    private val transactionsDocRef = mFirebaseDatabase.collection("buyersTransactions")
        .document(userId)

    private val initialTransactionDate = mFirebaseDatabase.collection("buyers")
        .document(userId)

    private val _transactionsList = MutableLiveData<List<Order>>()
    val transactionsList: LiveData<List<Order>>
        get() = _transactionsList

    private val sdf = SimpleDateFormat("MMM-yyyy", Locale.US)

    private val _yearsActive = MutableLiveData<List<Int>>()
    val yearsActive: LiveData<List<Int>>
        get() = _yearsActive

    private val _transactionsTotal = MutableLiveData<String>()
    val transactionsTotal : LiveData<String>
        get() = _transactionsTotal

    private val _currentYearSelected = MutableLiveData<Int>()
    val currentYearSelected : LiveData<Int>
        get() = _currentYearSelected

    private val _currentMonthSelected = MutableLiveData<Int>()
    val currentMonthSelected : LiveData<Int>
        get() = _currentMonthSelected

    val loading = MutableLiveData<Boolean>()


    init {
        _currentYearSelected.value = Calendar.getInstance().get(Calendar.YEAR)
        _currentMonthSelected.value = Calendar.getInstance().get(Calendar.MONTH) + 1

        //does not have to be a live
        initialTransactionDate.get().addOnSuccessListener {documentSnapshot ->
            val userInfo = documentSnapshot.toObject(User::class.java)
            val dateJoined = userInfo?.dateJoined
            val yearSdf = SimpleDateFormat("yyyy").format(dateJoined!!.toDate())
            createYearList(yearSdf.toInt())
        }.addOnCompleteListener {
            //Nothing for now
        }
    }




    fun refresh() {
        val monthYearRef = "${_currentMonthSelected.value}-${_currentYearSelected.value}"

        transactionsDocRef.collection(monthYearRef)
            .addSnapshotListener { snapshot, error ->
                loading.value = true
                if (error != null) {
                    Log.d("TransactionSummaries", "Listen failed")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val snapshotList = ArrayList<Order>()
                    for (doc in snapshot.documents){
                        doc?.let{
                            snapshotList.add(it.toObject(Order::class.java)!!)
                        }

                    }

                    _transactionsList.value = snapshotList
                    calcTransactionsTotal()


                }
            }

    }


    fun selectedMonth(monthButtonId: Int){
        _currentMonthSelected.value = when(monthButtonId){
            R.id.radio_jan -> 1
            R.id.radio_feb -> 2
            R.id.radio_march -> 3
            R.id.radio_apr -> 4
            R.id.radio_may -> 5
            R.id.radio_jun -> 6
            R.id.radio_jul -> 7
            R.id.radio_aug -> 8
            R.id.radio_sep -> 9
            R.id.radio_oct -> 10
            R.id.radio_nov -> 11
            else -> 12
        }
    }

    fun selectedYear(year: Int){
        _currentYearSelected.value = year
    }

    private fun createYearList(initialYear: Int){
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val yearList = ArrayList<Int>()
        for (i in initialYear..currentYear){
            yearList.add(i)
        }
        _yearsActive.value = yearList
        println(yearList)
        println(_yearsActive.value)

    }

    private fun calcTransactionsTotal(){
        var tempSum = 0.0
        _transactionsList.value?.let {
            for(i in it){
                tempSum += i.orderTotal
            }

            _transactionsTotal.value = formatCurrency(tempSum)
        }
    }


}