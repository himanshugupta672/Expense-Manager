package com.example.budgettracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.budgettracker.R.color.red
import com.example.budgettracker.R.color.white
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalBalance: TextView
    private lateinit var txtBudget: TextView
    private lateinit var txtExpense: TextView
    private lateinit var deletedTransaction: Transaction
    private lateinit var transactions: List<Transaction>
    private lateinit var oldTransactions: List<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var btnAdd: FloatingActionButton
    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        transactions = arrayListOf()
        totalBalance = findViewById(R.id.totalBalance)
        txtBudget = findViewById(R.id.txtbudget)
        txtExpense = findViewById(R.id.txtExpense)
        recyclerView = findViewById(R.id.recyclerView)
        btnAdd = findViewById(R.id.btnAdd)
        transactionAdapter = TransactionAdapter(transactions)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)



        db= Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions").build()

        recyclerView.adapter =  transactionAdapter

        //swip to remove

        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return  false;
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTransaction(transactions[viewHolder.adapterPosition])
            }

        }

        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(recyclerView)

        btnAdd.setOnClickListener {
            val intent = Intent(this,AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }


    private fun fetchAll(){
        GlobalScope.launch {


            transactions = db.transactionDao().getAll()

            runOnUiThread {
                updateDashboard()
                transactionAdapter.setData(transactions)
            }
        }
    }
    private fun updateDashboard(){
        val totalAmount = transactions.map { it.amount }.sum()
        val budgetAmout = transactions.filter { it.amount>0 }.map {it.amount  }.sum()
        val expenseAmount = totalAmount - budgetAmout

        totalBalance.text = "₹ %.2f".format(totalAmount)
        txtBudget.text = "₹ %.2f".format(budgetAmout)
        txtExpense.text = "₹ %.2f".format(expenseAmount )


    }

    private fun undoDelete(){
        GlobalScope.launch {
            db.transactionDao().insertAll(deletedTransaction)

            transactions = oldTransactions

            runOnUiThread {
                transactionAdapter.setData(transactions)
                updateDashboard()
            }
        }

    }

    private fun showSnackbar(){
        val view = findViewById<View>(com.google.android.material.R.id.coordinator)
        val snackbar = Snackbar.make(view,"Transaction Deleted", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo"){
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this,red))
            .setTextColor(ContextCompat.getColor(this, white))
            .show()

    }

    private fun deleteTransaction(transaction: Transaction){
        deletedTransaction = transaction

        //saving the deleted transaction into oldTransaction in case of undo
        oldTransactions = transactions
        GlobalScope.launch {
            db.transactionDao().delete(transaction)
            transactions = transactions.filter { it.id!= transaction.id }
            runOnUiThread {
                updateDashboard()
                transactionAdapter.setData(transactions)
                showSnackbar()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}