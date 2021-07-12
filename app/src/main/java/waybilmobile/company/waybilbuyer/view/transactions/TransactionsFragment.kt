package waybilmobile.company.waybilbuyer.view.transactions

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.ToggleButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_transactions.*
import waybilmobile.company.waybilbuyer.R
import waybilmobile.company.waybilbuyer.viewModel.transactions.TransactionsViewModel


class TransactionsFragment : Fragment() {

    private lateinit var viewModel: TransactionsViewModel
    private var transactionsAdapter = TransactionsAdapter(arrayListOf())
    private var transactionYearsAdapter = TransactionYearsAdapter(arrayListOf())


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        close_fragment_transactions.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel = ViewModelProviders.of(this).get(TransactionsViewModel::class.java)


        years_filter_recycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = transactionYearsAdapter
        }

        transactions_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionsAdapter
        }


        toggle_month.setFilterToggleListener(radio_group_month)
        toggle_year.setFilterToggleListener(years_filter_recycler)



        radio_group_month.setOnCheckedChangeListener { _, checkedId ->
            viewModel.selectedMonth(checkedId)
            toggle_month.text = radio_group_month.findViewById<RadioButton>(checkedId).text
            toggle_month.isChecked = false
            radio_group_month.visibility = View.GONE
        }


        //Checking the radio button corresponding to current month
        val initialMonthCheck = radio_group_month.getChildAt(viewModel.currentMonthSelected.value!! - 1)
        radio_group_month.check(initialMonthCheck.id)

        toggle_year.text = viewModel.currentYearSelected.value.toString()
        println(viewModel.currentYearSelected.value.toString())




        viewModel.refresh()
        observeTransactionViewModel()


    }


    private fun observeTransactionViewModel() {
        viewModel.transactionsList.observe(
            viewLifecycleOwner,
            Observer { transactionsList ->
                if (transactionsList.isEmpty()){
                    no_data_message.visibility = View.VISIBLE
                    transactions_recycler.visibility = View.GONE
                }else{
                    transactionsList?.let {
                        transactions_recycler.visibility = View.VISIBLE
                        no_data_message.visibility = View.GONE
                        transactionsAdapter.updateTransactionsList(transactionsList)
                    }
                }
            })
        viewModel.currentMonthSelected.observe(viewLifecycleOwner, Observer { currentMonth ->
            currentMonth?.let {
                viewModel.refresh()
            }
        })
        viewModel.currentYearSelected.observe(viewLifecycleOwner, Observer { currentYear ->
            currentYear?.let {
                viewModel.refresh()
            }
        })
        viewModel.yearsActive.observe(viewLifecycleOwner, Observer { yearsActive ->
            yearsActive?.let {
                transactionYearsAdapter.updateTransactionYears(yearsActive as ArrayList<Int>)
            }
        })

        viewModel.transactionsTotal.observe(viewLifecycleOwner, Observer { transactionsTotal ->
            transactionsTotal?.let {
                transactionsTotal_transactionsFragment.text = it
            }
        })

        transactionYearsAdapter.selectedYear.observe(viewLifecycleOwner, Observer{ currentYear ->
            currentYear?.let {
                viewModel.selectedYear(currentYear)
                toggle_year.text = currentYear.toString()
                toggle_year.isChecked = false

            }
        })

    }

    private fun ToggleButton.setFilterToggleListener(subCategoryContainer: View) {
        this.setOnFocusChangeListener { _, hasFocus -> this.isChecked = hasFocus }
        this.setOnCheckedChangeListener { _, isChecked ->
            subCategoryContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }


}