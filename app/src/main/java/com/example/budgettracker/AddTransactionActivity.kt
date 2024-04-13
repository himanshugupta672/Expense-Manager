package com.example.budgettracker

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddTransactionActivity : AppCompatActivity() {
    lateinit var btnClose : ImageButton
    lateinit var btnAddTransaction: Button
    lateinit var descriptionInput: TextInputEditText
    lateinit var descriptionLayout: TextInputLayout
    lateinit var amountInput: TextInputEditText
    lateinit var amountLayout: TextInputLayout
    lateinit var labelInput: TextInputEditText
    lateinit var labelLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        btnAddTransaction = findViewById(R.id.btnAddTransaction)
        descriptionInput = findViewById(R.id.descriptionInput)
        descriptionLayout = findViewById(R.id.descriptionLayout)
        amountInput = findViewById(R.id.amountInput)
        amountLayout = findViewById(R.id.amountLabel)
        labelInput = findViewById(R.id.labelInput)
        labelLayout = findViewById(R.id.labelLayout)
        btnClose =  findViewById(R.id.btnClose)


        labelInput.addTextChangedListener {
            if(it!!.count()>0){
                labelLayout.error = null
            }
        }

        amountInput.addTextChangedListener {
            if(it!!.count()>0){
                amountLayout.error = null
            }
        }

        btnAddTransaction.setOnClickListener {
            val lable = labelInput.text.toString()
            val description = descriptionInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull()

            if(lable.isEmpty()){
                labelLayout.error = "Please enter a valid label"
            }

            else if(amount==null){
                amountLayout.error = "Please enter a valid amount"
            }
            else{
                val transaction = Transaction(0,lable,amount,description)
                insert(transaction)
            }
        }

        btnClose.setOnClickListener {
            finish()
        }

    }
    private fun insert(transaction: Transaction){
        val  db= Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions").build()

        GlobalScope.launch {
            db.transactionDao().insertAll(transaction)
            finish()
        }
    }
}